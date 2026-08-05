-- KEYS[1] = queue:active:{scheduleId}
-- KEYS[2] = queue:wait:{scheduleId}
-- KEYS[3] = queue:seq:{scheduleId}
-- KEYS[4] = queue:schedules
--
-- ARGV[1] = memberId
-- ARGV[2] = activeExpireAt(ms)
-- ARGV[3] = capacity
-- ARGV[4] = now(ms)
-- ARGV[5] = scheduleId
--
-- return: 1 = active, 0 = waiting

redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[4])

if redis.call('ZSCORE', KEYS[1], ARGV[1]) then
    return 1
end

if redis.call('ZSCORE', KEYS[2], ARGV[1]) then
    return 0
end

if redis.call('ZCARD', KEYS[2]) == 0
        and redis.call('ZCARD', KEYS[1]) < tonumber(ARGV[3]) then
    redis.call('ZADD', KEYS[1], ARGV[2], ARGV[1])
    redis.call('SADD', KEYS[4], ARGV[5])
    return 1
end

local sequence = redis.call('INCR', KEYS[3])
redis.call('ZADD', KEYS[2], sequence, ARGV[1])
redis.call('SADD', KEYS[4], ARGV[5])

return 0