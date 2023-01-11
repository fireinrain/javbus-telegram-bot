#FROM adoptopenjdk/openjdk11:jdk-11.0.16.1_1
FROM openjdk:18-ea-11-jdk-alpine3.15

ADD target/javbus-tg-bot-jar-with-dependencies.jar /app/app.jar
ADD javbus-tg-bot.db /app/app.jar

#会在当前目录下生成db文件ls
ENTRYPOINT ["java", "-jar","/app/app.jar"]