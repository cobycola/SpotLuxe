package com.SpotLuxe;

import com.SpotLuxe.entity.Shop;
import com.SpotLuxe.service.impl.ShopServiceImpl;

import com.SpotLuxe.common.utils.CacheClient;
import com.SpotLuxe.common.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.SpotLuxe.common.constant.RedisConstants.CACHE_SHOP_KEY;
import static java.util.concurrent.TimeUnit.SECONDS;

@SpringBootTest
class SpotLuxeApplicationTests {
    @Resource
    private CacheClient cacheClient;
    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private RedisIdWorker redisIdWorker;

    private ExecutorService es= Executors.newFixedThreadPool(500);
    @Test
    void testIdWorker() throws InterruptedException {
        // 创建一个CountDownLatch，初始计数为300，用于等待300个任务完成
        CountDownLatch countDownLatch = new CountDownLatch(300);
        // 定义一个Runnable任务
        Runnable task = () -> {
            // 每个任务生成100个ID
            for (int i = 0; i < 100; i++) {
                // 使用redisIdWorker生成一个订单ID
                long id = redisIdWorker.nextId("order");
                // 打印生成的ID
                System.out.println("id = " + id);
            }
            // 任务完成后，减少CountDownLatch的计数
            countDownLatch.countDown();
        };
        // 记录测试开始时间
        long begin = System.currentTimeMillis();
        // 提交300个任务到线程池
        for(int i=0;i<300; i++){
            es.submit(task);
        }
        // 等待所有任务完成
        countDownLatch.await();
        // 记录测试结束时间
        long end=System.currentTimeMillis();
        // 打印总耗时
        System.out.println("time = "+(end-begin));
    }
    @Test
    void testSaveShop() throws InterruptedException {
        Shop shop = shopService.getById(1L);
        cacheClient.setWithLogicalExpire(CACHE_SHOP_KEY+1L,shop,10L,SECONDS);
    }
}
