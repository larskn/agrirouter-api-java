package com.dke.data.agrirouter.api.service.messaging.encoding;

import com.dke.data.agrirouter.api.dto.encoding.EncodeMessageResponse;
import com.dke.data.agrirouter.api.service.parameters.MessageHeaderParameters;
import com.dke.data.agrirouter.api.service.parameters.PayloadParameters;
import com.sap.iotservices.common.protobuf.gateway.MeasureProtos;
import com.sap.iotservices.common.protobuf.gateway.MeasureRequestMessageProtos;

/** Encoding of messages. */
public interface EncodeMessageService {

  /**
   * Encode a given message using the internal protobuf encoding mechanism.
   *
   * @param messageHeaderParameters -
   * @param payloadParameters -
   * @return -
   */
  EncodeMessageResponse encode(
      MessageHeaderParameters messageHeaderParameters, PayloadParameters payloadParameters);


}
