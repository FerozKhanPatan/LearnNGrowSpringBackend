FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy Maven wrapper files first
COPY mvnw.cmd ./
COPY .mvn .mvn

# Copy the rest of the application
COPY pom.xml .
COPY src ./src

# Create Linux mvnw from Windows mvnw.cmd
RUN echo '#!/bin/sh' > mvnw && \
    echo 'exec "$0.cmd" "$@"' >> mvnw && \
    chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]