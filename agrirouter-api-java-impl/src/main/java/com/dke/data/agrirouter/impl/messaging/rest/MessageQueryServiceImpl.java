package com.dke.data.agrirouter.impl.messaging.rest;

import static com.dke.data.agrirouter.impl.RequestFactory.MEDIA_TYPE_PROTOBUF;

import agrirouter.feed.response.FeedResponse;
import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.service.messaging.encoding.MessageDecoder;
import com.dke.data.agrirouter.api.service.parameters.MessageQueryParameters;
import com.dke.data.agrirouter.impl.EnvironmentalService;
import com.dke.data.agrirouter.impl.messaging.encoding.EncodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.helper.MessageQueryService;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import javax.ws.rs.core.MediaType;

public class MessageQueryServiceImpl extends EnvironmentalService
    implements com.dke.data.agrirouter.api.service.messaging.MessageQueryService,
        MessageSender,
        MessageDecoder<FeedResponse.MessageQueryResponse> {

  private final MessageQueryService messageQueryService;

  private MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;

  @Override
  public void setRequestFormatJSON() {
    mediaType = MediaType.APPLICATION_JSON_TYPE;
    this.messageQueryService.setRequestFormatJSON();
  }

  @Override
  public void setRequestFormatProtobuf() {
    mediaType = MEDIA_TYPE_PROTOBUF;
    this.messageQueryService.setRequestFormatProtobuf();
  }

  @Override
  public MediaType getRequestFormat() {
    return mediaType;
  }

  public MessageQueryServiceImpl(Environment environment) {
    super(environment);
    this.messageQueryService =
        new MessageQueryService(
            new EncodeMessageServiceImpl(), TechnicalMessageType.DKE_FEED_MESSAGE_QUERY
        );
    this.setRequestFormatJSON();
  }

  @Override
  public String send(MessageQueryParameters parameters) {
    return this.messageQueryService.send(parameters);
  }

  @Override
  public FeedResponse.MessageQueryResponse unsafeDecode(ByteString message)
      throws InvalidProtocolBufferException {
    return FeedResponse.MessageQueryResponse.parseFrom(message);
  }
}
