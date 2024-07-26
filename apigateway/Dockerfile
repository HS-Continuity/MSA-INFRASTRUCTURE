FROM openjdk:17-jdk-slim
ADD /build/libs/*.jar configserver.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/configserver.jar"]