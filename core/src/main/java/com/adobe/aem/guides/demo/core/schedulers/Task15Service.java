package com.adobe.aem.guides.demo.core.schedulers;

import com.adobe.aem.guides.demo.core.schedulers.Config;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

@Component(service = Task15Service.class, immediate = true)
@Designate(ocd = Config.class)
public class Task15Service {

    private String clientId;
    private String apiToken;
    private String pagePath;

    @Activate
    @Modified
    protected void activate(Config config) {
        this.clientId = config.clientId();
        this.apiToken = config.apiToken();
        this.pagePath = config.pagePath();
    }

    public String getClientId() {
        return clientId;
    }

    public String getApiToken() {
        return apiToken;
    }

    public String getPagePath() {
        return pagePath;
    }
}

