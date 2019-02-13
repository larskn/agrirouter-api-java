package com.dke.data.agrirouter.impl.messaging.encoding;

import static org.junit.jupiter.api.Assertions.*;

import agrirouter.request.Request;
import agrirouter.request.payload.endpoint.Capabilities;
import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.dto.encoding.EncodeMessageResponse;
import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.dke.data.agrirouter.api.service.messaging.encoding.EncodeMessageService;
import com.dke.data.agrirouter.api.service.parameters.MessageHeaderParameters;
import com.dke.data.agrirouter.api.service.parameters.PayloadParameters;
import com.google.protobuf.ByteString;
import java.util.Base64;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EncodeMessageServiceImplTest {

  @Test
  void givenValidParametersEncodeAndDecodeBackShouldNotFail() {
    EncodeMessageService encodeMessageService = new EncodeMessageServiceImpl();

    ByteString toSendMessage = ByteString.copyFromUtf8("secretMessage");
    MessageHeaderParameters messageHeaderParameters = getMessageHeaderParameters();
    PayloadParameters payloadParameters = getPayloadParameters(toSendMessage);

    EncodeMessageResponse encodedMessage =
        encodeMessageService.encode(messageHeaderParameters, payloadParameters);

    String encodedMessageBase64 = encodedMessage.getEncodedMessageBase64();
    DecodeMessageServiceImpl decodeMessageService = new DecodeMessageServiceImpl();
    DecodeMessageResponse response = decodeMessageService.decode(encodedMessageBase64);
    Assertions.assertEquals(
        "secretMessage",
        response.getResponsePayloadWrapper().getDetails().getValue().toStringUtf8());
  }

  @Test
  void givenWrongPayloadEncodeAndDecodeBackShouldFail() {
    EncodeMessageService encodeMessageService = new EncodeMessageServiceImpl();

    ByteString toSendMessage = ByteString.copyFromUtf8("wrong Message");
    MessageHeaderParameters messageHeaderParameters = getMessageHeaderParameters();
    PayloadParameters payloadParameters = getPayloadParameters(toSendMessage);

    EncodeMessageResponse encodedMessage =
        encodeMessageService.encode(messageHeaderParameters, payloadParameters);

    String encodedMessageBase64 = encodedMessage.getEncodedMessageBase64();
    DecodeMessageServiceImpl decodeMessageService = new DecodeMessageServiceImpl();
    DecodeMessageResponse response =
        decodeMessageService.decode(encodedMessage.getEncodedMessageBase64());
    Assertions.assertNotEquals(
        "secretMessage",
        response.getResponsePayloadWrapper().getDetails().getValue().toStringUtf8());
  }

  @Test
  void givenNullPayLoadParametersEncodeShouldThrowException() {
    EncodeMessageService encodeMessageService = new EncodeMessageServiceImpl();

    MessageHeaderParameters messageHeaderParameters = getMessageHeaderParameters();
    assertThrows(
        IllegalArgumentException.class,
        () -> encodeMessageService.encode(messageHeaderParameters, null));
  }

  @Test
  void givenNullMessageHeaderEncodeShouldThrowException() {
    EncodeMessageService encodeMessageService = new EncodeMessageServiceImpl();

    PayloadParameters payloadParameters =
        getPayloadParameters(ByteString.copyFromUtf8("secretMessage"));
    assertThrows(
        IllegalArgumentException.class, () -> encodeMessageService.encode(null, payloadParameters));
  }

  @Test
  void settingFormatProtobufIsRecognized() {
    EncodeMessageService encodeMessageService = new EncodeMessageServiceImpl();
    encodeMessageService.setRequestFormatProtobuf();
    ByteString toSendMessage = ByteString.copyFromUtf8("secretMessage");
    MessageHeaderParameters messageHeaderParameters = getMessageHeaderParameters();
    PayloadParameters payloadParameters = getPayloadParameters(toSendMessage);

    EncodeMessageResponse encodedMessage =
        encodeMessageService.encode(messageHeaderParameters, payloadParameters);

    assertEquals(encodedMessage.getEncodedMessageBase64(),"");
    assertNotNull(encodedMessage.getEncodedMessageProtobuf());

    byte[] messageBuffer = encodedMessage.getEncodedMessageProtobuf().toByteArray();
    byte[] encodedMessageBase64 = Base64.getEncoder().encode(messageBuffer);
    String encodedMessageBase64String = ByteString.copyFrom(encodedMessageBase64).toStringUtf8();
    DecodeMessageServiceImpl decodeMessageService = new DecodeMessageServiceImpl();

    DecodeMessageResponse response = decodeMessageService.decode(encodedMessageBase64String);
    Assertions.assertEquals(
        "secretMessage",
        response.getResponsePayloadWrapper().getDetails().getValue().toStringUtf8());
  }

  @NotNull
  private MessageHeaderParameters getMessageHeaderParameters() {
    MessageHeaderParameters messageHeaderParameters = new MessageHeaderParameters();
    messageHeaderParameters.setApplicationMessageId("1");
    messageHeaderParameters.setApplicationMessageSeqNo(1);
    messageHeaderParameters.setTechnicalMessageType(TechnicalMessageType.DKE_CAPABILITIES);
    messageHeaderParameters.setMode(Request.RequestEnvelope.Mode.DIRECT);
    return messageHeaderParameters;
  }

  @NotNull
  private PayloadParameters getPayloadParameters(ByteString toSendMessage) {
    PayloadParameters payloadParameters = new PayloadParameters();
    payloadParameters.setTypeUrl(
        Capabilities.CapabilitySpecification.getDescriptor().getFullName());
    payloadParameters.setValue(toSendMessage);
    return payloadParameters;
  }
}
