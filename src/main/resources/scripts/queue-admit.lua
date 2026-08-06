-- KEYS[1] = queue:active:{scheduleId}
-- KEYS[2] = queue:wait:{scheduleId}
-- KEYS[3] = queue:seq:{scheduleId}
-- KEYS[4] = queue:schedules
--
-- ARGV[1] = now(ms)
-- ARGV[2] = activeExpireAt(ms)
-- ARGV[3] = capacity
-- ARGV[4] = scheduleId
--
-- return = 승급된 회원 수

redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1])

local activeCount = redis.call('ZCARD', KEYS[1])
local slots = tonumber(ARGV[3]) - activeCount
local admittedCount = 0

if slots > 0 then
    local members = redis.call('ZRANGE', KEYS[2], 0, slots - 1)

    for _, member in ipairs(members) do
        redis.call('ZREM', KEYS[2], member)
        redis.call('ZADD', KEYS[1], ARGV[2], member)
        admittedCount = admittedCount + 1
    end
end

local remainingActive = redis.call('ZCARD', KEYS[1])
local remainingWaiting = redis.call('ZCARD', KEYS[2])

if remainingActive == 0 and remainingWaiting == 0 then
    redis.call('DEL', KEYS[3])
    redis.call('SREM', KEYS[4], ARGV[4])
end

return admittedCount