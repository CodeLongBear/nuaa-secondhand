package com.nuaa.secondhand.service;

import com.nuaa.secondhand.dto.Result;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AgentService {

    /** 智能商品推荐 */
    Result recommend();

    /** 智能议价/咨询助手 */
    Result consult(Long goodsId, String question);

    /** 智能商品推荐（SSE 流式） */
    SseEmitter recommendStream();

    /** 智能议价/咨询（SSE 流式） */
    SseEmitter consultStream(Long goodsId, String question);
}
