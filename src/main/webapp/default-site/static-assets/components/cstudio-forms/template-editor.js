CStudioAuthoring.Module.requireModule(
	'codemirror',
	'/static-assets/components/cstudio-common/codemirror/lib/codemirror.js', {}, {
	moduleLoaded: function() {

		CStudioAuthoring.Utils.addJavascript("/static-assets/components/cstudio-common/codemirror/lib/util/formatting.js");
		CStudioAuthoring.Utils.addJavascript("/static-assets/components/cstudio-common/codemirror/mode/xml/xml.js");
		CStudioAuthoring.Utils.addJavascript("/static-assets/components/cstudio-common/codemirror/mode/javascript/javascript.js");
		CStudioAuthoring.Utils.addJavascript("/static-assets/components/cstudio-common/codemirror/mode/htmlmixed/htmlmixed.js");
		CStudioAuthoring.Utils.addJavascript("/static-assets/components/cstudio-common/codemirror/mode/css/css.js");
		CStudioAuthoring.Utils.addCss("/static-assets/components/cstudio-common/codemirror/lib/codemirror.css");
		CStudioAuthoring.Utils.addCss("/static-assets/themes/cstudioTheme/css/template-editor.css");

		CStudioAuthoring.Module.requireModule(
			"cstudio-forms-engine",
			'/static-assets/components/cstudio-forms/forms-engine.js',
			{  },
			{ moduleLoaded: function() {
								
				CStudioForms.TemplateEditor = CStudioForms.TemplateEditor ||  function()  {
					return this;
				}

				
				var CMgs = CStudioAuthoring.Messages;
            	var contextNavLangBundle = CMgs.getBundle("contextnav", CStudioAuthoringContext.lang);

				CStudioForms.TemplateEditor.prototype = {

					render: function(templatePath, channel, onSaveCb) {
	
						var getContentCb = {
							success: function(response) {
								this.context.renderTemplateEditor(templatePath, response, onSaveCb);
							},
							failure: function() {
							},
							context: this
						};
						
						CStudioAuthoring.Service.getContent(templatePath, true, getContentCb, false);			
					},
					
					renderTemplateEditor: function(templatePath, content, onSaveCb) {

						var permsCallback = {
								success: function(response) {
									var isWrite = CStudioAuthoring.Service.isWrite(response.permissions);

									var modalEl = document.createElement("div");
									modalEl.id = "cstudio-template-editor-container-modal";
									document.body.appendChild(modalEl);
											
									var containerEl = document.createElement("div");
									containerEl.id = "cstudio-template-editor-container";
									YAHOO.util.Dom.addClass(containerEl, 'seethrough');
									modalEl.appendChild(containerEl);
									
									var formHTML = 
										"<div id='template-editor-toolbar'><div id='template-editor-toolbar-variable'></div></div>" +
										"<div id='editor-container'>"+
										"</div>" + 
										"<div id='template-editor-button-container'>";
										
									if(isWrite == true) {
										formHTML += 
											"<div class='edit-buttons-container'>" +
				 						    	"<div  id='template-editor-update-button' class='btn btn-primary cstudio-template-editor-button'>Update</div>" + 
												"<div  id='template-editor-cancel-button' class='btn btn-default cstudio-template-editor-button'>Cancel</div>" +
											"<div/>";
									}
									else {
										formHTML +=
											"<div  id='template-editor-cancel-button' style='right: 120px;' class='btn btn-default cstudio-template-editor-button'>Close</div>";							
									}

									formHTML +=
										"</div>";

									containerEl.innerHTML = formHTML;
									var editorContainerEl = document.getElementById("editor-container");	
									var editorEl = document.createElement("textarea");
									//editorEl.cols= "79";
									//editorEl.rows= "40";
									editorEl.style.backgroundColor= "white";
									editorEl.value= content;
									editorContainerEl.appendChild(editorEl);
									
									
									var initEditorFn = function() {
										if(typeof CodeMirror === "undefined" ) {
											window.setTimeout(500, initEditorFn);
										}
										else {		
											var mode = "htmlmixed";
											
											if(templatePath.indexOf(".css") != -1) {
												mode = "css";
											}
											else if(templatePath.indexOf(".js") != -1) {
												mode = "javascript";
											}
											
											editorEl.codeMirrorEditor = CodeMirror.fromTextArea(editorEl, {
												mode: mode,
												lineNumbers: true,
												lineWrapping: true,
												smartIndent: false//
												//    onGutterClick: foldFunc,
												//    extraKeys: {"Ctrl-Q": function(cm){foldFunc(cm, cm.getCursor().line);}}
							  				});
							  				
							  				var codeEditorEl = YAHOO.util.Dom.getElementsByClassName("CodeMirror", null, editorContainerEl)[0];
							  				codeEditorEl.style.backgroundColor = "white";
							  				
							  				var codeEditorCanvasEl = YAHOO.util.Dom.getElementsByClassName("CodeMirror-wrap", null, editorContainerEl)[0];
							  				codeEditorCanvasEl.style.height = "100%";

							  				var codeEditorScrollEl = YAHOO.util.Dom.getElementsByClassName("CodeMirror-scroll", null, editorContainerEl)[0];
							  				codeEditorScrollEl.style.height = "100%";
										}
									};
									
									initEditorFn();

									if(templatePath.indexOf(".ftl") != -1) {
										var templateEditorToolbarVarElt = document.getElementById("template-editor-toolbar-variable");
										var variableLabel = document.createElement("label");
										variableLabel.innerHTML = CMgs.format(contextNavLangBundle, "variableLabel");
										templateEditorToolbarVarElt.appendChild(variableLabel);

										//Create array of options to be added
										var variableOpts = [
											{label:"Content variable", value:"${model.VARIABLENAME}"},
										    {label:"Request parameter", value:"${RequestParameters[\"PARAMNAME\"]!\"DEFAULT\"}"},
										    {label:"Studio support", value:"<#import \"/templates/system/common/cstudio-support.ftl\" as studio />\r\n\t...\r\n\t<@studio.toolSupport />"},
										    {label:"Dynamic navigation", value:"<#include \"/templates/web/navigation/navigation.ftl\">\r\n\t...\r\n\t<@renderNavigation \"/site/website\", 1 />"},

										    {label:"Incontext editing attribute (pencil)", value:"<@studio.iceAttr iceGroup=\"ICEGROUID\"/>"},
										    {label:"Component DropZone attribute", value:"<@studio.componentContainerAttr target=\"TARGETID\" />"},
										    {label:"Component attribute", value:"<@studio.componentAttr path=model.storeUrl ice=false />"},
										    {label:"Render list of components", value:"<#list model.VARIABLENAME.item as module>\r\n\t<@renderComponent component=module />\r\n</#list>"},
										    {label:"Iterate over a list of items and load content item", value:"<#list model.VARIABLENAME.item as myItem>\r\n\t<#assign myContentItem =  siteItemService.getSiteItem(myItem.key) />\r\n\t${myContentItem.variableName}\r\n</#list>"},
										    {label:"Iterate over repeat group", value:"<#list model.VARIABLENAME.item as row>\r\n\t${row.VARIABLENAME}\r\n</#list>"},


										    {label:"Freemarker value assignment", value:"<#assign imageSource = model.image!\"\" />"},
										    {label:"Freemarker value IF", value:"<#if CONDITION>\r\n\t...\r\n</#if>"},
										    {label:"Freemarker value LOOP", value:"<#list ARRAY as value>\r\n\t${value_index}: ${value}\r\n</#list>"},
										    {label:"Freemarker Fragment include", value:"<#include \"/templates/PATH\" />"},
										    {label:"Freemarker Library import", value:"<#import \"/templates/PATH\" as NAMESPACE />"},

											{label:"HTML Page", value:"<#import \"/templates/system/common/cstudio-support.ftl\" as studio />\r\n<html lang=\"en\">\r\n<head>\r\n\t</head>\r\n\t<body>\r\n\t\t<h1>CONTENT HERE</h1>\r\n\t<@studio.toolSupport/>\r\n\t</body>\r\n</html>"},
											{label:"HTML Component", value:"<#import \"/templates/system/common/cstudio-support.ftl\" as studio />\r\n<div <@studio.componentAttr path=model.storeUrl ice=false /> >\r\nCOMPONENT MARKUP</div>"},

										 ];

										//Create and append select list
										var selectList = document.createElement("select");
										selectList.id = "variable";
										templateEditorToolbarVarElt.appendChild(selectList);

										//Create and append the options
										for (var i = 0; i < variableOpts.length; i++) {
										    var option = document.createElement("option");
										    option.value = variableOpts[i].value;
										    option.text = variableOpts[i].label;
										    selectList.appendChild(option);
										}

										var addButton = document.createElement("button");
										addButton.id = "addButtonVar";
										addButton.innerHTML = "Add Code";
										addButton.className = "btn btn-primary";
										templateEditorToolbarVarElt.appendChild(addButton);

										addButton.onclick = function() {
									    	editorEl.codeMirrorEditor.replaceRange(selectList.options[selectList.selectedIndex].value, editorEl.codeMirrorEditor.getCursor());
										};
									}	

									var cancelEl = document.getElementById('template-editor-cancel-button');
									cancelEl.onclick = function() {
							            var cancelEditServiceUrl = "/api/1/services/api/1/content/unlock-content.json"
							                + "?site=" + CStudioAuthoringContext.site
							                + "&path=" + templatePath;

							            var cancelEditCb = {
							                success: function(response) {
							                    modalEl.parentNode.removeChild(modalEl);
							                },
							                failure: function() {
							                }
							            };
							            YAHOO.util.Connect.asyncRequest('GET', CStudioAuthoring.Service.createServiceUri(cancelEditServiceUrl), cancelEditCb);
									};

									var saveSvcCb = {
										success: function() {
							                modalEl.parentNode.removeChild(modalEl);
							                onSaveCb.success();
										},
										failure: function() {
										}
									};
									
									if(isWrite == true) {
										var saveEl = document.getElementById('template-editor-update-button');
										saveEl.onclick = function() {
											editorEl.codeMirrorEditor.save();
											var value = editorEl.value;
											var path = templatePath.substring(0, templatePath.lastIndexOf("/"));
											var filename = templatePath.substring(templatePath.lastIndexOf("/")+1);

                    var writeServiceUrl = "/api/1/services/api/1/content/write-content.json" +
                        "?site=" + CStudioAuthoringContext.site +
                        "&phase=onSave" +
                        "&path=" + path +
                        "&fileName=" + filename +
                        "&user=" + CStudioAuthoringContext.user +
                        "&unlock=true";

					                    
											YAHOO.util.Connect.setDefaultPostHeader(false);
											YAHOO.util.Connect.initHeader("Content-Type", "text/pain; charset=utf-8");
											YAHOO.util.Connect.asyncRequest('POST', CStudioAuthoring.Service.createServiceUri(writeServiceUrl), saveSvcCb, value);
										};
									}					

								},
								failure: function() {
									
								}
						}
						
						CStudioAuthoring.Service.getUserPermissions(
							CStudioAuthoringContext.site, 
							templatePath, 
							permsCallback);
						
					}	
				};

				CStudioAuthoring.Module.moduleLoaded("cstudio-forms-template-editor",CStudioForms.TemplateEditor);
			}
		} );
	}
} );	