[![codecov](https://codecov.io/gh/jonassavas/spring-task-api/branch/main/graph/badge.svg)](https://codecov.io/gh/jonassavas/spring-task-api)

# Spring Task API

> **Note:** This project is currently in development. The API is functional for core features, but more enhancements and frontend integration will soon be available!

A RESTful backend API for a task management system inspired by Trello. This project provides a scalable way to manage users, task boards, task groups, and tasks, with JWT-based authentication and PostgreSQL persistence. The project is primarily developed for learning and personal use, as I found Trello boards to be the most effective way to track assignments and deadlines during my studies. However, I had to customize the experience heavily using browser extensions to achieve the exact functionality I wanted. This project aims to replicate and improve that experience in a fully customizable, backend-driven solution.

---

## Features

- **User Authentication** – Register and login with JWT-based security.  
- **Task Boards** – Create, update, and manage boards for organizing tasks.  
- **Default Board** – Optionally create Done, Today, Tomorrow, This Week, and Deadlines in one request.
- **Task Groups** – Customize colors, strikethrough styling, and task sort mode.
- **Tasks** – Create, update, move, reorder, and assign due dates.
- **Persistence** – All data is stored in a PostgreSQL database.  
- **Validation & Error Handling** – Request DTO validation, global exception handling, and clear error messages.  
- **RESTful Endpoints** – Fully structured API for easy frontend integration.  

---

## Architecture Overview

The API follows a **layered architecture** for maintainability and scalability:

1. **Controller Layer**  
   - Exposes REST endpoints.  
   - Handles input validation (`@Valid`) and HTTP responses.  

2. **Service Layer**  
   - Contains business logic and transactional operations.  
   - Manages creation, deletion, updates, and queries for tasks, groups, and boards.  

3. **Repository Layer**  
   - Uses Spring Data JPA to persist entities in PostgreSQL.  
   - Provides CRUD operations and custom queries when needed.  

4. **Domain / DTO Layer**  
   - **Entities** – Represent database tables (`UserEntity`, `TaskBoardEntity`, `TaskGroupEntity`, `TaskEntity`).  
   - **DTOs** – Handle request and response objects with validation rules.  
   - **Mappers** – Convert between entities and DTOs.  

**Data Flow Example:**  
1. Client sends a POST request to `/boards/{boardId}/groups` with a `CreateTaskGroupRequestDto`.  
2. Controller validates the request and calls the Service layer.  
3. Service creates a `TaskGroupEntity` and saves it via the Repository.  
4. Response DTO (`TaskGroupDto`) is returned to the client.  

---

## API Endpoints

### Authentication
- `POST /auth/register` – Register a new user  
- `POST /auth/login` – Authenticate and receive a JWT

### Users
- `GET /users/me` - List user information
- `PATCH /users/me` - Update user information (name, password, email)
- `DELETE /users/me` - Delete user/account

### Boards
- `GET /taskboards` – List user taskboards  
- `POST /taskboards` – Create a new taskboard  
- `GET /taskboards/{id}` – Get a board with its groups and tasks
- `PATCH /taskboards/reorder` – Replace the order of all your boards
- `PATCH /taskboards/{id}` – Update a taskboard  
- `DELETE /taskboards/{id}` – Delete a taskboard  

### Task Groups
- `GET /taskboards/{boardId}/groups` – List task groups in a board  
- `POST /taskboards/{boardId}/groups` – Create a new task group  
- `PATCH /taskboards/{boardId}/groups/{groupId}` – Update a task group  
- `DELETE /taskboards/{boardId}/groups/{groupId}` – Delete a task group  
- `PATCH /taskboards/{boardId}/groups/reorder` – Replace the order of all groups on a board

### Tasks
- `POST /groups/{groupId}/tasks` – Create a task in a group  
- `PATCH /tasks/{taskId}` – Update a task  
- `PATCH /tasks/reorder` – Reorder tasks within a group or move them between groups
- `DELETE /tasks/{taskId}` – Delete a task  

---

## Board preset and ordering

Create an empty board with `POST /taskboards` and `{"taskBoardName":"My board"}`. To create the preset, send:

```json
{"taskBoardName":"My board","useDefaultConfiguration":true}
```

Both requests return the existing compact board response. Fetch `GET /taskboards/{id}` to see the groups and tasks. The preset groups appear in this order: Done, Today, Tomorrow, This Week, Deadlines. Only Done has `strikeThroughTasks: true`; only Deadlines has `sortMode: "DUE_DATE_ASC"`. The API supplies the styling flag; the frontend applies the text decoration. All preset groups start empty, and users move tasks between them manually. Colors are unset until chosen.

Groups may use `sortMode: "MANUAL"` or `"DUE_DATE_ASC"` in create and update requests. `POST /groups/{groupId}/tasks` accepts an optional `dueDate` in ISO `YYYY-MM-DD` format. `PATCH /tasks/{taskId}` can change `taskName`, move a task by supplying `taskGroupId`, set `dueDate`, or clear it with `{"dueDate":null}`. A moved task is appended to its destination. Date-sorted groups display the earliest date first, undated tasks last, and ties in position order. Manual reorder requests involving a date-sorted group return 409; task moves still work.

Reorder endpoints accept complete lists. For boards, send `{"boardIds":[3,1,2]}` to `PATCH /taskboards/reorder`; include every board you own exactly once. For groups, send `{"groupIds":[12,10,11]}` to `PATCH /taskboards/{boardId}/groups/reorder`; include every group on that board exactly once. Invalid lists return 400 without changing positions.

For tasks, send the final ordered IDs for both groups to `PATCH /tasks/reorder`:

```json
{"sourceGroupId":10,"destinationGroupId":11,"sourceTaskIds":[102],"destinationTaskIds":[201,101]}
```

Every task currently in either group must appear once across the two lists. The source list may be empty. To reorder within one group, use the same ID for source and destination, put the complete final order in `sourceTaskIds`, and use an empty or identical `destinationTaskIds` list. Invalid lists return 400.

## Running locally

Use Java 21, Maven (or `mvnw`), and PostgreSQL. `docker compose up -d db` starts the development database. The local defaults are in `src/main/resources/application.properties`; replace the development database password and JWT secret before deploying anywhere. Run `./mvnw spring-boot:run` (or `.\mvnw.cmd spring-boot:run` on Windows). The OpenAPI UI is at `/swagger-ui.html`.

`./mvnw spotless:check` checks formatting; `./mvnw clean verify` runs tests and produces the JaCoCo report. CI also starts PostgreSQL and runs the migration and ordering integration tests. Those tests are skipped locally unless `RUN_POSTGRES_TESTS=true` and `TEST_DATABASE_URL`, `TEST_DATABASE_USERNAME`, and `TEST_DATABASE_PASSWORD` point to a disposable PostgreSQL database.

## Upgrading an existing PostgreSQL database

The application now uses Flyway and validates the schema at startup. A fresh database runs V1 and V2 automatically. For an existing database created by Hibernate, stop writes and take a backup first, for example `pg_dump -Fc -f task-api-before-flyway.dump <database>`. Use the Flyway CLI to mark the existing schema as version 1, for example:

```sh
flyway -url=jdbc:postgresql://localhost:5432/postgres -user=postgres -password=<password> -baselineVersion=1 baseline
```

Then start the updated application. V2 adds the new fields, assigns stable positions to existing boards, groups, and tasks, and applies uniqueness constraints. It handles both the old `main` schema and a schema partially updated by the issue-26 branch. Check the migration and application startup logs before allowing writes again. Do not baseline a fresh, empty database; it needs V1 to create its tables.
