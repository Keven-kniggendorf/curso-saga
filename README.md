# Projeto E-commerce com Padrão Saga (Orquestração)

Este projeto implementa uma arquitetura de microsserviços baseada no padrão **Saga Orquestrado**. O sistema simula o fluxo completo de um pedido em um e-commerce, onde múltiplos serviços se comunicam de forma assíncrona por meio do **Apache Kafka** para garantir consistência em um ambiente distribuído.

## Arquitetura e Microsserviços

A aplicação é composta pelos seguintes microsserviços, cada um com seu próprio banco de dados para garantir o desacoplamento:

-   **`order-service`**: Ponto de entrada da aplicação. Responsável por criar os pedidos e iniciar o fluxo da saga.
    -   **Banco de Dados**: MongoDB

-   **`orchestrator-service`**: Atua como o coordenador central da saga. Ele escuta os eventos de todos os outros serviços e comanda a próxima etapa do fluxo ou inicia as transações de compensação (rollback) em caso de falha.

-   **`product-validation-service`**: Valida se os produtos do pedido existem e se são válidos.
    -   **Banco de Dados**: PostgreSQL

-   **`payment-service`**: Gerencia a execução e o status dos pagamentos.
    -   **Banco de Dados**: PostgreSQL

-   **`inventory-service`**: Verifica e atualiza a disponibilidade de estoque, realizando a baixa dos produtos.
    -   **Banco de Dados**: PostgreSQL

### Comunicação via Kafka

A comunicação entre os serviços é orientada a eventos. O orquestrador publica comandos em tópicos específicos do Kafka, e cada serviço executa sua lógica de negócio ao consumir uma mensagem. Ao finalizar, o serviço publica um evento de resposta (sucesso ou falha), que é consumido de volta pelo orquestrador para dar continuidade ao fluxo.

---

## Tecnologias Utilizadas

-   **Backend**: Java 17, Spring Boot
-   **Mensageria**: Apache Kafka
-   **Bancos de Dados**: PostgreSQL, MongoDB
-   **Build**: Gradle
-   **Containerização**: Docker, Docker Compose
-   **Scripting**: Python

---

## Estrutura do Projeto

```
curso-saga/
│
├── order-service/
├── payment-service/
├── inventory-service/
├── orchestrator-service/
├── product-validation-service/
├── docker-compose.yml
└── build.py
```

---

## Como Executar Localmente

**Pré-requisitos:** Docker, Docker Compose e Python 3 instalados.

1.  Clone o repositório:
    ```bash
    git clone [https://github.com/Keven-kniggendorf/curso-saga.git](https://github.com/Keven-kniggendorf/curso-saga.git)
    cd curso-saga
    ```

2.  Execute a aplicação. Você tem duas opções:

    **Opção 1 (Recomendada): Usando o script de automação**

    Este método utiliza o script `build.py` para compilar todas as aplicações Java em paralelo antes de iniciar os containers. É a forma mais garantida de rodar o projeto do zero, evitando problemas de cache de build do Docker.

    ```bash
    python build.py
    ```

    **Opção 2: Usando apenas o Docker Compose**

    Este comando irá iniciar todos os serviços de infraestrutura e os microsserviços do projeto. Ele depende das configurações de *multi-stage builds* nos Dockerfiles para compilar as aplicações.

    ```bash
    docker-compose up --build
    ```

---

## Testando a Aplicação

Após iniciar os containers, o `order-service` estará disponível na porta `8080`. Você pode utilizar ferramentas como **Postman**, **Insomnia** ou qualquer cliente HTTP para enviar uma requisição `POST` para `http://localhost:8080/api/order` com o seguinte corpo:

**Exemplo de Requisição (JSON)**
```json
{
  "products": [
    {
      "product": {
        "code": "COD-1"
      },
      "quantity": 1
    },
    {
      "product": {
        "code": "COD-2"
      },
      "quantity": 2
    }
  ]
}
```
O fluxo será automaticamente processado pelos demais serviços, e você pode acompanhar os logs de cada container e os eventos sendo persistidos no banco de dados de cada serviço.

---
