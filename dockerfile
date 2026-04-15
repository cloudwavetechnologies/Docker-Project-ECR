FROM openjdk:17-jdk-slim
WORKDIR /app
# Copy the JAR file built by Maven into the Docker image. Make sure to update the path if your JAR file is located elsewhere.
COPY target/myapp-jar-with-dependencies.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
# To build the Docker image, run: