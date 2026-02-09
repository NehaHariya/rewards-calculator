# rewardsCalculator

Lightweight Spring Boot service that calculates customer reward points from transaction records.

Overview
- Java 21, Spring Boot 3.2.6.
- REST endpoints to return per-customer monthly reward totals and overall totals.
- Sample data lives in `src/main/resources/sample-data.json` and is loaded at startup by `SampleDataLoader`.

Rewards rules
- 2 points for every whole dollar spent over $100 in a single transaction.
- 1 point for every whole dollar spent over $50 up to $100 in a single transaction.
- Example: a $120 purchase = 2 * (120 - 100) + 50 = 90 points.

Endpoints
- GET /customers
  - Returns a JSON array of customers with their per-month rewards and totalRewards.
- GET /customers/{name}
  - Returns a single customer by name (case-insensitive). URL-encode special characters and spaces (example below).
- GET /transactions
  - Returns a flat JSON array listing every valid transaction from the sample dataset. Each element contains:
    - `customerName` (string)
    - `month` (int) — normalized to 0 if missing in the source data
    - `amount` (double)
  - This endpoint is useful for debugging or exporting raw transaction lists.
- The same endpoints are also exposed under the API base path: GET /api/rewards/customers, GET /api/rewards/customers/{name}, GET /api/rewards/transactions

Sample data
- `src/main/resources/sample-data.json` contains multiple customers and edge-case transactions (exact thresholds, fractional dollars, zeros/negatives, large amounts, empty months, Unicode names).
- The app ignores null/negative/zero transaction amounts and counts only whole dollars toward points (uses floor semantics).
- For the `GET /transactions` endpoint:
  - null transaction amounts are skipped
  - non-positive amounts (<= 0) are skipped
  - month keys are used as-is; if a month is missing (null) the endpoint returns month = 0

Build & run
Prerequisites
- JDK 21 installed and configured (the project uses language level 21).
- Maven (the repository includes the Maven wrapper `mvnw`).

Build the project:

```bash
./mvnw -DskipTests package
```

Run the app (reads `src/main/resources/application.properties`):

```bash
./mvnw spring-boot:run
# or run the packaged artifact
java -jar target/rewards-0.0.1-SNAPSHOT.war
```

By default (in this repo) the application properties set `server.port=8081` to avoid conflicts during local development — check `src/main/resources/application.properties` if you need to change the port.

Quick examples (adjust port if you changed it):

- List all customers:
```bash
curl -sS http://localhost:8081/customers | jq
```

- Get a single customer (plain name):
```bash
curl -sS http://localhost:8081/customers/Customer1 | jq
```

- Get a customer with spaces/Unicode in the name (example `Special Name Üser`):
```bash
# URL-encode space and Ü
curl -sS http://localhost:8081/customers/Special%20Name%20%C3%9Cser | jq
```

- List all transactions (flat list):
```bash
curl -sS http://localhost:8081/transactions | jq
# or under the API base path
curl -sS http://localhost:8081/api/rewards/transactions | jq
```

Example `GET /transactions` output (truncated):

```json
[
  { "customerName": "Customer1", "month": 1, "amount": 55.0 },
  { "customerName": "Customer1", "month": 1, "amount": 100.5 },
  { "customerName": "Customer1", "month": 1, "amount": 120.0 },
  ...
]
```

Testing
- Unit and integration tests are included under `src/test/java`.
- Run tests with:

```bash
./mvnw test
```

Notes and troubleshooting
- If you hit a Whitelabel 404 or connection error, confirm the application started and is listening on the expected port (see application logs for "Tomcat initialized with port ...").
- If you see editor/IDE errors about `record` or Java 21 features, update your IDE project SDK / language level to JDK 21 and re-import the Maven project.
- Large aggregated reward totals could overflow `int` for extremely large datasets; if you expect that, consider switching totals to `long`.

Further improvements you can make
- Add JSON Schema validation for `sample-data.json` or make the sample data path configurable via `application.properties`.
- Wire an in-memory DB (H2 + data.sql) if you want to demo persistence or real SQL aggregation.
- Expose OpenAPI (springdoc) documentation for the endpoints.

Contact / development notes
- Models are implemented as Java records for concise immutable DTOs.
- `SampleDataLoader` uses Jackson to parse `sample-data.json` and exposes a typed Map that `RewardsService` consumes.

Enjoy — let me know if you want me to add more tests, change totals to `long`, or move sample data into YAML or a seeded database.
