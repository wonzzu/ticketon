-- KEYS[1] = queue:active:{scheduleId}
-- KEYS[2] = queue:wait:{scheduleId}
-- ARGV[1] = memberId
-- ARGV[2] = expireAt(ms)
-- ARGV[3] = capacity
-- ARGV[4] = now(ms)
-- return  : 1 = 즉시 입장, 0 = 대기열로


redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[4])

if redis.call('ZSCORE', KEYS[1], ARGV[1]) then
    return 1
end

if redis.call('ZCARD', KEYS[2]) > 0 then
    return 0
end

if redis.call('ZCARD', KEYS[1]) < tonumber(ARGV[3]) then
    redis.call('ZADD', KEYS[1], ARGV[2], ARGV[1])
    return 1
end

return 0