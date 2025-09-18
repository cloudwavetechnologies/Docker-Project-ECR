FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/myapp-jar-with-dependencies.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
