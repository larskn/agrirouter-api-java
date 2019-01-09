import agrirouter.request.payload.account.Endpoints;
import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.env.QA;
import com.dke.data.agrirouter.api.service.messaging.EndpointsUnfilteredListService;
import com.dke.data.agrirouter.api.service.messaging.FetchMessageService;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.dke.data.agrirouter.api.service.parameters.EndpointsUnfilteredMessageParameters;
import com.dke.data.agrirouter.impl.messaging.encoding.DecodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.rest.EndpointsUnfilteredListServiceImpl;
import com.dke.data.agrirouter.impl.messaging.rest.FetchMessageServiceImpl;
import com.google.gson.Gson;

import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args){
        Environment environment = new QA() {
            @Override
            public String getAgrirouterLoginUsername() {
                return null;
            }

            @Override
            public String getAgrirouterLoginPassword() {
                return null;
            }
        };

        FetchMessageServiceImpl fetchMessageService;
        Optional<List<FetchMessageResponse>> fetchMessageResponseListOptional;


        String onboardingString = "{\n" +
                    "    \"deviceAlternateId\": \"5cfd1117-ed2f-4f76-bca7-c8ae0e50665d\",\n" +
                    "    \"capabilityAlternateId\": \"79dfd918-7051-471a-9b73-3f3c23deca38\",\n" +
                    "    \"sensorAlternateId\": \"82a8bc23-7cc2-431a-b77e-0b74f9a53cb7\",\n" +
                    "    \"connectionCriteria\": {\n" +
                    "        \"gatewayId\": \"3\",\n" +
                    "        \"measures\": \"https://dke-qa.eu1.cp.iot.sap/iot/gateway/rest/measures/5cfd1117-ed2f-4f76-bca7-c8ae0e50665d\",\n" +
                    "        \"commands\": \"https://dke-qa.eu1.cp.iot.sap/iot/gateway/rest/commands/5cfd1117-ed2f-4f76-bca7-c8ae0e50665d\"\n" +
                    "    },\n" +
                    "    \"authentication\": {\n" +
                    "        \"type\": \"PEM\",\n" +
                    "        \"secret\": \"9edkUYZ7jpl6F4onF7AT1QdLBCFKImXdyobA\",\n" +
                    "        \"certificate\": \"-----BEGIN ENCRYPTED PRIVATE KEY-----\\nMIIE6zAdBgoqhkiG9w0BDAEDMA8ECEeWZZWF1M2yAgMCAAAEggTIPtKb5rshhOGG\\nCP0tLRH2AHbQ3WZoGrwCnq62cdPk1KVRbX3jUw09T67zfMtI8/KmAQmcuCQ29xZJ\\n5dvp6IRJPGNj8UH+hIhjhU6GZkdb+nmgEt6nirEEp5bDTvldAW3MeWpMmtbhZTqJ\\nEaECs7dRZcwf88FXdR8tO/mmlPf+hE1MN32fym4NWjbv7NTmHaUzX1nufu0qdVyh\\nnNL8xK3fk4a+HP/sfHI5PxO+qOotQmoK2bCa9ztG0vQPswKba3ig1pxAQieMoerW\\n9WZmLJhf6CczSao9xk/A7ACCJOZx64sgLhMQjauDu+761/FRHKqWTaDf+tJJHGc8\\npsGmA4u8z+JFWG6Y2u72QQ+qTr7nyBnnTpjqJXO5I2q6seYR1vmRGoCezqPJTNag\\nhn/8attDB9izQBnkpRW71ZwHSRIZseec9D5WvJqv9ZdQ92tEsTkpRmaaMYmXjztV\\nK4mG4xmv83f/ODG/KCwpsMfC2IbkFW3/23OFPjcsCcT76fD5BaHV5YfDuf7q0WRC\\n0Q522/JL3afUMpjBEGuw+KN3xlZqORyAnjUr3GBHzvStIgMdRJSvCDyhC+01hOtZ\\na2OFd5zqw+qMtuX+mOrMt04x3FlWKVAEP2EJLT0/CiXQskjWEGroHSiz+tq0Np82\\ny98AcFbbl7qLogWOo267+tVznN4206rQL0+bqeWli/toYSu2OXrQbeu64VkWkLCz\\ns/TEK+WKpaEF1MjkGIwlzm1KUmQ7+ls6V/apKHDCq7TC0YP7okF+1tWPQrKerYv4\\nlTkey5b0n++V7UJIkZCAMAWPpLaQFwBqHrIwCqJ95Z1zysy+L4WJBRegOr8mqZC6\\na/+Tk87Wvgx9VsuZ3O37p9TdeWhOg2sh1JvZ5WIU+3jk/kppGn4aNAQjErKgwlBH\\nMlBzRpzkUCOBA48hufBi6lUJPr6el9Le7HQTcle77WvIFjrMyMBdPnn7WG2bzgYb\\nblm+sq+TBqNp7tE+5KSH1lRP+6hp9BGXkZoyBYFAPOC5hhhwwdZxEROz5kI6pYS5\\n29v52ClVc2FQoc+GZOyoOpEADaDvfUV6tkE9oCQVkE829gOIHrHbyUdjjYavauiQ\\n8YA5maiqqG4a/mpuf6jMHDnlbqHXUyiwpZ6PceGYw/2ZGa6nx42JIgXh2CEVbGIo\\nwAuhxm9QjAvi1IJl897epwBso63/fQ3joVIqm3F8ZPonu/TDWEW04JlYFf4faAPE\\nVNr+etubV8GjamZkuWsboTvy32RVRtOxILTgMQ63ssFZs1oyC7QoZ/Ovvv8/ODh/\\n1NqLsZsiJbNIDqp/WXXqqQdBYAp0Mc2+C8VVz1hbD5R3UfvnhYjCKDnNqVz4ZiAC\\nq5slKg4Tj8HUfwnLDP3UExBP5geKcpFNoaxH1Tu1kJ32E8/ntjChFhqJC1h/ZrhK\\n3x8j5uN8VyUMu3cOQLRRfHCaowKBiZsRE/PGJAcz7PmVn/NNj0aRDkeXlMvImmYy\\ntw2JKJag23rfUT1WPORI6tt52MLWkfPsSSIVE93xDJom8MQPV/qudSgzO9wFJ2+H\\nKKhQslMdYDHD8TvHH5nFKI3UyQNup661ZMfvzhjtKQXixMgLgppRtSdFZg/EZ8bS\\n/Tu8hiCuGObJMJBLptqe\\n-----END ENCRYPTED PRIVATE KEY-----\\n-----BEGIN CERTIFICATE-----\\nMIIEaDCCA1CgAwIBAgIPAKOIMk/828bVEAECODTsMA0GCSqGSIb3DQEBCwUAMFYx\\nCzAJBgNVBAYTAkRFMSMwIQYDVQQKExpTQVAgSW9UIFRydXN0IENvbW11bml0eSBJ\\nSTEiMCAGA1UEAxMZU0FQIEludGVybmV0IG9mIFRoaW5ncyBDQTAeFw0xODEyMTgx\\nMTQzNDdaFw0xOTEyMTgxMTQzNDdaMIG1MQswCQYDVQQGEwJERTEcMBoGA1UEChMT\\nU0FQIFRydXN0IENvbW11bml0eTEVMBMGA1UECxMMSW9UIFNlcnZpY2VzMXEwbwYD\\nVQQDFGhkZXZpY2VBbHRlcm5hdGVJZDo1Y2ZkMTExNy1lZDJmLTRmNzYtYmNhNy1j\\nOGFlMGU1MDY2NWR8Z2F0ZXdheUlkOjN8dGVuYW50SWQ6MTE1MDcwNzc2MHxpbnN0\\nYW5jZUlkOmRrZS1xYTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAIPw\\nKQHeP/YngXa3qB2mE8J+gOJz00G4ADKbRI3px7uFfvmDjiVG1fW1dN8mVuvJxGPE\\nbQpjcrvum1d9InISrvyX67fL4zusO8tNyBfS+Tr1Pvks5GoAA4ZPA3nGG4xSLGMV\\nBp+qDFxTsiH5gtFpwtAaHDwZHOD0FtbGRHMxbGq9Wg4vrrfXGGoPLOemfTwOjoMy\\nioXx+hMwnWgXtV5f5w/8Vbkt1kDGBdRNJuyUncnR4Mzz1w8YUHm/VrG0YgLk0iZp\\nVYAcNPcFytoy0KGZbDvYx0CMH93AL6TgOlrm9xancNNl/vfICCAhEK/TDQVzzARz\\nh23SkOPEP7Da7kGJ1vcCAwEAAaOB0jCBzzBIBgNVHR8EQTA/MD2gO6A5hjdodHRw\\nczovL3Rjcy5teXNhcC5jb20vY3JsL1RydXN0Q29tbXVuaXR5SUkvU0FQSW9UQ0Eu\\nY3JsMAwGA1UdEwEB/wQCMAAwJQYDVR0SBB4wHIYaaHR0cDovL3NlcnZpY2Uuc2Fw\\nLmNvbS9UQ1MwDgYDVR0PAQH/BAQDAgbAMB0GA1UdDgQWBBRorHVLk3Mh+Dv15LxA\\nNMwwKSATgDAfBgNVHSMEGDAWgBSVt7P1WN7VtLNYRuDypsl4Tr0tdTANBgkqhkiG\\n9w0BAQsFAAOCAQEA7M3UXOFT0af787g80nQ7CXf6L3lUWvEEApL8fGvBCC36upsw\\nW2IMRLq3CiAtF3qxVZMP9prSN0Y6Oj8gIALaVqiDjMNH5rucO9v5fNtmqsSEkSGD\\nFEbx71ev++elVHTGQmZOjVsq3ox2Lc6Pygq/C3YEyTfTf3a6goBbOiKd72bHgE0f\\nN3NDyGRGnYzX9fWtOUBgBVHwXZLjoIojhI6uISWpB/aXq0biFmty/DDnYH3Dqiy7\\nsF4n1MUN8QIVGQiPRvLNvNaeApVN8WJOwp2xnLWPfwg8gVv6Zv0HsbW8R/DNT9Iq\\nUlYvi7QooEtqBovBTAo+xoDtOnNe8tCm5yBTFA==\\n-----END CERTIFICATE-----\\n\"\n" +
                    "    }\n" +
                    "}";

        Gson gson = new Gson();
        OnboardingResponse onboardingResponse = gson.fromJson(onboardingString,OnboardingResponse.class);
        //System.out.println(gson.toJson(onboardingResponse));


        EndpointsUnfilteredMessageParameters unfilteredMessageParameters = new EndpointsUnfilteredMessageParameters();
        unfilteredMessageParameters.setOnboardingResponse(onboardingResponse);
        unfilteredMessageParameters.setDirection(Endpoints.ListEndpointsQuery.Direction.SEND_RECEIVE);
        unfilteredMessageParameters.setTechnicalMessageType(TechnicalMessageType.IMG_JPEG);

        EndpointsUnfilteredListService unfilteredEndpointListService = new EndpointsUnfilteredListServiceImpl(environment);

        unfilteredEndpointListService.send(unfilteredMessageParameters);

        fetchMessageService = new FetchMessageServiceImpl();
        //((FetchMessageServiceImpl) fetchMessageService).setResponseFormatJSON();
        fetchMessageResponseListOptional = fetchMessageService.fetch(onboardingResponse,5,2000);
        if(fetchMessageResponseListOptional.isPresent()){
            List<FetchMessageResponse> fetchMessageResponseList = fetchMessageResponseListOptional.get();
            DecodeMessageService decodeMessageService = new DecodeMessageServiceImpl();
            for(FetchMessageResponse fetchMessageResponse: fetchMessageResponseList) {
                DecodeMessageResponse message = decodeMessageService.decode(fetchMessageResponse.getCommand().getMessage());
                System.out.println("JSON Result: " + message.toString());
                //We do not decode, because this breaks!
                // DecodeMessageResponse value = decodeMessageService.decode(fetchMessageResponse.getCommand().getMessage());
                System.out.println("MessageType: "+ message.getResponseEnvelope().getType().getDescriptorForType().getFullName());
            }
        }
        //Step 1: Get Answer in RAW Protobuf

        unfilteredEndpointListService.send(unfilteredMessageParameters);

        fetchMessageService = new FetchMessageServiceImpl();
        fetchMessageService.setResponseFormatProtobuf();
        fetchMessageResponseListOptional = fetchMessageService.fetch(onboardingResponse,5,2000);
        if(fetchMessageResponseListOptional.isPresent()){
            List<FetchMessageResponse> fetchMessageResponseList = fetchMessageResponseListOptional.get();
            for(FetchMessageResponse fetchMessageResponse: fetchMessageResponseList) {
                DecodeMessageService decodeMessageService = new DecodeMessageServiceImpl();
                System.out.println("Raw Protobuf Result: " + fetchMessageResponse.getCommand().getMessage());
                //We do not decode, because this breaks!
                // DecodeMessageResponse value = decodeMessageService.decode(fetchMessageResponse.getCommand().getMessage());
                //System.out.println("MessageType: "+ value.getResponseEnvelope().getType().getDescriptorForType().getFullName());
            }
        }
        //Step 2: Get Answer in JSON

    }



}
