package org.simon.aichat.service;

import org.simon.aichat.claude3.ChatConversation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatConversationRecord {
    private Long chatId;

    private Long userId;

    private List<ChatConversation> conversations = new ArrayList<>();

    private Date gmtCreate;

    private Date gmtModified;

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<ChatConversation> getConversations() {
        return conversations;
    }

    public void setConversations(List<ChatConversation> conversations) {
        this.conversations = conversations;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }
}
