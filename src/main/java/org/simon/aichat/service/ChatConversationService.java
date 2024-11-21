package org.simon.aichat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.simon.aichat.claude3.ChatConversation;
import org.simon.aichat.dbservice.ChatRecord;
import org.simon.aichat.dbservice.ChatRepository;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;

import java.util.*;

@Service
public class ChatConversationService {
    @Resource
    private ChatRepository chatRepository;

    public ChatConversationRecord getChatConversationsByChatId(Long chatId) {
        Optional<ChatRecord> chatRecord = chatRepository.findById(chatId);
        if (chatRecord.isEmpty()) {
            return null;
        }

        ChatConversationRecord chatConversationRecord = new ChatConversationRecord();
        chatConversationRecord.setChatId(chatRecord.get().getId());
        chatConversationRecord.setUserId(chatRecord.get().getUserId());
        chatConversationRecord.setGmtCreate(chatRecord.get().getGmtCreate());
        chatConversationRecord.setGmtModified(chatRecord.get().getGmtModified());

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<ChatConversation> chatConversations = objectMapper.readValue(chatRecord.get().getContent(), new TypeReference<>() {
            });
            chatConversationRecord.setConversations(chatConversations);
        } catch (JsonProcessingException e) {
            System.err.printf("Can't getChatConversationsByChatId '%s': %s", chatId, e.getMessage());
        }

        return chatConversationRecord;
    }

    public Long saveChatConversation(ChatConversationRecord chatConversationRecord) {
        ChatRecord record = new ChatRecord();
        record.setId(chatConversationRecord.getChatId());
        record.setUserId(chatConversationRecord.getUserId() == null ? 1L : chatConversationRecord.getUserId());
        record.setGmtCreate(chatConversationRecord.getGmtCreate() == null ? new Date() : chatConversationRecord.getGmtCreate());
        record.setGmtModified(new Date());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String conversationStr = objectMapper.writeValueAsString(chatConversationRecord.getConversations());

            record.setContent(conversationStr);
            record = chatRepository.save(record);
        } catch (JsonProcessingException e) {
            System.err.printf("Can't saveChatConversation '%s': %s", e.getMessage());
            return null;
        }
        return record.getId();
    }

    public List<ChatRecordSummary> getChatRecordsSummaryByUserId(Long userId) {
        Optional<List<ChatRecord>> chatRecords = chatRepository.findByUserId(userId);

        if(chatRecords.isEmpty()) {
            return null;
        }

        List<ChatRecordSummary> chatRecordSummaries = new ArrayList<>();
        chatRecords.ifPresent(chatRecordList -> {
            chatRecordList.forEach(record -> {
                ChatRecordSummary chatRecordSummary = new ChatRecordSummary();
                chatRecordSummary.setChatId(record.getId());
                chatRecordSummary.setUserId(record.getUserId());
                chatRecordSummary.setGmtCreate(record.getGmtCreate());
                chatRecordSummary.setGmtModified(record.getGmtModified());

                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    List<ChatConversation> chatConversations = objectMapper.readValue(record.getContent(), new TypeReference<>() {
                    });

                    if(!chatConversations.isEmpty() && !chatConversations.get(0).getContents().isEmpty()) {
                        chatConversations.get(0).getContents().stream()
                                .filter(item -> item.getType() == ContentBlock.Type.TEXT).forEach(item -> {
                                    chatRecordSummary.setChatSummary(item.getText().length() > 30 ? item.getText().substring(0, 30) : item.getText());
                                });
                    }
                } catch (JsonProcessingException e) {
                    System.err.printf("Can't getChatRecordsSummaryByUserId '%s': %s", userId, e.getMessage());
                }


                chatRecordSummaries.add(chatRecordSummary);
            });
        });

        chatRecordSummaries.sort(Comparator.comparing(ChatRecordSummary::getGmtModified));
        Collections.reverse(chatRecordSummaries);
        return chatRecordSummaries;
    }
}
