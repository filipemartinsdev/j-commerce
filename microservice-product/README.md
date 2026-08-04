[« Home](../README.md)

# Product Microservice

Microservice for managing Product Catalogue, Stock, Wishlist and Shopping Cart. 

This microservice server a `GraphQL` API, using `MongoDB` for centralized/schemaless catalogue and `PostgreSQL + PgVector` to perform Semantic Search on products. 

## Stack

- Java 21
- Spring Boot 4
- GraphQL
- MongoDB
- PostgreSQL 17 + PgVector

## Features

### Catalogue
- Product catalogue
- Product search by category and Semantic Search

### Products

- Product/SKU management
- Category management
- Product search and filtering

### Stock

- Stock level tracking per SKU
- Stock movement by Type (Inbound or Outbound) and Reason (Entry, Sale, Refund, Damaged...)

### Wishlist

- Per-user wishlist
- Add/remove favorite products

### Shopping Cart

- Cache-based cart
- Add/remove items

## Pricing

The pricing is managed by an external Pricing Engine defined on [Pricing Microservice](../microservice-pricing). 




---



