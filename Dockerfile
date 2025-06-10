FROM openjdk:21-jdk
ADD build/libs/checkers-0.0.1-SNAPSHOT.jar checkers-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "--enable-preview", "-jar", "checkers-0.0.1-SNAPSHOT.jar"]