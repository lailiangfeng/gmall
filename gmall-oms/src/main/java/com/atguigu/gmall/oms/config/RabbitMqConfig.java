package com.atguigu.gmall.oms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class RabbitMqConfig {
    //定时关单步骤
    //1，oms里service发送关单消息到交换机
    //2，交换机吧消息路由到延时队列  没有消费者 ttlBinding()
    //3，延时队列变成死信息 到死信交换机  ttlQueue()
    //4，死信交换机把消息路由到死信队列 dlqueue()
    //5，死信队列的消息被消费  deadBinding()
    //6.listener 里监听死信队列 消费消息
    /**
     * 延时队列绑定到交换机
     * rountingKey：order.ttl
     * @return
     */
    @Bean("ORDER-TTL-BINDING")
    public Binding ttlBinding(){

        return new Binding("ORDER-TTL-QUEUE", Binding.DestinationType.QUEUE, "GMALL-ORDER-EXCHANGE", "order.ttl", null);
    }


    /**
     * 延时队列
     * @return
     */
    @Bean("ORDER-TTL-QUEUE")
    public Queue ttlQueue(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange","GMALL-ORDER-EXCHANGE");//交换机
        map.put("x-dead-letter-routing-key","order.dead");//routing-key
        map.put("x-message-ttl",120000);//延时时间
        return new Queue("ORDER-TTL-QUEUE",true,false,false,map);
    }



    /**
     * 声名死信队列
     * @return
     */
    @Bean("ORDER-DEAD-QUEUE")
    public Queue dlqueue(){

        return new Queue("ORDER-DEAD-QUEUE", true, false, false, null);
    }

    /**
     * 死信队列绑定到交换机
     * routingKey：order.close
     * @return
     */
    @Bean("ORDER-DEAD-BINDING")
    public Binding deadBinding(){

        return new Binding("ORDER-DEAD-QUEUE", Binding.DestinationType.QUEUE, "GMALL-ORDER-EXCHANGE", "order.dead", null);
    }


}
