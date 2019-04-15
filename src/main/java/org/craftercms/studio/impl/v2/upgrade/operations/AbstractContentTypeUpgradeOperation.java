/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.upgrade.operations;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradeOperation;
import org.springframework.beans.factory.annotation.Required;
import org.w3c.dom.Document;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Base implementation of {@link UpgradeOperation} for all content-type related upgrades
 *
 * <p>Supported YAML properties:
 * <ul>
 *     <li><strong>includedContentTypes</strong0>: (optional) list of content-types that can be handled by this
 *     operation, if left unset all content-types will be included</li>
 *     <li><strong>formDefinitionXpath</strong>: (optional) XPath selector to evaluate if a content-type should be
 *     handled by this operation, if left unset all content-types will be included</li>
 *     <li><strong>maxCacheItems</strong>: (optional) maximum number of items in the cache, can be used for
 *     performance tuning in case an upgrade needs to parse too many files in the repository. Defaults to 200
 *     </li>
 * </ul>
 * </p>
 *
 * @author joseross
 */
public abstract class AbstractContentTypeUpgradeOperation extends AbstractContentUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(AbstractContentTypeUpgradeOperation.class);

    public static final String CONFIG_KEY_CONTENT_TYPES = "includedContentTypes";
    public static final String CONFIG_KEY_FORM_DEFINITION = "formDefinitionXpath";
    public static final String CONFIG_KEY_MAX_ITEMS = "maxCacheItems";
    public static final String NAME_PLACEHOLDER = "\\{name}";

    public static final int DEFAULT_MAX_ITEMS = 200;

    /**
     * List of content-types that can be handled by this operation (optional)
     */
    protected List<String> includedContentTypes;

    /**
     * XPath to evaluate if a content-type can be handled by this operation (optional)
     */
    protected String formDefinitionXpath;

    /**
     * XPath to query the content-type of item descriptors
     */
    protected String contentTypeXpath;

    /**
     * Path template to find the form-definition.xml file for a content-type
     */
    protected String formDefinitionTemplate;

    /**
     * Cache used to avoid parsing the same file multiple times during the execution of this operation
     */
    protected Cache<Path, Document> cache;

    protected DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    protected XPathFactory xPathFactory = XPathFactory.newInstance();
    protected TransformerFactory transformerFactory = TransformerFactory.newInstance();

    @Required
    public void setContentTypeXpath(final String contentTypeXpath) {
        this.contentTypeXpath = contentTypeXpath;
    }

    @Required
    public void setFormDefinitionTemplate(final String formDefinitionTemplate) {
        this.formDefinitionTemplate = formDefinitionTemplate;
    }

    @Override
    protected void doInit(final HierarchicalConfiguration<ImmutableNode> config) {
        super.doInit(config);
        includedContentTypes = config.getList(String.class, CONFIG_KEY_CONTENT_TYPES);
        formDefinitionXpath = config.getString(CONFIG_KEY_FORM_DEFINITION);
        int maxCacheItems = config.getInt(CONFIG_KEY_MAX_ITEMS, DEFAULT_MAX_ITEMS);
        cache = CacheBuilder
            .newBuilder()
            .maximumSize(maxCacheItems)
            .build();
    }

    @Override
    protected boolean shouldBeUpdated(final String site, final Path file) throws UpgradeException {
        logger.debug("Checking file {0} for site {1}", file, site);
        try {
            Document document = loadDocument(file);

            String contentTypeName = (String) select(document, contentTypeXpath, XPathConstants.STRING);

            if(CollectionUtils.isNotEmpty(includedContentTypes) && !includedContentTypes.contains(contentTypeName)) {
                logger.debug("File {0} of content-type {1} will not be updated", file, contentTypeName);
                return false;
            }

            if(StringUtils.isNotEmpty(formDefinitionXpath)) {
                Path formDefinition = getFormDefinition(site, contentTypeName);

                document = loadDocument(formDefinition);
                return (Boolean) select(document, formDefinitionXpath, XPathConstants.BOOLEAN);
            } else {
                return true;
            }
        } catch (Exception e) {
            throw new UpgradeException("Error parsing xml file " + file, e);
        }
    }

    /**
     * Parses the given XML file
     * @param file the file to parse
     * @return the parsed file
     * @throws ExecutionException if there is any error parsing the file
     */
    protected Document loadDocument(Path file) throws ExecutionException {
        return cache.get(file, () -> {
            logger.debug("Parsing file {0}", file);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(file.toFile());
        });
    }

    /**
     * Finds the form-definition.xml five for the given content-type
     * @param site the site id
     * @param contentTypeName the content-type name
     * @return the form definition file
     */
    protected Path getFormDefinition(String site, String contentTypeName) {
        return getRepositoryPath(site).getParent()
            .resolve(formDefinitionTemplate.replaceFirst(NAME_PLACEHOLDER, contentTypeName));
    }

    /**
     * Evaluates a XPath selector on the given object
     * @param source the object to evaluate
     * @param xpath the selector to evaluate
     * @param type the type to return
     * @return the evaluation result
     * @throws XPathExpressionException if there is any error evaluating the selector
     */
    protected Object select(Object source, String xpath, QName type) throws XPathExpressionException {
        return xPathFactory.newXPath().compile(xpath).evaluate(source, type);
    }

    /**
     * Writes an XML file to the file system without committing any changes in the repository
     * @param file the file to write
     * @param document the XML document to write
     * @throws UpgradeException if there is any error writing the file
     */
    protected void writeFile(Path file, Document document) throws UpgradeException {
        try (Writer writer = Files.newBufferedWriter(file)) {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
        } catch (Exception e) {
            throw new UpgradeException("Error writing file " + file, e);
        }
    }

}
