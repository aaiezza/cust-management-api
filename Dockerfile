FROM amazoncorretto:21.0.5-alpine
VOLUME /tmp
EXPOSE 8080

# Install curl and jq
RUN apk add --no-cache curl jq

ARG JAR_FILE=target/customer-management-api-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
