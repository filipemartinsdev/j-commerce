# 🤖 J-Commerce AI Agent Documentation

Complete development guide for AI agents working on the J-Commerce microservices platform.

---

## 📚 Documentation Structure

This folder contains **8 comprehensive guides** totaling **5,900+ lines** of documentation:

### 1. **AGENT.md** (Start Here! 👈)
**Quick introduction to the documentation**
- Overview of all guides
- Quick navigation
- When to use each guide
- Getting started instructions
- Tech stack summary

**Use when**: First time here? Start with this!

---

### 2. **OVERVIEW.md** (15 min read)
**What is J-Commerce and how it works**
- Project purpose and goals
- Architecture overview (visual diagram)
- Distributed authentication (JWT with ECC)
- Inter-service communication patterns
- Complete tech stack
- Overview of all 5 microservices
- Data flow examples
- Service dependency matrix

**Use when**: Need to understand the big picture

---

### 3. **ARCHITECTURE.md** (20 min read)
**How the code is organized and patterns used**
- 4-layer clean architecture (domain → application → infra)
- Directory structure template
- Layer responsibilities explained
- Domain-Driven Design (DDD) principles
- Event-driven architecture
- Communication patterns (HTTP vs Events)
- Data Transfer Objects (DTOs)
- Exception handling
- Security architecture
- Standard response format

**Use when**: Implementing features, modifying code structure, understanding design patterns

---

### 4. **DEVELOPMENT.md** (15 min read)
**Coding standards and best practices**
- Naming conventions (classes, methods, variables, constants)
- Package organization
- 5 core coding principles (SRP, DI, null handling, immutability, fail-fast)
- Common code patterns with examples:
  - Services with repositories
  - Controllers with services
  - Entities with business methods
  - Event publishing
- Anti-patterns to avoid
- Code review checklist
- Logging guidelines
- Feature implementation checklist

**Use when**: Writing code, reviewing code, following project standards

---

### 5. **MICROSERVICES.md** (25 min read)
**Detailed information about each microservice**
- 5-service overview table
- **Identity Service** (Authentication & user profiles)
  - Database schema
  - API endpoints
  - Key classes
  - How to use
- **Product Service** (Catalog & inventory)
- **Order Service** (Shopping cart & orders)
- **Payment Service** (Payment processing)
- **Notification Service** (Email notifications)
- Inter-service communication examples (HTTP and Events)
- Working with microservices guide
- Service dependency matrix

**Use when**: Working on specific services, need API reference, implementing service communication

---

### 6. **TESTING.md** (20 min read) ⭐ NEW
**Unit testing standards and complete patterns**
- Testing philosophy (AAA pattern, isolation, fast execution)
- Complete service test structure with real examples
- AdminProductServiceTests as reference model
- 6 key test patterns:
  - Success cases
  - Exception/failure cases
  - Pagination tests
  - Event publishing verification
  - Empty result handling
  - State change validation
- Test case organization for each method
- Writing checklist
- Anti-patterns to avoid
- Mockito usage guide (stubbing & verification)
### 6. **TESTING.md** (20 min read)
**Unit testing standards with Mockito and JUnit 5**
- Testing philosophy (AAA pattern, isolation, fast execution)
- Complete service test structure with real examples
- AdminProductServiceTests as reference model
- 6 key test patterns
- Mockito usage guide (stubbing & verification)
- Test case organization
- Writing checklist
- Anti-patterns to avoid

**Use when**: Writing unit tests for Services, need testing patterns, improving test quality

---

### 7. **INTEGRATION_TESTING.md** (25 min read) ⭐ NEW
**Integration testing standards with @DataJpaTest and @SpringBootTest**
- Integration vs Unit testing philosophy
- Types of integration tests (Repository, Service+Repo, Controller, Full Stack)
- Repository tests with @DataJpaTest and @ActiveProfiles("test")
- Controller tests with MockMvc
- Service + Repository integration tests
- Constraint and validation testing
- Lazy loading and relationship testing
- Complete code templates and examples
- Test coverage recommendations per layer

**Use when**: Writing Repository tests, Controller tests, testing with real database, @DataJpaTest patterns

---

### 8. **REFERENCE.md** (Quick lookups)

---

## 🎯 Quick Start Guide

### First Visit? Follow this order:

