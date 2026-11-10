-- 秒杀下单：原子地判断库存 + 去重 + 扣减
-- KEYS[1]: 库存 key（seckill:stock:{goodsId}）
-- KEYS[2]: 下单标记 key（seckill:order:{goodsId}，set 存 userId）
-- ARGV[1]: 用户 id
-- 返回：0 成功 / 1 库存不足 / 2 重复下单

local stock = tonumber(redis.call('get', KEYS[1]))
if stock == nil or stock <= 0 then
    return 1
end

local exist = redis.call('sismember', KEYS[2], ARGV[1])
if exist == 1 then
    return 2
end

redis.call('incrby', KEYS[1], -1)
redis.call('sadd', KEYS[2], ARGV[1])
return 0
