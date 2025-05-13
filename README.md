# 📚 Library Management System

A full-stack Library Management System built with **Spring Boot** and **PostgreSQL**, providing a secure and scalable RESTful API for managing books, users, and borrow operations.

---

## 🚀 Project Overview

This application allows librarians and patrons to interact with a library system via a REST API. It supports user registration, authentication via JWT, role-based access control, book management, and borrow/return functionality.

Key features include:
- Role-based access for **Librarian** and **Patron**
- Secure authentication with **JWT**
- **Book** and **User** CRUD operations
- Borrowing and returning books
- **Swagger** documentation for API exploration
- Integration and unit testing using **H2**
- **Logging** of key application events
- **Postman Collection** included for testing API endpoints

---

## 🧰 Technology Stack

- **Java 21**
- **Spring Boot 3**
- **Spring Security (JWT)**
- **Spring Data JPA**
- **PostgreSQL** (Main database)
- **H2** (For testing)
- **Lombok**
- **MapStruct**
- **Swagger/OpenAPI (springdoc-openapi)**
- **Maven**
- **JUnit & Mockito & Spring Boot Test**
- **SLF4J **


---

## 🏁 Getting Started

### ✅ Prerequisites
- Java 21+
- Maven
- PostgreSQL
- Git

### 🔧 Running Locally

1. **Clone the repository**
   ```bash
   git clone https://github.com/zeynepucuncuoglu/libraryManagementSystem.git
   cd library-management-system




## ⚙️ application.properties Configuration

The `application.properties` file contains configuration settings for the application. Below is an example of the `application.properties` configuration used in this project.

### 📂 Database Configuration

```properties
# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/library_db
spring.datasource.username=your_database_username
spring.datasource.password=your_database_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# Hibernate DDL mode
spring.jpa.hibernate.ddl-auto=update

# Show SQL queries in logs
spring.jpa.show-sql=true

# SQL logging format
spring.jpa.properties.hibernate.format_sql=true

# Database connection pool
spring.datasource.hikari.maximum-pool-size=10

```
3. **Run the Application**

   #### ▶️ Option A: Run with Maven

If you're using the Maven wrapper (recommended):


```bash
./mvnw spring-boot:run
```
 #### ▶️ Option B: if you have Maven installed globally

```bash
mvn spring-boot:run
```

## 📘 API Documentation

### 🔍 Swagger UI

This project includes interactive API documentation powered by **Swagger (SpringDoc OpenAPI)**.

- **URL:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

After running the application locally, open the link in your browser to explore and test all available endpoints.


---

### 📬 Postman Collection

A complete Postman collection is provided to simplify testing.

- **File:** `postman/LibraryManagementSystem.postman_collection.json`

#### 🚀 How to Import into Postman

1. Open [Postman](https://www.postman.com/)
2. Click **Import**
3. Select the file `LibraryManagementSystem.postman_collection.json` from the project folder
4. Use the pre-configured requests to test all available endpoints

> 🛡️ For secured endpoints, update the JWT token in the `Authorization` header (`Bearer <your_token>`).

## 📚 API Endpoints

The API offers a set of endpoints for managing books, users, borrowing/returning books, and more. Below is the description of all available endpoints.

### 🏠 Base URL
All API endpoints are accessible through the base URL:  
`http://localhost:8080/api/`

---

### 1. **Books Management**

#### 📄 Add a Book

- **Method:** `POST`
- **Endpoint:** `/books`
- **Description:** Add a new book to the library system.
- **Request Body:**
    ```json
    {
      "title": "Book Title",
      "author": "Author Name",
      "isbn": "1234567890123",
      "publicationDate": "2023-05-13",
      "genre": "Fiction"
    }
    ```

#### 🔍 View Book Details

- **Method:** `GET`
- **Endpoint:** `/books/{id}`
- **Description:** Get detailed information about a specific book.
- **Path Parameter:**
    - `id`: The ID of the book.

#### 🔎 Search for Books

- **Method:** `GET`
- **Endpoint:** `/books/search`
- **Description:** Search for books by title, author, ISBN, or genre.
- **Query Parameters:**
    - `title`: Search by title (optional)
    - `author`: Search by author (optional)
    - `isbn`: Search by ISBN (optional)
    - `genre`: Search by genre (optional)
- **Example:**
    ```bash
    GET /books/search?title=Harry Potter&author=J.K. Rowling
    ```

#### ✏️ Update Book Information

- **Method:** `PUT`
- **Endpoint:** `/books/{id}`
- **Description:** Update information for an existing book.
- **Path Parameter:**
    - `id`: The ID of the book.
- **Request Body:**
    ```json
    {
      "title": "Updated Book Title",
      "author": "Updated Author Name",
      "isbn": "1234567890123",
      "publicationDate": "2023-05-13",
      "genre": "Fantasy"
    }
    ```

#### 🗑️ Delete a Book

