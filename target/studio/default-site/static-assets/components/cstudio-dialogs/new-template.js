var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;

CStudioAuthoring.Dialogs = CStudioAuthoring.Dialogs || {};

/**
 * NewContentType
 */
CStudioAuthoring.Dialogs.NewTemplate = CStudioAuthoring.Dialogs.NewTemplate || {

	/**
	 * initialize module
	 */
	initialize: function(config) {
		this.config = config;
	},

	/**
	 * show dialog
	 */
	showDialog: function(cb, config) {	
		this.config = config;
		this._self = this;
		this.cb = cb;
		this.dialog = this.createDialog();
		this.dialog.show();
		document.getElementById("cstudio-wcm-popup-div_h").style.display = "none";
		
	},
	
	/**
	 * hide dialog
	 */
    closeDialog:function() {
        this.dialog.destroy();
    },

    /**
	 * create dialog
	 */
	createDialog: function() {
		YDom.removeClass("cstudio-wcm-popup-div", "yui-pe-content");

		var newdiv = YDom.get("cstudio-wcm-popup-div");
		if (newdiv == undefined) {
			newdiv = document.createElement("div");
			document.body.appendChild(newdiv);
		}

		var divIdName = "cstudio-wcm-popup-div";
		newdiv.setAttribute("id",divIdName);
		newdiv.className= "yui-pe-content";
        newdiv.innerHTML = '<div class="contentTypePopupInner" id="upload-popup-inner">' +
                           '<div class="contentTypePopupContent" id="contentTypePopupContent"> ' +
                           '<div class="contentTypePopupHeader">Create Template</div> ' +
                           '<div>'+
                             '<div>Provide a filename for the template</div>'+
							 '<input id="templateName" size="50"><br/>' +
                           '<div>' +

						   '<div class="contentTypePopupBtn"> ' +
						   '<input type="button" class="cstudio-button ok" id="createButton" value="Create" />' +
                           '<input type="button" class="cstudio-button" id="createCancelButton" value="Cancel"/>' +
                           '</div>' +

                           '</div> ' +
                           '</div>';
		
		document.getElementById("upload-popup-inner").style.width = "350px";
		document.getElementById("upload-popup-inner").style.height = "150px";

		 var dialog = new YAHOO.widget.Dialog("cstudio-wcm-popup-div", 
								{ width : "360px",
								  height : "160px",	
								  fixedcenter : true,
								  visible : false,
								  modal:true,
								  close:false,
								  constraintoviewport : true,
								  underlay:"none"							  							
								});	
								
		// Render the Dialog
		dialog.render();
		
		var eventParams = {
			self: this,
			nameEl: document.getElementById('templateName')
		};
		

		YAHOO.util.Event.addListener("createButton", "click", this.createClick, eventParams);

		YAHOO.util.Event.addListener("createCancelButton", "click", this.popupCancelClick);

		
		return dialog;
	},

	/** 
	 * create clicked 
	 */
	createClick: function(event, params) {
		var _self = CStudioAuthoring.Dialogs.NewTemplate;
		var name = params.nameEl.value;
		var templatePath = "/templates/web/"+name;
		
		if(templatePath.indexOf(".ftl") == -1) {
			templatePath += ".ftl";
		}
		
		var writeServiceUrl = "/proxy/alfresco/cstudio/wcm/content/write-content-asset"
		+ "?site=" + CStudioAuthoringContext.site 
		+ "&path=" + templatePath;

		var saveSvcCb = {
			success: function() {
				CStudioAuthoring.Dialogs.NewTemplate.closeDialog();
				
				CStudioAuthoring.Operations.openTemplateEditor
					(templatePath, "default", { 
						success: function() { 
							_self.cb.success(templatePath); 
						}, 
						failure: function() {
						}
					});
			},
			failure: function() {
			}
		};	
			
		YAHOO.util.Connect.setDefaultPostHeader(false);
		YAHOO.util.Connect.initHeader("Content-Type", "text/pain; charset=utf-8");
		YAHOO.util.Connect.asyncRequest('POST', CStudioAuthoring.Service.createServiceUri(writeServiceUrl), saveSvcCb, "");
 
	},		
	
	/**
	 * event fired when the ok is pressed
	 */
	popupCancelClick: function(event) {
		CStudioAuthoring.Dialogs.NewTemplate.closeDialog();
	}


};

CStudioAuthoring.Module.moduleLoaded("new-template-dialog", CStudioAuthoring.Dialogs.NewTemplate);