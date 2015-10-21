/**
 *  Common API
 *  Utilities: General purpose functions
 *  Constants: General purpose Constants
 *  RequiredResources: part of the bootstrap, loads base required scripts/css
 *  SelectedContent: so much depends on contexual content, this object maintains that state
 *  Module: module system
 *  Events: All the events in the sysyem
 *  Operations: Reusable UI / Application level operations.  Call these not the services
 *  Service: All REST activity encapsulated within
 */
/** Shortcuts to YAHOO libraries **/
var YDom = YAHOO.util.Dom;
var YConnect = YAHOO.util.Connect;
var JSON = YAHOO.lang.JSON;
var YEvent = YAHOO.util.Event;

/* Removing this check because,
 * 401 error is returning some other cases apart from Authentication failed case.
 YConnect.failureEvent.subscribe(function() {
 if (arguments[1] && arguments[1].length == 1 && arguments[1][0].status == 401) {
 alert ("Authentication failed, redirecting to login page.");
 window.location.reload(true);
 }
 });
 */

(function(undefined){

    // Private functions
    var encodePathToNumbers = function(path) {
        var re1 = new RegExp('/', 'g');

        var res = path.replace(re1, '00');    // substitute all forward slash with '00'
        res = res.replace(/-/g, '111');     // substitute all dashes with '111'
        res = res.replace(/\./g, '010');      // substitute all periods with '010'
        return res;
    };

    var decodeNumbersToPath = function(pathWithNumbers) {
        var res = pathWithNumbers.replace(/00/g, '/');
        res = res.replace(/111/g, '-');
        res = res.replace(/010/g, '.');
        return res;
    };

    /**
     * authoring object
     */
    if (typeof CStudioAuthoring == "undefined" || !CStudioAuthoring) CStudioAuthoring = {
        /**
         * Registers 1 or more namespaces under the CStudioAuthoring Object and returns the last registered namespace object.
         * Note:
         *  - If the first parameter is false CStudioAuthoring will not be implied and the global scope would be the root
         *  - The method may receive multiple namespaces as parameter
         * i.e.
         *  - CStudioAuthoring.namespace("Events", "Utils.Docs", "CStudioAuthoring.Constants")
         *    creates => CStudioAuthoring.Events, CStudioAuthoring.Utils.Docs, CStudioAuthoring.Constants
         *  - CStudioAuthoring.namespace(false, "Events", "Utils.Docs", "CStudioAuthoring.Constants")
         *    creates => Events (window.Events), Utils.Docs (window.Utils.Docs), Constants (window.Constants)
         * @param {String} multiple_arguments The namespaces to create
         * @return {Object} the last namespace object created
         */
        namespace: function() {
            var imply = (arguments[0] !== false),
                oRoot = imply ? CStudioAuthoring : window,
                a=arguments, o=null, j, k, d;
            for (var i = (imply ? 0 : 1), l = a.length; i < l; i++) {
                d = a[i].split(".");
                o = oRoot;
                for (j = (!imply ? 0 : ((d[0] == "CStudioAuthoring") ? 1 : 0)), // Imply CStudioAuthoring if not otherwise specified
                         k = d.length; j < k; j++) {
                    !(o[ d[j] ]) && (o[ d[j] ] = {});
                    o = o[ d[j] ];
                }
            }
            return o;
        },
        /**
         * Creates and initializes a specified namespace under the CStudioAuthoring object.
         * Note:
         *  - Namespace is only created/initialized if it didn't exist before
         *  - CStudioAuthoring namespace is always taken as the root object, is always implied even if not specified
         * i.e.
         *  - { "Events": {...}, "Utils.StringUtils": {...} } creates CStudioAuthoring.Events, CStudioAuthoring.Utils.StringUtils
         *    and initialize them with the paired value of the namespace (object key)
         * @param {Object} oNamespaces An object containing the namespaces to register & initialize
         */
        register: function(oNamespaces){
            var np, oNamespace, exists = this.isSet,
                set = YAHOO.lang.augmentObject,
                rootnp = "CStudioAuthoring.", aux;
            if(arguments.length == 1) {
                for(np in oNamespaces) {
                    if (!exists(np, true)) {
                        oNamespace = this.namespace(np);
                        if (!(typeof oNamespaces[np] == "function"))
                            set(oNamespace, oNamespaces[np]);
                        else {
                            aux = ((np.substr(0, 16) !== rootnp) ? (rootnp + np) : np);
                            eval(aux + "=oNamespaces[np]");
                        }
                    }
                }
            } else {
                np = arguments[0];
                oNamespace = this.namespace(np);
                if (typeof arguments[1] != "function")
                    set(oNamespace, arguments[1]);
                else {
                    aux = ((np.substr(0, 16) !== rootnp) ? (rootnp + np) : np);
                    eval(aux + "=arguments[1]");
                }
            }
            return oNamespace;
        },
        /**
         * Checks if a namespace (or anything) has a value different from undefined, if it has been initialized
         * @param {String} namespace The namespace to check to see if initialized. Non-String values will be evaluated for a value != undefined
         * @param {Boolean} imply If true, looks for the namespace inside the CStudioAuthoring object
         */
        isSet: function(namespace, imply){
            var o = namespace;
            if (Object.prototype.toString.call(namespace) == "[object String]") {
                var props = namespace.split("."),
                    l = props.length;
                o = imply ? CStudioAuthoring : window;
                if (l == 1) return !!(o[namespace]);
                for (var i = 0; i < l && o !== undefined; ++i) o = o[props[i]];
                return (o !== undefined);
            }
            return (o !== undefined);
        }
    }

    var CSA = CStudioAuthoring;

    function CStudioConstant (value) {
        this.getValue = function () {
            return value;
        }
    }

    CStudioConstant.prototype.toString = function () {
        return this.getValue();
    }

    CStudioConstant.toString = function () {
        return "CStudioAuthoring.Constant";
    }

    CSA.register({
        Constant: CStudioConstant,
        /**
         * authoring events
         */
        Events: {
            contextNavLoaded: new YAHOO.util.CustomEvent("contextNavLoadedEvent", CSA),
            contextNavReady: new YAHOO.util.CustomEvent("contextNavReadyEvent", CSA),
            moduleActiveContentReady: new YAHOO.util.CustomEvent("modActiveContentEvent", CSA),

            widgetScriptLoaded: new YAHOO.util.CustomEvent("widgetScript", CSA),
            moduleScriptLoaded: new YAHOO.util.CustomEvent("moduleScript", CSA),

            contentSelected: new YAHOO.util.CustomEvent("contentSelected", CSA),
            contentUnSelected: new YAHOO.util.CustomEvent("contentunSelected", CSA)
        },
        /**
         * general place for constants
         */
        Constants: {
            /*
             * Permission checking constants */
            PERMISSION_READ: new CStudioConstant("read"),
            PERMISSION_WRITE: new CStudioConstant("write"),
            PERMISSION_DELETE: new CStudioConstant("delete"),
            PERMISSION_CREATE_FOLDER: new CStudioConstant("create folder")
        },
        /**
         * required resources, exension of the authoring environment bootstrap
         */
        OverlayRequiredResources: {
            css: [
                '/static-assets/yui/treeview/assets/skins/sam/treeview.css',
                '/static-assets/themes/cstudioTheme/yui/assets/skin.css',
                '/static-assets/themes/cstudioTheme/css/contextNav.css',
                '/static-assets/yui/container/assets/container.css',
                '/static-assets/jquery/jquery-time/jquery.timeentry.css',
                '/static-assets/jquery/jquery-ui/themes/smoothness/jquery-ui.css'
            ], js: [
                '/static-assets/yui/connection/connection-min.js',
                '/static-assets/yui/json/json-min.js',
                '/static-assets/yui/resize/resize-min.js',
                '/static-assets/yui/event-delegate/event-delegate-min.js',
                '/static-assets/yui/container/container_core-min.js',
                '/static-assets/yui/menu/menu-min.js',
                '/static-assets/yui/treeview/treeview-min.js',
                '/static-assets/yui/animation/animation-min.js',
                '/static-assets/yui/container/container-min.js',
                '/static-assets/yui/selector/selector-min.js',
                '/static-assets/components/cstudio-contextual-nav/contextual-nav.js',
                '/static-assets/yui/calendar/calendar-min.js',
                '/static-assets/components/cstudio-components/loader.js'
            ],
            /**
             * this CSS has dynamically defined contents so load order is important
             * Context must be available
             */
            loadContextNavCss: function() {
                //CStudioAuthoring.Utils.addCss('/overlay-css?baseUrl=' +
                //                           CStudioAuthoringContext.baseUri);
                CSA.Utils.addCss('/static-assets/styles/temp.css');
                CSA.Utils.addCss('/static-assets/styles/forms-engine.css');
            },

            /**
             * load all the resources initially
             * required to run the authoring environment
             */
            loadRequiredResources: function() {
                for (var i = 0; i < this.css.length; i++) {
                    CSA.Utils.addCss(this.css[i]);
                }
                for (var j = 0; j < this.js.length; j++) {
                    CSA.Utils.addJavascript(this.js[j]);
                }
            }
        },
        Clipboard: {
            /**
             * constructor
             */
            init: function() {},
            /**
             * get content items in clipboard
             */
            getClipboardContent: function(callback) {
                CSA.Service.getClipboardItems(
                    CStudioAuthoringContext.site,
                    callback);
            },
            /**
             * copy an content item on to the clipboard
             */
            copyContent: function(contentTO, callback) {
                CSA.Service.copyContentToClipboard(
                    CStudioAuthoringContext.site,
                    contentTO.uri,
                    "content",
                    callback);
            },
            /**
             * cut a content item on to the clipboard
             */
            cutContent: function(contentTO, callback) {
                CSA.Service.cutContentToClipboard(
                    CStudioAuthoringContext.site,
                    contentTO.uri,
                    "content",
                    callback);
            },
            /**
             * paste all content as child of contentTO
             */
            pasteContent: function(contentTO, callback) {

                CSA.Service.pasteContentFromClipboard(
                    CStudioAuthoringContext.site,
                    contentTO.uri,
                    callback);
            },

            /**
             * permissions to display the items on clipboard
             */
            getPermissions: function(path, callback) {

                CSA.Service.getUserPermissions(
                    CStudioAuthoringContext.site, path,
                    callback);
            }
        },
        /**
         * track content that is currently selected
         */
        SelectedContent:{

            selectedContent: null,

            /**
             * constructor
             */
            init: function() {
                this.selectedContent = [];
            },

            setContent: function (contentTO) {
                this.selectedContent = [contentTO];
                CSA.Events.contentSelected.fire(contentTO);
            },

            /**
             * content is selected, track it
             */
            selectContent: function(contentTO) {
                if (this.at(contentTO) == -1) {
                    this.selectedContent.push(contentTO);
                    CSA.Events.contentSelected.fire(contentTO);
                }
            },

            /**
             * content unselected, stop tracking it
             */
            unselectContent: function(contentTO) {

                var position = this.at(contentTO);

                if (position != -1) {
                    this.selectedContent.splice(position, 1);
                    CSA.Events.contentUnSelected.fire(contentTO);
                }
            },

            /**
             * return the number of selected content items
             */
            getSelectedContentCount: function() {
                return this.selectedContent.length;
            },

            /**
             * return the selected content
             */
            getSelectedContent: function() {
                return this.selectedContent;
            },

            /**
             * return the position of the item
             */
            at: function(contentTO) {
                var retAt = -1;

                var atContentToId = CSA.Utils.createContentTOId(contentTO);

                for (var i = 0; i < this.selectedContent.length; i++) {
                    var curContentTO = this.selectedContent[i];
                    var curContentToId = CSA.Utils.createContentTOId(curContentTO);

                    if (atContentToId == curContentToId) {
                        retAt = i;
                        break;
                    }
                }

                return retAt;
            }
        },
        /**
         * authoring module manager
         */
        Module: {

            loadedModules: new Array(),
            waitingForModule: new Array(),
             
            /**
             * either receive the Module Class or wait for it to be loaded
             */
            requireModule: function(moduleName, script, moduleConfig, callback) {

                var moduleClass = this.loadedModules[moduleName];

                if (!moduleClass) {
                    if(!this.waitingForModule) {
                        this.waitingForModule = [];
                    }

                    var waiting = this.waitingForModule[moduleName];

                    if(!waiting) {
                        waiting = [];
                    }

                    waiting.push({ callback: callback, moduleConfig: moduleConfig });
                    this.waitingForModule[moduleName] = waiting;

                    CSA.Utils.addJavascript(script);
                } else {
                    callback.moduleLoaded(moduleName, moduleClass, moduleConfig);
                }
            },

            /**
             * event that module has been loaded for those wating
             */
            moduleLoaded: function(moduleName, moduleClass) {

                this.loadedModules[moduleName] = moduleClass;

                try {
                    var waiting = this.waitingForModule[moduleName];
                    var waiter;

                    if(waiting) {
                        for(var i=0; i<waiting.length; i++) {
                            waiter = waiting[i];

                            if(waiter.callback) {
                                var config = (waiter.moduleConfig) ? waiter.moduleConfig : {};
                                waiter.callback.moduleLoaded(moduleName, moduleClass, config);
                            }
                        }
                    }
                }
                catch(err) {
                    var msg = "";
                    msg += "Error while loading module: " + moduleName + "\r\n";
                    msg += "Err:" + err +"\r\n";
                    msg += "callback:" + ((waiter.callback) ? waiter.callback : "none") + "\r\n";
                    msg += "moduleClass:" + ((waiter.moduleClass) ? moduleClass.moduleClass : "none") + "\r\n";
                    msg += "moduleConfig:" + ((waiter.moduleConfig) ? moduleClass.moduleConfig : "none") + "\r\n";

                    if( window.console && window.console.log) { 
                        window.console.log(msg);
                    }
                }
            }
        },
        /**
         * common operations
         */
        Operations: {

            _showDialogueView: function(oRequest, setZIndex, dialogWidth){
                var width = (dialogWidth) ? dialogWidth : "602px";
                var Loader = CSA.Env.Loader,
                    moduleid = oRequest.controller;
                var fn = function() {
                    var dialogueId = CSA.Utils.getScopedId(moduleid || "view"),
                        Controller, dialogue;
                    Controller = CSA.Env.ModuleMap.get(moduleid);
                    dialogue = new CSA.Component.Dialogue(dialogueId, {
                        loadBody: {
                            loaderFn: oRequest.fn,
                            callback: function() {
                                /* set timezone dynamically */
                                if (arguments[0] &&
                                    arguments[0].getResponseHeader &&
                                    arguments[0].getResponseHeader.Timezone) {
                                    var timeZoneText = arguments[0].getResponseHeader.Timezone;
                                    if (timeZoneText) {
                                        timeZoneText = timeZoneText.replace(/^\s+|\s+$/, '');
                                        var oTimeZoneSpan = YDom.get("timeZone");
                                        if (oTimeZoneSpan) {
                                            oTimeZoneSpan.innerHTML = timeZoneText;
                                        }
                                    }
                                }
                                var view;
                                if (Controller) {
                                    view = new Controller({ context: dialogueId });
                                    view.on("end", function(){ dialogue.destroy(); });
                                }
                                oRequest.callback && oRequest.callback.call(view, dialogue);
                                dialogue.centreY();
                            }
                        },
                        fixedcenter: true,
                        width: width,
                        modal: true,
                        close: false,
                        underlay: "none",
                        autofillheight: null
                    });
                    dialogue.render(document.body);
                    dialogue.centreY();

                    //set z-index to 101 so that dialog will display over context nav bar
                    if (setZIndex && dialogue.element && dialogue.element.style.zIndex != "") {
                        dialogue.element.style.zIndex = "102000";
                        dialogue.mask.style.zIndex = "101000";
                    }
                };
                var params = ["component-dialogue"];
                oRequest.controller && params.push(oRequest.controller);
                params.push(fn);
                Loader.use.apply(Loader, params);
            },

            translateContent: function(langBundle){
                var elements = document.querySelectorAll('[data-translation]');
                for(var i=0; i<elements.length; i++){
                    elements[i].innerHTML = CMgs.format(langBundle, elements[i].getAttribute('data-translation'));
                }
            },

            deleteContent: function(items) {

                var controller, view;
                if (CSA.Utils.isAdmin()) {
                    controller = "viewcontroller-delete";
                    view = CSA.Service.getDeleteView;
                } else {
                    // scheduled delete not supported
                    controller = "viewcontroller-submitfordelete";
                    view = CSA.Service.getSubmitForDeleteView;
                    //controller = "viewcontroller-request-delete";
                    //view = CSA.Service.getRequestDeleteView;
                }

                CSA.Operations._showDialogueView({
                    fn: view,
                    controller: controller,
                    callback: function(dialogue) {
                        CSA.Operations.translateContent(formsLangBundle);
                        if(YDom.get("cancelBtn")){YDom.get("cancelBtn").value = CMgs.format(formsLangBundle, "cancel");}
                        if(YDom.get("deleteBtn")){YDom.get("deleteBtn").value = CMgs.format(formsLangBundle, "deleteDialogDelete");}
                        this.loadDependencies(items);
                        this.on("submitComplete", function(evt, args){
                            var reloadFn = function(){
                                window.location.reload();
                            };
                            dialogue.hideEvent.subscribe(reloadFn);
                            dialogue.destroyEvent.subscribe(reloadFn);
                        });
                        // Admin version of the view does not have this events
                        // but then the call is ignored
                        this.on("hideRequest", function(evt, args){
                            dialogue.hide();
                        });
                        this.on("showRequest", function(evt, args){
                            dialogue.show();
                        });
                    }
                }, true);

            },
            viewSchedulingPolicy: function(callback) {
                CSA.Operations._showDialogueView({
                    fn: CSA.Service.getSchedulingPolicyView,
                    callback: callback
                });
            },

            viewContentHistory: function(contentObj, callback){
                CSA.Operations._showDialogueView({
                    fn: CSA.Service.getHistoryView,
                    controller: "viewcontroller-history",
                    callback: function(dialogue) {

                        CSA.Operations.translateContent(formsLangBundle);

                        YDom.get("historyCloseBtn").value = CMgs.format(formsLangBundle, "close");

                        this.loadHistory(contentObj);

                        this.on("submitComplete", function(evt, args){

                            var reloadFn = function(){
                                window.location.reload();
                            };

                            dialogue.hideEvent.subscribe(reloadFn);
                            dialogue.destroyEvent.subscribe(reloadFn);
                        });

                        // Admin version of the view does not have this events
                        // but then the call is ignored
                        this.on("hideRequest", function(evt, args){
                            dialogue.hide();
                        });

                        this.on("showRequest", function(evt, args){
                            dialogue.show();
                        });
                    }
                }, true);
            },

            approveCommon: function (site, items) {

                CSA.Operations._showDialogueView({
                    fn: CSA.Service.getApproveView,
                    controller: 'viewcontroller-approve',
                    callback: function(dialogue) {
                        CSA.Operations.translateContent(formsLangBundle);
                        this.loadItems(items);
                        this.loadPublishingChannels();

                        this.on("submitComplete", function(evt, args){
                            window.location.reload();
                        });

                    }
                }, true, '800px');

            },

            submitContent: function(site, contentItems) {

                CSA.Operations._showDialogueView({
                    fn: CSA.Service.getRequestPublishView,
                    controller: 'viewcontroller-requestpublish',
                    callback: function(dialog) {
                        CSA.Operations.translateContent(formsLangBundle);
                        this.renderItems(contentItems);
                    }
                }, true, '800px');

                /*CStudioAuthoring.Module.requireModule('dialog-simple-submit',
                    '/static-assets/components/cstudio-dialogs/submit-simple.js', {
                        contentItems: contentItems,
                        site: site
                    }, {
                        moduleLoaded: function(moduleName, dialogClass, moduleConfig) {
                            // in preview, this function undefined raises error -- unlike dashboard
                            dialogClass.showDialog && dialogClass.showDialog(moduleConfig.site, moduleConfig.contentItems);
                        }
                    });*/
            },

            /**
             * render a preview of the given content
             */
            renderContentAssetPreview: function(nodeRef, callback) {

                CStudioAuthoring.Service.renderContentAssetPreview(nodeRef, callback);
            },

            /**
             * open a gallery search page
             */
            openGallerySearch: function(searchType, searchContext, select, mode, newWindow, callback, searchId) {

                var openInSameWindow = (newWindow) ? false : true;

                var searchUrl = CStudioAuthoringContext.authoringAppBaseUri +
                    "/gallery?site=" +
                    CStudioAuthoringContext.site +
                    "&s=";

                if (searchType) {
                    searchUrl += "&context=" + searchType;
                }

                if (searchContext.includeAspects && searchContext.includeAspects.length > 0) {
                    searchUrl += "&includeAspects=";

                    for (var i = 0; i < searchContext.includeAspects.length; i++) {
                        searchUrl += searchContext.includeAspects[i];

                        if (i < (searchContext.includeAspects.length - 1)) {
                            searchUrl += "|";
                        }
                    }
                }

                if (searchContext.excludeAspects && searchContext.excludeAspects.length > 0) {
                    searchUrl += "&includeAspects=";

                    for (var j = 0; i < searchContext.excludeAspects.length; j++) {
                        searchUrl += searchContext.excludeAspects[j];

                        if (j < (searchContext.excludeAspects.length - 1)) {
                            searchUrl += "|";
                        }
                    }
                }

                if (!CStudioAuthoring.Utils.isEmpty(searchContext.keywords)) {
                    searchUrl += "&keywords=" + searchContext.keywords;
                }

                if (!CStudioAuthoring.Utils.isEmpty(searchContext.sortBy)) {
                    searchUrl += "&sortBy=" + searchContext.sortBy;
                }

                if (!CStudioAuthoring.Utils.isEmpty(searchContext.type)) {
                    searchUrl += "&type=" + searchContext.type;
                }

                if (!CStudioAuthoring.Utils.isEmpty(searchContext.uploadTime)) {
                    searchUrl += "&uploadTime=" + searchContext.uploadTime;
                }

                if (!CStudioAuthoring.Utils.isEmpty(select)) {
                    searchUrl += "&selection=" + select;
                }

                if (!CStudioAuthoring.Utils.isEmpty(mode)) {
                    searchUrl += "&mode=" + mode;
                }

                if (!CStudioAuthoring.Utils.isEmpty(searchContext.itemsPerPage)) {
                    searchUrl += "&ipp=" + searchContext.itemsPerPage;
                }
                else {
                    searchUrl += "&ipp=20";
                }

                if (!CStudioAuthoring.Utils.isEmpty(searchContext.page)) {
                    searchUrl += "&page=" + searchContext.page;
                }
                else {
                    searchUrl += "&page=1";
                }

                if (searchContext.filters && searchContext.filters.length && searchContext.filters.length > 0) {
                    for (var i = 0; i < searchContext.filters.length; i++) {
                        var startDate = searchContext.filters[i].startDate;
                        var endDate = searchContext.filters[i].endDate;

                        if ( (startDate != null && startDate != undefined && startDate != "")
                            || (endDate != null && endDate != undefined && endDate != "") ) {
                            searchUrl += "&" + searchContext.filters[i].qname + "=" + searchContext.filters[i].value
                            + "|" + searchContext.filters[i].startDate + "|" + searchContext.filters[i].endDate;
                        } else {
                            searchUrl += "&" + searchContext.filters[i].qname + "=" + searchContext.filters[i].value;
                        }
                    }
                }

                // non filter URL data
                if (searchContext.nonFilters && searchContext.nonFilters.length && searchContext.nonFilters.length > 0) {
                    for (var i = 0; i < searchContext.nonFilters.length; i++) {
                        searchUrl += "&" + searchContext.nonFilters[i].qname + "=" + searchContext.nonFilters[i].value;
                    }
                }

                if (!CStudioAuthoring.Utils.isEmpty(searchId) && searchId != "undefined") {
                    searchUrl += "&searchId=" + searchId;
                } else {
                    /* first time search called from other page : show empty result */
                    if (!searchContext.filters || !searchContext.filters.length || searchContext.filters.length == 0)
                        if (!searchContext.contextName || searchContext.contextName == "undefined")
                            searchUrl += "&presearch=false";
                }


                var childSearch = null;

                if (!searchId || searchId == null || searchId == "undefined"
                    || !CStudioAuthoring.ChildSearchManager.searches[searchId]) {
                    childSearch = CStudioAuthoring.ChildSearchManager.createChildSearchConfig();
                    childSearch.openInSameWindow = openInSameWindow;
                    searchId = CStudioAuthoring.Utils.generateUUID();

                    childSearch.searchId = searchId;
                    childSearch.searchUrl = searchUrl + "&searchId=" + searchId;
                    childSearch.saveCallback = callback;

                    CStudioAuthoring.ChildSearchManager.openChildSearch(childSearch);

                }
                else {
                    if (window.opener) {

                        if (window.opener.CStudioAuthoring) {

                            var openerChildSearchMgr = window.opener.CStudioAuthoring.ChildSearchManager;

                            if (openerChildSearchMgr) {

                                childSearch = openerChildSearchMgr.searches[searchId];
                                childSearch.searchUrl = searchUrl;

                                openerChildSearchMgr.openChildSearch(childSearch);
                            }
                        }
                    }
                    else {
                        childSearch = CStudioAuthoring.ChildSearchManager.searches[searchId];
                        childSearch.searchUrl = searchUrl;

                        CStudioAuthoring.ChildSearchManager.openChildSearch(childSearch);
                    }
                }
            },

            /**
             * open a search page
             */
            openSearch: function(searchType, searchContext, select, mode, newWindow, callback, searchId) {

                var openInSameWindow = (newWindow) ? false : true;

                var searchUrl = CStudioAuthoringContext.authoringAppBaseUri +
                    "/search?site=" +
                    CStudioAuthoringContext.site +
                    "&s="+searchContext.keywords;

                if (searchType) {
                    searchUrl += "&context=" + searchType;
                }

                if (searchContext.includeAspects && searchContext.includeAspects.length > 0) {
                    searchUrl += "&includeAspects=";

                    for (var i = 0; i < searchContext.includeAspects.length; i++) {
                        searchUrl += searchContext.includeAspects[i];

                        if (i < (searchContext.includeAspects.length - 1)) {
                            searchUrl += "|";
                        }
                    }
                }

                if (searchContext.excludeAspects && searchContext.excludeAspects.length > 0) {
                    searchUrl += "&includeAspects=";

                    for (var j = 0; i < searchContext.excludeAspects.length; j++) {
                        searchUrl += searchContext.excludeAspects[j];

                        if (j < (searchContext.excludeAspects.length - 1)) {
                            searchUrl += "|";
                        }
                    }
                }


                if (!CStudioAuthoring.Utils.isEmpty(searchContext.keywords)) {
                    searchUrl += "&keywords=" + searchContext.keywords;
                }

                if (!CStudioAuthoring.Utils.isEmpty(searchContext.sortBy)) {
                    searchUrl += "&sortBy=" + searchContext.sortBy;
                }

                if (!CStudioAuthoring.Utils.isEmpty(select)) {
                    searchUrl += "&selection=" + select;
                }

                if (!CStudioAuthoring.Utils.isEmpty(mode)) {
                    searchUrl += "&mode=" + mode;
                }

                if (!CStudioAuthoring.Utils.isEmpty(searchContext.itemsPerPage)) {
                    searchUrl += "&ipp=" + searchContext.itemsPerPage;
                }
                else {
                    searchUrl += "&ipp=20";
                }

                if (!CStudioAuthoring.Utils.isEmpty(searchContext.page)) {
                    searchUrl += "&page=" + searchContext.page;
                }
                else {
                    searchUrl += "&page=1";
                }

                if (searchContext.filters && searchContext.filters.length && searchContext.filters.length > 0) {
                    for (var i = 0; i < searchContext.filters.length; i++) {
                        var startDate = searchContext.filters[i].startDate;
                        var endDate = searchContext.filters[i].endDate;

                        if ( (startDate != null && startDate != undefined && startDate != "")
                            || (endDate != null && endDate != undefined && endDate != "") ) {
                            searchUrl += "&" + searchContext.filters[i].qname + "=" + searchContext.filters[i].value
                            + "|" + searchContext.filters[i].startDate + "|" + searchContext.filters[i].endDate;
                        } else {
                            searchUrl += "&" + searchContext.filters[i].qname + "=" + searchContext.filters[i].value;
                        }
                    }
                }

                // non filter URL data
                if (searchContext.nonFilters && searchContext.nonFilters.length && searchContext.nonFilters.length > 0) {
                    for (var i = 0; i < searchContext.nonFilters.length; i++) {
                        searchUrl += "&" + searchContext.nonFilters[i].qname + "=" + searchContext.nonFilters[i].value;
                    }
                }

                if (!CStudioAuthoring.Utils.isEmpty(searchId) && searchId != "undefined") {
                    searchUrl += "&searchId=" + searchId;
                } else {
                    /* first time search called from other page : show empty result */
                    if (!searchContext.filters || !searchContext.filters.length || searchContext.filters.length == 0)
                        if (!searchContext.contextName || searchContext.contextName == "undefined")
                            searchUrl += "&presearch=false";
                }


                var childSearch = null;

                if (!searchId || searchId == null || searchId == "undefined"
                    || !CStudioAuthoring.ChildSearchManager.searches[searchId]) {
                    childSearch = CStudioAuthoring.ChildSearchManager.createChildSearchConfig();
                    childSearch.openInSameWindow = openInSameWindow;
                    searchId = CStudioAuthoring.Utils.generateUUID();

                    childSearch.searchId = searchId;
                    childSearch.searchUrl = searchUrl + "&searchId=" + searchId;
                    childSearch.saveCallback = callback;

                    CStudioAuthoring.ChildSearchManager.openChildSearch(childSearch);

                }
                else {
                    if (window.opener) {

                        if (window.opener.CStudioAuthoring) {

                            var openerChildSearchMgr = window.opener.CStudioAuthoring.ChildSearchManager;

                            if (openerChildSearchMgr) {

                                childSearch = openerChildSearchMgr.searches[searchId];
                                childSearch.searchUrl = searchUrl;

                                openerChildSearchMgr.openChildSearch(childSearch);
                            }
                        }
                    }
                    else {
                        childSearch = CStudioAuthoring.ChildSearchManager.searches[searchId];
                        childSearch.searchUrl = searchUrl;

                        CStudioAuthoring.ChildSearchManager.openChildSearch(childSearch);
                    }
                }
            },

            /**
             * open a browse page
             */
            openBrowse: function(searchType, path, select, mode, newWindow, callback, searchId) {

                var searchContext = CStudioAuthoring.Service.createSearchContext();

                var openInSameWindow = (newWindow) ? false : true;

                var searchUrl = CStudioAuthoringContext.authoringAppBaseUri +
                    "/browse?site=" +
                    CStudioAuthoringContext.site +
                    "&s=";

                if (searchType) {
                    searchUrl += "&context=" + searchType;
                }


                if (!CStudioAuthoring.Utils.isEmpty(select)) {
                    searchUrl += "&selection=" + select;
                }

                if (!CStudioAuthoring.Utils.isEmpty(mode)) {
                    searchUrl += "&mode=" + mode;
                }

                if (path) {
                    searchUrl += "&PATH=" + path;
                }

                if (!CStudioAuthoring.Utils.isEmpty(searchId) && searchId != "undefined") {
                    searchUrl += "&searchId=" + searchId;
                } else {
                    /* first time search called from other page : show empty result */
                    if (!searchContext.filters || !searchContext.filters.length || searchContext.filters.length == 0)
                        if (!searchContext.contextName || searchContext.contextName == "undefined")
                            searchUrl += "&presearch=false";
                }


                var childSearch = null;

                if (!searchId || searchId == null || searchId == "undefined"
                    || !CStudioAuthoring.ChildSearchManager.searches[searchId]) {
                    childSearch = CStudioAuthoring.ChildSearchManager.createChildSearchConfig();
                    childSearch.openInSameWindow = openInSameWindow;
                    searchId = CStudioAuthoring.Utils.generateUUID();

                    childSearch.searchId = searchId;
                    childSearch.searchUrl = searchUrl + "&searchId=" + searchId;
                    childSearch.saveCallback = callback;

                    CStudioAuthoring.ChildSearchManager.openChildSearch(childSearch);

                }
                else {
                    if (window.opener) {

                        if (window.opener.CStudioAuthoring) {

                            var openerChildSearchMgr = window.opener.CStudioAuthoring.ChildSearchManager;

                            if (openerChildSearchMgr) {

                                childSearch = openerChildSearchMgr.searches[searchId];
                                childSearch.searchUrl = searchUrl;

                                openerChildSearchMgr.openChildSearch(childSearch);
                            }
                        }
                    }
                    else {
                        childSearch = CStudioAuthoring.ChildSearchManager.searches[searchId];
                        childSearch.searchUrl = searchUrl;

                        CStudioAuthoring.ChildSearchManager.openChildSearch(childSearch);
                    }
                }
            },

            refreshPreview: function() {
                var previewFrameEl = document.getElementById("engineWindow");
                previewFrameEl.contentWindow.location.reload();
            },

            setPreview: function(url) {
                var previewFrameEl = document.getElementById("engineWindow");
                previewFrameEl.src = url;
            },

            /**
             * given a transfer object, open a preview URL
             */
            openPreview: function(contentTO, windowId, soundTone, incontextEdit, targetWindowId) {

                if(!targetWindowId) {
                    // if no target is supplied assume local call
                    // basically mimics behavior before target was implmented
                    targetWindowId = window.name;
                }

                var url = "";
                var filename = (contentTO.pathSegment) ? contentTO.pathSegment : contentTO.name;

                if (CStudioAuthoring.Utils.endsWith(filename, ".xml")) {

                    url = CStudioAuthoringContext.previewAppBaseUri + contentTO.browserUri;
                    if (contentTO.document && contentTO.assets && contentTO.assets.length == 1) {
                        url = CStudioAuthoringContext.previewAppBaseUri + contentTO.assets[0].uri;
                    }

                } else {
                    url = CStudioAuthoringContext.previewAppBaseUri+contentTO.uri;
                }

                if (incontextEdit) {
                    window.location.reload();
                } else {

					// remove server name and port etc
					if(url.indexOf("//") != -1) {
						url = url.replace("//","--x--");
						url = url.substring(url.indexOf("/"));
						if(url.indexOf("--x--") != -1) {
							url = "/";
						}
						
					}

                    var Topics = crafter.studio.preview.Topics;
                    window.location = '/studio/preview/#/?page='+url+'&site='+CStudioAuthoringContext.site;
                }

            },

            /**
             * open a content form
             * @param formId is the form ID
             * @param id contentID (path or id) if available otherwise null
             * @param noderef noderef if available otherwise null
             * @param path is the contextual path where the context should be created
             * @param edit true/false is this an edit or a new?
             * @param popup true false, open as a popup
             * @param respLabel is the user viewable name that is sent back from the form (should map to a field ID)
             * @param respValue is the ID that is sent back from the form (should map to a field ID)
             * @param callback is the callback that should be fired when the form is closed
             * @param newly added includeMetaData
             */
            openContentWebForm: function(formId, id, noderef, path, edit, asPopup, callback, auxParams,includeMetaData) {
                var readOnly = false
                auxParams = (auxParams) ? auxParams : [];
                
                for(var j=0; j<auxParams.length; j++) {
                    if(auxParams[j].name=="readonly") {
                        readOnly = true;
                    }
                }

                if(readOnly == true) {
                    CStudioAuthoring.Operations.openContentWebFormWithPermission(formId, id, noderef, path, edit, asPopup,
                    callback, "true", auxParams,includeMetaData);
                }
                else {
                    //check permissions etc
                    var checkPermissionsCb = {
                        success: function(results) {
                            var isWrite = CStudioAuthoring.Service.isWrite(results.permissions);
                            if (isWrite == true) {
                                readOnly = "false";
                            } else {
                                readOnly = "true";
                            }

                            CStudioAuthoring.Operations.openContentWebFormWithPermission(
                                formId, id, noderef, path, edit, asPopup, callback, readOnly,auxParams,includeMetaData);
                        },
                        failure: function() {

                            CStudioAuthoring.Operations.openContentWebFormWithPermission(formId, id, noderef, path, edit, asPopup,
                                callback, "true", auxParams,includeMetaData);
                        }
                    };
                    var permissionPath = "";
                    if (!CStudioAuthoring.Utils.isEmpty(id)) {
                        permissionPath = id;
                    } else {
                        permissionPath = path;
                    }
                    CStudioAuthoring.Service.getUserPermissions(CStudioAuthoringContext.site, permissionPath, checkPermissionsCb);

                }
            },

            /**
             * open a content form
             * @param formId is the form ID
             * @param id contentID (path or id) if available otherwise null
             * @param noderef noderef if available otherwise null
             * @param path is the contextual path where the context should be created
             * @param edit true/false is this an edit or a new?
             * @param asPopup true false, open as a popup
             * @param callback is the callback that should be fired when the form is closed
             * @param readOnly Permission to indicate how the form is going to be opened
             * @param respLabel is the user viewable name that is sent back from the form (should map to a field ID)
             * @param respValue is the ID that is sent back from the form (should map to a field ID)
             * @param newly added includeMetaData
             */
            openContentWebFormWithPermission: function(formId, id, noderef, path, edit, asPopup, callback, readOnly,auxParams,includeMetaData) {
                if(!auxParams) {
                    auxParams = [];
                }

                if(id) {
                    var getContentItemsCb = {
                        success: function (contentTO) {
                            CStudioAuthoring.Operations.performSimpleIceEdit(contentTO.item,
                                null, //field
                                this.isEdit,
                                this.callback,
                                this.aux

                            );
                        },

                        failure: function () {
                            callback.failure();
                        },
                        isEdit: edit,
                        callback: callback,
                        aux: auxParams
                    };
                    
                    CStudioAuthoring.Service.lookupContentItem(CStudioAuthoringContext.site, id, getContentItemsCb, false, false);
                }
                else {
                    // new item
                    CStudioAuthoring.Operations.performSimpleIceEdit({ contentType: formId, uri:path },
                            null, //field
                            false, //isEdit
                            callback
                            //aux
                            );

                }

            },

            /**
             * open form with simple form engine
             */
            openContentWebFormSimpleEngine: function(contentType, path, edit, readOnly, callback, auxParams,includeMetaData) {
                alert("USED?")
                // if(includeMetaData){
                //     auxParams = CStudioAuthoring.Operations.addMetadata(auxParams);
                // }

                // var childForm = CStudioAuthoring.ChildFormManager.createChildFormConfig();

                // childForm.formId = CStudioAuthoring.Utils.generateUUID();
                // childForm.formName = contentType.form ;
                // childForm.windowName = path;

                // childForm.formUrl = CStudioAuthoringContext.authoringAppBaseUri +
                // "/form?site=" + CStudioAuthoringContext.site + "&form=" +
                // contentType.form +
                // "&path=" + path;

                // if(contentType.type){
                //     if(contentType.type=="component"){
                //         childForm.formUrl += "&childForm=true";
                //     }
                // }

                // var readOnlySetByAux = false
                // for(var j=0; j<auxParams.length; j++) {
                //     if(auxParams[j].name=="readonly") {
                //         readOnlySetByAux = true;
                //         readOnly = true;
                //     }

                //     childForm.formUrl += "&" + auxParams[j].name +"="+auxParams[j].value;
                // }

                // childForm.formSaveCallback = callback;

                // lookupItemCb = {
                //     success: function(itemTO) {
                //         if(itemTO.item.lockOwner != "" && itemTO.item.lockOwner != CStudioAuthoringContext.user) {
                //             readOnly = true;
                //         }

                //         if(readOnly && (readOnly=="true" || readOnly==true) && readOnlySetByAux == false ) {
                //             childForm.formUrl += "&readonly=true";
                //         }

                //         if(edit && (edit == true || edit == "true") && (!readOnly || readOnly == false || readOnly == "false")) {
                //             childForm.formUrl += "&edit=" + edit;
                //         }

                //         childForm.formUrl += "&wid=" + childForm.formId;

                //         CStudioAuthoring.ChildFormManager.openChildForm(childForm);
                //     },

                //     failure: function() {
                //     }
                // };

                // if(path.indexOf(".xml") != -1) {
                //     // item is existing content
                //     CStudioAuthoring.Service.lookupContentItem(CStudioAuthoringContext.site, path, lookupItemCb, false);
                // }
                // else {
                //     // item is new
                //     lookupItemCb.success({ item: { lockOwner: CStudioAuthoringContext.user } });
                // }
            },

            /**
             * this method will open a form with the legacy form server
             * this method is maintained for backward compatability and for extremely complex use cases
             */
            openContentWebFormLegacyFormServer: function(formId, id, noderef, path, edit, asPopup, callback, readOnly,auxParams,includeMetaData) {
                alert("legacy form server no longer supported");
            },

            addMetadata: function(params) {
                if(typeof CStudioForms != "undefined" && CStudioForms) {
                    var metadataControl = CStudioForms.nodeManagers["page-metadata"];
                    if(metadataControl) {
                        if(metadataControl.hasPageId() && metadataControl.hasPageIdGroup() ) {
                            metadataControl.addToParams(params);
                        }
                    }
                }
                return params;
            },

            hasParam : function(params, name) {
                for (var i = 0; i < params.length; i++) {
                    if(params[i].name == name)
                        return true;
                }
                return false;
            },

            /**
             * open order taxonomy dialog
             */
            orderTaxonomy: function(site, modelName, level, orderedCb) {

                var openDialogCb = {
                    moduleLoaded: function(moduleName, dialogClass, moduleConfig) {
                        dialogClass.showDialog(moduleConfig.site, moduleConfig.modelName, moduleConfig.level, moduleConfig.orderedCb);
                    }
                }

                var moduleConfig = {
                    site: site,
                    modelName: modelName,
                    level: level,
                    orderedCb: orderedCb
                };

                CStudioAuthoring.Module.requireModule(
                    "dialog-order-taxonomy",
                    "/static-assets/components/cstudio-dialogs/order-taxonomy.js",
                    moduleConfig,
                    openDialogCb);
            },

            /**
             * create a new taxonomy item
             */
            newTaxonomy: function(site, modelName, level, newCb) {

                var openDialogCb = {
                    moduleLoaded: function(moduleName, dialogClass, moduleConfig) {
                        dialogClass.showDialog(moduleConfig.site, moduleConfig.modelName, moduleConfig.level, moduleConfig.newCb);
                    }
                }

                var moduleConfig = {
                    site: site,
                    modelName: modelName,
                    level: level,
                    newCb: newCb
                };

                CStudioAuthoring.Module.requireModule(
                    "dialog-new-taxonomy",
                    "/static-assets/components/cstudio-dialogs/new-taxonomy.js",
                    moduleConfig,
                    openDialogCb);
            },

            performSimpleIceEdit: function(item, field, isEdit, callback, aux) {
                var editorId =  CStudioAuthoring.Utils.generateUUID();
                
                if(callback) {
                    CStudioAuthoring.InContextEdit.registerIceCallback(editorId, callback);
                }

                var id = CSA.Utils.getScopedId(),
                    controller = 'viewcontroller-in-context-edit',
                    animator,
                    view;

                isEdit = (typeof(isEdit) == "undefined") ? true : isEdit;

                var $modal = $('<div><div class="no-ice-mask"></div><div class="studio-ice-dialog studio-ice-container-'+editorId+'" style="display:none"><div class="bd"></div></div></div>');
                $modal.find('.bd').attr('id', id);

                animator = new crafter.studio.Animator($modal.find('.studio-ice-container-'+editorId));

                (!callback) && (callback = {
                    success: function() {
                        if(CStudioAuthoringContext.isPreview) {
                            CStudioAuthoringContext.Service.refreshPreview();
                        }else {
                            window.location.reload();
                        }
                    }
                });

                CSA.Env.Loader.use(controller, function() {
                    CStudioAuthoring.Service.getInContextEditView({
                        success: function (response) {
                            window.top.studioFormZorder= (window.top.studioFormZorder) ? window.top.studioFormZorder + 1 : 9999;
                            var template = '<iframe id="in-context-edit-editor-'+editorId+'" frameborder="0" style="z-index:'+window.top.studioFormZorder+';" onload="CStudioAuthoring.InContextEdit.autoSizeIceDialog();"></iframe>"';
                            var parentEl = window.top.document.body;
                            $modal
                                .find('.bd').html(template).end()
                                .appendTo(parentEl);

                            var Controller = CSA.Env.ModuleMap.get(controller);

                            view = new Controller({ context: id, editorId: editorId });

                            view.initializeContent(
                                item, 
                                field, 
                                CStudioAuthoringContext.site, 
                                isEdit, 
                                callback, 
                                $modal.find('.studio-ice-container-'+editorId), 
                                aux,
                                editorId);

                            view.on("end", function () {
                                $modal.remove();
                            });

                            view.on("updateContent", function(evt, args){
                                callback.success();
                            });

                            animator.slideInDown();


                        }
                    });

                });

            },

            openCopyDialog:function(site, uri, callback, args) {

                var idx = uri.lastIndexOf("index.xml");
                var folderPath = uri;
                if (idx > 0) {
                    folderPath = uri.substring(0, uri.lastIndexOf("index.xml"));
                }
                var cut = false,  // args.cut was the original value, but this parameter is always returning undefined
                    serviceUri = CSA.Service.getPagesServiceUrl + "?site=" + site + "&path=" + folderPath + "&depth=-1&order=default",
                    getCopyTreeItemRequest = CStudioAuthoring.Service.createServiceUri(serviceUri);

                submitDialogCb = {
                    moduleLoaded: function(moduleName, dialogClass, moduleConfig) {
                        dialogClass.createDialog(cut, site);

                        var fillCopyDialog = {
                            success:function(response) {
                                var copyTree= eval("(" + response.responseText + ")");

                                dialogClass.updateDialog(copyTree, cut);

                            },
                            failure:function() {
                                alert("Unable to load contents. Please close the dialogue window and try again.");
                            }
                        };
                        // Call to get the dialog contents
                        YConnect.asyncRequest('GET', getCopyTreeItemRequest, fillCopyDialog);
                    }
                };

                CStudioAuthoring.Module.requireModule("dialog-copy",
                    "/static-assets/components/cstudio-dialogs/copyDialog.js",
                    {},
                    submitDialogCb);
            },

            /**
             * Assign a new template to an existing content item
             */
            assignContentTemplate: function(site, author, path, assignCallback, currentContentType) {

                var chooseTemplateCb = {
                    success: function(contentTypes) {
                        //Remove current content type from list.
                        var originalTypesCount = contentTypes.length;
                        if (currentContentType && contentTypes.length > 1) {
                            var newContentTypes = new Array();
                            for (var typeIdx=0; typeIdx < contentTypes.length; typeIdx++) {
                                var contType = contentTypes[typeIdx];
                                if (contType.form != currentContentType) {
                                    newContentTypes.push(contType);
                                }
                            }
                            contentTypes = newContentTypes;
                        }

                        if (contentTypes.length == 0) {
                            alert("no content types available for [" + site + ":" + path + "]");
                        } else {
                            var selectTemplateDialogCb = {
                                moduleLoaded: function(moduleName, dialogClass, moduleConfig) {
                                    dialogClass.showDialog(moduleConfig.contentTypes, path, false, moduleConfig.selectTemplateCb, true);
                                }
                            }

                            var typeSelectedCb = {

                                success: function(typeSelected) {

                                    var changeTemplateServiceCb = {
                                        success: function() {
                                            this.assignCallback.success(this.typeSelected);
                                        },

                                        failure: function() {
                                            this.assignCallback.failure();
                                        },

                                        assignCallback: assignCallback,
                                        typeSelected: typeSelected

                                    };
                                    changeTemplateServiceCb.success();
                                },

                                failure: function() {
                                    this.assignCallback.failure();
                                },

                                assignCallback: assignCallback
                            };

                            var moduleConfig = {
                                contentTypes: contentTypes,
                                selectTemplateCb: typeSelectedCb
                            };

                            CStudioAuthoring.Module.requireModule("dialog-select-template",
                                "/static-assets/components/cstudio-dialogs/select-content-type.js",
                                moduleConfig,
                                selectTemplateDialogCb);
                        }
                    },

                    failure: function() {
                    }
                };

                CStudioAuthoring.Service.lookupAllowedContentTypesForPath(site, path, chooseTemplateCb);
            },

            getImageRequest: function(data) {
                var callback = {
                    success: function(oResponse) {
                        data.image.src = data.url;
                    },
                    failure: function (oResponse) {
                        var secondCallback = {
                            success: function (oResponse) {
                                data.image.src = data.url;
                            },
                            failure: function (oResponse) {
                                data.image.src = data.url;
                            }
                        }
                        setTimeout(function(){ CStudioAuthoring.Service.getImageRequest({ url:data.url, callback: secondCallback}); },700);
                    }
                }
                CStudioAuthoring.Service.getImageRequest({ url:data.url, callback: callback});
            },

            /**
             * create content for a given site, at a given path
             * opens a dialog if needed or goes directly to the form if no
             * template selection is require (only one option
             */
            createNewContentForType: function(site, path, type, asPopup, formSaveCb, childForm) {
                var auxParams = [];
                if(childForm && childForm == true) {
                    auxParams = [ { name: "childForm", value: "true" }];
                }

                CStudioAuthoring.Operations.openContentWebForm(
                    type,
                    null,
                    null,
                    path,
                    false,
                    asPopup,
                    formSaveCb,
                    auxParams);

            },
            /**
             * create content for a given site, at a given path
             * opens a dialog if needed or goes directly to the form if no
             * template selection is require (only one option
             */
            createNewContent: function(site, path, asPopup, formSaveCb, childForm) {
                var auxParams = [];
                if(childForm && childForm == true) {
                    auxParams = [ { name: "childForm", value: "true" }];
                }

                var callback = {
                    success: function(contentTypes) {
                        if (contentTypes.length == 0) {
                            alert("no content types available for [" + site + ":" + path + "]");
                        }
                        else if (contentTypes.length == 1) {

                            var formId = contentTypes[0].form;


                            CStudioAuthoring.Operations.openContentWebForm(
                                formId,
                                null,
                                null,
                                path,
                                false,
                                asPopup,
                                formSaveCb,
                                auxParams);
                        }
                        else {
                            var selectTemplateCb = {
                                success: function(selectedTemplate) {
                                    CStudioAuthoring.Operations.openContentWebForm(
                                        selectedTemplate,
                                        null,
                                        null,
                                        path,
                                        false,
                                        this.asPopup,
                                        this.formSaveCb,
                                        auxParams);
                                },

                                failure: function() {
                                    this.formSaveCb.failure();
                                },

                                formSaveCb: formSaveCb,
                                asPopup: asPopup
                            };

                            var selectTemplateDialogCb = {
                                moduleLoaded: function(moduleName, dialogClass, moduleConfig) {

                                    dialogClass.showDialog(moduleConfig.contentTypes, path, false, moduleConfig.selectTemplateCb, false);
                                }
                            }

                            var moduleConfig = {
                                contentTypes: contentTypes,
                                selectTemplateCb: selectTemplateCb
                            };

                            CStudioAuthoring.Module.requireModule("dialog-select-template",
                                "/static-assets/components/cstudio-dialogs/select-content-type.js",
                                moduleConfig,
                                selectTemplateDialogCb);
                        }
                    },

                    failure: function() {
                    }
                };


                CStudioAuthoring.Service.lookupAllowedContentTypesForPath(site, path, callback);
            },

            /**
             * Gets the list of files on workflow that will be affected by editing a given item
             * @param params {object} The set of parameters to query the service with
             * @param callback {object} Object containing success and failure callbacks. Success is called with the parsed response as parameter, not the XHR response
             */
            getWorkflowAffectedFiles: function (params, callback) {
                var CSA = CStudioAuthoring,
                    Connect = YAHOO.util.Connect,
                    serviceParams = [],
                    serviceURI;

                if (params) for (var key in params) {
                    serviceParams.push(key + "=" + params[key]);
                };

                serviceURI = CSA.Service.createServiceUri(CSA.Service.getWorkflowAffectedPathsServiceUrl + '?' + serviceParams.join('&'));

                Connect.asyncRequest('GET', serviceURI, {
                    success: function (response) {
                        var content;
                        try {
                            content = CSA.Utils.decode(response.responseText).items;
                        } catch (ex) {  }
                        callback.success && callback.success(content);
                    },
                    failure: function (response) {
                        if (callback.failure) callback.failure(response);
                        else {
                            // TODO can we improve this message to say something useful? will response bring any sort of useful message?
                            var message = 'An error occurred trying to get the affected files.';
                            var dialog = new YAHOO.widget.SimpleDialog(CSA.Utils.getScopedId('error'), {
                                width: "300px",     visible: true,      fixedcenter: true,
                                draggable: false,   close: false,       modal: true,
                                text: message,      icon: YAHOO.widget.SimpleDialog.ICON_WARN,
                                buttons: [{ text:"Accept", handler: function() { this.hide(); }, isDefault:true }]
                            });
                            dialog.setHeader("Warning");
                            dialog.render(document.body);
                        }
                    }
                });
            },

            /**
             * edit content
             */
            editContent: function(formId, site, id, noderef, path, asPopup, callback,auxParams) {

                var CSA = CStudioAuthoring,
                    params = { site: (site || CStudioAuthoringContext.site), path: path },
                    doEdit = function () {
                        CSA.Operations.openContentWebForm(
                            formId, id, noderef, path, true, asPopup, callback, auxParams);
                    };

                CSA.Operations.getWorkflowAffectedFiles(params, {
                    success: function (content) {
                        if (content && content.length) {

                            CSA.Operations._showDialogueView({
                                controller: 'viewcontroller-cancel-workflow',
                                fn: function (oAjaxCfg) {
                                    // because _showDialogueView was designed to load the body from a
                                    // webscript, must simulate the ajax process here
                                    oAjaxCfg.success({ responseText: '' });
                                },
                                callback: function () {
                                    var view = this;
                                    view.setContent(content);
                                    view.on('continue', function () {
                                        doEdit();
                                    });
                                }
                            });

                        } else {
                            doEdit();
                        }
                    }
                });
            },

            /**
             * view content
             */
            viewContent: function(formId, site, id, noderef, path, asPopup, callback,auxParams) {
                if(!auxParams) {
                    auxParams = [];
                }

                auxParams[auxParams.length] = { "name": "readonly", "value" : "true" };

                CStudioAuthoring.Operations.openContentWebForm(formId, id, noderef, path, true, asPopup, callback, auxParams);
            },

            // duplicateContent: function(site, path, argsCallback) {
            //     var serviceUri = "/api/1/services/api/1/clipboard/duplicate.json?site=" + site + "&path=" + path;
            //     var ajaxRequest=CStudioAuthoring.Service.createServiceUri(serviceUri);

            //     var serviceCallback = {
            //         success: function(oResponse) {
            //             argsCallback.success();
            //             var contentTypeJson = oResponse.responseText;

            //             try {
            //                 var contentTypes = eval("(" + contentTypeJson + ")");
            //                 var formId = contentTypes.form;
            //                 var path = contentTypes.path;
            //                 var editCb = {
            //                     success: function() {
            //                         this.callingWindow.location.reload(true);
            //                     },

            //                     failure: function() {
            //                     },

            //                     callingWindow: window
            //                 };

            //                 var auxParams = new Array();
            //                 /******** CRAFTER-533 & 534 ****************/
            //                 // this is a temp fix since cstudio currently doesn't support drafter feature
            //                 // the parameters should be added back when draft becomes available 
            //                 //var param = {};
            //                 //param['name'] = "draft";
            //                 //param['value'] = "true";
            //                 //auxParams.push(param);
            //                 //param = {};
            //                 //param['name'] = "duplicate";
            //                 //param['value'] = "true";
            //                 //auxParams.push(param);
            //                 /******** CRAFTER-533 & 534 ****************/
            //                 CStudioAuthoring.Operations.editContent(
            //                     formId,
            //                     CStudioAuthoringContext.site,path,
            //                     "", path, false,editCb,auxParams);
            //             }
            //             catch(err) {
            //                 //callback.failure(err);
            //             }
            //         },
            duplicateContent: function(site, path, argsCallback) {

                CStudioAuthoring.Service.getContent(path, false, {
                    success: function(parentContent) {
                        // determine the ID of the top level object
//parentContent = parentContent.responseText;

                        var origObjectIdPos = parentContent.indexOf("objectId")+9;
                        var origObjectId = parentContent.substr(origObjectIdPos, 36);
                        var origGroupId = origObjectId.substr(0,4);

                        var contentTypePos = parentContent.indexOf("content-type")+13;
                        var contentTypeEndPos = parentContent.indexOf("<", contentTypePos);
                        var contentType = parentContent.substr(contentTypePos, contentTypeEndPos-contentTypePos);
                      
                        // find a list of paths like a "/site/components/page/GRPID/OBJID/*.xml"
                        var dependencyRegExp = new RegExp("(\\/site\\/components\\/page\\/"+origGroupId+"\\/"+origObjectId+"\\/([^\.]+)\\.xml)","gm");

var dependencies = [];
while(found=dependencyRegExp.exec(parentContent)) {
  dependencies.push(found[0]);
}

                        // create a new ID for this page
                        var newObjectId = CStudioAuthoring.Utils.generateUUID();
                        var newGroupId = newObjectId.substring(0,4);

                        // create new path for this page
                        var newPath = path.replace("/index.xml", "-"+newGroupId+"/index.xml");

                        for(var i=0; i<dependencies.length; i++) {
                            var dependencyPath = dependencies[i];
                            if(dependencyPath.indexOf("/site") != -1) {
                                // generate new path 
                                var newDepPath = dependencyPath.replace(origObjectId, newObjectId);
                                    newDepPath = newDepPath.replace(origGroupId, newGroupId);

                                // replace the value in the parent
                                var replaceIdRegex = new RegExp(dependencyPath, 'g');
                                parentContent = parentContent.replace(replaceIdRegex, newDepPath);

                                // load the dependency
                                CStudioAuthoring.Service.getContent(dependencyPath, false, {
                                    success: function(dependencyContent) {
//dependencyContent = dependencyContent.responseText;
                                        var childSaveCb = {
                                            success: function(){}, 
                                            failure: function(){}
                                        };

                                        // write it in new location
                                        var childContentTypePos = dependencyContent.indexOf("content-type")+13;
                                        var childContentTypeEndPos = dependencyContent.indexOf("<", childContentTypePos);
                                        var childContentType = dependencyContent.substr(childContentTypePos, childContentTypeEndPos-childContentTypePos);

                                        var writeChildFileName = this.path.substr(newDepPath.lastIndexOf("/")+1);
                                        var writeChildPath = this.path; //.substr(0, newDepPath.lastIndexOf("/"));
                                        var writeChildServiceUrl = CStudioAuthoring.Service.createWriteServiceUrl(writeChildPath, writeChildFileName, null, childContentType, CStudioAuthoringContext.site, true, false, false, true);

                                        //var writeChildServiceUrl = "/proxy/alfresco/cstudio/wcm/content/write-content"
                                        //                    + "?site=" + CStudioAuthoringContext.site 
                                        //                    + "&path=" + writeChildPath;
                                                            
                                        YAHOO.util.Connect.setDefaultPostHeader(false);
                                        YAHOO.util.Connect.initHeader("Content-Type", "text/pain; charset=utf-8");
                                        YAHOO.util.Connect.asyncRequest('POST', CStudioAuthoring.Service.createServiceUri(writeChildServiceUrl), childSaveCb, dependencyContent);
                                    },
                                    failure: function(err) {
                                        alert("failed to load component content: "+err);
                                    },
                                    path: newDepPath
                                });
                            }
                        }

                        // update the object id and group id in the top level object
                        parentContent = parentContent.replace(origObjectId, newObjectId);
                        parentContent = parentContent.replace(origGroupId, newGroupId);

                        // save the top level content
                        var writeFileName = newPath.substr(newPath.lastIndexOf("/")+1);
                        var writePath = newPath; //.substr(0, newPath.lastIndexOf("/"));
                        var writeServiceUrl = CStudioAuthoring.Service.createWriteServiceUrl(writePath, writeFileName, null, contentType, CStudioAuthoringContext.site, true, false, false, true);
                        //var writeServiceUrl = "/proxy/alfresco/cstudio/wcm/content/write-content"
                        //                    + "?site=" + CStudioAuthoringContext.site 
                        //                    + "&path=" + writePath;
                        
var parentSaveCb = {
                            success: function(){
                                // open the top level content for edit
                                var getContentItemCb = {
                                    success: function (contentTO) {
                                          contentTO = contentTO.item;
                                         CStudioAuthoring.Operations.editContent(
                                            contentType, //contentTO.form,
                                            CStudioAuthoringContext.siteId,
                                            newPath, //contentTO.uri,
                                            null, //contentTO.nodeRef,
                                            newPath, //contentTO.uri,
                                            false,
                                            { success: function() {}, failure: function() {}});
                                    },
                                    failure: function() {

                                    }
                                };

                                CStudioAuthoring.Service.lookupContentItem(CStudioAuthoringContext.site, newPath, getContentItemCb, false, false);            
                            }, 
                            failure: function(){}
                        };

                        YAHOO.util.Connect.setDefaultPostHeader(false);
                        YAHOO.util.Connect.initHeader("Content-Type", "text/pain; charset=utf-8");
                        YAHOO.util.Connect.asyncRequest('POST', CStudioAuthoring.Service.createServiceUri(writeServiceUrl), parentSaveCb, parentContent);      
                    },
                    failure: function(err) {
                        alert("failed to load content");
                    }
                });
            },

            /**
             * create new template
             */
            createNewTemplate: function(path, templateSaveCb) {
                var createTemplateDialogCb = {
                    moduleLoaded: function(moduleName, dialogClass, moduleConfig) {
                        dialogClass.showDialog(templateSaveCb, path);
                    }
                };

                var createModuleConfig = {
                    createTemplateCb: templateSaveCb,
                    path: path
                };

                CStudioAuthoring.Module.requireModule("new-template-dialog",
                    "/static-assets/components/cstudio-dialogs/new-template.js",
                    createModuleConfig,
                    createTemplateDialogCb);
            },

            /**
             * create new script
             */
            createNewScript: function(path, scriptSaveCb) {
                var createScriptDialogCb = {
                    moduleLoaded: function(moduleName, dialogClass, moduleConfig) {
                        dialogClass.showDialog(scriptSaveCb, moduleConfig);
                    }
                };

                var createModuleConfig = {
                    createScriptDialogCb: scriptSaveCb,
                    path: path
                };

                CStudioAuthoring.Module.requireModule("new-script-dialog",
                    "/static-assets/components/cstudio-dialogs/new-script.js",
                    createModuleConfig,
                    createScriptDialogCb);
            },

            /**
             * open template
             */
            openTemplateEditor: function(contentType, channel, templateSaveCb) {
                var loadTemplateEditorCb = {
                    moduleLoaded: function(moduleName, moduleClass, moduleConfig) {																		var editor = new moduleClass();
                        editor.render(moduleConfig.contentType, moduleConfig.channel, moduleConfig.cb);
                    }
                }

                CStudioAuthoring.Module.requireModule("cstudio-forms-template-editor",
                    "/static-assets/components/cstudio-forms/template-editor.js",
                    { contentType: contentType, channel: channel, cb: templateSaveCb},
                    loadTemplateEditorCb);

            },

            /**
             * create taxonomy for a given site, at a given path
             * opens a dialog if needed or goes directly to the form if no
             * type selection is require (only one option
             */
            createNewTaxonomy: function(path, formSaveCb) {

                var callback = {
                    success: function(contentTypes) {
                        if (contentTypes.types.length == 0) {
                            alert("no taxonomy types available for [" + site + ":" + path + "]");
                        }
                        //else if (contentTypes.types.length == 1) {
                        // fill in this case 
                        //}
                        else {
                            var selectTemplateCb = {
                                success: function(selectedType) {

                                    var createTaxonomyDialogCb = {
                                        moduleLoaded: function(moduleName, dialogClass, moduleConfig) {														if(moduleName == "dialog-create-taxonomy") {
                                            dialogClass.showDialog(
                                                moduleConfig.taxonomyType,
                                                moduleConfig.taxonomyName,
                                                path,
                                                moduleConfig.createTemplateCb);
                                        }
                                        }
                                    };

                                    var createModuleConfig = {
                                        taxonomyType: selectedType.type,
                                        taxonomyName: selectedType.label,
                                        createTemplateCb: selectTemplateCb
                                    };

                                    CStudioAuthoring.Module.requireModule("dialog-create-taxonomy",
                                        "/static-assets/components/cstudio-dialogs/create-taxonomy-item.js",
                                        createModuleConfig,
                                        createTaxonomyDialogCb);
                                },

                                failure: function() {
                                    this.formSaveCb.failure();
                                },

                                formSaveCb: formSaveCb
                            };

                            var selectTemplateDialogCb = {
                                moduleLoaded: function(moduleName, dialogClass, moduleConfig) {

                                    dialogClass.showDialog(moduleConfig.contentTypes, path, false, moduleConfig.selectTemplateCb, false);
                                }
                            }

                            var moduleConfig = {
                                contentTypes: contentTypes,
                                selectTemplateCb: selectTemplateCb
                            };

                            CStudioAuthoring.Module.requireModule("dialog-select-taxonomy",
                                "/static-assets/components/cstudio-dialogs/select-taxonomy-type.js",
                                moduleConfig,
                                selectTemplateDialogCb);
                        }
                    },

                    failure: function() {
                    }
                };

                CStudioAuthoring.Service.lookupAllowedTaxonomyTypesForPath(path, callback);
            },

            /* submit content moved up, next to approveCommon */

            /**
             * approve content
             */
            approveContent: function(site, contentItems) {
                CStudioAuthoring.Module.requireModule(
                    'dialog-approve',
                    '/static-assets/components/cstudio-dialogs/go-live.js', {
                        contentItems: contentItems,
                        site: site
                    }, {
                        moduleLoaded: function(moduleName, dialogClass, moduleConfig) {
                            // in preview, this function undefined raises error -- unlike dashboard
                            dialogClass.showDialog &&
                            dialogClass.showDialog(moduleConfig.site, moduleConfig.contentItems);
                        } });
            },

            /**
             * approve-schedule content
             */
            approveScheduleContent: function(site, contentItems) {
                CStudioAuthoring.Module.requireModule(
                    'dialog-schedule-to-go-live',
                    '/static-assets/components/cstudio-dialogs/schedule-to-go-live.js', {
                        contentItems: contentItems,
                        site: site
                    }, {
                        moduleLoaded: function(moduleName, dialogClass, moduleConfig) {
                            // in preview, this function undefined raises error -- unlike dashboard
                            dialogClass.showDialog && dialogClass.showDialog(moduleConfig.site, moduleConfig.contentItems);
                        }
                    });
            },

            /**
             * reject content
             */
            rejectContent: function(site, contentItems) {
                var submitDialogCb = {
                    moduleLoaded: function(moduleName, dialogClass, moduleConfig) {
                        dialogClass.showDialog && dialogClass.showDialog(moduleConfig.site, moduleConfig.contentItems);
                    }
                }
                var moduleConfig = {
                    contentItems: contentItems,
                    site: site
                };
                CStudioAuthoring.Module.requireModule("dialog-reject",
                    "/static-assets/components/cstudio-dialogs/reject.js",
                    moduleConfig,
                    submitDialogCb);
            },

            /**
             * reload the page action for dialog box buttons.
             */
            pageReload: function(flow){

                if(flow){

                    var panel = YDom.getElementsByClassName("yui-panel-container")[0];
                    if(panel.style.visibility == 'visible' || panel.style.visibility == ''){
                        panel.style.visibility = "hidden";
                    }

                    if(flow=="deleteSchedule") {
                        if(CStudioAuthoringContext.isPreview
                            && CStudioAuthoringContext.isPreview==true
                            && CStudioAuthoringContext.role == "admin") {
                            var deletedPage = document.location.href;
                            deletedPage = deletedPage.replace(CStudioAuthoringContext.previewAppBaseUri, "");
                            var parentPath = "";
                            if(deletedPage.charAt(deletedPage.length - 1) == "/") {
                                deletedPage = deletedPage.substring(0, deletedPage.length - 1);
                            }
                            parentPath = deletedPage.substring(0, deletedPage.lastIndexOf("/"));
                            parentPath = CStudioAuthoringContext.previewAppBaseUri + parentPath;
                            document.location = parentPath;
                            return;
                        }
                    }

                }

                var tempMask = document.createElement("div");
                tempMask.style.backgroundColor = '#ccc';
                tempMask.style.opacity = '0.3';
                tempMask.style.position = 'absolute';
                tempMask.style.top = '0px';
                tempMask.style.left = '0px';
                tempMask.style.height = '100%';
                tempMask.style.width = '100%';
                tempMask.style.zIndex = '9999';
                tempMask.style.paddingTop = '300px';
                tempMask.style.textAlign = 'center';

                var loadingImageEl = document.createElement("img");
                loadingImageEl.src = contextPath + CStudioAuthoringContext.baseUri + "/static-assets/themes/cstudioTheme/images/treeview-loading.gif";
                tempMask.appendChild(loadingImageEl);

                document.body.appendChild(tempMask);
                window.location.reload(true);
            },

            uploadAsset: function(site, path, isUploadOverwrite, uploadCb) {
                CStudioAuthoring.Operations.openUploadDialog(site, path, isUploadOverwrite, uploadCb);	  				},

            /**
             *	opens a dialog to upload an asset
             */
            openUploadDialog: function(site, path, isUploadOverwrite, callback) {

                var serviceUri = CStudioAuthoring.Service.writeContentServiceUrl;

                var openUploadDialogCb = {
                    moduleLoaded: function(moduleName, dialogClass, moduleConfig) {
                        dialogClass.showDialog(moduleConfig.site, moduleConfig.path, moduleConfig.serviceUri, moduleConfig.callback, moduleConfig.isUploadOverwrite);
                    }
                };

                var moduleConfig = {
                    path: path,
                    site: site,
                    serviceUri: serviceUri,
                    callback: callback,
                    isUploadOverwrite: isUploadOverwrite
                }

                CSA.Utils.addCss('/static-assets/libs/cropper/dist/cropper.css');
                CSA.Utils.addCss('/static-assets/themes/cstudioTheme/css/icons.css');

                CStudioAuthoring.Module.requireModule("upload-dialog", "/static-assets/components/cstudio-dialogs/upload-asset-dialog.js", moduleConfig, openUploadDialogCb);
                CStudioAuthoring.Module.requireModule("jquery-cropper", "/static-assets/libs/cropper/dist/cropper.js");
            },

            /**
             * create a folder at a given location within the web project
             */
            createFolder: function(site, path, callingWindow, callback) {
                CStudioAuthoring.Operations.openCreateNewFolderDialog(site, path, callingWindow, callback);
            },

            /**
             * open the create folder dialog
             */
            openCreateNewFolderDialog: function(site, path, callingWindow, callback) {
                if (path.lastIndexOf(".") > 0) {
                    path = path.substring(0, path.lastIndexOf("/"));
                }
                var serviceUri = CStudioAuthoring.Service.createFolderServiceUrl;
                var openCreateFolderDialogCb = {
                    moduleLoaded: function(moduleName, dialogClass, moduleConfig) {
                        dialogClass.showDialog(moduleConfig.site, moduleConfig.path, moduleConfig.serviceUri, moduleConfig.callingWindow, moduleConfig.callback);
                    }
                };

                var moduleConfig = {
                    path: path,
                    site: site,
                    serviceUri: serviceUri,
                    callingWindow: callingWindow,
                    callback: callback
                }

                CStudioAuthoring.Module.requireModule("new-folder-name-dialog", "/static-assets/components/cstudio-dialogs/new-folder-name-dialog.js", moduleConfig, openCreateFolderDialogCb);
            }
        },
        /**
         * all services are encapsulated here
         * There should be no use of REST outside this API
         */
        Service: {
            /**
             * Performs an AJAX request with the given configuration
             * @param oRequest
             */
            request: function(oRequest){
                var Connect = YAHOO.util.Connect;
                if (oRequest.resetFormState && Connect._isFormSubmit) {
                    Connect.resetFormState();
                }
                Connect.setDefaultPostHeader(oRequest.defaultPostHeader || false);
                Connect.initHeader("Content-Type", "application/json; charset=utf-8");
                Connect.asyncRequest(
                    oRequest.method || "GET",
                    oRequest.url,
                    oRequest.callback,
                    oRequest.data);
            },
            /**
             * Reference to CStudioAuthoring.Service.request
             * @see CStudioAuthoring.Service.request
             */
            _getView: function(oRequest){
                var Connect = YAHOO.util.Connect;
                Connect.setDefaultPostHeader(oRequest.defaultPostHeader || false);
                Connect.initHeader("Content-Type", "application/json; charset=utf-8");
                Connect.asyncRequest(
                    oRequest.method || "GET",
                    oRequest.url,
                    oRequest.callback,
                    oRequest.data);
            },
            /**
             * Private method for formating a URL with the context site and URI
             * @param url
             */
            _formatURL: function(url){
                return CStudioAuthoring.StringUtils.keyFormat(url, {
                    site: CStudioAuthoringContext.site,
                    base: CStudioAuthoringContext.baseUri
                });
            },

            getViewCommon: function (url, callback) {

                var srv = CStudioAuthoring.Service,
                    url = srv._formatURL(url);

                srv._getView({
                    url: url,
                    callback: callback,
                    method: 'GET',
                    defaultPostHeader: true
                });

            },

            getHistoryView: function(callback) {
                CSA.Service.getViewCommon('{base}/static-assets/components/cstudio-dialogs-templates/history.html?site={site}', callback);
            },

            getApproveView: function(callback) {
                CSA.Service.getViewCommon('{base}/static-assets/components/cstudio-dialogs-templates/approve.html?site={site}', callback);
            },

            getRequestPublishView: function (callback) {
                CSA.Service.getViewCommon('{base}/static-assets/components/cstudio-dialogs-templates/request-publish.html?site={site}', callback);
            },

            getRequestDeleteView: function (callback) {
                CSA.Service.getViewCommon('{base}/static-assets/components/cstudio-dialogs-templates/request-delete.html?site={site}', callback);
            },

            getSubmitForDeleteView: function(callback) {
                CSA.Service.getViewCommon('{base}/static-assets/components/cstudio-dialogs-templates/submit-for-delete.html?site={site}', callback);
            },

            getDeleteView: function(callback) {
                CSA.Service.getViewCommon('{base}/static-assets/components/cstudio-dialogs-templates/delete.html?site={site}', callback);
            },

            getSchedulingPolicyView: function(callback) {
                var srv = CStudioAuthoring.Service,
                    url = srv._formatURL("{base}/service/ui/workflow-actions/schedule-policy?site={site}");
                srv._getView({
                    url: url,
                    callback: callback,
                    method: "GET"
                });
            },

            getScheduleView: function (callback) {
                CSA.Service.getViewCommon('{base}/static-assets/components/cstudio-dialogs-templates/schedule.html?site={site}', callback);
            },

            getInContextEditView: function(callback) {
                CSA.Service.getViewCommon('{base}/static-assets/components/cstudio-dialogs-templates/in-context-edit.html', callback);
            },

            getImageRequest: function(data) {
                CSA.Service.getViewCommon(data.url, data.callback);
            },

            // constants
            defaultNavContext: "default",
            
            // UI (legacy pattern)
            contextServiceUri: "/context-nav",
            getComponentPreviewServiceUrl: "/crafter-controller/component",

            // service uris

            // content services
            // READ OPS
            getContentUri: "/api/1/services/api/1/content/get-content.json",
            contentExistsUrl: "/api/1/services/api/1/content/content-exists.json",
            lookupContentItemServiceUri: "/api/1/services/api/1/content/get-item.json",
            getVersionHistoryServiceUrl: "/api/1/services/api/1/content/get-item-versions.json",
            lookupContentServiceUri: "/api/1/services/api/1/content/get-items-tree.json",
            searchServiceUrl: "/api/1/services/api/1/content/search.json",
            writeContentServiceUrl: "/api/1/services/api/1/content/write-content.json",
            lookupContentTypeServiceUri: "/api/1/services/api/1/content/get-content-type.json",
            allContentTypesForSite: "/api/1/services/api/1/content/get-content-types.json",
            allowedContentTypesForPath: "/api/1/services/api/1/content/get-content-types.json",
            retrieveSitesUrl: "/api/1/services/api/1/user/get-sites-3.json",
            retrievePublishingChannelsUrl: "/api/1/services/api/1/deployment/get-available-publishing-channels.json",
            
            getPagesServiceUrl: "/api/1/services/api/1/content/get-pages.json",
            lookupFoldersServiceUri: "/api/1/services/api/1/content/get-pages.json", // NEED A SERVICE


            // WRITE OPS
            getRevertContentServiceUrl: "/api/1/services/api/1/content/revert-content.json",
            unlockContentItemUrl: "/api/1/services/api/1/content/unlock-content.json",
            changeContentTypeUrl: "/api/1/services/api/1/content/change-content-type.json",
            submitDeleteContent: "/api/1/services/api/1/content/delete-content.json",
            deleteContentUrl: "/api/1/services/api/1/workflow/go-delete.json",
            createFolderServiceUrl: "/api/1/services/api/1/content/create-folder.json",
            
            // ORDER SERVICES
            // READ
            getServiceOrderUrl: "/api/1/services/api/1/content/get-item-orders.json",
            getNextOrderSequenceUrl: "/api/1/services/api/1/content/get-next-item-order.json",
            
            //WRITE
            reorderServiceSubmitUrl: "/api/1/services/api/1/content/reorder-items.json",
            
            // DEPLOYMENT SERVICES
            // READ OPS
            getDeploymentHistoryServiceUrl: "/api/1/services/api/1/deployment/get-deployment-history.json",
            getScheduledItemsServiceUrl: "/api/1/services/api/1/deployment/get-scheduled-items.json",
            getDependenciesServiceUrl: "/api/1/services/api/1/dependency/get-dependencies.json",

            // Preview Services
            previewSyncAllServiceUrl: "/api/1/services/api/1/preview/sync-site.json",

            // Activity Services
            getUserActivitiesServiceUrl: "/api/1/services/api/1/activity/get-user-activities.json",

            // Security Services
            getPermissionsServiceUrl: "/api/1/services/api/1/security/get-user-permissions.json",
            lookupAuthoringRoleServiceUrl : "/api/1/services/api/1/security/get-user-roles.json",
            verifyAuthTicketUrl: "/api/1/services/api/1/user/validate-token.json",
            logoutUrl: "/api/1/services/api/1/user/logout.json",

            // Configuration Services
            getConfigurationUrl: "/api/1/services/api/1/site/get-configuration.json",

            // Workflow Services
            getGoLiveQueueItemsServiceUrl: "/api/1/services/api/1/workflow/get-go-live-items.json",
            getWorkflowAffectedPathsServiceUrl: "/api/1/services/api/1/workflow/get-workflow-affected-paths.json",
            createWorkflowJobsServiceUrl: "/api/1/services/api/1/workflow/create-jobs.json",
            getWorkflowJobsServiceUrl: "/api/1/services/api/1/workflow/get-active-jobs.json",
            rejectContentServiceUrl: "/api/1/services/api/1/workflow/reject.json",

            // Clipboard
            copyServiceUrl: "/api/1/services/api/1/clipboard/copy-item.json",
            copyContentToClipboardServiceUri: "/api/1/services/api/1/clipboard/copy-item.json",
            cutContentToClipboardServiceUri: "/api/1/services/api/1/clipboard/cut-item.json",
            pasteContentFromClipboardServiceUri: "/api/1/services/api/1/clipboard/paste-item.json",
            getClipboardItemsServiceUri: "/api/1/services/api/1/clipboard/get-items.json",
                    
            // Analytics
            getAnalyticsReportUrl: "/api/1/services/analytics/get-report.json",
            
            // Dependencies
            lookupContentDependenciesServiceUri: "/api/1/services/api/1/dependency/get-dependencies.json?deletedep=true&",

            // Crop Image
            cropImageServiceUri: "/api/1/services/api/1/content/crop-image.json",

            // not ported yet
            // writeContentAssetServiceUrl:  "/cstudio/content/upload-content-asset",
            // wcmMapContentServiceUri: "/proxy/alfresco/cstudio/wcm/content/map-content",
            // allSearchableContentTypesForSite: "/proxy/alfresco/cstudio/wcm/contenttype/get-all-searchable-content-types",
            // lookupUserProfileServiceUrl: "/proxy/alfresco/cstudio/profile/get-profile",
            // getJsonFormattedModelDataUrl: "/proxy/alfresco/cstudio/model/get-model-data?format=json",
            // getTaxonomyServiceUrl: "/proxy/alfresco/cstudio/model/get-model-data",
            // getStatusListUrl: "/proxy/alfresco/cstudio/wcm/workflow/get-status-list",
            // renderContentPreviewUrl: "/service/cstudio/wcm/components/content-viewer",
            // cleanHtmlUrl: "/service/cstudio/services/content/cleanhtml",
            // updateTaxonomyUrl: "/proxy/alfresco/cstudio/taxonomy/update-taxonomy",
            // createTaxonomyItemUrl: "/proxy/alfresco/cstudio/taxonomy/create",
            // allowedTaxonomyTypesForPathUrl: "/proxy/alfresco/cstudio/taxonomy/allowed-types",
            // getContentFieldValueServiceUrl: "/service/cstudio/services/content/readfield",
            // updateContentFieldValueServiceUrl: "/service/cstudio/services/content/writefield",
            // getSiteServiceUrl : "/proxy/alfresco/cstudio/site/get-site",
            // previewSyncAllServiceUrl: "/proxy/alfresco/cstudio/wcm/sync/sync-site",
            // setObjectStateServiceUrl: "/proxy/alfresco/cstudio/objectstate/set-object-state",
            

            /**
             * lookup authoring role. having 'admin' role in one of user roles will return admin. otherwise it will return contributor
             * this method is used in preview overlay
             *
             */
            lookupAuthoringRole: function(site, user, callback) {
                var serviceUri = this.lookupAuthoringRoleServiceUrl + "?site=" + site + "&user=" + user;

                var serviceCallback = {
                    success: function(response) {
                        var contentResults = eval("(" + response.responseText + ")");
                        var roles = contentResults.roles;
                        var role = "contributor";
                        if (roles != undefined) {
                            for (var i = 0; i < roles.length; i++) {
                                if (roles[i] == "admin") {
                                    role = "admin";
                                    break;
                                }
                            }
                        }
                        contentResults.role = role;
                        callback.success(contentResults);
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest("GET", this.createServiceUri(serviceUri), serviceCallback);
            },

            /**
             * get domain name
             */
            getDomainName: function(site) {
                alert("Service.getDomainName depricated");
            },

            /**
             * add the appropriate base to the service
             */
            createServiceUri: function(service) {
                var uri = CStudioAuthoringContext.baseUri + service;
                uri += (uri.indexOf("?") == -1) ? "?" : "&";
                uri += "nocache=" + new Date();

                return uri;
            },

            createEngineServiceUri: function(service) {
                return CStudioAuthoringContext.previewAppBaseUri + service;
            },

            /**
             * Add webapp context and make proxy to the service
             */
            createProxyServiceUri: function(service) {
                return CStudioAuthoringContext.baseUri + this.ALFRESCO_PROXY + service;
            },

            Analytics: {
                /**
                 * get analytics report
                 * @param site site ID
                 * @param webPropertyId
                 * @param reportId
                 */
                getReport: function(site, webPropertyId, reportId, callback, filter) {

                    var serviceUrl = CStudioAuthoring.Service.getAnalyticsReportUrl;
                    serviceUrl += "?site="+site;
                    serviceUrl += "&webPropertyId=" + webPropertyId;
                    serviceUrl += "&reportId=" + reportId;

                    if(filter) {
                        serviceUrl += "&filter="+filter
                    }

                    var serviceCallback = {
                        success: function(response) {
                            var res = response.responseText || "null";  // Some native JSON parsers (e.g. Chrome) don't like the empty string for input
                            callback.success(YAHOO.lang.JSON.parse(res));
                        },
                        failure: function(response) {
                            callback.failure(response);
                        }
                    };
                    YConnect.asyncRequest('GET', CStudioAuthoring.Service.createServiceUri(serviceUrl), serviceCallback);
                }
            },

            /**
             * pretty Formatting for HTML markup
             */
            prettyFormatHtmlMarkup: function(markup, callback) {
                var html = markup;
                html = html.replace(/\s{2,}/g, ' ');
                html = html.replace(/\r/g, '');
                html = html.replace(/\n/g, '');
                html = html.replace(/\t/g, '');

                var newHtml = "";
                var tagStag = [];

                var containerTags = ['p','ul','ol','li','div','b', 'table','tr','td','th'];
                var inScript = false;
                var inComment = false;
                var inClosingTag = false;
                var inAtomTag = false;
                var inContainerTag = false;
                var indent = 0;

                // function to determin if
                var isContainingTagFn = function(tagName) {
                    var retContainerTag = false;

                    for(var j=0; j<containerTags.length; j++) {
                        if(tagName == containerTags[j]) {
                            retContainerTag = true;
                            break;
                        }
                    }

                    return retContainerTag;
                };

                for(var i=0; i<html.length; i++) {
                    var curChar = html.substring(i, i+1);

                    // look for closings so we can add line breaks
                    if(curChar == '<' && inScript == false && inComment == false) {
                        var spacePos;
                        var bracketPos;
                        var pos;
                        var tagName;

                        // parse tag name
                        if(html.substring(i, i+2) == '</') {
                            spacePos = html.indexOf(' ', i);
                            bracketPos = html.indexOf('>', i);
                            pos = (spacePos != -1 && spacePos <= bracketPos) ? spacePos : bracketPos;
                            tagName = html.substring(pos, i+2);
                        }
                        else {
                            spacePos = html.indexOf(' ', i);
                            bracketPos = html.indexOf('>', i);
                            pos = (spacePos <= bracketPos) ? spacePos : bracketPos;
                            tagName = html.substring(pos, i+1);
                        }

                        if(html.substring(i, i+2) == '</') {
                            inClosingTag = true;

                            if(isContainingTagFn(tagName)) {
                                indent--;
                            }
                        }
                        else {

                            if(tagName == "br") {
                                inAtomTag = true;
                            }
                            else {
                                if(isContainingTagFn(tagName)) {
                                    inContainerTag = true;
                                    indent++;
                                }
                            }
                        }
                    }

                    // add line breaks
                    if(curChar == '>'
                        && (tagName != "a" && tagName != "span")
                        && (inClosingTag || inAtomTag || inContainerTag)) {
                        newHtml += curChar + "\r\n";

                        // add indent
                        for(var k=0; k<indent; k++) {
                            newHtml +="   ";
                        }

                        inClosingTag = false;
                        inAtomTag = false;
                        inContainerTag = false;
                    }
                    else {
                        newHtml += curChar;
                    }
                }

                callback.success(newHtml);
            },

            /**
             * set the state of a given object
             */
            setObjectState: function(site,path,state,callback) {

                var serviceUri = this.setObjectStateServiceUrl;
                serviceUri += "?site=" + site+
                "&path=" + path +
                "&state=" + state +
                "&systemprocessing=false";

                var serviceCallback = {
                    success: function(response) {
                        callback.success({ status: "success" });
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest('GET', this.createServiceUri(serviceUri), serviceCallback);
            },

            /**
             * clean markuo
             */
            cleanHtmlMarkup: function(markup, callback) {

                var serviceUri = this.cleanHtmlUrl;

                var serviceCallback = {
                    success: function(response) {
                        var cleanMarkup = response.responseText;
                        callback.success(cleanMarkup);
                    },

                    failure: function(response) {
                        callback.failure(response);
                    },

                    originalMarkup: markup
                };

                YConnect.setDefaultPostHeader(false);
                YConnect.initHeader("Content-Type", "application/xml; charset=utf-8");
                YConnect.asyncRequest('POST', this.createServiceUri(serviceUri), serviceCallback, markup);
            },

            /**
             * write content Asset (NON XML)
             */
            writeContentAsset: function() {
                var serviceUri = this.writeContentAssetServiceUrl;
                // this method is not done.  upload asset is a form based api
                // this api will need to create a hidden form to make this api work
                // see dialog upload asset for example				
            },

            /**
             * this method exists for legacy reasons.  Do not call it, use the actual service instead
             */
            createWriteServiceUrl: function(path, filename, oldPath, contentType, site, createFolders, draft, duplicate, unlock) {
                var url =
                    this.writeContentServiceUrl +
                    '?site=' + site +
                    '&path=' + path +
                    '&fileName=' + filename +
                    '&contentType=' + contentType +
                    '&createFolders=' + createFolders +
                    '&draft=' + draft +
                    '&duplicate=' + duplicate +
                    '&unlock=' + unlock;

                if (oldPath && oldPath != null) {
                    url += '&old=' + oldPath;
                }

                return url;
            },

            /**
             * write content (XML)
             * Path is where you want the content to go
             * filename is the name of the file specifically
             * oldpath is OPTIONAL and is used if you are doing a rename on write
             * content is THE CONTENT
             * contentType is MIMETYPE for assets or CONTENTTYPE for XML
             * site is the site
             * createfolders TRUE if any missing paths should be created
             * draft TRUE if item is not yet saved to working area
             * duplicate TRUE if you are duplicating an existing item
             * unlock TRUE if item should be unlocked after the write
             */
            writeContent: function(path, filename, oldPath, content, contentType, site, createFolders, draft, duplicate, unlock, callback) {
                var serviceUri = this.createWriteServiceUrl(path, filename, oldPath, contentType, site, createFolders, draft, duplicate, unlock);

                YConnect.setDefaultPostHeader(false);
                YConnect.initHeader("Content-Type", "application/xml; charset=utf-8");
                YConnect.asyncRequest('POST', this.createServiceUri(serviceUri), {
                    success: function(response) {
                        var content = response.responseText;
                        callback.success(content);
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                }, content);
            },

            /**
             * get content for a specific field
             */
            getContentFieldValue: function(itemPath, field, site, callback) {

                var serviceUri = this.getContentFieldValueServiceUrl;
                serviceUri += "?siteId="+site;
                serviceUri += "&contentPath="+itemPath;
                serviceUri += "&field="+field;

                var serviceCallback = {
                    success: function(response) {
                        var value = response.responseText;
                        callback.success(value);

                    },

                    failure: function(response) {
                        callback.failure(response);
                    },

                    itemPath: itemPath,
                    field: field,
                    site: site
                };

                YConnect.setDefaultPostHeader(false);
                YConnect.initHeader("Content-Type", "application/xml; charset=utf-8");
                YConnect.asyncRequest('GET', this.createServiceUri(serviceUri), serviceCallback);
            },

            /**
             * update content for a specific field
             */
            updateContentFieldValue: function(itemPath, field, site, content, callback) {

                var serviceUri = this.updateContentFieldValueServiceUrl;
                serviceUri += "?siteId="+site;
                serviceUri += "&contentPath="+itemPath;
                serviceUri += "&field="+field;

                var serviceCallback = {
                    success: function(response) {
                        callback.success();
                    },

                    failure: function(response) {
                        callback.failure(response);
                    },

                    itemPath: itemPath,
                    field: field,
                    site: site,
                    content: content
                };

                YConnect.setDefaultPostHeader(false);
                YConnect.initHeader("Content-Type", "application/xml; charset=utf-8");
                YConnect.asyncRequest('POST', this.createServiceUri(serviceUri), serviceCallback, content);
            },

            /**
             * lookup configuration
             */
            lookupConfigurtion: function(site, configPath, callback) {
                var serviceUrl = this.getConfigurationUrl;
                serviceUrl += "?site="+site;
                serviceUrl += "&path=" + configPath;

                var serviceCallback = {
                    success: function(response) {
                        var res = response.responseText || "null";  // Some native JSON parsers (e.g. Chrome) don't like the empty string for input
                        callback.success(YAHOO.lang.JSON.parse(res));
                        try{
                            CStudioAuthoring.Operations.translateContent(previewLangBundle);
                        }catch(err){

                        }

                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                };
                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },

            /**
             * unlock the content item
             */
            unlockContentItem: function(site, path, callback) {
                var serviceUrl = this.unlockContentItemUrl +
                    "?site=" + site +
                    "&path=" + path;

                var serviceCallback = {
                    success: function(response) {
                        callback.success();
                    },
                    failure: function(response) {
                        callback.failure();
                    }
                };

                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },
            /**
             *  unlock the content item synchronous
             *  Used on unload event of the window
             */
            unlockContentItemSync: function(site, path){

                var _self = this;
                function isLockedByUser(site, path) {
                    var value = false, response, itemTO;
                    var serviceUrl = _self.lookupContentItemServiceUri + "?site=" + site + "&path=" + path + "&populateDependencies=false";
                    var xhrObj = YConnect.createXhrObject();
                    xhrObj.conn.open("GET", _self.createServiceUri(serviceUrl), false);
                    xhrObj.conn.send(null);

                    response = xhrObj.conn.responseText;
                    if( response && response != "") {
                        itemTO = eval("(" + response + ")");
                        if ( itemTO.item.lockOwner == CStudioAuthoringContext.user) {
                            value = true;
                        }
                    }
                    return value;
                }

                if ( !isLockedByUser(site, path))
                    return;

                var serviceUrl = this.unlockContentItemUrl +
                    "?site=" + site +
                    "&path=" + path;

                var xhrObj = YConnect.createXhrObject();
                xhrObj.conn.open("GET", this.createServiceUri(serviceUrl), false);
                xhrObj.conn.send(null);

                return xhrObj.conn.responseText;
            },

            /**
             * given a site id and a path look up the available taxonomy types
             */
            lookupAllowedTaxonomyTypesForPath: function(path, callback) {

                CStudioAuthoring.Service.lookupGlobalConfigurtion(
                    "/taxonomies-config.xml",

                    {
                        success: function(config) {
                            this.callback.success(config);

                        },

                        failure: function() {
                            this.callback.failure();
                        },

                        context: this,
                        callback: callback
                    });
            },


            /**
             * create taxonomy item
             */
            createTaxonomyItem: function(path, type, title, createCb) {
                var serviceUrl = this.createTaxonomyItemUrl +
                    "?type=" + type +
                    "&name=" + title +
                    "&path=" + path;

                var serviceCallback = {
                    success: function(response) {
                        createCb.success();
                    },
                    failure: function(response) {
                        createCb.failure();
                    }
                };

                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },


            /**
             * update taxonomy
             */
            updateTaxonomies: function(site, taxonomies, updateCb) {
                var serviceUrl = this.updateTaxonomyUrl +
                    "?site="+site;

                var serviceCallback = {
                    success: function(response) {
                        updateCb.success();
                    },
                    failure: function(response) {
                        updateCb.failure();
                    }
                };

                YConnect.setDefaultPostHeader(false);
                YConnect.initHeader("Content-Type", "application/json; charset=utf-8");
                YConnect.asyncRequest('POST', this.createServiceUri(serviceUrl), serviceCallback, JSON.stringify(taxonomies));
            },

            /**
             * update taxonomy
             */
            updateTaxonomies: function(site, taxonomies, updateCb) {
                var serviceUrl = this.updateTaxonomyUrl +
                    "?site="+site;

                var serviceCallback = {
                    success: function(response) {
                        updateCb.success();
                    },
                    failure: function(response) {
                        updateCb.failure();
                    }
                };

                YConnect.setDefaultPostHeader(false);
                YConnect.initHeader("Content-Type", "application/json; charset=utf-8");
                YConnect.asyncRequest('POST', this.createServiceUri(serviceUrl), serviceCallback, JSON.stringify(taxonomies));
            },

            /**
             * change template functionality.
             */
            changeContentType: function(site, contentPath, contentType, changeContentTypeCb) {
                var serviceUrl = this.changeContentTypeUrl;
                serviceUrl += "?site=" + site;
                serviceUrl += "&path=" + contentPath;
                serviceUrl += "&contentType=" + contentType;
                var serviceCallback = {
                    success: function(response) {
                        changeContentTypeCb.success(contentType);
                        //window.location.reload();
                    },
                    failure: function(response) {
                        alert("Error changing Content Type");
                        changeContentTypeCb.failure();
                    }
                };
                YConnect.asyncRequest('POST', this.createServiceUri(serviceUrl), serviceCallback);
            },
            /**
             * Constructs get-content service url with the given path as a parameter
             */
            createGetContentServiceUri: function(path) {
                return CStudioAuthoringContext.baseUri + this.getContentUri +
                    "?site=" + CStudioAuthoringContext.site +
                    "&path=" + path +
                    "&edit=false" +
                    "&ticket=" + CStudioAuthoring.Utils.Cookies.readCookie("ccticket") +
                    "&nocache=" + new Date();
            },
            /**
             * check, if the content is edited by another user.
             */
            checkContentStatus : function(path,callback) {
                var serviceCallback = {
                    success : function(response) {
                        callback.success(response);
                    },
                    failure : function(response) {
                        callback.failure(response);
                    }
                };
                var serviceUri = this.createServiceUri(this.getContentUri) +
                    "?site=" + CStudioAuthoringContext.site +
                    "&path=" + path +
                    "&edit=true" +
                    "&ticket=" + CStudioAuthoring.Utils.Cookies.readCookie("ccticket") +
                    "&nocache=" + new Date();

                YConnect.asyncRequest('GET',serviceUri, serviceCallback);
            },

            /**
             *  Returns the item content
             *  If edit equals true, tries to lock the content
             */
            getContent: function(path, edit, callback){
                var serviceUrl = CStudioAuthoring.Service.getContentUri
                    + "?site=" + CStudioAuthoringContext.site
                    + "&path=" + path +
                    "&edit=" + edit +
                    "&ticket=" + CStudioAuthoring.Utils.Cookies.readCookie("ccticket") +
                    "&nocache=" + new Date();

                var serviceCallback = {
                    success: function(content) {
                        var contentData = YAHOO.lang.JSON.parse(content.responseText)
                        callback.success(contentData.content);
                    },
                    failure: function(err) {
                        callback.failure(err);
                    }
                };

                YConnect.asyncRequest('GET', CStudioAuthoring.Service.createServiceUri(serviceUrl), serviceCallback);
            },
            /**
             * determine if content exists
             */
            contentExists : function(path,callback) {
                var serviceCallback = {
                    success : function(response) {
                        var result = YAHOO.lang.JSON.parse(response.responseText).content;
                        callback.exists(result);
                    },
                    failure : function(response) {
                        callback.failure(response);
                    }
                };
                var serviceUri = this.contentExistsUrl + "?site=" + CStudioAuthoringContext.site + "&path=" + path;

                YConnect.asyncRequest('GET',this.createServiceUri(serviceUri), serviceCallback);
            },

            /**
             * pull component preview from preview server
             */
            getComponentPreview: function(componentId, callback) {
                var serviceUrl = this.getComponentPreviewServiceUrl;
                // adding to uid to prevent cached response				
                serviceUrl += "?path=" + componentId + "&uid=" + CStudioAuthoring.Utils.generateUUID()+"&preview=true";
                var serviceCallback = {
                    success: function(response) {
                        var result = response.responseText;
                        callback.success(result);
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                var cObj = YConnect.asyncRequest('GET', serviceUrl, serviceCallback);
                setTimeout(function() { YConnect.abort(cObj, serviceCallback) },20000);
            },
            /**
             * copy content to clipboard
             */
            copyContentToClipboard: function(site, contentId, contentType, callback) {
                var serviceUrl = this.copyContentToClipboardServiceUri;
                serviceUrl += "?site=" + site;
                serviceUrl += "&user=" + CStudioAuthoringContext.user;
                serviceUrl += "&contentId=" + contentId;
                serviceUrl += "&contentType=content";
                var serviceCallback = {
                    success: function(jsonResponse) {
                        var results = eval("(" + jsonResponse.responseText + ")");
                        callback.success(results);
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                };
                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },
            /**
             * cut content to clipboard
             */
            cutContentToClipboard: function(site, contentId, contentType, callback) {
                var serviceUrl = this.cutContentToClipboardServiceUri;
                serviceUrl += "?site=" + site;
                serviceUrl += "&user=" + CStudioAuthoringContext.user;
                serviceUrl += "&contentId=" + contentId;
                serviceUrl += "&contentType=content";
                var serviceCallback = {
                    success: function(jsonResponse) {
                        var results = eval("(" + jsonResponse.responseText + ")");
                        callback.success(results);
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                };
                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },
            /**
             * copy content to clipboard
             */
            pasteContentFromClipboard: function(site, toPathContentId, callback) {
                var serviceUrl = this.pasteContentFromClipboardServiceUri;
                serviceUrl += "?site=" + site;
                serviceUrl += "&parentPath=" + toPathContentId;
                var serviceCallback = {
                    success: function(jsonResponse) {
                        var results = eval("(" + jsonResponse.responseText + ")");
                        callback.success(results);
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                };
                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },
            /**
             * get clipboard items
             */
            getClipboardItems: function(site, callback) {
                var serviceUrl = this.getClipboardItemsServiceUri;
                serviceUrl += "?site=" + site;
                serviceUrl += "&user=" + CStudioAuthoringContext.user;

                var serviceCallback = {
                    success: function(jsonResponse) {
                        var results = eval("(" + jsonResponse.responseText + ")");
                        callback.success(results);
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                };
                YConnect.asyncRequest('GET', CStudioAuthoring.Service.createServiceUri(serviceUrl), serviceCallback);
            },

            previewServerSyncAll: function(site, callback) {
                var serviceUrl = this.previewSyncAllServiceUrl;
                serviceUrl += "?site=" + site;

                var serviceCallback = {
                    success: function(jsonResponse) {
                        var results = {};
                        if(jsonResponse.responseText != "") {
                            results = eval("(" + jsonResponse.responseText + ")");
                        }

                        callback.success(results);
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest('POST', this.createServiceUri(serviceUrl), serviceCallback);
            },
            /**
             * crop image
             */
            cropImage: function(site, path, left, top, height, width, callback) {
                var serviceUrl = this.cropImageServiceUri;
                serviceUrl += "?site=" + site;
                serviceUrl += "&path=" + path;
                serviceUrl += "&t=" + top;
                serviceUrl += "&l=" + left;
                serviceUrl += "&h=" + height;
                serviceUrl += "&w=" + width;
                var serviceCallback = {
                    success: function(jsonResponse) {
                        var results = eval("(" + jsonResponse.responseText + ")");
                        callback.success(results);
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                };
                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },

            getUserPermissions: function(site, path, callback) {
                var serviceUrl = this.getPermissionsServiceUrl;
                serviceUrl += "?site=" + site + "&path=" + path + "&user=" + CStudioAuthoringContext.user;
                var serviceCallback = {
                    success: function(jsonResponse) {
                        var results = eval("(" + jsonResponse.responseText + ")");
                        callback.success(results);
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                };
                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },

            /**
             * look at perms to see if there is a write in the group
             */
            isWrite:function(permissions) {
                for (var i = 0; i < permissions.length; i++) {
                    if(permissions[i] == "write") {
                        return true;
                    }
                }

                return false;
            },

            createFlatMap:function(itemArray) {
                var _pupulateMap = function(itemArray, map) {
                    for (var i = 0; i < itemArray.length; i++) {
                        var item = itemArray[i];
                        map[item.uri] = item;
                        if (item.children.length > 0) {
                            _pupulateMap(item.children, map);
                        }
                    }
                }
                var map = {};
                _pupulateMap(itemArray, map);
                return map;
            },
            getChildren:function(parentItem, flatMap) {
                var children = new Array();
                for (var key in flatMap) {
                    var aItem = flatMap[key];
                    if (aItem.mandatoryParent == parentItem.uri) {
                        children.push(aItem);
                    }
                }
                return children;
            },
            /**
             * get go live items
             */
            getGoLiveQueueItems: function(site, includeInprogressItems, sortBy, sortAscDesc, callback, filterByNumber) {
                callback.beforeServiceCall();
                var serviceUrl = this.getGoLiveQueueItemsServiceUrl;
                serviceUrl += "?site=" + site;
                if (sortBy != null && sortBy != null) {
                    serviceUrl += "&sort=" + sortBy;
                    if (sortAscDesc != undefined && sortAscDesc != null) {
                        serviceUrl += "&ascending=" + sortAscDesc;
                    }
                }
                if (filterByNumber != null && filterByNumber != '') {
                    serviceUrl += "&num=" + filterByNumber;
                }
                if (includeInprogressItems) {
                    serviceUrl += "&includeInProgress=true";
                }
                var serviceCallback = {
                    success: function(jsonResponse) {
                        var results = eval("(" + jsonResponse.responseText + ")");
                        CStudioAuthoringWidgets.GoLiveQueueDashboard.resultMap = CStudioAuthoring.Service.createFlatMap(results.documents);
                        callback.success(results);
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };
                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },
            /**
             * get user activites items
             */
            getUserActivitiesServices: function(site, user, sortBy, sortAscDesc, number,filterBy, hideLive, callback) {
                callback.beforeServiceCall();
                var serviceUrl = this.getUserActivitiesServiceUrl;
                serviceUrl += "?site=" + site;
                if (user != undefined && user != null) {
                    serviceUrl += "&user=" + user;
                }
                if (sortBy != null && sortBy != null) {
                    serviceUrl += "&sort=" + sortBy;

                    if (sortAscDesc != undefined && sortAscDesc != null) {
                        serviceUrl += "&ascending=" + sortAscDesc;
                    }
                }
                if (number != undefined && number != null) {
                    serviceUrl += "&num=" + number;
                }
                if (filterBy == undefined && filterBy == null) {

                    filterBy = "pages";
                }
                serviceUrl += "&filterType=" + filterBy;
                serviceUrl += "&excludeLive=" + ((hideLive != undefined && hideLive != null) ? hideLive : false);
                var serviceCallback = {
                    success: function(jsonResponse) {
                        var results = eval("(" + jsonResponse.responseText + ")");
                        callback.success(results);
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                };
                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },
            /**
             * get scheduled items
             */
            getScheduledItems: function(site, sortBy, sortAscDesc,filterBy,callback) {
                callback.beforeServiceCall();
                var serviceUrl = this.getScheduledItemsServiceUrl;
                serviceUrl += "?site=" + site;
                if (sortBy != null && sortBy != null) {
                    serviceUrl += "&sort=" + sortBy;

                    if (sortAscDesc != undefined && sortAscDesc != null) {
                        serviceUrl += "&ascending=" + sortAscDesc;
                    }
                }
                if (filterBy == undefined && filterBy == null) {

                    filterBy = "pages";
                }
                serviceUrl += "&filterType=" + filterBy;
                var serviceCallback = {
                    success: function(jsonResponse) {
                        var results = eval("(" + jsonResponse.responseText + ")");
                        callback.success(results);
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };
                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },
            /**
             * get recently deployed items
             */
            getDeploymentHistory: function(site, sortBy, sortAscDesc, days, number,filterBy, callback) {
                callback.beforeServiceCall();
                var serviceUrl = this.getDeploymentHistoryServiceUrl;
                serviceUrl += "?site=" + site;
                if (days != undefined && days != null) {
                    serviceUrl += "&days=" + days;
                }
                if (sortBy != null && sortBy != null) {
                    serviceUrl += "&sort=" + sortBy;
                    if (sortAscDesc != undefined && sortAscDesc != null) {
                        serviceUrl += "&ascending=" + sortAscDesc;
                    }
                }
                if (number != undefined && number != null) {
                    serviceUrl += "&num=" + number;
                }
                if (filterBy == undefined && filterBy == null) {

                    filterBy = "pages";
                }
                serviceUrl += "&filterType=" + filterBy;
                var serviceCallback = {
                    success: function(jsonResponse) {
                        var results = eval("(" + jsonResponse.responseText + ")");
                        callback.success(results);
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                };
                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },

            /**
             * revert content item
             */
            revertContentItem: function(site, contentTO, version, callback) {

                var serviceUrl = this.getRevertContentServiceUrl;

                serviceUrl += "?site=" + site;
                serviceUrl += "&path=" + contentTO.uri;
                serviceUrl += "&version=" + version;

                var serviceCallback = {
                    success: function(jsonResponse) {
                        var results = eval("(" + jsonResponse.responseText + ")");
                        callback.success(results);
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                }

                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },

            /**
             * get version history for given content path
             */
            getVersionHistory: function(site, contentTO, callback) {

                var serviceUrl = this.getVersionHistoryServiceUrl;
                serviceUrl += "?site=" + site;

                serviceUrl += "&path=" + contentTO.uri;
                serviceUrl += "&maxhistory=100";

                var serviceCallback = {
                    success: function(jsonResponse) {
                        var results = eval("(" + jsonResponse.responseText + ")");
                        callback.success(results);
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },


            /**
             * given a site id and a path look up the available content types
             */
            deleteContentForPathService: function(site, path, callback) {
                var serviceUrl = this.deleteContentForPathUrl;
                serviceUrl += "?site=" + site;
                serviceUrl += "&path=" + path;
                var serviceCallback = {
                    success: function(oResponse) {
                        if (callback) {
                            callback.success();
                        }
                    },
                    failure: function(response) {
                        if (callback) {
                            callback.failure(response);
                        }
                    }
                };
                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },
            /**
             * Retrieve the content as a JSON object for a given path
             */
            retrieveWcmMapContent: function(path, callback) {
                var serviceUrl = this.wcmMapContentServiceUri + "?site=" + CStudioAuthoringContext.site + "&path=" + path;
                var serviceCallback = {
                    success: function(oResponse) {
                        callback.success(JSON.parse(oResponse.responseText));
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };
                YConnect.asyncRequest('GET', this.createServiceUri(serviceUrl), serviceCallback);
            },
            /**
             * retrieve the content for a given contextual nav context
             */
            retrieveContextualNavContent: function(navContext, callback) {
                navContext = (navContext) ? navContext : this.defaultNavContext;
                var serviceUrl = this.contextServiceUri + "?site=" +  CStudioAuthoringContext.site + "&context=" + navContext;
                YConnect.asyncRequest("GET", this.createServiceUri(serviceUrl), {
                    success: function(oResponse) {
                        var navContent = oResponse.responseText;
                        callback.success(navContent);
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                });
            },

            /**
             * given a context, retrieve the site dropdown context
             */
            retrieveContextNavConfiguration: function(context, callback) {
                CStudioAuthoring.Service.lookupConfigurtion(
                    CStudioAuthoringContext.site,
                    "/context-nav/contextual-nav.xml",
                    {
                        success: function(config) {
                            if(!config.context.length) {
                                this.callback.success(config.context);
                            }
                        },

                        failure: function() {
                            this.callback.failure();
                        },

                        context: this,
                        callback: callback
                    });
            },

            /**
             * given a context, retrieve the site dropdown context
             */
            retrieveSiteDropdownConfiguration: function(context, callback) {
                if(this.contextNavInitialized) {

                    if(!this.contextNavConfig.contexts.length) {
                        callback.success(this.contextNavConfig.contexts.context);
                    }
                    else {
                        callback.success(this.contextNavConfig.contexts[0]);
                    }
                }
                else {
                    CStudioAuthoring.Service.lookupConfigurtion(
                        CStudioAuthoringContext.site,
                        "/context-nav/site-dropdown.xml",
                        {
                            success: function(config) {
                                this.context.contextNavConfig = config;
                                this.context.contextNavInitialized = true;

                                if(!config.contexts.context.length) {
                                    this.callback.success(config.contexts.context);
                                }
                                else {
                                    this.callback.success(config.contexts.context[0]);
                                }
                            },

                            failure: function() {
                                this.callback.failure();
                            },

                            context: this,
                            callback: callback
                        });
                }
            },

            /**
             * finds the site-content menu root path from item-path
             */
            getDropDownParentPathFromItemPath: function(dropdownConfig, path){
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

                        for (k = 0, c = modules.length; k < c; k++) {
                            if (modules[k].params) {
                                var ppath = modules[k].params.path;
                                if (path.indexOf(ppath) > -1)
                                    return ppath;
                            }
                        }
                    }
                }
                return "";
            },

            /**
             * retrieve site-dropdown and match parent
             */
            matchDropdownParentNode: function(path) {
                var retPath = "";
                this.retrieveSiteDropdownConfiguration("default", {
                    success: function(config) {
                        retPath = CStudioAuthoring.Service.getDropDownParentPathFromItemPath(config, path);
                    },

                    failure: function() {
                    },

                    context: this
                });
                return retPath;
            },

            /**
             * content-menu parent path
             */
            menuParentPathKeyFromItemUrl: function(path) {
                return this.matchDropdownParentNode(path) + '-latest-opened-path';
            },

            
            /**
             * retrieve list of channels for a given site
             */
            retrievePublishingChannels: function(site, callback) {
                var serviceUrl = this.retrievePublishingChannelsUrl + "?site="+site;

                var serviceCallback = {
                    success : function(response) {
                        var channels = eval("(" + response.responseText + ")");
                        
                        callback.success(channels);
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest("GET", this.createServiceUri(serviceUrl), serviceCallback);
            },


            /**
             * retrieve a list of sites and their metadata
             */
            retrieveSitesList: function(callback) {
                var retSites = null;
                var serviceUrl = this.retrieveSitesUrl;

                var serviceCallback = {
                    success : function(response) {
                        var sitesModel = eval("(" + response.responseText + ")");
                        var menuModel = [];

                        if(sitesModel.length) {
                            for(var i=0; i<sitesModel.length; i++) {
                                menuModel.push({name: sitesModel[i].name, siteId: sitesModel[i].siteId, link: "/preview#/?page=/&site="+ sitesModel[i].shortId});
                            }
                        }

                        callback.success(menuModel);
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest("GET", this.createServiceUri(serviceUrl), serviceCallback);
            },

            /**
             * lookup Content item
             */
            lookupContentItem: function(site, path, callback, isDraft, populateDependencies) {
 
                var serviceUri = this.lookupContentItemServiceUri + "?site=" + site + "&path=" + path;
                if (isDraft) {
                    serviceUri = serviceUri + "&draft=true";
                }

                if (populateDependencies != undefined && !populateDependencies) {
                    serviceUri = serviceUri + "&populateDependencies=false";
                }

                var serviceCallback = {
                    success: function(response) {
                        var contentResults = eval("(" + response.responseText + ")");

                        try {
                            callback.success(contentResults, callback.argument);
                        }
                        catch(err) {
                        }
                    },

                    failure: function(response) {
                        callback.failure("error loading data", callback.argument);
                    }
                };


                YConnect.asyncRequest("GET", this.createServiceUri(serviceUri), serviceCallback);
            },

            /**
             * lookup folders
             */
            lookupSiteFolders: function(site, path, depth, order, callback, populateDependencies) {

                var serviceUri = this.lookupFoldersServiceUri + "?site=" + site + "&path=" + path + "&depth=" + depth + "&order=" + order;

                if (populateDependencies != undefined && !populateDependencies) {
                    serviceUri = serviceUri + "&populateDependencies=false";
                }

                var serviceCallback = {
                    success: function(response) {
                        var contentResults = eval("(" + response.responseText + ")");

                        callback.success(contentResults, callback.argument);
                    },

                    failure: function(response) {
                        callback.failure(response, callback.argument);
                    }
                };

                YConnect.asyncRequest("GET", this.createServiceUri(serviceUri), serviceCallback);
            },

            /**
             * lookup pages
             */
            lookupSiteContent: function(site, path, depth, order, callback) {

                var serviceUri = this.lookupContentServiceUri + "?site=" + site + "&path=" + path + "&depth=" + depth + "&order=" + order;

                var serviceCallback = {
                    success: function(response) {
                        var contentResults = eval("(" + response.responseText + ")");

                        callback.success(contentResults, callback.argument);
                    },

                    failure: function(response) {
                        callback.failure(response, callback.argument);
                    }
                };


                YConnect.asyncRequest("GET", this.createServiceUri(serviceUri), serviceCallback);
            },

            /**
             * create workflow jobs
             */
            createWorkflowJobs: function(jobRequests, callback) {
                var serviceUri = this.createWorkflowJobsServiceUrl;

                var serviceCallback = {
                    success: function(response) {
                        var targets = eval("(" + response.responseText + ")");

                        callback.success(targets);
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                var requestAsString = JSON.stringify(jobRequests);

                YConnect.setDefaultPostHeader(false);
                YConnect.initHeader("Content-Type", "application/json; charset=utf-8");
                YConnect.asyncRequest('POST', this.createServiceUri(serviceUri), serviceCallback, requestAsString);
            },

            /**
             * lookup translation jobs
             */
            getWorkflowJobs: function(site, callback) {

                var serviceUri = this.getWorkflowJobsServiceUrl + "?site=" + site;

                var serviceCallback = {
                    success: function(response) {
                        var jobs = eval("(" + response.responseText + ")");

                        callback.success(jobs);
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };


                YConnect.asyncRequest("GET", this.createServiceUri(serviceUri), serviceCallback);
            },


            /**
             * lookup user profile
             */
            getSite: function(key, mappingKey, callback) {

                var serviceUri = this.getSiteServiceUrl + "?key=" + key;
                if (mappingKey != undefined) {
                    serviceUri = serviceUrl + "&mappingKey=" + mappingKey;
                }

                var serviceCallback = {
                    success: function(response) {
                        var result = eval("(" + response.responseText + ")");
                        callback.success(result.site);
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest("GET", this.createServiceUri(serviceUri), serviceCallback);
            },

            /**
             * lookup user profile
             */
            lookupUserProfile: function(site, user, callback) {

                var serviceUri = this.lookupUserProfileServiceUrl + "?site=" + site + "&user=" + user;

                var serviceCallback = {
                    success: function(response) {
                        var contentResults = eval("(" + response.responseText + ")");

                        contentResults.studioRole = (contentResults.contextual == "SiteManager") ? "admin" : "contributor";

                        callback.success(contentResults, callback.argument);
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };


                YConnect.asyncRequest("GET", this.createServiceUri(serviceUri), serviceCallback);
            },


            // is this really a service and not a util, can we rename it to something descriptive?
            isCreateFolder: function(permissions) {
                for (var i = 0; i < permissions.length; i++) {
                    if(permissions[i] == "create folder") {
                        return true;
                    }
                }
                return false;
            },

            // is this really a service and not a util, can we rename it to something descriptive?
            isUserAllowed: function(permissions) {
                for (var i = 0; i < permissions.length; i++) {
                    if(permissions[i] == "not allowed") {
                        return false;
                    }
                }
                return true;
            },

            // is this really a service and not a util, can we rename it to something descriptive?
            isDeleteAllowed: function(permissions) {
                for (var i = 0; i < permissions.length; i++) {
                    if(permissions[i] == "delete") {
                        return true;
                    }
                }
                return false;
            },

            // is this really a service and not a util, can we rename it to something descriptive?
            isPublishAllowed: function(permissions) {
                for (var i = 0; i < permissions.length; i++) {
                    if(permissions[i] == "publish") {
                        return true;
                    }
                }
                return false;
            },

            /**
             * lookup content type metadata
             */
            lookupContentType: function(site, type, callback) {

                var serviceUri = this.lookupContentTypeServiceUri + "?site=" + site + "&type=" + type;

                var serviceCallback = {
                    success: function(oResponse) {
                        var contentTypeJson = oResponse.responseText || "null";  // Some native JSON parsers (e.g. Chrome) don't like the empty string for input

                        try {

                            var contentType = YAHOO.lang.JSON.parse(contentTypeJson);
                            callback.success(contentType);
                        }
                        catch(err) {
                            callback.failure(err);
                        }
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest('GET', this.createServiceUri(serviceUri), serviceCallback);
            },

            /**
             * given a site id returns the available All content types
             */
            getAllContentTypesForSite: function(site, callback) {

                var serviceUri = this.allContentTypesForSite + "?site=" + site;

                var serviceCallback = {
                    success: function(oResponse) {
                        var contentTypeJson = oResponse.responseText;

                        try {
                            var contentTypes = eval("(" + contentTypeJson + ")");

                            if(!contentTypes.length) {
                                contentTypes = [contentTypes];
                            }
                            callback.success(contentTypes);
                        }
                        catch(err) {
                            callback.failure(err);
                        }
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest('GET', this.createServiceUri(serviceUri), serviceCallback);
            },

            /**
             * given a site id and a path look up the available content types
             */
            lookupAllowedContentTypesForPath: function(site, path, callback) {


                if (!path.match(".xml$")) path = path + "/";

                var serviceUri = this.allowedContentTypesForPath + "?site=" + site + "&path=" + path;

                var serviceCallback = {
                    success: function(oResponse) {
                        var contentTypeJson = oResponse.responseText;

                        try {
                            var contentTypes = eval("(" + contentTypeJson + ")");
                            callback.success(contentTypes);
                        }
                        catch(err) {
                            callback.failure(err);
                        }
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest('GET', this.createServiceUri(serviceUri), serviceCallback);
            },


            /**
             * given a site id returns All searchable content types
             */
            getAllSearchableContentTypesForSite: function(site, user, callback) {

                var serviceUri = this.allSearchableContentTypesForSite + "?site=" + site + "&user=" + user;

                var serviceCallback = {
                    success: function(oResponse) {
                        var contentTypeJson = oResponse.responseText;

                        try {
                            var contentTypes = eval("(" + contentTypeJson + ")");
                            callback.success(contentTypes);
                        }
                        catch(err) {
                            callback.failure(err);
                        }
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest('GET', this.createServiceUri(serviceUri), serviceCallback);
            },


            /**
             * given a list of items return the topdown dependencies
             */
            lookupContentDependencies: function(site, contentItems, callback) {
                var serviceUri = this.lookupContentDependenciesServiceUri + "site=" + site;
                //var dependencyXml = CStudioAuthoring.Utils.createContentItemsXml(contentItems);
                var dependencyJson = CStudioAuthoring.Utils.createContentItemsJson(contentItems);
                var serviceCallback = {
                    success: function(oResponse) {
                        var respJson = oResponse.responseText;
                        try {
                            var dependencies = eval("(" + respJson + ")");
                            callback.success && callback.success(dependencies);
                        } catch(err) {
                            callback.failure && callback.failure(err);
                        }
                    },
                    failure: callback.failure
                };
                YConnect.setDefaultPostHeader(false);
                YConnect.initHeader("Content-Type", "application/json; charset=utf-8");
                YConnect.asyncRequest('POST', this.createServiceUri(serviceUri), serviceCallback, dependencyJson);
            },


            /**
             * given a site id and a path look up the available content types
             */
            setWindowState: function(userId, pageId, widgetId, stateName, stateValue) {

                var stateId = userId + "-" + pageId + "-" + widgetId + "-" + stateName;

                localStorage.setItem(stateId, stateValue);
            },

            /**
             * given a site id and a path look up the available content types
             */
            getWindowState: function(userId, pageId, widgetId, stateName, callback) {

                var stateId = userId + "-" + pageId + "-" + widgetId + "-" + stateName;
                var stateValue = "";

                stateValue = localStorage.getItem(stateId);

                return stateValue;
            },

            /**
             * return all taxonomies
             */
            getTaxonomies: function(site, callback) {
                alert("NOT IMPLEMENTED");
                callback.failure();
            },

            /**
             * retrieves a given taxonomy
             */
            getTaxonomy: function(site, modelName, level, currentOnly, elementName, callback) {
                var serviceUri = this.getTaxonomyServiceUrl +
                    "?site=" + site +
                    "&elementName=" + elementName +
                    "&format=json";

                if (modelName && modelName != null) {
                    serviceUri += "&modelName=" + modelName;
                }

                if (level) {
                    serviceUri += "&startLevel=" + level;
                    serviceUri += "&currentOnly="+ currentOnly;
                }

                var serviceCallback = {
                    success : function(response) {
                        this.callback.success(JSON.parse(response.responseText));
                    },
                    failure: function(response) {
                        this.callback.failure(response);
                    },

                    callback: callback
                };

                YConnect.asyncRequest("GET", this.createServiceUri(serviceUri), serviceCallback);
            },

            /**
             * retrieves a possible status of a content
             */
            getStatusList: function(site, callback) {
                var serviceUri = this.getStatusListUrl + "?site=" + site;
                var serviceCallback = {
                    success : function(response) {
                        callback.success(JSON.parse(response.responseText));
                    },
                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest("GET", this.createServiceUri(serviceUri), serviceCallback);
            },

            /**
             * DEPRICATED, use getTaxonomy instead
             * Get Product & Version Data
             */
            getModelData : function(site, modelName, callback) {
                CStudioAuthoring.Service.getTaxonomy(site, modelName, -1, false, callback);
            },

            /**
             * given a site id and a path retrive the navigation order
             */
            reorderServiceRequest: function(site, path, order, callback) {

                if (!path.match(".xml$")) path = path + "/";

                var serviceUri = this.reorderServiceSubmitUrl + "?site=" + site + "&order=" + order + "&path=" + path;

                var serviceCallback = {
                    success: function(oResponse) {
                        var contentTypeJson = oResponse.responseText;

                        try {
                            var contentTypes = eval("(" + contentTypeJson + ")");
                            callback.success(contentTypes);
                        }
                        catch(err) {
                            callback.failure(err);
                        }
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest('GET', this.createServiceUri(serviceUri), serviceCallback);
            },

            getOrderServiceRequest: function(site, path, order, callback) {

                if (!path.match(".xml$")) path = path + "/";

                var serviceUri = this.getServiceOrderUrl + "?site=" + site + "&order=" + order + "&path=" + path;

                var serviceCallback = {
                    success: function(oResponse) {
                        var contentTypeJson = oResponse.responseText;

                        try {
                            var contentTypes = eval("(" + contentTypeJson + ")");
                            callback.success(contentTypes);
                        }
                        catch(err) {
                            callback.failure(err);
                        }
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest('GET', this.createServiceUri(serviceUri), serviceCallback);
            },

            /*
             * given a site path retrieves the next sequence order number
             */
            getNextOrderSequenceRequest: function(site, path, callback){
                var serviceUri = this.getNextOrderSequenceUrl + "?site=" + site + "&parentpath=" + path;

                var serviceCallback = {
                    success: function(oResponse) {
                        var nextValueJson = oResponse.responseText;
                        var nextValue = eval("(" + nextValueJson + ")")
                        try {
                            callback.success(parseFloat(nextValue.nextValue));
                        }
                        catch(err) {
                            callback.failure(err);
                        }
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest('GET', this.createServiceUri(serviceUri), serviceCallback);
            },

            /**
             *  create the panel on the call back of reorder service request
             */
            reorderServiceCreatePanel: function(panelid, contentTypes, site, control) {

                var createDialogOrder = {
                    moduleLoaded: function(moduleName, dialogClass, moduleConfig) {
                        dialogClass.layout(moduleConfig);
                        dialogClass.content(moduleConfig.id, moduleConfig.contentItems, moduleConfig);
                        dialogClass.create(moduleConfig.id);
                    }
                };

                var moduleConfig = {
                    contentItems: contentTypes,
                    site: site,
                    id: panelId,
                    control: control
                };

                CStudioAuthoring.Module.requireModule("dialog-nav-order", "/static-assets/components/cstudio-dialogs/page-nav-order-panel.js", moduleConfig, createDialogOrder);
            },


            /**
             * given a site id and a path , and order set the navigation
             */
            reorderServiceSubmit: function(site, path, order, callback) {

                var serviceUri = this.reorderServiceSubmitUrl + "?site=" + site + "&order=" + order + "&path=" + path;

                var serviceCallback = {
                    success: function(oResponse) {
                        var contentTypeJson = oResponse.responseText;

                        try {
                            var contentTypes = eval("(" + contentTypeJson + ")");
                            callback.success(contentTypes);
                        }
                        catch(err) {
                            callback.failure(err);
                        }
                    },

                    failure: function(response) {
                        callback.failure(response);
                    }
                };

                YConnect.asyncRequest('GET', this.createServiceUri(serviceUri), serviceCallback);
            },

            /**
             * renderContentPreview
             */
            renderContentAssetPreview: function(nodeRef, callback) {

                var serviceUri = this.renderContentPreviewUrl + "?nodeRef=" + nodeRef;

                var serviceCallback = {
                    success: function(response) {
                        callback.success(response.responseText);
                    },

                    failure: function(response) {
                        callback.failure("error retrieving content asset preview");
                    }
                };

                YConnect.asyncRequest("GET", this.createServiceUri(serviceUri), serviceCallback);
            },

            /**
             * returns a empty search context
             */
            createSearchContext: function() {
                return {
                    searchTypes: [],
                    includeAspects: [],
                    excludeAspects: [],
                    keywords: "",
                    filters: [],
                    nonFilters: [],
                    sortBy: "",
                    sortAscending: true,
                    page: 1,
                    pageSize: 20
                };
            },

            /**
             * execute a search
             */
            search: function(site, searchContext, callback) {

                var serviceUrl = this.searchServiceUrl;

                serviceUrl += "?site=" + site;

                searchContext.searchType = (searchContext.searchType) ? searchContext.searchType : "";
                searchContext.sortBy = (!searchContext.sortBy || searchContext.sortBy == "relevance") ? "" : searchContext.sortBy;
                searchContext.page = (searchContext.currentPage) ? searchContext.currentPage : 1;
                searchContext.sortAscending = (searchContext.sortAscending) ? searchContext.sortAscending : true;
                searchContext.pageSize = (searchContext.itemsPerPage) ? searchContext.itemsPerPage : 20;
                searchContext.includeAspects = (searchContext.includeAspects) ? searchContext.includeAspects : new Array();

                var searchCb = {
                    success : function(response) {

                        var results = eval("(" + response.responseText + ")");

                        callback.success(results);
                    },
                    failure: function(response) {
                        //alert(response.responseText);
                        callback.failure(response);
                    }
                };

                var searchConfig = '{';

                searchConfig += '"contentTypes": [ ';
                for (var j = 0; j < searchContext.contentTypes.length; j++) {
                    if (searchContext.contentTypes[j] != "") {
                        searchConfig += "'" + searchContext.contentTypes[j] + "'";
                        if (j < searchContext.contentTypes.length - 1) {
                            searchConfig += ",";
                        }
                    }
                }
                searchConfig += ']';

                searchConfig += ',"includeAspects": [ ';
                for (var k = 0; k < searchContext.includeAspects.length; k++) {
                    if (searchContext.includeAspects[k] != "") {
                        searchConfig += "'" + searchContext.includeAspects[k] + "'";
                        if (k < searchContext.includeAspects.length - 1) {
                            searchConfig += ",";
                        }
                    }
                }
                searchConfig += ']';

                searchConfig += ',"excludeAspects": [ ';
                for (var l = 0; l < searchContext.excludeAspects.length; l++) {
                    if (searchContext.excludeAspects[l] != "") {
                        searchConfig += "'" + searchContext.excludeAspects[l] + "'";
                        if (l < searchContext.excludeAspects.length - 1) {
                            searchConfig += ",";
                        }
                    }
                }
                searchConfig += ']';

                // prepare keyword for JSON.  
                var dkeywords = "";
                if (!CStudioAuthoring.Utils.isEmpty(searchContext.keywords)) {
                    dkeywords = decodeURIComponent(searchContext.keywords);
                    dkeywords = CStudioAuthoring.Utils.escapeJSONSensitiveCharacter(dkeywords);
                }

                searchConfig +=
                    ',"keyword": "' + dkeywords + '", 	' +
                    '"page": "' + searchContext.page + '",' +
                    '"pageSize": "' + searchContext.pageSize + '",' +
                    '"sortBy": "' + searchContext.sortBy + '",' +
                    '"sortAscending": "' + searchContext.sortAscending + '",' +

                    '"filters": [';
                if (searchContext.filters != null) {
                    var lastElementIdx = searchContext.filters.length - 1;

                    var seperatorFlag = false;
                    for (var i = 0; i < searchContext.filters.length; i++) {
                        name = searchContext.filters[i].qname;
                        value = searchContext.filters[i].value;
                        var startDate = searchContext.filters[i].startDate;
                        var endDate = searchContext.filters[i].endDate;
                        var useWildCard = searchContext.filters[i].useWildCard;
                        if (!useWildCard || (useWildCard == null) || !(useWildCard == "true" || useWildCard == "false") )
                            useWildCard = "true";

                        seperatorFlag = false;
                        if (value != null && value != "" && value != 'all') {
                            searchConfig += '{"qname" : "' + name + '" , "value" : "' + value + '", "useWildCard" : "' + useWildCard + '"}';
                            seperatorFlag = true;
                        } else if ( (startDate != null && startDate != "") || (endDate != null && endDate != "") ) {
                            // date filter with Range
                            startDate = (startDate != null && startDate != "") ? startDate : "";
                            endDate = (endDate != null && endDate != "") ? endDate : "";
                            searchConfig += '{"qname" : "' + name + '" , "value" : "' + value + '", "startDate" : "' + startDate + '", "endDate" : "' + endDate + '"}';
                            seperatorFlag = true;
                        }

                        if (seperatorFlag && i < lastElementIdx) {
                            searchConfig += ',';
                        }
                    }
                }
                searchConfig += '], "columns":[] }';

                YConnect.setDefaultPostHeader(false);
                YConnect.initHeader("Content-Type", "application/json; charset=utf-8");
                YConnect.asyncRequest("POST", CStudioAuthoring.Service.createServiceUri(serviceUrl), searchCb, searchConfig);

            }
        },

        /**
         * given a list of items return the topdown dependencies
         */
        lookupContentDependencies: function(site, contentItems, callback) {

            var serviceUri = this.lookupContentDependencies + "?site=" + site;

            var dependencyXml = CStudioAuthoring.Utils.createContentItemsXml(contentItems);

            var serviceCallback = {
                success: function(oResponse) {
                    var respJson = oResponse.responseText;

                    try {
                        var dependencies = eval("(" + respJson + ")");
                        callback.success(dependencies);
                    }
                    catch(err) {
                        callback.failure(err);
                    }
                },

                failure: function(response) {
                    callback.failure(response);
                }
            };

            YConnect.setDefaultPostHeader(false);
            YConnect.initHeader("Content-Type", "application/xml; charset=utf-8");
            YConnect.asyncRequest('POST', this.createServiceUri(serviceUri), serviceCallback, dependencyXml);
        },

        /**
         * Authoring Utility methods
         */
        Utils: {
            _counter: 0,
            addedCss: [],
            addedJs: [],
            noop: function() {},

            /**
             * Verifies if the user has a specific permission
             * @param permission {CStudioAuthoring.Constant} The permission to check whether user has it or not.
             * @param userPermssions {Array} The collection of permissions the user is granted
             * @return {boolean} true if user has the permission, false if not
             */
            hasPerm: function (permission, permssions) {
                if (permission instanceof CStudioConstant) {
                    var has = false;
                    CSA.Utils.each(permssions, function (index, value) {
                        if (value === permission.toString()
                        || value === permission.getValue()) {
                            has = true;
                            return false; // exit the loop
                        }
                    });
                    return has;
                } else {
                    throw ('Invalid Argument Exception: The permission to check must be of type ' + CStudioConstant);
                }
            },

            /**
             * Returns a page scope unique integer
             */
            getScopedInt: function() {
                return this._counter++;
            },
            /**
             * Returns a page scope unique identifier. May
             * be used to uniquely identify a DOM element which
             * doesn't have an id
             * @param prefix {String} A text to prepend to the unique ID
             * @return {String} A unique string within the page scope
             */
            getScopedId: function(prefix) {
                return [prefix || "", "_", this.getScopedInt()].join("");
            },
            /**
             * Utility to iterate through arrays or objects. Returning
             * false in the iterator would break the loop. The iterator
             * gets called with the value of the current iteration as context
             * unless a different context is supplied
             * @param o {Object|Array} The object or array to iterate through
             * @param iterator {Function} Function to execute upon each value in the array/object
             * @param context {Object} Inside the supplied iterator "this" will refer to it
             */
            each: function(o, iterator, context) {
                if (YAHOO.lang.isArray(o)) {
                    for (var i = 0, l = o.length; i < l; i++) {
                        var r = iterator.call(context || o[i], i, o[i]);
                        if (r === false) break;
                    }
                } else if (YAHOO.lang.isObject(o)) {
                    for (var key in o) {
                        var r = iterator.call(context || o[key], key, o[key]);
                        if (r === false) break;
                    }
                }
            },
            /**
             * True if user agent has native JSON parsing support else false
             */
            nativeUAJSONSupport: (window.JSON && JSON.toString() == '[object JSON]'),
            decode: function(jsonstring){
                if (this.nativeUAJSONSupport) {
                    return JSON.parse(jsonstring);
                } else {
                    return ( eval("(" + jsonstring + ")") );
                }
            },
            encode: function(object) {
                if (this.nativeUAJSONSupport) {
                    return JSON.stringify(object);
                } else {
                    try {
                        return YAHOO.lang.JSON.stringify(object);
                    } catch (e) {
                        throw("CStudioAuthorig.Utils.encode: YAHOO.lang.JSON is missing");
                    }
                }
            },
            isAdmin: function(){
                return (CStudioAuthoringContext.role == "admin");
            },
            getIconFWClasses: function(item){

                if (!item) return "";

                var CSA = CStudioAuthoring,
                    classes = ["status-icon"],
                    _each = CSA.Utils.each,
                    dashed = CSA.StringUtils.toDashes,
                    states = [
                        "submitted","inProgress","scheduled",
                        "deleted","disabled","asset",
                        "component","floating","document",
                        "submittedForDeletion", "inFlight"
                    ],
                    name;

                _each(states, function(i, state){
                    item[state] && classes.push( dashed(state) );
                });
                name = classes.join(" ");

                if (CSA.Utils.isItemLocked(item)) {
                    name+="-lock";
                }
                if(item.container && item.name != "index.xml") {
                    name="status-icon folder";
                }

                return name;
            },

            isItemLocked: function (item) {
                // TODO We need a better way of checking this
                return (item.lockOwner != "");
            },

            /**
             * get the width of the screen
             */
            viewportWidth: function() {
                var viewportwidth;

                if (typeof window.innerWidth != 'undefined'){
                    viewportwidth = window.innerWidth;
                }
                else if (typeof document.documentElement != 'undefined'
                    && typeof document.documentElement.clientWidth != 'undefined'
                    && document.documentElement.clientWidth != 0) {

                    viewportwidth = document.documentElement.clientWidth;
                }
                else{
                    viewportwidth = document.getElementsByTagName('body')[0].clientWidth;
                }

                return viewportwidth;
            },

            /**
             * fire the given callback when the given element becomes visible to the user
             */
            registerEventOnIsVisible: function(el, callback) {

                var y = Dom.getY(el);

                var o = new YAHOO.util.CustomEvent('element finder',

                    null,

                    true,

                    YAHOO.util.CustomEvent.FLAT

                );

                o.subscribe(callback);

                function f() {

                    var top = (document.documentElement.scrollTop ?

                        document.documentElement.scrollTop :

                        document.body.scrollTop);

                    var vpH = Dom.getViewportHeight();

                    var view = parseInt(vpH + top);

                    if ( view >= y ) {

                        o.fire(view);

                    }

                }

                Event.on(window, 'scroll', f);

            },

            /**
             * Adds a DOM event to a given element
             * @param {HTMLElement} el      the element to bind the handler to
             * @param {string}      type   the type of event handler
             * @param {function}    fn      the callback to invoke
             * @param {boolen}      capture capture or bubble phase
             */
            addEventListener: (function () {
                if (window.addEventListener) {
                    return function(el, sType, fn, capture) {
                        el.addEventListener(sType, fn, (capture));
                    };
                } else if (window.attachEvent) {
                    return function(el, sType, fn, capture) {
                        el.attachEvent("on" + sType, fn);
                    };
                } else {
                    return function(){};
                }
            })(),

            /**
             * Remove all child nodes from an element
             */
            emptyElement: function(el) {
                while ( el.hasChildNodes() ) {
                    el.removeChild(el.firstChild);
                }
                return;
            },

            /**
             * Remove all child nodes from an element that have a specific attribute value
             */
            removeSpecificElements: function(el, attr) {
                var attrVal;
                if (el.hasChildNodes()) {
                    for (var i = 0; i < el.childNodes.length; ) {
                        attrVal = el.childNodes[i].getAttribute(attr);
                        if (!!attrVal) {
                            // Attribute value is defined and it's not an empty string
                            el.removeChild(el.childNodes[i]);
                        } else {
                            i++;
                        }
                    }
                }
                return;
            },

            /**
             * utility method to check arrays for values
             */
            arrayContains: function(value, array) {
                var i = array.length;
                while (i--) {
                    if (array[i] == value) {
                        return true;
                    }
                }

                return false;
            },
            /**
             * dynamically add a javascript file
             */
            addJavascript: function(script) {
                if (!this.arrayContains(script, this.addedJs)) {

                    this.addedJs.push(script);

                    if(script.indexOf("http") == -1) {
                        script = CStudioAuthoringContext.baseUri + script;
                    }

                    /*script = (script.indexOf("?")==-1)
                        ? script + "?nocache="+new Date()
                        : script + "&nocache="+new Date();*/

                    var headID = document.getElementsByTagName("head")[0];
                    var newScript = document.createElement('script');
                    newScript.type = 'text/javascript';
                    newScript.src = script;
                    if (script.indexOf('undefined.js') === -1) {
                        headID.appendChild(newScript);
                    }
                }
            },
            /**
             * Get a script's path
             * @param scriptName - script name including the .js extension
             * @return full script path (including protocol and server name) or null
             */
            getScriptPath : function (scriptName) {
                var scripts = document.getElementsByTagName('SCRIPT'),
                    scriptName = scriptName.replace(/(.js)$/, "\\\$1"),
                    scriptNameRegExp = new RegExp(scriptName + "$"),
                    pathRegExp = new RegExp("(.*)" + scriptName + "$"),
                    path = null;

                if(scripts && scripts.length > 0) {
                    for(var i in scripts) {
                        if(scripts[i].src && scripts[i].src.match(scriptNameRegExp)) {
                            path = scripts[i].src.replace(pathRegExp, '$1');
                        }
                    }
                }
                return path;
            },
            /**
             * dynamically add a css file
             */
            addCss: function(css) {
                if (!this.arrayContains(css, this.addedCss)) {

                    this.addedCss.push(css);

                    css = CStudioAuthoringContext.baseUri + css;

                    css = (css.indexOf("?")==-1) ? 
                        css + "?nocache="+new Date() : css + "&nocache="+new Date();

                    var headID = document.getElementsByTagName("head")[0];
                    var cssNode = document.createElement('link');
                    cssNode.type = 'text/css';
                    cssNode.rel = 'stylesheet';
                    cssNode.href = css;
                    cssNode.media = 'screen';
                    headID.appendChild(cssNode);
                }
            },
            /**
             * generate uuid part
             */
            generateUUIDPart: function() {
                return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
            },
            /**
             * generate pho UUID
             */
            generateUUID: function() {
                return (this.generateUUIDPart() +
                this.generateUUIDPart() + "-" +
                this.generateUUIDPart() + "-" +
                this.generateUUIDPart() + "-" +
                this.generateUUIDPart() + "-" +
                this.generateUUIDPart() +
                this.generateUUIDPart() +
                this.generateUUIDPart());
            },

            /**
             * given a page or component path retrieves the parent path
             */
            getParentPath: function(relativePath){
                var parentPath = relativePath;
                if (relativePath) {
                    if (relativePath.lastIndexOf(".xml") > -1) {
                        var index = relativePath.lastIndexOf("/");
                        if (index > 0) {
                            var fileName = relativePath.substring(index + 1);
                            var path = relativePath.substring(0, index);
                            if (fileName === "index.xml") {
                                var secondIndex = path.lastIndexOf("/");
                                if (secondIndex > 0) {
                                    path = path.substring(0, secondIndex);
                                }
                            }
                            parentPath = path;
                        }
                    }
                }

                return parentPath;
            },

            /**
             * get query variable
             */
            getQueryVariable: function(query, variable) {
                variable = variable.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
                var regexS = "[\\?&]" + variable + "=([^&#]*)";
                var regex = new RegExp(regexS);
                var results = regex.exec(decodeURIComponent(query));

                if (results == null)
                    return "";
                else
                    return results[1];
            },

            getQueryParameterByName : function(name) {
                name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
                var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
                    results = regex.exec(location.search);
                return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
            },

            /**
             * format a date
             */
            formatDateFromString: function(dateTime, timeFormat) {

                try {
                    var updatedDateTime = "";

                    if (dateTime != undefined && dateTime != "")
                    {
                        var itemDateTime = dateTime.split('T');
                        var itemDate = itemDateTime[0].replace(/-/g, "/");
                        var itemTime = itemDateTime[1].split(':', 2);
                        var tt = "";

                        var simpleTimeFormatFlag = false;
                        var tooltipformat = false;
                        if (timeFormat != undefined && timeFormat == "tooltipformat") {
                            tooltipformat = true;
                        } else if ((timeFormat != undefined && timeFormat != "") || timeFormat == "simpleformat") {
                            simpleTimeFormatFlag = true;
                        }

                        if (itemTime[0] >= 12) {
                            tt = "P";
                            itemTime[0] = itemTime[0] - 12;
                            if (itemTime[0] == 0)
                                itemTime[0] = itemTime[0] + 12;
                            if (itemTime[0] < 10) {
                                itemTime[0] = "0" + itemTime[0];
                            }
                        }
                        else {
                            tt = "A";
                            if (itemTime[0] == 0)
                                itemTime[0] = 12;
                        }

                        var myDate = new Date(itemDate);
                        var d = myDate.getDate();
                        var m = myDate.getMonth() + 1;
                        var y = myDate.getFullYear();

                        if (m < 10)
                            m = "0" + m;

                        if (d < 10)
                            d = "0" + d;

                        if (simpleTimeFormatFlag) { // if simple time format pass mm/dd/yy format

                            var newDate = m + "/" + d + "/" + y;
                            updatedDateTime = newDate;

                        } else if (tooltipformat) { //date format for tooltip

                            var year = y + "";
                            var newDate = (isNaN(m)?m:parseInt(m, 10)) + "/" +
                                (isNaN(d)?d:parseInt(d, 10)) + "/" +
                                year.substr(2);
                            updatedDateTime = newDate + " " + itemTime[0] + ":" + itemTime[1] + "" + tt + "  ";

                        } else {

                            var newDate = m + "/" + d;
                            updatedDateTime = newDate + " " + itemTime[0] + ":" + itemTime[1] + "" + tt + "  ";

                        }

                    } else {

                        var myDate = new Date(itemDate);
                        var d = myDate.getDate();
                        var m = myDate.getMonth() + 1;
                        var y = myDate.getFullYear();

                        if (m < 10)
                            m = "0" + m;

                        if (d < 10)
                            d = "0" + d;

                        var newDate = m + "/" + d;

                    }

                    return updatedDateTime;
                }
                catch(err) {
                    return dateTime;
                }
            },

            formatDateFromStringNullToEmpty: function(dateTime, timeFormat) {
                if ( (dateTime == "null") || (dateTime == null) || (dateTime == undefined) || (dateTime == "") )
                    return "";
                else
                    return this.formatDateFromString(dateTime, timeFormat);
            },


            /**
             * create yui based datepicker
             */
            yuiCalendar: function(sourceElement, eventToFire, TargetElement, afterRender) {

                //Dom.get("settime").checked = true;

                var datePicker = Dom.get(sourceElement);
                over_cal = false; // flag for blur events
                targetField = Dom.get(TargetElement);

                Event.addListener(sourceElement, eventToFire, function() {

                    //create a calendar outer container and apply styles to position absolute
                    var calendarContainer = document.createElement("div");
                    calendarContainer.id = "calendarContainer";
                    calendarContainer.className = "calendarContainer";

                    //create a wrapper to display the calendar
                    var calendarWrapper = document.createElement("div");
                    calendarWrapper.id = "calendarWrapper";

                    //append the wrapper inside the container
                    calendarContainer.appendChild(calendarWrapper);

                    //append container to source elements parent node
                    datePicker.parentNode.appendChild(calendarContainer);

                    //init calendar
                    newCalendar = new YAHOO.widget.Calendar(sourceElement, "calendarWrapper");
                    /** added to prevent past date selection*/
                    var todaysDate = new Date();
                    todaysDate = (todaysDate.getMonth()+ 1) + "/" + todaysDate.getDate()+"/" + todaysDate.getFullYear();
                    newCalendar.cfg.setProperty("mindate", todaysDate);

                    //attach select event and listeners
                    newCalendar.selectEvent.subscribe(getDate, targetField, true);
                    newCalendar.renderEvent.subscribe(setupListeners, newCalendar, true);

                    //attach calendar events
                    Event.addListener(sourceElement, 'blur', hideCalendar);
                    Event.addListener(sourceElement, eventToFire, showCalendar);

                    newCalendar.render();

                    function setupListeners() {
                        Event.addListener('calendarContainer', 'mouseover', function() {
                            over_cal = true;
                        });

                        Event.addListener('calendarContainer', 'mouseout', function() {
                            over_cal = false;
                        });
                    }

                    function hideCalendar() {
                        if (!over_cal) {
                            Dom.setStyle('calendarContainer', 'display', 'none');
                        }
                    }


                    function getDate(type, args, obj) {

                        var rowDate = args.toString();
                        var splitRowDate = rowDate.split(",");
                        var selYear = splitRowDate[0];
                        var selMonth = splitRowDate[1];
                        var selDay = splitRowDate[2];

                        var selectedDate = selMonth + "/" + selDay + "/" + selYear;

                        targetField.value = selectedDate;
                        Dom.get("settime").checked = true;
                        Dom.get("datepicker").style.border = "1px solid #0176B1";
                        Dom.get("datepicker").style.color = "#000000";
                        Dom.get("timepicker").style.border = "1px solid #0176B1";
                        Dom.get("timepicker").style.color = "#000000";
                        //reset and hide calendar
                        over_cal = false;
                        hideCalendar();
                    }

                    function showCalendar(ev) {
                        var tar = Event.getTarget(ev);
                        cur_field = tar;

                        Dom.setStyle('calendarContainer', 'display', 'block');

                    }

                });

            },

            /**
             * Bind an element (input) to a series of events. Whenever any of these elements are triggered
             * the cursor position will be stored in the element.
             *
             * Return: the element (or false, if the element doesn't exist)
             */

            initCursorPosition : function (elementId, events) {

                var getSelectionStart = function (o) {
                    if (o.createTextRange) {
                        var r = document.selection.createRange().duplicate()
                        r.moveEnd('character', o.value.length)
                        if (r.text == '') return o.value.length
                        return o.value.lastIndexOf(r.text)
                    } else return o.selectionStart
                };

                var addCursorPosListener = function (el, event) {
                    YEvent.addListener(el, event, function(){
                        var cursorPos = getSelectionStart(el);
                        el.setAttribute('data-cursor', cursorPos);
                    });
                }

                var el = YDom.get(elementId);

                if (el) {
                    if (events.constructor.toString().indexOf("Array") != -1 &&
                        events.length > 0) {
                        for (var i = 0; i < events.length; i++) {
                            addCursorPosListener(el, events[i]);
                        }
                    }
                    return el;
                } else {
                    return false;
                }
            },

            /**
             * create timepicker increment and decrement helper
             * that increse the input time
             */

            textFieldTimeIncrementHelper : function(triggerEl, targetEl, event, keyCode) {

                var incrementHandler = function(type, args) {

                    /* CSTUDIO-401: Removing default action when using arrow keys
                     if (args) {
                     var e = args[1];    // the actual event object
                     YEvent.preventDefault(e);   // Prevent the default action
                     }
                     */

                    var timePicker = YDom.get(targetEl),
                        timeValue = timePicker.value,
                        cursorPosition;

                    if( timeValue != 'Time...' && timeValue != ''){
                        var timeValueArray = timeValue.split(/[: ]/),
                            hourValue = timeValueArray[0],
                            minuteValue = timeValueArray[1],
                            secondValue = timeValueArray[2],
                            amPmValue = timeValueArray[3];

                        cursorPosition = timePicker.getAttribute('data-cursor');

                        if( cursorPosition > -1 && cursorPosition < 3){

                            if(hourValue.charAt(0) == '0')
                                hourValue = hourValue.charAt(1);

                            hourValue = (parseInt(hourValue)%12)+1;

                            if(hourValue.toString().length < 2)
                                hourValue =	"0"+hourValue;
                            else
                                hourValue = hourValue.toString();
                        }else if(cursorPosition > 2 && cursorPosition < 6){

                            if(minuteValue.charAt(0) == '0')
                                minuteValue = minuteValue.charAt(1);

                            if(parseInt(minuteValue) == 59){
                                minuteValue = (parseInt(minuteValue)%59);
                            }else{
                                minuteValue = (parseInt(minuteValue)%59)+1;
                            }

                            if(minuteValue.toString().length < 2)
                                minuteValue =	"0"+minuteValue;
                            else
                                minuteValue = minuteValue.toString();

                        }else if(cursorPosition > 5 && cursorPosition < 9){
                            if(secondValue.charAt(0) == '0')
                                secondValue = secondValue.charAt(1);

                            if(parseInt(secondValue) == 59){
                                secondValue = (parseInt(secondValue)%59);
                            }else{
                                secondValue = (parseInt(secondValue)%59)+1;
                            }

                            if(secondValue.toString().length < 2)
                                secondValue =	"0"+secondValue;
                            else
                                secondValue = secondValue.toString();
                        }else if(cursorPosition > 8){
                            amPmValue = (amPmValue == 'a.m.') ? 'p.m.' : 'a.m.';
                        }

                        timePicker.value = hourValue+":"+minuteValue+":"+secondValue+" "+amPmValue;
                    }
                };

                YEvent.addListener(triggerEl, event, incrementHandler);

                if (keyCode) {
                    // Add keyboard support, incomplete --CSTUDIO-401
                    klInc = new YAHOO.util.KeyListener(targetEl, { keys: keyCode}, incrementHandler);
                    klInc.enable();
                }
            },

            /**
             * create timepicker decrement and decrement helper
             * that decrese the input time
             */

            textFieldTimeDecrementHelper : function(triggerEl, targetEl, event, keyCode) {

                var decrementHandler = function(type, args) {

                    /* CSTUDIO-401: Removing default action when using arrow keys
                     if (args) {
                     var e = args[1];    // the actual event object
                     YEvent.preventDefault(e);   // Prevent the default action
                     }
                     */

                    var timePicker = YDom.get(targetEl),
                        timeValue = timePicker.value,
                        cursorPosition;

                    if( timeValue != 'Time...' && timeValue != ''){
                        var timeValueArray = timeValue.split(/[: ]/),
                            hourValue = timeValueArray[0],
                            minuteValue = timeValueArray[1],
                            secondValue = timeValueArray[2],
                            amPmValue = timeValueArray[3];

                        cursorPosition = timePicker.getAttribute('data-cursor');

                        if( cursorPosition > -1 && cursorPosition < 3){

                            if(hourValue.charAt(0) == '0')
                                hourValue = hourValue.charAt(1);

                            if(parseInt(hourValue) == 1){
                                hourValue = 12;
                            }else{
                                hourValue = (parseInt(hourValue)-1)%12;
                            }

                            if(hourValue.toString().length < 2)
                                hourValue =	"0"+hourValue;
                            else
                                hourValue = hourValue.toString();
                        }else if(cursorPosition > 2 && cursorPosition < 6){

                            if(minuteValue.charAt(0) == '0')
                                minuteValue = minuteValue.charAt(1);

                            if(parseInt(minuteValue) == 0){
                                minuteValue = 59;
                            }else{
                                minuteValue = (parseInt(minuteValue)-1)%59;
                            }

                            if(minuteValue.toString().length < 2)
                                minuteValue =	"0"+minuteValue;
                            else
                                minuteValue = minuteValue.toString();

                        }else if(cursorPosition > 5 && cursorPosition < 9){
                            if(secondValue.charAt(0) == '0')
                                secondValue = secondValue.charAt(1);

                            if(parseInt(secondValue) == 0){
                                secondValue = 59;
                            }else{
                                secondValue = (parseInt(secondValue)-1)%59;
                            }

                            if(secondValue.toString().length < 2)
                                secondValue =	"0"+secondValue;
                            else
                                secondValue = secondValue.toString();
                        }else if(cursorPosition > 8){
                            if(amPmValue == 'a.m.')
                                amPmValue = 'p.m.';
                            else
                                amPmValue = 'a.m.';
                        }

                        timePicker.value = hourValue+":"+minuteValue+":"+secondValue+" "+amPmValue;
                    }
                };

                YEvent.addListener(triggerEl, event, decrementHandler);

                if (keyCode) {
                    // Add keyboard support, incomplete --CSTUDIO-401
                    klDec = new YAHOO.util.KeyListener(targetEl, { keys: keyCode}, decrementHandler);
                    klDec.enable();
                }
            },

            /**
             * create timepicker that format the input time
             */
            textFieldTimeHelper: function(sourceElement, eventToFire, targetElement) {
                //Dom.get("settime").checked = true;
                var Dom = YAHOO.util.Dom,
                    Event = YAHOO.util.Event,
                    timePicker = Dom.get(sourceElement);
                //patterns to match the time format
                var timeParsePatterns = [
                    // Now
                    {
                        re: /^now/i,
                        example: new Array('now'),
                        handler: function() {
                            return new Date();
                        }
                    },
                    // p.m.
                    {
                        re: /(\d{1,2}):(\d{1,2}):(\d{1,2})(?:p| p)/,
                        example: new Array('9:55:00 pm', '12:55:00 p.m.', '9:55:00 p', '11:5:10pm', '9:5:1p'),
                        handler: function(bits) {
                            var d = new Date();
                            var h = parseInt(bits[1], 10);
                            d.setHours(h);
                            d.setMinutes(parseInt(bits[2], 10));
                            d.setSeconds(parseInt(bits[3], 10));
                            return d + "~p.m.";
                        }
                    },
                    // p.m., no seconds
                    {
                        re: /(\d{1,2}):(\d{1,2})(?:p| p)/,
                        example: new Array('9:55 pm', '12:55 p.m.', '9:55 p', '11:5pm', '9:5p'),
                        handler: function(bits) {
                            var d = new Date();
                            var h = parseInt(bits[1], 10);
                            d.setHours(h);
                            d.setMinutes(parseInt(bits[2], 10));
                            d.setSeconds(0);
                            return d + "~p.m.";
                        }
                    },
                    // p.m., hour only
                    {
                        re: /(\d{1,2})(?:p| p)/,
                        example: new Array('9 pm', '12 p.m.', '9 p', '11pm', '9p'),
                        handler: function(bits) {
                            var d = new Date();
                            var h = parseInt(bits[1], 10);
                            d.setHours(h);
                            d.setMinutes(0);
                            d.setSeconds(0);
                            return d + "~p.m.";
                        }
                    },
                    // hh:mm:ss
                    {
                        re: /(\d{1,2}):(\d{1,2}):(\d{1,2})/,
                        example: new Array('9:55:00', '19:55:00', '19:5:10', '9:5:1', '9:55:00 a.m.', '11:55:00a'),
                        handler: function(bits) {
                            var d = new Date();
                            var h = parseInt(bits[1], 10);
                            if (h == 12) {
                                //h = 0;
                            }
                            d.setHours(h);
                            d.setMinutes(parseInt(bits[2], 10));
                            d.setSeconds(parseInt(bits[3], 10));
                            return d + "~a.m.";
                        }
                    },
                    // hh:mm
                    {
                        re: /(\d{1,2}):(\d{1,2})/,
                        example: new Array('9:55', '19:55', '19:5', '9:55 a.m.', '11:55a'),
                        handler: function(bits) {
                            var d = new Date();
                            var h = parseInt(bits[1], 10);
                            if (h == 12) {
                                //h = 0;
                            }
                            d.setHours(h);
                            d.setMinutes(parseInt(bits[2], 10));
                            d.setSeconds(0);
                            return d + "~a.m.";
                        }
                    },
                    // hhmmss
                    {
                        re: /(\d{1,6})/,
                        example: new Array('9', '9a', '9am', '19', '1950', '195510', '0955'),
                        handler: function(bits) {
                            var d = new Date();
                            var h = bits[1].substring(0, 2)
                            var m = parseInt(bits[1].substring(2, 4), 10);
                            var s = parseInt(bits[1].substring(4, 6), 10);
                            if (isNaN(m)) {
                                m = 0;
                            }
                            if (isNaN(s)) {
                                s = 0;
                            }
                            if (h == 12) {
                                //h = 0;
                            }
                            d.setHours(parseInt(h, 10));
                            d.setMinutes(parseInt(m, 10));
                            d.setSeconds(parseInt(s, 10));
                            return d + "~a.m.";
                        }
                    }
                ];
                var isShiftPlusTabPressed = false;
                var isTabPressed = false;
                // attach the event to call the main function
                Event.addListener(sourceElement, eventToFire, function() {
                    //parse the value using patterns and retrive the date with format
                    var inputTime = parseTimeString(this.value);

                    if(inputTime == undefined) {
                        alert('( '+this.value+' ) is not a valid time format, please provide a valid time');
                        Dom.get(targetElement).value = "";
                        var oTimeIncBtn = Dom.get('timeIncrementButton');
                        if (!isShiftPlusTabPressed && isTabPressed && oTimeIncBtn) {
                            oTimeIncBtn.focus();
                            isTabPressed = false;
                        } else {
                            var oDatePicker = Dom.get('datepicker');
                            if (isShiftPlusTabPressed && oDatePicker) {
                                oDatePicker.focus();
                                isShiftPlusTabPressed = false;
                            }
                        }
                        return;
                    } else {
                        var finalTimeFormat = inputTime.split("~");
                        var timeStamp = setTimeStamp(new Date(finalTimeFormat[0]), finalTimeFormat[1]);
                        //Check for 12 hours format time
                        var timeSplit = timeStamp.split(":");
                        if (timeSplit.length == 3) {
                            var hours = parseInt(timeSplit[0], 10);
                            if (hours == 0 || hours > 12) {
                                alert('( '+this.value+' ) is not a valid time format, please provide a valid time');
                                Dom.get("timepicker").focus();
                                var oTimeIncBtn = Dom.get('timeIncrementButton');
                                if (!isShiftPlusTabPressed && isTabPressed && oTimeIncBtn) {
                                    oTimeIncBtn.focus();
                                    isTabPressed = false;
                                } else {
                                    var oDatePicker = Dom.get('datepicker');
                                    if (isShiftPlusTabPressed && oDatePicker) {
                                        oDatePicker.focus();
                                        isShiftPlusTabPressed = false;
                                    }
                                }
                                return;
                            }
                        }
                        //set the value
                        Dom.get(targetElement).value = timeStamp;
                    }

                })

                //on focus of the target element clean the field
                Event.addListener(targetElement, "focus", function() {
                    Dom.get("settime").checked = true;
                    Dom.get("datepicker").style.border = "1px solid #0176B1";
                    Dom.get("datepicker").style.color = "#000000";
                    Dom.get("timepicker").style.border = "1px solid #0176B1";
                    Dom.get("timepicker").style.color = "#000000";
                    if (Dom.get(targetElement).value == "Time...")
                        Dom.get(targetElement).value = "";

                })

                //on focus of the target element clean the field
                Event.addListener(targetElement, "keypress", function(evt) {
                    if (evt.shiftKey && evt.keyCode == 9) {
                        isShiftPlusTabPressed = true;
                    } else {
                        isShiftPlusTabPressed = false;
                    }
                    if (evt.keyCode == 9) {
                        isTabPressed = true;
                    } else {
                        isTabPressed = false;
                    }
                })

                //Parses a string to figure out the time it represents
                function parseTimeString(s) {
                    for (var i = 0; i < timeParsePatterns.length; i++) {
                        var re = timeParsePatterns[i].re;
                        var handler = timeParsePatterns[i].handler;
                        var bits = re.exec(s);
                        if (bits) {
                            return handler(bits);
                        }
                    }
                }

                // set the timestamp and format for the output
                function setTimeStamp(timeStamp, timeFormat) {
                    return padAZero(timeStamp.getHours())
                        + ':'
                        + padAZero(timeStamp.getMinutes())
                        + ':'
                        + padAZero(timeStamp.getSeconds())
                        + ' '
                        + timeFormat;
                }


                //padd a zero if single digit found on hour/minute/seconds
                function padAZero(s) {
                    s = s.toString();
                    if (s.length == 1) {
                        return '0' + s;
                    } else {
                        return s;
                    }
                }


            },

            /**
             * return true if value starts with second valuie
             */
            startsWith: function(value, startsWith) {
                return (value.match("^" + startsWith) == startsWith);
            },

            /**
             *  ends with
             */
            endsWith: function(stringValue, match) {

                if (match != null) {
                    return stringValue.length >= match.length && stringValue.substr(stringValue.length - match.length) == match;
                }
                else {
                    return -1;
                }
            },

            /**
             * close the current window
             */
            closeWindow: function() {
                window.open("", "_self");
                window.close();
            },

            /**
             * return the Y Position of an element
             */
            getY: function(el) {
                var val = 0;

                while(el != null ) {
                    val += el.offsetTop;
                    el = el.offsetParent;
                }

                return val;
            },

            /**
             * return the x,y coords of an element
             */
            findPos: function(obj) {
                var curleft = curtop = 0;

                if (obj.offsetParent) {

                    do {
                        curleft += obj.offsetLeft;
                        curtop += obj.offsetTop;
                    } while (obj = obj.offsetParent);
                }

                return [curleft,curtop];
            },

            /**
             * Get elements by class name
             * original code: http://code.google.com/u/robnyman/
             * License: MIT
             */
            getElementsByClassName: function(className, tag, elm) {

                if (document.getElementsByClassName) {
                    getElementsByClassName = function (className, tag, elm) {
                        elm = elm || document;
                        var elements = elm.getElementsByClassName(className),
                            nodeName = (tag) ? new RegExp("\\b" + tag + "\\b", "i") : null,
                            returnElements = [],
                            current;
                        for (var i = 0, il = elements.length; i < il; i += 1) {
                            current = elements[i];
                            if (!nodeName || nodeName.test(current.nodeName)) {
                                returnElements.push(current);
                            }
                        }
                        return returnElements;
                    };
                }
                else if (document.evaluate) {
                    getElementsByClassName = function (className, tag, elm) {
                        tag = tag || "*";
                        elm = elm || document;
                        var classes = className.split(" "),
                            classesToCheck = "",
                            xhtmlNamespace = "http://www.w3.org/1999/xhtml",
                            namespaceResolver = (document.documentElement.namespaceURI === xhtmlNamespace) ? xhtmlNamespace : null,
                            returnElements = [],
                            elements,
                            node;
                        for (var j = 0, jl = classes.length; j < jl; j += 1) {
                            classesToCheck += "[contains(concat(' ', @class, ' '), ' " + classes[j] + " ')]";
                        }
                        try {
                            elements = document.evaluate(".//" + tag + classesToCheck, elm, namespaceResolver, 0, null);
                        }
                        catch (e) {
                            elements = document.evaluate(".//" + tag + classesToCheck, elm, null, 0, null);
                        }
                        while ((node = elements.iterateNext())) {
                            returnElements.push(node);
                        }
                        return returnElements;
                    };
                }
                else {
                    getElementsByClassName = function (className, tag, elm) {
                        tag = tag || "*";
                        elm = elm || document;
                        var classes = className.split(" "),
                            classesToCheck = [],
                            elements = (tag === "*" && elm.all) ? elm.all : elm.getElementsByTagName(tag),
                            current,
                            returnElements = [],
                            match;
                        for (var k = 0, kl = classes.length; k < kl; k += 1) {
                            classesToCheck.push(new RegExp("(^|\\s)" + classes[k] + "(\\s|$)"));
                        }
                        for (var l = 0, ll = elements.length; l < ll; l += 1) {
                            current = elements[l];
                            match = false;
                            for (var m = 0, ml = classesToCheck.length; m < ml; m += 1) {
                                match = classesToCheck[m].test(current.className);
                                if (!match) {
                                    break;
                                }
                            }
                            if (match) {
                                returnElements.push(current);
                            }
                        }
                        return returnElements;
                    };
                }

                return getElementsByClassName(className, tag, elm);
            },

            /**
             * return true if value is undefined, null or an empty string
             */
            isEmpty: function(value) {
                return (!value || value == null || value == "");
            },

            /**
             * given a list of content items, return an XML
             */
            createContentItemsXml: function(contentItems) {

                var xmlString = '<items>';

                for (var i = 0; i < contentItems.length; i++) {
                    xmlString = xmlString + '<item uri="' + contentItems[i].uri + '"/>';
                }

                xmlString += '</items>';

                return xmlString;
            },

            /**
             * given a list of content items, return an json
             */
            createContentItemsJson: function(contentItems) {

                var itemsJson = "[ ";

                for (var i = 0; i < contentItems.length; i++) {
                    var itemJson;
                    if (i > 0) itemsJson = itemsJson + ",";
                    itemJson = "{ uri : \"" + contentItems[i].uri + "\" }";
                    itemsJson = itemsJson + " " + itemJson;
                }
                itemsJson = itemsJson + " ]";
                return itemsJson;
            },


            /**
             * when caching content TOs we want a URI we can count on.  Not all content is
             * referenced the same way so building composite key allows us to have simple / common
             * approach to loading and storing content without concern for the type
             */
            createContentTOId: function(contentTO) {
                var id = (contentTO.id) ? contentTO.id : "";
                var noderef = (contentTO.id) ? "" : ""; // this is hold over code from client, not 2.x
                var uri = (contentTO.uri) ? contentTO.uri : "";

                return id + "-" + noderef + "-" + uri;
            },

            /**
             * get full name from search result content TO
             */
            getAuthorFullNameFromContentTOItem: function(contentTOItem) {
                var lastName = (!(CStudioAuthoring.Utils.isEmpty(contentTOItem.userLastName)))? contentTOItem.userLastName : "";
                var separator = ( !(CStudioAuthoring.Utils.isEmpty(contentTOItem.userLastName))
                && !(CStudioAuthoring.Utils.isEmpty(contentTOItem.userFirstName)) )? ", " : "";
                var firstName = (!(CStudioAuthoring.Utils.isEmpty(contentTOItem.userFirstName)))? contentTOItem.userFirstName : "";

                return lastName + separator + firstName;
            },

            /**
             * for a given tree node look up the last item
             */
            getContentItemStatus: function(contentTO, navbarStatus) {

                var status = "";

                if (contentTO.deleted == true) {
                    return status + CMgs.format(siteDropdownLangBundle, "statusDeleted");
                } else if (contentTO.submittedForDeletion == true) {
                    if(contentTO.scheduled ==  true){
                        status = status + CMgs.format(siteDropdownLangBundle, "statusScheduledForDelete");
                    } else {
                        status = status + CMgs.format(siteDropdownLangBundle, "statusSubmittedForDelete");
                    }

                    //Disabled string not required in status to show on nav bar
                    if (!navbarStatus && contentTO.disabled == true) {
                        status = status + " " + CMgs.format(siteDropdownLangBundle, "statusAndDisabled");
                    }
                    return status;
                } else if (contentTO.inFlight == true) {
                    status = status + CMgs.format(siteDropdownLangBundle, "statusProcessing");
                    //Disabled string not required in status to show on nav bar
                    if (!navbarStatus && contentTO.disabled == true) {
                        status = status + " " + CMgs.format(siteDropdownLangBundle, "statusAndDisabled");
                    }
                    return status;
                } else if (contentTO.inProgress == true) {
                    status = status + CMgs.format(siteDropdownLangBundle, "statusInProgress");
                } else if (contentTO.live == true) {
                    status = status + CMgs.format(siteDropdownLangBundle, "statusLive");
                }

                if (contentTO.submitted == true) {
                    if (contentTO.inProgress == true) {
                        status = "";
                    }
                    else {
                        if (status.length > 0) {
                            status = status + " " +CMgs.format(siteDropdownLangBundle, "statusAnd") + " ";
                        }
                    }

                    status = status + CMgs.format(siteDropdownLangBundle, "statusSubmitted");
                }

                if (contentTO.scheduled == true) {
                    if (contentTO.inProgress == true) {
                        status = "";
                    }

                    if (status.length > 0) {
                        status = status + " " +CMgs.format(siteDropdownLangBundle, "statusAnd") + " ";
                    }

                    status = status + CMgs.format(siteDropdownLangBundle, "statusScheduled");
                }

                //Disabled string not required in status to show on nav bar
                if (!navbarStatus && contentTO.disabled == true) {
                    if (status.length > 0) {
                        status = status + " " +CMgs.format(siteDropdownLangBundle, "statusAnd") + " ";
                    }

                    status = status + CMgs.format(siteDropdownLangBundle, "statusDisabled");
                }

                if (status == "") {
                    status = CMgs.format(siteDropdownLangBundle, "statusLive");
                }

                return status;
            },

            /**
             * given a node, return the proper classes for the item's state
             */
            getContentItemClassName: function(contentTO) {

                var name = "status-icon ";
                if (contentTO.component != true) {
                    if (contentTO.document == true)
                        name = name + " document ";
                    else
                        name = name + "page";

                    if (contentTO.floating == true) {
                        name = name + " floating ";
                    }
                    if (contentTO.deleted == true||contentTO.submittedForDeletion==true) {
                        name = name + " deleted ";
                    }
                    if ( (contentTO.submitted == true || contentTO.scheduled == true) && contentTO.floating == false) {
                        if (contentTO.submitted == true) {
                            name = name + " submitted ";
                        }
                        if (contentTO.scheduled == true) {
                            name = name + " scheduled ";
                        }
                    }
                    else {
                        if (contentTO.deleted != true && contentTO.inProgress == true) {
                            name = name + " in-progress ";
                        }
                    }
                }
                else {
                    if (contentTO.container == true) {
                        name = " parentFolder ";
                    }
                    else {
                        if (contentTO.component == true) {
                            name = name + " component ";
                            if (contentTO.deleted == true||contentTO.submittedForDeletion) {
                                name = name + " deleted ";
                            }
                            if (contentTO.submitted == true || contentTO.scheduled == true) {
                                if (contentTO.submitted == true) {
                                    name = name + " submitted ";
                                }

                                if (contentTO.scheduled == true) {
                                    name = name + " scheduled ";
                                }
                            }
                            else {
                                if (contentTO.deleted != true && contentTO.inProgress == true) {
                                    name = name + " in-progress";
                                }
                            }
                        }
                    }
                }

                if(contentTO.lockOwner != "") {
                    name = name + "-lock";
                }


                name += " acn";

                if (contentTO.component != true) {
                    if (contentTO.document == true)
                        name = name + "-document";
                    else
                        name = name + "-page";

                    if (contentTO.floating == true) {
                        name = name + "-floating";
                    }
                    if (contentTO.deleted == true||contentTO.submittedForDeletion==true) {
                        name = name + "-deleted";
                    }
                    if ( (contentTO.submitted == true || contentTO.scheduled == true) && contentTO.floating == false) {
                        if (contentTO.submitted == true) {
                            name = name + "-submitted";
                        }
                        if (contentTO.scheduled == true) {
                            name = name + "-scheduled";
                        }
                    }
                    else {
                        if (contentTO.deleted != true && contentTO.inProgress == true) {
                            name = name + "-progress";
                        }
                    }
                }
                else {
                    if (contentTO.container == true) {
                        name = "parentFolder";
                    }
                    else {
                        if (contentTO.component == true) {
                            name = name + "-component";
                            if (contentTO.deleted == true||contentTO.submittedForDeletion) {
                                name = name + "-deleted";
                            }
                            if (contentTO.submitted == true || contentTO.scheduled == true) {
                                if (contentTO.submitted == true) {
                                    name = name + "-submitted";
                                }

                                if (contentTO.scheduled == true) {
                                    name = name + "-scheduled";
                                }
                            }
                            else {
                                if (contentTO.deleted != true && contentTO.inProgress == true) {
                                    name = name + "-progress";
                                }
                            }
                        }
                    }
                }

                if(contentTO.lockOwner != "") {
                    name = name + "-lock";
                }

                if (contentTO.disabled == true) {
                    name = name + " strike-dashboard-item";
                }

                return name;
            },

            getScheduledDateTimeUI: function (dateValue, timeValue) {
                var dateValueArray = dateValue.split("/");
                var timeValueArray = timeValue.split(" ");
                var timeSplit = timeValueArray[0].split(":");

                var schedDate = new Date(dateValueArray[2], dateValueArray[0] - 1, dateValueArray[1], timeSplit[0], timeSplit[1], timeSplit[2], "");

                ///////////////////////////////////////////////////////////////////////////////////
                // getMonth is zero based, so adding 1 with it to show proper month in the html. //
                //////////////////////////////////////////////////////////////////////////////////
                var schedDateMonth = schedDate.getMonth() + 1;
                if (schedDateMonth < 10) {
                    schedDateMonth = "0" + schedDateMonth;
                }

                var schedDateDay = schedDate.getDate();
                if (schedDateDay < 10) {
                    schedDateDay = "0" + schedDateDay;
                }

                var meridian = "AM";
                if (timeValueArray[1] == "p.m.") {
                    meridian = "PM";
                }

                var hours = parseInt(timeSplit[0], 10);
                if (hours < 10) {
                    hours = "0" + hours;
                }

                var mins = parseInt(timeSplit[1], 10);
                if (mins < 10) {
                    mins = "0" + mins;
                }

                var scheduledDate = schedDateMonth + '/' + schedDateDay + ' ' + hours + ":" + mins + " " + meridian;

                return scheduledDate;
            },

            buildToolTip: function (itemNameLabel, label, style, status, editedDate, modifier, lockOwner, schedDate) {
                var schedInfo = "";
                if (schedDate) {
                    schedInfo = CStudioAuthoring.StringUtils.format(["</tr><tr>",
                        "<td class='acn-width80'>Scheduled: </td>",
                        "<td class='acn-width200'>{0}</td>"].join(""), schedDate);
                }

                return CStudioAuthoring.StringUtils.format(["<table class='width100 acn-tooltip'>",
                    "<tr>",
                    "<td class='acn-width80'><strong>{0}:</strong></td>",
                    "<td class='acn-width200'><div class='acn-width200' style='word-wrap: break-word;'>{1}</div></td>",
                    "</tr>",
                    /*"<tr><td class='acn-width83'><strong>Status:</strong></td>",
                    "<td class='acn-width200'><span class='{2}'></span>",
                    "<span style='padding-left:2px;'>{3}</span></td></tr>",*/
                    "<tr>",
                    "<td class='acn-width80'><strong>Last Edited:</strong> </td>",
                    "<td class='acn-width200'>{4}</td>",
                    "</tr><tr>",
                    "<td class='acn-width80'><strong>Edited by:</strong> </td>",
                    "<td class='acn-width200'>{5}</td>",
                    "</tr><tr>",
                    "<td class='acn-width80'><strong>Locked by:</strong> </td>",
                    "<td class='acn-width200'>{6}</td>",
                    schedInfo,
                    "</tr>",
                    "</table>"].join(""), itemNameLabel, label, style, status, editedDate, modifier, lockOwner);
            },

            getTooltipContent: function(item) {
                var status = this.getContentItemStatus(item);
                var style = this.getIconFWClasses(item);
                var internalName = item.internalName;
                var label = "";
                var formattedEditDate = "";
                var modifier = "";
                var fileName = "";
                var formattedSchedDate = "";
                var retTitle = "";
                var itemNameLabel = "Page";
                var lockOwner = "";

                if (item.component) {
                    itemNameLabel = "Component";
                } else if (item.document) {
                    itemNameLabel = "Document";
                }

                if (internalName == "crafter-level-descriptor.level.xml") {
                    internalName = "Section Defaults";
                }

                if (item.newFile) {
                    label = internalName + "*";
                } else {
                    label = internalName;
                }

                // this API will replace double quotes with ASCII character
                // to resolve page display issue
                label = CStudioAuthoring.Utils.replaceWithASCIICharacter(label);

                if (item.container == true) {
                    fileName = item.name;
                }

                //spilt status and made it as comma seperated items.
                var statusStr = status;
                if (status.indexOf(" and ") != -1) {
                    var statusArray = status.split(" and ");
                    if (statusArray &&  statusArray.length >= 2) {
                        statusStr = "";
                        for (var statusIdx=0; statusIdx<statusArray.length; statusIdx++) {
                            if (statusIdx == (statusArray.length - 1)) {
                                statusStr += statusArray[statusIdx];
                            } else {
                                statusStr += statusArray[statusIdx] + ", ";
                            }
                        }
                    }
                }

                if (item.userFirstName != undefined && item.userLastName != undefined) {
                    modifier = item.userFirstName + " " + item.userLastName;
                }

                if (item.lockOwner != undefined) {
                    lockOwner = item.lockOwner;
                }

                if (item.lastEditDateAsString != "" && item.lastEditDateAsString != undefined) {
                    formattedEditDate = this.formatDateFromString(item.lastEditDateAsString, "tooltipformat");
                } else if (item.eventDate != "" && item.eventDate != undefined) {
                    formattedEditDate = this.formatDateFromString(item.eventDate, "tooltipformat");
                }

                if (item.scheduled == true) {
                    formattedSchedDate = this.formatDateFromString(item.scheduledDate, "tooltipformat");

                    retTitle = this.buildToolTip(itemNameLabel, label,
                        style,
                        statusStr,
                        formattedEditDate,
                        modifier,
                        lockOwner,
                        formattedSchedDate);
                } else {

                    retTitle = this.buildToolTip(itemNameLabel, label,
                        style,
                        statusStr,
                        formattedEditDate,
                        modifier,
                        lockOwner);
                }
                return retTitle;
            },

            showLoadingImage: function(elementId) {
                if (YDom.get(elementId + "-loading")) {
                    YDom.get(elementId + "-loading").style.display = "block";
                }
            },

            hideLoadingImage: function(elementId) {
                if (YDom.get(elementId + "-loading")) {
                    YDom.get(elementId + "-loading").style.display = "none";
                }
            },

            /** takes String as param and escapes all JSON sensitive character in that String **/
            escapeJSONSensitiveCharacter: function(str) {
                if (CStudioAuthoring.Utils.isEmpty(str))
                    return str;
                return str.replace(/\\/g, "\\\\").replace(/\n/g, "\\n").replace(/\r/g, "\\r").replace(/\t/g, "\\t").replace(/\f/g, "\\f").replace(/"/g,"\\\"").replace(/'/g,"\\\'").replace(/\&/g, "\\&");
            },

            replaceWithASCIICharacter: function(str) {
                if (CStudioAuthoring.Utils.isEmpty(str))
                    return str;
                return str.replace(/"/g, "&#34;");
            },

            setDefaultFocusOn: function(focusedButton) {
                if (!focusedButton) return;
                focusedButton.focus();
                /* 
                 * after dialog rendered, default focused button outline style not displaying in firefox4
                 * This code block adds focus outline manually and on blur, we should remove added styles.
                 */
                var oDiv = document.createElement("div");
                oDiv.style.display="none";
                var stylePrefix = "#none"
                if (focusedButton.id != undefined && focusedButton.id != "") {
                    stylePrefix = "#" + focusedButton.id;
                } else if (focusedButton.className != undefined && focusedButton.className != "") {
                    stylePrefix = "." + focusedButton.className;
                }
                oDiv.innerHTML = "<style>" + stylePrefix + "::-moz-focus-inner { border:1px dotted; }</style>";
                focusedButton.parentNode.appendChild(oDiv);
                focusedButton.onblur = function (evt) {
                    oDiv.parentNode.removeChild(oDiv);
                    focusedButton.onblur = null;
                }
            }
        },
        "Utils.Doc": {
            /**
             * given a select object and a value, set the select box
             */
            setSelectValue: function(selectEl, value) {

                if (selectEl) {
                    for (var i = 0; i < selectEl.length; i++) {
                        if (selectEl[i].value == value) {
                            selectEl.selectedIndex = i;
                            break;
                        }
                    }
                }
            }
        },
        /**
         * common sort query string parameter format
         **/
        "Utils.formatSortKey": {
            init: function(value) {

                switch (value) {
                    case 'Page Name':
                        newValue = 'internalName';
                        break;

                    case 'Last Edited By':
                        newValue = 'userLastName';
                        break;

                    case 'URL':
                        newValue = 'browserUri';
                        break;

                    case 'Last Edited':
                        newValue = 'eventDate';
                        break;

                    case 'Edit':
                        newValue = 'eventDate';
                        break;
                }

                return newValue;

            }
        },
        /**
         * Useful String manipulation utilities
         **/
        StringUtils: {
            isString: function(value) {
                return (Object.prototype.toString.call(value) === "[object String]");
            },
            format: function(format){
                var args = Array.prototype.slice.call(arguments, 1);
                return (format.replace(/\{(\d+)\}/g, function(match, index){
                    return args[index];
                }));
            },
            /**
             * Formats a string with placeholders with the given function
             * note: Iterator function must return the replacement value
             * or the complete match if no operation was performed
             * @param {String} format The text to format
             * @param {Function} iterator The function that formats each placeholder
             * @returns {String} The formatted text
             */
            advFormat: function(format, iterator) {
                return (format.replace(/\{.*?\}/g, function(match, index){
                    return iterator(match.substr(1, match.length - 2), index);
                }));
            },
            keyFormat: function(format, oHash) {
                return this.advFormat(format, function(match){
                    return oHash[match] || match;
                });
            },
            /**
             * Transforms a string separated by the supplied char into camelcase.
             * If no separation char is supplied then dash is assumed.
             * @param str {String} String to transfor
             * @param separator {String} the separator char
             */
            toCamelcase: function(str, separator) {
                !separator && (separator = "-");
                var parts = str.split(separator),
                    len = parts.length;
                if (len == 1) return parts[0];
                var camel = [];
                camel.push((str.charAt(0) == separator) ? parts[0].charAt(0).toUpperCase() + parts[0].substring(1) : parts[0]);
                for (var i = 1; i < len; i++)
                    camel.push( parts[i].charAt(0).toUpperCase() + parts[i].substring(1) );
                return camel.join("");
            },
            toDashes: function(str) {
                return (str.replace(/::/g, '/')
                    .replace(/([A-Z]+)([A-Z][a-z])/g, '$1-$2')
                    .replace(/([a-z\d])([A-Z])/g, '$1-$2')
                    .toLowerCase());
            },
            truncate: function(str, size) {
                return (str.length <= size) ? str : (str.substr(0, size-3) + "...");
            }
        },
        /**
         * Various storage utilities making use of HTML5 localstorage
         * if supported or falling back to cookies if not
         **/
        Storage: {
            /**
             * Shorcut to the localStorage object if available
             **/
            ls: window.localStorage,
            /**
             * Stores a value with the provided key in the browser's local storage or
             * falling back to cookies if localstorage is not supported
             * @param {String} key The key to store the value with
             * @param {String} value The value to store
             * @param {Number} hours In case cookie fallback needed, optionally specify a expiration time for the cookie in hours
             * @return The stored value
             **/
            store: function(key, value, hours){
                if ( this.ls ) {
                    try {
                        this.ls.setItem(key, value);
                    } catch (e) {
                        if (e == QUOTA_EXCEEDED_ERR) alert('Your local Storage Quota exeeded.');
                        this.write(key, value, hours);
                    }
                } else {
                    this.write(key, value, hours);
                }
                return value;
            },
            /**
             * Retrieves the value associated with the provided key. If localstorage is not supported
             * looks for the value as a cookie
             * @param {String} key The key to retrieve the associated value
             * @return The value associated to the key or an empty string
             **/
            retrieve: function(key){
                var value;
                if ( this.ls ) {
                    value = this.ls.getItem(key);
                }
                if ( value === null || value === undefined ) value = this.read(key);
                return value;
            },
            /**
             * Deletes an entry from the localstorage or a cookie if localstorage is not supported
             * @param {String} key The key to delete
             * @return The deleted value
             **/
            del: function(key) {
                var value;
                if ( this.ls ) {
                    value = this.ls.getItem(key);
                    delete this.ls.removeItem(key);
                }
                if( value === undefined ) value = this.eliminate(key);
                return value;
            },
            /**
             * Reads a cookie from the client computer
             * @param {String} name The name of the cookie to retrieve
             * @return The read value
             */
            read: function(name) {
                var cookieValue = "";
                var search = (name + "=");
                if ( document.cookie.length > 0 ) {
                    var offset = document.cookie.indexOf(search);
                    if (offset != -1) {
                        offset += search.length;
                        var end = document.cookie.indexOf(";", offset);
                        if (end == -1) end = document.cookie.length;
                        cookieValue = decodeURIComponent(document.cookie.substring(offset, end))
                    }
                }
                return cookieValue;
            },
            /**
             * Writes a cookie to the client computer
             * @param {String} name The name for the new cookie
             * @param {String} value The cookie's value
             * @param {Number} hours Hours from the moment of registration
             * @return The stored value
             */
            write: function(name, value, hours) {
                var expire,
                    domainVal;
                if ( hours ) {
                    expire = (new Date( (new Date()).getTime() + (hours * 3600000) )).toGMTString();
                } else {
                    // IE9 doesn't like an empty expire value (i.e. it won't create the cookie)
                    // For IE9, creating a cookie with an expire value set to half an hour (0.5 hours)
                    expire = (YAHOO.env.ua.ie == 9) ? (new Date( (new Date()).getTime() + CStudioAuthoring.Utils.Cookies.durationHours(0.5) )).toGMTString() : "";
                }
                domainVal = (CStudioAuthoringContext.cookieDomain != 'localhost') ? "domain=" + CStudioAuthoringContext.cookieDomain : "";
                document.cookie = CStudioAuthoring.StringUtils.format(
                    "{0}={1}; expires={2}; path=/; " + domainVal,
                    name,
                    encodeURIComponent(value),
                    expire);
                return value;
            },
            /**
             * Eliminates the specified cookie
             * @param {String} key The cookie name to delete
             * @return The eliminated value
             **/
            eliminate: function(name) {
                var value = this.read(name); // retrieve the value before deleting
                this.write(name, "", -168); // Set expiration to a week before now
                return value;
            }
        },
        /**
         * Collection of utilities for dealing with cookies
         */
        "Utils.Cookies": {
            /**
             * given a unit, return enough millis for that unit of hours
             */
            durationHours: function(unit) {
                // 60 * 60 * 1000 = 3600000
                return 3600000 * unit;
            },
            /**
             * given a unit, return enough millis for that unit of hours
             */
            durationDays: function(unit) {
                // 24 * 60 * 60 * 1000 = 86400000
                return 86400000 * unit;
            },
            /**
             * write a cookie
             */
            createCookie: function(name, value, duration) {
                var expires,
                    date,
                    domainVal;
                if (duration) {
                    date = new Date();
                    date.setTime(date.getTime() + duration);
                    expires = date.toGMTString();
                } else {
                    // IE doesn't like an empty expire value (i.e. it won't create the cookie)
                    // For IE, creating a cookie with an expire value set to half an hour (0.5 hours)
                    expires = (YAHOO.env.ua.ie) ? (new Date( (new Date()).getTime() + CStudioAuthoring.Utils.Cookies.durationHours(0.5) )).toGMTString() : "";
                }
                domainVal = (CStudioAuthoringContext.cookieDomain != 'localhost') ? "domain=" + CStudioAuthoringContext.cookieDomain : "";
                if(expires){
                    document.cookie =
                        [name, "=", value, "; expires=", expires, "; path=/; " + domainVal].join("");
                }else{
                    document.cookie =
                        [name, "=", value, "; path=/; " + domainVal].join("");
                }

            },
            /**
             * read a cookie
             */
            readCookie: function(name) {
                var nameEQ = name + "=";
                var ca = document.cookie.split(';');
                for (var i = 0; i < ca.length; i++) {
                    var c = ca[i];
                    while (c.charAt(0) == ' ') c = c.substring(1, c.length);
                    if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
                }
                return null;
            },
            /**
             * destroy a cookie
             */
            eraseCookie: function(name) {
                var domainVal = (CStudioAuthoringContext.cookieDomain != 'localhost') ? "domain=" + CStudioAuthoringContext.cookieDomain : "";
                document.cookie = name + "=null; expires=Thu, 01-Jan-1970 00:00:01 GMT; path=/; " + domainVal;
            }
        },
        /**
         * pass the correct parameter for sortAscDesc
         **/
        "Utils.sortByAsc": {
            init: function(sortBy, widgetId) {

                var previousSortedBy = YDom.get('sortedBy-' + widgetId).innerHTML;
                var previousSortType = YDom.get('sort-type-' + widgetId).innerHTML;
                var currentSortBy = (sortBy) ? sortBy : null;

                if (currentSortBy == null) return false;
                currentSortBy = currentSortBy.replace("-" + widgetId, "");

                if (previousSortedBy == currentSortBy) {

                    if (previousSortType == "true") {
                        currentSortType = "false";
                    }
                    else {
                        currentSortType = "true";
                    }

                } else {

                    currentSortType = "false";
                }

                return currentSortType;
            }
        },
        /**
         * manages child searches
         */
        ChildSearchManager: {
            searches: [],
            /**
             * create a new child form configuration
             */
            createChildSearchConfig: function() {
                return { searchId: "",
                    searchUrl: "",
                    openAsTab: "",
                    saveCallback: null
                };
            },
            /**
             * signal seach close
             * @param searchId name of search
             * @param value returned
             */
            signalSearchClose: function(searchId, contentTOs) {

                var childSearchConfig = this.searches[searchId];

                childSearchConfig.saveCallback.success(searchId, contentTOs);
            },
            /**
             * open child search
             * @parm form configuration
             */
            openChildSearch: function(childSearchConfig) {

                if (this.searches == null) {
                    this.searches = new Array();
                }

                this.searches[childSearchConfig.searchId] = childSearchConfig;


                if (childSearchConfig.openInSameWindow) {
                    newWindow = document.location = childSearchConfig.searchUrl;
                }
                else {

                    var newWindow;

                    if (YAHOO.env.ua.ie > 0)
                        newWindow = window.open(childSearchConfig.searchUrl, null);
                    else
                        newWindow = window.open(childSearchConfig.searchUrl, childSearchConfig.searchId);

                }
            }
        },
        /**
         * Child form Manager
         */
        ChildFormManager: {
            forms: {},
            /**
             * create a new child form configuration
             */
            createChildFormConfig: function() {
                return {
                    formName: "",
                    formUrl: "",
                    openAsTab: "",
                    windowHeight: "",
                    windowWidth: "",
                    windowTitle: "",
                    windowName: "",
                    formSaveCallback: null
                };
            },
            /**
             * signal form close
             * @param formName name of form
             * @param value returned
             */
            signalFormClose: function(formName, name, value) {
                var childFormConfig = this.forms[formName];

                if(childFormConfig) {
                    childFormConfig.formSaveCallback.success(
                        formName,
                        name,
                        value);
                }
            },

            /*
             * @return formId : the form id if the form is open; if not, false
             */
            getChildFormByName: function(windowName) {
                var form = null;

                if (this.forms) {
                    for (var formId in this.forms) {
                        form = this.forms[formId];
                        if (form.windowName == windowName) {
                            return formId;
                        }
                    }
                }
                return false;
            },

            /**
             * open child form
             * @parm form configuration
             */
            openChildForm: function(childFormConfig) {
                this.forms = this.forms || {};

                var formId = childFormConfig.form;
                var childFormId;

                if (childFormConfig.windowName == null || childFormConfig.windowName == "") {
                    // formId is encoded such that any '/' or '-' are removed, since IE doesn't
                    // accept these characters in a window name
                    childFormConfig.windowName = encodePathToNumbers(formId);
                } else {
                    childFormConfig.windowName = encodePathToNumbers(childFormConfig.windowName);
                }

                childFormId = this.getChildFormByName(childFormConfig.windowName);
                childFormConfig.windowRef = window.open(childFormConfig.formUrl, childFormConfig.windowName);

                if (!childFormId) {
                    this.forms[formId] = childFormConfig;
                } else {
                    if (this.forms[childFormId].windowRef.closed) {
                        // A child name with the same window name was created previously, but it doesn't reference 
                        // a window any more => delete the reference to this child form
                        delete this.forms[childFormId];
                        this.forms[formId] = childFormConfig;
                    }
                }
            }

        },
        /**
         * Preview refresh mechanism
         */
        WindowManagerProxy: {
            lastKey: null,
            flash: false,
            title: document.title,
            currentWindowLocation: document.location,

            init: function() {

                if (typeof(CStudioAuthoringContext) == "undefined") {
                    YAHOO.lang.later(1000, this, CSA.WindowManagerProxy.init);
                } else {

                    if (!window.name || window.name == "") {
                        window.name = CStudioAuthoring.Utils.generateUUID();
                    }

                    CSA.Utils.Cookies.eraseCookie("cstudio-main-window");

                    // Check if user is to be notified.
                    this.notify = CSA.Utils.Cookies.readCookie("cstudio-preview-notify");
                    if(this.notify != null) {

                        CStudioAuthoring.Utils.Cookies.eraseCookie("cstudio-preview-notify");
                        if (document.location.href.indexOf(CStudioAuthoringContext.authoringAppBaseUri) == -1) {
                            // in some cases where common-api.js is not included in preview
                            // this message shows up on the dashboard because the cookie does not get erased.  
                            // The basic assumption here is preview is not rooted below authoring url
                            YAHOO.lang.later(2000, this, function() {
                                alert("Preview Loaded");
                            });
                        }

                    }

                    YAHOO.lang.later(1000, this, function() {

                        var cookie = CSA.Utils.Cookies.readCookie("cstudio-main-window");
                        if (cookie != null) {

                            var pieces = cookie.split('|');

                            var key = pieces[0];
                            var loc = pieces[1];
                            var tone = pieces[2];
                            var signalToWindowId = pieces[3];

                            if (signalToWindowId == window.name && (this.lastKey == null || this.lastKey != key)) {
                                CStudioAuthoring.Utils.Cookies.createCookie("cstudio-main-window-location",loc);
                                document.location = loc;
                            }

                            this.lastKey = key;
                        }

                    }, [],1000);
                }
            }
        },

        /**
         * added service to get node icon.
         */
        IconService: {
            getItemIcon : function (item) {
                var itemIconClass = "navPage";
                if (item.document) {
                    itemIconClass = "document";
                } else if (item.floating) {
                    itemIconClass = "ttFloating";
                } else if (item.component) {
                    itemIconClass = "ttComponent";
                } else {
                    itemIconClass = "tt";
                }

                if (item.deleted||item.submittedForDeletion) {
                    itemIconClass += "Deleted";
                }
                if (item.submitted || item.scheduled) {
                    if (item.submitted) {
                        itemIconClass += "Submitted";
                    }
                    if (item.scheduled) {
                        itemIconClass += "Scheduled";
                    }
                }
                else if (!item.deleted && item.inProgress) {
                    itemIconClass += "InProgress";
                }

                // If no flag is set, then it should be live item
                if (!item.deleted && !item.submitted && !item.scheduled && !item.inProgress) {
                    itemIconClass = "navPage";
                }

                itemIconClass += " icon";

                return itemIconClass;
            }
        }
    });
    CSA.Clipboard.init();
    CSA.SelectedContent.init();
    /**
     * form command bar
     * @param containerId
     *    id of the container that will hold command bar
     * @addSpacer
     *   add spacer between form and controls
     */
    CSA.register({
        CommandToolbar: function(containerId, addSpacer) {
            this.init(containerId, addSpacer);
        }
    });
    /**
     * initialize the form command toolbar
     * @param {String} id ID of the container element for the toolbar
     */
    CSA.CommandToolbar.prototype.init = (function(id, addSpacer) {
        this.container = document.getElementById(id);
        this.controlBox = null;
        if (this.container != null) {
            if (addSpacer == true) {
                var formControlSpacer = document.createElement("div");
                formControlSpacer.id = "formBottomSpacer";
                formControlSpacer.style.height = "30px";
                this.container.appendChild(formControlSpacer);
            }

            var xformfooterWrapper = document.createElement("div");
            xformfooterWrapper.id = "xformfooterWrapper";
            this.container.appendChild(xformfooterWrapper);

            var xformfooterheader = document.createElement("div");
            xformfooterheader.id = "xformfooterheader";
            xformfooterWrapper.appendChild(xformfooterheader);

            var submissionControls = document.createElement("div");
            submissionControls.id = "submission-controls";
            this.controlBox = submissionControls;
            YDom.addClass(this.controlBox, "cstudio-form-controls-button-container");
            xformfooterheader.appendChild(submissionControls);
        }
    });
    /**
     * add a new control to the end of the controls
     * @param controlId
     *    id of button
     * @param label
     *    label on button
     * @param actionCallback
     *    function to be executed when button is pushed
     */
    CSA.CommandToolbar.prototype.addControl = (function(controlId, label, actionCallback) {
        if (this.controlBox != null) {
            //var buttonControl = document.createElement("button");
            var buttonControl = document.createElement("input");
            buttonControl.id = controlId;
            buttonControl.type = "button";
            buttonControl.className = "cstudio-xform-button";
            //buttonControl.innerHTML = label;
            buttonControl.value = label;
            YDom.addClass(buttonControl, "cstudio-form-control-button ");
            YDom.addClass(buttonControl, "cstudio-button");
            this.controlBox.appendChild(buttonControl);

            buttonControl.onclick = function() {
                if(actionCallback.click) {
                    actionCallback.click();
                }
                else {
                    actionCallback();
                }
            };
        }
    });

    /**
     * disable an existing control
     * i.e. the controlId to be disabled
     * @param controlId
     *    id of button
     */
    CSA.CommandToolbar.prototype.disableControl = (function(controlId) {
        if (this.controlBox != null) {
            var buttonControl = document.getElementById(controlId);

            if (buttonControl != null) {
                buttonControl.className = "cstudio-xform-button-disabled";
                buttonControl.onclick = function() {};
            }
        }
    });

    if (!window.opener) {
        CSA.WindowManagerProxy.init();
    }

    /* Registering Request Timeout values for go live service request */
    CSA.register({
        "Request.Timeout": {
            GoLiveTimeout: 180000
        }
    });

})();

/**
 * simple internationalization mechanism
 */
CStudioAuthoring.Messages = CStudioAuthoring.Messages || {
    bundles: { },

    registerBundle: function(namespace, lang, bundle) {
        var M = CStudioAuthoring.Messages;

        if(!M.bundles[namespace]) {
            M.bundles[namespace] = { };
        }

        M.bundles[namespace][lang] = bundle;
    },

    getBundle: function(namespace, lang) {
        var bundle;
        var M = CStudioAuthoring.Messages;
        var namespace = M.bundles[namespace];
        if(namespace) {
            bundle = namespace[lang];

            if(bundle && lang != "en") {
                // fallback
                bundle.fallbackBundle = namespace["en"];
            }
            else {
                bundle = namespace["en"];
            }
        }

        return bundle;
    },

    format: function(bundle, messageId, a, b, c, d, e, f, g) {
        var formattedMessage = messageId;
        var spaceRegex = new RegExp(" ", 'g');
        var starRegex = new RegExp("\\*", 'g');
        var key = messageId.replace(spaceRegex, '');
        key = key.replace(starRegex, '');

        if(bundle[key]) {
            formattedMessage = bundle[key];
        }
        else if(bundle.fallbackBundle && bundle.fallbackBundle[key]) {
            formattedMessage = bundle.fallbackBundle[key];
        }

        if(a) formattedMessage = formattedMessage.replace("{0}", a);
        if(b) formattedMessage = formattedMessage.replace("{1}", b);
        if(c) formattedMessage = formattedMessage.replace("{2}", c);
        if(d) formattedMessage = formattedMessage.replace("{3}", d);
        if(e) formattedMessage = formattedMessage.replace("{4}", e);
        if(f) formattedMessage = formattedMessage.replace("{5}", f);
        if(g) formattedMessage = formattedMessage.replace("{6}", g);


        return formattedMessage;
    },

    display: function(bundle, messageId, a, b, c, d, e, f, g) {
        var formattedMessage = CStudioAuthoring.Messages.format(bundle, messageId, a, b, c, d, e, f, g);
        document.write(formattedMessage);
    }
}

CStudioAuthoring.InContextEdit = {
    registerDialog: function(editorId, context) {

        if(!window.top.iceDialogs) {
            window.top.iceDialogs = [];
        }

        window.top.iceDialogs[editorId] = context;
        window.top.iceDialogs[window.top.iceDialogs.length] = {key: editorId, value: context };
    },

    registerIceCallback: function(editorId, callback) {
        if(!window.top.iceCallback) {
            window.top.iceCallback = [];
        }

        window.top.iceCallback[editorId] = callback;
        window.top.iceCallback[window.top.iceCallback.length] = {key: editorId, value: callback };
    },
  
    getIceCallback: function(editorId) {

        var iceWindowCallback;
        
        if(window.top.iceCallback) {
            iceWindowCallback = window.top.iceCallback[editorId];
        }

        return iceWindowCallback;
    },

    unstackDialog: function(editorId) {

        //if(window.top.iceDialogs.length > 1) {
            for(var i=0; i < window.top.iceDialogs.length; i++) {
                if(window.top.iceDialogs[i].key == editorId) {

                    var dialog = window.top.iceDialogs[i].value;
                    window.top.iceDialogs.splice(i, 1);

                    if(dialog) {
                        dialog.close();
                    }
                }
            }
        //}
    },

    regions: [],

    initializeEditRegion: function(regionElId, formField, regionLabel) {

        this.regions.push({id: regionElId, formId: formField, label: regionLabel});

        var regionEl = document.getElementById(regionElId);
        var registerEl = document.createElement("div");
        registerEl.style.display = "none";

        var controlBoxEl = document.createElement("div");

        var editControlEl = document.createElement("img");
        editControlEl.src = "/proxy/authoring/static-assets/themes/cstudioTheme/images/edit.png";

        var contentItem;
        var itemIsLoaded = true;

        if(formField.indexOf(":") == -1) {
            contentItem = CStudioAuthoring.SelectedContent.getSelectedContent()[0];
        } else {
            contentItem = formField.substring(0,formField.indexOf(':'));
            formField = formField.substring(formField.indexOf(':')+1);
            itemIsLoaded = false;
        }

        editControlEl.content = {
            field: formField,
            item: contentItem,
            itemIsLoaded: itemIsLoaded
        };

        editControlEl.onclick = CStudioAuthoring.InContextEdit.editControlClicked;

        controlBoxEl.appendChild(editControlEl);
        var contentBoxEl = document.createElement("div");
        contentBoxEl.innerHTML = regionEl.innerHTML;

        regionEl.innerHTML = "";
        regionEl.appendChild(controlBoxEl);
        regionEl.appendChild(contentBoxEl);

        controlBoxEl.style.display = "none";

        var iceToolsModuleCb = {
            moduleLoaded: function(moduleName, moduleClass, moduleConfig) {

                CStudioAuthoring.IceTools.IceToolsOffEvent.subscribe(
                    function() {
                        controlBoxEl.style.display = "none";
                    });

                CStudioAuthoring.IceTools.IceToolsOnEvent.subscribe(
                    function() {
                        controlBoxEl.style.display = "inline";
                        controlBoxEl.style.width = "20px";
                    });
            }
        };

        CStudioAuthoring.Module.requireModule(
            "ice-tools-controller",
            '/static-assets/components/cstudio-preview-tools/ice-tools.js',
            0,
            iceToolsModuleCb
        );
    },

    editControlClicked: function() {
        if(this.content.itemIsLoaded == true) {
            CStudioAuthoring.Operations.performSimpleIceEdit(
                    CStudioAuthoring.SelectedContent.getSelectedContent()[0], 
                    this.content.field
                    //isEdit
                    //callback
                    //aux
                    );
        } else {

            var lookupContentCb = {
                success: function(contentTO) {
                    CStudioAuthoring.Operations.performSimpleIceEdit(
                        contentTO.item, 
                        this.field
                        //isEdit
                        //callback
                        //aux
                        );
                },
                failure: crafter.noop,
                field: this.content.field

            };

            CStudioAuthoring.Service.lookupContentItem(CStudioAuthoringContext.site, this.content.item, lookupContentCb, false);

        }
    },

    initializeComponentEditRegion: function(regionElId, regionLabel) {

        var id = regionElId.replace("cstudio-component-","");

        var lookupContentCb = {
            success: function(contentTO) {
                var regionEl = document.getElementById(regionElId);
                var registerEl = document.createElement("div");
                registerEl.style.display = "none";

                var controlBoxEl = document.createElement("div");

                var editControlEl = document.createElement("img");
                editControlEl.src = "/proxy/authoring/static-assets/themes/cstudioTheme/images/edit-component.png";
                controlBoxEl.style.display = "inline";
                controlBoxEl.style.cursor = "pointer";

                var editTemplateControlEl = document.createElement("img");
                editTemplateControlEl.src = "/proxy/authoring/static-assets/themes/cstudioTheme/images/icons/code-edit.gif";
                controlBoxEl.style.cursor = "pointer";

                var onSaveCb = {
                    success: function() {
                        if(!CStudioAuthoringContext.channel || CStudioAuthoringContext.channel == "web") {
                            document.location = document.location;
                        }
                        else {

                            var cb = {
                                moduleLoaded: function(moduleName, moduleClass, moduleConfig) {
                                    try {
                                        moduleClass.render();
                                    }
                                    catch (e) {
                                    }
                                },

                                self: this
                            };

                            CStudioAuthoring.Module.requireModule(
                                "medium-panel-"+CStudioAuthoringContext.channel,
                                '/static-assets/components/cstudio-preview-tools/mods/agent-plugins/'+channel.value+'/'+CStudioAuthoringContext.channel+'.js',
                                0,
                                cb);

                        }
                    },
                    failure: function() {
                    }
                };

                editControlEl.onclick = function() {
                    CStudioAuthoring.Operations.performSimpleIceEdit(contentTO.item
                            // field
                            // isEdit
                            // callback
                            // aux
                        );
                };

                editTemplateControlEl.onclick = function() {
                    var contentType = contentTO.item.renderingTemplates[0].uri;

                    if(CStudioAuthoringContext.channel && CStudioAuthoringContext.channel != "web") {
                        contentType = contentType.substring(0, contentType.lastIndexOf(".ftl")) +
                        "-" + CStudioAuthoringContext.channel + ".ftl";
                    }

                    CStudioAuthoring.Operations.openTemplateEditor(contentType, "default", onSaveCb);

                }


                controlBoxEl.appendChild(editControlEl);
                controlBoxEl.appendChild(editTemplateControlEl);
                var contentBoxEl = document.createElement("div");
                contentBoxEl.innerHTML = regionEl.innerHTML;

                regionEl.innerHTML = "";
                regionEl.appendChild(controlBoxEl);
                regionEl.appendChild(contentBoxEl);

                controlBoxEl.style.display = "none";

                var iceToolsModuleCb = {
                    moduleLoaded: function(moduleName, moduleClass, moduleConfig) {

                        CStudioAuthoring.IceTools.IceToolsOffEvent.subscribe(
                            function() {
                                controlBoxEl.style.display = "none";
                            });

                        CStudioAuthoring.IceTools.IceToolsOnEvent.subscribe(
                            function() {
                                regionEl.style.display = "inline-block";
                                controlBoxEl.style.display = "inline";

                            });
                    }
                };

                CStudioAuthoring.Module.requireModule(
                    "ice-tools-controller",
                    '/static-assets/components/cstudio-preview-tools/ice-tools.js',
                    0,
                    iceToolsModuleCb
                );

            },
            failure: function() {
            }
        };

        CStudioAuthoring.Service.lookupContentItem(CStudioAuthoringContext.site, id, lookupContentCb, false)
    },

    autoInitializeEditRegions: function() {
        var iceEls = YAHOO.util.Dom.getElementsByClassName("cstudio-ice", null, document.body);

        if (iceEls) {
            for(var i = 0; i < iceEls.length; ++i) {
                CStudioAuthoring.InContextEdit.initializeEditRegion(iceEls[i].id, iceEls[i].id);
            }
        }

        var componentIceEls = YAHOO.util.Dom.getElementsByClassName("cstudio-component-ice", null, document.body);

        for(var i = 0; componentIceEls && i < componentIceEls.length; i++) {
            CStudioAuthoring.InContextEdit.initializeComponentEditRegion(
                componentIceEls[i].id, componentIceEls[i].id);
        }

    },

    autoSizeIceDialog: function(editorId) {
        var el = document.getElementById('in-context-edit-editor-'+editorId);
        var containerEl = document.getElementById('viewcontroller-in-context-edit-'+editorId+'_0_c');
        if(!containerEl) return;

        var height = YAHOO.util.Dom.getViewportHeight() - 200;

        containerEl.style.height = height+'px';
        el.style.height = height+'px';
        var iframeDoc = el.contentWindow.document;
        el.style.width = iframeDoc.body.scrollWidth+100+'px';

        iframeDoc.activeElement.parentNode.style.background = "#F0F0F0";
        iframeDoc.activeElement.style.background = "#F0F0F0";
        window.scrollBy(0,1);
    }
};

(function (w) {

    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event;

    var CrafterStudioUtils = w.CStudioAuthoring.Utils,
        aElements = [],

        getStyle = Dom.getStyle,
        isString = YAHOO.lang.isString,

        offset = function (elem) {
            var aXY = Dom.getXY(elem);
            return {
                left: aXY[0],
                top: aXY[1]
            };
        },
        height = function (elem) {
            var v = elem.offsetHeight;
            /* TODO: required to consider other elements to really get this value */
            return v;
        },
        width = function (elem) {
            var v = elem.offsetWidth;
            /* TODO: required to consider other elements to really get this value */
            return v;
        },
        isVisible;

    var div = document.createElement( "div" ),
        tds,
        trustOffsets;

    div.innerHTML = "<table><tr><td style='padding:0;border:0;display:none'></td><td>t</td></tr></table>";
    tds = div.getElementsByTagName( "td" );

    trustOffsets = (tds[ 0 ].offsetHeight === 0);

    tds[ 0 ].style.display = "";
    tds[ 1 ].style.display = "none";

    trustOffsets = trustOffsets && (tds[ 0 ].offsetHeight === 0);

    isVisible = function (element) {
        var width = element.offsetWidth,
            height = element.offsetHeight;
        return !(
        (width === 0 && height === 0) ||
        (!trustOffsets && (element.style.display || getStyle("display") === "none"))
        );
    };

    var scroll = function () {

        if (!aElements.length) {
            return CrafterStudioUtils._removeScrollListener();
        }

        var a = Dom.getDocumentScrollLeft(),
            b = Dom.getDocumentScrollTop(),
            viewportH = Dom.getViewportHeight(),
            viewportW = Dom.getViewportWidth();

        for (var i = 0, l = aElements.length; i < l; ++i) {
            var elem = aElements[i];
            if (isVisible(elem)) {

                var o = offset(elem),
                    x = o.left,
                    y = o.top,
                    _w = width(elem),
                    _h = height(elem),

                    yAxisCondition = (y <= (viewportH + b)) && (b <= (y + _h));

                if ( (yAxisCondition) && !(elem._hasBeenNotified) ) {

                    // trigger
                    elem._hasBeenNotified = true;
                    elem._onVisibleHandler();

                }

            }
        }
    }


    CrafterStudioUtils.isVisible = isVisible;
    CrafterStudioUtils.onVisible = function (elem, handler) {
        if (isString(elem)) elem = document.getElementById(elem);
        if (elem && handler) {
            elem._onVisibleHandler = handler;
            aElements.push(elem);
            CrafterStudioUtils._addScrollListener();
            scroll();
        }
    };
    CrafterStudioUtils._addScrollListener = function () {
        if (!CrafterStudioUtils._addScrollListener.listeningScroll) {
            Event.addListener(w, "scroll", scroll);
            CrafterStudioUtils._addScrollListener.listeningScroll = true;
        }
    };
    CrafterStudioUtils._removeScrollListener = function () {
        Event.removeListener(w, "scroll", scroll);
        CrafterStudioUtils._addScrollListener.listeningScroll = false;
    };

    CrafterStudioUtils._addScrollListener.listeningScroll = false;

})(window);

/*
 * Create crafterSite cookie on DOM Ready (so CStudioAuthoringContext object is available)
 */
(function (w) {

    // Parameter 'win' of the anonymous function will be the object passed as parameter 'w'
    YAHOO.util.Event.onDOMReady(function (e, args, win) {
        //
        if(!(!(window.ActiveXObject) && "ActiveXObject" in window)){
            win.CStudioAuthoring.Utils.Cookies.createCookie("crafterSite", win.CStudioAuthoringContext.site);
        }
    }, w);

}) (window);

 (function startAuthLoop() {
  
     if (typeof CStudioAuthoringContext != 'undefined') {

         var authLoopCb = {
             success: function(config){

                function authRedirect(authConfig) {
                       location = "/studio/#/login";
                }

                function authLoop(configObj) {
                    var alfrescoTicket,
                        serviceUri,
                        serviceCallback,
                        delay = 60000;  // poll once every minute

                        serviceUri = CStudioAuthoring.Service.verifyAuthTicketUrl;

                        serviceCallback = {
                            success: function(response) {
                                var resObj = response.responseText
 
                                if (resObj.indexOf("true") != -1) {

                                    setTimeout(function() { authLoop(configObj); }, delay);
                                } 
                                else {
                                    // Ticket is invalid
                                    authRedirect(configObj);
                                }
                            },
                            failure: function(response) {
                                authRedirect(configObj);
                                //throw new Error('Unable to read session ticket');
                            }
                        };

                        YConnect.asyncRequest("GET", CStudioAuthoring.Service.createServiceUri(serviceUri), serviceCallback);

                }

                // Start the authentication loop
                if (config.authentication) {
                    authLoop(config.authentication);
                } else {
                    authLoop(null);
                }
            },

            failure: function(){
                throw new Error('Unable to read site configuration');
            }
        }

        CStudioAuthoring.Service.lookupConfigurtion(
            CStudioAuthoringContext.site, "/site-config.xml", authLoopCb);

    } else {
        // The authentication loop cannot be started until CStudioAuthoringContext exists
        setTimeout(startAuthLoop, 1000);
    }
})();
