package org.simon.aichat.claude3;

import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;

public class ChatContent {
    private ContentBlock.Type type;
    private String text;
    private ChatImageContent image;

    public ContentBlock.Type getType() {
        return type;
    }

    public void setType(ContentBlock.Type type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ChatImageContent getImage() {
        return image;
    }

    public void setImage(ChatImageContent image) {
        this.image = image;
    }

    public static ChatContent fromText(String text) {
        ChatContent chatContent = new ChatContent();
        chatContent.text = text;
        chatContent.type = ContentBlock.Type.TEXT;
        return chatContent;
    }

    public static ChatContent fromImage(ChatImageContent image) {
        ChatContent chatContent = new ChatContent();
        chatContent.image = image;
        return chatContent;
    }
}
