# SaltyNote Service

[![Java CI with Maven](https://github.com/SaltyNote/saltynote-service/actions/workflows/maven.yml/badge.svg)](https://github.com/SaltyNote/saltynote-service/actions/workflows/maven.yml)

## Overview

This is the backend service for [saltynote](https://saltynote.com). It
uses [JWT](https://auth0.com/docs/tokens/json-web-tokens) for authentication. As high-level, this service provides APIs
for:

1.  User (signup, login, token refresh, token cleanup, password reset)
2.  Note (create, update, fetch and delete)

For more information about the endpoints, please refer the [readonly swagger ui](https://api.saltynote.com/swagger-ui/) for more details.

**Note:** Why `POST /note/id/delete`? 
> Because, I find inside Chrome Extension, the `DELETE` requests are blocked(not sure whether it is resolved now). As a workaround, `POST /note/id/delete` is used to delete a note.

![Swagger UI](./docs/images/swagger-ui.png)

## Get Started

This is a standard spring boot project with Maven, so you can use generic maven command to run it. While the simplest &
quickest way is to run [`./start.sh`](./start.sh).

### Prerequisite

1.  JDK 11
2.  Docker (docker-compose) for development database
3.  IDE ([Eclipse](https://www.eclipse.org/) or [Intellij](https://www.jetbrains.com/idea/))

### Configuration

1.  The service relies on database to store `user/note` information. In development env, you can run `docker-compose up`
   to start mariadb locally(*add `-d` if you want start it as “detached” mode*).
2.  This service also need smtp service to send email(*Note: this is optional now, if not setup, the email payload will
   be logged([code](src/main/java/com/saltynote/service/event/EmailEventListener.java#L50-L55)).*). 

## License

saltynote service is licensed under MIT - [LICENSE](./LICENSE)

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FSaltyNote%2Fsaltynote-service.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2FSaltyNote%2Fsaltynote-service?ref=badge_large)
