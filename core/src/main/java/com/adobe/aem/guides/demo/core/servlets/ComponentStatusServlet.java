package com.adobe.aem.guides.demo.core.servlets;

import com.google.gson.JsonObject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.osgi.service.component.annotations.Component;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(service = Servlet.class, property = {
        ServletResolverConstants.SLING_SERVLET_METHODS + "=GET",
        ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/componentstatus"
})
public class ComponentStatusServlet extends SlingSafeMethodsServlet {

    private static final String COMPONENT_PATH = "/content/sekhar"; // Updated path

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonObject jsonResponse = new JsonObject();

        try (ResourceResolver resourceResolver = request.getResourceResolver()) {

            // Check permissions
            Session session = resourceResolver.adaptTo(Session.class);
            if (session == null || !session.nodeExists(COMPONENT_PATH)) {
                jsonResponse.addProperty("error", "Component not accessible");
                response.getWriter().write(jsonResponse.toString());
                return;
            }

            // Fetch the resource
            Resource resource = resourceResolver.getResource(COMPONENT_PATH);
            if (resource != null) {
                Boolean isActive = resource.getValueMap().get("isActive", Boolean.class);
                jsonResponse.addProperty("status", isActive != null ? isActive : false);
            } else {
                jsonResponse.addProperty("error", "Component not found in JCR");
            }

        } catch (Exception e) {
            jsonResponse.addProperty("error", "An error occurred: " + e.getMessage());
        }

        // Send response
        try {
            response.getWriter().write(jsonResponse.toString());
        } catch (IOException ioException) {
            response.getWriter().write("{\"error\": \"Failed to write response\"}");
        }
    }
}
