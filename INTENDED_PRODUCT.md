# What this application is supposed to be

This repo is a **work-in-progress logistics / fleet backend** for a haulage company. It is meant to sit behind a **mobile app (Flutter)** and an **office/admin UI**, talking to **MySQL** (`localhost:3306`, database `logistics`).

It is not a finished product. A lot of what is in the code is the next layer half-built: role portals commented out, request types not wired in yet, vehicle–driver assignment still being iterated. Those pieces look unused; they are the production roadmap, not dead experiments.

---

## The product

A small transport company needs one system for:

| Who | What they do in the product |
|-----|-----------------------------|
| **Office staff** | Customers, loads (jobs), dispatch, reports |
| **Admin** | Users, roles, company-wide dashboard |
| **Drivers** | Own trips, log mileage/fuel/trailers, send live GPS from a phone |
| **Mechanics** | Vehicle faults / maintenance queue, mark jobs resolved |

The live loop the data model already supports:

1. Office captures a **customer** and a **load** (pickup → delivery, weight, status).
2. A **driver** is registered (license, next of kin, phone) and **assigned to a vehicle**.
3. A **trip** is logged against that driver, load, customer, plate, start/end mileage, fuel, trailers.
4. The truck’s **location** is updated (REST today; WebSocket is there for a live map).
5. If something breaks, a **fault** is filed on that vehicle/driver; a mechanic resolves it with notes.

That is a dispatch + tracking + workshop system. CRUD for those entities is largely in place. The **role-gated apps** on top of it are the unfinished part.

---

## Clients this backend was written for

There is no frontend in this repo. The backend is clearly waiting for one:

- CORS allows `http://localhost:3000` with a comment **“Flutter dev server”**, plus a LAN IP (phone on the same Wi‑Fi).
- Auth is JWT in the `Authorization` header (typical for a mobile client).
- SockJS `/ws` is for devices that cannot hold a raw WebSocket.
- `GET /api/driver-trips/driver/username/{username}` is a “**my trips**” screen for a logged-in driver.
- `GET /api/vehicles/location/by-driver/{driverId}` is an office map: **where is this driver?**

So the intended split is: **Flutter (or similar) for drivers**, browser/office UI on port 3000, this API on 8080, **MySQL** as the store.

---

## Where development actually went

Commit history is a straight build-up of that product, not a rewrite:

| Stage | What landed |
|-------|-------------|
| Foundation | Spring Security, signup/login, role enum (`OFFICE`, `ADMIN`, `MECHANIC`, `DRIVER`) |
| People | Driver registration (`User` joined inheritance → `Driver` / `Admin` / `Mechanic` / `OfficeStaff`) |
| Commercial | Customer creation, then loads |
| Operations | Driver trips (list + create), then fault tracking |
| Fleet | Vehicle updates, adding drivers, **assigning a driver to a vehicle** (last several commits: “driver assigning”, “driver registration to vehicle step 2”, “driver confusion”) |

The work **paused on vehicle ↔ driver assignment**. That matches leftover types still in the tree (`DriverAssignmentRequest` with a `driverId` field) while the live endpoint still takes a raw `Map`. The next prompt was likely to finish that wiring and then turn on the role shells.

---

## Shells that are still in-progress (not leftovers)

These are the “empty rooms” of the next production slice. They should be treated as **planned work**, not deleted as unused.

### 1. Role portals — `UserController1` (commented out)

This file is the blueprint for **four apps behind one API**, using `@PreAuthorize`:

| Path | Role | Intended screens |
|------|------|------------------|
| `GET /api/profile` | any logged-in user | Current user profile (`userService.findByUsername`) |
| `/api/admin/**` | `ADMIN` | Dashboard, create user, list all users |
| `/api/driver/**` | `DRIVER` | My trips, log a trip |
| `/api/mechanic/**` | `MECHANIC` | Vehicle maintenance list, log maintenance |
| `/api/office/**` | `OFFICE` | Reports, create report |

