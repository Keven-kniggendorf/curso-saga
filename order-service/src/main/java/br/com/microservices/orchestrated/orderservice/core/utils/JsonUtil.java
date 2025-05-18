package br.com.microservices.orchestrated.orderservice.core.utils;

import br.com.microservices.orchestrated.orderservice.core.document.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    //aqui estou transformando um objeto em json

    public String toJson(Object object){

        try{
            return objectMapper.writeValueAsString(object);
        } catch (Exception ex) {
            return "";
        }

    }

    //Aqui estou tratando a exceção de forma genérica, mas o ideal seria criar uma exceção específica para cada tipo de erro
    //Estou fazendo o tratamento de versão pegando um json e transformando em um objeto

    public Event toEvent(String json){
        try{
            return objectMapper.readValue(json, Event.class);
        } catch (Exception ex) {
            return null;
        }
    }




}
