# Ticket Management System

A comprehensive Spring Boot REST API for managing tickets with full CRUD operations, automatic date handling, and robust error handling.

## Features

- ✅ **Full CRUD Operations**: Create, Read, Update, Delete tickets
- ✅ **Automatic Date Setting**: Date is automatically set when creating tickets
- ✅ **Input Validation**: Comprehensive validation with detailed error messages
- ✅ **Error Handling**: Proper HTTP status codes and error responses
- ✅ **MySQL Database**: Persistent data storage
- ✅ **RESTful Design**: Standard REST API patterns

## Prerequisites

- Java 17
- Maven 3.6+
- MySQL 8.0+

## Setup

1. **Database Setup**
   ```sql
   CREATE DATABASE ticket;
   ```

2. **Environment Variables**
   Set the following environment variables:
   ```bash
   DB_USERNAME=your_username
   DB_PASSWORD=your_password
   ```

3. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## API Endpoints

### Base URL: `http://localhost:8080/api/tickets`

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/` | Create a new ticket | Ticket object | 201 Created |
| GET | `/` | Get all tickets | - | 200 OK |
| GET | `/{id}` | Get ticket by ID | - | 200 OK / 404 Not Found |
| PUT | `/{id}` | Update ticket | Ticket object | 200 OK / 404 Not Found |
| DELETE | `/{id}` | Delete ticket | - | 204 No Content / 404 Not Found |

### Ticket Object Structure

```json
{
  "titleTicket": "string (required)",
  "assign": "string (required)",
  "status": "string (required)",
  "priority": "string (required)",
  "dateCurrent": "date (optional, auto-set to current date if not provided, format: YYYY-MM-DD)",
  "subTask": "string (optional)"
}
```

### Example Requests

**Create Ticket (with automatic date):**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "titleTicket": "Fix login bug",
    "assign": "John Doe",
    "status": "In Progress",
    "priority": "High",
    "subTask": "Investigate authentication flow"
  }'
```

**Create Ticket (with custom date):**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "titleTicket": "Fix login bug",
    "assign": "John Doe",
    "status": "In Progress",
    "priority": "High",
    "dateCurrent": "2024-01-15",
    "subTask": "Investigate authentication flow"
  }'
```

**Get All Tickets:**
```bash
curl -X GET http://localhost:8080/api/tickets
```

**Get Ticket by ID:**
```bash
curl -X GET http://localhost:8080/api/tickets/1
```

**Update Ticket:**
```bash
curl -X PUT http://localhost:8080/api/tickets/1 \
  -H "Content-Type: application/json" \
  -d '{
    "titleTicket": "Fix login bug",
    "assign": "John Doe",
    "status": "Completed",
    "priority": "High",
    "dateCurrent": "2024-01-15",
    "subTask": "Authentication flow fixed"
  }'
```

**Delete Ticket:**
```bash
curl -X DELETE http://localhost:8080/api/tickets/1
```

## Error Responses

The API returns standardized error responses:

**Validation Error (400 Bad Request):**
```json
{
  "titleTicket": "Title is required",
  "assign": "Assignee is required"
}
```

**Not Found Error (404 Not Found):**
```json
{
  "error": "Ticket not found with id: 999"
}
```

**Internal Server Error (500 Internal Server Error):**
```json
{
  "error": "An unexpected error occurred"
}
```

## Technologies Used

- Spring Boot 3.5.3
- Spring Data JPA
- MySQL Connector
- Lombok
- Spring Validation
- Maven

## Project Structure

```
src/main/java/com/ticket/
├── TicketApplication.java          # Main application class
├── controller/
│   └── TicketController.java      # REST controller
├── entity/
│   └── Ticket.java               # JPA entity
├── repository/
│   └── TicketRepository.java     # Data access layer
├── service/
│   └── TicketService.java        # Business logic
└── exception/
    ├── GlobalExceptionHandler.java    # Global error handling
    └── TicketNotFoundException.java   # Custom exception
```

## Database Schema

The application automatically creates the following table:

```sql
CREATE TABLE tickets (
    sno BIGINT AUTO_INCREMENT PRIMARY KEY,
    title_ticket VARCHAR(255) NOT NULL,
    assign VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    priority VARCHAR(255) NOT NULL,
    date_current DATE NOT NULL,
    sub_task TEXT
);
``` 