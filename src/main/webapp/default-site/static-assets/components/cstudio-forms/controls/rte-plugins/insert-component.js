CStudioForms.Controls.RTE.InsertComponent = CStudioForms.Controls.RTE.InsertComponent || (function () {

	var WAITING_IMG = "<img src='"+CStudioAuthoringContext.authoringAppBaseUri+"/static-assets/themes/cstudioTheme/images/wait.gif' alt='Loading ...' />",
		ERROR_IMG = "<img src='"+CStudioAuthoringContext.authoringAppBaseUri+"/static-assets/themes/cstudioTheme/images/fail.png' alt='Loading failed' />",
		moveEl,
		editEl,
		deleteEl;

	return {
		init: function (ed, url) {

			var _self = this;

			var beforeSaveCb = {
				beforeSave: function() {
					var rteControl =  this.editor.contextControl;
					var docBodyEl = this.editor.dom.doc.body;
					
					if(docBodyEl){
						var componentEls = YAHOO.util.Dom.getElementsByClassName("crComponent", null, docBodyEl),
							componentElsLen = componentEls.length;

						if (componentElsLen > 0) {
							for(var i=0; i < componentElsLen; i++){
								var currentEl = componentEls[i];
								currentEl.innerHTML = "";
								this.editor.contextControl.save();
								_self.renderComponents(this.editor);	
							}
							rteControl._onChange(null, rteControl);
						}
					}
				},
				editor: ed
			};
			ed.contextControl.form.registerBeforeSaveCallback(beforeSaveCb);

		},

	    createControl: function(n, cm) {
	    	var editor = cm.editor,
				model = editor.contextControl.form.model,
				rteWidgets = editor.contextControl.rteConfig.rteWidgets,
				_self = this;

			if (n == 'insertComponent') {
				editor.onLoadContent.add(function(ed, cm) {
					_self.renderComponents(ed);	
				});

				editor.onDblClick.add(function(ed, e) {
					if(_self.isControl(e.target)) {
						_self.showControls(ed, _self.getWidgetContainerElement(e.target));
					}
				});

				editor.onClick.add(function(ed, e) {
					if(_self.firstClick && _self.firstClick == true) {
						_self.firstClick = false;
						return;
					}
						
					_self.handleComponentDrop(ed, e);
					_self.hideControls(ed);
				});

				amplify.subscribe('/rte/blurred', function() {
					_self.hideControls(editor);
				});
				
				if(!rteWidgets) {
					rteWidgets = [];
				}
				
				if(typeof rteWidgets == "object" && !Array.isArray(rteWidgets)) {
					// FIX-ME: Strangely if there's only one widget in the rte config, rteWidgets will not be an array of widgets
					// but an object instead
					rteWidgets = [rteWidgets.widget];
				}

				if(rteWidgets.length > 0) {	
					var c = cm.createMenuButton('insertComponent', {
	                	title : 'Insert Component',
	                    //image : 'img/example.gif',
	                    style: "mce_insertComponent",
	//	                    icons : false
	                });
	                
	                c.rteWidgets = rteWidgets;

	 				c.onRenderMenu.add(function(c, m) {
						for(var i=0; i<rteWidgets.length; i++) {
							var widget = rteWidgets[i];

							var onclickFn = function() {
								var formSaveCb = { 
									success: function(formName, name, value) {
										var id = name.substring(name.lastIndexOf("/")+1).replace(".xml", "");
										
										if(!model['rteComponents']) {
											model['rteComponents'] = [];
										}
										
										var componentItem = {	
											id: id, 
											contentId: name,
											include: name
										};
										
										model['rteComponents'][model['rteComponents'].length] = componentItem;											
										editor.execCommand('mceInsertContent', false, 
										"<div id=\"" + id + "\" class='crComponent' >" + WAITING_IMG + "</div>");
										
										_self.renderComponent(editor, componentItem);	
									},
									failure: function() {
									}	
								};
							
								var path = this.onclick.widget.contentPath;
								path = path.replace("{objectGroupId}", model.objectGroupId);
								path = path.replace("{objectId}", model.objectId);

								path = path.replace("{objectGroupId2}", model.objectGroupId.substring(0, 2));
								path = path.replace("{parentPath}", CStudioAuthoring.Utils.getQueryParameterByName("path").replace(/\/[^\/]*\/[^\/]*\/([^\.]*)(\/[^\/]*\.xml)?$/, "$1"));
								/* Date macros */
								var currentDate = new Date();
								path = path.replace("{year}", currentDate.getFullYear());
								path = path.replace("{month}", ("0" + (currentDate.getMonth() + 1)).slice(-2));
								
								CStudioAuthoring.Operations.openContentWebForm(
									this.onclick.widget.contentType,
									null,
									null,
									path,
									false,
									false,
									formSaveCb,
									[{ name: "childForm", value: "true"}]);
							};							
							
		                	onclickFn.widget = widget;

		                	m.add({title : widget.name, onclick :onclickFn });
				        };
					});

	 				c.onRenderMenu.add(function(c, m) {
						/* ==== */
						// Add widget Libraries
						var rteWidgetLibraries = editor.contextControl.rteConfig.rteWidgetLibraries;
						if(!rteWidgetLibraries) {
							rteWidgetLibraries = [];
						}
				
						if(typeof rteWidgetLibraries == "object" && !Array.isArray(rteWidgetLibraries)) {
							// FIX-ME: Strangely if there's only one widget in the rte config, rteWidgets will not be an array of widgets
							// but an object instead
							rteWidgetLibraries = [rteWidgetLibraries.library];
						}

						if(rteWidgetLibraries.length > 0) {	
			                c.rteWidgetLibraries = rteWidgetLibraries;

			 				c.onRenderMenu.add(function(c, m) {
								for(var i=0; i<rteWidgetLibraries.length; i++) {
									var library = rteWidgetLibraries[i];

									var formSaveCb = { 
										success: function(searchId, selectedTOs) {
											var item = selectedTOs[0];
											var name = item.uri;
											var id = name.substring(name.lastIndexOf("/")+1).replace(".xml", "");
										
											if(!model['rteComponents']) {
												model['rteComponents'] = [];
											}
										
											var componentItem = {	
												id: id, 
												contentId: name,
												include: name
											};
										
											model['rteComponents'][model['rteComponents'].length] = componentItem;											
											editor.execCommand('mceInsertContent', false, 
											"<div id=\"" + id + "\" class='crComponent' >" + WAITING_IMG + "</div>");
										
											_self.renderComponent(editor, componentItem);	
										},
										failure: function() {
										}	
									};

									var onclickFn = function() {
										 var path = this.onclick.library.contentPath;
										 CStudioAuthoring.Operations.openBrowse("", path, 1, "select", true, formSaveCb);
									};							
							
				                	onclickFn.library = library;

				                	m.add({title : "Library: " + library.name, onclick :onclickFn });
						        };
							});
						}
					});

			        return c;
				}
			};

	        return null;
	    },
	   
	   	/**
	   	 * render components
	   	 */ 
	    renderComponents: function(editor) {
			var model = editor.contextControl.form.model;
			var _self = this;
			var components = [];
			
			if(model['rteComponents'] && model['rteComponents'].length) {
				components = model['rteComponents'];
			}
			
			for(var i=0; i<components.length; i++) {

				var componentItem = components[i];
				this.renderComponent(editor, componentItem);
			}						
	    },

		/**
		 * render individual component
		 */    
	    renderComponent: function(editor, componentItem) {
			var componentEl = editor.dom.doc.getElementById(componentItem.id);
			var _self = this;
			
			if(componentEl) {
				try {
					componentEl.innerHTML = WAITING_IMG;
					previewCb = {
						success: function(content) {
							componentEl.innerHTML = content;
							YAHOO.util.Dom.addClass(componentEl, 'mceNonEditable');
						},
						
						failure: function() {
							componentEl.innerHTML = ERROR_IMG;
						}
					};
					
					CStudioAuthoring.Service.getComponentPreview(componentItem.contentId, previewCb);		
				}
				catch(err) {
				}
			}
	    },
	    
	    showControls: function(editor, el) {
	    	var _self = this;
	    	var controlsEl = editor.dom.doc.getElementById("cstudio-component-controls");
	    	var model = editor.contextControl.form.model;

			controlsEl = editor.dom.doc.createElement("ul");
			controlsEl.id = "cstudio-component-controls";

			// Do not show show the context menu when right-clicking on the component controls
			controlsEl.className = "context-menu-off";	
			
			var controlsHTML = "<li class='edit'><a href='#'><span class='visuallyhidden'>Edit</span></a></li>"+
								"<li class='move'><a href='#'><span class='visuallyhidden'>Move</span></a></li>"+
								"<li class='delete'><a href='#'><span class='visuallyhidden'>Delete</span></a></li>";

			controlsEl.innerHTML = controlsHTML;
			el.appendChild(controlsEl);
			
			moveEl = tinymce.DOM.select("#cstudio-component-controls .move > a", editor.getDoc())[0];
			moveEl.onclick = function() {
				var id = el.id;
				var contentItem = null;
				var components = model['rteComponents'];
				for(var i=0; i<components.length; i++) {
					if(components[i].id == id) {
						_self.componentOnTheMove = components[i];
						_self.firstClick = true; // should be able to stop the event more elegantly
						editor.dom.doc.body.style.cursor = "move";
						editor.dom.doc.body.focus();
						break;	
					}
				}
			}
			
			editEl = tinymce.DOM.select("#cstudio-component-controls .edit > a", editor.getDoc())[0];
			editEl.onclick = function() {
				var id = el.id;
				var contentItem = null;
				var components = model['rteComponents'];
				for(var i=0; i<components.length; i++) {
					if(components[i].id == id) {
						contentItem = components[i];
						break;	
					}
				}
				
				if(contentItem) {
					var lookupItemCb = {
						success: function(contentTO) {
							var formSaveCb = { 
								success: function(formName, name, value) {
									_self.renderComponent(tinyMCE.activeEditor, contentItem);	
								},
								failure: function() {
								}	
							};
			
							CStudioAuthoring.Operations.openContentWebForm(
								contentTO.item.contentType,
								null,
								null,
								contentItem.contentId,
								true,
								false,
								formSaveCb,
								[{ name: "childForm", value: "true"}]);
						},
						failure: function() {
						}
					};
					
					CStudioAuthoring.Service.lookupContentItem(CStudioAuthoringContext.site, contentItem.contentId, lookupItemCb);
				}					
			}
			
			deleteEl = tinymce.DOM.select("#cstudio-component-controls .delete > a", editor.getDoc())[0];
			deleteEl.onclick = function() {
				var id = el.id;
				var components = model['rteComponents'];
				
				el.parentNode.removeChild(el);

				for(var i=0; i<components.length; i++) {
					if(components[i].id == id) {
						model['rteComponents'].splice(i,1);
						break;	
					}
				}			
			};
	    },
	    
	    handleComponentDrop: function(ed, e) {
			var _self = this;

			if(_self.componentOnTheMove && e.target !== moveEl) {
				var componentEl = ed.dom.doc.getElementById(_self.componentOnTheMove.id);
				componentEl.parentNode.removeChild(componentEl);

				tinyMCE.activeEditor.execCommand('mceInsertContent', false, 
					"<div id=\"" + _self.componentOnTheMove.id + "\" class='crComponent' >" + WAITING_IMG + "</div>");
				
				_self.renderComponent(tinyMCE.activeEditor, _self.componentOnTheMove);				

				ed.dom.doc.body.style.cursor = "default";
				_self.componentOnTheMove = null;
			}
		},
	    
	    hideControls: function(editor) {
	    	var controlsEl;

	    	try {
	    		controlsEl = editor.dom.doc.getElementById("cstudio-component-controls");
	    	} catch (err) {
	    		// FIXME: caused by _renderRepeat (forms-engine.js)
	    		// When repeat groups are created, the whole container is re-rendered so all repeat groups (old and new)
	    		// create new controls. However, the existing controls (along with the events bound to them) are not 
	    		// removed so when the "/rte/blurred" event is published, it is heard by these controls as well.
	    		// Only IE finds this to be an error (hence the try/catch block -uncomment the throw, create a new repeat
	    		// group and save the form to see for yourself); however, this implementation is equally affecting all 
				// browsers => it is resulting in a memory leak and will degrade performace as new repeat-groups are created.
	    		// throw "Unable to access cstudio-component-controls (insert-component.js @hideControls)";	
	    	}
	    	
	    	if(controlsEl) {
	    		controlsEl.parentNode.removeChild(controlsEl);
	    	}
	    },
	    
	    getWidgetContainerElement: function(el) {
	    	if (YAHOO.util.Dom.hasClass(el, "crComponent")) {
	    		return el;
	    	} else {
	    		return YAHOO.util.Dom.getAncestorByClassName(el, "crComponent");
	    	}
	    },    
	    
	    isControl: function(el) {
	    	var containerEl = this.getWidgetContainerElement(el);
	    	return (containerEl) ? true : false;
	    }
	};

}) ();

tinymce.create('tinymce.plugins.CStudioInsertComponentPlugin', CStudioForms.Controls.RTE.InsertComponent);

// Register plugin with a short name
tinymce.PluginManager.add('insertcomponent', tinymce.plugins.CStudioInsertComponentPlugin);

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-rte-insert-component", CStudioForms.Controls.RTE.InsertComponent);
