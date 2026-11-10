package com.nuaa.secondhand.service.impl;

import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.service.SignService;
import com.nuaa.secondhand.utils.RedisConstants;
import com.nuaa.secondhand.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 签到服务：BitMap 记录 + BitField 统计连续天数 */
@Slf4j
@Service
public class SignServiceImpl implements SignService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sign() {
        Long userId = UserHolder.getUserId();
        LocalDate now = LocalDate.now();
        String key = RedisConstants.SIGN_KEY + userId + ":" + now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        int dayOfMonth = now.getDayOfMonth();
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return Result.ok();
    }

    @Override
    public Result signCount() {
        Long userId = UserHolder.getUserId();
        LocalDate now = LocalDate.now();
        String key = RedisConstants.SIGN_KEY + userId + ":" + now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        int dayOfMonth = now.getDayOfMonth();

        // 取出本月前 dayOfMonth 个 bit，组成无符号整数
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth))
                        .valueAt(0)
        );
        if (result == null || result.isEmpty() || result.get(0) == null) {
            return Result.ok(0);
        }
        long num = result.get(0);
        // BITFIELD 返回值的 bit0 = 今天（最大 offset），从低位往回数连续签到天数
        int count = 0;
        while (num != 0) {
            if ((num & 1L) == 1) {
                count++;
                num >>>= 1;
            } else {
                break;
            }
        }
        return Result.ok(count);
    }
}
