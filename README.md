In order to run the backend we have created a docker-compose.yml. You can run it by running
```docker compose -f localDocker/docker-compose.yml up```
This will start a PostgreSQL database and the backend. You can also use pgAdmin at ```http://localhost:8079/```

The database runs on port 8080, you can access it via ```http://localhost:8080/```
Our API Specification provides all the information you need regarding the endpoints and what bodies they need.