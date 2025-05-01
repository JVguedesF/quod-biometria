# Build stage
FROM maven:3.9.5-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies
RUN mvn dependency:go-offline -B
COPY src ./src
# Build application
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install necessary dependencies for OpenCV
RUN apk add --no-cache libstdc++ libgcc

# Copy JAR file from build stage
COPY --from=build /app/target/*.jar app.jar

# Create directory for application logs
RUN mkdir -p /app/logs

# Environment variables
ENV JAVA_OPTS="-Xms512m -Xmx1g"
ENV SPRING_PROFILES_ACTIVE="prod"

# Expose the application port
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]