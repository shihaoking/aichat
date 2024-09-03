package org.simon.aichat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.simon.aichat.claude3.ChatConversation;
import org.simon.aichat.dbservice.ChatRecord;
import org.simon.aichat.dbservice.ChatRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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
            System.err.printf("Can't invoke '%s': %s", chatId, e.getMessage());
        }

        return chatConversationRecord;
    }

    public boolean saveChatConversation(ChatConversationRecord chatConversationRecord) {
        ChatRecord record = new ChatRecord();
        record.setId(chatConversationRecord.getChatId());
        record.setUserId(chatConversationRecord.getUserId() == null ? 1L : chatConversationRecord.getUserId());
        record.setGmtCreate(chatConversationRecord.getGmtCreate() == null ? new Date() : chatConversationRecord.getGmtCreate());
        record.setGmtModified(new Date());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String conversationStr = objectMapper.writeValueAsString(chatConversationRecord.getConversations());

            record.setContent(conversationStr);
            chatRepository.save(record);

        } catch (JsonProcessingException e) {
            System.err.printf("Can't invoke '%s': %s", e.getMessage());
            return false;
        }
        return true;
    }
}
