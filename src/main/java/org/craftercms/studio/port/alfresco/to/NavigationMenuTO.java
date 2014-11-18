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
package org.craftercms.cstudio.alfresco.to;

public class NavigationMenuTO {
	
	protected String href;
	protected String label;
	protected String moduleHook;
	protected String module;
	protected String moduleName;
	
	
	public NavigationMenuTO(String modulehook,String label,String href)
	{
		this.label = label;
		this.href = href;
		this.moduleHook = "";
		this.moduleName ="";
		this.module = "";
		
	}
	
	public NavigationMenuTO(String modulehook)
	{
		this.module = "";
		this.label = "";
		this.href = "";
		this.moduleHook = modulehook;
		this.moduleName ="";
	}
	
	public NavigationMenuTO(String module,String moduleName)
	{
		this.module = module;
		this.moduleName = moduleName;
		this.label = "";
		this.href = "";
		this.moduleHook = "";
		
	}
	
	public String getModuleHook() {
		return moduleHook;
	}

	public void setModuleHook(String moduleHook) {
		this.moduleHook = moduleHook;
	}
	
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

}
