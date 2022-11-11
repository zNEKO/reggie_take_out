package com.neko.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neko.reggie.entity.User;
import com.neko.reggie.service.UserService;
import com.neko.reggie.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author z-nek
* @description 针对表【user(用户信息)】的数据库操作Service实现
* @createDate 2022-11-05 15:51:38
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




