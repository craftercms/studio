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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ProfileService;
import org.craftercms.cstudio.alfresco.to.RoleConfigTO;
import org.craftercms.cstudio.alfresco.to.RolesConfigTO;
import org.craftercms.cstudio.alfresco.to.TimeStamped;
import org.craftercms.cstudio.alfresco.to.UserProfileTO;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author spallam
 *
 */

public class ProfileServiceImpl extends ConfigurableServiceBase implements ProfileService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProfileServiceImpl.class);

	public static final String GROUP_AUTHORITY_TYPE = "GROUP_";
	
	/**
	 * site types and roles configuration mapping
	 */
	protected Map<String, RolesConfigTO> _rolesMapping = new FastMap<String, RolesConfigTO>();

    @Override
    public void register() {
        this._servicesManager.registerService(ProfileService.class, this);
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.service.api.ProfileService#assignUserRole(java.lang.String, java.lang.String, java.lang.String)
      */
	public boolean assignUserRole(String user, String role, String site) {
		String roleName = GROUP_AUTHORITY_TYPE + site + " " + role;
		roleName = roleName.trim();
		PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
		Set<String> users = persistenceManagerService.getContainedAuthorities(
				AuthorityType.USER, roleName, true);
		if (users == null || !users.contains(user)) {
			persistenceManagerService.addAuthority(roleName, user);
			// add users to any parent groups
			Set<String> parentGroups = persistenceManagerService.getContainingAuthorities(
					AuthorityType.GROUP, roleName, true);
			if (parentGroups != null && parentGroups.size() > 0) {
				addUserToParentGroups(parentGroups, user, site);
			}
			LOGGER.debug("Assigned " + role + " role to " + user);
			return true;
		} 
		LOGGER.debug("Failed to assign " + role + " role to " + user);
		return false;
	}

	/**
	 * add user to parent groups
	 * 
	 * @param parentGroups
	 * @param user
	 * @param site
	 */
	protected void addUserToParentGroups(Set<String> parentGroups, 
			String user, String site) {
		boolean isRoleParent = false;
		boolean isUserAssignedToParent = false;
		String siteGroup = GROUP_AUTHORITY_TYPE + site;
		siteGroup = siteGroup.trim();
		isRoleParent = parentGroups.contains(siteGroup);
		if (isRoleParent == false) {
			String parentGroup = parentGroups.toArray()[0].toString();
			if (parentGroup != null) {
                PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
				Set<String> users = persistenceManagerService.getContainedAuthorities(
						AuthorityType.USER, parentGroup, true);
				isUserAssignedToParent = users.contains(user);
				if (isUserAssignedToParent == false) {
					persistenceManagerService.addAuthority(parentGroup, user);
					LOGGER.debug("Assigned " + parentGroup + " role to " + user);
				}
			}
		}
	}


	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ProfileService#removeUserRole(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean removeUserRole(String user, String role, String site) {
		String roleName = GROUP_AUTHORITY_TYPE + site + " " + role;
		roleName = roleName.trim();
		PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
		Set<String> users = persistenceManagerService.getContainedAuthorities(
				AuthorityType.USER, roleName, true);
		if (users != null && users.contains(user)) {
			persistenceManagerService.removeAuthority(roleName, user);
			Set<String> parentGroups = persistenceManagerService.getContainingAuthorities(
					AuthorityType.GROUP, roleName, true);
			if (parentGroups != null && parentGroups.size() > 0) {
				removeUserFromParentGroups(parentGroups, user, site, roleName);
			}
			LOGGER.debug("Removed " + role + " role from " + user);
			return true;
		} 
		LOGGER.debug("Failed to remove " + role + " role from " + user);
		return false;
	}

	
	/**
	 * remove user from parent groups
	 * 
	 * @param parentGroups
	 * @param user
	 * @param site
	 * @param roleName
	 */
	protected void removeUserFromParentGroups(Set<String> parentGroups, 
			String user, String site, String roleName) {
		String siteGroup = GROUP_AUTHORITY_TYPE + site;
		siteGroup = siteGroup.trim();
		boolean isRoleParent = false;
		isRoleParent = parentGroups.contains(siteGroup);
		if (isRoleParent == true) {
			PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
			Set<String> subGroups = persistenceManagerService.getContainedAuthorities(
					AuthorityType.GROUP, roleName, true);
			String childGroup = subGroups.toArray()[0].toString();
			Set<String> usersChildGroup = persistenceManagerService
					.getContainedAuthorities(AuthorityType.USER, childGroup,
							true);
			boolean isUserMemeberOfChild = usersChildGroup.contains(user);
			if (isUserMemeberOfChild == true) {
				persistenceManagerService.removeAuthority(childGroup, user);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ProfileService#getUserProfile(java.lang.String, java.lang.String, boolean)
	 */
	public UserProfileTO getUserProfile(String user, String site, boolean populateUserRoles) {
		UserProfileTO userProfile = new UserProfileTO();
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        boolean createMissingPeople = persistenceManagerService.createMissingPeople();
		persistenceManagerService.setCreateMissingPeople(false);
		try {
			NodeRef userNodeRef = persistenceManagerService.getPerson(user);
			if (userNodeRef != null) {
				populateUserProperties(userNodeRef, userProfile);
				if (populateUserRoles) {
					populateUserRoles(user, site, userProfile);
				}
				LOGGER.debug("Got user profile for " + user + ", site: " + site);
			} else {
				LOGGER.error("User " + user + " not found.");
			}
		} catch (NoSuchPersonException e) {
			LOGGER.error(user + " is not found.");
			return null;
		} finally {
			persistenceManagerService.setCreateMissingPeople(createMissingPeople);
		}
		return userProfile;
	}


	/**
	 * populate user properties in user profile 
	 * 
	 * @param userRef
	 * @param userProfile
	 */
	protected void populateUserProperties(NodeRef userRef, UserProfileTO userProfile) {
		Map<String, String> properties = new HashMap<String, String>();
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
		properties.put(ContentModel.PROP_FIRSTNAME.getLocalName(), 
				(String)persistenceManagerService.getProperty(userRef, ContentModel.PROP_FIRSTNAME));
		properties.put(ContentModel.PROP_LASTNAME.getLocalName(), 
				(String)persistenceManagerService.getProperty(userRef, ContentModel.PROP_LASTNAME));
		properties.put(ContentModel.PROP_EMAIL.getLocalName(), 
				(String)persistenceManagerService.getProperty(userRef, ContentModel.PROP_EMAIL));
		userProfile.setProfile(properties);
	}

	/**
	 * populate user roles in user profile
	 * 
	 * @param user
	 * @param site
	 * @param userProfile
	 */
	protected void populateUserRoles(String user, String site, 
			UserProfileTO userProfile) {
		checkForUpdate(site);
		RolesConfigTO rolesConfig = _rolesMapping.get(site);
		PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);

		if (rolesConfig != null) {
			Set<String> userAuthorities 
				= persistenceManagerService.getAuthoritiesForUser(user);
			if (userAuthorities != null && userAuthorities.size() > 0) {
				int weight = 0;
				String contextualRole = null;
				Map<String, List<String>> userRoles = new FastMap<String, List<String>>();
				for (String authority : userAuthorities) {
					authority = authority.replaceFirst("GROUP_", "");
					RoleConfigTO roleConfig = rolesConfig.getRoles().get(authority);
					if (roleConfig != null) {
						// set the contextual role if the current role's weight is greater
						if (contextualRole == null) { 
							contextualRole = roleConfig.getLabel();
							weight = roleConfig.getWeight();
						} else if (roleConfig.getWeight() < weight) {
							contextualRole = roleConfig.getLabel();
							weight = roleConfig.getWeight();
						} 
						List<String> groupLabels = userRoles.get(roleConfig.getRole());
						if (groupLabels == null) {
							groupLabels = new FastList<String>();
						}
						if (!groupLabels.contains(roleConfig.getLabel())) {
							groupLabels.add(roleConfig.getLabel());
						}
						userRoles.put(roleConfig.getRole(), groupLabels);
					}
				}
				userProfile.setContextual(contextualRole);
				userProfile.setUserRoles(userRoles);
			}
		}
	}

	/**
	 * Check is user has that particular role
	 */
	public boolean checkUserRole(String user, String site, String role) {
		UserProfileTO userProfile = getUserProfile(user, site, true);
		boolean isTrustedUser = false;
		if (userProfile != null) {
			try {
				if (userProfile.getUserRoles() != null) {
					Map<String, List<String>> userRoles = new HashMap<String, List<String>>();
					userRoles = userProfile.getUserRoles();
					Collection<List<String>> values = userRoles.values();
					Iterator<List<String>> itr = values.iterator();
					while (itr.hasNext()) {
						List<String> roles = (List<String>) itr.next();
						isTrustedUser = roles.contains(role);
						if (isTrustedUser) {
							break;
						}
					}
				}
				return isTrustedUser;
			} catch (Exception e) {
				LOGGER.error("Failed to get user role ");
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ProfileService#getUserRef(java.lang.String)
	 */
	public NodeRef getUserRef(String user) {
		NodeRef personRef = null;
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
		boolean createMissingPeople = persistenceManagerService.createMissingPeople();
		persistenceManagerService.setCreateMissingPeople(false);
		try {
			personRef = persistenceManagerService.getPerson(user);
		} catch (NoSuchPersonException e) {
			LOGGER.error(user + " is not found.");
		}
		persistenceManagerService.setCreateMissingPeople(createMissingPeople);
		return personRef;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.impl.ConfigurableServiceBase#getConfigRef(java.lang.String)
	 */
	protected NodeRef getConfigRef(String key) {
		if (!StringUtils.isEmpty(_configPath)) {
			String siteConfigPath = _configPath.replaceFirst(CStudioConstants.PATTERN_SITE, key);
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            return persistenceManagerService.getNodeRef(siteConfigPath + "/" + _configFileName);
		} else {
			LOGGER.error("no configuration file path specified.");
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.impl.ConfigurableServiceBase#getConfiguration(java.lang.String)
	 */
	protected TimeStamped getConfiguration(String key) {
		// key is the site type
		return _rolesMapping.get(key);
	}

    @Override
    protected void removeConfiguration(String key) {
        if (!StringUtils.isEmpty(key)) {
            _rolesMapping.remove(key);
        }
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.service.impl.ConfigurableServiceBase#loadConfiguration(java.lang.String)
      */
	@SuppressWarnings("unchecked")
	protected void loadConfiguration(String key) {
		// key is the site type
		NodeRef configRef = getConfigRef(key);
		PersistenceManagerService persistenceManagerService =getService(PersistenceManagerService.class);
		Document document = persistenceManagerService.loadXml(configRef);
		if (document != null) {
			Element root = document.getRootElement();
			RolesConfigTO rolesConfig = new RolesConfigTO();
			String topGroup = root.valueOf("@name");
			rolesConfig.setTopGroup(topGroup);
			List<Node> groupNodes = root.selectNodes("group");
			Map<String, RoleConfigTO> roles = new FastMap<String, RoleConfigTO>();
			for (Node groupNode : groupNodes) {
				String groupName = groupNode.valueOf("@name");
				if (!StringUtils.isEmpty(groupName)) {
					RoleConfigTO roleConfig = new RoleConfigTO();
					roleConfig.setName(groupName);
					roleConfig.setLabel(groupNode.valueOf("@label"));
					roleConfig.setRole(groupNode.valueOf("@role"));
					int weight = ContentFormatUtils.getIntValue(groupNode.valueOf("@weight"));
					roleConfig.setWeight(weight);
					roles.put(groupName, roleConfig);
				}
			}
			rolesConfig.setRoles(roles);
			rolesConfig.setLastUpdated(new Date());
			_rolesMapping.put(key, rolesConfig);
		}
	}
}
