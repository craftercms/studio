package org.craftercms.studio.impl.v2.service.ui.internal;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.model.ui.MenuItem;

import java.util.List;
import java.util.Set;

/**
 * Internal version of the {@link org.craftercms.studio.api.v2.service.ui.UiService}.
 *
 * @author avasquez
 */
public interface UiServiceInternal {

    /**
     * Returns the global menu items available based on the specified permissions
     *
     * @param permissions the permissions that restrict what menu items the user has access to
     *
     * @return the list of menu items
     *
     * @throws ServiceLayerException if another error occurs
     */
    List<MenuItem> getGlobalMenu(Set<String> permissions) throws ServiceLayerException;

}
