package com.nuaa.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nuaa.secondhand.dto.LoginFormDTO;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.User;

public interface UserService extends IService<User> {

    /** 发送验证码（开发环境直接返回，生产接短信服务） */
    Result sendCode(String studentNo);

    /** 验证码登录：学号不存在则自动注册 */
    Result login(LoginFormDTO loginForm);

    /** 获取当前登录用户 */
    Result me();

    /** 用户列表（含信誉分、在售商品数） */
    Result listUsers();

    /** 用户详情 */
    Result userDetail(Long id);

    /** 用户发布的在售商品 */
    Result userGoods(Long id);
}
