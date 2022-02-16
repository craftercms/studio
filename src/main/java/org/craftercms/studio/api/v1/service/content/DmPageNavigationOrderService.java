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
package org.craftercms.studio.api.v1.service.content;


import org.dom4j.Document;

/**
 *
 * Order Service for Navigation Pages
 *
 * @author shankark
 * @author Dejan Brkic
 *
 */
public interface DmPageNavigationOrderService {

    /**
     * Return new navigation order
     */
    double getNewNavOrder(String site, String path);


    double getNewNavOrder(String site, String path, double currentMaxNavOrder);

    /**
     * Always adds/overwrites the Document element with new unique nav order
     *
     * @param doc
     * @return true if document was updated with new nav order
     */
    boolean addNavOrder(String site, String path, Document doc);

    /**
     * Updates the Document element with new unique nav order if one does not exist
     *
     * @param document
     * @return true if document was updated with new nav order
     */
    boolean updateNavOrder(String site, String path, Document document);

    void deleteSequencesForSite(String site);

    int getPageNavigationOrderIncrement();
}
