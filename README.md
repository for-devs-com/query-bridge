# Dynamic Query Engine

Dynamic Query Engine (DQE) is a robust and flexible framework designed to facilitate dynamic database interactions through a set of RESTful APIs. It allows users to connect to various databases, execute queries, and retrieve data dynamically without hardcoding database configurations or query structures.

## Features

- **Dynamic Database Connectivity**: Connect to different databases at runtime using provided credentials.
- **Query Execution**: Execute SQL queries dynamically and retrieve results.
- **Table and Column Discovery**: List tables and columns in the connected database.
- **Data Retrieval**: Fetch data from tables with pagination support.
- **Secure Query Handling**: Prevent SQL injection through rigorous input validation and prepared statements.

## API Endpoints

- **POST /api/connect-database**: Connects to the database using the provided credentials.
- **GET /api/listTables**: Lists the tables in the connected database.
- **GET /api/columns/{tableName}**: Lists the columns of the specified table.
- **GET /api/data/{tableName}**: Retrieves the data of the specified table with pagination.
- **POST /api/executeQuery**: Executes a SQL query and returns the result.

## How It Works

1. **Dynamic Connection**: The engine establishes a connection to the database using credentials provided via API.
2. **Query Execution**: Users can execute SQL queries dynamically. The engine validates and sanitizes these queries to prevent SQL injection.
3. **Data Retrieval**: The results of the queries are returned in a structured format, allowing for easy integration with other applications.
4. **Context Management**: The engine manages database contexts and connections efficiently using a thread-safe approach.

## Security and Validation

- **Input Validation**: Ensures that all SQL queries are validated to prevent SQL injection attacks.
- **Prepared Statements**: Uses prepared statements to execute queries, enhancing security.
- **Error Handling**: Provides detailed logging and error messages to aid in debugging and maintaining the application.

## Usage

To use the Dynamic Query Engine, you can send requests to the provided endpoints with the necessary parameters. Below is an example of how to connect to a database and execute a query:

**Connecting to a Database**:
```sh
POST /api/connect-database
{
    "databaseName": "mydb",
    "host": "localhost",
    "port": 5432,
    "userName": "user",
    "password": "password"
}
````

POST /api/executeQuery
```
SELECT * FROM my_table WHERE id = 1
```


## Postman Collection

To help you get started with the Dynamic Query Engine (DQE) APIs, we have provided a Postman collection. You can use this collection to test the endpoints and see how the APIs work.

### How to Use the Postman Collection

1. Download the [Postman collection](./postman/DQE.postman_collection.json) file.
2. Open Postman.
3. Click on `Import` in the top-left corner.
4. Select the `Upload Files` tab.
5. Choose the downloaded JSON file and click `Open`.
6. The collection will be imported into Postman, and you can start using the APIs.

### License Discussion

When you're ready to discuss which license to use for your software, consider the following common licenses and their purposes:

1. **MIT License**: A permissive license that allows reuse with few restrictions.
2. **Apache License 2.0**: Similar to the MIT License but includes provisions for patent rights.
3. **GNU General Public License (GPL)**: Requires any distributed modifications to also be open-source.
4. **BSD License**: Another permissive license with fewer restrictions.

Each license has different implications for how others can use, modify, and distribute your software, so it's important to choose one that aligns with your goals for the project.

Feel free to reach out whenever you're ready to discuss the licensing options!
