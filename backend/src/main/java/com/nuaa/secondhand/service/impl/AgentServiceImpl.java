package com.nuaa.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Goods;
import com.nuaa.secondhand.mapper.GoodsMapper;
import com.nuaa.secondhand.service.AgentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** AI Agent：智能推荐 + 议价咨询（DeepSeek） */
@Slf4j
@Service
public class AgentServiceImpl implements AgentService {

    private static final String RECOMMEND_SYSTEM =
            "你是校园二手交易平台的智能推荐助手。根据候选商品列表，结合大学生常见需求（教材、数码、生活用品等），"
                    + "推荐 3 个最值得关注的商品，每个附一句推荐理由。只推荐列表中的商品，不要编造。";

    private static final String CONSULT_SYSTEM =
            "你是校园二手交易平台的议价咨询助手。请基于给定的商品信息和同类商品价格数据，回答用户关于价格是否合理、"
                    + "是否值得购买的问题。数据不足时明确说明，不要编造价格。";

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url}")
    private String baseUrl;

    @Value("${deepseek.model}")
    private String model;

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public Result recommend() {
        try {
            List<Goods> candidates = goodsMapper.selectList(
                    new LambdaQueryWrapper<Goods>().eq(Goods::getStatus, 1).last("LIMIT 20"));
            if (candidates == null || candidates.isEmpty()) {
                return Result.fail("暂无在售商品");
            }
            StringBuilder sb = new StringBuilder("候选商品：\n");
            for (Goods g : candidates) {
                sb.append("- ").append(g.getTitle())
                        .append("（").append(g.getCategory())
                        .append("，￥").append(g.getPrice()).append("）\n");
            }
            String answer = callDeepSeek(RECOMMEND_SYSTEM, sb.toString());
            return Result.ok(answer);
        } catch (Exception e) {
            log.error("智能推荐失败", e);
            return Result.fail("智能推荐暂不可用，请稍后再试");
        }
    }

    @Override
    public Result consult(Long goodsId, String question) {
        try {
            Goods goods = goodsMapper.selectById(goodsId);
            if (goods == null) {
                return Result.fail("商品不存在");
            }
            List<Goods> similar = goodsMapper.selectList(
                    new LambdaQueryWrapper<Goods>()
                            .eq(Goods::getCategory, goods.getCategory())
                            .eq(Goods::getStatus, 1)
                            .last("LIMIT 10"));
            StringBuilder sb = new StringBuilder();
            sb.append("目标商品：").append(goods.getTitle())
                    .append("，成色：").append(goods.getConditionDesc() == null ? "未说明" : goods.getConditionDesc())
                    .append("，售价 ￥").append(goods.getPrice()).append("\n");
            sb.append("同类商品价格：\n");
            for (Goods g : similar) {
                sb.append("- ").append(g.getTitle()).append(" ￥").append(g.getPrice()).append("\n");
            }
            sb.append("\n用户问题：").append(question);
            String answer = callDeepSeek(CONSULT_SYSTEM, sb.toString());
            return Result.ok(answer);
        } catch (Exception e) {
            log.error("议价咨询失败", e);
            return Result.fail("咨询助手暂不可用，请稍后再试");
        }
    }

    private String callDeepSeek(String system, String user) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", user)
        ));
        body.put("temperature", 0.3);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(baseUrl + "/chat/completions", entity, String.class);
        JsonNode root = objectMapper.readTree(resp.getBody());
        return root.path("choices").get(0).path("message").path("content").asText();
    }
}
