# Use a slim OpenJDK base image
FROM openjdk:11-jdk-slim

# Maintainer label
LABEL maintainer="nabeel.amd93@gmail.com"

# Create a volume for temporary files
VOLUME /tmp

# Argument to pass the JAR file from build context
ARG JAR_FILE=target/*.jar

# Copy the JAR file into the container
COPY ${JAR_FILE} app.jar

# Expose application port
EXPOSE 9096

# Run Spring Boot application
ENTRYPOINT ["sh", "-c", "java -jar /app.jar"]
