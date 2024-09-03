package org.simon.aichat.claude3;

import java.util.ArrayList;
import java.util.List;

public class ChatConversation {

    private String role;

    private List<ChatContent> contents = new ArrayList<>();

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<ChatContent> getContents() {
        return contents;
    }

    public void setContents(List<ChatContent> contents) {
        this.contents = contents;
    }
}
