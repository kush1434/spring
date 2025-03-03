## Recent Changes

[Upgrading old dependencies to newest versions.](https://github.com/nighthawkcoders/spring_portfolio/issues/12)

# [Spring Portfolio Starter](https://nighthawkcodingsociety.com/projectsearch/details/Spring%20Portfolio%20Starter)

- Runtime link: https://spring2025.nighthawkcodingsociety.com/
- JWT Login: https://nighthawkcoders.github.io/APCSA/data/login
- Jokes endpoint: https://spring.nighthawkcodingsociety.com/api/jokes/



## Visual thoughts

- Starter code should be fun and practical
- Organize with Bootstrap menu 
- Add some color and fun through VANTA Visuals (, halo, solar, net)
- Show some practical and fun links (hrefs) like Twitter, Git, Youtube
- Show student project specific links (hrefs) per page
- Show student About me pages

## Getting started

- Clone project and open in VSCode
- Verify Project Structure to use a good Java JDK (adoptopenjdk:17)
- Play or entry point is Main.java, look for Run option in code.  This eanbles Spring to load
- Java source (src/main/java/...) has Java files.  Find "controllers" path, these files enable HTTP route and HTML file relationship.
- HTML source (src/main/resources/...) had templates and supporting files.  Find index.html as this file is launched by defaul in Spring.  Other HTML files are loaded by building an "@Controller"

## IDE management

- A ".gitignore" can teach a Developer a lot about Java runtime.  A target directory is created when you press play button, byte code is generated and files are moved into this location.
- "pom.xml" file can teach you a lot about Java dependencies.  This is similar to "requirements.txt" file in Python.  It manages packages and dependencies.

## .env files
- In order to run this project locally, a .env file should be set up with the appropriate variables:
- GAMIFY_API_URL
- GAMIFY_API_KEY


## Person MVC
![Class Diagram](https://github.com/user-attachments/assets/26219a16-e3dc-45e3-af1c-466763957dce)

- Basically there is a rough MVCframework.
- The webpages act as the view. These pages can view details about the users, and request the controller to change details about them
- The controller is mainly "personViewController" for the backend, but other controllers include "personApiController" for the front end.
- Techincally the image is wrong, "personDetailsService" is a controller. It is used by other controllers to change the database, so it seemed more accurate to call it a part of the model, rather than a controller.
- The person.java is the pojo (object) that is used for the database schema.
