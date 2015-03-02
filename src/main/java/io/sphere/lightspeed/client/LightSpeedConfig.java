package io.sphere.lightspeed.client;

import io.sphere.sdk.models.Base;

public class LightSpeedConfig extends Base {
    private final String appUrl;
    private final String appId;
    private final String appPrivateId;
    private final String username;
    private final String password;

    private LightSpeedConfig(final String appUrl, final String appId, final String appPrivateId, final String username, final String password) {
        this.appUrl = appUrl;
        this.appId = appId;
        this.appPrivateId = appPrivateId;
        this.username = username;
        this.password = password;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppPrivateId() {
        return appPrivateId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static LightSpeedConfig of(final String appUrl, final String appId, final String appPrivateId, final String username, final String password) {
        return new LightSpeedConfig(appUrl, appId, appPrivateId, username, password);
    }
}
