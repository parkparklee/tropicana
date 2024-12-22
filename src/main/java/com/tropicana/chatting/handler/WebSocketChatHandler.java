package com.tropicana.chatting.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tropicana.chatting.dto.ChatMessageDto;
import com.tropicana.chatting.dto.ChatMessageDto.Type;
import java.io.IOException;
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

  //TODO greeting & leaving 메시지에 userId 대신 사용자가 설정한 이름을 넣도록 수정해야 함
  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String payload = message.getPayload();

    ChatMessageDto chatMessage = mapper.readValue(payload, ChatMessageDto.class);

    if (chatMessage.getType().equals(Type.CREATE)) {
      chatRooms.computeIfAbsent(chatMessage.getChatRoomId(), k -> new HashSet<>()).add(session);
    } else if (chatMessage.getType().equals(Type.JOIN)) {
      Long chatRoomId = chatMessage.getChatRoomId();
      chatRooms.get(chatRoomId).add(session);

      String greetingMessage = String.format("%s님이 입장하셨습니다.", chatMessage.getUserId());
      sendMessage(chatRoomId, greetingMessage);

    } else if (chatMessage.getType().equals(Type.REJOIN)) {
      chatRooms.get(chatMessage.getChatRoomId()).add(session);
    } else if (chatMessage.getType().equals(Type.CHAT)) {
      sendMessage(chatMessage.getChatRoomId(), chatMessage.getMessage());
    } else if (chatMessage.getType().equals(Type.LEAVE)) {
      chatRooms.get(chatMessage.getChatRoomId()).remove(session);
      String leavingMessage = String.format("%s님이 퇴장하셨습니다.", chatMessage.getUserId());
      sendMessage(chatMessage.getChatRoomId(), leavingMessage);
    }
  }

  private void sendMessage(Long chatRoomId, String message) throws IOException {
    for (WebSocketSession webSocketSession : chatRooms.get(chatRoomId)) {
      webSocketSession.sendMessage(new TextMessage(message));
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    sessions.remove(session);
  }
}
