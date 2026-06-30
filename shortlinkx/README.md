# ShortLinkX - URL Shortener & SaaS Platform

ShortLinkX is a production-ready, highly optimized SaaS URL Shortener application built using Spring Boot. It supports multi-user tenant boundaries, JWT authentication, custom alias configuration, link expiration limits, database indexing, daily automated cleanup schedules, high-performance Redis caching, in-memory rate limiting, and an analytics/metrics performance dashboard.

---

## Architecture & Technology Stack

* **Framework**: Spring Boot 3.5.x (Java 21)
* **Security**: Spring Security & Stateless JSON Web Tokens (JWT)
* **Database**: MySQL 8 (JPA / Hibernate ORM)
* **Caching**: Redis (via Spring Cache Abstraction)
* **Rate Limiting**: Sliding window token bucket via thread-safe `ConcurrentHashMap`
* **Scheduling**: Spring Scheduler
* **APIs & Docs**: REST standards, Springdoc-OpenAPI 3 / Swagger UI
* **Production Health**: Spring Boot Actuator with custom health checks

---

## Key Features

1. **URL Shortening & Expiration**: Create custom short codes or supply a custom alias. Specify optional expiry timestamps after which redirects return `HTTP 410 Gone`.
2. **Stateless JWT Authentication**: Secure user registration, logins, and API access boundaries.
3. **Database Performance Indexing**: Database columns `short_code`, `custom_alias`, `user_id`, and `expires_at` are indexed for speed.
4. **Redis Cache Abstraction**: Cache lookups, link metadata details, and user profiles. Automatic eviction policies on URL edits/deletions/clicks keep caches fresh.
5. **In-Memory Rate Limiting**: Limit of 100 URL creations per user per hour, returning `HTTP 429 Too Many Requests` when exceeded.
6. **Automated Expired Link Cleanup**: Nightly background cleanup deletes expired URL mapping records, logging execution time and counts.
7. **Performance Dashboard**: Fetch aggregated metrics detailing active vs expired counts, click totals, most clicked destination, and recent links.
8. **Swagger API Sandbox**: Fully documented endpoints with request/response model parameters, schemas, validation rules, and built-in Authorize JWT support.

---

## API Documentation & Routes

API documentation is generated dynamically at runtime.

* **Swagger UI Page**: `http://localhost:8082/swagger-ui.html`
* **OpenAPI Spec Details**: `http://localhost:8082/v3/api-docs`

### Major Endpoint Groups

#### 1. Authentication
* `POST /api/v1/auth/register` - Create a new user account.
* `POST /api/v1/auth/login` - Authenticate and fetch JWT token.

#### 2. URL Management
* `POST /api/v1/urls` - Shorten a URL (protected).
* `GET /{shortCode}` - Resolve and redirect (public).
* `GET /api/v1/urls/{shortCode}` - Retrieve link details (protected, owners only).
* `PUT /api/v1/urls/{shortCode}` - Update target URL or expiry date (protected, owners only).
* `DELETE /api/v1/urls/{shortCode}` - Delete a URL mapping (protected, owners only).

#### 3. User & Dashboard
* `GET /api/v1/users/me` - Fetch profile statistics (protected).
* `GET /api/v1/users/me/urls` - Paginated, sorted, and filtered URL list (protected).
* `GET /api/v1/users/me/dashboard` - Retrieve performance metrics dashboard (protected).

#### 4. Actuator Health
* `GET /actuator/health` - Custom health check verifying MySQL and Redis.
* `GET /actuator/metrics` - General JVM/runtime performance statistics.

---

## Installation & Local Setup

### Prerequisites
* Java 21 SDK
* MySQL Server 8
* Redis Server 6+
* Maven 3+

### Environment Setup
1. Copy the `.env.example` file in the root to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Configure database credentials, Redis host/port, and a strong JWT secret inside `.env`.

### Running Locally
Run the application using the Maven wrapper:
```bash
# Run with 'dev' profile (defaults to localhost connections)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Running Tests
Execute unit and integration tests:
```bash
./mvnw test
```
*Note: Test configurations run with a `create-drop` database schema and mock caching, requiring no active Redis instance to complete successfully.*
