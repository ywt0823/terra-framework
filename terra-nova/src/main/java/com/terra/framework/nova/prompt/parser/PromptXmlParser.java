package com.terra.framework.nova.prompt.parser;

import com.terra.framework.nova.prompt.config.PromptConfig;
import com.terra.framework.nova.prompt.exception.PromptException;
import com.terra.framework.nova.prompt.template.PromptTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A parser for XML-based prompt mapper files.
 * <p>
 * This class reads an XML resource and parses its content to extract
 * prompt definitions with their configurations.
 *
 * @author DeavyJones
 */
public class PromptXmlParser {

    private final InputStream inputStream;
    private final String resourceName;

    public PromptXmlParser(InputStream inputStream, String resourceName) {
        this.inputStream = inputStream;
        this.resourceName = resourceName;
    }

    /**
     * Parses the XML resource to extract prompt definitions.
     *
     * @return A map of prompt templates, keyed by their fully qualified ID (namespace + id).
     */
    public Map<String, PromptTemplate> parse() {
        Map<String, PromptTemplate> promptTemplates = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            Element root = doc.getDocumentElement();
            String namespace = root.getAttribute("namespace");
            if (namespace == null || namespace.trim().isEmpty()) {
                throw new PromptException("Prompt XML file [" + resourceName + "] is missing namespace attribute.");
            }

            // Parse global configuration from namespace element
            PromptConfig globalConfig = parseConfigFromElement(root);

            NodeList promptNodes = root.getElementsByTagName("prompt");
            for (int i = 0; i < promptNodes.getLength(); i++) {
                Element promptNode = (Element) promptNodes.item(i);
                String id = promptNode.getAttribute("id");
                String content = promptNode.getTextContent().trim();

                if (id == null || id.trim().isEmpty()) {
                    throw new PromptException("Found <prompt> tag with missing 'id' in " + resourceName);
                }

                // Parse prompt-level configuration
                PromptConfig promptConfig = parseConfigFromElement(promptNode);
                
                // Merge prompt config with global config (prompt config takes precedence)
                PromptConfig finalConfig = promptConfig.mergeWith(globalConfig);

                String fullyQualifiedId = namespace + "." + id;
                if (promptTemplates.containsKey(fullyQualifiedId)) {
                    throw new PromptException("Duplicate prompt ID '" + id + "' found in namespace '" + namespace + "'.");
                }

                promptTemplates.put(fullyQualifiedId, new PromptTemplate(id, content, finalConfig));
            }
        } catch (Exception e) {
            throw new PromptException("Failed to parse prompt XML file [" + resourceName + "]", e);
        }
        return promptTemplates;
    }

    /**
     * Parses configuration attributes from an XML element.
     *
     * @param element The XML element to parse
     * @return A PromptConfig object with parsed configuration
     */
    private PromptConfig parseConfigFromElement(Element element) {
        PromptConfig config = new PromptConfig();

        // Parse model attribute
        String model = element.getAttribute("model");
        if (model != null && !model.trim().isEmpty()) {
            config.setModel(model.trim());
        }

        // Parse temperature attribute
        String temperature = element.getAttribute("temperature");
        if (temperature != null && !temperature.trim().isEmpty()) {
            try {
                config.setTemperature(Double.parseDouble(temperature.trim()));
            } catch (NumberFormatException e) {
                throw new PromptException("Invalid temperature value '" + temperature + "' in " + resourceName);
            }
        }

        // Parse max-tokens attribute
        String maxTokens = element.getAttribute("max-tokens");
        if (maxTokens != null && !maxTokens.trim().isEmpty()) {
            try {
                config.setMaxTokens(Integer.parseInt(maxTokens.trim()));
            } catch (NumberFormatException e) {
                throw new PromptException("Invalid max-tokens value '" + maxTokens + "' in " + resourceName);
            }
        }

        // Parse top-p attribute
        String topP = element.getAttribute("top-p");
        if (topP != null && !topP.trim().isEmpty()) {
            try {
                config.setTopP(Double.parseDouble(topP.trim()));
            } catch (NumberFormatException e) {
                throw new PromptException("Invalid top-p value '" + topP + "' in " + resourceName);
            }
        }

        return config;
    }
} 