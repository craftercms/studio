/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
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
package org.craftercms.studio.impl.v1.service.dependency;

import java.util.ArrayList;
import java.util.List;


import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.to.DmDependencyTO;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DmDependencyDiffService extends AbstractRegistrableService {

    private static final Logger logger = LoggerFactory.getLogger(DmDependencyDiffService.class);

    @Override
    public void register() {
        getServicesManager().registerService(DmDependencyDiffService.class, this);
    }

    /**
	 * Computes addedDependenices and removedDependenices based on the DiffRequest information provided
	 * @param diffRequest
	 * @return
	 * @throws ServiceException
	 */
	public DiffResponse diff(DiffRequest diffRequest) throws ServiceException {
		
		if(diffRequest == null)
			throw new ServiceException("diffcontext cannot be null");
		
		DiffResponse response = new DiffResponse();
		boolean recursive = diffRequest.isRecursive();
		String site = diffRequest.getSite();
		
		String sourcePath = diffRequest.getSourcePath();
		String destPath = diffRequest.getDestPath();
		if(StringUtils.isEmpty(destPath)){
			destPath = sourcePath;
		}
		
		List<String> sourceDependencies = new ArrayList<String>();
        sourceDependencies = findDependencies(site,diffRequest.getSourceSandbox(),sourcePath, recursive, sourceDependencies);
		List<String> destDependencies =  new ArrayList<String>();
        destDependencies = findDependencies(site,diffRequest.getDestSandbox(),destPath, recursive, destDependencies);

		//Removed dependenices
		for(String destDependency:destDependencies){
			if(!sourceDependencies.contains(destDependency)){
				response.removedDependenices.add(destDependency);
			}
		}
		//Added dependenices
		for(String sourceDependency:sourceDependencies){
			if(!destDependencies.contains(sourceDependency)){
				response.addedDependencies.add(sourceDependency);
			}
		}
		return response;
	}
	
	/**
	 * 
	 * Gets all the dependencies of the provided path. Optionally does recurisive dependency fetch. 
	 * 
	 * @throws ServiceException
	 */
	protected List<String> findDependencies(String site, String sandbox, String relativePath, boolean isRecursive, List<String> dependencies) throws ServiceException{
        DmDependencyService dmDependencyService = getService(DmDependencyService.class);
        List<String> dependenciesFromDoc = dmDependencyService.getDependencyPaths(site, relativePath);
        dependencies.addAll(dependenciesFromDoc);
        if(isRecursive){
            for(String dependency:dependenciesFromDoc){
                if (!dependencies.contains(dependency)) {
                    dependencies.addAll(findDependencies(site, sandbox, dependency, isRecursive, dependencies));
                }
            }
        }
		return dependencies;
	}
	
	/**
	 * util to convert dependencyTO list to list of Uris
	 * 
	 */
	protected List<String> convertDependencyTO(List<DmDependencyTO> dependency){
		List<String> dependencyUris = new ArrayList<String>();
		for(DmDependencyTO to : dependency){
			dependencyUris.add(to.getUri());
		}
		return dependencyUris;
	}
	
	
	/**
	 * 
	 * DiffResponse is returned by Diff executor with added and removed dependenices 
	 *
	 */
	public static class DiffResponse{
		
		protected List<String> addedDependencies = new ArrayList<String>();
		
		protected List<String> removedDependenices = new ArrayList<String>();
		
		
		public List<String> getAddedDependencies() {
			return addedDependencies;
		}
		public void setAddedDependencies(List<String> addedDependencies) {
			this.addedDependencies = addedDependencies;
		}
		
		public List<String> getRemovedDependenices() {
			return removedDependenices;
		}
		public void setRemovedDependenices(List<String> removedDependenices) {
			this.removedDependenices = removedDependenices;
		}
	}
	
	
	/**
	 * DiffRequest used for providing info to the Diff Executor
	 * 
	 */
	 public static  class DiffRequest {
	    	
			protected String site;
			
			protected String sourcePath;
			
			protected String destPath;
		
		 	protected String sourceSandbox;
			
			protected String destSandbox;
			
			//optional if provide will be used or by default the doc will be picked from sourceSandbox
			protected Document sourceDoc;  
			
			//optional if provide will be used or by default the doc will be picked from destSandbox
			protected Document destDoc;
			
			protected boolean recursive=true;

			
			public DiffRequest(String site, String sourcePath,String destPath, String sourceSandbox,String destSandbox, boolean recursive) {
				super();
				this.site = site;
				this.sourcePath = sourcePath;
				this.destPath = destPath;
				this.sourceSandbox = sourceSandbox;
				this.destSandbox = destSandbox;
				this.recursive = recursive;
			}

			public String getSite() {
				return site;
			}

			public void setSite(String site) {
				this.site = site;
			}

			public String getSourceSandbox() {
				return sourceSandbox;
			}

			public void setSourceSandbox(String sourceSandbox) {
				this.sourceSandbox = sourceSandbox;
			}

			public String getDestSandbox() {
				return destSandbox;
			}

			public void setDestSandbox(String destSandbox) {
				this.destSandbox = destSandbox;
			}

			public Document getSourceDoc() {
				return sourceDoc;
			}

			public void setSourceDoc(Document sourceDoc) {
				this.sourceDoc = sourceDoc;
			}

			public Document getDestDoc() {
				return destDoc;
			}

			public void setDestDoc(Document destDoc) {
				this.destDoc = destDoc;
			}

			public boolean isRecursive() {
				return recursive;
			}

			public void setRecursive(boolean recursive) {
				this.recursive = recursive;
			}

			public String getSourcePath() {
				return sourcePath;
			}

			public void setSourcePath(String sourcePath) {
				this.sourcePath = sourcePath;
			}
			public String getDestPath() {
				return destPath;
			}

			public void setDestPath(String destPath) {
				this.destPath = destPath;
			}

	    }
}
