package com.tropicana.chatting.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class ChatRoom {

  private final Long id;
  private final String title;
  private final String description;


}
