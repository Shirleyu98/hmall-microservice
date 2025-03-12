FROM openjdk:11.0-jre-buster

WORKDIR /app

COPY target/hm-gateway.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
