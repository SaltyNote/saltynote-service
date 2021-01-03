# Code Structure

This is the [source code](../src/main/java/com/saltynote/service) structure. 

```plaintext
├── ServiceApplication.java   # Spring Boot Entry.
├── aop                       # So far, aop is only used for controller calling monitor.
├── component                 # Non-serivce spring component, most can be put in service package.
├── controller                # Controllers that expose the endpoints, when wanting to create a new api/feature, 
|                             # This is the first package we should touch.
├── domain                    # These are pojos used for internal/extenal data transfer.
├── entity                    # These are pojos that maps to database tables.
├── event                     # Spring ApplicationEvent types and handlers. Now it is used for new user welcome email sending. 
├── exception                 # Custom exception classes.
├── filter                    # Spring filter, which can pre/post process requests/responses. Now it is used to attach custom http header to response.
├── repository                # Spring Data respository interfaces for database tables.
├── schedule                  # Custom tasks, most for offline processing. e.g. database cleanup.
├── security                  # Spring security configurations. JWT is configured inside this package.
├── service                   # Just a service layer.
├── swagger                   # Configuration for Swagger UI. In most cases, we will not touch this.
└── utils                     # Static utility classes.
```

