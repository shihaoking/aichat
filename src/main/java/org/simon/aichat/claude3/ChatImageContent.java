package org.simon.aichat.claude3;

public class ChatImageContent {
    private String format;
    private ChatImageSource source;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public ChatImageSource getSource() {
        return source;
    }

    public void setSource(ChatImageSource source) {
        this.source = source;
    }
}
