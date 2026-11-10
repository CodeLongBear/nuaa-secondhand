package com.nuaa.secondhand.service;

import com.nuaa.secondhand.dto.Result;

public interface SignService {

    /** 每日签到（BitMap） */
    Result sign();

    /** 本月连续签到天数 */
    Result signCount();
}
