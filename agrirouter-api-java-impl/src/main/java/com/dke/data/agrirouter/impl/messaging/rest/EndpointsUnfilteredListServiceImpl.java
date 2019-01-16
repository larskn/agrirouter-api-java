package com.dke.data.agrirouter.impl.messaging.rest;

import static com.dke.data.agrirouter.impl.RequestFactory.MEDIA_TYPE_PROTOBUF;

import agrirouter.request.Request;
import agrirouter.request.payload.account.Endpoints;
import com.dke.data.agrirouter.api.dto.encoding.EncodeMessageResponse;
import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.factories.impl.EndpointsUnfilteredMessageContentFactory;
import com.dke.data.agrirouter.api.service.messaging.EndpointsUnfilteredListService;
import com.dke.data.agrirouter.api.service.messaging.encoding.EncodeMessageService;
import com.dke.data.agrirouter.api.service.parameters.EndpointsUnfilteredMessageParameters;
import com.dke.data.agrirouter.api.service.parameters.MessageHeaderParameters;
import com.dke.data.agrirouter.api.service.parameters.PayloadParameters;
import com.dke.data.agrirouter.api.service.parameters.SendMessageParameters;
import com.dke.data.agrirouter.impl.EnvironmentalService;
import com.dke.data.agrirouter.impl.messaging.encoding.EncodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.validation.ResponseValidator;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EndpointsUnfilteredListServiceImpl extends EnvironmentalService
    implements EndpointsUnfilteredListService, MessageSender, ResponseValidator {
  private Logger LOGGER = LogManager.getLogger();

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

  private EncodeMessageService encodeMessageService;
  // private Logger logger;
  public EndpointsUnfilteredListServiceImpl(Environment environment) {
    super(environment);
    encodeMessageService = new EncodeMessageServiceImpl();
  }

  @Override
  public String send(EndpointsUnfilteredMessageParameters parameters) {

    TechnicalMessageType technicalMessageType = parameters.technicalMessageType;
    // there is no validation required
    // parameters.validate();

    EncodeMessageResponse encodedMessage = encodeMessage(parameters);

    SendMessageParameters sendMessageParameters = new SendMessageParameters();
    sendMessageParameters.onboardingResponse = parameters.onboardingResponse;
    sendMessageParameters.setMessages(encodedMessage);

    sendMessage(sendMessageParameters);

    return encodedMessage.getApplicationMessageID();
  }

  private EncodeMessageResponse encodeMessage(EndpointsUnfilteredMessageParameters parameters) {

    String applicationMessageID = UUID.randomUUID().toString();

    MessageHeaderParameters messageHeaderParameters = new MessageHeaderParameters();
    messageHeaderParameters.setApplicationMessageId(applicationMessageID);
    messageHeaderParameters.setApplicationMessageSeqNo(1);
    messageHeaderParameters.technicalMessageType =
        TechnicalMessageType.DKE_LIST_ENDPOINTS_UNFILTERED;
    messageHeaderParameters.mode = Request.RequestEnvelope.Mode.DIRECT;

    EndpointsUnfilteredMessageParameters endpointListMessageParameters =
        new EndpointsUnfilteredMessageParameters();
    endpointListMessageParameters.direction = parameters.direction;
    endpointListMessageParameters.technicalMessageType = parameters.technicalMessageType;
    endpointListMessageParameters.setOnboardingResponse(parameters.onboardingResponse);

    PayloadParameters payloadParameters = new PayloadParameters();
    payloadParameters.setTypeUrl(Endpoints.ListEndpointsQuery.getDescriptor().getFullName());
    payloadParameters.value =
        new EndpointsUnfilteredMessageContentFactory().message(endpointListMessageParameters);

    EncodeMessageResponse encodedMessage =
        this.encodeMessageService.encode(messageHeaderParameters, payloadParameters);

    return encodedMessage;
  }
}
