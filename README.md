# Driver Management (Logistics)

Spring Boot REST API for logistics operations: drivers, vehicles, trips, loads, customers, faults, and staff. The backend uses JWT authentication and can push live vehicle locations over WebSocket.

Maven artifact: `com.logistics:logistics` (`0.0.1-SNAPSHOT`). Application class: `com.logistics.LogisticsApplication`.

## Tech stack

- Java 17
- Spring Boot 3.4.0
- Spring Web, Spring Data JPA, Spring Security
- MySQL (`mysql-connector-j`)
- Lombok
- JWT (`JwtUtil`) for stateless auth
- STOMP over SockJS for vehicle location updates

## Prerequisites

- JDK 17+
- Maven 3.9+ (or use the included `mvnw` / `mvnw.cmd`)
- MySQL listening on the host and port in `src/main/resources/application.properties` (currently `localhost:3307`)
- A database named `logistics`

Schema is managed by Hibernate (`spring.jpa.hibernate.ddl-auto=create-drop`), so tables are created on startup and **dropped on shutdown**. Change this before using the app with real data.

## Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.application.name=logistics
spring.datasource.url=jdbc:mysql://localhost:3307/logistics
spring.datasource.username=root
spring.datasource.password=<your-password>
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

CORS is allowed from `http://localhost:3000` and `http://192.168.32.11` (see `SecurityConfig`).

## Run

From the project root:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The API listens on port **8080** by default.

Tests:

```bash
./mvnw test
```

## Authentication

Security is stateless JWT (`SecurityConfig`). Passwords are hashed with BCrypt.

| Method | Path | Auth |
|--------|------|------|
| `POST` | `/api/auth/signup` | Public |
| `POST` | `/api/auth/login` | Public |
| * | all other `/api/**` | Bearer JWT |

**Signup** (`UserController`) registers a `User`. Required fields: `username`, `password` (min 6 chars), `email`. Optional `role` defaults to `OFFICE`. Role decides which subclass is saved:

| Role | Entity |
|------|--------|
| `DRIVER` | `Driver` |
| `ADMIN` | `Admin` |
| `MECHANIC` | `Mechanic` |
| `OFFICE` | `OfficeStaff` |

Example signup:

```http
POST /api/auth/signup
Content-Type: application/json

{
  "username": "jdoe",
  "password": "secret1",
  "email": "jdoe@example.com",
  "role": "DRIVER"
}
```

**Login** returns a JWT (24h), role, and username:

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "jdoe",
  "password": "secret1"
}
```

Send the token on later requests:

```http
Authorization: Bearer <token>
```

## API

### Drivers — `/api/drivers`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/drivers` | Create driver (`DriverDTO`) |
| `GET` | `/api/drivers` | List drivers |
| `GET` | `/api/drivers/{id}` | Get driver |
| `DELETE` | `/api/drivers/{id}` | Delete driver |

### Vehicles — `/api/vehicles`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/vehicles` | List vehicles |
| `GET` | `/api/vehicles/{id}` | Get vehicle |
| `POST` | `/api/vehicles` | Create vehicle |
| `PUT` | `/api/vehicles/{id}` | Update vehicle |
| `DELETE` | `/api/vehicles/{id}` | Delete vehicle |
| `GET` | `/api/vehicles/{id}/faults` | Faults for a vehicle |
| `PUT` | `/api/vehicles/{vehicleId}/location?latitude=&longitude=` | Update GPS |
| `GET` | `/api/vehicles/{vehicleId}/location` | Get GPS |
| `GET` | `/api/vehicles/location/by-driver/{driverId}` | GPS by assigned driver |
| `PUT` | `/api/vehicles/{vehicleId}/assign-driver` | Body: `{"driverId": 1}` |
| `PUT` | `/api/vehicles/{vehicleId}/unassign-driver` | Clear assigned driver |

