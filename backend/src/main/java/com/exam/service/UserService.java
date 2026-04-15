package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.BusinessException;
import com.exam.dto.UserDTO;
import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import com.exam.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public Page<User> getUserList(Integer page, Integer size, Integer role) {
        if (!UserContext.isTeacher()) {
            throw new BusinessException("无权限访问");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    public User createUser(UserDTO dto) {
        if (!UserContext.isTeacher()) {
            throw new BusinessException("无权限操作");
        }

        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
        );
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()));
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole() != null ? dto.getRole() : 0);
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        userMapper.insert(user);

        log.info("教师[{}]创建用户[{}]", UserContext.getUserId(), user.getUsername());
        user.setPassword(null);
        return user;
    }

    public User updateUser(Long id, UserDTO dto) {
        if (!UserContext.isTeacher()) {
            throw new BusinessException("无权限操作");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (dto.getRealName() != null) {
            user.setRealName(dto.getRealName());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        userMapper.updateById(user);

        log.info("教师[{}]更新用户[{}]", UserContext.getUserId(), id);
        user.setPassword(null);
        return user;
    }

    public void deleteUser(Long id) {
        if (!UserContext.isTeacher()) {
            throw new BusinessException("无权限操作");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRole() == 1) {
            throw new BusinessException("不能删除教师账号");
        }

        userMapper.deleteById(id);
        log.info("教师[{}]删除用户[{}]", UserContext.getUserId(), id);
    }

    public void resetPassword(Long id) {
        if (!UserContext.isTeacher()) {
            throw new BusinessException("无权限操作");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        userMapper.updateById(user);
        log.info("教师[{}]重置用户[{}]密码", UserContext.getUserId(), id);
    }
}
