# QuickChat - Programming 1A POE Part 3

A Java-based console application for user registration, input validation, authentication, message handling, JSON persistence, and stored message management, built with Maven and tested using JUnit 5.

---

## Features

* **Username Validation**: Ensures usernames contain an underscore (`_`) and are 5 characters or fewer.
* **Password Complexity Checking**: Validates that passwords are at least 8 characters long and contain at least one uppercase letter, digit, and special character.
* **Phone Number Formatting**: Validates South African mobile numbers in international (`+27...`) or local (`0...`) format, normalising them to `+27`.
* **User Authentication**: Verifies login credentials against saved registration data, with three attempts allowed.
* **Message Handling & Dynamic Arrays**: Maintains dynamic arrays for Sent Messages, Disregarded Messages, Stored Messages (loaded from JSON), Message Hashes, and Message IDs without hard-coding message data.
* **Stored Messages Menu**:
  * **a)** Display sender and recipient of all stored messages
  * **b)** Display the longest stored message
  * **c)** Search for a message by Message ID
  * **d)** Search for all messages sent or stored for a particular recipient
  * **e)** Delete a message using its Message Hash
  * **f)** Display report of stored/sent messages
* **JSON Persistence**: Reads and writes messages to `quickchat-messages.json` using the built-in `Json` parser/serializer.
* **Automated Unit Testing**: JUnit 5 test suite executed automatically via GitHub Actions (`.github/workflows/TestJava.yml`).

---

## Project Structure

```text
├── .github/workflows/TestJava.yml
├── pom.xml
├── quickchat-messages.json
├── README.md
└── src
    ├── main
    │   └── java
    │       ├── CellNumber.java
    │       ├── Json.java
    │       ├── Login.java
    │       ├── Main.java
    │       ├── Message.java
    │       └── MessageStore.java
    └── test
        └── java
            ├── CellNumberTest.java
            ├── JsonTest.java
            ├── LoginTest.java
            ├── MessageStoreTest.java
            ├── MessageTest.java
            └── Part3Test.java
```

---

## Running & Testing

```bash
mvn test
mvn clean package
java -jar target/quickchat-1.0-SNAPSHOT.jar
```
