FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} finder-1.0-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/finder-1.0-SNAPSHOT.jar"]