1. **Read AGENT.md** (this file) - 5 min
2. **Skim OVERVIEW.md** - 10 min
3. **Read ARCHITECTURE.md** - 20 min
4. **Pick a service from MICROSERVICES.md** and read its section - 10 min
5. **Explore the code** - Find similar patterns and study them
6. **Start coding** - Use DEVELOPMENT.md and REFERENCE.md as guides

**Total: ~45 minutes to get productive**

---

## 🗺️ Find Information By Need

### Understanding the System
- **"What does this project do?"** → OVERVIEW.md
- **"How do services communicate?"** → ARCHITECTURE.md or OVERVIEW.md
- **"What's the tech stack?"** → OVERVIEW.md (Tech Stack section)
- **"How does authentication work?"** → OVERVIEW.md (Distributed Authentication)

### Writing Code
- **"How do I add a new endpoint?"** → DEVELOPMENT.md + ARCHITECTURE.md (Service Pattern)
- **"What's the naming convention?"** → DEVELOPMENT.md (Naming Conventions)
- **"How should I structure code?"** → ARCHITECTURE.md (Layered Architecture)
- **"What exceptions should I throw?"** → DEVELOPMENT.md (Exception Handling)
- **"Show me example code"** → REFERENCE.md (Common Snippets)

### Working on Services
- **"I need to work on [Service]"** → MICROSERVICES.md ([Service] section)
- **"What's the API for this service?"** → MICROSERVICES.md + REFERENCE.md (Endpoint Reference)
- **"What database schema exists?"** → MICROSERVICES.md (Database Schema)
- **"How do I call another service?"** → ARCHITECTURE.md (Communication Patterns)

### Code Quality
- **"Is this code good?"** → DEVELOPMENT.md (Anti-Patterns, Code Review Checklist)
- **"How do I review code?"** → DEVELOPMENT.md (Code Review Checklist)
- **"What's the best practice?"** → DEVELOPMENT.md (Coding Principles)

---

## 🎓 Key Concepts at a Glance

### Architecture Layers

```
┌─────────────────────────────────────┐
│  Infra (HTTP, Database)             │ ← Controllers, Repositories
├─────────────────────────────────────┤
│  Application (Use Cases)            │ ← Services, DTOs, Events
├─────────────────────────────────────┤
│  Domain (Business Logic)            │ ← Entities, Rules
├─────────────────────────────────────┤
│  External (Spring, Jakarta)         │ ← Frameworks
└─────────────────────────────────────┘
```

### 5 Microservices

| Service | Purpose | Key Feature |
|---------|---------|------------|
| **Identity** | Auth & profiles | JWT generation with private key |
| **Product** | Catalog & stock | Inventory management |
| **Order** | Shopping & orders | Cart + order lifecycle |
| **Payment** | Payments | Transaction processing |
| **Notification** | Emails | Event-driven alerts |

### Communication Patterns

- **HTTP**: Order calls Product (immediate response needed)
- **Events**: Product publishes StockChanged (other services react)

### Key Principles

- ✅ One class = one responsibility
- ✅ Dependencies via constructor
- ✅ Entities contain business rules
- ✅ Services orchestrate use cases
- ✅ Controllers handle HTTP only
- ✅ DTOs for request/response

---

## 🚀 Common Workflows

### Adding a New Feature

```
1. Read ARCHITECTURE.md (understand patterns)
2. Find similar existing code (study it)
3. Create entity (if needed) in domain/entity/
4. Create service in application/service/
5. Create DTO in application/dto/
6. Create controller in infra/web/
7. Publish events if needed
8. Follow DEVELOPMENT.md style
9. Check code review checklist
```

### Fixing a Bug

```
1. Find the code location (grep or file browser)
2. Read DEVELOPMENT.md (understand patterns)
3. Trace through layers (infra → application → domain)
4. Check REFERENCE.md (common pitfalls)
5. Make minimal change
6. Verify fix doesn't break other code
7. Follow DEVELOPMENT.md style
```

### Inter-Service Communication

```
1. Read ARCHITECTURE.md (Communication Patterns)
2. Decide: HTTP (synchronous) or Event (async)?
3. If HTTP: See MICROSERVICES.md for endpoint
4. If Event: Create event class, publish, listen
5. See examples in ARCHITECTURE.md and MICROSERVICES.md
```

---

## 📋 File Organization Reference

Each microservice follows this structure:

