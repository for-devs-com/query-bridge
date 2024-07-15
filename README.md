# Dynamic Query Engine

## Project Structure
```
dynamic-query-engine/
├── src/
│ ├── main/
│ │ ├── java/
│ │ │ ├── fordevs/
│ │ │ │ ├── dynamicqueryengine/
│ │ │ │ │ ├── config/
│ │ │ │ │ │ ├── DataSourceContextService.java
│ │ │ │ │ │ ├── DynamicDataSourceManager.java
│ │ │ │ │ │ ├── GlobalExceptionHandler.java
│ │ │ │ │ │ ├── WebConfig.java
│ │ │ │ │ ├── controller/
│ │ │ │ │ │ ├── DatabaseNavigatorController.java
│ │ │ │ │ ├── dto/
│ │ │ │ │ │ ├── DatabaseCredentials.java
│ │ │ │ │ │ ├── DynamicTableData.java
│ │ │ │ │ ├── service/
│ │ │ │ │ │ ├── DatabaseService.java
│ │ │ │ │ │ ├── DatabaseServiceImpl.java
│ │ │ │ │ │ ├── SchemaDiscoveryService.java
│ │ │ │ │ ├── DynamicQueryEngineApplication.java
│ │ ├── resources/
│ │ │ ├── application.properties
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
```

## Technologies Used

- **Java**: The primary programming language for the application.
- **Spring Boot**: A framework to simplify the creation of stand-alone, production-grade Spring-based applications.
- **Spring Data JPA**: A part of the larger Spring Data family, makes it easier to implement JPA-based repositories.
- **Spring Web**: To build web, including RESTful, applications using Spring MVC.
- **Lombok**: A Java library that automatically plugs into your editor and build tools, to avoid writing boilerplate code.
- **H2 Database**: An in-memory database used for development and testing.
- **Maven**: A build automation tool used primarily for Java projects.

## Explanation of the Code

### Config Package
- **DataSourceContextService.java**: Manages the current data source context, allowing dynamic switching between different data sources.
- **DynamicDataSourceManager.java**: Manages dynamic data sources. Responsible for creating, testing, and managing connections to various databases.
- **GlobalExceptionHandler.java**: Handles global exceptions across the application, providing centralized exception handling.
- **WebConfig.java**: Configures web settings, including CORS configurations and other web-related settings.

### Controller Package
- **DatabaseNavigatorController.java**: REST controller that handles HTTP requests for database navigation. It delegates the actual business logic to the `DatabaseService`.

### DTO Package
- **DatabaseCredentials.java**: Data Transfer Object (DTO) that encapsulates database credentials like database name, host, username, and password.
- **DynamicTableData.java**: DTO that represents dynamic table data, including rows, columns, and pagination information.

### Service Package
- **DatabaseService.java**: Interface for database-related operations. It provides method signatures for connecting to a database, listing tables and columns, getting table data with pagination, and executing SQL queries.
- **DatabaseServiceImpl.java**: Implementation of the `DatabaseService` interface. Contains the business logic for database operations, including validation of credentials and handling exceptions.
- **SchemaDiscoveryService.java**: Service that provides methods for discovering schema elements such as tables and columns in a database.

### Main Application Class
- **DynamicQueryEngineApplication.java**: The main application class that bootstraps the Spring Boot application.

### Resources
- **application.properties**: Configuration file for the Spring Boot application. Contains various properties for configuring the application.

## How to Run the Project

1. **Clone the repository**:
    ```bash
    git clone <repository-url>
    cd dynamic-query-engine
    ```

2. **Build the project**:
    ```bash
    ./mvnw clean install
    ```

3. **Run the application**:
    ```bash
    ./mvnw spring-boot:run
    ```

4. **Access the application**:
    - The application will be running at `http://localhost:8080`.

## Endpoints

- **POST /api/connect-database**: Connects to the database using the provided credentials.
- **GET /api/listTables**: Lists the tables in the connected database.
- **GET /api/columns/{tableName}**: Lists the columns of the specified table.
- **GET /api/data/{tableName}**: Retrieves the data of the specified table with pagination.
- **POST /api/executeQuery**: Executes a SQL query and returns the result.

## Contributing

1. **Fork the repository**.
2. **Create a new branch** (`git checkout -b feature/your-feature`).
3. **Commit your changes** (`git commit -am 'Add some feature'`).
4. **Push to the branch** (`git push origin feature/your-feature`).
5. **Create a new Pull Request**.

## License
Owned by for-devs. Still need to determine which license is best.
