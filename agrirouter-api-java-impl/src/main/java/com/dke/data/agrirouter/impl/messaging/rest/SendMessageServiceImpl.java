package com.dke.data.agrirouter.impl.messaging.rest;

import com.dke.data.agrirouter.api.service.messaging.SendMessageService;
import com.dke.data.agrirouter.api.service.parameters.SendMessageParameters;
import com.dke.data.agrirouter.impl.validation.ResponseValidator;
import org.apache.http.HttpStatus;

import javax.ws.rs.core.MediaType;

import static com.dke.data.agrirouter.impl.RequestFactory.MEDIA_TYPE_PROTOBUF;

public class SendMessageServiceImpl
    implements SendMessageService, ResponseValidator, MessageSender {

  private MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;

  @Override
  public void setRequestFormatJSON() {
    mediaType = MediaType.APPLICATION_JSON_TYPE;
  }

  @Override
  public void setRequestFormatProtobuf() {
    mediaType = MEDIA_TYPE_PROTOBUF;
  }

  @Override
  public MediaType getResponseFormat() {
    return mediaType;
  }

  @Override
  public void send(SendMessageParameters parameters) {
    parameters.validate();
    MessageSenderResponse response = this.sendMessage(parameters);
    this.assertResponseStatusIsValid(response.getNativeResponse(), HttpStatus.SC_OK);
  }
}
