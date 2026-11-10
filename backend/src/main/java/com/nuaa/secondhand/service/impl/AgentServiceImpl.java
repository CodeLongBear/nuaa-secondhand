package com.nuaa.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Favorite;
import com.nuaa.secondhand.entity.Goods;
import com.nuaa.secondhand.entity.Order;
import com.nuaa.secondhand.mapper.FavoriteMapper;
import com.nuaa.secondhand.mapper.GoodsMapper;
import com.nuaa.secondhand.mapper.OrderMapper;
import com.nuaa.secondhand.service.AgentService;
import com.nuaa.secondhand.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/** AI Agent：智能推荐 + 议价咨询（DeepSeek）。推荐基于用户收藏偏好个性化，咨询含历史成交价区间，均支持 SSE 流式输出。 */
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
    private FavoriteMapper favoriteMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource(name = "asyncExecutor")
    private Executor asyncExecutor;

    // ==================== 同步接口 ====================

    @Override
    public Result recommend() {
        try {
            String prompt = buildRecommendPrompt();
            if (prompt == null) {
                return Result.fail("暂无在售商品");
            }
            String answer = callDeepSeek(RECOMMEND_SYSTEM, prompt);
            return Result.ok(answer);
        } catch (Exception e) {
            log.error("智能推荐失败", e);
            return Result.fail("智能推荐暂不可用，请稍后再试");
        }
    }

    @Override
    public Result consult(Long goodsId, String question) {
        try {
            String prompt = buildConsultPrompt(goodsId, question);
            if (prompt == null) {
                return Result.fail("商品不存在");
            }
            String answer = callDeepSeek(CONSULT_SYSTEM, prompt);
            return Result.ok(answer);
        } catch (Exception e) {
            log.error("议价咨询失败", e);
            return Result.fail("咨询助手暂不可用，请稍后再试");
        }
    }

    // ==================== SSE 流式接口 ====================

    @Override
    public SseEmitter recommendStream() {
        SseEmitter emitter = new SseEmitter(120000L);
        asyncExecutor.execute(() -> {
            try {
                String prompt = buildRecommendPrompt();
                if (prompt == null) {
                    emitter.send(SseEmitter.event().data("暂无在售商品"));
                    emitter.complete();
                    return;
                }
                callDeepSeekStream(RECOMMEND_SYSTEM, prompt, emitter);
            } catch (Exception e) {
                log.error("流式推荐失败", e);
                completeWithError(emitter, e);
            }
        });
        return emitter;
    }

    @Override
    public SseEmitter consultStream(Long goodsId, String question) {
        SseEmitter emitter = new SseEmitter(120000L);
        asyncExecutor.execute(() -> {
            try {
                String prompt = buildConsultPrompt(goodsId, question);
                if (prompt == null) {
                    emitter.send(SseEmitter.event().data("商品不存在"));
                    emitter.complete();
                    return;
                }
                callDeepSeekStream(CONSULT_SYSTEM, prompt, emitter);
            } catch (Exception e) {
                log.error("流式咨询失败", e);
                completeWithError(emitter, e);
            }
        });
        return emitter;
    }

    // ==================== 候选构建 ====================

    /** 构建推荐 prompt：优先基于用户收藏偏好取候选，无偏好则取最新在售 */
    private String buildRecommendPrompt() {
        Long userId = UserHolder.getUserId();
        String preference = null;
        if (userId != null) {
            List<Favorite> favs = favoriteMapper.selectList(
                    new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId).last("LIMIT 10"));
            if (favs != null && !favs.isEmpty()) {
                List<Long> ids = favs.stream().map(Favorite::getGoodsId).toList();
                List<Goods> favGoods = goodsMapper.selectBatchIds(ids);
                preference = favGoods.stream()
                        .filter(g -> g.getCategory() != null)
                        .collect(Collectors.groupingBy(Goods::getCategory, Collectors.counting()))
                        .entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
            }
        }

        List<Goods> candidates;
        if (preference != null) {
            candidates = goodsMapper.selectList(
                    new LambdaQueryWrapper<Goods>().eq(Goods::getStatus, 1)
                            .eq(Goods::getCategory, preference)
                            .orderByDesc(Goods::getCreateTime).last("LIMIT 20"));
            if (candidates == null || candidates.size() < 3) {
                candidates = goodsMapper.selectList(
                        new LambdaQueryWrapper<Goods>().eq(Goods::getStatus, 1)
                                .orderByDesc(Goods::getCreateTime).last("LIMIT 20"));
            }
        } else {
            candidates = goodsMapper.selectList(
                    new LambdaQueryWrapper<Goods>().eq(Goods::getStatus, 1)
                            .orderByDesc(Goods::getCreateTime).last("LIMIT 20"));
        }
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        if (preference != null) {
            sb.append("用户偏好类别：").append(preference).append("\n");
        }
        sb.append("候选商品：\n");
        for (Goods g : candidates) {
            sb.append("- ").append(g.getTitle())
                    .append("（").append(g.getCategory())
                    .append("，￥").append(g.getPrice()).append("）\n");
        }
        return sb.toString();
    }

    /** 构建咨询 prompt：目标商品 + 同类历史成交价区间 + 同类在售价 + 用户问题 */
    private String buildConsultPrompt(Long goodsId, String question) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            return null;
        }
        List<Goods> similar = goodsMapper.selectList(
                new LambdaQueryWrapper<Goods>()
                        .eq(Goods::getCategory, goods.getCategory())
                        .eq(Goods::getStatus, 1)
                        .last("LIMIT 10"));

        // 历史成交价区间（已完成订单）
        BigDecimal[] range = null;
        List<Long> similarIds = similar.stream().map(Goods::getId).toList();
        if (!similarIds.isEmpty()) {
            List<Order> doneOrders = orderMapper.selectList(
                    new LambdaQueryWrapper<Order>().eq(Order::getStatus, 3).in(Order::getGoodsId, similarIds));
            if (doneOrders != null && !doneOrders.isEmpty()) {
                List<BigDecimal> prices = doneOrders.stream().map(Order::getPrice).filter(Objects::nonNull).toList();
                if (!prices.isEmpty()) {
                    BigDecimal min = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                    BigDecimal max = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                    BigDecimal avg = prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);
                    range = new BigDecimal[]{min, max, avg};
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("目标商品：").append(goods.getTitle())
                .append("，成色：").append(goods.getConditionDesc() == null ? "未说明" : goods.getConditionDesc())
                .append("，售价 ￥").append(goods.getPrice()).append("\n");
        if (range != null) {
            sb.append("同类商品历史成交价区间：最低 ￥").append(range[0])
                    .append("，最高 ￥").append(range[1])
                    .append("，均价 ￥").append(range[2]).append("\n");
        }
        sb.append("同类在售商品价格：\n");
        for (Goods g : similar) {
            sb.append("- ").append(g.getTitle()).append(" ￥").append(g.getPrice()).append("\n");
        }
        sb.append("\n用户问题：").append(question);
        return sb.toString();
    }

    // ==================== DeepSeek 调用 ====================

    /** 同步一次性调用 */
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

    /** 流式调用（stream=true），逐 token 写入 SseEmitter */
    private void callDeepSeekStream(String system, String user, SseEmitter emitter) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(baseUrl + "/chat/completions");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(60000);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", system),
                    Map.of("role", "user", "content", user)
            ));
            body.put("temperature", 0.3);
            body.put("stream", true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(objectMapper.writeValueAsBytes(body));
            }

            int code = conn.getResponseCode();
            if (code != 200) {
                emitter.send(SseEmitter.event().data("[AI 服务返回错误: " + code + "]"));
                emitter.complete();
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        try {
                            JsonNode root = objectMapper.readTree(data);
                            String delta = root.path("choices").get(0).path("delta").path("content").asText();
                            if (delta != null && !delta.isEmpty()) {
                                emitter.send(SseEmitter.event().data(delta));
                            }
                        } catch (Exception ignore) {
                            // 忽略无法解析的数据行
                        }
                    }
                }
            }
            emitter.complete();
        } catch (Exception e) {
            log.error("DeepSeek 流式调用失败", e);
            completeWithError(emitter, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void completeWithError(SseEmitter emitter, Exception e) {
        try {
            emitter.send(SseEmitter.event().data("[AI 服务暂不可用，请稍后再试]"));
            emitter.complete();
        } catch (Exception ignore) {
            emitter.completeWithError(e);
        }
    }
}
