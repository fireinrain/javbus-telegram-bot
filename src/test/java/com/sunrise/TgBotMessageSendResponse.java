package com.sunrise;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TgBotMessageSendResponse {


    private boolean ok;
    private SendResult result;

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public boolean getOk() {
        return ok;
    }

    public void setResult(SendResult result) {
        this.result = result;
    }

    public SendResult getResult() {
        return result;
    }
}

class SendResult {

    @JsonProperty("message_id")
    private int messageId;
    @JsonProperty("sender_chat")
    private SenderChat senderChat;
    private Chat chat;
    private int date;
    private String text;

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setSenderChat(SenderChat senderChat) {
        this.senderChat = senderChat;
    }

    public SenderChat getSenderChat() {
        return senderChat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Chat getChat() {
        return chat;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getDate() {
        return date;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

}

class SenderChat {

    private String id;
    private String title;
    private String username;
    private String type;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}

class Chat {

    private String id;
    private String title;
    private String username;
    private String type;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}


