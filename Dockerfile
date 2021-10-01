FROM openjdk:11-jdk-slim as java_build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM node:16-alpine AS node_build
WORKDIR /app
COPY src/main/resources/assets/package.json .
COPY src/main/resources/assets/package-lock.json .
RUN npm install
COPY src/main/resources/assets/src ./src
COPY src/main/resources/assets/webpack.common.js .
COPY src/main/resources/assets/webpack.prod.js .
RUN npm run build:prod

FROM openjdk:11-jdk-slim
ARG DEPENDENCY=/app/target/dependency
COPY --from=java_build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=java_build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=java_build ${DEPENDENCY}/BOOT-INF/classes /app
COPY --from=node_build /app/build /app/static/assets
ENTRYPOINT ["java", "-cp", "app:app/lib/*", "io.plyschik.springbootblog.SpringBootBlogApplication"]
