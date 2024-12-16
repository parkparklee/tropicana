package com.tropicana.chatting.entity;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChatMessage {

  public enum Type {
    INIT, REJOIN, CHAT, LEAVE;
  }

  private final Type type;
  private final Long userId;
  private final Long chatRoomId;
  private final String message;

}
