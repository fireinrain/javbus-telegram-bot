#FROM adoptopenjdk/openjdk11:jdk-11.0.16.1_1
FROM adoptopenjdk/openjdk11:ubuntu-jre-nightly

ADD target/javbus-tg-bot-jar-with-dependencies.jar /app/app.jar
ADD javbus-tg-bot.db /app/javbus-tg-bot.db

#会在当前目录下生成db文件ls
ENTRYPOINT ["java", "-jar","/app/app.jar"]