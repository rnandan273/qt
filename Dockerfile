FROM openjdk:8-alpine

COPY target/uberjar/qt.jar /qt/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/qt/app.jar"]
