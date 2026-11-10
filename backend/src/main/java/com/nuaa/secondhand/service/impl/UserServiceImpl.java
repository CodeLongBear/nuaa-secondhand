package com.nuaa.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuaa.secondhand.dto.LoginFormDTO;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Goods;
import com.nuaa.secondhand.entity.User;
import com.nuaa.secondhand.mapper.GoodsMapper;
import com.nuaa.secondhand.mapper.UserMapper;
import com.nuaa.secondhand.service.UserService;
import com.nuaa.secondhand.utils.RedisConstants;
import com.nuaa.secondhand.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/** 用户服务：验证码 + 登录 + 注册 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private GoodsMapper goodsMapper;

    @Override
    public Result sendCode(String studentNo) {
        // 生成 6 位验证码
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        stringRedisTemplate.opsForValue().set(
                RedisConstants.LOGIN_CODE_KEY + studentNo,
                code,
                RedisConstants.LOGIN_CODE_TTL,
                TimeUnit.MINUTES
        );
        // 开发环境直接返回验证码，生产应改为发送短信
        log.info("学号 {} 的验证码：{}", studentNo, code);
        return Result.ok(code);
    }

    @Override
    public Result login(LoginFormDTO loginForm) {
        String studentNo = loginForm.getStudentNo();
        String code = loginForm.getCode();

        // 1. 校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + studentNo);
        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.fail("验证码错误或已过期");
        }

        // 2. 查询用户，不存在则自动注册
        User user = query().eq("student_no", studentNo).one();
        if (user == null) {
            user = new User();
            user.setStudentNo(studentNo);
            user.setNickname("同学" + studentNo.substring(studentNo.length() - 4));
            user.setCredit(0);
            save(user);
        }

        // 3. 生成 token 存 Redis
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = RedisConstants.LOGIN_USER_KEY + token;
        Map<String, String> userMap = new HashMap<>();
        userMap.put("id", user.getId().toString());
        userMap.put("nickname", user.getNickname());
        userMap.put("studentNo", user.getStudentNo());
        stringRedisTemplate.opsForHash().putAll(key, userMap);
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        return Result.ok(token);
    }

    @Override
    public Result me() {
        Map<Object, Object> user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("未登录");
        }
        return Result.ok(user);
    }

    @Override
    public Result listUsers() {
        List<User> users = list(new LambdaQueryWrapper<User>().orderByDesc(User::getCredit));
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("nickname", u.getNickname());
            m.put("studentNo", u.getStudentNo());
            m.put("school", u.getSchool());
            m.put("credit", u.getCredit());
            m.put("goodsCount", goodsMapper.selectCount(new LambdaQueryWrapper<Goods>()
                    .eq(Goods::getSellerId, u.getId()).eq(Goods::getStatus, 1)));
            result.add(m);
        }
        return Result.ok(result);
    }

    @Override
    public Result userDetail(Long id) {
        User u = getById(id);
        if (u == null) {
            return Result.fail("用户不存在");
        }
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("nickname", u.getNickname());
        m.put("studentNo", u.getStudentNo());
        m.put("school", u.getSchool());
        m.put("credit", u.getCredit());
        return Result.ok(m);
    }

    @Override
    public Result userGoods(Long id) {
        List<Goods> goods = goodsMapper.selectList(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getSellerId, id)
                .eq(Goods::getStatus, 1)
                .orderByDesc(Goods::getCreateTime));
        return Result.ok(goods);
    }
}
