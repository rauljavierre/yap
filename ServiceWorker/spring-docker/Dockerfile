FROM openjdk:alpine
ADD ServiceWorker.jar app.jar
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=docker", "/app.jar"]