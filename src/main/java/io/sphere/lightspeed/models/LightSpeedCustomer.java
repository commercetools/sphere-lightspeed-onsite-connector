package io.sphere.lightspeed.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.sphere.sdk.models.Base;

@JacksonXmlRootElement(localName = "customer")
public class LightSpeedCustomer extends Base implements Referenceable<LightSpeedCustomer> {
    private String id;
    private String uri;
    private String email;

    private LightSpeedCustomer() {
    }

    public String getId() {
        return id;
    }

    @Override
    public String getUri() {
        return uri;
    }

    public String getEmail() {
        return email;
    }

    public static TypeReference<LightSpeedCustomer> typeReference(){
        return new TypeReference<LightSpeedCustomer>() {
            @Override
            public String toString() {
                return "TypeReference<LightSpeedCustomer>";
            }
        };
    }
}
