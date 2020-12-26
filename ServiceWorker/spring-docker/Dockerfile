FROM openjdk:alpine
ADD ServiceCSVs.jar app.jar
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=docker", "/app.jar"]