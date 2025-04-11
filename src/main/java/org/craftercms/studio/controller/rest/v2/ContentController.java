/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.controller.rest.v2;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.ValidationException;
import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.ValidExistingContentPath;
import org.craftercms.commons.validation.annotations.param.ValidSiteId;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.QuickCreateItem;
import org.craftercms.studio.api.v2.dal.item.ContentItem;
import org.craftercms.studio.api.v2.dal.item.LightItem;
import org.craftercms.studio.api.v2.service.clipboard.ClipboardService;
import org.craftercms.studio.api.v2.service.content.ContentService;
import org.craftercms.studio.api.v2.service.content.ContentTypeService;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.model.history.ItemVersion;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.clipboard.DuplicateRequest;
import org.craftercms.studio.model.rest.clipboard.PasteRequest;
import org.craftercms.studio.model.rest.content.*;
import org.craftercms.studio.model.rest.content.GetChildrenBulkRequest.PathParams;
import org.dom4j.Document;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.beans.ConstructorProperties;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.ALPHANUMERIC;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.*;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.*;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.*;
import static org.craftercms.studio.model.rest.ApiResponse.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequestMapping(API_2 + CONTENT)
public class ContentController {

	private final ContentService contentService;
	private final ContentTypeService contentTypeService;
	private final DependencyService dependencyService;

	//TODO: Migrate logic to new content service
	private final ClipboardService clipboardService;

	@ConstructorProperties({"contentService", "dependencyService", "clipboardService", "contentTypeService"})
	public ContentController(ContentService contentService, DependencyService dependencyService,
							 ClipboardService clipboardService, ContentTypeService contentTypeService) {
		this.contentService = contentService;
		this.dependencyService = dependencyService;
		this.clipboardService = clipboardService;
		this.contentTypeService = contentTypeService;
	}

	@GetMapping(value = EXISTS, produces = APPLICATION_JSON_VALUE)
	public ResultOne<Boolean> contentExists(@NotEmpty @ValidSiteId @RequestParam String siteId,
						@ValidExistingContentPath @ValidateSecurePathParam @RequestParam String path)
		throws SiteNotFoundException {
		var result = new ResultOne<Boolean>();
		result.setEntity(RESULT_KEY_EXISTS, contentService.contentExists(siteId, path));
		result.setResponse(OK);
		return result;
	}

	@GetMapping(LIST_QUICK_CREATE_CONTENT)
	public ResultList<QuickCreateItem> listQuickCreateContent(@ValidSiteId @RequestParam(name = "siteId") String siteId)
		throws ServiceLayerException {
		List<QuickCreateItem> items = contentTypeService.getQuickCreatableContentTypes(siteId);
		ResultList<QuickCreateItem> result = new ResultList<>();
		result.setResponse(OK);
		result.setEntities(RESULT_KEY_ITEMS, items);
		return result;
	}

	@PostMapping(GET_DELETE_PACKAGE)
	public ResultOne<Map<String, List<LightItem>>> getDeletePackage(@RequestBody @Valid GetDeletePackageRequestBody request) throws SiteNotFoundException {
		List<LightItem> childItems = contentService.getChildItems(request.getSiteId(), request.getPaths());
		List<LightItem> dependentItems = dependencyService.getDependentPaths(request.getSiteId(), request.getPaths());
		ResultOne<Map<String, List<LightItem>>> result = new ResultOne<>();
		result.setResponse(OK);
		Map<String, List<LightItem>> items = new HashMap<>();
		items.put(RESULT_KEY_CHILD_ITEMS, childItems);
		items.put(RESULT_KEY_DEPENDENT_ITEMS, dependentItems);
		result.setEntity(RESULT_KEY_ITEMS, items);
		return result;
	}

	@PostMapping(value = DELETE, consumes = APPLICATION_JSON_VALUE)
	public Result delete(@RequestBody @Validated DeleteRequestBody deleteRequestBody)
		throws UserNotFoundException, ServiceLayerException, AuthenticationException {
		List<String> items = new ArrayList<>(deleteRequestBody.getItems());
		if (isNotEmpty(deleteRequestBody.getOptionalDependencies())) {
			items.addAll(deleteRequestBody.getOptionalDependencies());
		}

		contentService.deleteContent(deleteRequestBody.getSiteId(),
			items, deleteRequestBody.getTitle(),
			deleteRequestBody.getComment());
		var result = new Result();
		result.setResponse(OK);
		return result;
	}

	@PostMapping(value = GET_CHILDREN_BY_PATHS, produces = APPLICATION_JSON_VALUE)
	public Result getChildrenByPaths(@PathVariable @ValidSiteId String siteId, @Valid @RequestBody GetChildrenBulkRequest request)
		throws ServiceLayerException, UserNotFoundException {
		Map<String, PathParams> paramsMap = request.getPaths().stream()
			.collect(toMap(PathParams::getPath, identity()));
		GetChildrenByPathsBulkResult children = contentService.getChildrenByPaths(siteId,
			new ArrayList<>(paramsMap.keySet()), paramsMap);
		Result result = UnwrappedResult.of(children);
		result.setResponse(OK);
		return result;
	}

	@GetMapping(value = GET_DESCRIPTOR, produces = APPLICATION_JSON_VALUE)
	public ResultOne<String> getDescriptor(@NotEmpty @ValidSiteId @RequestParam String siteId,
					       @ValidExistingContentPath @ValidateSecurePathParam @RequestParam String path,
					       @RequestParam(required = false, defaultValue = "false") boolean flatten) throws
		ContentNotFoundException, SiteNotFoundException {
		Document descriptor = contentService.getItemDescriptor(siteId, path, flatten);
		var result = new ResultOne<String>();
		result.setResponse(OK);
		result.setEntity(RESULT_KEY_XML, descriptor.asXML());
		return result;
	}

