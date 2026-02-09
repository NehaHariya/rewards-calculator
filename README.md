# rewardsCalculator

Lightweight Spring Boot service that calculates customer reward points from transaction records.

Quick overview
- Java 21, Spring Boot 3.
- Loads sample data from `src/main/resources/sample-data.json` at startup.
- Exposes simple REST endpoints to view transactions and per-customer reward totals.

Default port
- The application runs on port 8080 by default (so the base URL is http://localhost:8080).

Reward rules (per transaction)
- 2 points for every whole dollar spent over $100.
- 1 point for every whole dollar spent over $50 up to $100.
- Example: $120 -> 2*(120-100) + 50 = 90 points.

Endpoints
- GET /customers
  - Returns a JSON array of customers. Each customer includes monthly reward totals and a `totalRewards` field.
  - Example: http://localhost:8080/customers

- GET /customers/{name}
  - Returns a single customer by name (case-insensitive). URL-encode spaces and special characters.
  - Example (browser or curl): http://localhost:8080/customers/Customer1
  - Example with space/unicode: http://localhost:8080/customers/Special%20Name%20%C3%9Cser

- GET /transactions
  - Returns a flat JSON array of all valid transactions from the sample dataset. Each transaction contains:
    - `customerName` (string)
    - `month` (int) — if a source month is missing the endpoint returns month = 0
    - `amount` (double)
  - Notes: null, zero, or negative amounts are skipped; only whole dollars are counted toward points (floor semantics).
  - Example: http://localhost:8080/transactions

API base path
- The same endpoints are available under the API base path:
  - GET /api/rewards/customers
  - GET /api/rewards/customers/{name}
  - GET /api/rewards/transactions
  - Example: http://localhost:8080/api/rewards/customers

Sample data
- Located at `src/main/resources/sample-data.json`. It includes edge cases (exact thresholds, fractional amounts, zero/negative values, missing months, Unicode names).

Run locally
Prerequisites
- JDK 21 and Maven (the repo includes the Maven wrapper).

Build and run
```bash
./mvnw -DskipTests package
./mvnw spring-boot:run
# or run the packaged artifact
java -jar target/rewards-0.0.1-SNAPSHOT.war
```

Testing
- Run unit tests:
```bash
./mvnw test
```

Notes
- If you don't see the app on the expected port, check `src/main/resources/application.properties` — the port can be changed there.
- The service ignores invalid transactions (null/<=0 amounts) and counts only whole dollars for points.

Want changes?
- If you'd like the README even shorter, or want example responses added for each endpoint, tell me which endpoints to expand and I will add example payloads.
