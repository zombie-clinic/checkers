### Run app locally

Run with Maven

`mvn spring-boot:run`

### API

Docs could be found under [localhost:8080/swagger-ui.html](localhost:8080/swagger-ui.html)

### Database

Test database is accessible under [localhost:8080/h2-console](localhost:8080/h2-console)

### Docker

To build and run Docker image

Create executable jar
`mvn clean package`

Build image
`docker build -t checkers-backend .`

Run container
`docker run -p 8080:8080 checkers-backend`
