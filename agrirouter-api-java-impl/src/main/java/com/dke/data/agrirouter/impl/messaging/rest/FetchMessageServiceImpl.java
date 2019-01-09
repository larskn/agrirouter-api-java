package com.dke.data.agrirouter.impl.messaging.rest;

import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import com.dke.data.agrirouter.api.dto.messaging.inner.Message;
import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.exception.InvalidNativeProtobufException;
import com.dke.data.agrirouter.api.service.messaging.FetchMessageService;
import com.dke.data.agrirouter.api.service.parameters.FetchMessageParameters;
import com.dke.data.agrirouter.impl.RequestFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.*;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.sap.iotservices.common.protobuf.gateway.CommandResponseListProtos;
import com.sap.iotservices.common.protobuf.gateway.CommandResponseListProtos.CommandResponseList;
import com.sap.iotservices.common.protobuf.gateway.CommandResponseMessage;
import com.sap.iotservices.common.protobuf.gateway.CommandResponseMessage.CommandResponseMessageProtos;
import com.sap.iotservices.common.protobuf.gateway.CommandResponseProtos;
import com.sap.iotservices.common.protobuf.gateway.CommandResponseProtos.CommandResponse.Command;
import org.eclipse.paho.client.mqttv3.internal.websocket.Base64;

import java.lang.reflect.Type;
import java.util.*;

public class FetchMessageServiceImpl implements FetchMessageService, MessageFetcher {

    @Override
    public Optional<List<FetchMessageResponse>> fetch(
            OnboardingResponse onboardingResponse, int maxTries, long interval) {
        FetchMessageParameters fetchMessageParameters = new FetchMessageParameters();
        fetchMessageParameters.setOnboardingResponse(onboardingResponse);
        return this.fetch(fetchMessageParameters, maxTries, interval);
    }

    @Override
    public Optional<List<FetchMessageResponse>> fetch(
            FetchMessageParameters parameters, int maxTries, long interval) {
        parameters.validate();
        Optional<byte[]> response = this.poll(parameters, maxTries, interval);
        Optional<List<FetchMessageResponse>> responses = Optional.empty();
        if(this.getResponseFormat() == RequestFactory.MEDIA_TYPE_PROTOBUF){
            if(response.isPresent()) {
                try {
                    List<FetchMessageResponse> responseList = new ArrayList<>();

                    CommandResponseList commandResponseList = CommandResponseList.parseFrom(response.get());
                    for (CommandResponseProtos.CommandResponse commandResponse : commandResponseList.getCommandsList()) {
                        FetchMessageResponse fetchMessageResponse = new FetchMessageResponse();
                        fetchMessageResponse.setCapabilityAlternateId(commandResponse.getCapabilityAlternateId());
                        fetchMessageResponse.setSensorAlternateId(commandResponse.getSensorAlternateId());


                        // Parse command arguments
                        Command commandArguments = commandResponse.getCommand();
                        CommandResponseMessageProtos commandResponseMessage = CommandResponseMessageProtos.parseFrom(commandArguments.getValues(0).getValue());

                        byte[] binaryMessage = commandResponseMessage.getMessage().toByteArray();


                        //Create a Base64-String, so that we do not have to change so many parts of the library.
                        String base64Command = Base64.encodeBytes(binaryMessage);
                        com.dke.data.agrirouter.api.dto.messaging.inner.Message message = new Message();
                        message.setMessage(base64Command);
                        message.setTimestamp("");
                        fetchMessageResponse.setCommand(message);
                        responseList.add(fetchMessageResponse);

                    }
                    responses = Optional.of(responseList);

                } catch (Exception e) {
                    throw new InvalidNativeProtobufException();
                }
            }
        }
        else{
            if(response.isPresent()) {
                String realResponseString = new String(response.get());
                Optional<String> responseString = Optional.of(realResponseString);
                responses = responseString.map(this::parseJson);
            }
        }
        return responses;
    }

    private List<FetchMessageResponse> parseJson(String json) {
        Type type = new TypeToken<List<FetchMessageResponse>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    @Override
    public FetchMessageResponse parseJson(byte[] json) {
        Type type = new TypeToken<FetchMessageResponse>() {}.getType();
        return new Gson().fromJson(new String(json), type);
    }
}
