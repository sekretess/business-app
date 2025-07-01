FROM gcr.io/distroless/java21-debian12
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
VOLUME /data/h2
ENTRYPOINT ["java", "-jar", "app.jar"]
