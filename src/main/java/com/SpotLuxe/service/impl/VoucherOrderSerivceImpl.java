package com.SpotLuxe.service.impl;

import com.SpotLuxe.dto.Result;
import com.SpotLuxe.entity.SeckillVoucher;
import com.SpotLuxe.entity.VoucherOrder;
import com.SpotLuxe.mapper.VoucherOrderMapper;
import com.SpotLuxe.service.ISeckillVoucherService;
import com.SpotLuxe.service.IVoucherOrderService;
import com.SpotLuxe.utils.RedisIdWorker;
import com.SpotLuxe.utils.UserHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;


@Service
public class VoucherOrderSerivceImpl extends ServiceImpl<VoucherOrderMapper,VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Override
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
        synchronized (userId.toString().intern()){
            //获取代理对象
            IVoucherOrderService proxy=(IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Result createVoucherOrder(Long voucherId){
        //一人一单
        //获取用户id
        Long userId=UserHolder.getUser().getId();

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
    }
}
