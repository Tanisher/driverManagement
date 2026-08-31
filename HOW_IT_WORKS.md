# How the backend works today

This is the **current** Spring Boot API (JWT + MySQL). For what the product is supposed to become, and the unfinished shells still in the tree, see [INTENDED_PRODUCT.md](INTENDED_PRODUCT.md).

## Run

MySQL must be up (`localhost:3306`, database `logistics`). Credentials are in `src/main/resources/application.properties`.

```bash
mvnw.cmd spring-boot:run
```

API: `http://localhost:8080`

## Auth

| Method | Path | Auth |
|--------|------|------|
| `POST` | `/api/auth/signup` | Public |
| `POST` | `/api/auth/login` | Public |
| * | other `/api/**` | `Authorization: Bearer <token>` |

Signup body: `username`, `password` (min 6), `email`, optional `role` (`OFFICE` default, or `ADMIN` / `MECHANIC` / `DRIVER`). Login returns `{ token, role, username }`.

Roles are stored and returned. They are **not** enforced on most routes yet (that is the `UserController1` work).

## Main routes

- `/api/customers` — create / list / get / delete
- `/api/loads` — create / list / get / delete
- `/api/drivers` — create / list / get / delete
- `/api/vehicles` — CRUD, faults, GPS, assign/unassign driver
- `/api/driver-trips` — list / get / delete; `POST /create` from `TripDTO`; driver-scoped list by id or username
- `/api/faults` — CRUD, filter by resolved, `PATCH /{id}/resolve`
- `/api/employees` — HR-style staff (separate from login users)

WebSocket: `/ws` (SockJS). Send `/app/vehicle/location`, subscribe `/topic/vehicleLocation`.
