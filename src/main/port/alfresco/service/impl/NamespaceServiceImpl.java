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
package org.craftercms.cstudio.alfresco.service.impl;

import java.util.Collection;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;

import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.NamespaceService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;


public class NamespaceServiceImpl extends AbstractRegistrableService implements NamespaceService {

    @Override
    public void register() {
        getServicesManager().registerService(NamespaceService.class, this);
    }

    
	
	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.NamespaceService#createContentQName(java.lang.String)
	 */
	public QName createContentName(String contentName) {
		if (StringUtils.isEmpty(contentName)) {
			return null;
		} else {
			String qName = QName.createValidLocalName(contentName);
			return QName.createQName(
					org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI, 
					qName);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.NamespaceService#getPrefixes(java.lang.String)
	 */
	public Collection<String> getPrefixes(String namespaceURI) {
        org.alfresco.service.namespace.NamespaceService alfrescoNamespaceService = getServicesManager().getService(org.alfresco.service.namespace.NamespaceService.class);
		return alfrescoNamespaceService.getPrefixes(namespaceURI);
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.NamespaceService#getPrefixedTypeName(org.alfresco.service.cmr.repository.NodeRef)
	 * 
	 * This will return "cstudio-rdy:readinessArticle" as an example
	 */
	public String getPrefixedTypeName(QName type) {
		if (type != null) {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
			TypeDefinition typeDef = persistenceManagerService.getType(type);
			if (typeDef != null) {
				return typeDef.getName().getPrefixString();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.NamespaceService#getPrefixedPropertyName(org.alfresco.service.namespace.QName)
	 */
	public String getPrefixedPropertyName(QName property) {
		
		if (property != null) {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
			PropertyDefinition propertyDef = persistenceManagerService.getProperty(property);
			if (propertyDef != null) {
				return propertyDef.getName().getPrefixString();
			} else {
				String propParts[] = property.getLocalName().split("\\.");
				if (propParts != null && propParts.length > 1) {
					PropertyDefinition propDef = persistenceManagerService.getProperty(persistenceManagerService.createQName(QName.splitPrefixedQName(property.toPrefixString())[0].concat(":").concat(propParts[0])));
					if (propDef != null) {
						StringBuilder propSB = new StringBuilder(propDef.getName().getPrefixString());
						
						for (int i = 1; i < propParts.length; i++) {
							propSB.append('.').append(propParts[i]);
						}
						
						return propSB.toString();
					}
				}
			}
		}
		return null;		
	}



	// Some static methods uses this call
	@Override
	public QName createQName(String prefixedQName) {
		 return getService(PersistenceManagerService.class).createQName(prefixedQName);
	}

}
