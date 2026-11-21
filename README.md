# Drive

A Google Drive–style cloud storage backend built with **Spring Boot**. Users register, log in with JWT-based authentication, and store files and folders in **Amazon S3**, while file metadata lives in **PostgreSQL**.

> **Status:** work in progress. Authentication, file upload, download, folder creation and listing work. Folder upload and trash/delete endpoints are not implemented yet (see [Roadmap](#roadmap)).

## Features

- **User accounts** – registration with validation, BCrypt password hashing (strength 12), email normalization
- **JWT authentication** – short-lived access tokens plus rotating refresh tokens
  - Refresh tokens are random 64-byte values, stored only as SHA-256 hashes, and can be revoked on logout
  - Tokens are also set as `HttpOnly`, `Secure`, `SameSite=Lax` cookies
- **File upload** – multipart uploads (up to 100 MB) streamed to S3, with a metadata record saved in PostgreSQL. If the database write fails, the uploaded S3 object is rolled back
- **File download** – streams objects directly from S3 with the original filename and MIME type, restricted to the file's owner
- **Folders** – create folders (stored as S3 prefixes); the schema supports nested folders through a self-referencing `parent_id`
- **Per-user storage** – every object key is prefixed with the owner's user ID
- **Versioned schema** – database migrations managed by Flyway
- **Consistent error responses** – a global exception handler returns a uniform JSON error body

## Tech stack

| Area | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.1.2 (Web MVC, Security, Data JPA, Validation) |
| Database | PostgreSQL 17 |
| Migrations | Flyway |
| Object storage | AWS SDK v2 for S3 |
| Auth | JJWT 0.13 |
| Build | Maven (wrapper included) |
| Testing | JUnit, Testcontainers, Spring REST Docs |

## Getting started

### Prerequisites

- JDK 21
- Docker (for PostgreSQL, and for the Testcontainers-based tests)
- An AWS account with an S3 bucket and an IAM access key that can read, write and delete objects in it

### 1. Clone the repository

```bash
git clone https://github.com/amitab-dev/Drive.git
cd Drive
```

### 2. Configure environment variables

| Variable | Required | Default | Description |
| --- | --- | --- | --- |
| `AWS_ACCESS_KEY_ID` | Yes | – | AWS access key |
| `AWS_SECRET_ACCESS_KEY` | Yes | – | AWS secret key |
| `S3_BUCKET_NAME` | Yes | – | Name of the S3 bucket to store files in |
| `JWT_SECRET` | Recommended | placeholder value | Secret used to sign access tokens. **Always set your own in production** |
| `JWT_TTL` | No | `900` | Access token lifetime in seconds (15 minutes) |
| `REFRESH_TTL` | No | `604800` | Refresh token lifetime in seconds (7 days) |

```bash
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export S3_BUCKET_NAME=your-bucket-name
export JWT_SECRET=a-long-random-secret
```

The S3 client currently targets the `ap-south-1` (Mumbai) region, so create your bucket there or change the region in `S3Config.java`.

### 3. Start PostgreSQL

```bash
docker compose up -d
```

This starts Postgres 17 on port `5432` with database `drive` (user `postgres`, password `password`). These are local development credentials only. Flyway applies the migrations automatically when the application starts.

### 4. Run the application

```bash
./mvnw spring-boot:run
```

The API is served on `http://localhost:8080`.

### Run the tests

```bash
./mvnw test
```

The tests start a throwaway PostgreSQL container through Testcontainers, so Docker must be running.

## API reference

All endpoints are under `/api/v1`. Everything except `/api/v1/auth/**` requires an `Authorization: Bearer <access_token>` header.

### Authentication

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/auth/` | Register a new user |
| `POST` | `/api/v1/auth/login` | Log in and receive an access token |
| `GET` | `/api/v1/auth/refresh` | Issue a new token pair using the refresh token cookie |
| `GET` | `/api/v1/auth/logout` | Revoke the refresh token and clear cookies |

**Register**

```json
POST /api/v1/auth/
{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "password": "secret123"
}
```

**Login**

```json
POST /api/v1/auth/login
{
  "email": "jane@example.com",
  "password": "secret123"
}
```

The response contains the user ID and access token, and the access and refresh tokens are also set as cookies.

### Files and folders

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/file/{userId}/upload` | Upload a file (multipart field `file`) |
| `GET` | `/api/v1/file/{userId}` | List the objects stored for a user |
| `GET` | `/api/v1/file/{fileId}/download` | Download a file owned by the authenticated user |
| `POST` | `/api/v1/file/{userId}/create-folder` | Create a folder (`{"folderName": "Documents"}`) |
| `POST` | `/api/v1/file/{userId}/upload-folder` | Not implemented yet |

**Example: upload a file**

```bash
curl -X POST "http://localhost:8080/api/v1/file/<userId>/upload" \
  -H "Authorization: Bearer <access_token>" \
  -F "file=@./report.pdf"
```

**Example: download a file**

```bash
curl -OJ "http://localhost:8080/api/v1/file/<fileId>/download" \
  -H "Authorization: Bearer <access_token>"
```

### Error format

```json
{
  "success": false,
  "status": "NOT_FOUND",
  "message": "User not found",
  "path": "/api/v1/file/...",
  "timestamp": "2026-11-18T16:45:00Z"
}
```

## Project structure

```
src/main/java/amitab/drive/
├── config/        Spring Security configuration
├── contollers/    REST controllers (auth, files)
├── dto/           Request and response objects
├── entity/        JPA entities (User, Token, File) and enums
├── exception/     Custom exceptions and the global handler
├── repository/    Spring Data JPA repositories
├── security/      JWT authentication filter
├── services/      Business logic (auth, tokens, files)
└── storage/       S3 client config and storage abstraction

src/main/resources/
├── application.yaml
└── db/migration/  Flyway SQL migrations (V1–V4)
```

## Database schema

| Table | Purpose |
| --- | --- |
| `users` | Accounts: name, unique email, hashed password, role |
| `tokens` | Hashed refresh tokens with expiry and revocation time |
| `files` | File and folder metadata: owner, name, S3 key, MIME type, size, status, parent folder, trash time |

## Roadmap

- [x] Download files
- [ ] Upload whole folders
- [ ] Move to trash, restore and permanent delete
- [ ] Rename and move files and folders
- [ ] Restrict file endpoints to the authenticated user's own ID
- [ ] Share files with other users
- [ ] Configurable S3 region
- [ ] Unit and integration tests for services and controllers

## Contributing

Issues and pull requests are welcome. Please open an issue first to discuss any larger change.

## License

No license has been specified yet. Add a `LICENSE` file to define how others may use this code.
