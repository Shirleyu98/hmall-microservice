FROM openjdk:11.0-jre-buster

WORKDIR /app

COPY target/pay-service.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]
