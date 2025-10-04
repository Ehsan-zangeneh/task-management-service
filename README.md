
# Task Management Service
This is a reactive project for task management and providing  APIs for<br/>
CRUD operations on tasks.<br/>
For executing this app, please make sure that the followings ports are free on your machine.<br/>
- 9090: task-manager application
- 8080: adminer
- 5432: PostgreSQL



### tech stack
 - Java 17
 - gradle 
 - Spring boot Webflux
 - Junit 5 & Testcontainer
 - Docker
 - PostgreSQL

<br/>


### Instalation

<b style="color:red; font-size:13px">Prerequisite:</b> <b style="font-size:13px">Docker must be running on your environment</b><br/>

1) Clone the project from github:
<i><b>git clone https://github.com/Ehsan-zangeneh/task-management-service.git </i></b><br/>
2) create a Docker network named "application-network": <i><b>docker network create application-network</i></b>
3) navigate to the project root path
4) build the project: <i><b>gradle clean build </i></b> [-x test]
5) create docker image: <i><b>docker build -t task-manager . </i></b> (don't forget the dot)
6) run docker-compose: <i><b>docker compose up -d</i></b>


<b style="font-size:14px"> On Windows</b><br/>
you can simply run the .bat file: navigate to the project folder and run <i><b>.\run-app-on-windows.bat</i></b><br/>


### Access
 the swagger ui: http://localhost:9090/swagger-ui.html <br/>

 <b style="font-size:14px"> Optional</b><br/>
you can use adminer to connect to the database: http://localhost:8080/ with the following credentials:<br/>
 system :  <i>PostgreSQL</i><br/>
 username: <i>ehsan</i><br/>
 password: <i>ehsan</i><br/>
 database: <i>task_db</i>
<br/>