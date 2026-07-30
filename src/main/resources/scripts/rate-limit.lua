-- KEYS[1] = ratelimit:{메서드}:{식별자}
-- ARGV[1] = windowSeconds
-- return  : 현재 카운트

local count = redis.call('INCR', KEYS[1])

if count == 1 then
    redis.call('EXPIRE', KEYS[1], ARGV[1])
end

return count