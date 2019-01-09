package com.dke.data.agrirouter.impl.messaging.rest;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

import java.util.Collections;
import java.util.UUID;

public class EndpointsUnfilteredListServiceImpl extends EnvironmentalService
        implements EndpointsUnfilteredListService, MessageSender, ResponseValidator {
    private Logger LOGGER = LogManager.getLogger();

    private EncodeMessageService encodeMessageService;
    //private Logger logger;
    public EndpointsUnfilteredListServiceImpl(Environment environment) {
        super(environment);
        encodeMessageService = new EncodeMessageServiceImpl();

    }

    @Override
    public String send(EndpointsUnfilteredMessageParameters parameters){

        TechnicalMessageType technicalMessageType = parameters.technicalMessageType;
        //there is no validation required
        //parameters.validate();

        EncodeMessageResponse encodedMessage = encodeMessage(parameters);

        SendMessageParameters sendMessageParameters = new SendMessageParameters();
        sendMessageParameters.onboardingResponse = parameters.onboardingResponse;
        sendMessageParameters.setEncodedMessages(Collections.singletonList(encodedMessage.getEncodedMessage()));

        sendMessage(sendMessageParameters);

        return encodedMessage.getApplicationMessageID();
    }


    private EncodeMessageResponse encodeMessage(EndpointsUnfilteredMessageParameters parameters){


        String applicationMessageID = UUID.randomUUID().toString();

        MessageHeaderParameters messageHeaderParameters = new MessageHeaderParameters();
        messageHeaderParameters.setApplicationMessageId(applicationMessageID);
        messageHeaderParameters.setApplicationMessageSeqNo(1);
        messageHeaderParameters.technicalMessageType = parameters.technicalMessageType;
        messageHeaderParameters.mode = Request.RequestEnvelope.Mode.DIRECT;

        EndpointsUnfilteredMessageParameters endpointListMessageParameters = new EndpointsUnfilteredMessageParameters();
        endpointListMessageParameters.direction = parameters.direction;
        endpointListMessageParameters.technicalMessageType = parameters.technicalMessageType;
        endpointListMessageParameters.setOnboardingResponse(parameters.onboardingResponse);

        PayloadParameters payloadParameters = new PayloadParameters();
        payloadParameters.setTypeUrl(Endpoints.ListEndpointsQuery.getDescriptor().getFullName());
        payloadParameters.value = new EndpointsUnfilteredMessageContentFactory().message(endpointListMessageParameters);

        String encodedMessage = this.encodeMessageService.encode(messageHeaderParameters, payloadParameters);

        return new EncodeMessageResponse(applicationMessageID,encodedMessage);
    }



}
