package br.com.microservices.orchestrated.inventoryservice.config.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class validationException extends RuntimeException {

    public validationException(String message) {

        super(message);

    }





}
