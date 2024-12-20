package org.simon.aichat.websocket;

import com.alibaba.fastjson.JSON;
import io.micrometer.common.util.StringUtils;
import org.simon.aichat.claude3.ConverseAsync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class RTChatWebSocketHandler extends TextWebSocketHandler {
    /**
     * 存储websocket客户端连接
     * */
    private static final Map<String, WebSocketSession> connections = new HashMap<>();
    private static final Map<String, List<Message>> chatRecords = new HashMap<>();

    @Autowired
    private ConverseAsync converseAsync;


    /**
     * 建立连接后触发
     * */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String hostName = session.getRemoteAddress().getHostName();
        System.out.println("成功建立websocket连接, hostName: " + hostName);

        // 建立连接后将连接以键值对方式存储，便于后期向客户端发送消息
        // 以客户端连接的唯一标识为key,可以通过客户端发送唯一标识
        if(!connections.containsKey(hostName)) {
            connections.put(hostName, session);
        }

        System.out.println("当前客户端连接数：" + connections.size());
    }

    /**
     * 接收消息
     * */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        System.out.println("收到消息: " + message.getPayload());

        List<Message> historyMessages = chatRecords.computeIfAbsent(session.getRemoteAddress().getHostName(), k -> new ArrayList<>());
        Message newMessage = Message.builder()
                .role(ConversationRole.USER)
                .content(ContentBlock.fromText(message.getPayload()))
                .build();


        historyMessages.add(newMessage);

        System.out.println("message list: " + historyMessages);

        System.out.println("Start send messages to claude by stream");
        converseAsync.converseStream(historyMessages,
                new ContentBlockDeltaComsumer(session), null,
                new MessageStopComnsumer(session));
        System.out.println("Get response messages from claude by stream");
    }

    /**
     * 传输异常处理
     * */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }

    /**
     * 关闭连接时触发
     * */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String hostName = session.getRemoteAddress().getHostName();
        System.out.println("触发关闭websocket连接, hostName=: " + hostName);

        // 移除连接
        connections.remove(hostName);
        chatRecords.remove(hostName);
    }

    @Override
    public boolean supportsPartialMessages() {
        return super.supportsPartialMessages();
    }

    /**
     * 向连接的客户端发送消息
     *
     * @author lucky_fd
     * @param session 客户端
     * @param message 消息体
     **/
    public void sendMessage(WebSocketSession session, TextMessage message) {
        try {
            // 判断连接是否正常
            if (session != null && session.isOpen()) {
                session.sendMessage(message);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 实时响应内容处理
     */
    private class ContentBlockDeltaComsumer implements Consumer<String> {
        private WebSocketSession session;

        public ContentBlockDeltaComsumer(WebSocketSession session) {
            this.session = session;
        }

        @Override
        public void accept(String message) {
            System.out.println(session.getRemoteAddress().getHostName() + "on stream content block delta, message: " + message);
            sendMessage(session, new TextMessage(message));
        }
    }


    /**
     * 响应完成后处理
     */
    private class MessageStopComnsumer implements Consumer<ChatStreamMessage> {
        private WebSocketSession session;


        public MessageStopComnsumer(WebSocketSession session) {
            this.session = session;
        }

        @Override
        public void accept(ChatStreamMessage message) {
            System.out.println(session.getRemoteAddress().getHostName() + " on stream message stop, message: " + JSON.toJSONString(message));

            if(StringUtils.isNotBlank(message.getStreamMessage())) {
                sendMessage(session, new TextMessage(message.getStreamMessage()));
            }

            Message respMessage = Message.builder()
                    .role(ConversationRole.ASSISTANT)
                    .content(ContentBlock.fromText(message.getFinallMessage()))
                    .build();

            List<Message> historyMessages = chatRecords.get(session.getRemoteAddress().getHostName());
            historyMessages.add(respMessage);
        }
    }
}
