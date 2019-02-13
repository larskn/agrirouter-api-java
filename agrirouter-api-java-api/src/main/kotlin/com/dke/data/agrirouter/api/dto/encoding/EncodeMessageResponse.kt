package com.dke.data.agrirouter.api.dto.encoding

import com.sap.iotservices.common.protobuf.gateway.MeasureRequestMessageProtos

/**
 * This class holds the data to be returned from an message encoding function
 *
 * @param applicationMessageID the generated application message ID
 * @param encodedMessageBase64 the encoded message as Base64-String
 * @param encodedMessageProtobuf the encoded message as Protobuf
 *
 */
data class EncodeMessageResponse(
        val applicationMessageID: String,
        val encodedMessageBase64: String,
        val encodedMessageProtobuf: MeasureRequestMessageProtos.MeasureRequestMessage
)