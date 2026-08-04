<div align="center">

<br>

<img src="images/others/jcommerce-logo.png" height="120pt">

<h3> J-Commerce </h3>



<p>  Microservice-based <br>
E-Commerce platform

<br> <br>

![Static Badge](https://img.shields.io/badge/Java-21-red?logo=openjdk)
![Static Badge](https://img.shields.io/badge/Kotlin-2.3-7F52FF?logo=kotlin)
![Static Badge](https://img.shields.io/badge/Spring_Boot-4.0-green?logo=springboot)
![Static Badge](https://img.shields.io/badge/Quarkus-3.5-4695EB?logo=quarkus)
![Static Badge](https://img.shields.io/badge/Caddy-2.11-1F88C0?logo=caddy)
![Static Badge](https://img.shields.io/badge/PostgreSQL-17-0D96F6?logo=postgresql&logoColor=0D96F6)
![Static Badge](https://img.shields.io/badge/MongoDB-7.0-47A248?logo=mongodb)

![Static Badge](https://img.shields.io/badge/Redis-7.4-FF4438?logo=redis)
![Static Badge](https://img.shields.io/badge/RabbitMQ-4-FF6600?logo=rabbitmq)
![Static Badge](https://img.shields.io/badge/Grafana-11.5-F46800?logo=grafana)
![Static Badge](https://img.shields.io/badge/Prometheus-3.1-E6522C?logo=prometheus)

</div>


## Microservices

1. [Identity](/microservice-identity/README.md) - Authentication and user profile management.
2. [Product](/microservice-product/README.md) - Product catalogue, stock level and wishlist.
3. [Order](/microservice-order/README.md) - Orders life cycle and shopping cart.
4. [Notification v2](/microservice-notification-v2/README.md) - User notifications management.
5. [Payment Worker](/worker-payment/README.md) - Payment status management (mock).

---

## Index

1. [Stack](#general-stack)
3. [How to execute](#how-to-execute)
3. [Use Cases](#use-cases)
4. [Architecture](#architecture)
5. [Database](#database)
6. [Messaging](#messaging)
7. [License](#License)

---

## General Stack

- Caddy Server
- Java 21
- Spring Boot
- Quarkus
- Docker
- PostgreSQL + PgVector
- MongoDB
- Redis
- RabbitMQ
- Prometheus
- Grafana

---

## How to execute

### Prerequisites

- Docker
- OpenSSL (for generate RSA key pair)
- Graph Hopper API Key (for geolocation on _Order microservice_)
- OpenAI API Key (for embedding on _Product microservice_)

### Running with Docker Compose

1. **Define environment variables**

   ````bash
   cp .example.env .env
   ```` 
   Update the `.env` file.


2. **Generate RSA Key pair***
    ````bash
    cd microservice-identity/src/main/resources
    ````

    ````bash
    openssl genrsa > app.key
    ````

    ````bash
    openssl rsa -in app.key -pubout -out app.pub
    ````
    *You can also use the default key pair for **testing**. 


3. **Start all microservices and infrastructure with docker**:

    ```bash
    docker compose up -d --build
    ```

The HTTPS will be automatically configured with Caddy, and services will be available at:

| Service               | URL                            | Connection   |
|-----------------------|--------------------------------|--------------|
| Identity              | https://localhost/identity     | RESTful      |
| Product               | https://localhost/product      | GraphQL/HTTP |
| Pricing               | https://localhost/pricing      | RESTful      |
| Order                 | https://localhost/order        | RESTful      |
| Notification          | https://localhost/notification | RESTful      |
| Grafana               | http://localhost:3000          | Web          |
| Prometheus Panel      | http://localhost:9090          | Web          |
| RabbitMQ Panel        | http://localhost:15672         | Web          |
| Identity Database     | localhost:5432                 | PostgreSQL   |
| Vector Database       | localhost:5433                 | PostgreSQL   |
| Pricing Database      | localhost:5434                 | PostgreSQL   |
| Order Database        | localhost:5435                 | PostgreSQL   |
| Notification Database | localhost:5436                 | PostgreSQL   |
| Payment Database      | localhost:5436                 | PostgreSQL   |
| Product Database      | localhost:27017                | MongoDB      |

The interactive documentation for each microservice will be available at:

| Service      | URL                                         |
|--------------|---------------------------------------------|
| Identity     | http://localhost:8080/swagger-ui/index.html |
| Product      | http://localhost:8081/graphiql              |
| Pricing      | http://localhost:8082/swagger-ui/index.html |
| Order        | http://localhost:8083/swagger-ui/index.html |
| Notification | http://localhost:8084/q/swagger-ui          |


> You can also use `docker-compose-mock.yaml` for tests, that contains a mocked catalogue.

---

## VPS-ready application

For VPS environments, you can use the `docker-compose.yaml` file for a VPS-ready application. However, you need to define the _domain_ variable in the `.env` file:

````dotenv
SERVER_DOMAIN=yourdomain.com
````

Services will be available at:

| Service       | URL                      |
|---------------|--------------------------|
| Identity      | https://yourdomain.com/identity    |
| Product       | https://yourdomain.com/product     |
| Order         | https://yourdomain.com/order     |
| Notification | https://yourdomain.com/notification     |
| Grafana       | https://grafana.yourdomain.com   |
| Prometheus Panel   | https://prometheus.yourdomain.com   |
| RabbitMQ Panel     | https://rabbitmq.yourdomain.com  |

---

## Use Cases

### Customer use cases

<img src="images/uml/customer_use_cases.png" width="400pt">

### Management use cases

<img src="images/uml/management_use_cases.png" width="400pt">


## Architecture

The system was modeled with concise C4 diagrams.

### System context

![c4_model_1.png](images/c4/c4_model_1.png)

![c4_model_1_items.png](images/c4/c4_model_1_items.png)

###  Containers

![c4_model_2.png](images/c4/c4_model_2.png)

![c4_model_2_items.png](images/c4/c4_model_2_items.png)

## Authentication

Authentication is implemented as a distributed model with one dedicated authorization service and multiple resource servers. The `microservice-identity` is responsible for the full authentication lifecycle. See the full authentication flow [here](microservice-identity/README.md).

## Messaging

The entire order flow is based on asynchronous communication, using **Spring AMQP** and **Quarkus Messaging RabbitMQ** to integrate the **RabbitMQ** Message Broker.

All queues have a respective Dead Letter Queue named as `<queue-name>.dlq` (they aren't documented here).

### Exchanges

| Name            | Description                             | Routing Keys                                                                    |
|-----------------|-----------------------------------------|---------------------------------------------------------------------------------|
| `order.topic`   | Topic exchange to handle order events   | `order.checked`, `order.created`, `order.canceled`, `order.dispatched`          | 
| `payment.topic` | Topic exchange to handle payment events | `payment.generated`, `payment.confirmed`, `payment.timeout`, `payment.refunded` |
| `dlx.direct`    | Direct exchange for dead letters queues | ...                                                                             |


### Queues

| Name                                      | Description                              |
|-------------------------------------------|------------------------------------------|
| `order.create_order`                      | Create an order from shopping cart       |
| `order.create_shipping`                   | Create shipping for order                |
| `order.handle_payment_timeout`            | Cancel order when payment expires        |
| `order.confirm_payment`                   | Confirm payment of order                 |
| `order.cancel_shipments`                  | Cancel all shipments from order          |
| `payment.generate_payment`                | Generate payment for order               |
| `payment.wait_pending_payment`            | Set TTL of 1 day for payment             |
| `payment.handle_payment_timeout`          | Handle payment timeout after the TTL     |
| `payment.decrease_stock`                  | Decrease stock for outbound products     |
| `payment.refund`                          | Refund all payments from order           |
| `product.refund`                          | Refund all products from order           |
| `notification.notify_payment_generated`   | Notify that a payment has been generated |
| `notification.notify_payment_confirmed`   | Notify that payment has been made        |
| `notification.notify_payment_timeout`     | Notify that payment exceded the TTL      |
| `notification.notify_payment_refunded`    | Notify that payment has been refunded    |
| `notification.notify_shipping_dispatched` | Notify that shipping has been dispatched |
| `notification.notify_canceled_order`      | Notify that order has been canceled      |


![order_exchange.png](images/messaging/order_topic.png)

![payment_exchange.png](images/messaging/payment_topic.png)



### Purchase confirmation

The payment (mock invoice) is generated and notified, through a robust asynchronous messaging flow.

![client_purchase_successful.png](images/messaging/purchase.png)

## License

[» MIT License](LICENSE.md)

---

<div align="center">

_Made with ❤️ and ☕ by **Filipe Martins**._

</div>
