package com.SpotLuxe.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.SpotLuxe.dto.Result;
import com.SpotLuxe.entity.VoucherOrder;
import com.SpotLuxe.mapper.VoucherOrderMapper;
import com.SpotLuxe.service.ISeckillVoucherService;
import com.SpotLuxe.service.IVoucherOrderService;
import com.SpotLuxe.utils.RedisIdWorker;
import com.SpotLuxe.utils.UserHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class VoucherOrderSerivceImpl extends ServiceImpl<VoucherOrderMapper,VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024*1024);

    private static final ExecutorService SECKILL_ORDER_EXECUTOR= Executors.newSingleThreadExecutor();

    private IVoucherOrderService proxy;
    @PostConstruct
    private void init(){
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }
    @PostConstruct
    public void initConsumerGroup() {
        try {
            stringRedisTemplate.opsForStream().createGroup("stream.orders", "g1 ");
        } catch (Exception e) {
            log.info("消费者组已存在: {}", e.getMessage());
        }
    }
    private class VoucherOrderHandler implements Runnable {
        String queryname = "stream.orders";
        @Override
        public void run() {
            while (true) {
                try {
                    //获取队列中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS stream.orders >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream()
                            .read(Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queryname, ReadOffset.lastConsumed())
                    );
                    //判断获取订单信息是否成功
                    if (list == null || list.isEmpty()) {
                        //如果获取失败，说明没有信息，继续下一次循环
                        continue;
                    }
                    //如果获取成功，可以下单
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                    //创建订单
                    handleVoucherOrder(voucherOrder);
                    //ACK确认
                    stringRedisTemplate.opsForStream().acknowledge(queryname, "g1", record.getId());
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                    handlePendingList();
                }
            }
        }
    }
    private void handlePendingList() {
        String queryname = "stream.orders";
        while(true) {
            try {
                //获取pending-list中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 STREAMS stream.orders 0
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from("g1", "c1"),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(queryname, ReadOffset.from("0"))
                );
                //判断获取订单信息是否成功
                if (list == null || list.isEmpty()) {
                    //如果获取失败，说明pending-list没有信息，结束循环
                    break;
                }
                //如果获取成功，可以下单
                MapRecord<String, Object, Object> record = list.get(0);
                Map<Object, Object> values = record.getValue();
                VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                //创建订单
                handleVoucherOrder(voucherOrder);
                //ACK确认 SACK stream.orders g1 id
                stringRedisTemplate.opsForStream().acknowledge(queryname, "g1", record.getId());
            } catch (Exception e) {
                log.error("处理pending-list订单异常", e);
                try{
                    Thread.sleep(20);
                }catch (InterruptedException ie){
                    ie.printStackTrace();
                }
            }
        }
    }

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static{
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        //1.执行lua脚本
        Long userId=UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");
        Long result=stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),userId.toString(),String.valueOf(orderId)
        );
        int r=result.intValue();
        if(r!=0){
            return Result.fail(r==1 ? "库存不足" : "不允许重复下单");
        }
        //获取代理对象
        proxy = (IVoucherOrderService) AopContext.currentProxy();
        //保存订单到数据库
        return Result.ok(orderId);
    }
    /*@Override
    public Result seckillVoucher(Long voucherId) {
        //1.执行lua脚本
        Long userId=UserHolder.getUser().getId();
        Long result=stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.EMPTY_LIST,
                voucherId.toString(),userId.toString()
        );
        int r=result.intValue();
        if(r!=0){
            return Result.fail(r==1 ? "库存不足" : "不允许重复下单");
        }
        long orderId = redisIdWorker.nextId("order");
        //初始化订单
        VoucherOrder voucherOrder=new VoucherOrder();
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        //保存订单到阻塞队列
        stringRedisTemplate.opsForList().leftPush(
                "order:queue",orderId.toString()
        )
        orderTasks.add(voucherOrder);
        //获取代理对象
        proxy = (IVoucherOrderService) AopContext.currentProxy();
        //保存订单到数据库
        return Result.ok(orderId);

        private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024*1024);
        private class VoucherOrderHandler implements Runnable{

        @Override
        public void run(){
            while(true){
                try{
                    //从阻塞队列中获取订单
                    VoucherOrder voucherOrder=orderTasks.take();
                    //创建订单
                    handleVoucherOrder(voucherOrder);
                }catch (Exception e){
                    log.error("订单处理异常",e);
                }
            }
        }

    }
    }
    }*/
    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        //获取用户
        Long userId = voucherOrder.getUserId();
        //尝试创建订单
        try {
            proxy.createVoucherOrder(voucherOrder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        //判断用户是否购买过
        int count = query()
                .eq("user_id", voucherOrder.getUserId())
                .eq("voucher_id", voucherOrder.getVoucherId())
                .count();
        if (count > 0) {
            log.error("用户已经购买过一次！");
            return;
        }
        //扣减库存
        boolean success=seckillVoucherService.update()
                .setSql("stock=stock-1")
                .eq("voucher_id",voucherOrder.getVoucherId())
                .gt("stock",0) //where id =? and stock>0
                .update();
        if(!success){
            log.error("库存不足！！！");
            return;
        }
        save(voucherOrder);
        return;
    }


/*    @Override
    public Result seckillVoucher(Long voucherId) {
        //查询优惠券
        SeckillVoucher voucher=seckillVoucherService.getById(voucherId);
        //判断秒杀是否开始
        if(voucher.getBeginTime().isAfter(LocalDateTime.now())){
            //未开始，返回
            return Result.fail("秒杀尚未开始");
        }
        //判断秒杀是否结束
        if(voucher.getEndTime().isBefore(LocalDateTime.now())){
            return Result.fail("秒杀已经结束！");
        }
        //判断库存是否充足
        if(voucher.getStock()<=0){
            return Result.fail("库存不足！");
        }
        Long userId=UserHolder.getUser().getId();
        //创建锁对象
        RLock lock=redissonClient.getLock(LOCK_ORDER_KEY+userId);
        //获取锁 注意 锁一定要在创建事务之前获取，否则事务提交后锁就释放了，事务就无法回滚了
        boolean isLock=lock.tryLock();
        if(!isLock){
            return Result.fail("不允许重复下单！");
        }
        try {
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } finally {
            lock.unlock();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Result createVoucherOrder(Long voucherId){
        //一人一单
        //获取用户id
        Long userId=UserHolder.getUser().getId();
        //查询订单
        int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        //判断是否存在
        if(count>0){
            return Result.fail("用户已经购买过一次！");
        }
        //扣减库存
        boolean success=seckillVoucherService.update()
                .setSql("stock=stock-1")
                .eq("voucher_id",voucherId)
                .gt("stock",0)
                .update();
        if(!success){
            return Result.fail("库存不足！");
        }
        //创建订单
        VoucherOrder voucherOrder=new VoucherOrder();
        long orderId=redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        //设置用户id
        voucherOrder.setUserId(userId);
        //设置代金券id
        voucherOrder.setVoucherId(voucherId);
        this.save(voucherOrder);
        return Result.ok(orderId);
    }*/
}
