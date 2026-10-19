# QuickChat - User Registration, Authentication & Messaging App

A Java-based console application for user registration, input validation, authentication and
message handling, built with Maven and tested using JUnit 5.

---

## 🚀 Features

* **Username Validation**: Ensures usernames contain an underscore (`_`) and are 5 characters or fewer.
* **Password Complexity Checking**: Validates that passwords are at least 8 characters long and contain at least one uppercase letter, digit, and special character.
* **Phone Number Formatting**: Validates South African mobile numbers using international format (`+27` followed by 9 digits). The rule lives in `CellNumber` and is shared by registration and message recipients.
* **User Authentication**: Verifies login credentials against saved registration data, with three attempts allowed.
* **Message Handling**: Unique 10-digit message IDs, recipient and content validation, message hashing, and send / store / disregard options.
* **Unit Testing**: 29 JUnit 5 tests covering the login and messaging classes.

---

## 🛠️ Project Structure

```text
├── pom.xml
└── src
    ├── main
    │   └── java
    │       ├── CellNumber.java   # Shared international cell number rule
    │       ├── Login.java        # Registration, validation and login
    │       ├── Main.java         # Console entry point
    │       └── Message.java      # Message creation, validation and hashing
    └── test
        └── java
            ├── LoginTest.java
            └── MessageTest.java
```

---

## ▶️ Running the Application

```bash
mvn clean package
java -jar target/quickchat-1.0-SNAPSHOT.jar
```

## 🧪 Running the Tests

```bash
mvn test
```

---

## 💬 Message Rules

| Rule | Requirement |
| --- | --- |
| Message ID | 10 digits, unique per message |
| Recipient cell | International format, `+27` followed by 9 digits |
| Message content | Less than 250 characters |
| Message hash | `first two ID digits : message number : FIRST AND LAST word` |

---
