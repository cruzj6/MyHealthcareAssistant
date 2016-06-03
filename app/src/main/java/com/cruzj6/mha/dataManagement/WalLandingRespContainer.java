package com.cruzj6.mha.dataManagement;

/**
 * Created by Joey on 6/2/16.
 */
public class WalLandingRespContainer {

    private String url;
    private String template;
    private String upLimit;
    private String token;

    public WalLandingRespContainer(String url, String template, String upLimit, String token)
    {
        this.url = url;
        this.template = template;
        this.upLimit = upLimit;
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public String getTemplate() {
        return template;
    }

    public String getUpLimit() {
        return upLimit;
    }

    public String getToken() {
        return token;
    }
}
