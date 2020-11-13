FROM openjdk:8-alpine

COPY target/uberjar/governance.jar /governance/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/governance/app.jar"]
