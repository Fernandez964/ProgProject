# QuickChat - User Registration, Authentication & Messaging App

A Java-based console application for user registration, input validation, authentication and
message handling, built with Maven and tested using JUnit 5.

---

## 🚀 Features

* **Username Validation**: Ensures usernames contain an underscore (`_`) and are 5 characters or fewer.
* **Password Complexity Checking**: Validates that passwords are at least 8 characters long and contain at least one uppercase letter, digit, and special character.
* **Phone Number Formatting**: Validates South African mobile numbers in international (`+27...`) or local (`0...`) format, normalising them to `+27`. The rule lives in `CellNumber` and is shared by registration and message recipients.
* **User Authentication**: Verifies login credentials against saved registration data, with three attempts allowed.
* **Message Handling**: Unique 10-digit message IDs, recipient and content validation, message hashing, and send / store / disregard options.
* **JSON Persistence**: Messages are written to `quickchat-messages.json` and read back on the next run, using a hand written JSON writer and parser (no external library).
* **Unit Testing**: 60 JUnit 5 tests covering the login, messaging, cell number, JSON and message store classes.

---

## 🛠️ Project Structure

```text
├── pom.xml
└── src
    ├── main
    │   └── java
    │       ├── CellNumber.java    # Shared cell number rule and normalisation
    │       ├── Json.java          # Hand written JSON writer and parser
    │       ├── Login.java         # Registration, validation and login
    │       ├── Main.java          # Console entry point
    │       ├── Message.java       # Message creation, validation and hashing
    │       └── MessageStore.java  # Keeps, prints and persists the messages
    └── test
        └── java
            ├── CellNumberTest.java
            ├── JsonTest.java
            ├── LoginTest.java
            ├── MessageStoreTest.java
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
| Recipient cell | International (`+27` followed by 9 digits) or local (`0` followed by 9 digits), stored as `+27` |
| Message content | 250 characters or less |
| Message hash | `first two ID digits : message number : FIRST AND LAST word` |

---

## 💾 Message File

| Menu option | Action |
| --- | --- |
| 1 | Send messages |
| 2 | Show the messages that are kept in the store |
| 3 | Write the messages to `quickchat-messages.json` |
| 4 | Read the messages back from `quickchat-messages.json` |
| 5 | Quit |

The file holds a JSON array with one object per message:

```json
[
  {
    "messageID": "1234567890",
    "messageNumber": 1,
    "recipientCell": "+27838968976",
    "messageContent": "Hello there",
    "messageHash": "12:1:HELLOTHERE",
    "status": "Sent"
  }
]
```

---
