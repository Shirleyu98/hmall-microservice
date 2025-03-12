FROM openjdk:11.0-jre-buster

WORKDIR /app

COPY target/user-service.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
