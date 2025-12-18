FROM eclipse-temurin:21-jdk-jammy

# Tools cần cho profiler
RUN apt-get update && apt-get install -y \
    procps \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Cài async-profiler
ENV AP_VERSION=4.2
RUN curl -L -o /tmp/ap.tar.gz \
    https://github.com/async-profiler/async-profiler/releases/download/v${AP_VERSION}/async-profiler-${AP_VERSION}-linux-x64.tar.gz \
    && tar -xzf /tmp/ap.tar.gz -C /opt \
    && ln -s /opt/async-profiler-${AP_VERSION}-linux-x64 /opt/async-profiler \
    && rm /tmp/ap.tar.gz

ENV PATH="/opt/async-profiler:$PATH"

WORKDIR /app
COPY target/bottleneck-resolve-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]ex
