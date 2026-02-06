# Concert Ticket Booking System

A robust, scalable Spring Boot-based concert ticket booking system with real-time pricing, distributed caching, and event-driven architecture.

## 🎯 Features

- **User Authentication & Authorization** - JWT-based authentication with role-based access control (USER/ADMIN)
- **Concert Management** - Create, update, and search concerts with advanced filtering
- **Dynamic Pricing** - Real-time ticket pricing based on demand and availability
- **Booking System** - Secure ticket booking with idempotency support
- **Real-time Availability** - Live ticket availability tracking with Redis caching
- **Event-Driven Architecture** - Kafka integration for asynchronous event processing
- **Analytics & Reporting** - Admin dashboard with sales analytics and settlement reports
- **Database Migration** - Flyway-based schema versioning and migration

## 🏗️ Architecture

### Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Framework** | Spring Boot | 3.5.10 |
| **Language** | Java | 21 |
| **Database** | PostgreSQL | 15 |
| **Cache** | Redis | 7 |
| **Message Broker** | Apache Kafka | Latest |
| **Security** | Spring Security + JWT | - |
| **Migration** | Flyway | - |
| **Build Tool** | Maven | - |

### System Components

```
┌─────────────────────────────────────────────────────────┐
│                    API Gateway Layer                     │
│              (Spring Security + JWT Auth)                │
└─────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│   Concert    │   │   Booking    │   │  Analytics   │
│  Controller  │   │  Controller  │   │  Controller  │
└──────────────┘   └──────────────┘   └──────────────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            ▼
                   ┌─────────────────┐
                   │  Service Layer  │
                   └─────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  PostgreSQL  │   │    Redis     │   │    Kafka     │
│   Database   │   │    Cache     │   │Event Broker  │
└──────────────┘   └──────────────┘   └──────────────┘
```

## 📋 Prerequisites

Before running this application, ensure you have the following installed:

- **Java 21** or higher
- **Maven 3.8+**
- **Docker & Docker Compose** (for containerized deployment)
- **Git**

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd ticketing-system
```

### 2. Environment Configuration

Create a `.env` file in the project root (or use the existing one):

```properties
# Application
SPRING_APPLICATION_NAME=
SERVER_PORT=

# PostgreSQL
POSTGRES_DB=
POSTGRES_USER=
POSTGRES_PASSWORD=
SPRING_DATASOURCE_URL=

# Redis
SPRING_DATA_REDIS_HOST=
SPRING_DATA_REDIS_PORT=

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=
SPRING_KAFKA_CONSUMER_GROUP_ID=

# JWT
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=

# Docker Compose
SPRING_DOCKER_COMPOSE_ENABLED=
SPRING_DOCKER_COMPOSE_LIFECYCLE_MANAGEMENT=
```

### 3. Running with Docker Compose (Recommended)

The application uses Docker Compose to automatically start all required services:

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

Services will be available at:
- **Application**: http://localhost:8080
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379
- **Kafka**: localhost:9092

### 4. Running Locally (Development)

If you prefer to run the application locally without Docker:

#### Start Infrastructure Services

```bash
# Start only PostgreSQL, Redis, and Kafka
docker-compose up -d db redis kafka
```

#### Run the Application

```bash
# Using Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean package -DskipTests
java -jar target/ticketing-0.0.1-SNAPSHOT.jar
```

## 📚 API Documentation

### Swagger UI

Once the application is running, access the interactive API documentation at:

**http://localhost:8080/swagger-ui.html**

> [!TIP]
> **Authentication in Swagger:**
> 1. Click the **"Authorize"** button at the top right.
> 2. Enter your JWT token (obtained from `/api/v1/auth/login`) in the format: `Bearer <your_token>`.


### API Endpoint Specification (Reference)

#### Concert Management
- `GET /api/v1/concerts` - List all concerts with filters
- `GET /api/v1/concerts/{id}` - Get concert details
- `POST /api/v1/concerts` - Create concert (Admin only)
- `PUT /api/v1/concerts/{id}` - Update concert (Admin only)

#### Booking Management
- `POST /api/v1/bookings` - Create new booking (requires Idempotency-Key)
- `GET /api/v1/bookings/{id}` - Get booking details
- `GET /api/v1/bookings` - List user's bookings
- `POST /api/v1/bookings/{id}/cancel` - Cancel booking

#### Pricing & Availability
- `GET /api/v1/concerts/{id}/pricing` - Get real-time pricing
- `GET /api/v1/concerts/{id}/availability` - Get current availability

#### Settlement & Reports
- `GET /api/v1/concerts/{id}/settlement` - Get settlement report (Admin only)
- `GET /api/v1/transactions` - List all transactions (Admin only)
- `GET /api/v1/analytics/dashboard` - Real-time analytics dashboard (Admin only)

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Integration Tests Only

```bash
mvn test -Dtest=*IntegrationTest
```

### Test Coverage

The project includes integration tests for:
- Authentication (register, login)
- Concert management (CRUD operations)
- Booking workflow
- Pricing and availability

## 🗄️ Database

### Schema Management

Database schema is managed using Flyway migrations located in:
```
src/main/resources/db/migration/
```

### Migration Files

- `V1__init_schema.sql` - Initial schema creation
- Additional migrations follow the naming pattern: `V{version}__{description}.sql`

### Manual Database Access

```bash
# Connect to PostgreSQL
docker exec -it ticketing-db psql -U admin -d concert

