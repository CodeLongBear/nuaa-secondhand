package com.nuaa.secondhand.controller;

import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.service.SignService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sign")
public class SignController {

    @Resource
    private SignService signService;

    /** 签到 */
    @PostMapping
    public Result sign() {
        return signService.sign();
    }

    /** 连续签到天数 */
    @GetMapping("/count")
    public Result count() {
        return signService.signCount();
    }
}
