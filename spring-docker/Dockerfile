FROM openjdk:alpine
ADD urlshortener.jar app.jar
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=docker", "/app.jar"]