	@PostMapping(value = PASTE_ITEMS, produces = APPLICATION_JSON_VALUE)
	public ResultList<String> pasteItems(@Valid @RequestBody PasteRequest request) throws Exception {
		var result = new ResultList<String>();
		result.setResponse(OK);
		result.setEntities(RESULT_KEY_ITEMS,
			clipboardService.pasteItems(request.getSiteId(), request.getOperation(),
				request.getTargetPath(), request.getItem()));

		return result;
	}

	@PostMapping(value = DUPLICATE_ITEM, produces = APPLICATION_JSON_VALUE)
	public ResultOne<String> duplicateItem(@Valid @RequestBody DuplicateRequest request) throws Exception {
		var result = new ResultOne<String>();
		result.setResponse(OK);
		result.setEntity(RESULT_KEY_ITEM,
			clipboardService.duplicateItem(request.getSiteId(), request.getPath()));

		return result;
	}

	@GetMapping(value = ITEM_BY_PATH, produces = APPLICATION_JSON_VALUE)
	public ResultOne<ContentItem> getItemByPath(@ValidSiteId
						     @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
						     @ValidExistingContentPath
						     @RequestParam(value = REQUEST_PARAM_PATH) String path,
						     @RequestParam(value = REQUEST_PARAM_PREFER_CONTENT, required = false,
							     defaultValue = "false") boolean preferContent)
		throws ServiceLayerException, UserNotFoundException {
		ContentItem detailedItem = contentService.getItemByPath(siteId, path, preferContent);
		ResultOne<ContentItem> result = new ResultOne<>();
		result.setEntity(RESULT_KEY_ITEM, detailedItem);
		result.setResponse(OK);
		return result;
	}

	@PostMapping(value = SANDBOX_ITEMS_BY_PATH, produces = APPLICATION_JSON_VALUE)
	public GetContentItemsByPathResult getSandboxItemsByPath(@RequestBody @Valid GetSandboxItemsByPathRequestBody request)
		throws ServiceLayerException, UserNotFoundException {
		String siteId = request.getSiteId();
		Collection<String> missing = Collections.emptyList();
		List<String> paths = request.getPaths();
		boolean preferContent = request.isPreferContent();
		List<ContentItem> sandboxItems = contentService.getContentItemsByPath(siteId, paths, preferContent);

		if (CollectionUtils.isEmpty(sandboxItems) || paths.size() != sandboxItems.size()) {
			List<String> found = sandboxItems.stream().map(ContentItem::getPath).collect(Collectors.toList());
			if (preferContent) {
				found.addAll(sandboxItems.stream().map(si -> StringUtils.replace(si.getPath(),
					FILE_SEPARATOR + INDEX_FILE, "")).toList());
			}
			missing = CollectionUtils.subtract(paths, found);
		}

		GetContentItemsByPathResult result = new GetContentItemsByPathResult();
		result.setEntities(RESULT_KEY_ITEMS, sandboxItems);
		result.setMissingItems(missing);
		result.setResponse(OK);
		return result;
	}

	@PostMapping(ITEM_LOCK_BY_PATH)
	public Result itemLockByPath(@RequestBody @Valid LockItemByPathRequest request)
		throws UserNotFoundException, ServiceLayerException {
		contentService.lockContent(request.getSiteId(), request.getPath());
		Result result = new Result();
		result.setResponse(OK);
		return result;
	}

	@PostMapping(ITEM_UNLOCK_BY_PATH)
	public Result itemUnlockByPath(@RequestBody @Valid UnlockItemByPathRequest request)
		throws ContentNotFoundException, SiteNotFoundException {
		contentService.unlockContent(request.getSiteId(), request.getPath());
		Result result = new Result();
		result.setResponse(OK);
		return result;
	}

	@Valid
	@GetMapping(GET_CONTENT_BY_COMMIT_ID)
	public ResponseEntity<Resource> getContentByCommitId(@ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
							     @ValidExistingContentPath @RequestParam(value = REQUEST_PARAM_PATH) String path,
							     @EsapiValidatedParam(type = ALPHANUMERIC) @RequestParam(value = REQUEST_PARAM_COMMIT_ID) String commitId)
		throws ServiceLayerException, UserNotFoundException {
		Resource resource = contentService.getContentByCommitId(siteId, path, commitId).orElseThrow();

		String mimeType = StudioUtils.getMimeType(path);
		return ResponseEntity
			.ok()
			.contentType(MediaType.parseMediaType(mimeType))
			.body(resource);
	}

	@PostMapping(value = RENAME, consumes = APPLICATION_JSON_VALUE)
	public Result rename(@Valid @RequestBody RenameRequestBody renameRequestBody)
		throws AuthenticationException, UserNotFoundException, ServiceLayerException, ValidationException {
		contentService.renameContent(renameRequestBody.getSiteId(), renameRequestBody.getPath(), renameRequestBody.getName());
		var result = new Result();
		result.setResponse(OK);
		return result;
	}

	@GetMapping(value = ITEM_HISTORY)
	public ResultList<ItemVersion> getHistory(@ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
						  @ValidExistingContentPath @RequestParam(value = REQUEST_PARAM_PATH) String path) throws ServiceLayerException {
		ResultList<ItemVersion> result = new ResultList<>();
		result.setResponse(OK);
		result.setEntities(RESULT_KEY_ITEMS, contentService.getContentVersionHistory(siteId, path));

		return result;
	}
}
