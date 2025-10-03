
# Task Management Service
This is a reactive project for task management and providing  APIs for<br/>
CRUD operations on tasks.<br/>
For executing this app, please make sure ports [9090, 8080, 5432] are free on your machine.

### tech stack
 - Java 17
 - gradle 
 - Spring boot Webflux
 - Junit 5 & Test container(for postgreSQL)
 - Docker
 - PostgreSQL



### Instalation

first clone the project from github <br/>
<i><b>git clone https://github.com/Ehsan-zangeneh/task-management-service.git </i></b><br/>


on the <b>Windows OS</b> you can simply run the .bat file:<br/>
navigate to the project folder and run:
<i><b>.\run-app-on-windows.bat</i></b><br/>

you need docker be running on your environment. Also, a docker network named<br/>
"application-network" is required.
1) build the project: <i><b>gradle clean build </i></b> [-x test]
2) create docker image: <i><b>docker build -t task-manager . </i></b> (don't forget the dot)
3) run docker-compose: <i><b>docker compose up -d</i></b>

### Access
 the swagger ui: http://localhost:9090/swagger-ui.html <br/>
 adminer to connect to the database: http://localhost:8080/ <br/>
 system :  <i>PostgreSQL<i><br/>
 username: <i>ehsan</i><br/>
 password: <i>ehsan</i><br/>
 database: <i>task_db</i>
