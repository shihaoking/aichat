package org.simon.aichat.claude3;

import com.fasterxml.jackson.annotation.JsonIgnore;
import software.amazon.awssdk.services.bedrockruntime.model.ImageSource;

public class ChatImageSource {
    private String bytesStr;

    @JsonIgnore
    private ImageSource.Type type;

    public String getBytesStr() {
        return bytesStr;
    }

    public void setBytesStr(String bytesStr) {
        this.bytesStr = bytesStr;
    }

    public ImageSource.Type getType() {
        return type;
    }

    public void setType(ImageSource.Type type) {
        this.type = type;
    }
}