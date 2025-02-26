package com.orainge.tools.network.dns.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.orainge.tools.network.dns.consts.DNSType;

import java.io.IOException;

/**
 * DNSType 序列化规则
 */
public class DNSTypeSerializer extends StdSerializer<DNSType> {
    private DNSTypeSerializer() {
        super(DNSType.class);
    }

    @Override
    public void serialize(DNSType value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        String str = value.getType();
        gen.writeString(str);
    }
}