# Common queries
\dt                    # List tables
\d events              # Describe events table
SELECT * FROM users;   # Query users
```

## 🔒 Security

### JWT Authentication

The system uses JWT tokens for authentication:

1. **Login** with Basic Auth to receive a JWT token
2. **Include token** in subsequent requests: `Authorization: Bearer <token>`
3. **Tokens expire** after a configured period (check application.properties)

### Role-Based Access Control

- **USER**: Can view concerts, create bookings, manage own bookings
- **ADMIN**: Full access including concert creation, analytics, settlements

## 📊 Monitoring & Logging

### Application Logs

Logs are configured via Logback and stored in:
```
logs/application.log
```

### Log Levels

Configure in `.env`:
```properties
LOGGING_LEVEL_COM_CONCERT_TICKETING=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB=INFO
LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG
```

## 🐳 Docker Deployment

### Build Docker Image

```bash
docker build -t concert-ticketing-system .
```

### Run with Docker Compose

```bash
docker-compose up -d
```

### Environment Variables for Docker

Docker-specific environment variables are prefixed with `DOCKER_` in the `.env` file.

## 🛠️ Development

### Project Structure

```
ticketing-system/
├── src/
│   ├── main/
│   │   ├── java/com/concert/ticketing/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── exception/       # Exception handlers
│   │   │   ├── filter/          # Security filters
│   │   │   ├── model/           # JPA entities
│   │   │   ├── repositories/    # JPA repositories
│   │   │   ├── services/        # Business logic
│   │   │   └── utils/           # Utility classes
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/    # Flyway migrations
│   └── test/                    # Integration tests
├── docker-compose.yaml
├── Dockerfile
├── pom.xml
└── README.md
```

### Code Style

- Follow Java naming conventions
- Use Lombok annotations to reduce boilerplate
- Write integration tests for new features
- Document public APIs with JavaDoc

## 🚨 Troubleshooting

### Port Already in Use

```bash
# Check what's using port 8080
netstat -ano | findstr :8080

# Kill the process (Windows)
taskkill /PID <PID> /F
```

### Database Connection Issues

```bash
# Verify PostgreSQL is running
docker ps | grep ticketing-db

# Check logs
docker logs ticketing-db
```

### Redis Connection Issues

```bash
# Test Redis connection
docker exec -it ticketing-redis redis-cli ping
# Should return: PONG
```

### Kafka Issues

```bash
# Check Kafka logs
docker logs kafka

# List topics
docker exec -it kafka kafka-topics.sh --list --bootstrap-server localhost:9092
```

## 📝 Postman Collection

Import the Postman collection for easy API testing:
```
ticketing_system_postman_collection.json
```

The collection includes:
- Pre-configured environment variables
- All API endpoints with example requests
- Authentication setup

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Contact

For questions or support, please contact the development team.

---

**Built with ❤️ using Spring Boot**
