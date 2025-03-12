FROM openjdk:11.0-jre-buster

WORKDIR /app

COPY target/trade-service.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
