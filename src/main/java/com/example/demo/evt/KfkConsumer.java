package com.example.demo.evt;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class KfkConsumer {

    @Resource
    KafkaTemplate kafkaTemplate;

    @KafkaListener(topics = "firm-huobi-future-prod", groupId = "bugfix_test")
    public void reSent(ConsumerRecord consumerRecord) {
        try {

            Object key = consumerRecord.key();
            Object value = consumerRecord.value();
            long timestamp = consumerRecord.timestamp();
            Long lastTime = System.currentTimeMillis() - 60 * 60 * 1000;
            if (value == null || key == null || timestamp <= lastTime) {
                System.out.println("received okex topic value is null or timestamp is out of limit time!");
                return;
            }

            System.out.println(value);
            //kafkaTemplate.send("",value.toString());

        } catch (Throwable e) {
            System.out.println("接收Kafka中行情信息出错");
        }
    }
}