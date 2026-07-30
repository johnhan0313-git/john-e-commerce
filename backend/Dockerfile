# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

ARG MAVEN_MIRROR_URL=https://maven.aliyun.com/repository/public
ARG HTTP_PROXY=
ARG HTTPS_PROXY=
ARG NO_PROXY=localhost,127.0.0.1

ENV HTTP_PROXY=$HTTP_PROXY HTTPS_PROXY=$HTTPS_PROXY http_proxy=$HTTP_PROXY https_proxy=$HTTPS_PROXY NO_PROXY=$NO_PROXY no_proxy=$NO_PROXY

COPY pom.xml .
RUN mkdir -p /root/.m2 \
  && printf '%s\n' \
    '<settings>' \
    '  <mirrors><mirror>' \
    '    <id>aliyun</id><mirrorOf>*</mirrorOf>' \
    "    <url>${MAVEN_MIRROR_URL}</url>" \
    '  </mirror></mirrors>' \
    '</settings>' > /root/.m2/settings.xml \
  && mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package \
  && mv target/john-ecommerce-*.jar /app/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/app.jar ./app.jar
EXPOSE 8020
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
