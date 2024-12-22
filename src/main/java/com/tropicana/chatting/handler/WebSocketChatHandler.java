package com.tropicana.chatting.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tropicana.chatting.dto.ChatMessageDto;
import com.tropicana.chatting.dto.ChatMessageDto.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {

  private final ObjectMapper mapper;
  private final Set<WebSocketSession> sessions = new HashSet<>();
  private final Map<Long, Set<WebSocketSession>> chatRooms = new ConcurrentHashMap<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    sessions.add(session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String payload = message.getPayload();

    ChatMessageDto chatMessage = mapper.readValue(payload, ChatMessageDto.class);

    if (chatMessage.getType().equals(Type.INIT)) {
      chatRooms.computeIfAbsent(chatMessage.getChatRoomId(), k -> new HashSet<>()).add(session);
    } else if (chatMessage.getType().equals(Type.CHAT)) {
      for (WebSocketSession webSocketSession : chatRooms.get(chatMessage.getChatRoomId())) {
        webSocketSession.sendMessage(new TextMessage(mapper.writeValueAsString(chatMessage)));
      }
    } else if (chatMessage.getType().equals(Type.LEAVE)) {
      chatRooms.get(chatMessage.getChatRoomId()).remove(session);
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    sessions.remove(session);
  }
}
