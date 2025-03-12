FROM openjdk:11.0-jre-buster
WORKDIR /app

COPY target/item-service.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
