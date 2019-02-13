package com.dke.data.agrirouter.api.service.parameters

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse
import com.dke.data.agrirouter.api.exception.IllegalParameterDefinitionException
import com.dke.data.agrirouter.api.service.ParameterValidation
import com.dke.data.agrirouter.api.dto.encoding.EncodeMessageResponse
import com.sap.iotservices.common.protobuf.gateway.MeasureProtos
import com.sap.iotservices.common.protobuf.gateway.MeasureRequestMessageProtos
import lombok.ToString
import java.lang.IllegalArgumentException
import javax.validation.constraints.NotNull
import javax.ws.rs.core.MediaType


/**
 * Parameters class. Encapsulation for the services.
 */
@ToString
open class SendMessageParameters : ParameterValidation {

    @NotNull
    lateinit var onboardingResponse: OnboardingResponse

    lateinit var encodedMessages: List<String>

    lateinit var measureMessages: List<MeasureRequestMessageProtos.MeasureRequestMessage>

    var mediatype: MediaType =MediaType.APPLICATION_JSON_TYPE

    fun setMessages(encodeMessageResponse:EncodeMessageResponse){
        encodedMessages = listOf<String>(encodeMessageResponse.encodedMessageBase64)
        measureMessages = listOf<MeasureRequestMessageProtos.MeasureRequestMessage>(encodeMessageResponse.encodedMessageProtobuf)
    }

    override fun validate() {
        super.validate()
        if (mediatype == MediaType.APPLICATION_JSON_TYPE){
            if(encodedMessages==null || encodedMessages.isEmpty() ){
                throw IllegalArgumentException("Encoded Message may not be empty, when using MediaType JSON")
            }
        }
        else
        {
            if(measureMessages==null || measureMessages.size == 0 ){
                throw IllegalArgumentException("MeasureMessages may not be empty, when using MediaType PROTOBUF")
            }
        }
    }
}