FROM eclipse-temurin:22-jre

WORKDIR /app

COPY ParallelPasswordCracker.jar ./ParallelPasswordCracker.jar

ENTRYPOINT ["java", "-jar", "ParallelPasswordCracker.jar"]