It is fully commented, so it does not run yet. It already assumes UserService methods that are **not implemented** (`findByUsername`, `findAllUsers`). Turning this on is the natural next backend step after assignment is stable.

`CustomUserDetailsService` already puts the role on the token (`roles(user.getRole().name())`), so `@PreAuthorize("hasAuthority('ADMIN')")` is waiting for `enableMethodSecurity` and this controller.

### 2. Typed driver assignment — `DriverAssignmentRequest`

```java
public class DriverAssignmentRequest {
    private Long driverId;
}
```

Vehicle assign currently uses `Map<String, Long>`. This class is the next, cleaner API body. Commits named “driver assigning” / “driver confusion” sit right here.

### 3. Shared login identity — `BaseUser`

```java
public interface BaseUser {
    Long getId();
    void setId(Long id);
    String getUsername();
    void setUsername(String username);
}
```

All role types share username/id. This was probably meant so profile, trips, and assignment do not care whether the row is a `Driver` or `OfficeStaff`. Not wired onto `User` yet.

### 4. Role-specific user rows (entities exist; APIs do not)

Signup already **persists the right subclass** from `role`:

- `Driver` — name, license, next of kin, phone, optional vehicle
- `Admin` — department, super-admin flag
- `Mechanic` — specialization, certification level
- `OfficeStaff` — office location, position

There are **no** dedicated REST fields yet for mechanic certification, admin department, etc. The tables/classes are ready for the portals in `UserController1`.

### 5. Two kinds of “staff”

- `OfficeStaff` extends `User` → can **log in**
- `Employee` is a separate HR-style record (name, position, ID number, start date) → **cannot log in**

That is likely intentional: payroll/HR list vs people who use the system. Unfinished, not a mistake to collapse blindly.

### 6. Live tracking (half connected)

- REST `PUT /api/vehicles/{id}/location` **saves** lat/long in MySQL
- WebSocket `/app/vehicle/location` → `/topic/vehicleLocation` **broadcasts** but does not save

The intended driver-app flow is: phone sends GPS over STOMP, office map subscribes, **and** the vehicle row in MySQL stays current. Only the REST half writes to the database today.

### 7. Load status as a workflow

`Load.status` is a free string with comments like Pending / In Transit / Delivered. Office reports in `UserController1` would need this to become a real lifecycle, not just a label.

---

## What already works (the floor for the next work)

You can already, with JWT, against **MySQL**:

- Sign up / log in (role on the token)
- CRUD customers, loads, drivers, vehicles, employees, faults, trips
- Assign / unassign driver ↔ vehicle
- Driver-only trip list (token must match that driver)
- Fault create / resolve
- Vehicle GPS via REST

What is **not** done yet, and is the production direction:

- Enforce roles on URLs (`/api/admin`, `/api/driver`, `/api/mechanic`, `/api/office`)
- Finish driver–vehicle assignment with `DriverAssignmentRequest`
- Persist WebSocket GPS
- Profile + admin user list (`findByUsername` / `findAllUsers`)
- Mechanic maintenance API (faults are the data; the mechanic portal is the shell)
- Office reports
- The Flutter / office UI in this repo

---

## Database

Production (and this WIP) is **MySQL**, as in `application.properties`:

- URL: `jdbc:mysql://localhost:3306/logistics`
- Hibernate `ddl-auto=update`

H2 is **tests only**. It is not how this app is meant to run.

---

## Suggested next slice (matches the shells)

1. Keep MySQL; finish **assign driver to vehicle** using `DriverAssignmentRequest`.
2. Implement `UserService.findByUsername` / `findAllUsers`.
3. Enable method security and bring **`UserController1` live** (split into real controllers if nested classes stay messy).
4. Driver app: log trip + GPS; mechanic app: faults; office: customers/loads/reports; admin: users.
5. Point WebSocket location updates at `VehicleService.updateVehicleLocation` so the map and the database stay in sync.
