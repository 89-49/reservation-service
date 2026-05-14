# Reservation Service Test Guide

## Local Docker Test

Create a local env file from the example.

```bash
cp .env.local.example .env.local
```

Fill `GPR_USER`, `GPR_TOKEN`, and `JWT_SECRET`.

Run the service locally.

```bash
docker compose --env-file .env.local -f docker-compose.local.yml up -d --build
```

Check logs.

```bash
docker logs -f reservation-service
```

Direct local request:

```http
GET http://localhost:8085/api/v1/reservations
Authorization: Bearer <token>
X-User-Id: <user-uuid>
X-User-Roles: BUYER
```

The gateway normally adds `X-User-Id` and `X-User-Roles`. If you call the reservation service directly in Apidog, add those headers manually.

## GCP Test Through Gateway

Use the gateway address, not the reservation service address.

```http
GET http://<gateway-ip>:8090/api/v1/reservations
Authorization: Bearer <token>
```

The gateway should validate the token, add user headers, discover `RESERVATION-SERVICE` from Eureka, and route the request.

## GCP Direct Service Test

Use this only to isolate reservation-service.

```http
GET http://<reservation-service-ip>:8085/api/v1/reservations
Authorization: Bearer <token>
X-User-Id: <user-uuid>
X-User-Roles: BUYER
```

If this works but the gateway request fails, check gateway routing or Eureka registration.

## Deployment Checks

On the reservation VM, `.env` must include:

```env
SERVER_IP=<reservation-service-vm-external-ip>
SERVER_PORT=8085
```

Check registered Eureka address after deployment. It should be the reservation VM external IP, not a Docker bridge IP such as `172.18.x.x`.
