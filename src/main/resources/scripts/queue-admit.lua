-- KEYS[1] = queue:active:{scheduleId}
-- KEYS[2] = queue:wait:{scheduleId}
--
-- ARGV[1] = now(ms)
-- ARGV[2] = activeExpireAt(ms)
-- ARGV[3] = capacity
--
-- return = 승급된 회원 수

redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1])

local activeCount = redis.call('ZCARD', KEYS[1])
local slots = tonumber(ARGV[3]) - activeCount

if slots <= 0 then
    return 0
end

local members = redis.call('ZRANGE', KEYS[2], 0, slots - 1)

if #members == 0 then
    return 0
end

for _, member in ipairs(members) do
    redis.call('ZREM', KEYS[2], member)
    redis.call('ZADD', KEYS[1], ARGV[2], member)
end

return #members