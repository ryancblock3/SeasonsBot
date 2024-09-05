FROM openjdk:17-jdk-slim

# Install required packages
RUN apt-get update && apt-get install -y \
    fontconfig \
    libfreetype6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY *.jar app.jar
COPY logos /app/logos
ENV LOGO_PATH=/app/logos
ENTRYPOINT ["java","-jar","/app/app.jar"]