package br.com.microservices.orchestrated.orchestratorservice.core.producer;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class SagaOrchestratorProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendEvent(String payLoad, String topic){
        try{
            log.info("Sending ecent to topic {} with data {} ",topic, payLoad);
            kafkaTemplate.send(topic, payLoad);
        } catch (Exception ex){
            log.error("Error trying to end data to topic {} with data {} ",topic, payLoad, ex);
        }
    }


}
