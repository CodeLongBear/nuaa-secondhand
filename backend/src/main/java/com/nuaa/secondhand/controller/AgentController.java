package com.nuaa.secondhand.controller;

import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.service.AgentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent")
public class AgentController {

    @Resource
    private AgentService agentService;

    /** 智能商品推荐 */
    @GetMapping("/recommend")
    public Result recommend() {
        return agentService.recommend();
    }

    /** 智能议价/咨询 */
    @PostMapping("/consult/{goodsId}")
    public Result consult(@PathVariable Long goodsId, @RequestParam String question) {
        return agentService.consult(goodsId, question);
    }
}
