package org.simon.aichat.websocket;

public class ChatStreamMessage {
    private String streamMessage;

    private String finallMessage;

    public String getStreamMessage() {
        return streamMessage;
    }

    public void setStreamMessage(String streamMessage) {
        this.streamMessage = streamMessage;
    }

    public String getFinallMessage() {
        return finallMessage;
    }

    public void setFinallMessage(String finallMessage) {
        this.finallMessage = finallMessage;
    }
}
