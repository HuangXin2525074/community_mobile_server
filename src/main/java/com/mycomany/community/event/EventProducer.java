package com.mycomany.community.event;

import com.alibaba.fastjson.JSONObject;
import com.mycomany.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    // handling event
    public void fireEvent(Event event){
        // send event to particular entity
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
