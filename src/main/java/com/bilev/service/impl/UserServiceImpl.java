package com.bilev.service.impl;

import com.bilev.dao.api.BlockDao;
import com.bilev.dao.api.ContractDao;
import com.bilev.dao.api.RoleDao;
import com.bilev.dao.api.UserDao;
import com.bilev.dto.BasicContractDto;
import com.bilev.dto.BasicUserDto;
import com.bilev.dto.ContractDto;
import com.bilev.dto.UserDto;
import com.bilev.exception.NotFoundException;
import com.bilev.exception.UnableToSaveException;
import com.bilev.model.*;
import com.bilev.service.api.UserService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private BlockDao blockDao;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ContractDao contractDao;

    @Autowired
    private ShaPasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) throws NotFoundException {
        User user = userDao.findByEmail(email);
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(int userId) throws NotFoundException {
        try {
            User user = user = userDao.getByKey(userId);
            return modelMapper.map(user, UserDto.class);
        } catch (NotFoundException e) {
            throw new NotFoundException("User not found", e);
        }

    }

    @Override
    @Transactional(readOnly = true)
    public List<BasicUserDto> getAllUsers() {
        List<User> users = userDao.getAllUsers();

        return modelMapper.map(users, new TypeToken<List<BasicUserDto>>() {}.getType());
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public int saveUser(BasicUserDto basicUserDto) throws UnableToSaveException {
        User user = modelMapper.map(basicUserDto, User.class);

        if (user.getRole() == null) {
            user.setRole(roleDao.getRoleByName(Role.RoleName.ROLE_CLIENT));
        }

        user.setPassword(passwordEncoder.encodePassword(user.getPassword(), null));

        try {
            userDao.saveOrUpdate(user);
        } catch (UnableToSaveException e) {
            throw new UnableToSaveException("Email is not unique / fields are empty", e);
        }

        return  user.getId();
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public int saveContract(BasicContractDto contractDto) throws UnableToSaveException {
        Contract contract = modelMapper.map(contractDto, Contract.class);

        if (contract.getBalance() == null) contract.setBalance(0.0);
        if (contract.getBlock() == null) contract.setBlock(blockDao.getBlockByType(Block.BlockType.NON));

        try {
            contractDao.saveOrUpdate(contract);
        } catch (UnableToSaveException e) {
            throw new UnableToSaveException("Phone number is not unique / fields are empty", e);
        }

        return contract.getId();
    }
}