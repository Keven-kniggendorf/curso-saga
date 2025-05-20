package br.com.microservices.orchestrated.orderservice.core.service;

import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.dto.EventFilter;
import br.com.microservices.orchestrated.orderservice.core.repository.EventRepository;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository repository;

    public void notifyEnding(Event event) {
        event.setOrderId(event.getOrderId());
        event.setCreatedAt(LocalDateTime.now());
        save(event);
        log.info("Order {} with saga notified! TransactionId: {}", event.getOrderId(), event.getTransactionId());
    }

    public List<Event> findAll() {

        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Event findByFilters(EventFilter filter){
        validateEmptyFilters(filter);
        if (!org.springframework.util.ObjectUtils.isEmpty(filter.getOrderId())) {
            return findByOrderId(filter.getOrderId()) ;
        }else{
            return findByTransactionId(filter.getTransactionId());
        }
    }

    //metodo de busca por OrderId
    private Event findByOrderId(String orderId) {
        return repository
                .findTop1ByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ValidationException("Event not found by orderId."));
    }

    //Metodo de busca por TransactionId
    private Event findByTransactionId(String TransactionId) {
        return repository
                .findTop1ByTransactionIdOrderByCreatedAtDesc(TransactionId)
                .orElseThrow(() -> new ValidationException("Event not found by transactionId."));
    }



    private void validateEmptyFilters(EventFilter filter) {
        if (org.springframework.util.ObjectUtils.isEmpty(filter.getOrderId()) && org.springframework.util.ObjectUtils.isEmpty(filter.getTransactionId())) {
            throw new ValidationException("OrderId or TransactionId must be informed.");

        }
    }




    public Event save(Event event) {
        return repository.save(event);

    }


}
