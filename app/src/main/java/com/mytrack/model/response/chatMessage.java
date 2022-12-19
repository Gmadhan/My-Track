package com.mytrack.model.response;

import java.util.Date;

public class chatMessage {

    private String messageText;
    private String messageUser;
    private String from;
    private String imageUrl;
    private long messageTime;

    public chatMessage(String messageText, String messageUser, String from, String imageUrl) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.from = from;
        this.imageUrl = imageUrl;

        // Initialize to current time
        messageTime = new Date().getTime();
    }

    public chatMessage(){

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
