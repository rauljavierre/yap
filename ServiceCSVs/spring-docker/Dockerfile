FROM openjdk:alpine
ADD YapShortener.jar app.jar
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=docker", "/app.jar"]