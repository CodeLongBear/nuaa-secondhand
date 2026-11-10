package com.nuaa.secondhand.service;

import com.nuaa.secondhand.dto.Result;

public interface AgentService {

    /** 智能商品推荐 */
    Result recommend();

    /** 智能议价/咨询助手 */
    Result consult(Long goodsId, String question);
}
