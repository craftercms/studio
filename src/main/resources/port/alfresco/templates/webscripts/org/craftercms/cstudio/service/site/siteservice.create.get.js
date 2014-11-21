var handleSiteNameMacro = function(siteName, targetRoot) {

        var handleFolder = function(folder) {

                for(var i=0; i < folder.children.length; i++)  {
	                var child = folder.children[i];

	                if(child.isContainer == true) {
	                	handleFolder(child);
	                	
	                	if(child.name == "SITENAME") {
	                		child.name = siteName;
	                	}
	                }
	                else {
                        if(child.name.indexOf(".xml") != -1 
                        || child.name.indexOf(".css") != -1
                        || child.name.indexOf(".js") != -1
                        || child.name.indexOf(".txt") != -1) {                        
                        	child.content = child.content.replaceAll("SITENAME",  siteName);
                        }
	                }
                }
        };

        handleFolder(targetRoot);
};

var blueprint = function(siteName, sourceRoot, targetRoot, scriptName) {
        for(var i=0; i < sourceRoot.children.length; i++)  {
                var child = sourceRoot.children[i];
                child.copy(targetRoot, true);
        }

         handleSiteNameMacro(siteName, targetRoot);

        // execute controller
        var action = actions.create("script");
        action.parameters["script-ref"] = blueprintRoot.childByNamePath(scriptName);
        action.parameters["space"] = targetRoot;

        action.execute(targetRoot);
}



// define basic functions
var siteName = args.site;
var blueprintName = args.blueprint;
var sitesConfigRoot = companyhome.childByNamePath("cstudio/config/sites");
var blueprintsRoot = companyhome.childByNamePath("cstudio/site-blueprints");
var blueprintRoot = blueprintsRoot.childByNamePath(blueprintName);

// handle site config
		var blueprintConfigRoot = blueprintRoot.childByNamePath("site-config");
        var siteConfigRoot = sitesConfigRoot.createFolder(siteName);
        
        blueprint(siteName, blueprintConfigRoot, siteConfigRoot, "site-config.js");

 // handle site content
        var blueprintSiteRoot = blueprintRoot.childByNamePath("site-content");
        var sitesRoot = companyhome.childByNamePath("wem-projects");
        var siteRoot = sitesRoot.createFolder(siteName).createFolder(siteName);
 
// create folder structures
        var workRoot = siteRoot.createFolder("work-area");
        var liveRoot = siteRoot.createFolder("live");
        var tempRoot = siteRoot.createFolder("draft");

        blueprint(siteName, blueprintSiteRoot, workRoot, "site-content.js");

        authoringSiteService.addConfigSpaceExportAspect(siteName);
        authoringSiteService.initializeCache(siteName);
        authoringSiteService.createObjectStatesforNewSite(siteRoot);
        authoringSiteService.extractMetadataForNewSite(siteRoot);
        authoringSiteService.extractDependenciesForNewSite(siteRoot);
