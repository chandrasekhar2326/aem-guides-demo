package com.adobe.aem.guides.demo.core.servlets;

import com.adobe.granite.asset.api.Asset;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Iterator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Component(
        service = Servlet.class,
        property = {
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/profiles/assets",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=GET"
        }
)
public class ProfilesAssetsServlet extends SlingAllMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(ProfilesAssetsServlet.class);
    private static final String ASSET_PATH = "/content/dam/task02/images";

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        log.info("doGet method started");
        ResourceResolver resourceResolver = request.getResourceResolver();
        log.info("Obtained ResourceResolver");

        Resource assetFolder = resourceResolver.getResource(ASSET_PATH); //by using this resource resolevr we are getting the resource from the jcr of assetpath and obtain to asset folder
        if (assetFolder == null) {
            log.info("Asset folder not found at path: {}", ASSET_PATH);
        } else {
            log.info("Asset folder found at path: {}", ASSET_PATH);
        }

        JsonArray assetsArray = new JsonArray();
        if (assetFolder != null) {
            Iterator<Resource> assetIterator = assetFolder.listChildren();
            log.info("Iterating through asset folder children");
            while (assetIterator.hasNext()) {
                Resource assetResource = assetIterator.next();
                log.info("Processing asset: {}", assetResource.getPath());
                Asset asset = assetResource.adaptTo(Asset.class);

                if (asset != null) {
                    JsonObject assetObj = new JsonObject();
                    assetObj.addProperty("text", asset.getName());  // Image Name
                    assetObj.addProperty("value", asset.getPath()); // Image Path
                    assetsArray.add(assetObj);
                    log.info("Added asset: Name={}, Path={}", asset.getName(), asset.getPath());
                } else {
                    log.info("Skipping resource {} as it is not an asset", assetResource.getPath());
                }
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(assetsArray.toString());
        log.info("Response sent: {}", assetsArray.toString());
    }
}

