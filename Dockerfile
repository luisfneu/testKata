# Use Eclipse Temurin Java 23 (more reliable than openjdk:23)
FROM eclipse-temurin:23-jdk

# Set working directory
WORKDIR /app

# Install Maven and curl
RUN apt-get update && \
    apt-get install -y maven curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME explicitly to ensure Java 23 is used
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Verify Java version before building
RUN java --version && javac --version

# Copy Maven configuration files first
COPY pom.xml .

# Download dependencies (for better layer caching)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application with explicit Java 23 configuration
RUN echo "Java version in container:" && java -version && \
    echo "Maven version:" && mvn -version && \
    mvn clean package -DskipTests -Djava.version=23 -Dmaven.compiler.release=23

# Create a new stage for the runtime
FROM eclipse-temurin:23-jdk

# Set working directory
WORKDIR /app

# Set JAVA_HOME for runtime stage
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Create a non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Copy the JAR file from the build stage
COPY --from=0 /app/target/*.jar app.jar

# Change ownership of the application files
RUN chown -R spring:spring /app

# Switch to the non-root user
USER spring:spring

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/api/dungeon/health || exit 1

# Set JVM options for Java 23 with preview features
ENV JAVA_OPTS="-Xmx512m -Xms256m --enable-preview"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
