var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;

CStudioAuthoring.Dialogs = CStudioAuthoring.Dialogs || {};

/**
 * Submit to go live
 */
CStudioAuthoring.Dialogs.DialogCreateTaxonomy = CStudioAuthoring.Dialogs.DialogCreateTaxonomy || {

	/**
	 * initialize module
	 */
	initialize: function(config) {
	},

	changeTemplateCalled : false,
	/**
	 * show dialog
	 */
	showDialog: function(taxonomyType, TaxonomyName, path, onSaveCallback) {
		
		this._self = this;

		this.dialog = this.createDialog(taxonomyType, TaxonomyName, path, onSaveCallback);

		this.path = path;
		this.onSaveCallback = onSaveCallback;

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
	createDialog: function(taxonomyType, taxonomyName, path, selectTemplateCb) {
		YDom.removeClass("cstudio-wcm-popup-div", "yui-pe-content");
		
		var newdiv = document.createElement("div");
		var divIdName = "cstudio-wcm-popup-div";
		newdiv.setAttribute("id",divIdName);
		newdiv.className= "yui-pe-content";
		newdiv.innerHTML = '<div class="contentTypePopupInner" id="ct_contentTypePopupInner" style="width:600px;height:240px;">'+
								'<div class="contentTypePopupContent" id="ct_contentTypePopupContent"> '+
									'<form name="contentFromWCM" action=""> '+
									'<div class="contentTypePopupHeader">Create Taxonomy Item of type '+
									taxonomyName + '</div> '+
									'<div class="contentTypeOuter">' +

'<input id="taxonomyTypeVal" type="hidden" value="'+taxonomyType+'" />'+
'<input id="taxonomyPathVal" type="hidden" value="'+path+'" />'+

'<table>' +
'<tr><td>Title:</td><td><input type="text" id="taxonomyLabelVal" /></td></tr>' +
'<tr><td>&nbsp;</td><td>&nbsp;</td></tr>' +
'<tr><td>disabled:</td><td><input type="checkbox" id="taxonomyDisabledVal" /></td></tr>'+
'</table>'+
									'</div> '+
									'<div style="width:100%; text-align:center; margin:0; position:absolute; left:0; bottom:15px;">'+
									'<div class="contentTypePopupBtn"> '+
										'<input type="submit" class="cstudio-xform-button ok" id="submitWCMPopup" value="OK" />'+
										'<input type="submit" class="cstudio-xform-button cancel" id="closeWCMPopup" value="Cancel /">'+
									'</div></div>'+
									'</form> '+
								'</div> '+
							'</div>';

		document.body.appendChild(newdiv);
		 // Instantiate the Dialog
		content_type_dialog = new YAHOO.widget.Dialog("cstudio-wcm-popup-div",
								{ width : "610px",
								  height : "250px",
								  fixedcenter : true,
								  visible : false,
								  modal:true,
								  close:false,
								  constraintoviewport : true,
								  underlay:"none"
								});
                var resizeHandler = new YAHOO.util.Resize("ct_contentTypePopupInner", { proxy:true, minHeight:240, minWidth:600, handles:['br'] });
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
                            if (newHeight < 240) {
                                newHeight = 240;
                                dialogObj.style.height = "240px";
			    }
                            dialogWraper.style.width = newWidth + 10 + "px";
                            dialogWraper.style.height = newHeight + 10 + "px";

			    //reset content type drop down and preview image size
                            var widthDiff = parseInt(((newWidth - 600) / 2), 10);
                            var heightDiff = (newHeight - 240);

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
                    CStudioAuthoring.Dialogs.DialogCreateTaxonomy.closeDialog();
                }
         );

		return content_type_dialog;
	},
    
	/**
	 * event fired when the ok is pressed
	 */
	contentPopupSubmit: function(event, args) {
		var taxonomyType = document.getElementById("taxonomyTypeVal").value;
		var taxonomyPath = document.getElementById("taxonomyPathVal").value;
		var taxonomyLabel = document.getElementById("taxonomyLabelVal").value;
		//var taxonomyDisabled = document.getElementById("taxonomyDisabled").checked;						

		var createCb = {
			success: function(response) {
				CStudioAuthoring.Dialogs.DialogCreateTaxonomy.closeDialog();
				args.self.onSaveCallback.success();
			},
			failure: function(response) {
				alert(response);
				CStudioAuthoring.Dialogs.DialogCreateTaxonomy.closeDialog();
			}
		};
		
		CStudioAuthoring.Service.createTaxonomyItem(taxonomyPath, taxonomyType, taxonomyLabel, createCb);
	},

	closeDialog:function() {
        this.dialog.hide();
        var element = YDom.get("cstudio-wcm-popup-div");
        element.parentNode.removeChild(element);
    },
    
    /**
	 * event fired when the cancel is pressed
	 */
	contentPopupCancel: function(event) {
		CStudioAuthoring.Dialogs.DialogCreateTaxonomy.hideDialog();
	}
};

CStudioAuthoring.Module.moduleLoaded("dialog-create-taxonomy", CStudioAuthoring.Dialogs.DialogCreateTaxonomy);
