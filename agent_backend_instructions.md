# Gemini AI Instructions & Best Practices (Backend)

These instructions govern how you (Gemini AI) should approach building the Vigilant CI/CD monitoring tool Backend.

## Project Structure & Separation of Concerns

### Backend (Spring Boot)
- **Controller Layer (`com.vigilant.controller`)**: Only handle HTTP/WebSocket requests and responses. Delegate business logic to services. Keep controllers lean.
- **Service Layer (`com.vigilant.service`)**: Core business logic resides here. Services should be stateless. Do not mix data access logic with business logic.
- **Repository Layer (`com.vigilant.repository`)**: Exclusively for database interactions using Spring Data JPA.
- **Model Layer (`com.vigilant.model` / `entity` / `dto`)**: Clear separation between Database Entities and Data Transfer Objects (DTOs). Never expose raw database entities directly to the API; always map them to DTOs.
- **Kafka Layer (`com.vigilant.kafka`)**: Keep Kafka producers and consumers in their own dedicated packages. 
- **Config Layer (`com.vigilant.config`)**: All configurations (WebSocket, Kafka, Security) must be well-documented and centralized here.

### Strict Architectural Rules
- **Layered Architecture Flow**: Execution flow MUST ALWAYS be `Controller -> Service Interface -> Repository`. Controllers must never interact directly with a Repository.
- **Service Interfaces**: ALL services must be abstract (interfaces) with concrete implementations in an `impl` package (e.g., `UserService` interface and `UserServiceImpl` class).
- **Lombok Injection**: Manual constructors are strictly prohibited. Use Lombok's `@RequiredArgsConstructor` everywhere for dependency injection.
- **Entity Boilerplate**: Entities MUST use Lombok `@Getter`, `@Setter`, and `@NoArgsConstructor`. Do not write manual getters, setters, or default constructors.
- **Data Transfer Objects (DTOs)**: All DTOs MUST be implemented as Java `record`s to ensure simplicity and immutability. Do not use classes or Lombok for DTOs.

### Security & Configuration Rules
- **No Duplicate Configurations**: Do not duplicate configurations across the application. For example, if global CORS is defined in `WebConfig`, do not use `@CrossOrigin` on individual controllers.
- **API DTO Mapping**: REST Controllers MUST ONLY accept and return DTOs/records in `@RequestBody` and response payloads. NEVER expose raw database Entities directly to or from the API to avoid schema coupling and security vulnerabilities.
- **DTO Mapping Encapsulation (SRP)**: To maintain the Single Responsibility Principle, Controllers should NOT contain manual `mapToResponse` or object translation boilerplate. All mapping logic (e.g. `toEntity()` or `static fromEntity()`) MUST be encapsulated directly inside the DTO records.
- **No @Repository on Interfaces**: NEVER annotate Spring Data `JpaRepository` interfaces with `@Repository`. It is completely redundant since Spring automatically generates proxies and registers them as beans.
- **Input Validation**: Enforce validation using `jakarta.validation` annotations (like `@Valid`, `@NotBlank`, `@Pattern`) directly on the DTO records. Use `@Pattern` to explicitly reject special characters and prevent script/injection attacks instead of writing custom if/else checks.
- **Externalized Messaging**: NEVER hardcode validation error messages or exception messages directly in the code (e.g., inside `@NotBlank`). ALWAYS externalize messages into `src/main/resources/messages.properties` and inject them using SpEL-style interpolation (e.g., `{validation.error.key}`).
- **Environment Variables**: ALL important configuration details (database credentials, URLs, encryption keys, API tokens) MUST be passed as environment variables and never hardcoded in `application.yml`.
- **Clean Code (Method Size)**: Complex logic MUST be divided into smaller, well-named private helper methods. Avoid "big-ass" methods at all costs. Every logical block should have a descriptive method name and Javadoc comments explaining the *why* and *what* of the functionality.
- **Secret Management**: ALWAYS encrypt sensitive secrets (tokens, passwords, keys) using an `AttributeConverter` or similar mechanism before storing them in the database. Never store plaintext secrets.

## Coding Best Practices
- **Strict Typing**: Leverage Java 25 strictly. Use modern Java features (records for DTOs, switch expressions, var where appropriate but readable).
- **Error Handling**: Implement global exception handlers (`@ControllerAdvice`). Do not swallow exceptions; log them appropriately.
- **Workflow & Commits**: Always ensure the code compiles and tests pass before proposing it to the user. Add comments explaining *why* a certain approach was taken if it involves complex logic.
