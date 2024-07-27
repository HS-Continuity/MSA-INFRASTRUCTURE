FROM openjdk:17-jdk-slim
ADD /build/libs/*.jar servicediscovery.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/servicediscovery.jar"]