#java version 17
FROM docker.arvancloud.ir/openjdk

#Working directory
WORKDIR /task-management-service

#copy my app jar file to the container
COPY build/libs/task-management-service-1.0-SNAPSHOT.jar task-management-service.jar

# application will run on this port
EXPOSE 9090

# Run the application
ENTRYPOINT ["java", "-jar", "task-management-service.jar"]