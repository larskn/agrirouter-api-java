package com.dke.data.agrirouter.impl.messaging.rest;

import static com.dke.data.agrirouter.impl.RequestFactory.MEDIA_TYPE_PROTOBUF;

import com.dke.data.agrirouter.api.dto.encoding.EncodeMessageResponse;
import com.dke.data.agrirouter.api.service.messaging.SendRawMessageService;
import com.dke.data.agrirouter.api.service.parameters.*;
import com.dke.data.agrirouter.impl.common.MessageIdService;
import com.dke.data.agrirouter.impl.messaging.encoding.EncodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.validation.ResponseValidator;
import com.google.protobuf.ByteString;
import java.io.UnsupportedEncodingException;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.apache.xerces.impl.dv.util.Base64;

public class SendRawMessageServiceImpl
    implements SendRawMessageService, ResponseValidator, MessageSender {

  private MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
  private EncodeMessageServiceImpl encodeMessageService;

  public SendRawMessageServiceImpl() {

    this.encodeMessageService = new EncodeMessageServiceImpl();
  }

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
  public String send(SendRawMessageParameters sendRawMessageParameters) {
    sendRawMessageParameters.validate();

    EncodeMessageResponse encodedMessageResponse = encodeMessage(sendRawMessageParameters);

    SendMessageParameters sendMessageParameters = new SendMessageParameters();
    sendMessageParameters.setOnboardingResponse(sendRawMessageParameters.getOnBoardingResponse());
    sendMessageParameters.setMessages(encodedMessageResponse);

    MessageSenderResponse response = this.sendMessage(sendMessageParameters);
    this.assertResponseStatusIsValid(response.getNativeResponse(), HttpStatus.SC_OK);

    return encodedMessageResponse.getApplicationMessageID();
  }

  private EncodeMessageResponse encodeMessage(SendRawMessageParameters parameters) {

    MessageHeaderParameters messageHeaderParameters = new MessageHeaderParameters();

    final String applicationMessageID = MessageIdService.generateMessageId();
    messageHeaderParameters.setApplicationMessageId(applicationMessageID);

    messageHeaderParameters.setTechnicalMessageType(parameters.getTechnicalMessageType());
    messageHeaderParameters.setMode(parameters.getMode());
    messageHeaderParameters.setRecipients(parameters.getReceipients());
    messageHeaderParameters.setTeamSetContextId(parameters.getTeamSetContextId());

    PayloadParameters payloadParameters = new PayloadParameters();
    payloadParameters.setTypeUrl(parameters.getTypeURL());
    if (parameters.getShallEncodeBase64() == true) {
      String base64 = Base64.encode(parameters.getRawData());
      try {
        payloadParameters.setValue(ByteString.copyFrom(base64, "ascii"));
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    } else {
      payloadParameters.setValue(ByteString.copyFrom(parameters.getRawData()));
    }

    EncodeMessageResponse encodedMessage =
        this.encodeMessageService.encode(messageHeaderParameters, payloadParameters);
    return encodedMessage;
  }
}
