FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} solpli_dev.jar
ENTRYPOINT ["java", "-jar", "/solpli_dev.jar"]
