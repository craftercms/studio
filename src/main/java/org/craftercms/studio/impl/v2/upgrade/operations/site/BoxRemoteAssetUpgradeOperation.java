/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.upgrade.operations.site;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.beans.ConstructorProperties;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.apache.commons.text.StringSubstitutor.replace;

/**
 * Implementation of {@link org.craftercms.commons.upgrade.UpgradeOperation} to update item descriptors that use the Box File Upload control
 *
 * <p>Supported YAML properties:</p>
 * <ul>
 *     <li><strong>fieldNameXpath</strong>: (required) XPath selector to find the name of the field that uses the Box
 *     control</li>
 *     <li><strong>profileIdXpath</strong>: (required) XPath selector to find the profile id configured for the Box
 *     control</li>
 *     <li><strong>itemXpath</strong>: (required) XPath selector to find the value of the field that uses the Box
 *     control</li>
 *     <li><strong>itemIdXpath</strong>: (required) XPath selector to find the Box file id</li>
 *     <li><strong>itemNameXpath</strong>: (required) XPath selector to find the Box file name</li>
 *     <li><strong>urlElementName</strong>: (required) Name for the new XML tag to add to the field</li>
 *     <li><strong>urlTemplate</strong>: (required) URL template to generate the new value for the Box file</li>
 * </ul>
 *
 * @author joseross
 */
public class BoxRemoteAssetUpgradeOperation extends AbstractContentTypeUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(BoxRemoteAssetUpgradeOperation.class);

    public static final String CONFIG_KEY_FIELD_XPATH = "fieldNameXpath";
    public static final String CONFIG_KEY_PROFILE_XPATH = "profileIdXpath";
    public static final String CONFIG_KEY_ITEM_XPATH = "itemXpath";
    public static final String CONFIG_KEY_ITEM_ID_XPATH = "itemIdXpath";
    public static final String CONFIG_KEY_ITEM_NAME_XPATH = "itemNameXpath";
    public static final String CONFIG_KEY_URL_NAME = "urlElementName";
    public static final String CONFIG_KEY_URL_TEMPLATE = "urlTemplate";

    public static final String PLACEHOLDER_PROFILE = "profile";
    public static final String PLACEHOLDER_ID = "id";
    public static final String PLACEHOLDER_EXTENSION = "extension";

    /**
     * XPath selector to find the name of the field that uses the Box control
     */
    protected String fieldNameXpath;

    /**
     * XPath selector to find the profile id configured for the Box control
     */
    protected String profileIdXpath;

    /**
     * XPath selector to find the value of the field that uses the Box control
     */
    protected String itemXpath;

    /**
     * XPath selector to find the Box file id
     */
    protected String itemIdXpath;

    /**
     * XPath selector to find the Box file name
     */
    protected String itemNameXpath;

    /**
     * Name for the new XML tag to add to the field
     */
    protected String urlElementName;

    /**
     * URL template to generate the new value for the Box file
     */
    protected String urlTemplate;

    @ConstructorProperties({"studioConfiguration", "contentTypeXpath", "formDefinitionTemplate"})
    public BoxRemoteAssetUpgradeOperation(StudioConfiguration studioConfiguration, String contentTypeXpath,
                                          String formDefinitionTemplate) {
        super(studioConfiguration, contentTypeXpath, formDefinitionTemplate);
    }

    @Override
    protected void doInit(final HierarchicalConfiguration config) {
        super.doInit(config);
        fieldNameXpath = config.getString(CONFIG_KEY_FIELD_XPATH);
        profileIdXpath = config.getString(CONFIG_KEY_PROFILE_XPATH);
        itemXpath = config.getString(CONFIG_KEY_ITEM_XPATH);
        itemIdXpath = config.getString(CONFIG_KEY_ITEM_ID_XPATH);
        itemNameXpath = config.getString(CONFIG_KEY_ITEM_NAME_XPATH);
        urlElementName = config.getString(CONFIG_KEY_URL_NAME);
        urlTemplate = config.getString(CONFIG_KEY_URL_TEMPLATE);
    }

    @Override
    protected void updateFile(StudioUpgradeContext context, Path file) throws UpgradeException {
        try {
            Document descriptor = loadDocument(file);
            String contentTypeName = (String) select(descriptor, contentTypeXpath, XPathConstants.STRING);
            Path formDefinition = getFormDefinition(context, contentTypeName);
            Document definition = loadDocument(formDefinition);
            NodeList formFields = (NodeList) select(definition, fieldNameXpath, XPathConstants.NODESET);
            logger.debug("Found {0} Box controls for content-type {1}", formFields.getLength(), contentTypeName);
            boolean updated = false;
            for(int i = 0; i < formFields.getLength(); i++) {
                Node formField = formFields.item(i);
                updated = findFields(file, definition, descriptor, formField) || updated;
            }
            if(updated) {
                logger.info("Updating file {0}", file);
                writeFile(file, descriptor);
            }

        } catch (Exception e) {
            throw new UpgradeException("Error updating descriptor for file " + file, e);
        }
    }

    /**
     * Find all fields in the given descriptor that use the Box control
     * @param file the XML file
     * @param definition the form definition of the content-type
     * @param descriptor the item descriptor
     * @param formField the form field
     * @return true if any field was updated
     * @throws XPathExpressionException if there is an error evaluating a XPath selector
     */
    protected boolean findFields(Path file, Document definition, Document descriptor, Node formField)
        throws XPathExpressionException {

        String fieldName = formField.getTextContent();

        Map<String, String> idValue = singletonMap(PLACEHOLDER_ID, fieldName);

        String profileId = (String) select(definition, replace(profileIdXpath, idValue), XPathConstants.STRING);

        NodeList items = (NodeList) select(descriptor, replace(itemXpath, idValue), XPathConstants.NODESET);

        logger.debug("Found {0} Box fields in file {1}", items.getLength(), file);

        boolean updated = false;

        for(int j = 0; j < items.getLength(); j++) {
            Node item = items.item(j);
            updated = updateField(descriptor, item, profileId, fieldName) || updated;
        }

        return updated;
    }

    /**
     * Updates the given field to add the new element if needed
     * @param descriptor the item descriptor
     * @param item the field item
     * @param profileId the profile id
     * @param fieldName the field name
     * @return true if any field was updated
     * @throws XPathExpressionException if there is an error evaluating a XPath selector
     */
    protected boolean updateField(Document descriptor, Node item, String profileId, String fieldName)
        throws XPathExpressionException {
        String fileId = (String) select(item, itemIdXpath, XPathConstants.STRING);
        String fileName = (String) select(item, itemNameXpath, XPathConstants.STRING);

        if((Boolean) select(item, urlElementName, XPathConstants.BOOLEAN)) {
            logger.info("Field {0}/{1} already has a {2} element, it will not be updated",
                fieldName, fileId, urlElementName);
        } else {
            Map<String, String> values = new HashMap<>();
            values.put(PLACEHOLDER_PROFILE, profileId);
            values.put(PLACEHOLDER_ID, fileId);
            values.put(PLACEHOLDER_EXTENSION, FilenameUtils.getExtension(fileName));

            String urlValue = replace(urlTemplate, values);
            logger.debug("Adding url element for field {0}/{1} with value {2}",
                fieldName, fileId, urlValue);

            Element urlNode = descriptor.createElement(urlElementName);
            urlNode.setTextContent(urlValue);
            item.appendChild(urlNode);

            return true;
        }

        return false;
    }

}
