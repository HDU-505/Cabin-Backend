package com.hdu.service.impl;

import com.hdu.entity.User;
import com.hdu.mapper.UserMapper;
import com.hdu.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author DZL
 * @since 2024-05-28
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
