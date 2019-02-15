package com.dke.data.agrirouter.api.service.messaging;

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.service.parameters.EndpointsListParameters;

public interface EndpointsListService extends MessagingService<EndpointsListParameters> {

  String requestFullListFiltered(OnboardingResponse onboardingResponse);

  String requestFullListUnFiltered(OnboardingResponse onboardingResponse);

}
