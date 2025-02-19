package com.adobe.aem.guides.demo.core.schedulers;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
@ObjectClassDefinition(name = "My Configuration Service")

public @interface Config{
        @AttributeDefinition(name = "Client ID", description = "Client ID for API authentication")
        String clientId() default "Sekhar";

        @AttributeDefinition(name = "API Token", description = "Token for API authentication")
        String apiToken() default "EA23CDF";

        @AttributeDefinition(name = "Page Path", description = "Path of the page to be updated")
        String pagePath() default "/content/task02/us/en";
    }

