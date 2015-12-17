CStudioForms.Controls.RTE = CStudioForms.Controls.RTE ||  
function(id, form, owner, properties, constraints, readonly)  {
	this.owner = owner;
	this.owner.registerField(this);
	this.errors = []; 
	this.properties = properties;
	this.constraints = constraints;
	this.inputEl = null;
	this.countEl = null;
	this.required = false;
	this.value = "_not-set";
	this.form = form;
	this.id = id;
	this.registeredPlugins = [];
	this.rteTables = [];
	this.rteTableStyles = {};
	this.rteLinkStyles = [];
	this.rteLinkTargets = [];
	this.readonly = readonly;
	this.codeModeXreduction = 130;	// Amount of pixels deducted from the total width value of the RTE in code mode
	this.codeModeYreduction = 130;	// Amount of pixels deducted from the total height value of the RTE in code mode
	this.rteWidth;
	this.delayedInit = true; 	// Flag that indicates that this control takes a while to initialize 
	
	return this;
}

CStudioForms.Controls.RTE.plugins =  CStudioForms.Controls.RTE.plugins || {};

CStudioAuthoring.Module.requireModule(
	"cstudio-forms-rte-config-manager",
	'/static-assets/components/cstudio-forms/controls/rte-config-manager.js',
	{  },
	{ moduleLoaded: function() {

		var YDom = YAHOO.util.Dom,
			YEvent = YAHOO.util.Event,
			YSelector = YAHOO.util.Selector;

YAHOO.extend(CStudioForms.Controls.RTE, CStudioForms.CStudioFormField, {

    getLabel: function() {
        return CMgs.format(langBundle, "richTextEditor");
    },
    
    /**
     * render the RTE
     */	
	render: function(config, containerEl) {
		var _thisControl = this;

		var configuration = "generic";
		for(var i=0; i<config.properties.length; i++) {
			var prop = config.properties[i];

			if(prop.name == "rteConfiguration") {
				if(prop.value && prop.Value != "") {
					configuration = prop.value;
				}
				
				break;
			}
		};
			
		CStudioForms.Controls.RTEManager.getRteConfiguration(configuration, "no-role-support", {
			success: function(rteConfig) {

				_thisControl._loadPlugins(rteConfig.rteModules.module, {
					success: function() {
						this.control._initializeRte(this.controlConfig, this.rteConfig, this.containerEl);
						
						this.control.form.registerBeforeUiRefreshCallback({
							beforeUiRefresh: function() {
								var content = tinyMCE.activeEditor.getContent({format : 'raw'});
                                if(content != ""){
                                    //Could be that the model hasn't been loaded yet
                                    tinyMCE.activeEditor.contextControl.updateModel(content);
                                }
							},
							context: this.control
						});
					},
					failure: function() {
					},
					
					controlConfig: config,
					rteConfig: rteConfig,
					containerEl: containerEl,
					control: _thisControl
				});
				_thisControl.rteTables = rteConfig.rteTables;
				_thisControl.rteTableStyles = rteConfig.rteTablestyles;
				_thisControl.rteLinkStyles = rteConfig.rteLinkStyles.style;
				_thisControl.rteLinkTargets = rteConfig.rteLinkTargets;
			},
			failure: function() {
			}
		});
	},

	/**
	 * get the value of this control
	 */
	getValue: function() {
		if(this.editor) {
			this.editor.save();
			value = this.inputEl.value; 
			this.value = value;
		}
		
		return this.value;
	},
		
	/**
	 * set the value for the control
	 */
	setValue: function(value) {
		this.value = value;
		try {
			tinyMCE.activeEditor.setContent(value, {format : 'raw'});
		}
		catch(err) {
		};

        if(this.inputEl){
            this.inputEl.value = value;
            this.count(null, this.countEl, this.inputEl);
            this._onChange(null, this);
        }
		this.updateModel(value);
        this.edited = false;
	},

	updateModel: function(value) {
		this.form.updateModel(this.id, CStudioForms.Util.unEscapeXml(value));
	},	

	/**
	 * get the widget name
	 */
	getName: function() {
		return "rte";
	},
	
	/**
	 * get supported properties
	 */
	getSupportedProperties: function() {
		return [
			{ label: CMgs.format(langBundle, "width"), name: "width", type: "int" },
			{ label: CMgs.format(langBundle, "height"), name: "height", type: "int" },
			{ label: CMgs.format(langBundle, "maxLength"), name: "maxlength", type: "int" },
			{ label: CMgs.format(langBundle, "allowResize"), name: "allowResize", type: "boolean" },
			{ label: CMgs.format(langBundle, "forceRootBlockP"), name: "forceRootBlockPTag", type: "boolean", defaultValue: "true" },
			{ label: CMgs.format(langBundle, "forcePNewLines"), name: "forcePTags", type: "boolean", defaultValue: "true" },
			{ label: CMgs.format(langBundle, "forceBRNewLines"), name: "forceBRTags", type: "boolean", defaultValue: "false" },
			{ label: CMgs.format(langBundle, "requireImageAlt"), name: "forceImageAlts", type: "boolean", defaultValue: "false" },
			{ label: CMgs.format(langBundle, "supportedChannels"), name: "supportedChannels", type: "supportedChannels" },
			{ label: CMgs.format(langBundle, "RTEConfiguration"), name: "rteConfiguration", type: "string", defaultValue: "generic" },
			{ label: CMgs.format(langBundle, "imageManager"), name: "imageManager", type: "datasource:image" }
		];
	},

	/**
	 * get the supported constraints
	 */
	getSupportedConstraints: function() {
		return [
			{ label: CMgs.format(langBundle, "required"), name: "required", type: "boolean" }
		];
	},

	/**
	 * Scroll to the top of an element, minus a pixel offset (in the case of an RTE, we use the pixel offset to move the RTE under the
	 * toolbar; otherwise, the toolbar will appear over the RTE)
	 * @param: el
	 * @param: pixOffset (optional -defaults to 0)
	 */
	scrollToTopOfElement: function (el, pixOffset) {
		pixOffset = pixOffset || 0;
		var scrollY = YDom.getY(el) - pixOffset;
	    window.scrollTo(0, scrollY);
	},

	// Clear the current selection in tinymce's text editor
	clearTextEditorSelection: function () {
		var rootEl = this.editor.dom.getRoot(),
			caretEl = document.createElement("span"),
			textEl = document.createTextNode(" ");	 // IE9 needs the span tag to have a text node; not necessary in FF nor Chrome

		caretEl.id = "caret_pos_holder";
		caretEl.appendChild(textEl);
		// Place cursor at the beginning of the textarea				
		if (rootEl.firstChild) {
			YDom.insertBefore(caretEl, rootEl.firstChild);
		} else {
			rootEl.appendChild(caretEl);
		}
		this.editor.selection.select(this.editor.dom.select('#caret_pos_holder')[0]); //select the span
		this.editor.dom.remove(this.editor.dom.select('#caret_pos_holder')[0]); //remove the span
	},

	focusIn: function() {

		var heightVal,
			widthVal;

		if (YDom.hasClass(this.containerEl, "rte-inactive")) {
			// No need to focus on an RTE that is already active
	        YDom.replaceClass(this.containerEl, "rte-inactive", "rte-active");
            var elements = YDom.getElementsByClassName('cstudio-form-container', 'div');
            YDom.setStyle(elements[0],'margin-top', '50px');
 
	        if (YDom.hasClass(this.containerEl, "text-mode")) {
	        	// The RTE is in text mode
		        this.resizeEditor(this.editor);		// Resize the editor (in case its contents exceed its set height)
	        } else {
	        	// The RTE is in code mode
	        	this.resizeCodeMirror(this.editor.codeMirror);
	        }
	        this.scrollToTopOfElement(this.containerEl, 30);
		}
	},
	
	focusOut: function() {

		var widthVal, heightVal, sizeCookie, cookieHeight;

		amplify.publish('/rte/blurred'); // Notify other components
		YDom.replaceClass(this.containerEl, "rte-active", "rte-inactive");
		var elements = YDom.getElementsByClassName('cstudio-form-container', 'div');
		YDom.setStyle(elements[0], 'margin-top', '20px');

		if (YDom.hasClass(this.containerEl, "text-mode")) {
			// The RTE is in text mode        	
			sizeCookie = tinymce.util.Cookie.getHash("TinyMCE_" + this.editor.id + "_size" + window.name);
			cookieHeight = (sizeCookie) ? sizeCookie.ch : 0;

			// Give priority to the height value stored in the cookie (if there's one)
			heightVal = (cookieHeight) ? cookieHeight : this.editor.settings.height;

			tinymce.DOM.setStyle(this.editor.editorId + "_ifr", "height", heightVal + "px");
			this.editor.getWin().scrollTo(0, 0); // Scroll to the top of the editor window

			// The editor selection is automatically cleared in IE9 when the text editor is blurred
			if (!YAHOO.env.ua.ie || YAHOO.env.ua.ie >= 10) {
				this.clearTextEditorSelection();
			}
			this.editor.onDeactivate.dispatch(this.editor, null); // Fire tinyMCE handlers for onDeactivate (eg. used by contextmenu)
		} else {
			// The RTE is in code mode
			widthVal = this.containerEl.clientWidth - this.codeModeXreduction;
			this.editor.codeMirror.setSelection({
				line: 0,
				ch: 0
			}); // Clear the current selection -in case there was any
			this.editor.codeMirror.setCursor({
				line: 0,
				ch: 0
			}); // Set the cursor to the beginning of the code editor
			this.editor.codeMirror.setSize(widthVal, +this.editor.settings.height);
			this.editor.codeMirror.scrollTo(0, 0); // Scroll to the top of the editor window
			this.editor.setContent(this.editor.codeMirror.getValue()); // Transfer content in codeMirror to RTE
		}
		this.save(); // Save the content in RTE and update form model
	},

	resizeEditor: function (editor, onInit) {

		var sizeCookie = tinymce.util.Cookie.getHash("TinyMCE_" + editor.id + "_size" + window.name);
		var cookieHeight = (sizeCookie) ? sizeCookie.ch : 0;
		
		var heightVal = Math.max(editor.settings.height, cookieHeight),
			currentHeight = +tinymce.DOM.getStyle(this.editor.editorId + "_ifr", "height").split("px")[0];

		heightVal = (!onInit) ? Math.max(heightVal, editor.getDoc().body.scrollHeight) : heightVal;

		if (currentHeight < heightVal || onInit) {
			tinymce.DOM.setStyle(editor.editorId + "_ifr", "height", heightVal + "px");
		}
	},

	resizeCodeMirror : function (codeMirror) {

		var cmHeight = Math.max((document.documentElement.clientHeight - this.codeModeYreduction), this.editor.codeMirror.getGutterElement().scrollHeight),
			cmWidth = this.containerEl.clientWidth - this.codeModeXreduction,
			currentHeight = +tinymce.DOM.getStyle(this.editor.codeMirror.getScrollerElement(), "height").split("px")[0];

		if (currentHeight < cmHeight) {
			codeMirror.setSize(cmWidth, cmHeight);
		}
	},

	resizeTextView: function (containerEl, rteWidth, elements) {
		var rteSidePadding = 40,
		  	rteMarginLeft = 230,
			rteContainerWidth = +rteWidth + rteSidePadding,
			sectionContainer,
        	fieldContainerWidth = (window.getComputedStyle) ? window.getComputedStyle(containerEl).getPropertyValue("width") :
                              	  (containerEl.currentStyle) ? containerEl.currentStyle.width : null;

		// If the section is collapsed by default, then the RTE's container width will not be calculated
		// correctly and we'll have to get the width from the section container
        if (!fieldContainerWidth || fieldContainerWidth == 'auto') {
        	// We assume the section container is the first ancestor with width value set in pixels
	        sectionContainer = YDom.getAncestorBy(containerEl, function(el) {
	        	var fieldWidth = (window.getComputedStyle) ? window.getComputedStyle(el).getPropertyValue("width") :
                              	  (el.currentStyle) ? el.currentStyle.width : null;
			    if (fieldWidth && fieldWidth != 'auto') {
			        return el;
			    }
			});
			fieldContainerWidth = (window.getComputedStyle) ? window.getComputedStyle(sectionContainer).getPropertyValue("width") :
                              	  	(sectionContainer.currentStyle) ? sectionContainer.currentStyle.width : "0px";
        }

        fieldContainerWidth = +(fieldContainerWidth.split("px")[0]);

        if (typeof fieldContainerWidth == 'number') {
            if (elements['rte-container']) {
            	if (rteContainerWidth < fieldContainerWidth - rteMarginLeft) {
	                YDom.setStyle(elements['rte-container'], "margin-left", rteMarginLeft + "px");
	                YDom.setStyle(elements['rte-container'], "width", rteContainerWidth + "px");
	                if (elements['rte-table']) {
		            	YDom.setStyle(elements['rte-table'], "width", rteWidth + "px");
		            }
	            } else {
	            	YDom.setStyle(elements['rte-container'], "max-width", ((rteContainerWidth > fieldContainerWidth) ? fieldContainerWidth : rteContainerWidth) + "px");	// If the RTEs width exceeds that of its container, then use the container's width instead
	                YDom.setStyle(elements['rte-container'], "width", "100%");
	                rteWidth = "96%";
	                if (elements['rte-table']) {
		            	YDom.setStyle(elements['rte-table'], "width", rteWidth);
		            }
	            }
            }
            return rteWidth;
        }
        return null;
	},

	/**
	 * render
	 */
	_initializeRte: function(config, rteConfig, containerEl) {
		var _thisControl = this,
			rteSidePadding = 40,
			rteMarginLeft = 230,
			rteContainerWidth,
			fieldContainerWidth,
			width,
            _self = this;

		containerEl.id = this.id;
		this.containerEl = containerEl;
		this.fieldConfig = config;
		this.rteConfig = rteConfig;
		
		YDom.addClass(this.containerEl, "rte-inactive");
		YDom.addClass(this.containerEl, "text-mode");
		// we need to make the general layout of a control inherit from common
		// you should be able to override it -- but most of the time it wil be the same
		var titleEl = document.createElement("span");

  		    YDom.addClass(titleEl, 'cstudio-form-field-title');
			titleEl.innerHTML = config.title;
		
		var controlWidgetContainerEl = document.createElement("div");
		YDom.addClass(controlWidgetContainerEl, 'cstudio-form-control-rte-container');

		var validEl = document.createElement("span");
			YDom.addClass(validEl, 'validation-hint');
			YDom.addClass(validEl, 'cstudio-form-control-validation');
			controlWidgetContainerEl.appendChild(validEl);

		/* tiny MCE initializes by class selector - so for now we will init each RTE uniquely
		 * this can be optimized if we give each rte of the same config a specific class, wait until
		 * the enire form is rendered and then init all RTEs which means only 1 init per type
		 */
		var inputEl = document.createElement("textarea");
			controlWidgetContainerEl.appendChild(inputEl);
			YDom.addClass(inputEl, 'datum');

			this.inputEl = inputEl;
			inputEl.value = (this.value == "_not-set") ? config.defaultValue : this.value;
			var rteUniqueInitClass = CStudioAuthoring.Utils.generateUUID();
			YDom.addClass(inputEl, rteUniqueInitClass);	
			YDom.addClass(inputEl, 'cstudio-form-control-input');

		var descriptionEl = document.createElement("span");
			YDom.addClass(descriptionEl, 'description');
			YDom.addClass(descriptionEl, 'cstudio-form-control-rte-description');
			descriptionEl.innerHTML = config.description;

		containerEl.appendChild(titleEl);
		containerEl.appendChild(controlWidgetContainerEl);
		controlWidgetContainerEl.appendChild(descriptionEl);

		YEvent.on(inputEl, 'change', this._onChangeVal, this);
		
		for(var i=0; i<config.properties.length; i++) {
			var prop = config.properties[i];

			switch (prop.name) {
				case "forceImageAlts" :
					this.forceImageAlts = (prop.value && prop.Value == "true") ? true : false;
					break;
				case "imageManager" :
					this.imageManagerName = (prop.value && prop.Value != "") ? prop.value : null;
					break;
				case "width" :
					this.rteWidth = (typeof prop.value == "string" && prop.value) ? prop.value : "400";
                    width = this.resizeTextView(containerEl, this.rteWidth, { "rte-container" : controlWidgetContainerEl});
                    break;
				case "height" : 
					var height = (prop.value === undefined) ? 140 : (Array.isArray(prop.value)) ? 140 : Math.max(+(prop.value), 50);
					if (isNaN(height)) {
						height = 140;
					}
					
					break;
				case "maxlength" :
					inputEl.maxlength = prop.value;
					break;
				case "forcePTags" :
					var forcePTags = (prop.value == "false") ? false : true;
					break;
				case "forceBRTags" :
					var forceBRTags = (prop.value == "true") ? true : false;
					break;
				case "forceRootBlockPTag" : 
					var forceRootBlockPTag = (prop.value == "false") ? false : "p";
					break;
			}
		}

		var pluginList = "paste, noneditable, cs_table, cs_contextmenu, cs_inlinepopups, ";
		for(var l=0; l<rteConfig.rteModules.module.length; l++) {
			// mce plugin names cannot have a - in them
			pluginList += "-"+rteConfig.rteModules.module[l].replace(/-/g,"")+",";
		}

		var toolbarConfig1 = (rteConfig.toolbarItems1 && rteConfig.toolbarItems1.length !=0) ? 
			rteConfig.toolbarItems1 : "bold,italic,|,bullist,numlist";
		
		var toolbarConfig2 = (rteConfig.toolbarItems2 && rteConfig.toolbarItems2.length !=0) ? rteConfig.toolbarItems2 : "";
		var toolbarConfig3 = (rteConfig.toolbarItems3 && rteConfig.toolbarItems3.length !=0) ? rteConfig.toolbarItems3 : "";
		var toolbarConfig4 = (rteConfig.toolbarItems4 && rteConfig.toolbarItems4.length !=0) ? rteConfig.toolbarItems4 : "";
						
		var editor = tinyMCE.init({
	        // General options
	        mode : "textareas",
	        editor_selector : rteUniqueInitClass,
	        theme : "advanced",
	        skin : "cstudio-rte",
	        width : width,
	        height: height,
	        encoding : "xml",
            valid_elements :"+*[*]",
            extended_valid_elements :"+*[*]",
            valid_children : "+*[*]",
			valid_elements :"+*[*]",
	        paste_auto_cleanup_on_paste : true,	        paste_auto_cleanup_on_paste : true,
			relative_urls : false,

			readonly: _thisControl.readonly,
			force_p_newlines: forcePTags,
			force_br_newlines: forceBRTags,
			forced_root_block: forceRootBlockPTag,
			inlinepopups_skin: "cstudio-rte",
			min_height: 74,
			remove_trailing_brs: false,

	        theme_advanced_resizing : true,
	        theme_advanced_resize_horizontal : false,
	        theme_advanced_toolbar_location : "top",
	        theme_advanced_toolbar_align : "left",
	        theme_advanced_statusbar_location : "bottom",
	        
	        theme_advanced_buttons1 : toolbarConfig1,
	        theme_advanced_buttons2 : toolbarConfig2, 
	        theme_advanced_buttons3 : toolbarConfig3,
	        theme_advanced_buttons4 : toolbarConfig4,
	        
	        content_css : "",

	        // Drop lists for link/image/media/template dialogs
	        // template_external_list_url : "js/template_list.js",
	        // external_link_list_url : "js/link_list.js",
	        // external_image_list_url : "js/image_list.js",
	        // media_external_list_url : "js/media_list.js",
			plugins : pluginList,

			setup: function(ed) {

				try {
					ed.contextControl = _thisControl;
					_thisControl.editor = ed;
	
		      		ed.onKeyUp.add(function(ed, e) {
		      			ed.save();
		      			ed.contextControl._onChange(null, ed.contextControl);
		          		// ed.contextControl.updateModel(ed.contextControl.inputEl.value );   Hmm .. do we really want to update the model on every key stroke?
		   			});

	 				ed.onDblClick.add(function(ed, e) {
	 					ed.contextControl._handleElementDoubleClick(ed,e);
	      				});

	 				ed.onClick.add(function(ed, e) {
	 					amplify.publish("/rte/clicked");
	      			});

					ed.onLoadContent.add(function(ed, cm) {
						ed.save();
						var value = ed.contextControl.inputEl.value;
                        if(value != "")//Could be that the model hasn't been loaded yet(Fix the repeat group issue)
                            ed.contextControl.updateModel(value);//Should we really update the model here?
						ed.contextControl._onChange(null, ed.contextControl);				
			        });
			        ed.onChange.add(function(ed, l) {
			        	ed.contextControl.resizeEditor(ed);
                        _self.edited = true;
		            });

		            ed.onInit.add(function(ed) {
				        amplify.publish('/field/init/completed');
				    });

                    ed.onBeforeExecCommand.add(function(ed, cmd, ui, val) {
				    	var ln = ed.selection.getNode();
				    	if (cmd == "unlink" && ln) {
				    		while (ln.nodeName != "A" && ln.parentNode) {
				    			// Look for the link node among the node's ancestors
				    			ln = ln.parentNode;
				    		}
				    		if (ln.nodeName == "A") {
				    			// Remove all class names from the link element so nothing remains of the link element; otherwise, FF
				    			// will create a span element and move all the class names to it
				    			ln.className = "";
				    		}
				    	}
					});
					
	   				ed.onPostRender.add(function(ed, cm) {

	   					ed.contextControl.resizeEditor(ed, true);

	   					// Add counter element
	   					var refEl = YSelector.query("table.mceLayout tbody", _thisControl.containerEl, true),
							theadEl = document.createElement("thead"),
							trEl = document.createElement("tr"),
							tdEl = document.createElement("td"),
							taEl = document.createElement("textarea"),
							ctrlEl = document.createElement("th");

						YDom.addClass(ctrlEl, 'cstudio-form-control-rte-count');
                        ctrlEl.setAttribute("colspan", "3");

						countEl = document.createElement("span");
						YDom.addClass(countEl, 'char-count');
                        ctrlEl.appendChild(countEl);

                        _thisControl.renderHelp(_thisControl.fieldConfig, ctrlEl);

                        trEl.appendChild(ctrlEl);
						theadEl.appendChild(trEl);
						YDom.insertBefore(theadEl, refEl);
						_thisControl.countEl = countEl;

						// Add textarea element for code view
						refEl = YSelector.query("tr.mceLast", refEl, true);
						trEl = document.createElement("tr");
						YDom.addClass(taEl, "code-view");
						tdEl.appendChild(taEl);
						trEl.appendChild(tdEl);
						YDom.insertBefore(trEl, refEl);
						_thisControl.editor.codeTextArea = taEl;

						YEvent.on(inputEl, 'keyup', _thisControl.count, countEl);
						YEvent.on(inputEl, 'keypress', _thisControl.count, countEl);
						YEvent.on(inputEl, 'mouseup', _thisControl.count, countEl);
						
						if (!YAHOO.env.ua.ie || YAHOO.env.ua.ie < 10) {
							// Bind focus event 
							tinymce.dom.Event.add(ed.getWin(), 'focus', function(e) {
								_thisControl.form.setFocusedField(_thisControl);
							}); 
						} else {
							// IE10 fires the 'focus' event on the window every time
							// you click on it; therefore, it becomes impossible for 
							// the RTE to lose focus. To work around this, we'll focus
							// on the RTE only after clicking on its body.
							tinymce.dom.Event.add(ed.getBody(), 'click', function(e) {
								_thisControl.form.setFocusedField(_thisControl);
							});
						}

						ed.contextControl._applyOverrideStyles(ed, rteConfig);

						if(_thisControl.readonly == true) {
							YEvent.on(ed.getBody(), 'click', function() {
								ed.execCommand('mceAutoResize');
							}, ed);
	   					}

		   			});
				}
				catch(err) {
					// log failure
				}
			}
		});

        // Update all content before saving the form (all content is automatically updated on focusOut)
        var callback = {};
        callback.beforeSave = function () {
            _thisControl.focusOut();
        };
        _thisControl.form.registerBeforeSaveCallback(callback);
	},
	
	/**
	 * handle element clicks
	 */
	_handleElementDoubleClick: function(editor, event) {
		var n = event.target;

		if(n.nodeName == "IMG") {
			this._handleImageDoubleClick(editor, event);
		} else if (n.nodeName == "A") {
			editor.execCommand("mceLink");
		}
	},

	/**
	 * handle element clicks
	 */
	_handleImageDoubleClick: function(editor, event) {
		CStudioAuthoring.Module.requireModule(
			"cstudio-forms-controls-rte-edit-image",
			'/static-assets/components/cstudio-forms/controls/rte-plugins/edit-image.js',
			{  },
			{ moduleLoaded: function(moduleName, moduleClass, moduleConfig) {
					moduleClass.renderImageEdit(editor,event.target);
			  }
		});		
	},
	
	/**
	 * apply override styles to the RTE
	 * Override styles override styles from your the site stylesheet
	 */
	_applyOverrideStyles: function(editor, configuration) {
		var styleOverrides = configuration.rteStyleOverride;
		var dom = editor.dom;
		var ss = dom.doc.createElement('style'),
		tt = dom.doc.createTextNode(styleOverrides);

		//First add the currentStyleSheets
		var styleSheets = tinymce.explode(this._getContentCSS(editor) + "," + this._getCurrentStyleSheets());

		tinymce.each(styleSheets, function(u) {
			dom.loadCSS(u);
		});
		
		if(configuration) {
			ss.setAttribute('type', 'text/css');
			//ss.setAttribute('title', channel);
			dom.doc.head.appendChild(ss);
		
			if (ss.styleSheet) {  // IE (6,7,8)
				ss.styleSheet.cssText = styleOverrides;
			} 
			else {
				ss.appendChild(tt);		// all other browsers
			}
		} 
	},

	/**
	 * Load the default theme style sheet
	 */
	_getContentCSS: function(editor){
		var url = tinymce.ThemeManager.urls[editor.settings.theme] || tinymce.documentBaseURL.replace(/\/$/, '');
		return editor.baseURI.toAbsolute(url + "/skins/" + editor.settings.skin + "/content.css");
	},
	
	/**
	 * return the style sheets that should be applied to the RTE given the current context it is in
	 */
	_getCurrentStyleSheets: function(channel) {
		var stylesheets  = "/studio/static-assets/themes/cstudioTheme/css/forms-rte.css";
		var rteConfig = this.rteConfig;
		
		for(var i=0; i<rteConfig.rteStylesheets.link.length; i++) {
			var item = rteConfig.rteStylesheets.link[i];
			if(!item.appliesToChannel 
					|| (!channel && item.appliesToChannel == "default")
					|| (channel && item.appliesToChannel.indexOf(channel) != -1 )) {				
				stylesheets += ", " + item.url;
			}
		}
		
		return stylesheets;
	},
	
	/**
	 * apply style sheets for channel 
	 */
	_applyChannelStyleSheets: function(channel) {
		var stylesheets = this._getCurrentStyleSheets(channel).split(",");

		var head = tinyMCE.activeEditor.dom.doc.getElementsByTagName('head')[0];

		// Clear all elements from head; IE doesn't like head.innerHTML = ''
		CStudioAuthoring.Utils.emptyElement(head);

        var link = document.createElement('link');
        link.setAttribute('rel', 'stylesheet');
        link.setAttribute('type', 'text/css');

		for(var i=0; i<stylesheets.length; i++) {
			var stylesheet = { loadFromPreview: true, url: stylesheets[i] };
			
	        if(stylesheet.loadFromPreview) {
	            // assume relative to preview server
	            link.setAttribute('href', CStudioAuthoringContext.previewAppBaseUri + stylesheet.url);
	        } 
	        else {
	            // assume fully qualified
	            link.setAttribute('href', stylesheet.url);
	        }
	
	        head.appendChild(link);
		}
		
		this._applyOverrideStyles(tinyMCE.activeEditor, this.rteConfig);
	},
	
	/**
	 * load the javascript plugins for the given RTE configuration
	 */
	_loadPlugins: function(plugins, initCallback) {
		// create list of plugins
		this.waitingForPlugins = [];
		
		// init the plugin list
		for(var k=0; k<plugins.length; k++) {
			this.waitingForPlugins[this.waitingForPlugins.length] = plugins[k];
		}		
		
		// define the callback that will fire the RTE init when all plugins are loaded
		var loadedCb = {
			moduleLoaded: function(moduleName, moduleClass, moduleConfig) {
				moduleConfig.context.registeredPlugins[moduleConfig.context.registeredPlugins.length] = moduleClass;
				 
				for(var j=0; j<moduleConfig.context.waitingForPlugins.length; j++) {
					var pluginName = "cstudio-forms-controls-rte-" + moduleConfig.context.waitingForPlugins[j];
					
					if(pluginName == moduleName) {
						moduleConfig.context.waitingForPlugins.splice(j,1);
						break;		
					}
				}
				
				if(moduleConfig.context.waitingForPlugins.length == 0) {
					// init the rte
					initCallback.success();
				}
			}
		};
		
		// load the modules	
		for(var i=0; i<plugins.length; i++) {
			CStudioAuthoring.Module.requireModule(
				"cstudio-forms-controls-rte-"+ plugins[i],
				'/static-assets/components/cstudio-forms/controls/rte-plugins/' + plugins[i] + '.js',
				{ context: this},
				loadedCb);		
		}
	},


	/** 
	 * on change
	 */
	_onChange: function(evt, obj) {
		obj.value = obj.inputEl.value;
		
		if(obj.required) {
			if(obj.inputEl.value == "") {
				obj.setError("required", "Field is Required");
				obj.renderValidation(true, false);
			}
			else {
				obj.clearError("required");
				obj.renderValidation(true, true);
			}
		}
		else {
			obj.renderValidation(false, true);
		}			

		obj.owner.notifyValidation();
		obj.count(evt, obj.countEl, obj.inputEl);
	},

    _onChangeVal: function(evt, obj) {
        obj.edited = true;
        this._onChange(evt, obj);
    },
	
	/**
	 * perform count calculation on keypress
	 * @param evt event
	 * @param el element
	 */
	count: function(evt, countEl, el) {
		// 'this' is the input box
	    el = (el) ? el : this;
	    var text = el.value;
	    
	    var charCount = ((text.length) ? text.length : ((el.textLength) ? el.textLength : 0));
	    var maxlength = (el.maxlength && el.maxlength != '') ? el.maxlength : -1;
	    
	    if(maxlength != -1) {
		    if (charCount > el.maxlength) {
				// truncate if exceeds max chars
				if (charCount > el.maxlength) {
				  this.value = text.substr (0, el.maxlength);
				  charCount = el.maxlength;
			    }
	      
	      		
				if (evt && evt != null
				&& evt.keyCode!=8 && evt.keyCode!=46 && evt.keyCode!=37
				&& evt.keyCode!=38 && evt.keyCode!=39 && evt.keyCode!=40	// arrow keys
				&& evt.keyCode!=88 && evt.keyCode !=86) {					// allow backspace and
																			// delete key and arrow keys (37-40)
																			// 86 -ctrl-v, 90-ctrl-z,
	          		if(evt)
	          			YEvent.stopEvent(evt);
	       		}
			}
	    }
	    
        if (maxlength != -1) {
        	countEl.innerHTML = charCount + ' / ' + el.maxlength;
        } 
        else {
        	countEl.innerHTML = charCount;
        }
    },
    
    /**
     * call this instead of calling editor.save()
     */
    save: function() {
		this.editor.save();	
		this._onChange(null, this);
		this.updateModel(this.inputEl.value);
    }
});


YEvent.delegate("formContainer", "click", function(e, matchedEl) {

		var rteRoot = YDom.getAncestorBy(matchedEl, function(el) {
		    if (el.nodeName == "TABLE" && YDom.hasClass(el, "mceLayout")) {
		        return el;
		    }
		});
		if (!rteRoot) {
			// Clicked outside of an RTE
			// Focus out of the RTE that's currently showing
			tinymce.activeEditor.contextControl.form.setFocusedField(null);
		}
	}, "div, span, td");


/* -------------------------------------------- */
/* --- Patch tinymce's ColorSplitButton methods --- */
/* Original File : <studio-root>/src/main/webapp/modules/editors/tinymce/tiny_mce.js
/* -------------------------------------------- */
tinymce.ui.ColorSplitButton.prototype.renderMenu = function() {
	var d = tinymce, c = d.DOM, a = d.dom.Event, b = d.is, e = d.each;

    var p = this, h, k = 0, q = p.settings, g, j, l, o, f;
    o = c.add(document.getElementById(tinymce.activeEditor.editorId + "_toolbargroup"), "div", {role: "listbox",id: p.id + "_menu","class": q.menu_class + " " + q["class"],style: "position:absolute;left:0;top:-1000px;"});
    h = c.add(o, "div", {"class": q["class"] + " mceSplitButtonMenu"});
    c.add(h, "span", {"class": "mceMenuLine"});
    g = c.add(h, "table", {role: "presentation","class": "mceColorSplitMenu"});
    j = c.add(g, "tbody");
    k = 0;
    e(b(q.colors, "array") ? q.colors : q.colors.split(","), function(i) {
        i = i.replace(/^#/, "");
        if (!k--) {
            l = c.add(j, "tr");
            k = q.grid_width - 1
        }
        g = c.add(l, "td");
        g = c.add(g, "a", {role: "option",href: "javascript:;",style: {backgroundColor: "#" + i},title: p.editor.getLang("colors." + i, i),"data-mce-color": "#" + i});
        if (p.editor.forcedHighContrastMode) {
            g = c.add(g, "canvas", {width: 16,height: 16,"aria-hidden": "true"});
            if (g.getContext && (f = g.getContext("2d"))) {
                f.fillStyle = "#" + i;
                f.fillRect(0, 0, 16, 16)
            } else {
                c.remove(g)
            }
        }
    });
    if (q.more_colors_func) {
        g = c.add(j, "tr");
        g = c.add(g, "td", {colspan: q.grid_width,"class": "mceMoreColors"});
        g = c.add(g, "a", {role: "option",id: p.id + "_more",href: "javascript:;",onclick: "return false;","class": "mceMoreColors"}, q.more_colors_title);
        a.add(g, "click", function(i) {
            q.more_colors_func.call(q.more_colors_scope || this);
            return a.cancel(i)
        })
    }
    c.addClass(h, "mceColorSplitMenu");
    new d.ui.KeyboardNavigation({root: p.id + "_menu",items: c.select("a", p.id + "_menu"),onCancel: function() {
            p.hideMenu();
            p.focus()
        }});
    a.add(p.id + "_menu", "mousedown", function(i) {
        return a.cancel(i)
    });
    a.add(p.id + "_menu", "click", function(i) {
        var m;
        i = c.getParent(i.target, "a", j);
        if (i && i.nodeName.toLowerCase() == "a" && (m = i.getAttribute("data-mce-color"))) {
            p.setColor(m)
        }
        return a.cancel(i)
    });
    return o
};

tinymce.ui.ColorSplitButton.prototype.showMenu = function() {
	var d = tinymce, c = d.DOM, a = d.dom.Event;

    var f = this, g, j, i, h;
    if (f.isDisabled()) {
        return
    }
    if (!f.isMenuRendered) {
        f.renderMenu();
        f.isMenuRendered = true
    }
    if (f.isMenuVisible) {
        return f.hideMenu()
    }
    i = c.get(f.id);
    c.show(f.id + "_menu");
    c.addClass(i, "mceSplitButtonSelected");
    h = c.getPos(i);
    c.setStyles(f.id + "_menu", {left: h.x - 1,top: (h.y - window.pageYOffset) + i.clientHeight,zIndex: 200000});
    i = 0;
    a.add(c.doc, "mousedown", f.hideMenu, f);
    f.onShowMenu.dispatch(f);
    if (f._focused) {
        f._keyHandler = a.add(f.id + "_menu", "keydown", function(k) {
            if (k.keyCode == 27) {
                f.hideMenu()
            }
        });
        c.select("a", f.id + "_menu")[0].focus()
    }
    f.isMenuVisible = 1
}

/* --------------------------------------- */
/* --- Patch tinymce's ListBox methods --- */
/* Original File : <studio-root>/src/main/webapp/modules/editors/tinymce/tiny_mce.js
/* --------------------------------------- */
tinymce.ui.ListBox.prototype.renderMenu =  function() {
	var e = tinymce.each,
		c = tinymce.DOM;

    var g = this, f;
    f = g.settings.control_manager.createDropMenu(g.id + "_menu", {menu_line: 1, "container": tinymce.activeEditor.editorId + "_toolbargroup" , "class": g.classPrefix + "Menu mceNoIcons",max_width: 160,max_height: 160});
    f.onHideMenu.add(function() {
        g.hideMenu();
        g.focus()
    });
    f.add({title: g.settings.title,"class": "mceMenuItemTitle",onclick: function() {
            if (g.settings.onselect("") !== false) {
                g.select("")
            }
        }});
    e(g.items, function(h) {
        if (h.value === undefined) {
            f.add({title: h.title,"class": "mceMenuItemTitle",onclick: function() {
                    if (g.settings.onselect("") !== false) {
                        g.select("")
                    }
                }})
        } else {
            h.id = c.uniqueId();
            h.onclick = function() {
                if (g.settings.onselect(h.value) !== false) {
                    g.select(h.value)
                }
            };
            f.add(h)
        }
    });
    g.onRenderMenu.dispatch(g, f);
    g.menu = f
};

tinymce.ui.ListBox.prototype.showMenu = function() {
	var d = tinymce,
		b = d.dom.Event,
		c = d.DOM,
		e = d.each;

    var g = this, i, h = c.get(this.id), f;
    if (g.isDisabled() || g.items.length == 0) {
        return
    }
    if (g.menu && g.menu.isMenuVisible) {
        return g.hideMenu()
    }
    if (!g.isMenuRendered) {
        g.renderMenu();
        g.isMenuRendered = true
    }
    i = c.getPos(h);
    f = g.menu;
    f.settings.offset_x = i.x - 1;
    f.settings.offset_y = i.y - window.pageYOffset;
    f.settings.keyboard_focus = !d.isOpera;
    if (g.oldID) {
        f.items[g.oldID].setSelected(0)
    }
    e(g.items, function(j) {
        if (j.value === g.selectedValue) {
            f.items[j.id].setSelected(1);
            g.oldID = j.id
        }
    });
    f.showMenu(0, h.clientHeight);
    b.add(c.doc, "mousedown", g.hideMenu, g);
    c.addClass(g.id, g.classPrefix + "Selected")
};

/* ------------------------------------------ */
/* --- Patch tinymce's MenuButton methods --- */
/* Original File : <studio-root>/src/main/webapp/modules/editors/tinymce/tiny_mce.js
/* ------------------------------------------ */
tinymce.ui.MenuButton.prototype.renderMenu = function() {
    var f = this, e;
    e = f.settings.control_manager.createDropMenu(f.id + "_menu", {menu_line: 1, "container": tinymce.activeEditor.editorId + "_toolbargroup", "class": this.classPrefix + "Menu",icons: f.settings.icons});
    e.onHideMenu.add(function() {
        f.hideMenu();
        f.focus()
    });
    f.onRenderMenu.dispatch(f, e);
    f.menu = e
};

tinymce.ui.MenuButton.prototype.showMenu = function() {
	var c = tinymce,
		a = c.dom.Event,
		b = c.DOM,
		d = c.each;

    var g = this, j, i, h = b.get(g.id), f;
    if (g.isDisabled()) {
        return
    }
    if (!g.isMenuRendered) {
        g.renderMenu();
        g.isMenuRendered = true
    }
    if (g.isMenuVisible) {
        return g.hideMenu()
    }
    j = b.getPos(g.settings.menu_container);
    i = b.getPos(h);
    f = g.menu;
    f.settings.offset_x = i.x - 1;
    f.settings.offset_y = i.y - window.pageYOffset + 2;
    f.settings.vp_offset_x = i.x;
    f.settings.vp_offset_y = i.y;
    f.settings.keyboard_focus = g._focused;
    f.showMenu(0, h.clientHeight);
    a.add(b.doc, "mousedown", g.hideMenu, g);
    g.setState("Selected", 1);
    g.isMenuVisible = 1
};

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-rte", CStudioForms.Controls.RTE);

}} );