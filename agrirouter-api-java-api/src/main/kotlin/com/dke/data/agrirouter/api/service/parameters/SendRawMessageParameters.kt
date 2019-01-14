package com.dke.data.agrirouter.api.service.parameters

import agrirouter.request.Request
import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse
import com.dke.data.agrirouter.api.enums.TechnicalMessageType
import com.dke.data.agrirouter.api.service.ParameterValidation
import com.google.protobuf.ByteString

class SendRawMessageParameters : ParameterValidation {
    var technicalMessageType: TechnicalMessageType? = null
    var mode: Request.RequestEnvelope.Mode? = null
    var receipients: MutableList<String> = mutableListOf<String>()
    var teamSetContextId: String? = null
    var rawData: ByteArray = ByteArray(0);
    var typeURL: String? = null
    var onBoardingResponse: OnboardingResponse? = null

    fun addReceipient(receipient:String){
        receipients.add(receipient)
    }
}