```
microservice-name/
├── src/main/java/com/domain/
│   ├── domain/                    ← Business logic
│   │   ├── entity/
│   │   ├── service/
│   │   └── repository/            (interfaces only)
│   ├── application/               ← Use cases
│   │   ├── service/
│   │   ├── dto/
│   │   ├── exception/
│   │   ├── event/
│   │   └── handler/
│   ├── infra/                     ← Framework/External
│   │   ├── web/                   (controllers)
│   │   └── persistence/           (JPA repos)
│   ├── common/                    ← Cross-cutting
│   │   ├── handler/
│   │   ├── dto/
│   │   └── security/
│   └── config/
└── resources/
    ├── application.yaml
    ├── application-dev.yaml
    └── db/
```

See ARCHITECTURE.md for detailed layer explanations.

---

## 🔍 Documentation Features

### Each Guide Includes:

✅ **Clear examples** - Code snippets you can reference  
✅ **Visual diagrams** - ASCII diagrams for concepts  
✅ **Quick reference tables** - Fast lookups  
✅ **Common patterns** - Ready-to-use templates  
✅ **Anti-patterns** - What NOT to do  
✅ **Real service examples** - References to actual code  

---

## 💡 Pro Tips

1. **Search within guides**: Use grep or Ctrl+F to search keywords
2. **Copy code snippets**: Most examples in REFERENCE.md are ready to use
3. **Cross-reference**: Documents link to each other
4. **Keep bookmarks**: Bookmark frequently used sections
5. **Read carefully**: Small details matter (annotations, naming, etc)

---

## 🤖 AI Agent Capabilities

When working with this codebase, you should be able to:

**Code Generation**
- ✅ Generate controllers, services, entities, DTOs
- ✅ Create repositories and custom queries
- ✅ Implement event handling
- ✅ **Write comprehensive unit tests** (see TESTING.md)

**Modifications**
- ✅ Add endpoints to controllers
- ✅ Extend services with new use cases
- ✅ Update entities with new fields
- ✅ Integrate event publishing

**Architecture**
- ✅ Follow clean architecture patterns
- ✅ Apply DDD principles
- ✅ Design microservice communication
- ✅ Implement security

**Quality**
- ✅ Code review and analysis
- ✅ Identify violations and anti-patterns
- ✅ Suggest improvements
- ✅ **Write and review unit tests**

---

## 📞 When Documentation Doesn't Answer

If you need help:

1. **Search for similar code** in the microservices
2. **Read the actual implementation** of the pattern you need
3. **Cross-reference sections** in different guides
4. **Check your IDE** for method signatures and comments

---

## 📊 Documentation Stats

- **Total Lines**: 5,900+
- **Guides**: 8
- **Code Examples**: 150+
- **Tables & Diagrams**: 70+
- **Estimated Reading Time**: 2 hours for full overview, 10 min for specific tasks

---

## 🎯 Success Checklist

Before making changes, you should be able to answer:

- [ ] What are the 4 layers of architecture? (See ARCHITECTURE.md)
- [ ] How do services communicate? (See OVERVIEW.md & ARCHITECTURE.md)
- [ ] What's the naming convention for classes? (See DEVELOPMENT.md)
- [ ] Where do I put business logic? (See ARCHITECTURE.md)
- [ ] How do I handle exceptions? (See DEVELOPMENT.md & ARCHITECTURE.md)
- [ ] What's in a DTO? (See ARCHITECTURE.md)
- [ ] How do I add an endpoint? (See MICROSERVICES.md & DEVELOPMENT.md)
- [ ] What are the 5 services? (See OVERVIEW.md)
- [ ] How do I write unit tests? (See TESTING.md) ⭐ NEW
- [ ] What's the AAA test pattern? (See TESTING.md) ⭐ NEW

**If yes to all**: You're ready to code!

---

## 🚀 Next Steps

1. **Explore**: Browse the code in the microservices
2. **Understand**: Read sections relevant to your task
3. **Follow**: Use patterns from existing code
4. **Review**: Check code review checklist before submitting
5. **Test**: Write unit tests following TESTING.md
6. **Improve**: Iteratively refine based on feedback

---

**Status**: ✅ Complete (8 guides with TESTING.md + INTEGRATION_TESTING.md)  
**Last Updated**: April 2026  
**Total Documentation**: 5,900+ lines across 8 guides

**Start with AGENT.md (this file) then pick your task!** 🎯
