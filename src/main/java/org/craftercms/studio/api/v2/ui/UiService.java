package org.craftercms.studio.api.v2.ui;

import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.model.ui.MenuItem;

import java.util.List;

/**
 * Service that provides the UI elements the current user has access to.
 *
 * @author avasquez
 */
public interface UiService {

    /**
     * Returns the global menu items available to the current user.
     *
     * @return the list of menu items
     *
     * @throws AuthenticationException if not user is logged in
     * @throws ServiceException if another error occurs
     */
    List<MenuItem> getGlobalMenu() throws AuthenticationException, ServiceException;

}
