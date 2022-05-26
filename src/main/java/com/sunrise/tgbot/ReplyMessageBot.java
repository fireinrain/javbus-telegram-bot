package com.sunrise.tgbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

/**
 * @description: 文件下载
 * @version: 1.00
 * @author: lzhaoyang
 * @date: 2021/4/11 4:45 AM
 */
public class ReplyMessageBot extends TelegramLongPollingBot {
    public static final Logger logging = LoggerFactory.getLogger(ReplyMessageBot.class);

    public static String chatId = "";

    public ReplyMessageBot(DefaultBotOptions options) {
        super(options);
    }

    @Override
    public String getBotUsername() {
        return TgBotConfig.REPLY_BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return TgBotConfig.REPLY_BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        //处理channel消息
        if (update.hasChannelPost()) {
            logging.info("----------------------> recieve message from channel place");
            chatId = update.getChannelPost().getChatId().toString();
            //channel post
            if (update.getChannelPost().hasText()) {
                String text = update.getChannelPost().getText();
                SendMessage message = new SendMessage();
                message.setChatId(update.getChannelPost().getChatId().toString());
                message.setText("'"+update.getChannelPost().getText()+"<<<<<-'"+TgBotConfig.REPLY_BOT_NAME);

                try {
                    // Call method to send the message
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }


        //文本消息
        if (update.hasMessage() && update.getMessage().hasText()) {

            logging.info(TgBotConfig.REPLY_BOT_NAME + " 收到消息： " + update.getMessage().getText());
            // Create a SendMessage object with mandatory fields
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId().toString());
            message.setText("'"+update.getMessage().getText()+"<<<<<-'"+TgBotConfig.REPLY_BOT_NAME);

            try {
                // Call method to send the message
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        //图片消息
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            List<PhotoSize> photo = update.getMessage().getPhoto();
            logging.info("收到图片" + photo.size() + "张");
            PhotoSize bigPhotoSize = update.getMessage().getPhoto().get(photo.size() - 1);
            String filePath = bigPhotoSize.getFilePath();

            if (null == filePath) {
                filePath = this.getFilePath(bigPhotoSize);
            }

            //make a getFile request
            FileOutputStream fileOutputStream = null;
            try {
                File file = downloadFile(filePath);
                String replaceFileName = file.getName().replace(".tmp", ".jpg");

                File saveFile = new File(replaceFileName);

                Files.copy(Paths.get(file.getPath()), Paths.get(saveFile.getPath()), StandardCopyOption.REPLACE_EXISTING);

            } catch (TelegramApiException | IOException e) {
                e.printStackTrace();
            }

        }

    }

    public String getFilePath(PhotoSize photo) {
        Objects.requireNonNull(photo);
        // If the file_path is already present, we are done!
        if (photo.getFilePath() != null) {
            return photo.getFilePath();
            // If not, let find it
        } else {
            // We create a GetFile method and set the file_id from the photo
            GetFile getFileMethod = new GetFile();
            getFileMethod.setFileId(photo.getFileId());
            try {
                // We execute the method using AbsSender::execute method.
                org.telegram.telegrambots.meta.api.objects.File file = this.execute(getFileMethod);
                // We now have the file_path
                return file.getFilePath();
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        // Just in case
        return null;
    }


    @Override
    public void onRegister() {
        super.onRegister();
    }
}
