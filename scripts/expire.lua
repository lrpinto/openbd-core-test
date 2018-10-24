for i,k in ipairs(KEYS) do
redis.call('PEXPIRE', k, 1)
end
return true;