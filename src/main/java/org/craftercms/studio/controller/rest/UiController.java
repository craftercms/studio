package org.craftercms.studio.controller.rest;

import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v2.ui.UiService;
import org.craftercms.studio.model.ApiResponse;
import org.craftercms.studio.model.rest.ApiResponseBody;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.ui.MenuItem;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/2/ui")
public class UiController {

    private UiService uiService;

    @Required
    public void setUiService(UiService uiService) {
        this.uiService = uiService;
    }

    @GetMapping("/views/global-menu")
    public ApiResponseBody getGlobalMenu() throws AuthenticationException, ServiceException {
        ResultList<MenuItem> result = new ResultList<>();
        result.setResponse(ApiResponse.OK);
        result.setEntities(uiService.getGlobalMenu());

        ApiResponseBody responseBody = new ApiResponseBody();
        responseBody.setResult(result);

        return responseBody;
    }

}
