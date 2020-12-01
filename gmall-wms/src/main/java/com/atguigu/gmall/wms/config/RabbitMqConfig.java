package com.atguigu.gmall.wms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class RabbitMqConfig {

    //定时解库存
    /**
     * 延时队列
     * @return
     */
    @Bean("WMS-TTL-QUEUE")
    public Queue ttlQueue(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange","GMALL-ORDER-EXCHANGE");//交换机
        map.put("x-dead-letter-routing-key","stock.unlock");//routing-key
        map.put("x-message-ttl",120000);//延时时间
        return new Queue("WMS-TTL-QUEUE",true,false,false,map);
    }

    /**
     * 延时队列绑定到交换机
     * rountingKey：order.ttl
     * @return
     */
    @Bean("WMS-TTL-BINDING")
    public Binding ttlBinding(){

        return new Binding("WMS-TTL-QUEUE", Binding.DestinationType.QUEUE, "GMALL-ORDER-EXCHANGE", "stock.ttl", null);
    }

//    /**
//     * 死信队列
//     * @return
//     */
//    @Bean("WMS-DEAD-QUEUE")
//    public Queue dlqueue(){
//
//        return new Queue("WMS-DEAD-QUEUE", true, false, false, null);
//    }

//    /**
//     * 死信队列绑定到交换机
//     * routingKey：order.close
//     * @return
//     */
//    @Bean("WMS-DEAD-BINDING")
//    public Binding deadBinding(){
//
//        return new Binding("WMS-DEAD-QUEUE", Binding.DestinationType.QUEUE, "GMALL-ORDER-EXCHANGE", "stock.dead", null);
//    }


}
