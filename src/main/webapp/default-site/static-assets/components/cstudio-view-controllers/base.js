/**
 * File:
 * Component ID: viewcontroller-base
 * @author: Roy Art
 * @date: 03.01.2011
 **/
(function(undefined){

    var Base,
        CustomEvent = YAHOO.util.CustomEvent,
        Event = YAHOO.util.Event,
        Selector = YAHOO.util.Selector,
        Lang = YAHOO.lang,
        Config = YAHOO.util.Config;

    /**
     * ViewController.Base holds common view controller methods.
     * @constructor
     */
    CStudioAuthoring.register("ViewController.Base", function() {
        this.initialisedEvt = new CustomEvent("view.controller.initialised");
        this.endEvt = new CustomEvent("view.controller.end");
        this.init.apply(this, arguments);
    });

    Base = CStudioAuthoring.ViewController.Base;
    Base.prototype = {
        /**
         * Class constructor-like method. This method invokes a method in
         * the subclasses that must be named "initialise". This may be used
         * to perform subclass-specific initialisation.
         * @param cfg {Object} Configuration attributes for the instance
         */
        init: function(cfg) {

            this._initDefaultConfig();
            cfg && this.cfg.applyConfig(cfg, true);

            this._startup();
            this._initEvents();
            this._initActions();

            this.initialise && this.initialise.apply(this, arguments);
            this.fire("initialised");

            return this;
        },
        /**
         * Initialises the Config Object of the instance
         */
        _initDefaultConfig: function() {
            var cfg = this.cfg = new Config(this);
            cfg.addProperty("context", {
                value: ""
            });
        },
        /**
         * Subclasses may define an array of events that can be later fired
         * by using the fire method. This method creates a custom event for
         * each declared event. Events won't fire if not declared.
         */
        _initEvents: function() {
            var suffix = "Evt";
            CStudioAuthoring.Utils.each(this.events, function(i, evt) {
                this[evt + suffix] = new CustomEvent("viewcontroller.event." + evt, this);
            }, this);
            return this;
        },
        /**
         * Subclasses may define an array of actions to bound to event handlers.
         * Each element of the array must refer to a valid unique selector inside
         * the view context element.
         * This method bounds the declared actions (elements) click event to the
         * function. Function will not be found if the conventional name is
         * not followed
         * @see _getFn
         */
        _initActions: function() {
            var _this = this;
            CStudioAuthoring.Utils.each(this.actions, function(i, selector) {
                this._initAction(selector);
            }, this);
            return this;
        },
        _initAction: function (selector) {
            var me = this,
                handler = this._getFn(selector);
            if (handler) {
                var el = me.getComponent(selector);
                Event.addListener(el, "click", function (evt) {
                    handler.call(me, this, evt);
                });
            }
        },
        /**
         * Subclasses may define an array of methods to execute to initialise
         * in different ways the views. Startup functions are called after configuration is
         * initialised, receiving no parameters. If contruction parameters are needed, use
         * the "initialise" method which is called with all contruction parameters
         */
        _startup: function(){
            CStudioAuthoring.Utils.each(this.startup, function(i, fnName) {
                var fn = this[fnName];
                fn && fn.call(this);
            }, this);
        },
        /**
         * Internal private method to find the function for a registered action.
         * The function name must follow the convention in order to be found. The
         * convention is, the camecased version of the (dashed) element class + ActionClicked
         * i.e. an action with class my-button-one will be automatically mapped to a function named
         * myButtonOneActionClicked
         * @param selector
         */
        _getFn: function(selector) {
            var suffix = "ActionClicked",
                camelizedName = CStudioAuthoring.StringUtils.toCamelcase(selector.substr(selector.indexOf(".") + 1)),
                fn = this[camelizedName + suffix];
            return fn;
        },
        /**
         * Gets a single element by a CSS selector. The selection is
         * scoped to the view context element
         * @param selector {String} The CSS selector to query upon
         */
        getComponent: function(selector) {
            //return Selector.query(selector, this.cfg.getProperty("context"), true);
            var parent = document.querySelector("#" + this.cfg.getProperty("context"));
			return parent.querySelector(selector);
        },
        /**
         * Gets a set of elements by a CSS selector. The selection is
         * scoped to the view context element
         * @param selector {String} The CSS selector to query upon
         */
        getComponents: function(selector) {
            //return Selector.query(selector, this.cfg.getProperty("context")); 
			var parent = document.querySelector("#" + this.cfg.getProperty("context"));
			var components = parent.querySelectorAll(selector);
			return Array.prototype.slice.call(components);//Convert to Array.
        },
        /**
         * Disables one, multiple or all actions
         * @param which {Array|String} Which actions to disable, leave blank to disable all
         * @see _actionEnable
         */
        disableActions: function(which) {
            this._actionEnable(false, which);
        },
        /**
         * Enables one, multiple or all actions
         * @param which {Array|String} Which actions to enable, leave blank to enable all
         * @see _actionEnable
         */
        enableActions: function(which) {
            this._actionEnable(true, which);
        },
        /**
         * Internal private method to enable/disable one, various or
         * all registered view actions
         * @param enable {Boolean} True means enable, false means disable
         * @param which {Array|String} Specify to enable/disable only a subset of registered actions
         */
        _actionEnable: function(enable, which) {
            if (Lang.isString(which)) {
                this.getComponent(which).disabled = !enable;
            } else {
                CStudioAuthoring.Utils.each(which || this.actions, function(i, selector){
                    this.getComponent(selector).disabled = !enable;
                }, this);
            }
        },
        /**
         * Subscribes a handler for a specified event.
         * @param {String} event The event's name. i.e wipeAndRevertEvt's name is wipeAndRevert
         * @param {Function} handler A function to execute when this event is fired
         * @return {Boolean} True if the handler was subscribed successfully
         */
        on: function(event, handler) {
            var evt = this[event + "Evt"];
            evt && evt.subscribe(handler);
            return !!evt;
        },
        /**
         * Fires an internal event if registered
         * @param evt {String} The Name of the event to fire
         * @param args {Object} Data that will be passed to the handler as the second param
         */
        fire: function(evt, args) {
            var e = this[evt + "Evt"];
            e && e.fire(args);
            return this;
        },
        /**
         * Convenience method to fire end event that co-working components might
         * take advange for. i.e. This method may be called as a way of notifing a submit
         * or a primary action was completed successfully
         */
        end: function() {
            this.fire("end");
            return this;
        }
    }

    CStudioAuthoring.Env.ModuleMap.map("viewcontroller-base", Base);

})();
