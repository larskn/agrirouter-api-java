package com.dke.data.agrirouter.impl.onboard.cloud;

import static com.dke.data.agrirouter.impl.messaging.rest.MessageFetcher.DEFAULT_INTERVAL;
import static com.dke.data.agrirouter.impl.messaging.rest.MessageFetcher.MAX_TRIES_BEFORE_FAILURE;

import agrirouter.cloud.registration.CloudVirtualizedAppRegistration;
import agrirouter.commons.MessageOuterClass;
import agrirouter.request.Request;
import agrirouter.response.Response;
import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.dto.encoding.EncodeMessageResponse;
import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.dke.data.agrirouter.api.exception.CouldNotOffboardVirtualCommunicationUnitException;
import com.dke.data.agrirouter.api.exception.CouldNotOnboardVirtualCommunicationUnitException;
import com.dke.data.agrirouter.api.factories.impl.CloudEndpointOffboardingMessageContentFactory;
import com.dke.data.agrirouter.api.factories.impl.CloudEndpointOnboardingMessageContentFactory;
import com.dke.data.agrirouter.api.factories.impl.parameters.CloudEndpointOffboardingMessageParameters;
import com.dke.data.agrirouter.api.factories.impl.parameters.CloudEndpointOnboardingMessageParameters;
import com.dke.data.agrirouter.api.service.messaging.FetchMessageService;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.dke.data.agrirouter.api.service.messaging.encoding.EncodeMessageService;
import com.dke.data.agrirouter.api.service.onboard.cloud.OnboardingService;
import com.dke.data.agrirouter.api.service.parameters.*;
import com.dke.data.agrirouter.impl.common.MessageIdService;
import com.dke.data.agrirouter.impl.messaging.encoding.DecodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.encoding.EncodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.rest.FetchMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.rest.MessageSender;
import com.dke.data.agrirouter.impl.validation.ResponseValidator;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.http.HttpStatus;

public class OnboardingServiceImpl implements OnboardingService, MessageSender, ResponseValidator {

  private final EncodeMessageService encodeMessageService;
  private final FetchMessageService fetchMessageService;
  private final DecodeMessageService decodeMessageService;

  public OnboardingServiceImpl() {
    this.encodeMessageService = new EncodeMessageServiceImpl();
    this.fetchMessageService = new FetchMessageServiceImpl();
    this.decodeMessageService = new DecodeMessageServiceImpl();
  }

  /**
   * Onboarding a virtual CU for an existing cloud application (incl. several checks).
   *
   * @param parameters Parameters for the onboarding.
   * @return -
   */
  @Override
  public List<OnboardingResponse> onboard(CloudOnboardingParameters parameters) {
    parameters.validate();
    EncodeMessageResponse encodedMessageResponse = this.encodeOnboardingMessage(parameters);
    SendMessageParameters sendMessageParameters =
        createSendMessageParameters(encodedMessageResponse, parameters.getOnboardingResponse());
    Optional<List<FetchMessageResponse>> fetchMessageResponses =
        sendMessageAndFetchResponses(sendMessageParameters, parameters.getOnboardingResponse());

    List<OnboardingResponse> responses = new ArrayList<>();
    if (fetchMessageResponses.isPresent()) {
      DecodeMessageResponse decodedMessageQueryResponse =
          this.decodeMessageService.decode(
              fetchMessageResponses.get().get(0).getCommand().getMessage());
      if (decodedMessageQueryResponse.getResponseEnvelope().getResponseCode()
          == HttpStatus.SC_BAD_REQUEST) {
        MessageOuterClass.Message message =
            this.decodeMessageService.decode(
                decodedMessageQueryResponse.getResponsePayloadWrapper().getDetails().getValue());
        throw new CouldNotOnboardVirtualCommunicationUnitException(message.getMessage());
      } else {
        if (decodedMessageQueryResponse.getResponseEnvelope().getType()
                == Response.ResponseEnvelope.ResponseBodyType.CLOUD_REGISTRATIONS
            && decodedMessageQueryResponse.getResponseEnvelope().getResponseCode()
                == HttpStatus.SC_CREATED) {
          CloudVirtualizedAppRegistration.OnboardingResponse onboardingResponse =
              this.decode(
                  decodedMessageQueryResponse.getResponsePayloadWrapper().getDetails().getValue());
          onboardingResponse
              .getOnboardedEndpointsList()
              .forEach(
                  endpointRegistrationDetails -> {
                    OnboardingResponse internalOnboardingResponse = new OnboardingResponse();
                    internalOnboardingResponse.setSensorAlternateId(
                        endpointRegistrationDetails.getSensorAlternateId());
                    internalOnboardingResponse.setCapabilityAlternateId(
                        endpointRegistrationDetails.getCapabilityAlternateId());
                    internalOnboardingResponse.setDeviceAlternateId(
                        endpointRegistrationDetails.getDeviceAlternateId());
                    internalOnboardingResponse.setAuthentication(
                        parameters.getOnboardingResponse().getAuthentication());
                    internalOnboardingResponse.setConnectionCriteria(
                        parameters.getOnboardingResponse().getConnectionCriteria());
                    responses.add(internalOnboardingResponse);
                  });
        }
      }
    }
    return responses;
  }

