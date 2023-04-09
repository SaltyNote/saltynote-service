# SaltyNote Service

[![Java CI with Maven](https://github.com/SaltyNote/saltynote-service/actions/workflows/maven.yml/badge.svg)](https://github.com/SaltyNote/saltynote-service/actions/workflows/maven.yml)
[![CodeFactor](https://www.codefactor.io/repository/github/saltynote/saltynote-service/badge)](https://www.codefactor.io/repository/github/saltynote/saltynote-service)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/406f90655dd44f12a6107bbfbf4c2c45)](https://app.codacy.com/gh/SaltyNote/saltynote-service/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)


## Overview

This is the backend service for [saltynote](https://saltynote.com). It
uses [JWT](https://auth0.com/docs/tokens/json-web-tokens) for authentication. As high-level, this service provides APIs
for:

1.  User (signup, login, token refresh, token cleanup, password reset)
2.  Note (create, update, fetch and delete)

Unit Test Coverage report: https://saltynote.github.io/saltynote-service/jacoco/

## Get Started

This is a standard spring boot project with Maven, so you can use generic maven command to run it. While the simplest &
quickest way is to run [`./start.sh`](./start.sh).

Swagger UI will be available at http://localhost:8888/swagger-ui.html

### Prerequisite

1.  Java 17 (due to Spring Boot V3)
2.  Docker (docker-compose) for development database
3.  IDE ([Eclipse](https://www.eclipse.org/) or [Intellij](https://www.jetbrains.com/idea/))

### Configuration

1.  The service relies on database to store `user` and `note` information. In development env, you can run `docker-compose up`
   to start mariadb locally(*add `-d` if you want start it as “detached” mode*). 
   > *No need to manually patch the DB schemas, during service startup, these [DB migration scripts](src/main/resources/db/migration) will be executed automatically by [flyway](https://github.com/flyway/flyway).*
2.  This service also need smtp service to send email(*Note: this is optional now, if not setup, the email payload will
   be logged([code](src/main/java/com/saltynote/service/event/EmailEventListener.java#L50-L55)).*). 

## License

saltynote service is licensed under MIT - [LICENSE](./LICENSE)

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FSaltyNote%2Fsaltynote-service.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2FSaltyNote%2Fsaltynote-service?ref=badge_large)
