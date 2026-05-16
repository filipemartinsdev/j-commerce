[« Home](../README.md)

# Order Microservice

Microservice to handle order flow and shipping logistics operations.

## API Endpoints

### Application

| Method | Endpoint | Description | Query Parameters |
|--------|----------|-------------|----------------|
| GET | /actuator/health | Get application health | - |
| GET | /swagger-ui/index.html | Swagger UI | - |

### Sales Orders

| Method | Endpoint | Description | Query Parameters |
|--------|----------|-------------|----------------|
| GET | /api/v1/sales-orders | List user's orders | `page`, `size`, `sort` |
| GET | /api/v1/sales-orders/{orderId} | Get order by ID | - |
| DELETE | /api/v1/sales-orders/{orderId} | Request order cancellation | - |

### Delivery Addresses

| Method | Endpoint | Description | Query Parameters |
|--------|----------|-------------|----------------|
| GET | /api/v1/delivery-addresses | List user's addresses | `page`, `size`, `sort` |
| GET | /api/v1/delivery-addresses/{id} | Get address by ID | - |
| POST | /api/v1/delivery-addresses | Create address | `byCoordinates`|
| PATCH | /api/v1/delivery-addresses/{id} | Update address | - |
| DELETE | /api/v1/delivery-addresses/{id} | Delete address | - |

### Admin - Storage Addresses

| Method | Endpoint | Description | Query Parameters       |
|--------|----------|-------------|------------------------|
| GET | /admin/api/v1/storage-addresses | List storage addresses | `page`, `size`, `sort` |
| GET | /admin/api/v1/storage-addresses/{id} | Get storage address by ID | -                      |
| POST | /admin/api/v1/storage-addresses | Create storage address | `byCoordinates`        |
| DELETE | /admin/api/v1/storage-addresses/{id} | Delete storage address | -                      |

### Admin - Shippings

| Method | Endpoint | Description | Query Parameters |
|--------|----------|-------------|----------------|
| GET | /admin/api/v1/shippings | List all shippings | `salesOrderId`, `page`, `size`, `sort` |
| POST | /admin/api/v1/shippings/{id} | Dispatch shipping | - |
| POST | /admin/api/v1/shippings/{id}/check-in | Start shipping (check-in) | - |
| POST | /admin/api/v1/shippings/{id}/check-out | Finish shipping (check-out) | - |
| DELETE | /admin/api/v1/shippings/{id} | Cancel shipping | - |

## Order Status

![sales_order_status.png](../images/sales_order_status.png)

![shipping_status.png](../images/shipping_status.png)

## Shopping cart confirmation

![order_activity_diagram.png](../images/order_activity_diagram.png)

## Database

- PostgreSQL 17
- Flyway for migrations

![order_db.png](../images/order_db.png)