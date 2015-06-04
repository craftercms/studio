var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;

CStudioAuthoring.Dialogs = CStudioAuthoring.Dialogs || {};

/**
 * NewContentType
 */
CStudioAuthoring.Dialogs.NewContentType = CStudioAuthoring.Dialogs.NewContentType || {

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
                           '<div class="contentTypePopupHeader">Create Content Type</div> ' +
                           '<div class="content">'+
                             '<div class="contentTypeOuter">'+
                                '<label for="contentTypeDisplayName"><span>Display Label:</span>'+
                                '<input title="Provide a display label for this content type" id="contentTypeDisplayName" type="text"></label>' +
                                '<label for="contentTypeName"><span>Content Type Name:</span>'+
                                '<input style="disabled" title="Provide a system name for this content type" id="contentTypeName" type="text"></label>' +
                                '<div class="selectInput">' +
                                '<label for="contentTypeObjectType">Type:</label>'+
                                '<select title="Select the type for this content type" id="contentTypeObjectType">' +
                                '</select></div>' +
                                '<label style="display:none;" class="checkboxInput" for="contentTypeAsFolder"><span>Model as index (content as folder)</span>'+
                                '<input style="display:none;" id="contentTypeAsFolder" type="checkbox" checked="true"></label>' +
                             '</div>' +
                             '<div class="contentTypePopupBtn"> ' +
                               '<input type="button" class="btn btn-primary cstudio-button ok" id="createButton" value="Create" disabled="disabled" />' +
                               '<input type="button" class="btn btn-default cstudio-button" id="createCancelButton" value="Cancel"/>' +
                             '</div>' +
                           '</div>';

		document.getElementById("upload-popup-inner").style.width = "350px";
		document.getElementById("upload-popup-inner").style.height = "270px";

        var objectTypes;

        if(this.config.objectTypes.type != undefined){
            objectTypes=this.config.objectTypes.type;
        }else{
            objectTypes=this.config.objectTypes[0];
        }
		
		if(!objectTypes.length) {
            objectTypes = [ objectTypes ];
		}
		
		var typeEl = document.getElementById("contentTypeObjectType");
		for(var k=0; k<objectTypes.length; k++) {
			var objectType = objectTypes[k];
			typeEl.options[typeEl.options.length] = new Option(objectType.label, objectType.name);
		}

		// Instantiate the Dialog
		var dialog = new YAHOO.widget.Dialog("cstudio-wcm-popup-div", 
								{ width : "360px",
                                  height: "306px",
                                  effect:{
                                      effect: YAHOO.widget.ContainerEffect.FADE,
                                      duration: 0.25
                                  },
								  fixedcenter : true,
								  visible : false,
								  modal:true,
								  close:false,
								  constraintoviewport : true,
								  underlay:"none"
								});

		// Render the Dialog
		dialog.render();
		
		this.buttonValidator("createButton", { "contentTypeDisplayName" : [/^$/] });
		
		var eventParams = {
			self: this,
			typeNameEl: document.getElementById('contentTypeName'),
			labelEl: document.getElementById('contentTypeDisplayName'),
			asFolderEl: document.getElementById('contentTypeAsFolder'),
			objectTypeEl: document.getElementById('contentTypeObjectType')
		};
		
		YEvent.on("contentTypeObjectType", "change", function() {
			 var type = document.getElementById('contentTypeObjectType').value;
			 if(type=="page") {
				 document.getElementById('contentTypeAsFolder').checked = true;
			 }
			 else {
			 	document.getElementById('contentTypeAsFolder').checked = false;
			 }
		});

		YEvent.on("contentTypeDisplayName", "keyup", function() {
                    YAHOO.Bubbling.fire("content-type.values.changed");
					value = document.getElementById('contentTypeDisplayName').value;

					var find = ' ';
					var re = new RegExp(find, 'g');
					value = value.replace(re, '-');
					value = value.toLowerCase();

                    document.getElementById('contentTypeName').value = value;

                });
                //YEvent.on("contentTypeName", "keyup", function() {
                //    YAHOO.Bubbling.fire("content-type.values.changed");
                //});

		YEvent.addListener("createButton", "click", this.createClick, eventParams);

		YEvent.addListener("createCancelButton", "click", this.popupCancelClick);

		return dialog;
	},

	/** 
	 * create clicked 
	 */
	createClick: function(event, params) {
		var label = CStudioAuthoring.Dialogs.NewContentType.xmlEscape(params.labelEl.value);
		var name = CStudioAuthoring.Dialogs.NewContentType.xmlEscape(params.typeNameEl.value);
		var type = CStudioAuthoring.Dialogs.NewContentType.xmlEscape(params.objectTypeEl.value);
        
		var contentAsFolder = (
			type == 'component' ? false : params.asFolderEl.checked
		);
		var baseServicePath = '/api/1/services/api/1/site/write-configuration.json?path=/cstudio/config/sites/' + 
			CStudioAuthoringContext.site +
			'/content-types/' + type + '/' + name + 
			'/';

		var typeConfig = 
			'<content-type name="/' + type + '/' + name +'" is-wcm-type="true">\r\n' +
			 '<label>'+ label +'</label>\r\n'+
			 '<form>/' + type + '/' + name +'</form>\r\n'+
			 '<form-path>simple</form-path>\r\n' +
			 '<model-instance-path>NOT-USED-BY-SIMPLE-FORM-ENGINE</model-instance-path>\r\n' +
			 '<file-extension>xml</file-extension>\r\n' +
			 '<content-as-folder>'+contentAsFolder+'</content-as-folder>\r\n' +
			 '<previewable>'+ (type == 'page') +'</previewable>\r\n' +
			 '<noThumbnail>true</noThumbnail>\r\n' +
			 '<image-thumbnail></image-thumbnail>\r\n' +
			'</content-type>';

		var contentTypeCb = { 
			success: function() {
				var controllerContent = 
					'<import resource="classpath:alfresco/templates/webscripts/org/craftercms/cstudio/common/lib/common-lifecycle-api.js">\r\n' +
					'controller = (controller) ? controller : {};\r\n'+
					'controller.execute();';	

				var writeControllerCb = {
					success: function() {
						var extractionContent = 
							'<import resource="classpath:alfresco/templates/webscripts/org/craftercms/cstudio/common/lib/common-extraction-api.js">\r\n' +
							'contentNode.addAspect("cstudio-core:pageMetadata");\r\n'+
							'var root = contentXml.getRootElement();\r\n'+
							'extractCommonProperties(contentNode, root);\r\n'+
							'contentNode.save();';

						var writeExtractionCb = {
							success: function() {
								var formDefContent = 
									'<form>\r\n'+
									'<title>'+label+'</title>\r\n'+
									'<description></description>\r\n' +
									'<content-type>/'+type+'/'+name+'</content-type>\r\n' +
									'<objectType>'+type+'</objectType>\r\n' +
									'<properties>\r\n' +
										"<property>\r\n"+
										"<name>content-type</name>\r\n"+
										"<label>Content Type</label>\r\n"+
										"<value>/"+type+'/'+name+"</value>\r\n"+
										"<type>string</type>\r\n"+					
										"</property>\r\n";
								
								if(!this.context.config.objectTypes.length) {
									this.context.config.objectTypes = [ this.context.config.objectTypes.type ];
								}
								
								for(var k=0; k<this.context.config.objectTypes.length; k++) {
									var objectType = this.context.config.objectTypes[k];
									
									if(objectType.name == type) {
										if(!objectType.properties.length) {
											objectType.properties = [ objectType.properties.property ];
										}
										
										var typeProps = objectType.properties;
										
										for(var j=0; j<typeProps.length; j++) {
											var typeProperty = typeProps[j];
											
											formDefContent +=
											"<property>\r\n"+
												"<name>" + typeProperty.name + "</name>\r\n"+
												"<label>" + typeProperty.label + "</label>\r\n"+
												"<value>" + typeProperty.value + "</value>\r\n"+
												"<type>" + typeProperty.type + "</type>\r\n"+					
											"</property>\r\n";
										}
										break;
									}
								}

								formDefContent +=
									'</properties>\r\n' +
									'<sections>\r\n' +		
										'<section>\r\n' +
											'<title>'+label+' Properties</title>\r\n' +
											'<description></description>\r\n' +
											'<defaultOpen>true</defaultOpen>\r\n' +
											'<fields>\r\n' +
											'</fields>\r\n' +
										'</section>\r\n' +
									'</sections>\r\n' +
									'</form>';

								var writeFormDefCb = {
									success: function() {
										CStudioAuthoring.Dialogs.NewContentType.closeDialog();
										CStudioAuthoring.Dialogs.NewContentType.cb.success("/"+type + '/' + name);
									},
									
									failure: function() {
									},
									context: this.context.context
									
								};
								
								this.context.writeConfig(baseServicePath + 'form-definition.xml', formDefContent, writeFormDefCb);
							},
							failure: function() {
							},
							context: this.context
						}
						this.context.writeConfig(baseServicePath + 'extract.js', extractionContent, writeExtractionCb);
					},
					failure: function() {
					},
					context: this.context
				};
				this.context.writeConfig(baseServicePath + 'controller.js', controllerContent, writeControllerCb);
			},
			failure: function() {
			},
			context: CStudioAuthoring.Dialogs.NewContentType
		};

		CStudioAuthoring.Dialogs.NewContentType.writeConfig(baseServicePath + 'config.xml', typeConfig, contentTypeCb);			 
	},

	writeConfig: function(url, content, cb) {
		YAHOO.util.Connect.setDefaultPostHeader(false);
		YAHOO.util.Connect.initHeader("Content-Type", "application/xml; charset=utf-8");
		YAHOO.util.Connect.asyncRequest('POST', CStudioAuthoring.Service.createServiceUri(url), cb, content);		
	},
	
	/**
	 * event fired when the ok is pressed
	 */
	popupCancelClick: function(event) {
		CStudioAuthoring.Dialogs.NewContentType.closeDialog();
	},

	/**
	 * Disables a specific button if one of the inputs in a list match a non-accepted value. Otherwise, enables the button.
	 * This method listens to the onBlur events of the inputs it controls
	 * @param buttonId : Id of the button to control
	 * @param inputConfigObj : An object where the keys are IDs of the inputs, and the values are arrays of
	   reg expressions with values that are invalid for the input
	 */
	buttonValidator: function(buttonId, inputConfigObj) {
        var enableButton,
            button = YDom.get(buttonId),
            configObj = inputConfigObj,
            inputEl = null,
            regExp;

        var checkButton = function () {
            
            enableButton = true;
            
            controlLoop:
                for (var inputId in configObj) {
                    if (configObj.hasOwnProperty(inputId)) {

                        if (inputEl = YDom.get(inputId)) {
                            // Assign and test that input element exists
                            for (var invalidValue in configObj[inputId]) {
                                // Loop through all the invalid values
                                if (inputEl.value.match(configObj[inputId][invalidValue])) {
                                    enableButton = false;
                                    break controlLoop;
                                }
                            }
                        }
                    }
                }

            if (button) {
                if (enableButton) {
                    button.removeAttribute("disabled");
                } else {
                    button.setAttribute("disabled", "disabled");
                }
            }
        };

        YAHOO.Bubbling.on("content-type.values.changed", checkButton);
	},
    
    xmlEscape: function(value) {
        value = value.replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
			.replace(/>/g, '&gt;')
			.replace(/"/g, '&quot;')
			.replace(/&nbsp;/g, '&amp;nbsp;');
            
        return value;
    }
};

CStudioAuthoring.Module.moduleLoaded("new-content-type-dialog", CStudioAuthoring.Dialogs.NewContentType);