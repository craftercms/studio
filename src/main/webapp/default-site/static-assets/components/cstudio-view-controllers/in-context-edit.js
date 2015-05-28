/**
 * File: in-context-edit.js
 * Component ID: viewcontroller-in-context-edit
 * @author: Russ Danner
 * @date: 4.27.2011
 **/
(function(){

    var InContextEdit,
        Dom = YAHOO.util.Dom;

    CStudioAuthoring.register("ViewController.InContextEdit", function() {
        CStudioAuthoring.ViewController.InContextEdit.superclass.constructor.apply(this, arguments);
    });

    InContextEdit = CStudioAuthoring.ViewController.InContextEdit;
    YAHOO.extend(InContextEdit, CStudioAuthoring.ViewController.Base, {
        events: ["updateContent"],
        actions: [".update-content", ".cancel"],

        initialise: function(usrCfg) {
            Dom.setStyle(this.cfg.getProperty("context"), "overflow", "visible");
        },

        /**
         * on initialization, go out and get the content and
         * populate the dialog.
         *
         * on error, display the issue and then close the dialog
         */
        initializeContent: function(item, field, site, isEdit, callback, $modal, aux) {
            var iframeEl = document.getElementById("in-context-edit-editor");
            var dialogEl = document.getElementById("viewcontroller-in-context-edit_0_c");
            var dialogBodyEl = document.getElementById("viewcontroller-in-context-edit_0");
            aux = (aux) ? aux : {};

            CStudioAuthoring.Service.lookupContentType(CStudioAuthoringContext.site, item.contentType, {
                context: this,
                iframeEl: iframeEl,
                dialogEl: dialogEl,
                failure: crafter.noop,
                dialogBodyEl: dialogBodyEl,
                success: function(contentType) {
                    var windowUrl = "";
                    windowUrl = this.context.constructUrlWebFormSimpleEngine(contentType, item, field, site, isEdit, aux);

                    this.iframeEl.src = windowUrl;
                    window.iceCallback = callback;

                    this.iframeEl.onload = function () {

                        var body = this.contentDocument.body,
                            html = $(body).parents('html').get(0),
                            count = 1,
                            max;

                        var interval = setInterval(function () {

                            max = Math.max(
                                body.scrollHeight,
                                html.offsetHeight,
                                html.clientHeight,
                                html.scrollHeight,
                                html.offsetHeight);

                            if (max > $(window).height()) {
                                max = $(window).height() - 100;
                            }

                            if (max > 350) {
                                clearInterval(interval);
                                $modal.height(max);
                            }

                            if (count++ > 5) {
                                clearInterval(interval);
                            }

                        }, 1000);

                    };

                }
            });
        },

        /**
         * get the content from the input and send it back to the server
         */
        updateContentActionClicked: function(buttonEl, evt) {
            //not used
        },

        /**
         * cancel the dialog
         */
        cancelActionClicked: function(buttonEl, evt) {
            //not used
        },

        /**
         * construct URL for simple form server
         */
        constructUrlWebFormSimpleEngine: function(contentType, item, field, site, isEdit, aux) {
            var windowUrl = "";
            
            windowUrl = CStudioAuthoringContext.authoringAppBaseUri +
            "/form?site=" + site + "&form=" +
            contentType.form +
            "&path=" + item.uri;

            if(field) {
                windowUrl += "&iceId=" + field;
            } else {
                windowUrl += "&iceComponent=true";
            }

            if(aux.readOnly && aux.readOnly == true ||  aux.readOnly == "true") {
                windowUrl += "&readonly=true";
            }

            if(isEdit == true || isEdit == "true"){
                windowUrl += "&edit="+isEdit;
            }
            
            return windowUrl;
        },

        /**
         * provide support for legacy form server
         */
        constructUrlWebFormLegacyFormServer: function(item, field, site) {
            alert("legacy form server is no longer supported");
        }
    });

    CStudioAuthoring.Env.ModuleMap.map("viewcontroller-in-context-edit", InContextEdit);

})();
