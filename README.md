### telegram bot for javbus

使用说明：

本项目如果要在本地部署跑起来，需要现在 tg上面 找到botfather，申请bot的BOT_TOKEN
拿到bot token 后 可以替换TgBotConfig类中的 JAVBUS_BOT_NAME 和JAVBUS_BOT_TOKEN

然后再tg客户端软件 把机器人添加到channel 或者是单独的和bot交互

配置有转发选项，实现的功能是 在单独对bot进行输入时 会把处理结果发送到channel，

channel 获取channel id 目前的方式是 先把channel 设置为public 然后调用web api的接口
从返回的数据中获得这个channel的chatId，这样在转发的时候就可以使用这个chatid了

当获取chat_id 后可以把channel 设置为private， chat id 并不会变 这样我们还是可以继续进行转发




