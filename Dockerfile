FROM adoptopenjdk/openjdk11:jdk-11.0.16.1_1

ADD target/javbus-tg-bot-jar-with-dependencies.jar /app/app.jar

#会在当前目录下生成db文件
ENTRYPOINT ["java", "-jar","app.jar"]