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
					"/page/site/" + site + "/cstudio-form?form=" +
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
			
			var windowUrl = "";
			
			if (CStudioAuthoringContext.formServerUri != undefined && CStudioAuthoringContext.formServerUri != "")
					formServerUri = CStudioAuthoringContext.formServerUri.substr(CStudioAuthoringContext.formServerUri.lastIndexOf("/"))
			var pipelinesParameter = "";	
			var windowUrl = formServerUri + "/cstudio-incontext/form-field?url=/share" +
								"/page/site/" +
									CStudioAuthoringContext.site +
										"/cstudio-webform";	
								 
			var cookieTicket = "";	
			var userId = "";
			var ticketInit =false;
			var userInit = false;			 								 
			var cookiesAlf = document.cookie.split(";");
			
			for (var i = 0;i < cookiesAlf.length;i++) {
				var nameCookie=cookiesAlf[i].substr(0,cookiesAlf[i].indexOf("="));
				nameCookie = nameCookie.replace(/^\s+|\s+$/g,"");
				if (nameCookie == "alf_ticket") {
					cookieTicket=unescape(cookiesAlf[i].substr(cookiesAlf[i].indexOf("=")+1));
					ticketInit = true;
				} else if (nameCookie == "username") {
					userId=cookiesAlf[i].substr(cookiesAlf[i].indexOf("=")+1);
					userInit = true;
				}
				
				if (ticketInit && userInit) {
					break;
				}
			}

			windowUrl += "%3Fform=" + item.contentType;	
			windowUrl += "%26id=" + item.uri + "%26path=" + item.uri;
			pipelinesParameter += "&path="+ item.uri;
			windowUrl += "%26edit=true";
			pipelinesParameter += "&edit=true";
			pipelinesParameter += "&mode=ice";
			pipelinesParameter += "&field="+field;		
			
			pipelinesParameter += "&baseAuthorUri=" + CStudioAuthoringContext.authoringAppBaseUri;
			pipelinesParameter += "&formServerBase=" + formServerUri;
			pipelinesParameter +=  "&shareServerBase=/share";
			
			pipelinesParameter +=  "&readonly=false";
			
			pipelinesParameter += "&contentType=" + item.form;
			pipelinesParameter += "&contentPath=" + item.uri;
			pipelinesParameter += "&oldContentPath=" + item.uri;
			pipelinesParameter += "&collab=" +CStudioAuthoringContext.previewAppBaseUri
			pipelinesParameter += "&role=" +CStudioAuthoringContext.role;
			pipelinesParameter += "&previewAppBaseUri="+CStudioAuthoringContext.previewAppBaseUri;
			pipelinesParameter += "&site=" + CStudioAuthoringContext.site;
			pipelinesParameter += "&cookieDomain=" +CStudioAuthoringContext.cookieDomain;
			windowUrl  += "%26showFormDef=true&alf_ticket=" + cookieTicket + "&user="+userId + pipelinesParameter;
	
			return windowUrl;
        }

    });

    CStudioAuthoring.Env.ModuleMap.map("viewcontroller-in-context-edit", InContextEdit);

})();
