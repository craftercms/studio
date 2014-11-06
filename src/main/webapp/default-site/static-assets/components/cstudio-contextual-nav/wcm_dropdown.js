var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;

/**
 * WCM Site Dropdown Plugin
 */
CStudioAuthoring.ContextualNav.WcmDropDown = CStudioAuthoring.ContextualNav.WcmDropDown || {

	initialized: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		if(!CStudioAuthoring.ContextualNav.WcmSiteDropdown) {
			initialized = true;
	        this.renderDropdown();
    	    CStudioAuthoring.ContextualNav.WcmSiteDropdown.init();
		}
	},
	
	renderDropdown:function(){
	    // Local Shortcuts
	    var YDom = YAHOO.util.Dom,
	        YEvent = YAHOO.util.Event,
	        auth = CStudioAuthoring,
	        utils = auth.Utils,
			strUtils = auth.StringUtils,
	        storage = auth.Storage;

			var CMgs = CStudioAuthoring.Messages;
			var contextNavLangBundle = CMgs.getBundle("contextnav", CStudioAuthoringContext.lang);
			var mainContainerEl = YDom.get('acn-dropdown-wrapper');

			mainContainerEl.innerHTML = 
	          '<div id="acn-dropdown" class="acn-dropdown">' +
    			'<div id="acn-dropdown-inner" class="acn-dropdown-inner">' +
      				'<a id="acn-dropdown-toggler" href="#" class="acn-dropdown-toggler acn-drop-arrow">' +
      				CMgs.format(contextNavLangBundle, "siteContent")+
      				'</a>' +
    				'</div>' +
    			'<div id="acn-dropdown-menu-wrapper" style="display:none" class="acn-dropdown-menu-wrapper unselectable">' +
      				'<div id="acn-resize" class="acn-resize">' +
              			'<div class="acn-data">' +
          					'<div id="acn-context-menu" class="acn-context-menu"></div>' +
          					'<div id="acn-context-tooltip" class="acn-context-tooltip"></div>' +
          					'<div id="acn-dropdown-menu" style="height:100%" class="acn-dropdown-menu">' +
            					'<div id="acn-dropdown-menu-inner" class="acn-dropdown-menu-inner unselectable"></div>' +
            					'<div id="acn-dropdown-footer" class="acn-dropdown-footer"></div>' +
         					'</div>' +
        				'</div>' +
      				'</div>' +
			    '</div>' +
  			'</div>';  

	    /**
	     * WCM Site Dropdown Contextual Nav Widget
	     */
	    auth.register({
	        "ContextualNav.WcmSiteDropdown": {
	            /**
	             * Static Members
	             * In the Object oriented implementation this should members should be static
	             */
	            instanceCount: 0, // Instance Counter
	            STORED_CONFIG_KEY_TEMPLATE: "wcm-site-dropdown-prefs-{0}",
	            INSTANCE_ID_TEMPLATE: "wcm-site-dropdown-{0}",
	            /**
	             * Provides a unique instance Id to assign to new component instances
	             * @return {String} A unique instance Id
	             */
	            getNextInstanceId: function() {
	                var np = auth.ContextualNav.WcmSiteDropdown;
	                return (strUtils.format(
						np.INSTANCE_ID_TEMPLATE,
						np.instanceCount++));
	            },
	            /**
	             * Closes the dropdown when the user clicks outside it
	             * @param {MouseEvent} evt The DOM event this method listens to
	             */
	            windowClickCloseFn: function(evt) {
	                /* For some reason YUI's context menu needs the click event propagation
	                 * further than the top parent element, so here: read the property set to
	                ** the event in the dropdown container to see if the click was inside it */
	                if(!evt.insidedropdown) {
						/* On windows firefox when right clicking inside the dropdown the dropdown
						 * wrapper click fn is not triggered, so check & see if the click was inside
						 * the dropdown */
						var parent = YDom.get('acn-dropdown-wrapper'),
							node = evt.target;
						while (node != null && parent != node && node.id != "cstudio-wcm-popup-div")
							node = node.parentNode;
	
						/* when we click on copy/change template pop ups
						 * context nav should be in open state	*/
						if (node != null && node.id == "cstudio-wcm-popup-div") {
							return;
						}
						(parent != node) && this.setVisible(false);
					}else if(evt.insidedropdown) {
						/**
						 * canned searches are under drop-down menu, but when canned searches are
						 * clicked drop-down need to be closed.
						 */
						if( evt.target.className == "canned-search-el" ) {
							this.setVisible(false);
						}
					}
	            },
	            dropdownWrapperClickFn: function(evt) {
	                evt.insidedropdown = true;
	                // Stopping the event even at this point caused
	                // the contextual menu actions to stop being triggered
	                // YEvent.stopPropagation(evt);
	            },
	            /* * * * * * * * * * * * *
	             * Instance Members
	             * * * * * * * * * * * */
	            handleScroll: false,
	            instanceId: null,
	            oConfig: {
	                persistState: true,
	                role: "default",
	                minHeight: 84,
	                maxHeight: 600,
	                minWidth: 225,
	                maxWidth: 1000
	            },
	            oPreferences: {
	                height: '180px',
	                width: '225px',
	                visible: false,
	                scrollX:0,
	                scrollY:0,
	                toString: function(){
	                    return YAHOO.lang.JSON.stringify(this);
	                }
	            },
	            // list of modules active / loaded
	            activeModules: [],
	            /**
	             * Widget Constructor
	             * @param {Object} oConfig Configuration items to override the default ones
	             * @param {Object} oPreferences Preferences to override the default & stored ones
	             * @see oConfig
	             * @see oPreferences
	             * @return {CStudioAuthoring.ContextualNav.WcmSiteDropdown} The instance of the object
	             */
	            init: function(oConfig, oPreferences) {
	                this.instanceId = auth.ContextualNav.WcmSiteDropdown.getNextInstanceId();
	                this.initializeConfig(oConfig);
	                this.initializePreferences(oPreferences);
	                this.initializeResizing();
	                this.initializeVisibility();
	                /**
	                 * when search text box is focused contex nav need to be hidden.
	                 */
	                var e = YDom.get("acn-searchtext");
	                YAHOO.util.Event.addListener(e, "focus", function(evt){
	                	auth.ContextualNav.WcmSiteDropdown.setVisible(false);
	                });
	                var cfg = this.oPreferences,
	                	self = this;
	                YEvent.onAvailable("acn-dropdown-menu", function() {
	                    YEvent.addListener("acn-dropdown-menu", "scroll", function(){
	                        cfg.scrollY = this.scrollTop;
	                        cfg.scrollX = this.scrollLeft;
	                        self.save();
	                    });
	                });
	                CStudioAuthoring.Service.retrieveSiteDropdownConfiguration("default", {
	                	success: function(config) {
							this.context.buildModules(config);
	                		this.context.save();
	                	},
	
	                	failure: function() {
	                	},
	
	                	context: this
	                });
	
	                return this;
	            },
	            /**
	             * Initializes the widget's configuration
	             * @param {Object} oConfig Set of values to override the defaults
	             */
	            initializeConfig: function(oConfig) {
	                oConfig && YAHOO.lang.augmentObject(this.oConfig, oConfig, true);
	                return this;
	            },
	            /**
	             * Intializes the widget's user preferences
	             * @param {String} oPreferences Set of values to override the defaults and stored
	             */
	            initializePreferences: function(oPreferences){
	                var storedstr = storage.retrieve( this.getStoredCfgKey() ),
	                    oStored = storedstr && storedstr !== ""
							? utils.decode(storedstr)
							: null;
	                oStored && YAHOO.lang.augmentObject(this.oPreferences, oStored, true);
	                oPreferences && YAHOO.lang.augmentObject(this.oPreferences, oPreferences, true);
	                return this;
	            },
	            /**
	             *
	             */
	            getStoredCfgKey: function(){
	                return strUtils.format(auth.ContextualNav.WcmSiteDropdown.STORED_CONFIG_KEY_TEMPLATE, this.oConfig.role);
	            },
	            initializeVisibility: function(){
	                // Enable the link element to open & close the dropdown
	                YEvent.on('acn-dropdown-toggler', 'click', function(evt){
	                    YEvent.preventDefault(evt);
	                    this.toggleDropdown();
	                }, null, this);
	                YEvent.on('acn-dropdown-wrapper', 'click', auth.ContextualNav.WcmSiteDropdown.dropdownWrapperClickFn);
	                if (this.oPreferences.visible) {
	                    // Set config visibility to false so that
	                    // setVisible method wont bypass the call
	                    this.oPreferences.visible = false;
	                    this.setVisible(true);
	                }
	                
	                this.setVisible(CStudioAuthoringContext.openSiteDropdown);
	                
	                return this;
	            },
	            initializeResizing: function(){
	                var cookie_dropdown_heightWidth = "wcm_site_dropdown_heightWidth",
	                    dom = YAHOO.util.Dom,
	                    $ = YAHOO.util.Selector.query,
	                    resizeDataAreaFn = function() {
	                        var bd = dom.get('acn-dropdown-menu'),
	                                wrpEl = dom.get('acn-dropdown-menu-wrapper'),
	                                wrpHeight = 0,
	                                wrpIsHidden = (wrpEl.style.display == 'none'),
	                                botBarH = parseInt(dom.getStyle($('.yui-resize-handle-b', 'acn-resize', true), 'height')),
	                                bdPadTop = parseInt(dom.getStyle(bd, 'padding-top')),
	                                bdPadBot = parseInt(dom.getStyle(bd, 'padding-bottom'));
	                        wrpIsHidden && (wrpEl.style.display = 'block');
	                        wrpHeight = Math.max(0, Math.round(wrpEl.offsetHeight));
	                        wrpIsHidden && (wrpEl.style.display = 'none');
	                        dom.setStyle(bd, 'height', (wrpHeight - botBarH - bdPadTop - bdPadBot) + 'px');
	                    };
	                // make dropdown resizable
	                var resize = new YAHOO.util.Resize('acn-resize', {
	                    height: this.oPreferences.height,
	                    width: this.oPreferences.width,
	                    minHeight: this.oConfig.minHeight,
	                    minWidth: this.oConfig.minWidth,
	                    maxHeight: this.oConfig.maxHeight,
	                    maxWidth: this.oConfig.maxWidth
	                }, this);
	                var self = this;
	                resize.on('endResize', function(args) {
	                    resizeDataAreaFn();
	                    self.oPreferences.width = args.width + "px";
	                    self.oPreferences.height = args.height + "px";
	                    self.save();
	                }, resize, true);
	                resizeDataAreaFn();
	                return this;
	            },
	            save: function(){
	                this.oConfig.persistState && storage.write(
	                    this.getStoredCfgKey(),
	                    this.oPreferences.toString(),
	                    360);
	                return this;
	            },
	            setVisible: function(visible) {
	                var cfg = this.oPreferences,
	                    setStyle = YDom.setStyle;
	                if (cfg.visible != visible) {
	                    !visible && this.updateScrollPosition(visible);
	                    setStyle("acn-dropdown-menu-wrapper", "display", visible ? "block" : "none");
	                    setStyle("acn-dropdown-wrapper", "background-color", visible ? "#f0f0f0" : "");
	                    setStyle("acn-dropdown-wrapper", "padding-bottom", visible ? "2px" : "");
	                    visible && this.updateScrollPosition(visible);
	                    cfg.visible = visible;
	                    YEvent[visible ? "addListener" : "removeListener"](window, 'click', auth.ContextualNav.WcmSiteDropdown.windowClickCloseFn, null, this);
	                    this.save();
	                }
	                return this;
	            },
	            /**
	             * toggle visibility on nav element
	             * state can be OPEN, CLOSED, TOGGLE
	             */
	            toggleDropdown: function() {
	                return this.setVisible(!this.oPreferences.visible);
	            },
	            updateScrollPosition: function(visible) {
	                if (this.handleScroll) {
	                    var e = YDom.get("acn-dropdown-menu"),
	                        cfg = this.oPreferences;
	                    if (visible) {
	                        e.scrollTop = cfg.scrollY;
	                        e.scrollLeft = cfg.scrollX;
	                    } else {
	                        cfg.scrollY = e.scrollTop;
	                        cfg.scrollX = e.scrollLeft;
	                    }
	                }
	                return this;
	            },
	
	            /**
	             * given a dropdown configuration, build the dropdown
	             */
	            buildModules: function(dropdownConfig) {
	                var groups = dropdownConfig.groups,
	                    j, k, a, b, c, menuItems, modules;
	
					if(!groups.length) {
						groups = new Array();
						groups[0] = dropdownConfig.groups.group;
					}
	
	                for (var i = 0, a = groups.length; i < a; i++) {
	
	                    menuItems = groups[i].menuItems;
	
	                    if(!menuItems.length) {
						    menuItems = new Array();
							menuItems[0] = groups[i].menuItems.menuItem;
						}
	
	                    for (j = 0, b = menuItems.length; j < b; j++) {
	                        modules = menuItems[j].modulehooks;
	
		                    if(!modules.length) {
								modules = new Array();
								modules[0] = menuItems[j].modulehooks.moduleHook;
							}
	
                            CStudioAuthoring.Service.lookupAuthoringRole(CStudioAuthoringContext.site, CStudioAuthoringContext.user, {
                                success: function(userRoles) {
    	                            for (k = 0, c = modules.length; k < c; k++)
	                                    this.initDropdownModule( userRoles, modules[k] );
                                },
                                failure: function() {
                                },
                                initDropdownModule: this.initDropdownModule
                            });
	                    }
	                }
	            },

	            /**
	             * initialize a dropdown module
	             */
	            initDropdownModule: function(userRoles, module) {
	                var allowed = false;
					if(!module.params || !module.params.roles) {
					    allowed = true;
					} else {
                        var roles = (module.params.roles.length) ? module.params.roles : [module.params.roles.role];
                        if (roles.length == 0 || roles[0] == undefined) {
                            allowed = true;
                        }
                        else {
                            var allowed = false;
                            var userRoles = userRoles.roles;
                            for(var j=0; j < userRoles.length; j++) {
                                var userRole = userRoles[j];

                                for(var i=0; i < roles.length; i++) {
                                    var role = roles[i];

                                    if(userRole == role) {
                                        allowed = true;
                                        break;
                                    }
                                }
                            }
                        }
					}
					if (allowed) {
                        var dropdownInnerEl = YDom.get("acn-dropdown-menu-inner");
                        var moduleContainerEl = document.createElement("div");
                        if(module.showDivider && module.showDivider == "true") {
                            YDom.addClass(moduleContainerEl, "acn-parent");
                        }

                        // THIS CODE ABOVE will be removed when we make the entire nav aware of the users roles and centralize the permissions

                        dropdownInnerEl.appendChild(moduleContainerEl);

                        module.containerEl = moduleContainerEl;

                        var self = this,
                            cb = {
                                moduleLoaded: function(moduleName, moduleClass, moduleConfig) {
                                    try {
                                        moduleClass.initialize(moduleConfig);
                                    } catch (e) {
                                        // in preview, this function undefined raises error -- unlike dashboard.
                                        // I agree, not a good solution!
                                    }
                                }
                            };
                             (module.name == "wcm-root-folder") && (cb.once = function(){
                            try {
                                CStudioAuthoring.ContextualNav.WcmRootFolder.treePathOpenedEvt.subscribe(function(evtType, aArgs){
                                    if (aArgs[0] == aArgs[1]) {
                                        // number of instaces == number of times event has fired
                                        self.handleScroll = true;
                                        self.oPreferences.visible && self.updateScrollPosition(true);
                                    }
                                });
                            } catch(ex) {}
                        });
                        CStudioAuthoring.Module.requireModule(
                            module.name,
                            '/static-assets/components/cstudio-contextual-nav/wcm-site-dropdown-mods/' + module.name + ".js",
                            module,
                            cb
                        );
					}
	            },

                refreshDropdown: function () {
                        // Get the dropdown wrapper
                    var container = document.getElementById("acn-dropdown-menu-inner"),
                        // Get all the direct decendants of the wrapper
                        elems = YAHOO.util.Selector.query("> div", container),
                        // Find the parent node of the site selector select
                        siteSelectorParent = document.getElementById("acn-site-dropdown").parentNode,
                        l = elems.length - 1;
                    // Remove all but the site selector parent div
                    while (l){
                        if (elems[l] !== siteSelectorParent) {
                            container.removeChild(elems[l]);
                        }
                        l--;
                    }
                    // Re-initialise the dropdown (refresh)
                    CStudioAuthoring.ContextualNav.WcmSiteDropdown.init();
                }

	        }
	    });
	    CStudioAuthoring.Events.widgetScriptLoaded.fire("wcm-site-dropdown");
	}
}

CStudioAuthoring.Module.moduleLoaded("wcm_dropdown", CStudioAuthoring.ContextualNav.WcmDropDown);
