package com.nuaa.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 用户 */
@Data
@TableName("tb_user")
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 学号（登录账号） */
    private String studentNo;

    private String nickname;

    private String password;

    private String phone;

    private String avatar;

    /** 学院 */
    private String school;

    /** 积分 */
    private Integer credit;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
