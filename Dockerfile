FROM openjdk:21

WORKDIR /app

COPY target/Shopping_Cart-0.0.1-SNAPSHOT.jar /app/Shopping_Cart-0.0.1-SNAPSHOT.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "Shopping_Cart-0.0.1-SNAPSHOT.jar"]

