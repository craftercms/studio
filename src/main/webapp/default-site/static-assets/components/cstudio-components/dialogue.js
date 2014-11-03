/**
 * File: dialogue.js
 * Component ID: component-dialogue
 * @author: Roy Art
 * @date: 03.01.2011
 **/
(function(){

    var Dialogue,
        Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event;

    CStudioAuthoring.register("Component.Dialogue", function(el, userConfig){
        CStudioAuthoring.Component.Dialogue.superclass.constructor.call(this, el, userConfig);
    });

    Dialogue = CStudioAuthoring.Component.Dialogue;
    Dialogue.CSS_CLASS_TOP = "cstudio-dialogue";
    Dialogue.CSS_BODY = "cstudio-dialogue-body";
    Dialogue.BODY_LOADING_TEMPLATE = [
        '<div class="body-loading">',
            '<span class="body-loading-message">{0}&hellip;</span>',
        '</div>'
    ].join("");

    YAHOO.extend(Dialogue, YAHOO.widget.SimpleDialog, {
        init: function(el, userConfig){

            Dialogue.superclass.init.call(this, el, userConfig);
            Dom.addClass(this.element, Dialogue.CSS_CLASS_TOP);

            this.changeBodyEvent.subscribe(function(bodyContent){
                Dom.addClass(this.body, Dialogue.CSS_BODY);
            }, this);

            return this;
        },
        initDefaultConfig: function () {
            Dialogue.superclass.initDefaultConfig.call(this);
            this.cfg.addProperty("loadBody", {
                handler: this.configLoadBodyFragment,
                value: false
            });
            this.cfg.addProperty("fixedX", {
                handler: this.configFixedX,
                value: true
            });
        },
        /**
         * Intended for loading an HTML fragment that will
         * serve as the Dialogue's body
         */
        configLoadBodyFragment: function(type, args, obj){
            var self = this,
                cfg = this.cfg.getProperty("loadBody");
            if (cfg) {
                var hasBody = !!this.body;
                this.setBody(CStudioAuthoring.StringUtils.format(
                        Dialogue.BODY_LOADING_TEMPLATE, "Loading, please wait&hellip;"));
                !hasBody && this.render();
                /* - - */
                cfg.loaderFn({
                    success: function(oResponse) {
                        self.setBody(oResponse.responseText);
                        cfg.callback && cfg.callback(oResponse);
                    },
                    failure: function() {
                        self.cfg.setProperty("close", true);
                        var elId = CStudioAuthoring.Utils.getScopedId("retry");
                        self.setBody('Unable to load the requested resource. <a id="'+elId+'" href="javascript:">Try again</a>');
                        Event.addListener(elId, "click", function(){
                            self.configLoadBodyFragment(type, args, obj);
                        });
                    }
                });
            }
        },

        configFixedX: function(type, args, obj){
            var val = args[0],
                alreadySubscribed = YAHOO.util.Config.alreadySubscribed,
                windowResizeEvent = YAHOO.widget.Overlay.windowResizeEvent;
            if (val) {
                this.centreX();
                if (!alreadySubscribed(this.beforeShowEvent, this.centreX, this))
                    this.beforeShowEvent.subscribe(this.centreX);
                if (!alreadySubscribed(windowResizeEvent, this.doCentreXOnDomEvent, this))
                    windowResizeEvent.subscribe(this.doCentreXOnDomEvent, this, true);
            } else {
                this.beforeShowEvent.unsubscribe(this.centreX);
                windowResizeEvent.unsubscribe(this.doCentreXOnDomEvent, this);
            }
        },
        centreX: function () {
            var nViewportOffset = YAHOO.widget.Overlay.VIEWPORT_OFFSET,
                elementWidth = this.element.offsetWidth,
                viewPortWidth = Dom.getViewportWidth(),
                x;
            if (elementWidth < viewPortWidth) {
                x = (viewPortWidth / 2) - (elementWidth / 2) + Dom.getDocumentScrollLeft();
            } else {
                x = nViewportOffset + Dom.getDocumentScrollLeft();
            }
            this.cfg.setProperty("x", parseInt(x, 10));
            this.cfg.refireEvent("iframe");
        },
        doCentreXOnDomEvent: function(){
            if (this.cfg.getProperty("visible")) {
                this.centreX();
            }
        },

        configFixedY: function(type, args, obj){
            var val = args[0],
                alreadySubscribed = YAHOO.util.Config.alreadySubscribed,
                windowResizeEvent = YAHOO.widget.Overlay.windowResizeEvent;
            if (val) {
                this.centreY();
                if (!alreadySubscribed(this.beforeShowEvent, this.centreY, this))
                    this.beforeShowEvent.subscribe(this.centreY);
                if (!alreadySubscribed(windowResizeEvent, this.doCentreYOnDomEvent, this))
                    windowResizeEvent.subscribe(this.doCentreYOnDomEvent, this, true);
            } else {
                this.beforeShowEvent.unsubscribe(this.centreY);
                windowResizeEvent.unsubscribe(this.doCentreYOnDomEvent, this);
            }
        },
        centreY: function () {
            var nViewportOffset = YAHOO.widget.Overlay.VIEWPORT_OFFSET,
                elementHeight = this.element.offsetHeight,
                viewPortHeight = Dom.getViewportHeight(),
                y;

            if (elementHeight < viewPortHeight) {
                y = (viewPortHeight / 2) - (elementHeight / 2) + Dom.getDocumentScrollTop();
            } else {
                y = nViewportOffset + Dom.getDocumentScrollTop();
            }

            this.cfg.setProperty("y", parseInt(y, 10));
            this.cfg.refireEvent("iframe");
        },
        doCentreYOnDomEvent: function(){
            if (this.cfg.getProperty("visible")) {
                this.centreY();
            }
        },

        destroy: function(){
            CStudioAuthoring.Component.Dialogue.superclass.destroy.call(this);
            YAHOO.widget.Overlay.windowResizeEvent.unsubscribe(
                this.doCentreXOnDomEvent, this);
        }
    });

    CStudioAuthoring.Env.ModuleMap.map("component-dialogue", Dialogue);

})();
