## Spring Boot File Upload / Download Rest API Example

**Tutorial**: [Uploading an Downloading files with Spring Boot](https://www.callicoder.com/spring-boot-file-upload-download-rest-api-example/)

> **This branch demonstrates how to store the files in MySQL database.**

## Steps to Setup

**1. Clone the repository** 

```bash
git clone https://github.com/callicoder/spring-boot-file-upload-download-rest-api-example.git
```

**2. Configure MySQL database**

Create a MySQL database named `file_demo`, and change the username and password in `src/main/resources/application.properties` as per your MySQL
installation -

```properties
spring.datasource.username= <YOUR MYSQL USERNAME>
spring.datasource.password= <YOUR MYSQL PASSWORD>
```

**3. Run the app using maven**

```bash
cd spring-boot-file-upload-download-rest-api-example
mvn spring-boot:run
```

That's it! The application can be accessed at `http://localhost:8080`.

You may also package the application in the form of a jar and then run the jar file like so -

```bash
mvn clean package
java -jar target/file-demo-0.0.1-SNAPSHOT.jar
```