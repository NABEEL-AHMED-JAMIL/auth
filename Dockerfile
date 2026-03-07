# Use a slim OpenJDK runtime image
FROM openjdk:11-jre-slim

# Maintainer label
LABEL maintainer="nabeel.amd93@gmail.com"

# Argument to pass the JAR file from build context
ARG JAR_FILE=target/*.jar

# Create app directory and set it as the working directory
WORKDIR /app

# Copy the JAR file into the container (destination: /app/auth.jar)
COPY ${JAR_FILE} auth.jar

# Create a volume for temporary files
VOLUME /tmp

# Expose application port
EXPOSE 9096

# Run Spring Boot application (use a secure random seed source for faster startup)
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/auth.jar"]
