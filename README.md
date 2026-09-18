# User Registration & Authentication App

A Java-based console application for user registration, input validation, and authentication, built with Maven and tested using JUnit 5.

---

## 🚀 Features

* **Username Validation**: Ensures usernames contain an underscore (`_`) and are 5 characters or fewer.
* **Password Complexity Checking**: Validates that passwords are at least 8 characters long and contain at least one uppercase letter, digit, and special character.
* **Phone Number Formatting**: Validates South African mobile numbers using international format (`+27` followed by 9 digits).
* **User Authentication**: Verifies login credentials against saved registration data.
* **Unit Testing**: Full test coverage using JUnit 5.

---

## 🛠️ Project Structure

```text
├── pom.xml
└── src
    ├── main
    │   └── java
    │       ├── Login.java
    │       └── Main.java
    └── test
        └── java
            └── LoginTest.java
