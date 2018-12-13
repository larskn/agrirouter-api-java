package com.dke.data.agrirouter.api.factories.impl.parameters

import com.dke.data.agrirouter.api.enums.TechnicalMessageType
import com.dke.data.agrirouter.api.service.ParameterValidation
import javax.validation.constraints.NotNull

class SubscriptionMessageParameters : ParameterValidation {

    var technicalMessageType: TechnicalMessageType? = null

    @NotNull
    lateinit var ddis: List<Int>

    var position: Boolean = false

}