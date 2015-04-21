(function(){

    CStudioAuthoring.register('Env.ModuleMap', {
        moduleInfo: {},
        map: function(moduleId, classRef) {
            this.moduleInfo[moduleId] = classRef;
        },
        get: function(moduleId) {
            return this.moduleInfo[moduleId];
        }
    });

})();

/**
 * File: loader.js
 * Component ID: component-loader
 * @author: Roy Art
 * @date: 03.01.2011
 **/
(function(){

    var CStudioLoader,
        CustomEvent = YAHOO.util.CustomEvent;

    CStudioAuthoring.register('Component.Loader', function(usrCfg) {

        CStudioAuthoring.Component.Loader.superclass.constructor.apply(
                this, arguments);

        this.loadStartEvt = new CustomEvent('loader.event.loadstart', this);
        this.loadCompleteEvt = new CustomEvent('loader.event.loadcomplete', this);

        var onSuccess = this.onSuccess;
        var _this = this;
        this.onSuccess = function() {
            onSuccess && onSuccess.apply(this, arguments);
            _this.onEnd && _this.onEnd();
            _this.onEnd = null;
            _this.fire('loadComplete');
        }

    });

    CStudioLoader = CStudioAuthoring.Component.Loader;
    YAHOO.extend(CStudioLoader, YAHOO.util.YUILoader, {
        use: function() {
            var last = arguments[arguments.length -1],
                callback = YAHOO.lang.isFunction(last) ? last : null,
                modules;

            if (callback) {
                modules = Array.prototype.splice.call(arguments, 0, arguments.length-1)
                this.onEnd = callback;
            } else {
                modules = Array.prototype.splice.call(arguments, 0);
            }

            this.require.apply(this, modules);
            this.insert();
        },
        insert: function(o, type) {
            this.fire('loadStart');
            CStudioLoader.superclass.insert.apply(this, arguments);
        },
        on: function(evt, handler, context){
            var e = this[evt + 'Evt'];
            e && e.subscribe(handler, context);
            return !!e;
        },
        fire: function(evt){
            var e = this[evt + 'Evt'];
            e && e.fire();
        }
    });

    CStudioAuthoring.Env.ModuleMap.map('component-loader', CStudioLoader);

})();

(function(){
    
    CStudioAuthoring.namespace('Env.Loader');

    CStudioAuthoring.Env.Loader = new CStudioAuthoring.Component.Loader({
        loadOptional: true,
        base: CStudioAuthoringContext.baseUri + '/static-assets/'
    });

    var Env = CStudioAuthoring.Env,
        Loader = Env.Loader,
        emptyArray = [];

    Loader.addModule({
        type: 'js',
        name:'component-dialogue',
        path: 'components/cstudio-components/dialogue.js',
        requires: emptyArray
    });

    Loader.addModule({
        type: 'js',
        name:'component-templateagent',
        path: 'components/cstudio-components/template-agent.js',
        requires: emptyArray
    });

    Loader.addModule({
        type: 'js',
        name: 'component-dropbox',
        path: 'components/cstudio-components/dropbox.js',
        requires: emptyArray
    });

    Loader.addModule({
        type: 'js',
        name:'template-schedulefordelete',
        path: 'components/cstudio-templates/schedule-for-delete.js',
        requires: emptyArray
    });

    Loader.addModule({
        type: 'js',
        name:'template-delete',
        path: 'components/cstudio-templates/delete.js',
        requires: emptyArray
    });

    Loader.addModule({
        type: 'js',
        name:'template-incontextedit',
        path: 'components/cstudio-templates/in-context-edit.js',
        requires: emptyArray
    });

    Loader.addModule({
        type: 'js',
        name:'template-history',
        path: 'components/cstudio-templates/history.js',
        requires: emptyArray
    });

    Loader.addModule({
        type: 'js',
        name:'template-approve',
        path: 'components/cstudio-templates/approve.js',
        requires: emptyArray
    });

    Loader.addModule({
        type: 'js',
        name:'viewcontroller-base',
        path: 'components/cstudio-view-controllers/base.js',
        requires: emptyArray
    });

    Loader.addModule({
        type: 'js',
        name:'viewcontroller-basedelete',
        path: 'components/cstudio-view-controllers/base-delete.js',
        requires: ['viewcontroller-base']
    });

    Loader.addModule({
        type: 'js',
        name:'viewcontroller-schedulefordelete',
        path: 'components/cstudio-view-controllers/schedule-for-delete.js',
        requires: [
            'viewcontroller-basedelete',
            'component-templateagent',
            'template-schedulefordelete'
        ]
    });

    Loader.addModule({
        type: 'js',
        name:'viewcontroller-delete',
        path: 'components/cstudio-view-controllers/delete.js',
        requires: [
            'viewcontroller-basedelete',
            'component-templateagent',
            'template-delete'
        ]
    });

    Loader.addModule({
        type: 'js',
        name:'viewcontroller-in-context-edit',
        path: 'components/cstudio-view-controllers/in-context-edit.js',
        requires: [
            'viewcontroller-base',
            'component-templateagent',
            'template-incontextedit'
        ]
    });

    Loader.addModule({
        type: 'js',
        name:'viewcontroller-history',
        path: 'components/cstudio-view-controllers/history.js',
        requires: [
            'viewcontroller-base',
            'component-templateagent',
            'template-history'
        ]
    });

    Loader.addModule({
        type: 'js',
        name:'viewcontroller-approve',
        path: 'components/cstudio-view-controllers/approve.js',
        requires: [
            'viewcontroller-base',
            'component-templateagent',
            'template-approve'
        ]
    });

    Loader.addModule({
        type: 'js',
        name: 'dialog-bulkupload',
        path: 'components/cstudio-dialogs/bulk-upload.js',
        requires: ['component-dropbox']
    });

    Loader.addModule({
        type: 'js',
        name:'template-cancel-workflow',
        path: 'components/cstudio-templates/cancel-workflow.js',
        requires: ['component-templateagent']
    });

    Loader.addModule({
        type: 'js',
        name:'viewcontroller-cancel-workflow',
        path: 'components/cstudio-view-controllers/cancel-workflow.js',
        requires: ['viewcontroller-base', 'template-cancel-workflow']
    });

    Loader.addModule({
        type: 'js',
        name:'viewcontroller-requestpublish',
        path: 'components/cstudio-view-controllers/request-publish.js',
        requires: ['viewcontroller-base', 'jquery-datetimepicker']
    });

    Loader.addModule({
        type: 'js',
        name:'viewcontroller-requestdelete',
        path: 'components/cstudio-view-controllers/request-delete.js',
        requires: ['viewcontroller-base']
    });
    
    Loader.addModule({
        type: 'js',
        name:'jquery-datetimepicker',
        path: 'libs/datetimepicker/jquery.datetimepicker.js',
        requires: ['jquery-datetimepicker-css']
    });

    Loader.addModule({
        type: 'css',
        name:'jquery-datetimepicker-css',
        path: 'libs/datetimepicker/jquery.datetimepicker.css',
        requires: emptyArray
    })

})();
 