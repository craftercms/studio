/**
 * File: in-context-edit.js
 * Component ID: viewcontroller-in-context-edit
 * @author: Russ Danner
 * @date: 4.27.2011
 **/
(function(){

    var InContextEdit,
        Event = YAHOO.util.Event,
        Dom = YAHOO.util.Dom,
        JSON = YAHOO.lang.JSON,

        eachfn = CStudioAuthoring.Utils.each,

       TemplateAgent = CStudioAuthoring.Component.TemplateAgent,
        template = CStudioAuthoring.TemplateHolder.History;

    CStudioAuthoring.register("ViewController.InContextEdit", function() {
        CStudioAuthoring.ViewController.InContextEdit.superclass.constructor.apply(this, arguments);
    });

    InContextEdit = CStudioAuthoring.ViewController.InContextEdit;
    YAHOO.extend(InContextEdit, CStudioAuthoring.ViewController.Base, {
        events: ["updateContent"],
        actions: [".update-content", ".cancel"],

        initialise: function(usrCfg) {
            Dom.setStyle(this.cfg.getProperty("context"), "overflow", "visible");
        },
        
        /**
         * on initialization, go out and get the content and 
         * populate the dialog.
         * 
         * on error, display the issue and then close the dialog
         */
        initializeContent: function(item, field, site, isEdit, callback) {
			var iframeEl = document.getElementById("in-context-edit-editor");
			var dialogEl = document.getElementById("viewcontroller-in-context-edit_0_c");
			var dialogBodyEl = document.getElementById("viewcontroller-in-context-edit_0")
			
	
				contentTypeCb = {
					success: function(contentType) {
						var windowUrl = "";
						
						if(contentType.formPath == "simple") {
							// use the simple form server
							windowUrl = this.context.constructUrlWebFormSimpleEngine(contentType, item, field, site, isEdit);
						}
						else {
							// use the legacy form server
							windowUrl = this.context.constructUrlWebFormLegacyFormServer(item, field, site);
						}

						this.iframeEl.src = windowUrl;
						this.dialogEl.style.width = "auto";
						this.dialogBodyEl.children[1].style.background = '#F0F0F0';					
						window.iceCallback = callback;
					},
					failure: function() {
					},
					
					iframeEl: iframeEl,
					dialogEl: dialogEl,
					dialogBodyEl: dialogBodyEl,
					context: this
				}
				
				CStudioAuthoring.Service.lookupContentType(CStudioAuthoringContext.site, item.contentType, contentTypeCb);
        },
        
        /** 
         * get the content from the input and send it back to the server
         */
        updateContentActionClicked: function(buttonEl, evt) {
			//not used         
        },
        
        /**
         * cancel the dialog
         */
        cancelActionClicked: function(buttonEl, evt) {
			//not used
        },
        
        /**
         * construct URL for simple form server
         */
        constructUrlWebFormSimpleEngine: function(contentType, item, field, site, isEdit) {
        	var windowUrl = "";

			windowUrl = CStudioAuthoringContext.authoringAppBaseUri +
					"/form?site=" + site + "&form=" +
					contentType.form +
					"&path=" + item.uri;
					
				if(field) {
					windowUrl += "&iceId=" + field;
				}
				else {
					windowUrl += "&iceComponent=true";
				}

				windowUrl += "&edit="+isEdit;

        	return windowUrl;
        },
        
        /**
         * provide support for legacy form server 
         */
        constructUrlWebFormLegacyFormServer: function(item, field, site) {
			alert("legacy form server is no longer supported");			
        }
    });

    CStudioAuthoring.Env.ModuleMap.map("viewcontroller-in-context-edit", InContextEdit);

})();
