FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} solepli_dev.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod" ,"/solepli_dev.jar"]
