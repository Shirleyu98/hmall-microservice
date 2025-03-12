FROM openjdk:11.0-jre-buster

WORKDIR /app

COPY target/cart-service.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
