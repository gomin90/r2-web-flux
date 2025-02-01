# Project File Structure

```
r2-web-flux/
├── .github/
│   ├── project.md
│   └── filelist.md
├── Procfile
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/gomin/r2webflux/
    │   │   ├── R2WebFluxApplication.java
    │   │   ├── config/
    │   │   │   ├── DatabaseConfig.java
    │   │   │   ├── JdbcConfig.java
    │   │   │   ├── SwaggerConfig.java
    │   │   │   └── StateMachineConfig.java
    │   │   ├── controller/
    │   │   │   └── AccountController.java
    │   │   ├── domain/
    │   │   │   └── Account.java
    │   │   ├── event/
    │   │   │   ├── AccountEvent.java
    │   │   │   └── AccountEventListener.java
    │   │   ├── repository/
    │   │   │   └── AccountRepository.java
    │   │   ├── service/
    │   │   │   └── AccountService.java
    │   │   └── state/
    │   │   │   ├── AccountState.java
    │   │   │   └── AccountStateHandler.java
    │   └── resources/
    │       ├── application.yml
    │       ├── config/
    │       │   └── r2-web-flux.json
    │       └── schema.sql
    └── test/
        └── java/com/gomin/r2webflux/
            ├── controller/
            └── service/
```
