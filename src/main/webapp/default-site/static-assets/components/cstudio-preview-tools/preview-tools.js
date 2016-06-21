(function () {

    var STUDIO_BAR_HEIGHT = 50;

    /**
     * editor tools
     */
    CStudioAuthoring.PreviewTools = CStudioAuthoring.PreviewTools || {

            initialized: false,
            panel: null,
            firstRender: true,
            PreviewToolsOffEvent: new YAHOO.util.CustomEvent("cstudio-preview-tools-off", CStudioAuthoring),
            PreviewToolsOnEvent: new YAHOO.util.CustomEvent("cstudio-preview-tools-on", CStudioAuthoring),

            /**
             * initialize module
             */
            initialize: function (config) {

                var panelEl, that, ptoOn, ptoLeft, ptoTop;

                if (!this.initialized) {

                    that = this;
                    panelEl = document.createElement("div");
                    panelEl.id = "preview-tools-panel-container";
                    ptoOn = !!(sessionStorage.getItem('pto-on'));   // cast string value to a boolean

                    if (!ptoOn) {
                        // There are no preferences set yet. Store default values.
                        ptoLeft = window.innerWidth - 300;
                        ptoTop = 100;
                        sessionStorage.setItem('pto-on', "");               // empty string value so that when we cast it to boolean we get false
                        sessionStorage.setItem('pto-left', ptoLeft);
                        sessionStorage.setItem('pto-top', ptoTop);
                    } else {
                        ptoLeft = +(sessionStorage.getItem('pto-left'));    // cast string value to a number
                        ptoTop = +(sessionStorage.getItem('pto-top'));      // cast string value to a number
                    }

                    if (ptoTop < STUDIO_BAR_HEIGHT) ptoTop = STUDIO_BAR_HEIGHT;

                    document.body.appendChild(panelEl);

                    var panel = new YAHOO.widget.Panel("preview-tools-panel-container", {
                        width: "265px",
                        close: false,
                        constraintoviewport: false,
                        draggable: false,
                        modal: false,
                        visible: false,
                        autofillheight: null
                    });

                    panel.moveEvent.subscribe(function (e, args) {
                        that.updateLocationPrefs.call(that);
                        if (args[0][1] < STUDIO_BAR_HEIGHT) {
                            setTimeout(function () {
                                panel.moveTo(args[0][0], STUDIO_BAR_HEIGHT);
                            });
                        }
                    });

                    YAHOO.widget.Overlay.windowResizeEvent.subscribe(function () {
                        CStudioAuthoring.PreviewTools.panelCheckBounds.call(CStudioAuthoring.PreviewTools);
                    });

                    panel.setHeader('<span data-translation="previewToolsTitle">Preview Tools</span>');
                    panel.render();

                    CStudioAuthoring.Service.lookupConfigurtion(CStudioAuthoringContext.site, "/preview-tools/panel.xml", {
                        success: function (config) {
                            this.context.buildModules(config);

                            if (ptoOn) {
                                this.context.turnToolsOn();
                            } else {
                                this.context.turnToolsOff();
                            }
                        },
                        failure: CStudioAuthoring.Utils.noop,
                        context: this
                    });

                    this.panel = panel;
                    this.initialized = true;
                }
            },

            turnToolsOn: function () {

                this.panelCheckBounds();
                this.panel.show();
                sessionStorage.setItem('pto-on', "on");

                var container = document.getElementById("container");
                if(container) {
                    container.style.display="block";
                }

                this.PreviewToolsOnEvent.fire();
            },

            turnToolsOff: function () {

                this.panel.hide();
                sessionStorage.setItem('pto-on', "");  // empty string value so that when we cast it to boolean we get false+

                var reportContainerEl = document.getElementById("cstudioPreviewAnalyticsOverlay");
                var container = document.getElementById("container");
                if(reportContainerEl) {
                    document.body.removeChild(reportContainerEl);
                }
                if(container) {
                    container.style.display="none";
                }

                this.PreviewToolsOffEvent.fire();
            },

            /*
             * Update the panel's location preferences based on the panel's current coordinates.
             */
            updateLocationPrefs: function () {
                var panelXYvalues = this.panel.cfg.config.xy.value;
                sessionStorage.setItem('pto-left', panelXYvalues[0]);
                sessionStorage.setItem('pto-top', panelXYvalues[1] < STUDIO_BAR_HEIGHT ? STUDIO_BAR_HEIGHT : panelXYvalues[1]);
            },

            /*
             * Keep the panel within the horizontal limits of the window.
             * This method will be called, for example, when the window is resized.
             */
            panelCheckBounds: function () {
                var offsetX, panelWidth, rightPadding, panelX, panel, ptoTop, winWidth;

                panel = this.panel;
                offsetX = panel.cfg.config.x.value > 50 ? 50 : panel.cfg.config.x.value;
                panelWidth = +(panel.cfg.config.width.value.split("px")[0]);
                rightPadding = 20;
                winWidth = window.innerWidth;

                panelX = offsetX + panelWidth + rightPadding;

                // Update the panel's position since it's starting to be outside the window
                if (panelX > winWidth) {
                    ptoTop = +(sessionStorage.getItem('pto-top'));
                    offsetX = (winWidth - panelWidth) - rightPadding;
                    panel.moveTo(offsetX, ptoTop);
                }
            },

            /**
             * given a dropdown configuration, build the nav
             */
            buildModules: function (navConfig) {

                var containerEl = document.getElementById("preview-tools-panel-container");
                containerEl.style.height = "auto";

                if (navConfig.modules.module) {
                    navConfig.modules = [navConfig.modules.module];
                }

                if (navConfig.modules.length) {
                    var containersEls = [];

                    for (var j = 0; j < navConfig.modules[0].length; j++) {
                        var moduleContainerEl = document.createElement("div");
                        containerEl.appendChild(moduleContainerEl);
                        containersEls[j] = moduleContainerEl;
                    }

                    CStudioAuthoring.Module.requireModule('ice-tools',
                        '/static-assets/components/cstudio-preview-tools/ice-tools.js');

                    for (var i = 0; i < navConfig.modules[0].length; i++) {
                        var module = navConfig.modules[0][i];
                        CStudioAuthoring.Module.requireModule(module.moduleName,
                            '/static-assets/components/cstudio-preview-tools/mods/' + module.moduleName + ".js",
                            {config: module}, {
                                context: this,
                                containerEl: containersEls[i],
                                moduleLoaded: function (moduleName, moduleClass, moduleConfig) {
                                    this.context.buildModule(this.containerEl, moduleClass, moduleConfig);
                                    moduleClass.initialize(moduleConfig);
                                }
                            }
                        );
                    }

                }
            },

            buildModule: function (containerEl, moduleClass, moduleConfig) {
                var moduleEl = document.createElement("div"),
                    headerEl = document.createElement("div"),
                    toggleEl = document.createElement("a"),
                    panelEl = document.createElement("div"),
                    previewLangBundle = CMgs.getBundle("previewTools", CStudioAuthoringContext.lang);

                var toggleFn = function (e) {
                    YEvent.preventDefault(e);

                    if (YDom.hasClass(moduleEl, 'contracted')) {
                        YDom.replaceClass(moduleEl, 'contracted', 'expanded');

                        if (!panelEl._csExpanded) {
                            // only call this once
                            panelEl._csExpanded = true;
                            if (moduleClass.firstExpand) {
                                moduleClass.firstExpand(panelEl, moduleConfig.config);
                            }
                        }

                        if (moduleClass.expand) {
                            moduleClass.expand(panelEl, moduleConfig.config);
                        }

                    } else {
                        YDom.replaceClass(moduleEl, 'expanded', 'contracted');
                        if (moduleClass.collapse) {
                            moduleClass.collapse(panelEl, moduleConfig.config);
                        }
                    }
                };

                moduleClass.toggleFn = toggleFn;

                // create the header for a module
                YDom.addClass(moduleEl, "contracted");
                YDom.addClass(headerEl, "acn-accordion-header");
                YDom.addClass(toggleEl, "acn-accordion-toggle");
                YDom.addClass(panelEl, "acn-accordion-panel");

                toggleEl.href = "#";
                toggleEl.innerHTML = '<span data-translation="'+moduleConfig.config.title+'">'+moduleConfig.config.title+'</span>';
                moduleEl.id = moduleConfig.config.moduleName + '-elem';
                headerEl.appendChild(toggleEl);

                containerEl.appendChild(moduleEl);
                moduleEl.appendChild(headerEl);
                moduleEl.appendChild(panelEl);

                toggleEl.onclick = toggleFn;
                moduleClass.render(panelEl, moduleConfig.config);
            }
        };

    CStudioAuthoring.Module.moduleLoaded("preview-tools-controller", CStudioAuthoring.PreviewTools);

}) ();