  /**
   * Offboarding a virtual CU. Will deliver no result if the action was successful, if there's any
   * error an exception will be thrown.
   *
   * @param parameters Parameters for offboarding.
   */
  @Override
  public void offboard(CloudOffboardingParameters parameters) {
    parameters.validate();
    EncodeMessageResponse encodedMessageResponse = this.encodeOffboardingMessage(parameters);
    SendMessageParameters sendMessageParameters =
        createSendMessageParameters(encodedMessageResponse, parameters.getOnboardingResponse());
    Optional<List<FetchMessageResponse>> fetchMessageResponses =
        sendMessageAndFetchResponses(sendMessageParameters, parameters.getOnboardingResponse());
    if (fetchMessageResponses.isPresent()) {
      DecodeMessageResponse decodedMessageQueryResponse =
          this.decodeMessageService.decode(
              fetchMessageResponses.get().get(0).getCommand().getMessage());
      if (decodedMessageQueryResponse.getResponseEnvelope().getResponseCode()
          == HttpStatus.SC_BAD_REQUEST) {
        MessageOuterClass.Message message =
            this.decodeMessageService.decode(
                decodedMessageQueryResponse.getResponsePayloadWrapper().getDetails().getValue());
        throw new CouldNotOffboardVirtualCommunicationUnitException(message.getMessage());
      }
    }
  }

  private Optional<List<FetchMessageResponse>> sendMessageAndFetchResponses(
      SendMessageParameters sendMessageParameters, OnboardingResponse onboardingResponse) {
    MessageSenderResponse response = this.sendMessage(sendMessageParameters);
    this.assertResponseStatusIsValid(response.getNativeResponse(), HttpStatus.SC_OK);
    return this.fetchMessageService.fetch(
        onboardingResponse, MAX_TRIES_BEFORE_FAILURE, DEFAULT_INTERVAL);
  }

  private EncodeMessageResponse encodeOnboardingMessage(CloudOnboardingParameters parameters) {
    final String applicationMessageID = MessageIdService.generateMessageId();

    List<CloudEndpointOnboardingMessageParameters> onboardCloudEndpointMessageParameters =
        new ArrayList<>();
    parameters
        .getEndpointDetails()
        .forEach(
            endpointDetailsParameters -> {
              CloudEndpointOnboardingMessageParameters onboardCloudEndpointMessageParameter =
                  new CloudEndpointOnboardingMessageParameters();
              onboardCloudEndpointMessageParameter.setEndpointId(
                  endpointDetailsParameters.getEndpointId());
              onboardCloudEndpointMessageParameter.setEndpointName(
                  endpointDetailsParameters.getEndpointName());
              onboardCloudEndpointMessageParameters.add(onboardCloudEndpointMessageParameter);
            });

    PayloadParameters payloadParameters = new PayloadParameters();
    payloadParameters.setTypeUrl(
        CloudVirtualizedAppRegistration.OnboardingRequest.getDescriptor().getFullName());
    payloadParameters.setValue(
        new CloudEndpointOnboardingMessageContentFactory()
            .message(
                onboardCloudEndpointMessageParameters.toArray(
                    new CloudEndpointOnboardingMessageParameters
                        [onboardCloudEndpointMessageParameters.size()])));

    String encodedMessage =
        this.encodeMessageService.encode(
            this.createMessageHeaderParameters(applicationMessageID), payloadParameters);
    return new EncodeMessageResponse(applicationMessageID, encodedMessage);
  }

  private EncodeMessageResponse encodeOffboardingMessage(CloudOffboardingParameters parameters) {
    final String applicationMessageID = MessageIdService.generateMessageId();

    CloudEndpointOffboardingMessageParameters cloudOffboardingParameters =
        new CloudEndpointOffboardingMessageParameters();
    cloudOffboardingParameters.setEndpointIds(new ArrayList<>());
    parameters
        .getEndpointIds()
        .forEach(
            endpointId -> {
              cloudOffboardingParameters.getEndpointIds().add(endpointId);
            });

    PayloadParameters payloadParameters = new PayloadParameters();
    payloadParameters.setTypeUrl(
        CloudVirtualizedAppRegistration.OffboardingRequest.getDescriptor().getFullName());

    payloadParameters.setValue(
        new CloudEndpointOffboardingMessageContentFactory().message(cloudOffboardingParameters));

    String encodedMessage =
        this.encodeMessageService.encode(
            this.createMessageHeaderParameters(applicationMessageID), payloadParameters);
    return new EncodeMessageResponse(applicationMessageID, encodedMessage);
  }

  private MessageHeaderParameters createMessageHeaderParameters(String applicationMessageID) {
    MessageHeaderParameters messageHeaderParameters = new MessageHeaderParameters();
    messageHeaderParameters.setApplicationMessageId(applicationMessageID);
    messageHeaderParameters.setTechnicalMessageType(
        TechnicalMessageType.DKE_CLOUD_ONBOARD_ENDPOINTS);
    messageHeaderParameters.setMode(Request.RequestEnvelope.Mode.DIRECT);
    return messageHeaderParameters;
  }

  private SendMessageParameters createSendMessageParameters(
      EncodeMessageResponse encodedMessageResponse, OnboardingResponse onboardingResponse) {
    SendMessageParameters sendMessageParameters = new SendMessageParameters();
    sendMessageParameters.setOnboardingResponse(onboardingResponse);
    sendMessageParameters.setEncodedMessages(
        Collections.singletonList(encodedMessageResponse.getEncodedMessage()));
    return sendMessageParameters;
  }

  @Override
  public CloudVirtualizedAppRegistration.OnboardingResponse unsafeDecode(ByteString message)
      throws InvalidProtocolBufferException {
    return CloudVirtualizedAppRegistration.OnboardingResponse.parseFrom(message);
  }
}
