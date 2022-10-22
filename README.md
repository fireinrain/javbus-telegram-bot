### telegram bot for javbus

部署一个Javbus Film 搜索机器人, 柯学涩涩。

运行环境:

```bash
JDK 1.8
```

远程使用说明：

```bash
1. 找个国外的主机
2. git clone https://github.com/fireinrain/javbus-telegram-bot.git
3. cd 到仓库目录, 执行 `mvn clean && mvn package`
4. cd target
5. 将javbus-tg-bot-jar-with-dependencies.jar包上传到远程机器
6. 设置环境变量 `vim /etc/profile`  将`export JAVBUS_BOT_NAME=xxxx` `export JAVBUS_BOT_TOKEN=xxxx` 加入到行尾
7. `source /etc/profile`
7. 启动: nohup java -jar javbus-tg-bot-jar-with-dependencies.jar >/dev/null 2>&1 &
8. 完成部署
```

本地开发:

```bash
本项目如果要在本地部署跑起来，需要现在 tg上面 找到botfather，申请bot的BOT_TOKEN

拿到bot token 后 可以替换setting.properties中的相同名字的配置，并设置Bot name

然后运行TelegramBotApp 作为入口类即可

注意: 因为telegram 在大陆是被屏蔽的，所以在本地使用的话 需要开启代理，并将代理的端口设置到
setting.properties 中。

```

最后:
如果本项目对您有帮助的话，可以给人家一个star不，万分感谢