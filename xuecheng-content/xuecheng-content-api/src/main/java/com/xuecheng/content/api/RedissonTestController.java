package com.xuecheng.content.api;

import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Queue;

@RestController
@RequestMapping("/redisson")
public class RedissonTestController {

    @Autowired
    RedissonClient redissonClient;

    @GetMapping("/joinqueue")
    public Queue<String> joinqueue(String queuer){
        RQueue<String> queue001 = redissonClient.getQueue("queue001");
        queue001.add(queuer);
        return queue001;
    }

    @GetMapping("/removequeue")
    public String removequeue(){
        RQueue<String> queue001 = redissonClient.getQueue("queue001");
        String queuer = queue001.poll();
        return queuer;

    }
}
