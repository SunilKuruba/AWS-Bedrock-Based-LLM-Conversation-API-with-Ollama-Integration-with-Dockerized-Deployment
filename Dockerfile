# Use OpenJDK base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /llm

# Copy the JAR file into the container
COPY target/scala-2.13/LLM-hw3-assembly-0.1.0-SNAPSHOT.jar llm.jar

# Expose the necessary ports
EXPOSE 8080
EXPOSE 11434

# Entry point to start Ollama and your application together
CMD ["bash", "-c", "ollama start & java -jar llm.jar"]
