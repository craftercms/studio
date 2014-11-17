var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;

CStudioAuthoring.Dialogs = CStudioAuthoring.Dialogs || {};

/**
 * Submit to go live
 */
CStudioAuthoring.Dialogs.DialogSelectTaxonomyType = CStudioAuthoring.Dialogs.DialogSelectTaxonomyType || {

	/**
	 * initialize module
	 */
	initialize: function(config) {
	},

	changeTemplateCalled : false,
	/**
	 * show dialog
	 */
	showDialog: function(contentTypes, path, asPopup, onSaveCallback, isChangeTemplate) {
		
		/**
		 * indicating, from where showDialog is called (new/change template).
		 */
		CStudioAuthoring.Dialogs.DialogSelectTaxonomyType.changeTemplateCalled = isChangeTemplate;  

		this._self = this;

		this.dialog = this.createDialog(path, onSaveCallback);

		this.path = path;
		this.onSaveCallback = onSaveCallback;
		this.asPopup = asPopup;			

		this.updateAvailableTemplates(this.dialog, contentTypes.types);
		this.setDefaultTemplate(contentTypes.types)
		this.dialog.show();
	},
	
	/**
	 * hide dialog
	 */
	hideDialog: function() {
		this.dialog.hide();
	},
	
	/**
	 * create dialog
	 */
	createDialog: function(path, selectTemplateCb) {
		YDom.removeClass("cstudio-wcm-popup-div", "yui-pe-content");
		
		var newdiv = document.createElement("div");
		var divIdName = "cstudio-wcm-popup-div";
		newdiv.setAttribute("id",divIdName);
		newdiv.className= "yui-pe-content";
		newdiv.innerHTML = '<div class="contentTypePopupInner" id="ct_contentTypePopupInner" style="width:600px;height:440px;">'+
								'<div class="contentTypePopupContent" id="ct_contentTypePopupContent"> '+
									'<form name="contentFromWCM" action=""> '+
									'<div class="contentTypePopupHeader">Choose Taxonomy Type</div> '+
									'<div>The following taxonomy types are available for use at this level.</div> '+
									'<div class="contentTypeOuter"> '+
										'<div class="templateName" id="templateName"> '+
										'<div class="contentTypeDropdown">'+
											'<div>Taxonomy Type Name: </div><select id="wcm-content-types-dropdown" size="16" class="cstudio-wcm-popup-select-control" style="width:273px; height:275px;"></select> '+
										'</div></div>'+
									'</div> '+
									'<div style="width:100%; text-align:center; margin:0; position:absolute; left:0; bottom:15px;">'+
									'<div class="contentTypePopupBtn"> '+
										'<input type="submit" class="cstudio-xform-button ok" id="submitWCMPopup" value="OK" />' +
										'<input type="submit" class="cstudio-xform-button cancel" id="closeWCMPopup" value="Cancel">' +
									'</div></div>'+
									'</form> '+
								'</div> '+
							'</div>';

		document.body.appendChild(newdiv);
		 // Instantiate the Dialog
		content_type_dialog = new YAHOO.widget.Dialog("cstudio-wcm-popup-div",
								{ width : "610px",
								  height : "450px",
								  fixedcenter : true,
								  visible : false,
								  modal:true,
								  close:false,
								  constraintoviewport : true,
								  underlay:"none"
								});
                var resizeHandler = new YAHOO.util.Resize("ct_contentTypePopupInner", { proxy:true, minHeight:440, minWidth:600, handles:['br'] });
                resizeHandler.on ("endResize", function() {
                    var dialogObj = this._wrap;
                    if (dialogObj) { try {
                        var dialogWraper = dialogObj.parentNode;
                        if (dialogWraper) {
                            var newWidth = parseInt(dialogObj.style.width, 10);
                            var newHeight = parseInt(dialogObj.style.height, 10);
                            if (newWidth < 600) {
                                newWidth = 600;
                                dialogObj.style.width = "600px";
			    }
                            if (newHeight < 440) {
                                newHeight = 440;
                                dialogObj.style.height = "440px";
			    }
                            dialogWraper.style.width = newWidth + 10 + "px";
                            dialogWraper.style.height = newHeight + 10 + "px";

			    //reset content type drop down and preview image size
                            var widthDiff = parseInt(((newWidth - 600) / 2), 10);
                            var heightDiff = (newHeight - 440);
                            var oDivTmpName = YDom.get("templateName");
                            var oDivPreImg = YDom.get("previewImage");
			    var oContentType = YDom.get("wcm-content-types-dropdown");
                            var oPreviewImage = YDom.get("contentTypePreviewImg");
			    if (oDivTmpName && oDivPreImg && oContentType && oPreviewImage) {
				    oDivTmpName.style.width = 275 + widthDiff + "px";
                                    oDivPreImg.style.width = 275 + widthDiff + "px";
				    oContentType.style.width = 273 + widthDiff + "px";
				    oContentType.style.height = 275 + heightDiff + "px";
				    oPreviewImage.style.width = 267 + widthDiff + "px";
				    oPreviewImage.style.height = 275 + heightDiff + "px";
			    }
                            content_type_dialog.center();
                        }
                    } catch (e) { } }
		});

		// Render the Dialog
		content_type_dialog.render();
        content_type_dialog.cfg.subscribe("configChanged", function (p_sType, p_aArgs) {
            var aProperty = p_aArgs[0],
                    sPropertyName = aProperty[0],
                    oPropertyValue = aProperty[1];
            if (sPropertyName == 'zindex') {
                var siteContextNavZIndex=100;
                YDom.get("cstudio-wcm-popup-div").parentNode.style.zIndex = oPropertyValue+siteContextNavZIndex;
            }

            //set focus on OK Button.
            if (YDom.get("submitWCMPopup")) {
                YDom.get("submitWCMPopup").focus();
            }
        });

		var eventParams = {
			self: this
		};
				
		YAHOO.util.Event.addListener("submitWCMPopup", "click", this.contentPopupSubmit, eventParams);
		
		YAHOO.util.Event.addListener("closeWCMPopup", "click",
                function() {
                    CStudioAuthoring.Dialogs.DialogSelectTaxonomyType.closeDialog();

                }
         );


		return content_type_dialog;
	},

	/**
	 * set default template
	 */
	setDefaultTemplate: function(contentTypes) {
		var contentTypesSelect = YDom.get("wcm-content-types-dropdown");
		if (!contentTypesSelect) return;
		var defaultSrc = CStudioAuthoringContext.baseUri+'/themes/cstudioTheme/images/';
		var defaultImg = "default-contentType.jpg";
		var contentTypePreviewImg = YDom.get("contentTypePreviewImg");

		for(k=0; k<contentTypes.length; k++) {
			if(contentTypesSelect.value == contentTypes[k].name){
				if(contentTypes[k].image){
					contentTypePreviewImg.src = defaultSrc + contentTypes[k].image;
				}
				else{
					contentTypePreviewImg.src = defaultSrc + defaultImg;
				}
			}
		}
	},
	
	/**
	 * update the content types
	 */
	updateAvailableTemplates: function(dialog, contentTypes) {

		var contentTypesSelect = document.getElementById("wcm-content-types-dropdown");    

		contentTypesSelect.contentTypes = contentTypes;
	    contentTypesSelect.innerHTML = "";

		for(j=0; j<contentTypes.length; j++) {
			var label = contentTypes[j].label;
			var option = document.createElement("option");
			option.text = contentTypes[j].label;
			option.value = contentTypes[j].type;
			if(j == 0 ) option.selected = "selected";//first template will be selected. 
			contentTypesSelect.options.add(option);
		}

		YAHOO.util.Event.addListener("wcm-content-types-dropdown", "change", function(){


			var defaultSrc = CStudioAuthoringContext.baseUri+'/static-assets/themes/cstudioTheme/images/';
			var defaultImg = "default-contentType.jpg";
			var contentTypePreviewImg = YDom.get("contentTypePreviewImg");

			for(k=0; k<contentTypes.length; k++) {
				if(this.value == contentTypes[k].name){
					if(contentTypes[k].image){
						contentTypePreviewImg.src = defaultSrc + contentTypes[k].image;
					}
					else{
						contentTypePreviewImg.src = defaultSrc + defaultImg;
					}
				}
			}
		});	
	},
	
	/** 
	 * close dialog
	 */
	closeDialog:function() {
        this.dialog.hide();
        var element = YDom.get("cstudio-wcm-popup-div");
        element.parentNode.removeChild(element);
    },
    
	/**
	 * event fired when the ok is pressed
	 */
	contentPopupSubmit: function(event, args) {

  		var contentTypesSelectEl = document.getElementById("wcm-content-types-dropdown");
		var selectedIndex = contentTypesSelectEl.selectedIndex;
		var selectedType = contentTypesSelectEl.contentTypes[selectedIndex]; 
		
		CStudioAuthoring.Dialogs.DialogSelectTaxonomyType.closeDialog();
		args.self.onSaveCallback.success(selectedType);
	},

	/**
	 * event fired when the cancel is pressed
	 */
	contentPopupCancel: function(event) {
		CStudioAuthoring.Dialogs.DialogSelectTaxonomyType.closeDialog();
	}
};

CStudioAuthoring.Module.moduleLoaded("dialog-select-taxonomy", CStudioAuthoring.Dialogs.DialogSelectTaxonomyType);
