<h1>Bank Service</h1>

A simple banking service implemented in Kotlin + Spring Boot, supporting:

- Account creation
- Deposits
- Withdrawals (no overdrafts allowed)
- Transfers between accounts
- Balance checks

The service is designed to reflect real-world banking constraints, including concurrency safety and atomic operations.


<h2>Tech Stack</h2>

- Kotlin 1.9
- Spring Boot 3
- In-memory storage (ConcurrentHashMap)
- JUnit 5

<h2>How to run</h2>

`./gradlew bootRun`

This will run a CommandLineRunner demo.

<h2>Run Tests</h2>

`./gradlew test`
