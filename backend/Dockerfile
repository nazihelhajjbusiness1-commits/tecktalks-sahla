# --- Stage 1: build the jar with Maven ---
# We use a JDK image here because compiling Java needs the full JDK + Maven.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy only what's needed to resolve dependencies first, so Docker can cache
# this layer and skip re-downloading them when only source code changes.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Now copy the actual source and build the jar.
COPY src src
RUN ./mvnw package -DskipTests -B

# --- Stage 2: run the jar with just a JRE ---
# The JDK/Maven are not needed to run a built jar, so the final image is
# much smaller and has a lighter attack surface.
FROM eclipse-temurin:21-jre AS run
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
