package com.nuaa.secondhand.controller;

import com.nuaa.secondhand.dto.LoginFormDTO;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /** 发送验证码 */
    @PostMapping("/code")
    public Result sendCode(@RequestParam String studentNo) {
        return userService.sendCode(studentNo);
    }

    /** 登录 */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm) {
        return userService.login(loginForm);
    }

    /** 当前登录用户 */
    @GetMapping("/me")
    public Result me() {
        return userService.me();
    }
}
