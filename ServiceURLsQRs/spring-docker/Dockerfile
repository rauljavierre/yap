FROM openjdk:alpine
ADD ServiceURLsQRs.jar app.jar
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=docker", "/app.jar"]