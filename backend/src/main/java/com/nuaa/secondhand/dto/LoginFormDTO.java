package com.nuaa.secondhand.dto;

import lombok.Data;

/** 登录表单 */
@Data
public class LoginFormDTO {

    /** 学号 */
    private String studentNo;

    /** 验证码 */
    private String code;
}
