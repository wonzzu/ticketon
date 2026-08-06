-- KEYS[1] = coupon:issued:{couponId}
-- KEYS[2] = coupon:stock:{couponId}
-- ARGV[1] = memberId
--
-- return: 1 = 보상 완료, 0 = 보상 대상 없음

local removed = redis.call('SREM', KEYS[1], ARGV[1])

if removed == 0 then
    return 0
end

redis.call('INCR', KEYS[2])

return 1