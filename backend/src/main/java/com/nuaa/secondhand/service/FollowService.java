package com.nuaa.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Follow;

public interface FollowService extends IService<Follow> {

    /** 关注 / 取关 */
    Result follow(Long followUserId, Boolean isFollow);

    /** 是否已关注 */
    Result isFollow(Long followUserId);

    /** 共同关注 */
    Result commonFollow(Long otherId);

    /** 关注动态 Feed 流 */
    Result feed();
}
