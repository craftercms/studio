// define basic functions
var siteName = args.site;
var blueprintName = "default";

// handle site config
		var sitesConfigRoot = companyhome.childByNamePath("cstudio/config/sites");
		sitesConfigRoot.childByNamePath(siteName).remove();

 // handle site content
        var sitesRoot = companyhome.childByNamePath("wem-projects");
        sitesRoot.childByNamePath(siteName).remove();

 // handle site forms
        var sitesFormsRoot = companyhome.childByNamePath("cstudio/config/forms");
        if (sitesFormsRoot != null) {
            var formsRoot = sitesFormsRoot.childByNamePath(siteName);
            if (formsRoot != null) {
                formsRoot.remove();
            }
        }

// handle site models
        var sitesModelsRoot = companyhome.childByNamePath("cstudio/model-prototypes");
        if (sitesModelsRoot != null) {
            var modelsRoot = sitesModelsRoot.childByNamePath(siteName);
            if (modelsRoot != null) {
                modelsRoot.remove();
            }
        }

authoringSiteService.deleteSite(siteName);
