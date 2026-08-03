-- KEYS[1] = coupon:issued:{couponId}
-- KEYS[2] = coupon:stock:{couponId}
-- ARGV[1] = memberId
-- return  : 남은 재고(0 이상) / -1 = 이미 발급 / -2 = 소진

if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
    return -1
end

local stock = tonumber(redis.call('GET', KEYS[2]))

if stock == nil or stock <= 0 then
    return -2
end

redis.call('DECR', KEYS[2])
redis.call('SADD', KEYS[1], ARGV[1])

return stock - 1