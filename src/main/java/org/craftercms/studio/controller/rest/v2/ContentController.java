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

package org.craftercms.studio.controller.rest.v2;

import org.craftercms.studio.model.QuickCreateItem;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.ResultList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEMS;
import static org.craftercms.studio.model.rest.ApiResponse.OK;

@RestController
public class ContentController {

    @GetMapping("/api/2/content/quick_create")
    public ResponseBody quickCreate() {
        ResponseBody responseBody = new ResponseBody();
        ResultList<QuickCreateItem> result = new ResultList<QuickCreateItem>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_ITEMS, getMockList());
        responseBody.setResult(result);
        return responseBody;
    }

    private List<QuickCreateItem> getMockList() {
        List<QuickCreateItem> toRet = new ArrayList<QuickCreateItem>();

        QuickCreateItem q1 = new QuickCreateItem();
        q1.setSiteId("mysite");
        q1.setLabel("Content Type 1");
        q1.setContentTypeId("/page/ct1");
        q1.setPath("/site/website");

        QuickCreateItem q2 = new QuickCreateItem();
        q2.setSiteId("mysite");
        q2.setLabel("Content Type 2");
        q2.setContentTypeId("/component/ct2");
        q2.setPath("/site/components/{objectId}/{year}/{month}/{yyyy}/{mm}/{dd}");

        QuickCreateItem q3 = new QuickCreateItem();
        q3.setSiteId("mysite");
        q3.setLabel("Content Type 3");
        q3.setContentTypeId("/page/ct3");
        q3.setPath("/site/website/articles");

        toRet.add(q1);
        toRet.add(q2);
        toRet.add(q3);

        return toRet;
    }

}
