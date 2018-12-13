package com.dke.data.agrirouter.api.factories.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.dke.data.agrirouter.api.exception.IllegalParameterDefinitionException;
import com.dke.data.agrirouter.api.factories.impl.parameters.SubscriptionMessageParameters;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SubscriptionMessageContentFactoryTest
    extends AbstractMessageContentFactoryTest<SubscriptionMessageContentFactory> {

  @Test
  void givenValidSubscriptionMessageParametersMessageShouldNotFail() {
    SubscriptionMessageParameters subscriptionMessageParameters =
        new SubscriptionMessageParameters();
    List<Integer> ddis = new ArrayList<>();
    ddis.add(1);
    subscriptionMessageParameters.ddis = ddis;
    subscriptionMessageParameters.setTechnicalMessageType(
        TechnicalMessageType.ISO_11783_TASKDATA_ZIP);
    subscriptionMessageParameters.setPosition(true);
    ByteString message = this.getInstanceToTest().message(subscriptionMessageParameters);
    assertFalse(message.isEmpty());
  }

  @Test
  void givenEmptySubscriptionMessageParametersMessageShouldThrowException() {
    SubscriptionMessageParameters subscriptionMessageParameters =
        new SubscriptionMessageParameters();
    assertThrows(
        IllegalParameterDefinitionException.class,
        () -> this.getInstanceToTest().message(subscriptionMessageParameters));
  }

  @Override
  protected SubscriptionMessageContentFactory getInstanceToTest() {
    return new SubscriptionMessageContentFactory();
  }
}
