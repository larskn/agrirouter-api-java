package com.dke.data.agrirouter.impl;

import com.sap.iotservices.common.protobuf.gateway.MeasureProtos;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Consumes("application/x-protobuf")
public class ProtobufEntityRequestWriter implements MessageBodyWriter<MeasureProtos.MeasureRequest> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        System.out.println("Calling Is Writable");
        if(mediaType.getSubtype().equals("x-protobuf")){
            return true;
        }
        return false;
    }

    @Override
    public long getSize(MeasureProtos.MeasureRequest measureRequest, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return measureRequest.toByteArray().length;
    }

    @Override
    public void writeTo(MeasureProtos.MeasureRequest measureRequest, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        entityStream.write(measureRequest.toByteArray());

    }
}
