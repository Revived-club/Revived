FROM ghcr.io/graalvm/jdk-community:24

WORKDIR /app

COPY build/libs/queue-*-all.jar queue.jar

ENTRYPOINT ["java", "-jar", "queue.jar"]