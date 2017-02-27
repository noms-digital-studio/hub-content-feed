package uk.gov.justice.digital.noms.hub.ports.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.noms.hub.domain.ContentItem;
import uk.gov.justice.digital.noms.hub.domain.MetadataRepository;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
@Slf4j
public class LaravelController {

    private final MetadataRepository metadataRepository;

    public LaravelController(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @RequestMapping(value = "pdf/course/{id}", method = RequestMethod.GET)
    public String findCourseCategoriesItemForID(@PathVariable String id) throws Exception {
        List<ContentItem> results = metadataRepository.findAll();
        List<String> categories = results.stream()
                .map(contentItem -> contentItem.getMetadata() != null ? contentItem.getMetadata().get("category") : null)
                .distinct()
                .collect(toList());

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("categories", categories);

        return populateTemplate(data, "category-response.vm");
    }


    @RequestMapping(value = "pdf/course/pdfs/{id}", method = RequestMethod.GET)
    public String findCoursesItemForID(@PathVariable String id) throws Exception {

        List<ContentItem> results = metadataRepository.findAll();
        List<ContentItem> items = results.stream()
                .filter(contentItem -> contentItem.getMetadata() != null && contentItem.getMetadata().get("category").equals(id))
                .map(contentItem -> contentItem)
                .collect(toList());

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("items", items);

        return populateTemplate(data, "pdf-response.vm");

    }

    private String populateTemplate(Map<String, Object> data, String name) throws Exception {
        VelocityContext context = new VelocityContext();
        context.put("data", data);
        StringWriter out = new StringWriter();
        VelocityEngine ve = velocityEngine();
        Template template = ve.getTemplate(name);
        template.merge(context, out);
        return out.toString();
    }

    private VelocityEngine velocityEngine() throws Exception {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        return ve;
    }
}