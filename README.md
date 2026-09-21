# 🎫 Smart Ticket Router

### AI-Powered Customer Support Ticketing System

Smart Ticket Router is a full-stack customer support platform that uses AI to automatically classify, prioritize, and route support tickets.

It also provides authentication, role-based access control, ticket workflows, comments, SLA monitoring, and an admin dashboard.

**Built with:** Java · Spring Boot · PostgreSQL · React · TypeScript · Python · FastAPI · Scikit-learn · Docker

---

## 🚀 How It Works

```text
Customer
   ↓
Creates Ticket
   ↓
Spring Boot Backend
   ↓
AI Classification
   ↓
Category + Priority
   ↓
Support Team
   ↓
Agent Handles Ticket
   ↓
Resolved
   ↓
SLA Monitoring
```

The AI service analyzes the ticket title and description and returns a category, priority, and confidence score.

Example:

```json
{
  "category": "PAYMENT",
  "priority": "HIGH",
  "confidence": 0.90
}
```

---

## ✨ Features

### 🔐 Authentication & Authorization

- JWT authentication
- BCrypt password hashing
- Role-based access control
- Google OAuth2 login
- Email verification
- Password reset
- Protected API endpoints

### 🎫 Ticket Management

- Create and manage support tickets
- Ticket categories and priorities
- Automatic AI classification
- Automatic support-team routing
- Ticket assignment
- Ticket comments
- Ticket history
- Status workflow
- SLA tracking

### 👥 User Roles

| Role | Access |
|---|---|
| **CUSTOMER** | Create and manage own tickets |
| **AGENT** | Work on assigned/team tickets |
| **ADMIN** | Manage users, tickets, assignments, and dashboard |

Authorization is enforced by the backend, not only by the frontend.

---

## 🤖 AI Classification

The AI service is a separate FastAPI microservice.

### Pipeline

```text
Ticket Title + Description
          ↓
       TF-IDF
          ↓
Logistic Regression
          ↓
Category + Priority
          ↓
     Confidence
```

### Categories

```text
ACCOUNT
PAYMENT
TECHNICAL
DELIVERY
REFUND
OTHER
```

### Priorities

```text
LOW
MEDIUM
HIGH
CRITICAL
```

### Ticket Routing

```text
PAYMENT   → PAYMENT_SUPPORT
TECHNICAL → TECHNICAL_SUPPORT
ACCOUNT   → ACCOUNT_SUPPORT
DELIVERY  → DELIVERY_SUPPORT
REFUND    → GENERAL_SUPPORT
OTHER     → GENERAL_SUPPORT
```

---

## 🛡️ AI Failure Handling

The backend does not depend completely on the AI service.

If the AI service fails, times out, or returns very low confidence, the backend uses a fallback:

```text
Category = OTHER
Priority = MEDIUM
Status   = FALLBACK
```

This allows ticket creation to continue even when the AI service is unavailable.

---

## 🔄 Ticket Workflow

Tickets follow a controlled workflow:

```text
OPEN
 ↓
IN_PROGRESS
 ↓
WAITING_FOR_CUSTOMER
 ↓
IN_PROGRESS
 ↓
RESOLVED
 ↓
CLOSED
```

The backend validates status transitions and rejects invalid workflows.

Important status changes are stored in ticket history.

---

## ⏱️ SLA Monitoring

Each priority has a defined SLA:

| Priority | SLA |
|---|---:|
| CRITICAL | 1 hour |
| HIGH | 4 hours |
| MEDIUM | 12 hours |
| LOW | 24 hours |

A scheduled Spring task checks unresolved tickets and identifies SLA breaches.

```text
Ticket Created
      ↓
SLA Deadline
      ↓
 ┌────┴─────┐
 ↓          ↓
Resolved   Expired
 ↓          ↓
Success    SLA Breach
```

---

## 💬 Ticket Conversations

Customers and agents can communicate through ticket comments.

```text
Customer
   ↓
Creates Ticket
   ↓
Agent Responds
   ↓
Customer Replies
   ↓
Agent Resolves Ticket
```

Comment access is protected by the same ticket and team authorization rules.

