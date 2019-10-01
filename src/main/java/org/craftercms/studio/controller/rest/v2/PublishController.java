package org.craftercms.studio.controller.rest.v2;

import org.apache.commons.collections.CollectionUtils;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.PublishingPackage;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.model.rest.CancelPublishingPackagesRequest;
import org.craftercms.studio.model.rest.PaginatedResultList;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultOne;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_ENVIRONMENT;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_LIMIT;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_OFFSET;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_PACKAGE_ID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_PATH;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SITEID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_STATE;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_PACKAGE;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_PACKAGES;
import static org.craftercms.studio.model.rest.ApiResponse.OK;

@RestController
@RequestMapping("/api/2/publish")
public class PublishController {

    private PublishService publishService;

    @GetMapping("/packages")
    public ResponseBody getPublishingPackages(@RequestParam(name = REQUEST_PARAM_SITEID, required = true) String siteId,
                                              @RequestParam(name = REQUEST_PARAM_ENVIRONMENT, required = false)
                                                      String environment,
                                              @RequestParam(name = REQUEST_PARAM_PATH, required = false) String path,
                                              @RequestParam(name = REQUEST_PARAM_STATE, required = false) String state,
                                              @RequestParam(name = REQUEST_PARAM_OFFSET, required = false,
                                                      defaultValue = "0") int offset,
                                              @RequestParam(name = REQUEST_PARAM_LIMIT, required = false,
                                                      defaultValue = "10") int limit) throws SiteNotFoundException {
        int total = publishService.getPublishingPackagesTotal(siteId, environment, path, state);
        List<PublishingPackage> packages = new ArrayList<PublishingPackage>();
        if (total > 0) {
            packages = publishService.getPublishingPackages(siteId, environment, path, state, offset, limit);
        }

        ResponseBody responseBody = new ResponseBody();
        PaginatedResultList<PublishingPackage> result = new PaginatedResultList<PublishingPackage>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isEmpty(packages) ? 0 : packages.size());
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_PACKAGES, packages);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping("/package")
    public ResponseBody getPublishingPackageDetails(@RequestParam(name = REQUEST_PARAM_SITEID) String siteId,
                                                    @RequestParam(name = REQUEST_PARAM_PACKAGE_ID) String packageId)
            throws SiteNotFoundException {
        PublishingPackageDetails publishingPackageDetails =
                publishService.getPublishingPackageDetails(siteId, packageId);
        ResponseBody responseBody = new ResponseBody();
        ResultOne<PublishingPackageDetails> result = new ResultOne<PublishingPackageDetails>();
        result.setEntity(RESULT_KEY_PACKAGE, publishingPackageDetails);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping("/cancel")
    public ResponseBody cancelPublishingPackages(
            @RequestBody CancelPublishingPackagesRequest cancelPublishingPackagesRequest) throws SiteNotFoundException {
        publishService.cancelPublishingPackages(cancelPublishingPackagesRequest.getSiteId(),
                cancelPublishingPackagesRequest.getPackageIds());
        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    public PublishService getPublishService() {
        return publishService;
    }

    public void setPublishService(PublishService publishService) {
        this.publishService = publishService;
    }
}
