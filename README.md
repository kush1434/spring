# Runtime links

Backend UI

Use login with .env setup user to manage and restore data.

- Runtime link: https://spring.opencodingsociety.com/

API access

Validate system is up by testing an endpoint

- Jokes endpoint: https://spring.opencodingsociety.com/api/jokes/

Examine JWT Login

Review cookies after accessing a page that needs them (ie Groups)

- JWT Login: https://pages.opencodingsociety.com/login

## Backend UI purpose

This Backend UI is to manage adminstrative functions like reseting passwords and managing database content: CRUD, Backup, and Restore.

- Thymeleaf UI should be visual and practical
- Home page is organized with Bootstrap menu and cards
- Most menus and operations are dedicated to Tables
- Some sample menus exist to reference basic capability

## Backend Primary purpose

The site is build on Springboot.  The project is primarly used to store and retrieve data through APIs.  The site has JWT authorization and implements security.  In optimal deployed form the data would be served through a professional database, it supports SQLite for development and deployment verification.

## Getting started

- Install favorite Java JDK on machine (ie adoptopenjdk:17)
- Clone project and open in VSCode
- Java source (src/main/java/...) has Java files.  
- HTML source (src/main/resources/...) had templates and supporting files.  Find index.html as this file is launched by defaul in Spring.

### Configuration Requirements

- Create custom `.env` file to setup default user passwords to satisfy code in Person.java.  Students of OCS should leave users as default until competency is obtained.

```java
final String adminPassword = dotenv.get("ADMIN_PASSWORD");
final String defaultPassword = dotenv.get("DEFAULT_PASSWORD");
```

- Modify `application.properties` ports to be unique for your indivdual project.

```text
server.port=8585
socket.port=8589
```

## Run Project

- Play or click entry point is Main.java, look for Run option in code.  This eanbles Springboot to build and load.
- Load loopback:port in browser (http://127.0.0.1:8585/)
- Login to ADMIN (toby) user using ADMIN_PASSWORD, examing menus and data
- Try API endpoint: http://127.0.0.1:8585/api/jokes/

## IDE management

- Extension Pack for Java from the Marketplace, you may need to close are restart VSCode
- A ".gitignore" can teach a Developer a lot about Java runtime.  A target directory is created when you press play button, byte code is generated and files are moved into this location.
- "pom.xml" file can teach you a lot about Java dependencies.  This is similar to "requirements.txt" file in Python.  It manages packages and dependencies.

## .env files

- In order to run this project locally, a .env file should be set up with the appropriate variables:
- GAMIFY_API_URL=
- GAMIFY_API_KEY= 
- ADMIN_PASSWORD=123Toby!
- DEFAULT_PASSWORD=123Qwerty!

## Person MVC

![Class Diagram](https://github.com/user-attachments/assets/26219a16-e3dc-45e3-af1c-466763957dce)

- Basically there is a rough MVCframework.
- The webpages act as the view. These pages can view details about the users, and request the controller to change details about them
- The controller is mainly "personViewController" for the backend, but other controllers include "personApiController" for the front end.
- Techincally the image is wrong, "personDetailsService" is a controller. It is used by other controllers to change the database, so it seemed more accurate to call it a part of the model, rather than a controller.
- The person.java is the pojo (object) that is used for the database schema.
