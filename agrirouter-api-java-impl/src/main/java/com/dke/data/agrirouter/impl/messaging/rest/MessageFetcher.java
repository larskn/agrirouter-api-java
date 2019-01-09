package com.dke.data.agrirouter.impl.messaging.rest;

import agrirouter.request.Request;
import com.dke.data.agrirouter.api.enums.CertificationType;
import com.dke.data.agrirouter.api.service.parameters.FetchMessageParameters;
import com.dke.data.agrirouter.impl.RequestFactory;
import com.dke.data.agrirouter.impl.validation.ResponseValidator;
import java.util.Optional;
import javax.print.attribute.standard.Media;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

public interface MessageFetcher extends ResponseValidator {

  int MAX_TRIES_BEFORE_FAILURE = 10;
  long DEFAULT_INTERVAL = 500;

  String EMPTY_CONTENT = "[]";

  default void setResponseFormatJSON(){
    RequestFactory.setRequestFormatJSON();
  }

  default void setResponseFormatProtobuf(){
    RequestFactory.setRequestFormatProtobuf();
  }

  default MediaType getResponseFormat(){
    return  RequestFactory.getMediaType();
  }

  default Optional<byte[]> poll(FetchMessageParameters parameters, int maxTries, long interval) {
    parameters.validate();
    int nrOfTries = 0;
    while (nrOfTries < maxTries) {
      Response response =
          RequestFactory.securedRequest(
                  parameters.getOnboardingResponse().getConnectionCriteria().getCommands(),
                  parameters.getOnboardingResponse().getAuthentication().getCertificate(),
                  parameters.getOnboardingResponse().getAuthentication().getSecret(),
                  CertificationType.valueOf(
                      parameters.getOnboardingResponse().getAuthentication().getType()))
              .get();
      this.assertResponseStatusIsValid(response, HttpStatus.SC_OK);
      byte[] entityContent = response.readEntity(byte[].class);
      if(RequestFactory.getMediaType() == MediaType.APPLICATION_JSON_TYPE){
        String entityString = String.valueOf(entityContent);
        if (!StringUtils.equalsIgnoreCase(entityString, EMPTY_CONTENT)) {
          return Optional.of(entityContent);
        }
      }
      else {
        if(!((entityContent == null)|| (entityContent.length == 0))) {
          return Optional.of(entityContent);
        }
      }
      nrOfTries++;
      try {
        Thread.sleep(interval);
      } catch (InterruptedException nop) {
        // NOP
      }
    }
    return Optional.empty();
  }
}
