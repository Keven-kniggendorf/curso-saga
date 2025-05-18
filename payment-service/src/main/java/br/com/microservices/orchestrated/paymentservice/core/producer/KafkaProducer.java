package br.com.microservices.orchestrated.paymentservice.core.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.orchestrator}")
    private String orchestratorTopic;

    public void sendEvent(String payLoad){
        try{
            log.info("Sending ecent to topic {} with data {} ",orchestratorTopic, payLoad);
            kafkaTemplate.send(orchestratorTopic, payLoad);
        } catch (Exception ex){
            log.error("Error trying to end data to topic {} with data {} ",orchestratorTopic, payLoad, ex);
        }
    }


}
