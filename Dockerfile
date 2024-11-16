FROM ubuntu:22.04
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y openjdk-17-jdk
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
VOLUME /data/h2
ENTRYPOINT ["java", "-jar", "app.jar"]
