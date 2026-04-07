<div align="center">

<br>

<img src="images/jcommerce-logo.png" height="120pt">

<h3> J-Commerce </h3>

<p>  Microservice-based <br>
E-Commerce platform

</div>

> 🚧 Comming soon...

## Microservices

1. [Identity](/microservice-identity/README.md) - Authentication and user profile management.
2. [Products](/microservice-products/README.md) - Product catalogue, stock level and wishlist.
3. [Orders](/microservice-orders/README.md) - Orders life cycle and shopping cart.
4. [Payment](/microservice-payment/README.md) - Payment management. 
5. [Notification](/microservice-notification/README.md) - User notifications management.

---

## Index
1. [Stack](#general-stack)
2. [Architecture](#achitecture)
3. [How to execute](#how-to-execute)
4. [License](#License)

---

## General Stack

- Caddy Server
- Java 21
- Spring Framework
- Docker
- PostgreSQL
- Redis
- RabbitMQ
- Prometheus
- Grafana

---

## Architecture

<img src="images/architecture.jpeg" width="1000pt">

### Distributed Authentication

- **Authentication Server**: generate and sign JWT. 
- **Resource server**: Just validate the JWT.

- **Asymmetric Cryptograph with ECC Algorithm**
    - Generate ECC Key pair to Authentication Server
    - Resource server have only the Public Key.


<img src="images/key-pair.png" width="500pt">

#### Validation
This approach invalidates JSON Web Tokens with false signature or modified Payload.


<img src="images/asymmetric-criptography-1.png" width="600pt">
<img src="images/asymmetric-criptography-2.png" width="600pt">
<img src="images/asymmetric-criptography-3.png" width="600pt">


---

## How to execute

> 🚧 In progress...

--- 

## License

[» MIT License](LICENSE.md)

---

_Made with ❤️ and ☕ by **Filipe Martins**._
