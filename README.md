![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

# Design Patterns Spring Boot Lab

A hands-on repository demonstrating implementation of classic design patterns using Spring Boot, with best practices code structure, 
comprehensive testing, and REST APIs.

## Table of Contents
- [Overview](#overview)
- [Implemented Patterns](#implemented-patterns)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Testing](#testing)
- [License](#license)

## Overview

Each pattern is implemented as a standalone Spring Boot module with:
- Real-world use cases
- RESTful API endpoints
- Unit and integration tests
- Proper exception handling
- Input validation

## Prerequisites

- Java 17 or higher
- Maven 3.9+
- Your favorite IDE (IntelliJ IDEA recommended)

## Implemented Patterns

### Behavioral Patterns

#### 1. Strategy Pattern
**Module:** `behavioral-patterns/strategy-pattern/`  
**Status:** ✅ Complete  
**Use Case:** Payment Processing System

A flexible payment processing system supporting multiple payment methods. Demonstrates how the Strategy pattern enables runtime algorithm selection without tight coupling.

**Supported Payment Methods:**
- Credit Card (2.9% + €0.30 fee)
- PayPal (3.4% + €0.35 fee)
- Cryptocurrency (1.0% + dynamic gas fees)
- Bank Transfer (no fee)

**API Endpoint:** `POST /api/payments/process`

**What You'll Learn:**
- Runtime strategy selection based on input
- Strategy-specific validation and processing
- Fee calculation polymorphism
- Enum-to-implementation mapping with Spring

**Tech Stack:** Spring Boot 3.4.1, Java 17, JUnit 5, AssertJ

--- 

### Creational Patterns

Coming soon...

### Structural Patterns

Coming soon...

--- 

```
design-patterns-spring-boot-lab/
├── behavioral-patterns/
│   └── strategy-pattern/
├── creational-patterns/
│   └── factory-pattern/
└── structural-patterns/
    └── adapter-pattern/
```

## Running a Pattern

Navigate to the pattern module and run:

```bash
cd behavioral-patterns/strategy-pattern
mvn spring-boot:run
```

Each module runs independently on its own port (check `application.properties`).

## Testing

Each pattern includes:
- Unit tests for each implementation
- Service layer tests
- Integration tests for REST endpoints

Run tests with:
```bash
mvn test
```

---

**Note:** This is a learning repository focused on demonstrating design patterns with clean, maintainable code structure.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

Michael Theophanopoulos

## Acknowledgments

- Design Patterns: Elements of Reusable Object-Oriented Software (Gang of Four)
- Spring Boot Documentation
