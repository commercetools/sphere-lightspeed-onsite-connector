package io.sphere.lightspeed.utils;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.sphere.lightspeed.client.XmlException;
import org.apache.commons.io.IOUtils;
import org.zapodot.jackson.java8.JavaOptionalModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public final class XmlUtils {
    private static final ObjectMapper MAPPER = newXmlMapper();

    public XmlUtils() {
    }

    public static ObjectMapper newXmlMapper() {
        return new XmlMapper(new WstxInputFactory(), new WstxOutputFactory())
                .registerModule(new JavaOptionalModule())
                .registerModule(new ParameterNamesModule())
                .registerModule(new JSR310Module())//Java 8 DateTime
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> String toXml(final T object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new XmlException(e);
        }
    }

    public static <T> T readObjectFromResource(final String resourcePath, final Class<T> clazz) {
        try {
            return MAPPER.readValue(readFromResource(resourcePath), clazz);
        } catch (IOException e) {
            throw new XmlException(e);
        }
    }

    public static String readStringFromResource(final String resourcePath) {
        try {
            return IOUtils.toString(readFromResource(resourcePath));
        } catch (IOException e) {
            throw new XmlException(e);
        }
    }

    public static <T> T readObject(final TypeReference<T> typeReference, final byte[] input) {
        try {
            return MAPPER.readValue(input, typeReference);
        } catch (IOException e) {
            throw new XmlException(input, e);
        }
    }

    private static InputStreamReader readFromResource(final String resourcePath) throws UnsupportedEncodingException {
        final InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        return new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8.name());
    }
}
