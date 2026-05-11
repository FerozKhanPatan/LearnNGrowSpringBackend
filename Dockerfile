FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy Maven wrapper files first
COPY mvnw* ./
COPY .mvn .mvn

# Copy the rest of the application
COPY pom.xml .
COPY src ./src

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]