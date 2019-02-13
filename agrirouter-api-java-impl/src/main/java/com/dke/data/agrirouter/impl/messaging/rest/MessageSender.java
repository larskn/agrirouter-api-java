package com.dke.data.agrirouter.impl.messaging.rest;

import static com.dke.data.agrirouter.impl.RequestFactory.MEDIA_TYPE_PROTOBUF;

import com.dke.data.agrirouter.api.dto.messaging.SendMessageRequest;
import com.dke.data.agrirouter.api.dto.messaging.inner.Message;
import com.dke.data.agrirouter.api.enums.CertificationType;
import com.dke.data.agrirouter.api.service.parameters.SendMessageParameters;
import com.dke.data.agrirouter.impl.RequestFactory;
import com.dke.data.agrirouter.impl.common.UtcTimeService;
import com.dke.data.agrirouter.impl.gson.MessageTypeAdapter;
import com.google.gson.GsonBuilder;
import com.google.protobuf.*;
import com.sap.iotservices.common.protobuf.gateway.MeasureProtos;
import com.sap.iotservices.common.protobuf.gateway.MeasureRequestMessageProtos;
import org.apache.commons.net.util.Base64;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public interface MessageSender {

  void setRequestFormatJSON();

  void setRequestFormatProtobuf();

  MediaType getResponseFormat();

  default String createMessageBody(SendMessageParameters parameters) {
    parameters.validate();
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Message.class, new MessageTypeAdapter());
    String json = gsonBuilder.create().toJson(this.createSendMessageJSONRequest(parameters));
    return json;
  }

  default SendMessageRequest createSendMessageJSONRequest(SendMessageParameters parameters) {
    parameters.validate();
    SendMessageRequest sendMessageRequest = new SendMessageRequest();
    sendMessageRequest.setSensorAlternateId(
        parameters.getOnboardingResponse().getSensorAlternateId());
    sendMessageRequest.setCapabilityAlternateId(
        parameters.getOnboardingResponse().getCapabilityAlternateId());
    List<Message> messages = new ArrayList<>();
    parameters
        .getEncodedMessages()
        .forEach(
            messageToSend -> {
              Message message = new Message();
              message.setMessage(messageToSend);
              message.setTimestamp("" + UtcTimeService.now().toEpochSecond());
              messages.add(message);
            });
    sendMessageRequest.setMessages(messages);
    return sendMessageRequest;
  }

  default MeasureProtos.MeasureRequest createSendMessageProtobufRequest(
      SendMessageParameters sendMessageParameters) {
    sendMessageParameters.validate();

    MeasureProtos.MeasureRequest.Builder measureMessageBuilder =
        MeasureProtos.MeasureRequest.newBuilder()
            .setCapabilityAlternateId(
                sendMessageParameters.onboardingResponse.capabilityAlternateId)
            .setSensorAlternateId(sendMessageParameters.onboardingResponse.sensorAlternateId)
            .setTimestamp(UtcTimeService.now().toEpochSecond())
            .setSensorTypeAlternateId("");

    for (MeasureRequestMessageProtos.MeasureRequestMessage measureMessage :
        sendMessageParameters.measureMessages) {
      MeasureProtos.MeasureRequest.Measure.Builder measureBuilder =
          MeasureProtos.MeasureRequest.Measure.newBuilder();

      ByteString protobufMessage =
          ByteString.copyFrom(
              measureMessage
                  .getMessage()
                  .toByteArray());

      com.google.protobuf.Message message =
          BytesValue.newBuilder().setValue(protobufMessage).build();
      measureBuilder.addValues(Any.pack(message, "message"));

      Timestamp timestamp =
          Timestamp.newBuilder()
              .setSeconds(UtcTimeService.now().toEpochSecond())
              .setNanos(0)
              .build();

      String protobufTimeStampString =
          String.valueOf(timestamp.getSeconds() * 1000 + timestamp.getNanos());

      com.google.protobuf.Message protobufTimestamp =
          StringValue.newBuilder().setValue(protobufTimeStampString).build();
      measureBuilder.addValues(Any.pack(protobufTimestamp, "timestamp"));

      measureMessageBuilder.addMeasures(measureBuilder.build());
    }

    MeasureProtos.MeasureRequest sendMessageProtobufRequest = measureMessageBuilder.build();

    String base64Representation = Base64.encodeBase64String(sendMessageProtobufRequest.toByteArray());
    System.out.println("Representation of Command in Base64: "+ base64Representation);

    return sendMessageProtobufRequest;
  }

  default MessageSenderResponse sendMessage(SendMessageParameters parameters) {

    Response response;

    if (getResponseFormat() == MEDIA_TYPE_PROTOBUF) {
      MeasureProtos.MeasureRequest data = this.createSendMessageProtobufRequest(parameters);

      Entity<MeasureProtos.MeasureRequest> protobufContent =
          Entity.entity(data, MEDIA_TYPE_PROTOBUF);
      response =
          RequestFactory.securedRequest(
                  parameters.getOnboardingResponse().getConnectionCriteria().getMeasures(),
                  parameters.getOnboardingResponse().getAuthentication().getCertificate(),
                  parameters.getOnboardingResponse().getAuthentication().getSecret(),
                  CertificationType.valueOf(
                      parameters.getOnboardingResponse().getAuthentication().getType()),
                  getResponseFormat(),
                  RequestFactory.DIRECTION_INBOX)
              .post(protobufContent);

    } else {
      Entity<SendMessageRequest> jsonContent =
          Entity.json(this.createSendMessageJSONRequest(parameters));
      response =
          RequestFactory.securedRequest(
                  parameters.getOnboardingResponse().getConnectionCriteria().getMeasures(),
                  parameters.getOnboardingResponse().getAuthentication().getCertificate(),
                  parameters.getOnboardingResponse().getAuthentication().getSecret(),
                  CertificationType.valueOf(
                      parameters.getOnboardingResponse().getAuthentication().getType()),
                  getResponseFormat(),
                  RequestFactory.DIRECTION_INBOX)
              .post(jsonContent);
    }

    return new MessageSenderResponse(response);
  }

  class MessageSenderResponse {

    private final Response nativeResponse;

    private MessageSenderResponse(Response nativeResponse) {
      this.nativeResponse = nativeResponse;
    }

    public Response getNativeResponse() {
      return nativeResponse;
    }
  }
}
