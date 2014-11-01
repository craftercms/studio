/**
 * File:
 * Component ID: component-templateagent
 * @author: Roy Art
 * @date: 10.01.2011
 **/
(function(){

    var Lang = YAHOO.lang,
        Util = CStudioAuthoring.StringUtils,
        TemplateAgent;

    CStudioAuthoring.register("TemplateHolder.TemplateAgent", function(){
        this.init.apply(this, arguments);
    });

    TemplateAgent = CStudioAuthoring.TemplateHolder.TemplateAgent;

    TemplateAgent.prototype = {
        init: function(oTemplate) {
            this.oTemplate = oTemplate;
        },
        get: function(tmpl, pieces) {
            var template = this.oTemplate[tmpl];
            if (Lang.isArray(pieces)) {
                return Util.format.apply(Util, [template].concat(pieces));
            } else /* if (Lang.isObject(pieces)) */ {
                return Util.advFormat(template, function(txt) {
                    return pieces[txt] || "";
                });
            }
        }
    }

})();
