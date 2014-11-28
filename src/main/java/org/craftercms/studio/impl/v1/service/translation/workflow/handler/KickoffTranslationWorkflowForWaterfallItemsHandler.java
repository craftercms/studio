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
package org.craftercms.studio.impl.v1.service.translation.workflow.handler;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javolution.util.FastList;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.service.translation.TranslationService;
import org.craftercms.studio.api.v1.service.workflow.WorkflowItem;
import org.craftercms.studio.api.v1.service.workflow.WorkflowJob;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.impl.v1.service.workflow.JobStateHandler;
import org.dom4j.Document;
import org.dom4j.Node;


/**
 * given a workflow job, create a new job to submit and monitor the translation
 * @author rdanner
 */
public class KickoffTranslationWorkflowForWaterfallItemsHandler implements JobStateHandler {

	protected static final String MSG_ERROR_CREATE_NEW_TRANSLATE_JOB = "err_create_new_translate_job";
	
	private static final Logger logger = LoggerFactory.getLogger(KickoffTranslationWorkflowForWaterfallItemsHandler.class);
	
	@Override
	public String handleState(WorkflowJob job, WorkflowService workflowService) {
		// load the configuration for child sites from site config
		String retState = job.getCurrentStatus();
		String site = job.getSite();
		
		try {
			// construct a list of file
			List<String> paths = new FastList<String>();
			
			for(WorkflowItem item : job.getItems()) {
				String path = item.getPath();
				paths.add(path);
				getDependents(site, path, paths);
			}

			// load the translation for the site
			Document siteConfigEl = _siteService.getSiteConfiguration(site);
			String sourceLanguage = siteConfigEl.valueOf("/site-config/translation/sourceLanguage");
			
			List<Node> targetEls = siteConfigEl.selectNodes("/site-config/translation/targetSites//targetSite");

			if (paths.size() > 0) {
				// for each configuration
				for(Node targetEl : targetEls) {
					String targetSiteId = targetEl.valueOf("id");
					String basePath = targetEl.valueOf("basePath");
					String targetLanguage = targetEl.valueOf("targetLanguage");
					
					// keep existing properties and add new ones
					Map<String, String> properties = job.getProperties();
					properties.put("sourceSite", site);
					properties.put("sourceLanguage", sourceLanguage);
					properties.put("targetSite", targetSiteId);
					properties.put("basePath", basePath);
					properties.put("targetLanguage", targetLanguage);
					
					// calculate the intersection
					List<String> targetPaths = _translationService.calculateTargetTranslationSet(site, paths, targetSiteId);
					
					// submit job
					for(String path : targetPaths) {
						List<String> submitAsSingleItemList = new ArrayList<String>();
						submitAsSingleItemList.add(path);
						
						workflowService.createJob(targetSiteId, submitAsSingleItemList,  "translate", properties);
					}
				}
			}
			retState = WorkflowService.STATE_ENDED;
		}
		catch(Exception err) {
			logger.error(MSG_ERROR_CREATE_NEW_TRANSLATE_JOB, err, job);
		}
		
		return retState;
	}

	/** getter site config property */
	public SiteService getSiteService() { return _siteService; }
	/** setter for site config property */
	public void setSiteService(SiteService service) { _siteService = service; }

	/** getter translation service property */
	public TranslationService getTranslationService() { return _translationService; }
	/** setter for translation service property */
	public void setTranslationService(TranslationService service) { _translationService = service; }

	public void setDependencyService(DmDependencyService service) { _dependencyService = service; }

	private void getDependents(String site, String path, List<String> dependents) {
		List<String> depPaths = _dependencyService.getDependencyPaths(site, path);
		for (String depPath : depPaths) {
			// Add only page and components
			if (!dependents.contains(depPath) && depPath.startsWith("/site/")) {
				dependents.add(depPath);
				getDependents(site, depPath, dependents);
			}
		}
	}

	private SiteService _siteService;
	private TranslationService _translationService;
	private DmDependencyService _dependencyService;
}
