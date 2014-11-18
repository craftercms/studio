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

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.constant.CStudioXmlConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentTypeService;
import org.craftercms.cstudio.alfresco.service.api.PermissionService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ContentTypeConfigTO;
import org.craftercms.cstudio.alfresco.to.PermissionsConfigTO;
import org.craftercms.cstudio.alfresco.to.TimeStamped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Responsible for determining the permissions based on the user and group. The
 * role mappings and permissions are read from files named roleMappingsFileName
 * and permissionsFileName in the path permissionsPath.
 * 
 * @author Sweta Chalasani
 * @author Sandra O'Keeffe
 * 
 */
public class PermissionServiceImpl extends ConfigurableServiceBase implements PermissionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PermissionServiceImpl.class);

	protected String _roleMappingsFileName;
	protected String _permissionsFileName;

	/** permission configuration mapping **/
	protected Map<String, PermissionsConfigTO> _permissionsConfigMap = new HashMap<String, PermissionsConfigTO>();

	protected Set<String> allRoles = null;

    @Override
    public void register() {
        this._servicesManager.registerService(PermissionService.class, this);
    }

    /*
    * (non-Javadoc)
    * @see org.craftercms.crafter.alfresco.service.api.PermissionService#getRoles(java.lang.String)
    */
	public Set<String> getRoles(String site) {
		// load roles again if configuration is updated
		if (this.allRoles == null || super.isConfigUpdated(getPermissionsKey(site, _roleMappingsFileName))) {
			// determine whether to refresh the config
			checkForUpdate(site);
			// get the config files from the permissionsConfigMap based on the key
			PermissionsConfigTO rolesConfig = _permissionsConfigMap.get(getPermissionsKey(site, _roleMappingsFileName));
			if (rolesConfig != null) {
				Set<String> uniqueRoles = new FastSet<String>();
				Map<String, List<String>> rolesMapping = rolesConfig.getRoles();
				if (rolesMapping != null) {
					for (String key : rolesMapping.keySet()) {
						List<String> roles = rolesMapping.get(key);
						if (roles != null) {
							uniqueRoles.addAll(roles);
						}
					}
				}
				this.allRoles = uniqueRoles;
			} else {
				this.allRoles = new FastSet<String>(0);
			}
		} 
		return this.allRoles;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.crafter.alfresco.service.api.PermissionService#getUserRoles
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public Set<String> getUserRoles(String site, String user) {
        
		Set<String> groups = getService(PersistenceManagerService.class).getContainingAuthorities(AuthorityType.GROUP, user, false);
		if (groups != null && groups.size() > 0) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Groups for " + user + " in " + site + ": " + groups);
			}
			// determine whether to refresh the config
			checkForUpdate(site);
			// get the config files from the permissionsConfigMap based on the key
			PermissionsConfigTO rolesConfig = _permissionsConfigMap.get(getPermissionsKey(site, _roleMappingsFileName));
			Set<String> userRoles = new FastSet<String>();
			if (rolesConfig != null) {
				Map<String, List<String>> rolesMap = rolesConfig.getRoles();
				for (String group : groups) {
					String groupName = group.replaceFirst("GROUP_", "");
					List<String> roles = rolesMap.get(groupName);
					if (roles != null) {
						userRoles.addAll(roles);
					}
				}
			}
			return userRoles;
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("No groups found for " + user + " in " + site);
			}
		}
		return new FastSet<String>(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.craftercms.crafter.alfresco.service.api.PermissionService#
	 * getUserPermissions(java.lang.String, java.lang.String, java.lang.String,
	 * java.util.List)
	 */
	@Override
	public Set<String> getUserPermissions(String site, String path, String user, List<String> groups) {

		// determine whether to refresh the config
		checkForUpdate(site);

		// get the config files from the permissionsConfigMap based on the key
		PermissionsConfigTO rolesConfig = _permissionsConfigMap.get(getPermissionsKey(site, _roleMappingsFileName));
		PermissionsConfigTO permissionsConfig = _permissionsConfigMap.get(getPermissionsKey(site, _permissionsFileName));
		Set<String> roles = new FastSet<String>();
		addUserRoles(roles, site, user);
		addGroupRoles(roles, site, groups, rolesConfig);
		// resolve the permission
		Set<String> permissions = populateUserPermissions(site, path, roles, permissionsConfig);
		// check if the user is allowed to edit the content 
		try {
            DmContentTypeService dmContentTypeService = getService(DmContentTypeService.class);
			ContentTypeConfigTO config = dmContentTypeService.getContentTypeByRelativePath(site, null, path);
			boolean isAllowed = dmContentTypeService.isUserAllowed(roles, config);
			if (!isAllowed) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("The user is not allowed to access " + config.getName() + ". adding permission: " + CStudioConstants.PERMISSION_VALUE_NOT_ALLOWED);
				}
				// If no default role is set
				permissions.add(CStudioConstants.PERMISSION_VALUE_NOT_ALLOWED);
				return permissions;
			}
		} catch (ServiceException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Error while getting the content type of " + path + ". skipping user role checking on the content.");
			}
		}
		return permissions;
	}

	/**
	 * add user roles 
	 * 
	 * @param roles
	 * @param site
	 * @param user
	 */
	protected void addUserRoles(Set<String> roles, String site, String user) {
		if (!StringUtils.isEmpty(user)) {
			Set<String> userRoles = this.getUserRoles(site, user);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding roles by user: " + userRoles);
			}
			roles.addAll(userRoles);
		} 
	}


	/**
	 * get roles by groups 
	 * 
	 * @param site
	 * @param groups
	 * @param rolesConfig 
	 * @return
	 */
	protected void addGroupRoles(Set<String> roles, String site, List<String> groups, PermissionsConfigTO rolesConfig) {
		if (groups != null) {
			Map<String, List<String>> rolesMap = rolesConfig.getRoles();
			for (String group : groups) {
				List<String> groupRoles = rolesMap.get(group);
				if (groupRoles != null) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Adding roles by group " + group + ": " + roles);
					}
					roles.addAll(groupRoles);
				}
			}
		}
	}


	/**
	 * populate user permissions
	 * 
	 * @param site
	 * @param path
	 * @param roles
	 * @param permissionsConfig
	 */
	protected Set<String> populateUserPermissions(String site, String path, Set<String> roles,
			PermissionsConfigTO permissionsConfig) {
		Set<String> permissions = new FastSet<String>();
		if (roles != null && !roles.isEmpty()) {
			for (String role : roles) {
				Map<String, Map<String, List<Node>>> permissionsMap = permissionsConfig.getPermissions();
				Map<String, List<Node>> siteRoles = permissionsMap.get(site);
				if (siteRoles == null || siteRoles.isEmpty()) {
					siteRoles = permissionsMap.get("*");
				}
				if (siteRoles != null && !siteRoles.isEmpty()) {
					List<Node> ruleNodes = siteRoles.get(role);
					if (ruleNodes == null || ruleNodes.isEmpty()) {
						ruleNodes = siteRoles.get("*");
					}
					if (ruleNodes != null && !ruleNodes.isEmpty()) {
						for (Node ruleNode : ruleNodes) {
							String regex = ruleNode.valueOf(CStudioXmlConstants.DOCUMENT_ATTR_REGEX);
							if (path.matches(regex)) {
								if (LOGGER.isDebugEnabled()) {
									LOGGER.debug("Permissions found by matching " + regex + " for " + role + " in " + site);
								}
								List<Node> permissionNodes = ruleNode.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_ALLOWED_PERMISSIONS);
								for (Node permissionNode : permissionNodes) {
									String permission = permissionNode.getText().toLowerCase();
									if (LOGGER.isDebugEnabled()) {
										LOGGER.debug("adding permissions " + permission + " to " + path + " for " + role + " in " + site);
									}
									permissions.add(permission);
								}
							}
						}
					} else {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("No default role is set. adding default permission: " + CStudioConstants.PERMISSION_VALUE_READ);
						}
						// If no default role is set
						permissions.add(CStudioConstants.PERMISSION_VALUE_READ);
					}
				} else {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("No default site is set. adding default permission: " + CStudioConstants.PERMISSION_VALUE_READ);
					}
					// If no default site is set
					permissions.add(CStudioConstants.PERMISSION_VALUE_READ);
				}
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("No user or group matching found. adding default permission: " + CStudioConstants.PERMISSION_VALUE_READ);
			}
			// If user or group did not match the roles-mapping file
			permissions.add(CStudioConstants.PERMISSION_VALUE_READ);
		}
		return permissions;
	}

	/*
	 * Responsible for refreshing the PermissionConfigTO objects that contain
	 * the mappings (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.crafter.alfresco.service.impl.ConfigurableServiceBase#
	 * loadConfiguration(java.lang.String)
	 */
	@Override
	protected void loadConfiguration(String key) {

		NodeRef configRef = getConfigRef(key);
		PersistenceManagerService persistenceManagerService =getService(PersistenceManagerService.class);
		Document document = persistenceManagerService.loadXml(configRef);
		if (document != null) {
			PermissionsConfigTO config = new PermissionsConfigTO();
			config.setMapping(document);
			Element root = document.getRootElement();

			// roles file
			loadRoles(root, config);

			// permissions file
			loadPermissions(root, config);

			config.setKey(key);
			config.setLastUpdated(new Date());

			_permissionsConfigMap.put(key, config);
		} else {
            if (LOGGER.isErrorEnabled()) {
			    LOGGER.error("Permission mapping not found for " + key);
            }
		}
	}

	@SuppressWarnings("unchecked")
	protected void loadRoles(Element root, PermissionsConfigTO config) {
		if (root.getName().equals(CStudioXmlConstants.DOCUMENT_ROLE_MAPPINGS)) {
			Map<String, List<String>> rolesMap = new FastMap<String, List<String>>();

			List<Node> userNodes = root.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_USER_NODE);
			rolesMap = getRoles(userNodes, rolesMap);

			List<Node> groupNodes = root.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_GROUPS_NODE);
			rolesMap = getRoles(groupNodes, rolesMap);

			config.setRoles(rolesMap);
		}
	}

	@SuppressWarnings("unchecked")
	protected Map<String, List<String>> getRoles(List<Node> nodes, Map<String, List<String>> rolesMap) {
		for (Node node : nodes) {
			String name = node.valueOf(CStudioXmlConstants.DOCUMENT_ATTR_PERMISSIONS_NAME);
			if (!StringUtils.isEmpty(name)) {
				List<Node> roleNodes = node.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_PERMISSION_ROLE);
				List<String> roles = new FastList<String>();
				for (Node roleNode : roleNodes) {
					roles.add(roleNode.getText());
				}
				rolesMap.put(name, roles);
			}
		}
		return rolesMap;
	}

	@SuppressWarnings("unchecked")
	protected void loadPermissions(Element root, PermissionsConfigTO config) {
		if (root.getName().equals(CStudioXmlConstants.DOCUMENT_PERMISSIONS)) {
			Map<String, Map<String, List<Node>>> permissionsMap = new FastMap<String, Map<String, List<Node>>>();
			List<Node> siteNodes = root.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_SITE);
			for (Node siteNode : siteNodes) {
				String siteId = siteNode.valueOf(CStudioXmlConstants.DOCUMENT_ATTR_SITE_ID);
				if (!StringUtils.isEmpty(siteId)) {
					List<Node> roleNodes = siteNode.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_PERMISSION_ROLE);
					Map<String, List<Node>> rules = new FastMap<String, List<Node>>();
					for (Node roleNode : roleNodes) {
						String roleName = roleNode.valueOf(CStudioXmlConstants.DOCUMENT_ATTR_PERMISSIONS_NAME);
						List<Node> ruleNodes = roleNode.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_PERMISSION_RULE);
						rules.put(roleName, ruleNodes);
					}
					permissionsMap.put(siteId, rules);
				}
			}
			config.setPermissions(permissionsMap);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.crafter.alfresco.service.impl.ConfigurableServiceBase#
	 * getConfiguration(java.lang.String)
	 */
	@Override
	protected TimeStamped getConfiguration(String key) {

		return _permissionsConfigMap.get(key);
	}

    @Override
    protected void removeConfiguration(String key) {
        if (!StringUtils.isEmpty(key)) {
            _permissionsConfigMap.remove(key);
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * org.craftercms.crafter.alfresco.service.impl.ConfigurableServiceBase#
      * getConfigRef(java.lang.String)
      */
	@Override
	protected NodeRef getConfigRef(String key) {
		String siteConfigPath = _configPath.replaceFirst(CStudioConstants.PATTERN_SITE, getSiteFromKey(key));
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        return persistenceManagerService.getNodeRef(siteConfigPath + "/" + getFilenameFromKey(key));
	}

	/*
	 * Checks for updates to both the role & permission mappings files
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.crafter.alfresco.service.impl.ConfigurableServiceBase#
	 * checkForUpdate(java.lang.String)
	 */
	@Override
	protected void checkForUpdate(String site) {
		super.checkForUpdate(getPermissionsKey(site, _roleMappingsFileName));
		super.checkForUpdate(getPermissionsKey(site, _permissionsFileName));
	}

	/* Derives the site from the key */
	protected String getSiteFromKey(String key) {
		return key.substring(0, key.indexOf(File.pathSeparator));
	}

	/* Derives the filename from the key */
	protected String getFilenameFromKey(String key) {
		return key.substring(key.indexOf(File.pathSeparator) + 1);
	}

	/* Derives a key based off the site and filename */
	protected String getPermissionsKey(String site, String filename) {
		return new StringBuffer(site).append(File.pathSeparator).append(filename).toString();
	}

	public void setRoleMappingsFileName(String roleMappingsFileName) {
		this._roleMappingsFileName = roleMappingsFileName;
	}

	public void setPermissionsFileName(String permissionsFileName) {
		this._permissionsFileName = permissionsFileName;
	}
}
