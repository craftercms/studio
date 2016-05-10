/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
 *
 */

package org.craftercms.studio.impl.v1.service.security;

import org.craftercms.commons.crypto.CipherUtils;
import org.craftercms.commons.crypto.CryptoUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.dal.Group;
import org.craftercms.studio.api.v1.dal.SecurityMapper;
import org.craftercms.studio.api.v1.dal.User;
import org.craftercms.studio.api.v1.dal.UserSession;
import org.craftercms.studio.api.v1.ebus.RepositoryEventContext;
import org.craftercms.studio.api.v1.job.CronJobContext;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;
import java.util.*;

public class DbSecurityProvider implements SecurityProvider {

    @Override
    public Set<String> getUserGroups(String user) {
        Set<String> userGroups = new HashSet<String>();
        List<Group> groups = securityMapper.getUserGroups(user);
        for (Group g : groups) {
            userGroups.add(g.getName());
        }
        return userGroups;
    }

    @Override
    public String getCurrentUser() {
        String username = null;
        RequestContext context = RequestContext.getCurrent();

        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            username = (String)httpSession.getAttribute("studio_user");
        }

        return username;
    }

    @Override
    public Map<String, String> getUserProfile(String user) {
        User u = securityMapper.getUser(user);
        Map<String, String> userProfile = new HashMap<String, String>();
        if (u != null) {
            userProfile.put("userName", u.getUsername());
            userProfile.put("firstName", u.getFirstname());
            userProfile.put("lastName", u.getLastname());
            userProfile.put("email", u.getEmail());
        }
        return userProfile;
    }

    @Override
    public String authenticate(String username, String password) {
        User user = securityMapper.getUser(username);
        if (user != null && CipherUtils.matchPassword(user.getPassword(), password)) {
            byte[] randomBytes = CryptoUtils.generateRandomBytes(20);
            String token = randomBytes.toString();
            Calendar expires = Calendar.getInstance();
            expires.add(Calendar.MINUTE, sessionTimeout);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("token", token);
            params.put("username", username);
            params.put("expires", expires.getTime());
            securityMapper.createUserSession(params);
            storeSessionTicket(token);
            storeSessionUsername(username);
            return token;
        } else {
            return null;
        }
    }

    protected void storeSessionTicket(String ticket) {
        RequestContext context = RequestContext.getCurrent();

        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            httpSession.setAttribute("studio_ticket", ticket);
        }
    }

    protected void storeSessionUsername(String username) {
        RequestContext context = RequestContext.getCurrent();

        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            httpSession.setAttribute("studio_user", username);
        }
    }

    @Override
    public boolean validateTicket(String ticket) {
        if (ticket == null) {
            ticket = getCurrentToken();
        }
        UserSession us = securityMapper.getUserSession(ticket);
        boolean valid = us != null && us.getExpires().after(new Date());
        if (valid) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, sessionTimeout);
            us.setExpires(calendar.getTime());
            securityMapper.extendSession(us);
        } else {
            if (us != null) {
                securityMapper.deactivateSession(ticket);
            }
        }
        return valid;
    }

    @Override
    public void addUserGroup(String groupName) {

    }

    @Override
    public void addUserGroup(String parentGroup, String groupName) {

    }

    @Override
    public String getCurrentToken() {
        String ticket;
        RequestContext context = RequestContext.getCurrent();

        if (context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            ticket = (String)httpSession.getAttribute("studio_ticket");
        } else {
            ticket = getJobOrEventTicket();
        }

        if (ticket == null) {
            ticket = "NOTICKET";
        }

        return ticket;
    }

    protected String getJobOrEventTicket() {
        String ticket = null;
        CronJobContext cronJobContext = CronJobContext.getCurrent();

        if (cronJobContext != null) {
            ticket = cronJobContext.getAuthenticationToken();
        } else {
            RepositoryEventContext repositoryEventContext = RepositoryEventContext.getCurrent();
            if (repositoryEventContext != null) {
                ticket = repositoryEventContext.getAuthenticationToken();
            }
        }

        return ticket;
    }

    @Override
    public void addUserToGroup(String groupName, String user) {

    }

    @Override
    public boolean logout() {
        String token = getCurrentToken();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("token", token);
        securityMapper.destroySession(params);
        storeSessionTicket(null);
        storeSessionUsername(null);
        return true;
    }

    @Override
    public void addContentWritePermission(String path, String group) {

    }

    @Override
    public void addConfigWritePermission(String path, String group) {

    }

    public String getPasswordHash(String password) {
        return CipherUtils.hashPassword(password);
    }

    @Autowired
    protected SecurityMapper securityMapper;

    protected int sessionTimeout = 15;

    public int getSessionTimeout() { return sessionTimeout; }
    public void setSessionTimeout(int sessionTimeout) { this.sessionTimeout = sessionTimeout; }
}
