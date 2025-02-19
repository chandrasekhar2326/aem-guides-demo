package com.adobe.aem.guides.demo.core.schedulers;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * AEM Scheduler to check pages under /content for expiryDate property and publish or unpublish pages accordingly.
 */
@Component(service = Runnable.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = Task11.Config.class)                   // Mandatory Configuration – The component is not activated without a configuration.
public class Task11 implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Task11.class);

    @Reference
    private ResourceResolverService resourceResolverService;

    public class ResourceResolverService {
        public ResourceResolver createWriter() {
            return null; // Should return a valid ResourceResolver instance
        }
    }

    @Override
    public void run() {
        LOG.info("Task11 Scheduler started execution.");

        // Get a writable ResourceResolver instance
        try (ResourceResolver resolver = resourceResolverService.createWriter()) {
            if (resolver != null) {
                LOG.info("Successfully obtained a ResourceResolver.");

                // JCR SQL2 Query to find all pages under /content that have an expiryDate
                String query = "SELECT * FROM [nt:base] WHERE ISDESCENDANTNODE('/content') AND [expiryDate] IS NOT NULL";
                LOG.info("Executing query to fetch pages with expiryDate: {}", query);

                @SuppressWarnings("unchecked")
                List<Resource> pages = (List<Resource>) resolver.findResources(query, Query.JCR_SQL2);

                LOG.info("Found {} pages with expiryDate.", pages.size());

                Calendar now = new GregorianCalendar(); // Get the current date-time

                // Iterate through each page and check expiryDate
                for (Resource page : pages) {
                    ValueMap properties = page.adaptTo(ValueMap.class); //The adaptTo(ValueMap.class) method converts the page resource into a ValueMap
                    Calendar expiryDate = properties.get("expiryDate", Calendar.class); //This retrieves the expiryDate property from the ValueMap.
                    //The get() method fetches the property with key "expiryDate" and converts it into a Calendar object.

                    if (expiryDate != null) {
                        LOG.info("Processing page: {} with expiryDate: {}", page.getPath(), expiryDate.getTime());

                        if (expiryDate.after(now)) {
                            LOG.info("ExpiryDate is in the future. Publishing page: {}", page.getPath());
                            publishPage(page);
                        } else {
                            LOG.info("ExpiryDate has passed. Unpublishing page: {}", page.getPath());
                            unpublishPage(page);
                        }
                    } else {
                        LOG.warn("Page {} does not have a valid expiryDate.", page.getPath());
                    }
                }
            } else {
                LOG.error("Failed to obtain a ResourceResolver.");
            }
        } catch (Exception e) {
            LOG.error("Error occurred while executing Task11 Scheduler.", e);
        }

        LOG.info("Task11 Scheduler execution completed.");
    }

    /**
     * Publishes a page by checking it into the JCR version manager.
     *
     * @param page the resource representing the page to publish
     */
    private void publishPage(Resource page) {
        try {
            Session session = page.getResourceResolver().adaptTo(Session.class);
            if (session != null) {
                String path = page.getPath();
                if (session.hasPermission(path, "jcr:write")) {
                    session.getWorkspace().getVersionManager().checkin(path);
                    LOG.info("Successfully published page: {}", path);
                } else {
                    LOG.warn("Insufficient permissions to publish page: {}", path);
                }
            } else {
                LOG.error("JCR Session is null for path: {}", page.getPath());
            }
        } catch (RepositoryException e) {
            LOG.error("Error while publishing page: {}", page.getPath(), e);
        }
    }

    /**
     * Unpublishes a page by checking it out from the JCR version manager.
     *
     * @param page the resource representing the page to unpublish
     */
    private void unpublishPage(Resource page) {
        try {
            Session session = page.getResourceResolver().adaptTo(Session.class);
            if (session != null) {
                String path = page.getPath();
                if (session.hasPermission(path, "jcr:write")) {
                    session.getWorkspace().getVersionManager().checkout(path);
                    LOG.info("Successfully unpublished page: {}", path);
                } else {
                    LOG.warn("Insufficient permissions to unpublish page: {}", path);
                }
            } else {
                LOG.error("JCR Session is null for path: {}", page.getPath());
            }
        } catch (RepositoryException e) {
            LOG.error("Error while unpublishing page: {}", page.getPath(), e);
        }
    }

    /**
     * Configuration for the Task11 Scheduler.
     */
    @ObjectClassDefinition(name = "Task11 Scheduler")
    public @interface Config {
        @AttributeDefinition(name = "Cron Expression", description = "Cron expression to schedule the task.")
        String cronExpression() default "0/3 * * * ?"; // Runs every 3 minutes
    }
}
