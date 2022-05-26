package com.sunrise.tgbot;


import com.fasterxml.jackson.annotation.JsonProperty;

public class TgBotTokenResponse {
    private boolean ok;
    private Result result;
    public void setOk(boolean ok) {
        this.ok = ok;
    }
    public boolean getOk() {
        return ok;
    }

    public void setResult(Result result) {
        this.result = result;
    }
    public Result getResult() {
        return result;
    }
}

class Result{
    private int id;
    @JsonProperty("is_bot")
    private boolean isBot;
    @JsonProperty("first_name")
    private String firstName;
    private String username;
    @JsonProperty("can_join_groups")
    private boolean canJoinGroups;
    @JsonProperty("can_read_all_group_messages")
    private boolean canReadAllGroupMessages;
    @JsonProperty("supports_inline_queries")
    private boolean supportsInlineQueries;
    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setIsBot(boolean isBot) {
        this.isBot = isBot;
    }
    public boolean getIsBot() {
        return isBot;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getFirstName() {
        return firstName;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }

    public void setCanJoinGroups(boolean canJoinGroups) {
        this.canJoinGroups = canJoinGroups;
    }
    public boolean getCanJoinGroups() {
        return canJoinGroups;
    }

    public void setCanReadAllGroupMessages(boolean canReadAllGroupMessages) {
        this.canReadAllGroupMessages = canReadAllGroupMessages;
    }
    public boolean getCanReadAllGroupMessages() {
        return canReadAllGroupMessages;
    }

    public void setSupportsInlineQueries(boolean supportsInlineQueries) {
        this.supportsInlineQueries = supportsInlineQueries;
    }
    public boolean getSupportsInlineQueries() {
        return supportsInlineQueries;
    }

}


