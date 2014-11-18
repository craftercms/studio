/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.dm.service.impl;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class AvmNodePropertyBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AvmNodePropertyBuilder.class);

    protected AVMNodeDescriptor _node;
    protected AVMService _avmService;
    protected Map<QName, PropertyValue> _propertyValueMap;

    public AvmNodePropertyBuilder(AVMNodeDescriptor node, AVMService avmService) {
        this._node = node;
        this._avmService = avmService;
    }

    public AvmNodePropertyBuilder removeProperty(QName qName) {
        _avmService.deleteNodeProperty(_node.getPath(), qName);
        return this;
    }

    public Date getDateProperty(QName qname) {
        PropertyValue value = _avmService.getNodeProperty(-1, _node.getPath(), qname);
        if (value != null) {
            return (Date) value.getValue(DataTypeDefinition.DATE);
        }
        return null;
    }

    public AvmNodePropertyBuilder addDateProperty(QName qname, Date date) {
        if (!isDirectory(_node)) {
            _avmService.setNodeProperty(_node.getPath(), qname, new PropertyValue(DataTypeDefinition.DATE, date));
        } else {
            logger.warn("Skip add date property on node:[" + _node.getPath() + "] value:[" + date
                    + "] as  its a directory");
        }
        return this;
    }

    public String getTextProperty(QName qname) {
        PropertyValue value = _avmService.getNodeProperty(-1, _node.getPath(), qname);
        if (value != null) {
            return value.getStringValue();
        }
        return null;
    }

    public AvmNodePropertyBuilder addTextProperty(QName qname, String value) {
        if (!isDirectory(_node)) {
            _avmService.setNodeProperty(_node.getPath(), qname, new PropertyValue(DataTypeDefinition.TEXT, value));
        } else {
            logger.warn("Skip add text property on node:[" + _node.getPath() + "] value:[" + value
                    + "] as its a directory");
        }
        return this;
    }

    public boolean getBooleanProperty(QName qname) {
        PropertyValue value = _avmService.getNodeProperty(-1, _node.getPath(), qname);
        return value != null && value.getBooleanValue();
    }

    public AvmNodePropertyBuilder addBooleanProperty(QName qname, boolean value) {
        if (!isDirectory(_node)) {
            _avmService.setNodeProperty(_node.getPath(), qname, new PropertyValue(DataTypeDefinition.BOOLEAN, value));
        } else {
            logger.warn("Skip add  boolean property on node:[" + _node.getPath() + "] value:[" + value
                    + "] as its a directory");
        }

        return this;
    }

    protected boolean isDirectory(AVMNodeDescriptor node) {
        if (node != null && node.isDirectory()) {
            return true;
        }
        return false;
    }
}