---

## 📊 Admin Dashboard

Administrators can access operational metrics such as:

- Total tickets
- Open tickets
- In-progress tickets
- Resolved tickets
- SLA breaches
- Unassigned tickets
- Critical tickets

---

## 🏗️ Architecture

```text
┌─────────────────────────────┐
│     React + TypeScript      │
│          Frontend           │
└──────────────┬──────────────┘
               │ REST API
               ▼
┌─────────────────────────────┐
│       Spring Boot API       │
│                             │
│ Controllers                 │
│ Services                    │
│ Repositories                │
│ Security                    │
│ Ticket Workflow             │
│ Routing                     │
│ SLA Monitoring              │
└──────────┬───────────┬──────┘
           │           │
           ▼           ▼
┌────────────────┐  ┌────────────────┐
│   PostgreSQL   │  │   FastAPI AI   │
│                │  │                │
│ JPA / Hibernate│  │ Scikit-learn  │
└────────────────┘  └────────────────┘
```

The backend follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

The AI classifier runs independently from the Java backend and communicates through HTTP.

---

## 🛠️ Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- Hibernate
- Bean Validation
- Maven
- Flyway
- OpenAPI / Swagger

### Database

- PostgreSQL 17

### AI / ML

- Python
- FastAPI
- Scikit-learn
- TF-IDF
- Logistic Regression
- Joblib

### Frontend

- React
- TypeScript
- Tailwind CSS

### Infrastructure

- Docker
- Docker Compose
- Git
- GitHub

---

## 🧪 Engineering Practices

The project includes:

- Authentication and authorization testing
- Customer isolation testing
- Agent team isolation testing
- Admin access testing
- Ticket workflow testing
- SLA monitoring tests
- Comment authorization tests
- DTO-based API design
- Request validation
- Global exception handling
- Consistent API error responses
- Pagination and filtering
- JPA Specifications
- Database migrations with Flyway
- Swagger/OpenAPI documentation

---

## 🗄️ Database Model

Main entities:

```text
User
SupportTeam
Ticket
TicketComment
TicketHistory
```

Basic relationships:

```text
User
 ├── Ticket
 └── SupportTeam

Ticket
 ├── TicketComment
 └── TicketHistory
```

---

## 🐳 Run Locally

### 1. Clone the repository

```bash
git clone <your-repository-url>
cd smart-ticket-router
```

### 2. Start the application

```bash
docker compose up --build
```

### 3. Services

```text
Frontend    → http://localhost:5173
Backend     → http://localhost:8080
Swagger     → http://localhost:8080/swagger-ui.html
AI Service  → http://localhost:8001
PostgreSQL  → localhost:5433
```

Ports may be different depending on the local configuration.

---

## 📁 Project Structure

```text
smart-ticket-router/
│
├── backend/
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── src/test/
│
├── ai-service/
│   ├── app/
│   ├── models/
│   └── requirements.txt
│
├── frontend/
│
├── docker-compose.yml
├── ARCHITECTURE.md
├── API.md
├── DATABASE.md
├── REQUIREMENTS.md
├── TODO.md
└── README.md
```

---

## 📚 What This Project Demonstrates

This project demonstrates practical experience with:

- Java and Spring Boot
- REST API development
- Spring Security and JWT
- Role-based access control
- PostgreSQL and JPA/Hibernate
- Microservice communication
- Machine Learning integration
- React and TypeScript
- Docker
- Testing
- Database design
- SLA-based workflows
- Failure handling

---

## 🔮 Future Improvements

- Production deployment
- Redis caching
- Kafka and event-driven architecture
- Advanced observability
- More ML training data
- Better confidence calibration
- Automated notifications
- Advanced analytics
- CI/CD pipeline

---

## 🎯 Project Goal

The goal of Smart Ticket Router was to build a realistic full-stack system instead of a simple CRUD application.

It combines:

```text
Java
+
Spring Boot
+
PostgreSQL
+
React
+
TypeScript
+
Python
+
Machine Learning
+
Docker
+
Microservices
```

**Built to learn. Built to understand. Built to ship.**
