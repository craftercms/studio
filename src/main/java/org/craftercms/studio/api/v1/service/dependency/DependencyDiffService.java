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

package org.craftercms.studio.api.v1.service.dependency;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.dom4j.Document;

import java.util.ArrayList;
import java.util.List;

public interface DependencyDiffService {

    /**
     * Computes addedDependenices and removedDependenices based on the DiffRequest information provided
     * @param diffRequest diff request
     * @return Diff response object
     * @throws ServiceLayerException general service error
     */
    DiffResponse diff(DiffRequest diffRequest) throws ServiceLayerException;

    /**
     *
     * DiffResponse is returned by Diff executor with added and removed dependenices
     *
     */
    class DiffResponse{

        protected List<String> addedDependencies = new ArrayList<String>();

        protected List<String> removedDependencies = new ArrayList<String>();


        public List<String> getAddedDependencies() {
            return addedDependencies;
        }
        public void setAddedDependencies(List<String> addedDependencies) {
            this.addedDependencies = addedDependencies;
        }

        public List<String> getRemovedDependencies() {
            return removedDependencies;
        }
        public void setRemovedDependencies(List<String> removedDependencies) {
            this.removedDependencies = removedDependencies;
        }
    }


    /**
     * DiffRequest used for providing info to the Diff Executor
     *
     */
    class DiffRequest {

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
