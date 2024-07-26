FROM openjdk:17-jdk-slim
ADD /build/libs/*.jar apigateway.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/apigateway.jar"]