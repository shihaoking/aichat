package org.simon.aichat.utils;

import org.apache.commons.lang3.StringUtils;
import org.simon.aichat.claude3.ChatContent;
import org.simon.aichat.claude3.ChatConversation;
import org.simon.aichat.claude3.ChatImageContent;
import org.simon.aichat.claude3.ChatImageSource;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.model.*;
import software.amazon.awssdk.utils.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ChatConversationUtils {
    public static Message conversation2Message(ChatConversation conversation) {
        if(conversation == null) {
            return null;
        }

        List<ChatContent> chatContents = conversation.getContents();

        List<ContentBlock> contentBlocks = new ArrayList<>();
        for (ChatContent chatContent : chatContents) {
            ContentBlock contentBlock = null;

            if (chatContent.getType() == ContentBlock.Type.TEXT) {
                contentBlock = ContentBlock.fromText(chatContent.getText());
            } else if (chatContent.getType() == ContentBlock.Type.IMAGE) {
                // 创建ImageBlock对象
                ChatImageContent imageContent = chatContent.getImage();
                //byte[] imgBytes = Base64.getDecoder().decode(imageContent.getSource().getBytesStr());
                SdkBytes imgBytes = SdkBytes.fromByteArray(Base64.getDecoder().decode(imageContent.getSource().getBytesStr()));
                ImageBlock imageBlock = ImageBlock.builder()
                        .source(ImageSource.fromBytes(imgBytes))
                        .format(ImageFormat.fromValue(imageContent.getFormat()))
                        .build();

                contentBlock = ContentBlock.fromImage(imageBlock);
            }

            if (contentBlock != null) {
                contentBlocks.add(contentBlock);
            }
        }

        return Message.builder()
                .content(contentBlocks)
                .role(ConversationRole.fromValue(conversation.getRole()))
                .build();
    }

    public static List<Message> conversations2Messages(List<ChatConversation> conversations) {
        if(CollectionUtils.isNullOrEmpty(conversations)) {
            return new ArrayList<>();
        }

        List<Message> messages = new ArrayList<>();
        for(ChatConversation conversation : conversations) {
            if(conversation == null) {
                continue;
            }

            messages.add(conversation2Message(conversation));
        }

        return messages;
    }

    public static Message textAndImageFile2Message(ConversationRole role, String userInput, MultipartFile imgFile) {

        List<ContentBlock> contentBlocks = new ArrayList<>();

        if(StringUtils.isNotBlank(userInput)) {
            contentBlocks.add(ContentBlock.fromText(userInput));
        }

        if(imgFile != null) {
            try {
                ImageBlock imageBlock = ImageBlock.builder()
                        .source(ImageSource.fromBytes(SdkBytes.fromByteArray(imgFile.getBytes())))
                        .format(ImageFormat.fromValue(imgFile.getContentType().replaceFirst("image/", "")))
                        .build();

                contentBlocks.add(ContentBlock.fromImage(imageBlock));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if(!CollectionUtils.isNullOrEmpty(contentBlocks)) {
            return Message.builder()
                    .content(contentBlocks)
                    .role(role)
                    .build();
        } else {
            return null;
        }
    }

    public static ChatConversation message2Conversation(Message message) {
        if(message == null) {
            return null;
        }

        ChatConversation chatConversation = new ChatConversation();
        chatConversation.setRole(message.roleAsString());

        for(ContentBlock contentBlock : message.content()) {
            if (contentBlock == null) {
                continue;
            }

            ChatContent chatContent = new ChatContent();
            chatContent.setType(contentBlock.type());

            if (ContentBlock.Type.TEXT == contentBlock.type()) {
                chatContent.setText(contentBlock.text());
            } else if (ContentBlock.Type.IMAGE == contentBlock.type()) {
                ImageBlock imageBlock = contentBlock.image();

                if (imageBlock == null) {
                    continue;
                }
                ChatImageContent imageContent = new ChatImageContent();
                chatContent.setImage(imageContent);
                imageContent.setFormat(imageBlock.formatAsString());

                ChatImageSource chatImageSource = new ChatImageSource();
                imageContent.setSource(chatImageSource);
                ImageSource imageSource = imageBlock.source();
                chatImageSource.setBytesStr(Base64.getEncoder().encodeToString(imageSource.bytes().asByteArray()));
            }

            chatConversation.getContents().add(chatContent);
        }

        return chatConversation;
    }

    public static List<ChatConversation> messages2Conversations(List<Message> messages) {
        if(CollectionUtils.isNullOrEmpty(messages)) {
            return new ArrayList<>();
        }

        List<ChatConversation> chatConversations = new ArrayList<>();
        for (Message message : messages) {
            if(message == null) {
                continue;
            }

            chatConversations.add(message2Conversation(message));
        }

        return chatConversations;
    }
}