- **Method:** `DELETE`
- **Endpoint:** `/books/{id}`
- **Description:** Delete a book from the library system.
- **Path Parameter:**
    - `id`: The ID of the book to delete.

---

### 2. **User Management**

#### 📝 Register a User

- **Method:** `POST`
- **Endpoint:** `/users/register`
- **Description:** Register a new user (librarian or patron).
- **Request Body:**
    ```json
    {
      "name": "John Doe",
      "email": "john@example.com",
      "role": "patron",
      "password": "securepassword123"
    }
    ```

#### 👤 View User Details

- **Method:** `GET`
- **Endpoint:** `/users/{id}`
- **Description:** Get details of a specific user (librarians only).
- **Path Parameter:**
    - `id`: The ID of the user.

#### 🛠️ Update User Information

- **Method:** `PUT`
- **Endpoint:** `/users/{id}`
- **Description:** Update information for an existing user.
- **Path Parameter:**
    - `id`: The ID of the user.
- **Request Body:**
    ```json
    {
      "name": "John Updated",
      "email": "john_updated@example.com"
    }
    ```

#### 🗑️ Delete a User

- **Method:** `DELETE`
- **Endpoint:** `/users/{id}`
- **Description:** Delete a user from the system.
- **Path Parameter:**
    - `id`: The ID of the user.

---

### 3. **Borrowing and Returning Books**

#### 📚 Borrow a Book

- **Method:** `POST`
- **Endpoint:** `/borrow/{bookId}`
- **Description:** Borrow a book from the library.
- **Path Parameter:**
    - `bookId`: The ID of the book being borrowed.
- **Request Body:**
    ```json
    {
      "dueDate": "2023-06-13"
    }
    ```

#### 🔄 Return a Book

- **Method:** `POST`
- **Endpoint:** `/return/{bookId}`
- **Description:** Return a borrowed book to the library (patrons only).
- **Path Parameter:**
    - `bookId`: The ID of the book being returned.

#### 📜 View Borrowing History

- **Method:** `GET`
- **Endpoint:** `/borrow/history`
- **Description:** View borrowing history for the authenticated user.
- **Query Parameters:**
    - `userId`: (optional) The ID of the user (librarians can query others' histories).

#### 📅 Manage Overdue Books

- **Method:** `GET`
- **Endpoint:** `/borrow/overdue`
- **Description:** List all overdue books for the library (librarians only).

---

### 4. **Authentication**

#### 🔑 Login (JWT)

- **Method:** `POST`
- **Endpoint:** `/auth/login`
- **Description:** Authenticate a user and return a JWT token.
- **Request Body:**
    ```json
    {
      "email": "john@example.com",
      "password": "securepassword123"
    }
    ```
- **Response:**
    ```json
    {
      "token": "your_jwt_token_here"
    }
    ```

> 🔒 Use this token for all authenticated requests by adding it in the `Authorization` header:  
> `Authorization: Bearer <your_token>`

---

### 🔐 Security

- **Authentication:** All secure endpoints require a valid JWT token in the `Authorization` header.
- **Roles:**
  - `librarian`: Full access to the system (manage books, users, borrowing history).
  - `patron`: Can only view books and manage their own borrowing.

---

This section gives a detailed overview of the API and how to interact with it. You can learn more about in swagger documentation as well.

## 🧪 Tests and Code Coverage

### 📝 Testing Overview

This project includes unit and integration tests to ensure the correctness and reliability of the **Library Management System**. 

- **Testing Framework:** The project uses **JUnit 5** for unit and integration testing.
- **Test Execution:** Tests can be run using Maven, as described below.

### 🚀 Running Tests

You can run all the tests with Maven using the following command:

```bash
./mvnw test
```
## 📊 Database Schema/Design

The Library Management System uses a PostgreSQL database for storing data related to books, users, and borrowing transactions.

### 🗂️ Database Tables

The database consists of the following main tables:

#### 1. **Users**
- **id** (Primary Key)
- **name** (String)
- **email** (String, Unique)
- **role** (Enum: 'librarian', 'patron')
- **password** (String)


#### 2. **Books**
- **id** (Primary Key)
- **title** (String)
- **author** (String)
- **isbn** (String, Unique)
- **publication_date** (Date)
- **genre** (String)

#### 3. **Borrow**
- **borrow_id** (Primary Key)
- **user_id** (Foreign Key: Users)
- **book_id** (Foreign Key: Books)
- **borrow_date** (Timestamp)
- **due_date** (Timestamp)
- **return_date** (Timestamp, Nullable)


### 📝 Notes on Relationships

- **Users ↔ Borrowings**: One-to-many relationship. A user can borrow multiple books, but each borrowing record is associated with only one user.
- **Books ↔ Borrowings**: One-to-many relationship. A book can be borrowed multiple times, but each borrowing record references a specific book.

> 📌 **Foreign Keys** are used to maintain referential integrity between the tables.

---





   
