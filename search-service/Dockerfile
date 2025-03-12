FROM openjdk:11.0-jre-buster

WORKDIR /app

COPY target/search-service.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "app.jar"]
