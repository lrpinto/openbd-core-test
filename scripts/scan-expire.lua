redis.replicate_commands()
local scanned_keys = {}
local done = false
local cursor = "0"
repeat
local result = redis.call("SCAN", cursor, "MATCH", ARGV[1], "count", ARGV[2])
cursor = result[1]
scanned_keys = result[2]
for i,k in pairs(scanned_keys) do
redis.call('EXPIRE', k, 1)
end
if cursor == 0 then
done = true
end
until done
return true;