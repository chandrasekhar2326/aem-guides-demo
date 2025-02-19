package com.adobe.aem.guides.demo.core.workflows;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

@Component(service = WorkflowProcess.class, property = { "process.label=Add Expire Date Property to Page" })
public class Task10AddExpireDateWorkflowProcess implements WorkflowProcess {

    private static final Logger log = LoggerFactory.getLogger(Task10AddExpireDateWorkflowProcess.class);
    private static final String PAGE_RESOURCE_TYPE = "cq:Page";
    private static final String EXPIRED_DATE_PROPERTY = "expireddate";
    private static final int EXPIRATION_PERIOD_DAYS = 365; // 1 year

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {

        // Retrieve the payload path (typically the page path)
        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        log.info("Executing workflow step for payload: {}", payloadPath);

        // Attempt to retrieve the resource resolver and process the resource
        try (ResourceResolver resourceResolver = getServiceResourceResolver()) {

            if (resourceResolver == null) {
                log.error("Failed to obtain a service ResourceResolver. Cannot proceed.");
                throw new WorkflowException("ResourceResolver is null.");
            }

            // Fetch the resource (page) based on the payload path
            Resource resource = resourceResolver.getResource(payloadPath);

            // Check if the resource exists and is a valid page
            if (resource != null && resource.isResourceType(PAGE_RESOURCE_TYPE)) {
                log.info("Processing resource at path: {}", payloadPath);

                // Add the expiry date property to the page
                addExpiredDateProperty(resource);
                log.info("Expiry date property added successfully for {}", payloadPath);
            } else {
                log.warn("Resource at path {} is either null or not a page.", payloadPath);
            }

        } catch (Exception e) {
            log.error("Error while adding expiry date property to page {}", payloadPath, e);
            throw new WorkflowException("Failed to add expiry date property", e);
        }
    }


    private void addExpiredDateProperty(Resource resource) throws RepositoryException {
        Node pageNode = resource.adaptTo(Node.class);
        if (pageNode != null && pageNode.hasNode("jcr:content")) {
            Node contentNode = pageNode.getNode("jcr:content");
            // Retrieve the creation date of the JCR node (fallback to current date if missing)
            Calendar creationDate = contentNode.hasProperty("jcr:created")
                    ? contentNode.getProperty("jcr:created").getDate() // Get created date from JCR
                    : new GregorianCalendar(); // Use current date as fallback

// Clone the creation date to avoid modifying the original value
            Calendar expirationDate = (Calendar) creationDate.clone();

// Add a fixed number of days to determine the expiration date
            expirationDate.add(Calendar.DAY_OF_MONTH, EXPIRATION_PERIOD_DAYS);


            // Add the expireddate property
            contentNode.setProperty(EXPIRED_DATE_PROPERTY, expirationDate);
            pageNode.getSession().save();
            log.info("Added {} property with value {} to page {}", EXPIRED_DATE_PROPERTY, expirationDate.getTime(),
                    resource.getPath());
        }
    }

    private ResourceResolver getServiceResourceResolver() throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put(ResourceResolverFactory.SUBSERVICE, "chandra"); // Ensure you have a service user
                                                                  // configured with the required
                                                                  // permissions
        return resourceResolverFactory.getServiceResourceResolver(param);
    }
}