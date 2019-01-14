package com.dke.data.agrirouter.impl.messaging.rest;

import agrirouter.feed.request.FeedRequests;
import agrirouter.request.Request;
import com.dke.data.agrirouter.api.dto.encoding.EncodeMessageResponse;
import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.dke.data.agrirouter.api.factories.impl.DeleteMessageMessageContentFactory;
import com.dke.data.agrirouter.api.factories.impl.parameters.DeleteMessageMessageParameters;
import com.dke.data.agrirouter.api.service.messaging.DeleteMessageService;
import com.dke.data.agrirouter.api.service.messaging.encoding.EncodeMessageService;
import com.dke.data.agrirouter.api.service.parameters.DeleteMessageParameters;
import com.dke.data.agrirouter.api.service.parameters.MessageHeaderParameters;
import com.dke.data.agrirouter.api.service.parameters.PayloadParameters;
import com.dke.data.agrirouter.api.service.parameters.SendMessageParameters;
import com.dke.data.agrirouter.impl.common.MessageIdService;
import com.dke.data.agrirouter.impl.messaging.encoding.EncodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.validation.ResponseValidator;
import java.util.Collections;
import java.util.Objects;

import com.sap.iotservices.common.protobuf.gateway.MeasureRequestMessageProtos;
import org.apache.http.HttpStatus;

import javax.ws.rs.core.MediaType;

import static com.dke.data.agrirouter.impl.RequestFactory.MEDIA_TYPE_PROTOBUF;

public class DeleteMessageServiceImpl
    implements DeleteMessageService, MessageSender, ResponseValidator {

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

  private final EncodeMessageService encodeMessageService;

  public DeleteMessageServiceImpl() {
    this.encodeMessageService = new EncodeMessageServiceImpl();
  }

  @Override
  public String send(DeleteMessageParameters parameters) {
    parameters.validate();

    SendMessageParameters sendMessageParameters = new SendMessageParameters();
    sendMessageParameters.setOnboardingResponse(parameters.getOnboardingResponse());
    EncodeMessageResponse encodedMessageResponse = encodeMessage(parameters);

    sendMessageParameters.setMessages(encodedMessageResponse);



    MessageSenderResponse response = this.sendMessage(sendMessageParameters);

    this.assertResponseStatusIsValid(response.getNativeResponse(), HttpStatus.SC_OK);
    return encodedMessageResponse.getApplicationMessageID();
  }

  private EncodeMessageResponse encodeMessage(DeleteMessageParameters parameters) {
    MessageHeaderParameters messageHeaderParameters = new MessageHeaderParameters();
    final String applicationMessageID = MessageIdService.generateMessageId();
    messageHeaderParameters.setApplicationMessageId(applicationMessageID);

    messageHeaderParameters.setTechnicalMessageType(TechnicalMessageType.DKE_FEED_DELETE);
    messageHeaderParameters.setMode(Request.RequestEnvelope.Mode.DIRECT);

    DeleteMessageMessageParameters deleteMessageMessageParameters =
        new DeleteMessageMessageParameters();
    deleteMessageMessageParameters.setMessageIds(
        Objects.requireNonNull(parameters.getMessageIds()));
    deleteMessageMessageParameters.setSenderIds(Objects.requireNonNull(parameters.getSenderIds()));
    deleteMessageMessageParameters.setSentFromInSeconds(parameters.getSentFromInSeconds());
    deleteMessageMessageParameters.setSentToInSeconds(parameters.getSentToInSeconds());

    PayloadParameters payloadParameters = new PayloadParameters();
    payloadParameters.setTypeUrl(FeedRequests.MessageDelete.getDescriptor().getFullName());
    payloadParameters.setValue(
        new DeleteMessageMessageContentFactory().message(deleteMessageMessageParameters));


    EncodeMessageResponse encodedMessage = this.encodeMessageService.encode(messageHeaderParameters, payloadParameters);
    return encodedMessage;
  }
}
