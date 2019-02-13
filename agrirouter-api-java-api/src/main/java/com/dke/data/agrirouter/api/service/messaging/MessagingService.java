package com.dke.data.agrirouter.api.service.messaging;

public interface MessagingService<T> {
  void setRequestFormatJSON();

  void setRequestFormatProtobuf();

  String send(T parameters);
}
