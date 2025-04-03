--1.参数列表
--1.1.优惠券id
--1.2.用户id
local voucherId=ARGV[1]
local userId=ARGV[2]
local orderId=ARGV[3]
local stockKey='seckill:stock:'..voucherId
local orderKey='seckill:order:'..voucherId
-- 2.判断库存是否充足
if(tonumber(redis.call('get', stockKey))<=0)then
    return 1
end
-- 3.判断用户是否购买过
if(redis.call('sismember',orderKey,userId)==1)then
    return 2
end
--4.扣减库存
redis.call('incrby',stockKey,-1)
--5.下单
redis.call('sadd',orderKey,userId)
--6.将订单加入队列
redis.call('xadd','stream.orders','*','userId',userId,'voucherId',voucherId,'id',orderId);
return 0
