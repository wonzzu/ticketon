-- KEYS = 좌석 키 목록 (seat:hold:{scheduleId}:{seatId})
-- ARGV[1] = memberId
-- return  : 실제로 해제한 개수

local released = 0

for i = 1, #KEYS do
    if redis.call('GET', KEYS[i]) == ARGV[1] then
        redis.call('DEL', KEYS[i])
        released = released + 1
    end
end

return released