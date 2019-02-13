package com.dke.data.agrirouter.impl.messaging.encoding;

import static com.dke.data.agrirouter.impl.RequestFactory.MEDIA_TYPE_PROTOBUF;

import agrirouter.request.Request;
import com.dke.data.agrirouter.api.dto.encoding.EncodeMessageResponse;
import com.dke.data.agrirouter.api.exception.CouldNotEncodeMessageException;
import com.dke.data.agrirouter.api.service.messaging.encoding.EncodeMessageService;
import com.dke.data.agrirouter.api.service.parameters.MessageHeaderParameters;
import com.dke.data.agrirouter.api.service.parameters.PayloadParameters;
import com.dke.data.agrirouter.api.util.TimestampUtil;
import com.dke.data.agrirouter.impl.NonEnvironmentalService;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.sap.iotservices.common.protobuf.gateway.MeasureRequestMessageProtos;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;

/** Internal service implementation. */
public class EncodeMessageServiceImpl extends NonEnvironmentalService
    implements EncodeMessageService {

  private MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;

  public void setRequestFormatJSON() {
    mediaType = MediaType.APPLICATION_JSON_TYPE;
  }

  public void setRequestFormatProtobuf() {
    mediaType = MEDIA_TYPE_PROTOBUF;
  }

  public MediaType getRequestFormat() {
    return mediaType;
  }

  public EncodeMessageResponse encode(
      MessageHeaderParameters messageHeaderParameters, PayloadParameters payloadParameters) {

    if (null == messageHeaderParameters || null == payloadParameters) {
      throw new IllegalArgumentException("Parameters cannot be NULL");
    }
    messageHeaderParameters.validate();
    payloadParameters.validate();

    try (ByteArrayOutputStream streamedMessage = new ByteArrayOutputStream()) {

      this.getNativeLogger().trace("Encode header.");
      this.header(messageHeaderParameters).writeDelimitedTo(streamedMessage);

      this.getNativeLogger().trace("Encode payload.");
      this.payload(payloadParameters).writeDelimitedTo(streamedMessage);

      this.getNativeLogger().trace("Encoding message.");
      byte[] encodedByteArray = streamedMessage.toByteArray();

      String encodedMessageBase64 = "";
      MeasureRequestMessageProtos.MeasureRequestMessage measureMessageProtobuf = null;
      if (getRequestFormat() == MediaType.APPLICATION_JSON_TYPE) {
        encodedMessageBase64 = Base64.getEncoder().encodeToString(encodedByteArray);
        this.logMethodEnd(encodedMessageBase64);
      } else {
        MeasureRequestMessageProtos.MeasureRequestMessage.Builder measureRequestBuilder =
            MeasureRequestMessageProtos.MeasureRequestMessage.newBuilder();

        measureRequestBuilder.setMessage(ByteString.copyFrom(encodedByteArray));
        measureMessageProtobuf = measureRequestBuilder.build();
      }

      return new EncodeMessageResponse(
          messageHeaderParameters.applicationMessageId,
          encodedMessageBase64,
          measureMessageProtobuf);

    } catch (IOException e) {
      throw new CouldNotEncodeMessageException(e);
    }
  }

  private Request.RequestEnvelope header(MessageHeaderParameters parameters) {
    this.logMethodBegin(parameters);

    this.getNativeLogger().trace("Create message header.");
    agrirouter.request.Request.RequestEnvelope.Builder messageHeader =
        Request.RequestEnvelope.newBuilder();
    messageHeader.setApplicationMessageId(parameters.getApplicationMessageId());
    messageHeader.setApplicationMessageSeqNo(parameters.getApplicationMessageSeqNo());
    messageHeader.setTechnicalMessageType(parameters.getTechnicalMessageType().getKey());
    messageHeader.setMode(parameters.getMode());
    if (StringUtils.isNotBlank(parameters.getTeamSetContextId())) {
      messageHeader.setTeamSetContextId(parameters.getTeamSetContextId());
    }
    if (!parameters.getRecipients().isEmpty()) {
      messageHeader.addAllRecipients(parameters.getRecipients());
    }
    if (parameters.getChunkInfo() != null) {
      messageHeader.setChunkInfo(parameters.getChunkInfo());
    }
    messageHeader.setTimestamp(new TimestampUtil().current());

    this.getNativeLogger().trace("Build message envelope.");
    Request.RequestEnvelope requestEnvelope = messageHeader.build();

    this.logMethodEnd(requestEnvelope);
    return requestEnvelope;
  }

  private Request.RequestPayloadWrapper payload(PayloadParameters parameters) {
    this.logMethodBegin(parameters);

    this.getNativeLogger().trace("Create message payload.");
    Request.RequestPayloadWrapper.Builder messagePayload =
        Request.RequestPayloadWrapper.newBuilder();
    Any.Builder builder = Any.newBuilder();
    builder.setTypeUrl(parameters.getTypeUrl());
    builder.setValue(parameters.getValue());
    messagePayload.setDetails(builder.build());

    this.getNativeLogger().trace("Message message payload wrapper.");
    Request.RequestPayloadWrapper requestPayloadWrapper = messagePayload.build();

    this.logMethodEnd(requestPayloadWrapper);
    return requestPayloadWrapper;
  }
}
