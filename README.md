# Employee Management REST API

A Spring Boot REST API for managing employee records with full CRUD operations, validation, and department statistics.

## Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Data JPA
- H2 In-Memory Database
- Bean Validation (Jakarta)
- Maven
- Docker + Docker Compose

## Project Structure

```
src/main/java/
  controller/     # REST controllers
  model/          # JPA entity (Employee)
  repository/     # Spring Data JPA repository
  service/        # Business logic
  exception/      # Global exception handling (404, 409, 400)
  dto/            # Request/response DTOs

src/test/java/    # Integration tests (MockMvc)
```

## API Endpoints

| Method | Endpoint                             | Description          |
|--------|--------------------------------------|----------------------|
| GET    | `/api/employees`                     | Get all employees    |
| GET    | `/api/employees/{id}`                | Get by ID            |
| GET    | `/api/employees/department/{dept}`   | Get by department    |
| GET    | `/api/employees/stats/departments`   | Department stats     |
| POST   | `/api/employees`                     | Create employee      |
| PUT    | `/api/employees/{id}`                | Update employee      |
| PATCH  | `/api/employees/{id}/status?status=X`| Update status        |
| DELETE | `/api/employees/{id}`                | Delete employee      |

### Employee Fields

| Field       | Type   | Notes                                  |
|-------------|--------|----------------------------------------|
| firstName   | String | Required                               |
| lastName    | String | Required                               |
| email       | String | Required, unique                       |
| department  | String | Required                               |
| salary      | Number | Required                               |
| hireDate    | Date   | Required                               |
| status      | String | ACTIVE, ON_LEAVE, or TERMINATED        |

## How to Run

### Maven

```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`.

### Docker

```bash
docker-compose up --build
```

## How to Run Tests

```bash
./mvnw test
```

The project includes 10 integration tests using MockMvc covering all endpoints and error scenarios.

## Sample Requests

### Create an Employee

```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane.doe@example.com",
    "department": "Engineering",
    "salary": 95000,
    "hireDate": "2024-01-15",
    "status": "ACTIVE"
  }'
```

### Get All Employees

```bash
curl http://localhost:8080/api/employees
```

### Get Employee by ID

```bash
curl http://localhost:8080/api/employees/1
```

### Get Department Statistics

```bash
curl http://localhost:8080/api/employees/stats/departments
```
