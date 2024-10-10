FROM ubuntu:22.04

# Set environment variable for non-interactive apt-get install
ENV DEBIAN_FRONTEND=noninteractive

# Update package list and install OpenJDK 17
RUN apt-get update && apt-get install -y openjdk-17-jdk
# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file (make sure to build the JAR using Maven/Gradle)
COPY target/*.jar app.jar

# Expose port 8080 (Spring Boot's default port)
EXPOSE 8080

# Mountable directory for H2DB file storage
VOLUME /data/h2

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
