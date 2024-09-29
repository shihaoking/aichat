package org.simon.aichat.restfulapi;

import org.simon.aichat.service.ChatRecordSummary;
import org.simon.aichat.utils.ChatConversationUtils;
import org.simon.aichat.claude3.ChatConversation;
import org.simon.aichat.claude3.ConverseAsync;
import org.simon.aichat.service.ChatConversationRecord;
import org.simon.aichat.service.ChatConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

@RestController
public class ChatController {

    @Autowired
    private ConverseAsync converseAsync;

    @Autowired
    private ChatConversationService chatConversationService;

    @PostMapping("/chat")
    public ResponseEntity<ChatConversation> chat(@RequestParam(required = false) Long id, @RequestParam String userInput, @RequestParam(required = false) MultipartFile imgFile) {

        ChatConversationRecord chatRecord = null;
        if(id != null) {
            chatRecord = chatConversationService.getChatConversationsByChatId(id);
        }

        if(chatRecord == null) {
            chatRecord = new ChatConversationRecord();
        }

        Message newInputMessage = ChatConversationUtils.textAndImageFile2Message(ConversationRole.USER, userInput, imgFile);
        if(newInputMessage != null) {
            //将本次用户的对话追加到数据库的记录里
            chatRecord.getConversations().add(ChatConversationUtils.message2Conversation(newInputMessage));
        }

        List<Message> messages = ChatConversationUtils.conversations2Messages(chatRecord.getConversations());

        System.out.printf("Start send messages to claude, %s \n\n", new Date());
        Message resultMsg = converseAsync.converseAsync(messages);
        System.out.printf("Get response messages from claude, %s \n\n", new Date());

        ChatConversation chatRespConversation = ChatConversationUtils.message2Conversation(resultMsg);
        //将本次系统的回答追加到数据库的记录里
        chatRecord.getConversations().add(chatRespConversation);
        chatConversationService.saveChatConversation(chatRecord);

        return ResponseEntity.ok(chatRespConversation);
    }

    @GetMapping("/chat/{id}")
    public ResponseEntity<ChatConversationRecord> chat(@PathVariable Long id) {
        ChatConversationRecord chatRecord = chatConversationService.getChatConversationsByChatId(id);
        if(chatRecord == null) {
            chatRecord = new ChatConversationRecord();
        }

        return ResponseEntity.ok(chatRecord);
    }

    @GetMapping("/chat_records_summary")
    public ResponseEntity<List<ChatRecordSummary>> chatRecordsSummary() {
        List<ChatRecordSummary> chatRecordSummaries = chatConversationService.getChatRecordsSummaryByUserId(1L);
        return ResponseEntity.ok(chatRecordSummaries);
    }
}
