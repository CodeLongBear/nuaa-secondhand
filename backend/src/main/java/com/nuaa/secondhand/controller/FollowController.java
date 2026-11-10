package com.nuaa.secondhand.controller;

import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.service.FollowService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private FollowService followService;

    /** 关注/取关 */
    @PutMapping("/{followUserId}/{isFollow}")
    public Result follow(@PathVariable Long followUserId, @PathVariable Boolean isFollow) {
        return followService.follow(followUserId, isFollow);
    }

    /** 是否已关注 */
    @GetMapping("/isFollow/{followUserId}")
    public Result isFollow(@PathVariable Long followUserId) {
        return followService.isFollow(followUserId);
    }

    /** 共同关注 */
    @GetMapping("/common/{otherId}")
    public Result common(@PathVariable Long otherId) {
        return followService.commonFollow(otherId);
    }

    /** 关注动态 Feed 流 */
    @GetMapping("/feed")
    public Result feed() {
        return followService.feed();
    }
}
