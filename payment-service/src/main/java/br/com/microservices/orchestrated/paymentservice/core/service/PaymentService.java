package br.com.microservices.orchestrated.paymentservice.core.service;

import br.com.microservices.orchestrated.paymentservice.core.dto.Event;
import br.com.microservices.orchestrated.paymentservice.core.dto.History;
import br.com.microservices.orchestrated.paymentservice.core.dto.OrderProducts;
import br.com.microservices.orchestrated.paymentservice.core.enums.EPaymentStatus;
import br.com.microservices.orchestrated.paymentservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.paymentservice.core.model.Payment;
import br.com.microservices.orchestrated.paymentservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.paymentservice.core.repository.PaymentRepository;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
    private static final Double REDUCE_SUM_VALUE = 0.0;
    private static final Double MIN_AMOUNT_VALUE = 0.1;

    private final JsonUtil jsonUtil;
    private final PaymentRepository paymentRepository;
    private final KafkaProducer producer;


    public void realizePayment(Event event){

        try{
            checkCurrentValidation(event);
            createPendingPayment(event);
            var payment = findByOrderIdAndTransactionId(event);
            validateAmount(payment.getTotalAmount());
            changePaymentSuccess(payment);
            handleSuccess(event);

        } catch (Exception e){
            log.error("Error while realizing payment", e);
        }
        producer.sendEvent(jsonUtil.toJson(event));
    }

    private void checkCurrentValidation(Event event){
        if (paymentRepository.existsByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())){
            throw new ValidationException("Payment already exists");
        }


    }

    private void createPendingPayment(Event event){
        var totalItems = calculateTotalItems(event);
        var totalAmount = calculateTotalAmount(event);
        var payment = Payment
                .builder()
                .orderId(event.getPayload().getId())
                .transactionId(event.getTransactionId())
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
            save(payment);
            setEventAmountItems(event, payment);

    }

    private double calculateTotalAmount(Event event){
        return event
                .getPayload()
                .getProducts()
                .stream()
                .map(product -> product.getQuantity() * product.getProduct().getUnitValue())
                .reduce(REDUCE_SUM_VALUE, Double::sum);
    }

    private int calculateTotalItems(Event event){
        return event
                .getPayload()
                .getProducts()
                .stream()
                .map(OrderProducts::getQuantity)
                .reduce(REDUCE_SUM_VALUE.intValue(), Integer::sum);
    }


    private void setEventAmountItems(Event event, Payment payment){
        event.getPayload().setTotalAmount(payment.getTotalAmount());
        event.getPayload().setTotalItems(payment.getTotalItems());

    }

    private void validateAmount(double totalAmount){
        if (totalAmount < MIN_AMOUNT_VALUE){
            throw new ValidationException("Total amount must be greater than is ".concat(MIN_AMOUNT_VALUE.toString()));
        }

    }

    private void changePaymentSuccess(Payment payment){
        payment.setStatus(EPaymentStatus.SUCCESS);
        save(payment);

    }

    private void handleSuccess(Event event){

    event.setStatus(ESagaStatus.SUCCESS);
    event.setSource(CURRENT_SOURCE);
    addHistory(event, "Payment realized successfully!");

    }


    private void addHistory(Event event, String message){
        var history = History
                .builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
            event.addHistory(history);

    }



    private Payment findByOrderIdAndTransactionId(Event event){
        return paymentRepository
                .findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())
                .orElseThrow(() -> new ValidationException("Payment not found by orderId and transactionId"));

    }


private void save(Payment payment){
        paymentRepository.save(payment);
}


}
