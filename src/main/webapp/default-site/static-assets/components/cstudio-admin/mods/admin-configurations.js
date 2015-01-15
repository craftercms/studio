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

		CStudioAuthoring.Utils.addCss("/static-assets/components/cstudio-admin/mods/admin-configurations.css");
		CStudioAdminConsole.Tool.AdminConfig = CStudioAdminConsole.Tool.AdminConfig ||  function(config, el)  {
			this.containerEl = el;
			this.config = config;
			this.types = [];
			return this;
		}

		/**
		 * Overarching class that drives the content type tools
 		*/
		YAHOO.extend(CStudioAdminConsole.Tool.AdminConfig, CStudioAdminConsole.Tool, {
			width: 1000,
			height: 600,
			
			renderWorkarea: function() {
				var workareaEl = document.getElementById("cstudio-admin-console-workarea");
		
					workareaEl.innerHTML = 
						"<div id='config-area'>" +
						"</div>";
						var actions = [];
						CStudioAuthoring.ContextualNav.AdminConsoleNav.initActions(actions);
						this.renderJobsList();
			},
	
			renderJobsList: function() {
		
				var containerEl = document.getElementById("config-area");
				
				containerEl.innerHTML = 
					"<div class='configuration-window'>" +
						"<select id='config-list'>" +
					 		" <option value='' >Select Configuration</option>" +
						"</select>" +
						"<div id='edit-area'>" + 
							"<div id='menu-area'>" + 
								"<div id='config-description'>" + 
								"</div>" + 
								"<div id='config-buttons'>" +
									//"<button type='submit' id='save-button' class='edit-button'>Save</button>" + 
								"</div>" + 
							"</div>" + 
							"<div id='content-area'>" +
								"<div id='edit-window'>" + 
									"<textarea id='text-editor'></textarea>" +
								"</div>" + 
								"<div id='sample-window'>" +
									"<textarea id='sample-text'></textarea>" + 
								"</div>" + 
							"</div>" + 
						"</div>" + 
					"</div>";
				// set editor for configuration file 
				var editorEl = document.getElementById("text-editor");	
				var editorContainerEl = document.getElementById("edit-window");	
				this.setEditor(editorContainerEl, editorEl, false);
				// set editor for sample configuration file 
				var sampleEditorEl = document.getElementById("sample-text");	
				var sampleEditorContainerEl = document.getElementById("sample-window");	
				this.setEditor(sampleEditorContainerEl, sampleEditorEl, true);

				var itemSelectEl = document.getElementById("config-list");
				// add action buttons
				var buttonAreaEl = document.getElementById("config-buttons");
				this.addButtons(buttonAreaEl, itemSelectEl, editorEl.codeMirrorEditor);
				// set configuration dropdown
				var editAreaEl = document.getElementById("edit-area");
				this.loadConfigFiles(itemSelectEl, editAreaEl, editorEl.codeMirrorEditor, sampleEditorEl.codeMirrorEditor);

				// hide display area by default
				editAreaEl.style.display = 'none';
		
			},
			
			/*
			* populate the list of configuration files
			*/
			loadConfigFiles: function (itemSelectEl, editAreaEl, editor, sampleEditor) {
				// load configuration to get the configuration files list
				CStudioAuthoring.Service.lookupConfigurtion(
					CStudioAuthoringContext.site, 
					"/administration/config-list.xml", {
						success: function(config) {
							if (config.files && config.files.length) {
								var index = 1;
								for (var fileIndex in config.files) {
									var fileConfig = config.files[fileIndex];
									var option = new Option(fileConfig.title, fileConfig.path, false, false);
									option.setAttribute("description", fileConfig.description);
									option.setAttribute("sample", fileConfig.samplePath);
									itemSelectEl.options[index++] = option;
								}
							} else if (config.files.file) {
								var fileConfig = config.files.file;
								var option = new Option(fileConfig.title, fileConfig.path, false, false);
								option.setAttribute("description", fileConfig.description);
								option.setAttribute("sample", fileConfig.samplePath);
								itemSelectEl.options[1] = option;
							}
						},
						failure: function() {
							alert("Failed to load configuration");
						}
					}
				);
			
				// add onchange behavior to display selected
				itemSelectEl.onchange = function() {
					var selectedIndex = itemSelectEl.selectedIndex;
					if(selectedIndex != 0) {
						editAreaEl.style.display = 'block';
						var descriptionEl = document.getElementById("config-description");
						descriptionEl.innerHTML = itemSelectEl[selectedIndex].getAttribute("description");
						// load configuration into editor
						var url = '/studio/api/1/service/api/1/content/get-content-at-path.json?path=/cstudio/config/sites/' +
							          CStudioAuthoringContext.site + itemSelectEl[selectedIndex].value;
						var getConfigCb = {
							success: function(response) {
								editor.setValue(response.responseText);
								CStudioAdminConsole.Tool.AdminConfig.prototype.expandEditor(editor);
							},
							failure: function() {
								editor.setValue("");
								CStudioAdminConsole.Tool.AdminConfig.prototype.expandEditor(editor);
							}
						};
						YAHOO.util.Connect.asyncRequest('GET', url, getConfigCb);
						
						var sampleTextEl = document.getElementById("sample-text");

						// load sample configuration into view sample area
						var samplePath = itemSelectEl[selectedIndex].getAttribute("sample");
						var viewSampleButtonEl = document.getElementById("view-sample-button");
						if (samplePath != 'undefined' && samplePath != '') {
							var url = '/studio/api/1/service/api/1/content/get-content-at-path.json?path=/cstudio/config/sites/' +
								    CStudioAuthoringContext.site + itemSelectEl[selectedIndex].getAttribute("sample");
							var getSampleCb = {
								success: function(response) {
									var sampleAreaEl = document.getElementById("sample-window");
									sampleAreaEl.style.display = 'inline';
									sampleEditor.setValue(response.responseText);
									CStudioAdminConsole.Tool.AdminConfig.prototype.shrinkEditor(sampleEditor);
									viewSampleButtonEl.style.display = 'inline';
									var hideSampleButtonEl = document.getElementById("hide-sample-button");
									hideSampleButtonEl.style.display = 'none';
									sampleAreaEl.style.display = 'none';
								},
								failure: function() {
									viewSampleButtonEl.style.display = 'none';
								}
							};
							YAHOO.util.Connect.asyncRequest('GET', url, getSampleCb);
						} else {
							viewSampleButtonEl.style.display = 'none';
						}

					} else {
						editAreaEl.style.display = 'none';
					}
				}; // end of change
			},
			
			/*
			* create CodeMirror editor
			*/
			setEditor: function (editorContainerEl, editorEl, readOnly) {
				// create edit area
				editorEl.style.backgroundColor= "white";
				editorEl.codeMirrorEditor = CodeMirror.fromTextArea(editorEl, {
					mode: 'xml',
					lineNumbers: true,
					lineWrapping: true,
					smartIndent: false
				});
				
				var codeEditorEl = YAHOO.util.Dom.getElementsByClassName("CodeMirror", null, editorContainerEl)[0];
				var codeEditorCanvasEl = YAHOO.util.Dom.getElementsByClassName("CodeMirror-wrap", null, editorContainerEl)[0];
						codeEditorCanvasEl.style.height = "100%";
						codeEditorCanvasEl.style.border = "thick solid #EEEEEE";
				var codeEditorScrollEl = YAHOO.util.Dom.getElementsByClassName("CodeMirror-scroll", null, editorContainerEl)[0];
						codeEditorScrollEl.style.height = "100%";
				
				if (readOnly) {
					codeEditorEl.style.backgroundColor = "#EEEEEE";
					editorEl.codeMirrorEditor.setOption("readOnly", true);
				} else {
					codeEditorEl.style.backgroundColor = "white";
				}

			},
			
			
			/*
			* add save, view sample and hide sample buttons
			*/
			addButtons: function (containerEl, itemSelectEl, editor) {
				containerEl.innerHTML = 
					"<button type='submit' id='save-button' class='edit-button'>Save</button>" + 
					"<button type='submit' id='view-sample-button' class='edit-button'>View Sample</button>" + 
					"<button type='submit' id='hide-sample-button' class='edit-button'>Hide Sample</button>";

				// add button actions
				var saveButtonEl = document.getElementById("save-button");
				// save the configuration file back to repo 
				saveButtonEl.onclick = function () { 
					var selectedIndex = itemSelectEl.selectedIndex;
					var saveCb = { success: function() { alert('Saved'); }, 
						failure: function() { alert('Save Failed'); } 
					};
					var xml = editor.getValue();
					var savePath = itemSelectEl[selectedIndex].value;
					if (savePath != 'undefined' && savePath != '') {
						var url = '/studio/proxy/alfresco/cstudio/wcm/config/write?path=/config/sites/' + 
							CStudioAuthoringContext.site + itemSelectEl[selectedIndex].value;

						YAHOO.util.Connect.setDefaultPostHeader(false);
						YAHOO.util.Connect.initHeader("Content-Type", "application/xml; charset=utf-8");
						YAHOO.util.Connect.asyncRequest('POST', url, saveCb, xml);
					} else {
						alert("No configuration path is defined.");
					}

				}; // end of save
				
				var viewSampleButtonEl = document.getElementById("view-sample-button");
				var hideSampleButtonEl = document.getElementById("hide-sample-button");
				var sampleAreaEl = document.getElementById("sample-window");
				
				viewSampleButtonEl.onclick = function () {
					CStudioAdminConsole.Tool.AdminConfig.prototype.shrinkEditor(editor);
					hideSampleButtonEl.style.display = 'inline';
					viewSampleButtonEl.style.display = 'none';
					sampleAreaEl.style.display = 'inline';
				};
				
				hideSampleButtonEl.onclick = function () {
					CStudioAdminConsole.Tool.AdminConfig.prototype.expandEditor(editor);
					hideSampleButtonEl.style.display = 'none';
					viewSampleButtonEl.style.display = 'inline';
					sampleAreaEl.style.display = 'none';
				};
				hideSampleButtonEl.style.display = 'none';
			},
			
			expandEditor: function(editor) {
				editor.setSize(this.width, this.height);
			},
			
			shrinkEditor: function(editor) {
				editor.setSize(this.width/2, this.height);
			}
			
		});

	CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-admin-configurations",CStudioAdminConsole.Tool.AdminConfig);

}});

	