### Loads — `/api/loads`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/loads` | Create load |
| `GET` | `/api/loads` | List loads |
| `GET` | `/api/loads/{id}` | Get load |
| `DELETE` | `/api/loads/{id}` | Delete load |

Loads belong to a customer (`customerId`) and track description, weight, pickup/delivery locations, and status (e.g. Pending, In Transit, Delivered). Stored in table `logistics_load`.

### Customers — `/api/customers`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/customers` | Create customer |
| `GET` | `/api/customers` | List customers |
| `GET` | `/api/customers/{id}` | Get customer |
| `DELETE` | `/api/customers/{id}` | Delete customer |

### Faults — `/api/faults`

Vehicle faults reported by a driver.

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/faults` | Create fault |
| `PUT` | `/api/faults/{id}` | Update fault |
| `PATCH` | `/api/faults/{id}/resolve` | Mark resolved (body: resolution notes) |
| `GET` | `/api/faults` | List faults |
| `GET` | `/api/faults/{id}` | Get fault |
| `GET` | `/api/faults/status/{resolved}` | Filter by resolved (`true`/`false`) |
| `DELETE` | `/api/faults/{id}` | Delete fault |

### Employees — `/api/employees`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/employees` | Create employee |
| `GET` | `/api/employees` | List employees |
| `GET` | `/api/employees/{id}` | Get employee |
| `DELETE` | `/api/employees/{id}` | Delete employee |

### Driver trips — `/api/driver-trips`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/driver-trips` | Create trip (full `DriverTrip` body) |
| `POST` | `/api/driver-trips/create` | Create trip from `TripDTO` (driver/load/customer IDs) |
| `GET` | `/api/driver-trips` | List trips |
| `GET` | `/api/driver-trips/{id}` | Get trip |
| `DELETE` | `/api/driver-trips/{id}` | Delete trip |
| `GET` | `/api/driver-trips/driver/{driverId}` | Trips for a driver (caller must be that driver) |
| `GET` | `/api/driver-trips/driver/username/{username}` | Same, looked up by username |

`TripDTO` fields: `dateTime`, `destination`, `startingMillage`, `endingMillage`, `fuelLitres`, `trailer1`, `trailer2`, `driverId`, `loadId`, `plateNumber`, `customerId`.

## Domain model

`User` is a joined inheritance root (`UserRole`: `OFFICE`, `ADMIN`, `MECHANIC`, `DRIVER`).

- **Driver** — name, license, contacts, optional vehicle, faults
- **Admin** — name, department, super-admin flag
- **Mechanic** — specialization, certification
- **OfficeStaff** — office location, position
- **Vehicle** — plate, make/model/year/color, service date, GPS, assigned driver, faults
- **Load** — cargo and route, linked to a customer
- **Customer** — contact details and loads
- **DriverTrip** — mileage, fuel, trailers, driver, load, customer, plate
- **Fault** — description, reported time, resolution, driver + vehicle
- **Employee** — HR-style staff record (separate from `User`)

DTOs/mappers live under `com.logistics.DTO` (`DriverDTO`, `VehicleDTO`, `FaultDTO`, `UserDTO`, and corresponding mappers).

## WebSocket

STOMP endpoint: `/ws` (SockJS). Connect with a JWT in the `Authorization` header.

| Direction | Destination | Payload |
|-----------|-------------|---------|
| Client → server | `/app/vehicle/location` | `VehicleLocationMessage` |
| Server → clients | `/topic/vehicleLocation` | same message, broadcast |

## Project layout

Canonical source is under `src/main/java/com/logistics/`:

```
controllers/   REST endpoints
entity/        JPA entities
repository/    Spring Data repositories
service/       interfaces
service/Impl/  service implementations
DTO/           request/response DTOs and mappers
payload/       login/JWT/WebSocket types
config/        SecurityConfig, WebSocketSecurityConfig
util/          JwtUtil, JwtTokenFilter
```

Resources: `src/main/resources/application.properties`  
Tests: `src/test/java/com/logistics/LogisticsApplicationTests.java`
