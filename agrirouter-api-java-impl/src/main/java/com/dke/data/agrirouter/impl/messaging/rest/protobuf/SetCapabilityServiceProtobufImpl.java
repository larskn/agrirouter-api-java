package com.dke.data.agrirouter.impl.messaging.rest.protobuf;

import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.impl.messaging.encoding.protobuf.EncodeMessageServiceProtobufImpl;
import com.dke.data.agrirouter.impl.messaging.rest.SetCapabilityServiceImpl;
import com.sap.iotservices.common.protobuf.gateway.MeasureProtos;

public class SetCapabilityServiceProtobufImpl
    extends SetCapabilityServiceImpl<MeasureProtos.MeasureRequest.Measure> {
  public SetCapabilityServiceProtobufImpl(Environment environment) {
    super(environment, new EncodeMessageServiceProtobufImpl(), new MessageSenderProtobufImpl());
  }
}
