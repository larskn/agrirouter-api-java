import agrirouter.feed.response.FeedResponse;
import agrirouter.request.Request;
import agrirouter.request.payload.account.Endpoints;
import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.dto.messaging.FetchMessageResponse;
import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.dke.data.agrirouter.api.env.Environment;
import com.dke.data.agrirouter.api.env.QA;
import com.dke.data.agrirouter.api.service.messaging.*;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.dke.data.agrirouter.api.service.parameters.*;
import com.dke.data.agrirouter.api.util.TimestampUtil;
import com.dke.data.agrirouter.impl.messaging.encoding.DecodeMessageServiceImpl;
import com.dke.data.agrirouter.impl.messaging.rest.*;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void getUnfilteredEndpointList(OnboardingResponse onboardingResponse, Environment environment){

        FetchMessageServiceImpl fetchMessageService;
        Optional<List<FetchMessageResponse>> fetchMessageResponseListOptional;

        EndpointsUnfilteredMessageParameters unfilteredMessageParameters = new EndpointsUnfilteredMessageParameters();
        unfilteredMessageParameters.setOnboardingResponse(onboardingResponse);
        unfilteredMessageParameters.setDirection(Endpoints.ListEndpointsQuery.Direction.SEND_RECEIVE);
        unfilteredMessageParameters.setTechnicalMessageType(TechnicalMessageType.IMG_JPEG);

        EndpointsUnfilteredListService unfilteredEndpointListService = new EndpointsUnfilteredListServiceImpl(environment);


        unfilteredEndpointListService.setRequestFormatJSON();
        unfilteredEndpointListService.send(unfilteredMessageParameters);

        fetchMessageService = new FetchMessageServiceImpl();
        fetchMessageService.setResponseFormatJSON();
        fetchMessageResponseListOptional = fetchMessageService.fetch(onboardingResponse,5,2000);
        if(fetchMessageResponseListOptional.isPresent()){
            List<FetchMessageResponse> fetchMessageResponseList = fetchMessageResponseListOptional.get();
            DecodeMessageService decodeMessageService = new DecodeMessageServiceImpl();
            for(FetchMessageResponse fetchMessageResponse: fetchMessageResponseList) {
                DecodeMessageResponse message = decodeMessageService.decode(fetchMessageResponse.getCommand().getMessage());
                //We do not decode, because this breaks!
                // DecodeMessageResponse value = decodeMessageService.decode(fetchMessageResponse.getCommand().getMessage());
                System.out.println("MessageType: "+ message.getResponseEnvelope().getType().getDescriptorForType().getFullName());
            }
        }


        unfilteredEndpointListService.setRequestFormatProtobuf();
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

    }

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

        Gson gson = new Gson();

        String onboardingStringApp1 = "{\n" +
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

        OnboardingResponse onboardingResponseApp1 = gson.fromJson(onboardingStringApp1,OnboardingResponse.class);
        //System.out.println(gson.toJson(onboardingResponse));

        String onboardingStringApp2 = "{\n" +
                "    \"deviceAlternateId\": \"715e1bf2-d942-4383-95a8-dc31b0453be3\",\n" +
                "    \"capabilityAlternateId\": \"79dfd918-7051-471a-9b73-3f3c23deca38\",\n" +
                "    \"sensorAlternateId\": \"0525cc41-37c4-45b6-9c0d-8a12502c8faa\",\n" +
                "    \"connectionCriteria\": {\n" +
                "        \"gatewayId\": \"3\",\n" +
                "        \"measures\": \"https://dke-qa.eu1.cp.iot.sap/iot/gateway/rest/measures/715e1bf2-d942-4383-95a8-dc31b0453be3\",\n" +
                "        \"commands\": \"https://dke-qa.eu1.cp.iot.sap/iot/gateway/rest/commands/715e1bf2-d942-4383-95a8-dc31b0453be3\"\n" +
                "    },\n" +
                "    \"authentication\": {\n" +
                "        \"type\": \"PEM\",\n" +
                "        \"secret\": \"t8KG6eweX150KSNQLi5XhB2yANZNKubvrlnQ\",\n" +
                "        \"certificate\": \"-----BEGIN ENCRYPTED PRIVATE KEY-----\\nMIIE6zAdBgoqhkiG9w0BDAEDMA8ECKNiCwMQo1reAgMCAAAEggTIooZOtMu/aYXA\\nwZiNzgjtE7iLO8FZHmAye9S08GurWGsenOrpyfUk3/TF76BlSXNF+KEOSVjFg5at\\nZdCcy/vYShdDcujuWW7vc6mrh87/OKiv8o1jFhU4sU7qA8PhMl7r2558bzb71bj9\\n+lijiwzO14+5p9EITnEFaeXhsNble0fEgSAElSOxzuPXH6OnuwqQTsHwh5uY+4wa\\n8idIcmxNtpNpdW7ACvGXFKJDeU6ZCwA1ixARNE3zJG+jNDpczYthl05lsGGQAHtK\\nZjJY+R+ut0TDVruLb6Kd6y/4ifEqf0ChhkuMKy8ZtBXy9OIPm0GBxv/unBcr2wm0\\nVsXUPmpSOrwrzttkApoo4rPKBL9NwwFHGcFXkfYCRZ841WdJJsNro/D2/3fJsdvU\\nIu2Xf8TaKRwP/7+Ah6j4gaIw1OyqZ9yk+gVJqOiptBL5CYmw78gr3DfjlNce98Xt\\nKHoqVyB8kOZD1d6GtKK//bkLgCFc78RID/y9H0R3fUTKgnKzKHKnzBjdexvN4X4u\\n1NeJpQUWMYtRqYfsc1eHbO8wuzu1jQtpf2DTlbf3F8T4FSeTh9y5FxVUSirD9Tzs\\n3PW8E7HDoDZAsby/HaTRoNKjSaPNF7KED2bOsZyTInbLgwWt+wXMfq28Puwfo8l2\\nGwzu+35oac6U1xJrHoaNiV9v3qo6dBCU83VBNL0xVrX8oB2YY/cPjtMzfwJnKLen\\nDDC/8tcKS7arVeJ/jOnxnXNQxSVbPMErROHyFzc61zGUVCqy0744U3xaWJD2Jgcs\\nFXDJZowHZztzwtJUdS0NIKJ4kGzu9hzUQhhr0t6BQj3vX3by0vhJEyUGf3R6dUGy\\nzxTJq0siZMqswm7skmUjwaNGloPbTfVx2617gybSNpAQavueoEazVMy5hv0NPaSO\\n8BHiLPP7/tGAg3Qn76b1Xe+9oBLg6gUFdGDcugf3iEnUvgn16xq5WsW4+L9kBbo3\\ndQPARGooQ05fDddcdzA0X5BCNBCAt3iPys3n5q57vTZ3A/KF7tFXe7Xgfat/qNMQ\\nrkjoIxXTvKHnLGCJKplsviPcDjajuyPnfHkqLP5fkqHfzh/oLKNt+HvBgsfkRIun\\nteFDdW2ROG9XIcqJRuqMIZr2P9wOKv/+xGo4GEbrrsXXsnt4iXCV/Dlt9P3COisb\\nMTZEqDmUeI4P8p89nmszbnLScrnUMJEAfsaVqslfSzRrerOmLwX4vhAWoIrdSdbJ\\n/DS7ALDVPttDj5H5rAX+JNoN+SvTN//7CE2F/ckghuq9R+fXinl8Ezl2gBIkHer8\\nGJzhfCFg3DjxjZWxmZL3zQhC20FG6xibUOODgwN1AYqFveOn2Ak00A9hDrtvAvQa\\n8z89v3xiACdB2UwTnRzJI4xI4PrrgydyiZXEroobSyWrL03IHYc0D7BCPVqFZx2b\\nOOxTPH0Sarxq2kGznQsSQokFc2MdKNkTpZIK/m+93H8FBAFiXzdPVIJD/RTrJicf\\nY7nhrEEOY1EPv9WBUn4omoOzXZcjfSqw1tW2vnhOwBmKjfNNS93Bwr3xaxskoBE4\\n9bTD1BkZY/x05+OCsg7DuQpOSObN0FHLmazyDztaTBuq/+iIcJWi7qy+1kHqws6k\\nrN/9ONQMAA8Va+eRNA7z\\n-----END ENCRYPTED PRIVATE KEY-----\\n-----BEGIN CERTIFICATE-----\\nMIIEaDCCA1CgAwIBAgIPAPjX12jTvgVBEAECQ65NMA0GCSqGSIb3DQEBCwUAMFYx\\nCzAJBgNVBAYTAkRFMSMwIQYDVQQKExpTQVAgSW9UIFRydXN0IENvbW11bml0eSBJ\\nSTEiMCAGA1UEAxMZU0FQIEludGVybmV0IG9mIFRoaW5ncyBDQTAeFw0xOTAxMTQx\\nNjA5MDBaFw0yMDAxMTQxNjA5MDBaMIG1MQswCQYDVQQGEwJERTEcMBoGA1UEChMT\\nU0FQIFRydXN0IENvbW11bml0eTEVMBMGA1UECxMMSW9UIFNlcnZpY2VzMXEwbwYD\\nVQQDFGhkZXZpY2VBbHRlcm5hdGVJZDo3MTVlMWJmMi1kOTQyLTQzODMtOTVhOC1k\\nYzMxYjA0NTNiZTN8Z2F0ZXdheUlkOjN8dGVuYW50SWQ6MTE1MDcwNzc2MHxpbnN0\\nYW5jZUlkOmRrZS1xYTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAIIp\\njmxRzvfitBWJ4xHXLeuEjkurvx5gFg05FIcce7FKsnDuiYd+mAK8VoKmPt9jKkTV\\nnValZV5urmP98lxEa0Z3pqFVzQusdgO/226Fnwu1dUzgUo8tHBsomyHR3HjXSkNS\\nPfgsrRhzCZBL0hJDpPzZny36X6SqdVA9grCDWllUSs774KLpYp42PE0LS4GT2VCW\\nVZJqWUuujpXHRvLMHJehUzOjaghja5zdoUQ8eywKFdSq1u5wxltezxQ8RVtE5aVB\\nXbn0aUzEBRIOPNkr+Gqiy9+FFW8MX7nY/a9gWqFEd2wenyUSPYJk3bQ1ccGig6c/\\nJ5Ox9vozMZxCRwhM8acCAwEAAaOB0jCBzzBIBgNVHR8EQTA/MD2gO6A5hjdodHRw\\nczovL3Rjcy5teXNhcC5jb20vY3JsL1RydXN0Q29tbXVuaXR5SUkvU0FQSW9UQ0Eu\\nY3JsMAwGA1UdEwEB/wQCMAAwJQYDVR0SBB4wHIYaaHR0cDovL3NlcnZpY2Uuc2Fw\\nLmNvbS9UQ1MwDgYDVR0PAQH/BAQDAgbAMB0GA1UdDgQWBBQkcnZiGFM0cjAcIE1F\\nvoLVWw1xtTAfBgNVHSMEGDAWgBSVt7P1WN7VtLNYRuDypsl4Tr0tdTANBgkqhkiG\\n9w0BAQsFAAOCAQEAG5N4tGWTwslVlLhZYLss8mW29mBEHW+lYlELg3HkxxgnPJuG\\nM+DIhCBhSBWtREUVd/H69vPSpha3pVLZgoUJtSql0WoA4Wl46J9fN3dDcY/yfH3K\\nmmyRQL0x3aL+QS4hculPoKAFmn8IveAD7G/Uzh8hvVqUKeJwf7BU7SxLECBAOlMX\\nDdpd3zsOs7/k1QMAkh27fZsrKZE2whpo9k2ffy2ZfFJFMdBwwbRrXFHfuEkmyxN+\\nv89VeBgA4Ym6rmVM18usD0ge+o1akFAglX45tjt+4GtqpPWYcZihZ1bhznYUsjmJ\\nhE3NImJbMESMz2zv4HGcq5/n4i3JjAn2W31laA==\\n-----END CERTIFICATE-----\\n\"\n" +
                "    }\n" +
                "}";

        OnboardingResponse onboardingResponseApp2 = gson.fromJson(onboardingStringApp2,OnboardingResponse.class);
        //System.out.println(gson.toJson(onboardingResponse));
        //getUnfilteredEndpointList(onboardingResponseApp1, environment);

        sendMessage(
                onboardingResponseApp1,
                onboardingResponseApp2,
                environment,
                TechnicalMessageType.ISO_11783_TASKDATA_ZIP,
                "Hello world mit äöü"
        );



    }

    private static void sendMessage(OnboardingResponse onboardingResponseApp1, OnboardingResponse onboardingResponseApp2, Environment environment ,TechnicalMessageType iso11783TaskdataZip, String hello_world_mit_äöü)
    {

        FetchMessageServiceImpl fetchMessageService;
        Optional<List<FetchMessageResponse>> fetchMessageResponseListOptional;

/*
        SendRawMessageService sendRawMessageService = new SendRawMessageServiceImpl();
        sendRawMessageService.setRequestFormatProtobuf();
        SendRawMessageParameters sendRawMessageParameters = new SendRawMessageParameters();

        sendRawMessageParameters.setOnBoardingResponse(onboardingResponseApp1);

        sendRawMessageParameters.setMode(Request.RequestEnvelope.Mode.DIRECT);
        sendRawMessageParameters.setTeamSetContextId("");
        sendRawMessageParameters.setTechnicalMessageType(TechnicalMessageType.ISO_11783_TASKDATA_ZIP);
        sendRawMessageParameters.addReceipient(onboardingResponseApp2.getSensorAlternateId());

        String message = new String("Hello World");
        sendRawMessageParameters.setRawData(message.getBytes());

        sendRawMessageParameters.setTypeURL("");

        sendRawMessageService.send(sendRawMessageParameters);

        fetchMessageService = new FetchMessageServiceImpl();
        fetchMessageService.setResponseFormatProtobuf();
        fetchMessageResponseListOptional = fetchMessageService.fetch(onboardingResponseApp1,5,2000);
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
*/

        MessageHeaderQueryService messageHeaderQueryService = new MessageHeaderQueryServiceImpl(environment);

        MessageQueryParameters messageHeaderQueryparameters = new MessageQueryParameters();
        messageHeaderQueryparameters.setOnboardingResponse( onboardingResponseApp2);
        messageHeaderQueryparameters.setMessageIds(new ArrayList<>());
        List<String> headerSenderIds = new ArrayList<>();
        headerSenderIds.add(onboardingResponseApp1.sensorAlternateId);
        messageHeaderQueryparameters.setSenderIds(headerSenderIds);
        messageHeaderQueryparameters.setSentFromInSeconds((long) 0);
        messageHeaderQueryparameters.setSentToInSeconds(new TimestampUtil().current().getSeconds() +  864000);
        messageHeaderQueryService.send(messageHeaderQueryparameters);


        fetchMessageService = new FetchMessageServiceImpl();
        fetchMessageService.setResponseFormatProtobuf();
        fetchMessageResponseListOptional = fetchMessageService.fetch(onboardingResponseApp2,5,2000);
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


        MessageQueryService messageQueryService = new MessageQueryServiceImpl(environment);

        MessageQueryParameters messageQueryparameters = new MessageQueryParameters();
        messageQueryparameters.setOnboardingResponse( onboardingResponseApp2);
        messageQueryparameters.setMessageIds(new ArrayList<>());
        List<String> querySenderIds = new ArrayList<>();
        querySenderIds.add(onboardingResponseApp1.sensorAlternateId);
        messageQueryparameters.setSenderIds(querySenderIds);
        messageQueryparameters.setSentFromInSeconds((long) 0);
        messageQueryparameters.setSentToInSeconds(new TimestampUtil().current().getSeconds() +  864000);
        messageQueryService.send(messageHeaderQueryparameters);


        fetchMessageService = new FetchMessageServiceImpl();
        fetchMessageService.setResponseFormatProtobuf();
        fetchMessageResponseListOptional = fetchMessageService.fetch(onboardingResponseApp2,5,2000);
        if(fetchMessageResponseListOptional.isPresent()){
            List<FetchMessageResponse> fetchMessageResponseList = fetchMessageResponseListOptional.get();
            for(FetchMessageResponse fetchMessageResponse: fetchMessageResponseList) {
                DecodeMessageService decodeMessageService = new DecodeMessageServiceImpl();
                System.out.println("Raw Protobuf Result: " + fetchMessageResponse.getCommand().getMessage());
                //We do not decode, because this breaks!
                DecodeMessageResponse value = decodeMessageService.decode(fetchMessageResponse.getCommand().getMessage());
                try {
                    FeedResponse.MessageQueryResponse messageQueryResponse = FeedResponse.MessageQueryResponse.parseFrom(value.getResponsePayloadWrapper().getDetails().getValue());
                    List<FeedResponse.MessageQueryResponse.FeedMessage> feedMessageList = messageQueryResponse.getMessagesList();
                    for(FeedResponse.MessageQueryResponse.FeedMessage feedMessage : feedMessageList){
                        System.out.println("Technical Message Type: "+ feedMessage.getHeader().getTechnicalMessageType().toString());
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }


    }



}
