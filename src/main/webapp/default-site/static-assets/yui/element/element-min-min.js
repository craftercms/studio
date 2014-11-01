YAHOO.util.Attribute=function(b,a){if(a){this.owner=a;
this.configure(b,true)
}};
YAHOO.util.Attribute.prototype={name:undefined,value:null,owner:null,readOnly:false,writeOnce:false,_initialConfig:null,_written:false,method:null,setter:null,getter:null,validator:null,getValue:function(){var a=this.value;
if(this.getter){a=this.getter.call(this.owner,this.name,a)
}return a
},setValue:function(b,f){var c,a=this.owner,e=this.name;
var d={type:e,prevValue:this.getValue(),newValue:b};
if(this.readOnly||(this.writeOnce&&this._written)){return false
}if(this.validator&&!this.validator.call(a,b)){return false
}if(!f){c=a.fireBeforeChangeEvent(d);
if(c===false){return false
}}if(this.setter){b=this.setter.call(a,b,this.name);
if(b===undefined){}}if(this.method){this.method.call(a,b,this.name)
}this.value=b;
this._written=true;
d.type=e;
if(!f){this.owner.fireChangeEvent(d)
}return true
},configure:function(c,b){c=c||{};
if(b){this._written=false
}this._initialConfig=this._initialConfig||{};
for(var a in c){if(c.hasOwnProperty(a)){this[a]=c[a];
if(b){this._initialConfig[a]=c[a]
}}}},resetValue:function(){return this.setValue(this._initialConfig.value)
},resetConfig:function(){this.configure(this._initialConfig,true)
},refresh:function(a){this.setValue(this.value,a)
}};
(function(){var a=YAHOO.util.Lang;
YAHOO.util.AttributeProvider=function(){};
YAHOO.util.AttributeProvider.prototype={_configs:null,get:function(b){this._configs=this._configs||{};
var c=this._configs[b];
if(!c||!this._configs.hasOwnProperty(b)){return null
}return c.getValue()
},set:function(c,b,e){this._configs=this._configs||{};
var d=this._configs[c];
if(!d){return false
}return d.setValue(b,e)
},getAttributeKeys:function(){this._configs=this._configs;
var b=[],c;
for(c in this._configs){if(a.hasOwnProperty(this._configs,c)&&!a.isUndefined(this._configs[c])){b[b.length]=c
}}return b
},setAttributes:function(b,d){for(var c in b){if(a.hasOwnProperty(b,c)){this.set(c,b[c],d)
}}},resetValue:function(b,c){this._configs=this._configs||{};
if(this._configs[b]){this.set(b,this._configs[b]._initialConfig.value,c);
return true
}return false
},refresh:function(c,e){this._configs=this._configs||{};
var b=this._configs;
c=((a.isString(c))?[c]:c)||this.getAttributeKeys();
for(var d=0,f=c.length;
d<f;
++d){if(b.hasOwnProperty(c[d])){this._configs[c[d]].refresh(e)
}}},register:function(c,b){this.setAttributeConfig(c,b)
},getAttributeConfig:function(c){this._configs=this._configs||{};
var d=this._configs[c]||{};
var b={};
for(c in d){if(a.hasOwnProperty(d,c)){b[c]=d[c]
}}return b
},setAttributeConfig:function(d,c,b){this._configs=this._configs||{};
c=c||{};
if(!this._configs[d]){c.name=d;
this._configs[d]=this.createAttribute(c)
}else{this._configs[d].configure(c,b)
}},configureAttribute:function(d,c,b){this.setAttributeConfig(d,c,b)
},resetAttributeConfig:function(b){this._configs=this._configs||{};
this._configs[b].resetConfig()
},subscribe:function(c,b){this._events=this._events||{};
if(!(c in this._events)){this._events[c]=this.createEvent(c)
}YAHOO.util.EventProvider.prototype.subscribe.apply(this,arguments)
},on:function(){this.subscribe.apply(this,arguments)
},addListener:function(){this.subscribe.apply(this,arguments)
},fireBeforeChangeEvent:function(b){var c="before";
c+=b.type.charAt(0).toUpperCase()+b.type.substr(1)+"Change";
b.type=c;
return this.fireEvent(b.type,b)
},fireChangeEvent:function(b){b.type+="Change";
return this.fireEvent(b.type,b)
},createAttribute:function(b){return new YAHOO.util.Attribute(b,this)
}};
YAHOO.augment(YAHOO.util.AttributeProvider,YAHOO.util.EventProvider)
})();
(function(){var d=YAHOO.util.Dom,b=YAHOO.util.AttributeProvider,c={mouseenter:true,mouseleave:true};
var a=function(f,e){this.init.apply(this,arguments)
};
a.DOM_EVENTS={click:true,dblclick:true,keydown:true,keypress:true,keyup:true,mousedown:true,mousemove:true,mouseout:true,mouseover:true,mouseup:true,mouseenter:true,mouseleave:true,focus:true,blur:true,submit:true,change:true};
a.prototype={DOM_EVENTS:null,DEFAULT_HTML_SETTER:function(e,g){var f=this.get("element");
if(f){f[g]=e
}return e
},DEFAULT_HTML_GETTER:function(g){var f=this.get("element"),e;
if(f){e=f[g]
}return e
},appendChild:function(e){e=e.get?e.get("element"):e;
return this.get("element").appendChild(e)
},getElementsByTagName:function(e){return this.get("element").getElementsByTagName(e)
},hasChildNodes:function(){return this.get("element").hasChildNodes()
},insertBefore:function(f,e){f=f.get?f.get("element"):f;
e=(e&&e.get)?e.get("element"):e;
return this.get("element").insertBefore(f,e)
},removeChild:function(e){e=e.get?e.get("element"):e;
return this.get("element").removeChild(e)
},replaceChild:function(f,e){f=f.get?f.get("element"):f;
e=e.get?e.get("element"):e;
return this.get("element").replaceChild(f,e)
},initAttributes:function(e){},addListener:function(f,g,e,h){h=h||this;
var k=YAHOO.util.Event,i=this.get("element")||this.get("id"),j=this;
if(c[f]&&!k._createMouseDelegate){return false
}if(!this._events[f]){if(i&&this.DOM_EVENTS[f]){k.on(i,f,function(m,l){if(m.srcElement&&!m.target){m.target=m.srcElement
}if((m.toElement&&!m.relatedTarget)||(m.fromElement&&!m.relatedTarget)){m.relatedTarget=k.getRelatedTarget(m)
}if(!m.currentTarget){m.currentTarget=i
}j.fireEvent(f,m,l)
},e,h)
}this.createEvent(f,{scope:this})
}return YAHOO.util.EventProvider.prototype.subscribe.apply(this,arguments)
},on:function(){return this.addListener.apply(this,arguments)
},subscribe:function(){return this.addListener.apply(this,arguments)
},removeListener:function(e,f){return this.unsubscribe.apply(this,arguments)
},addClass:function(e){d.addClass(this.get("element"),e)
},getElementsByClassName:function(e,f){return d.getElementsByClassName(e,f,this.get("element"))
},hasClass:function(e){return d.hasClass(this.get("element"),e)
},removeClass:function(e){return d.removeClass(this.get("element"),e)
},replaceClass:function(e,f){return d.replaceClass(this.get("element"),e,f)
},setStyle:function(e,f){return d.setStyle(this.get("element"),e,f)
},getStyle:function(e){return d.getStyle(this.get("element"),e)
},fireQueue:function(){var f=this._queue;
for(var e=0,g=f.length;
e<g;
++e){this[f[e][0]].apply(this,f[e][1])
}},appendTo:function(f,e){f=(f.get)?f.get("element"):d.get(f);
this.fireEvent("beforeAppendTo",{type:"beforeAppendTo",target:f});
e=(e&&e.get)?e.get("element"):d.get(e);
var g=this.get("element");
if(!g){return false
}if(!f){return false
}if(g.parent!=f){if(e){f.insertBefore(g,e)
}else{f.appendChild(g)
}}this.fireEvent("appendTo",{type:"appendTo",target:f});
return g
},get:function(g){var e=this._configs||{},f=e.element;
if(f&&!e[g]&&!YAHOO.lang.isUndefined(f.value[g])){this._setHTMLAttrConfig(g)
}return b.prototype.get.call(this,g)
},setAttributes:function(e,h){var j={},g=this._configOrder;
for(var f=0,k=g.length;
f<k;
++f){if(e[g[f]]!==undefined){j[g[f]]=true;
this.set(g[f],e[g[f]],h)
}}for(var i in e){if(e.hasOwnProperty(i)&&!j[i]){this.set(i,e[i],h)
}}},set:function(g,e,h){var f=this.get("element");
if(!f){this._queue[this._queue.length]=["set",arguments];
if(this._configs[g]){this._configs[g].value=e
}return
}if(!this._configs[g]&&!YAHOO.lang.isUndefined(f[g])){this._setHTMLAttrConfig(g)
}return b.prototype.set.apply(this,arguments)
},setAttributeConfig:function(g,f,e){this._configOrder.push(g);
b.prototype.setAttributeConfig.apply(this,arguments)
},createEvent:function(e,f){this._events[e]=true;
return b.prototype.createEvent.apply(this,arguments)
},init:function(e,f){this._initElement(e,f)
},destroy:function(){var e=this.get("element");
YAHOO.util.Event.purgeElement(e,true);
this.unsubscribeAll();
if(e&&e.parentNode){e.parentNode.removeChild(e)
}this._queue=[];
this._events={};
this._configs={};
this._configOrder=[]
},_initElement:function(g,h){this._queue=this._queue||[];
this._events=this._events||{};
this._configs=this._configs||{};
this._configOrder=[];
h=h||{};
h.element=h.element||g||null;
var e=false;
var i=a.DOM_EVENTS;
this.DOM_EVENTS=this.DOM_EVENTS||{};
for(var f in i){if(i.hasOwnProperty(f)){this.DOM_EVENTS[f]=i[f]
}}if(typeof h.element==="string"){this._setHTMLAttrConfig("id",{value:h.element})
}if(d.get(h.element)){e=true;
this._initHTMLElement(h);
this._initContent(h)
}YAHOO.util.Event.onAvailable(h.element,function(){if(!e){this._initHTMLElement(h)
}this.fireEvent("available",{type:"available",target:d.get(h.element)})
},this,true);
YAHOO.util.Event.onContentReady(h.element,function(){if(!e){this._initContent(h)
}this.fireEvent("contentReady",{type:"contentReady",target:d.get(h.element)})
},this,true)
},_initHTMLElement:function(e){this.setAttributeConfig("element",{value:d.get(e.element),readOnly:true})
},_initContent:function(e){this.initAttributes(e);
this.setAttributes(e,true);
this.fireQueue()
},_setHTMLAttrConfig:function(g,e){var f=this.get("element");
e=e||{};
e.name=g;
e.setter=e.setter||this.DEFAULT_HTML_SETTER;
e.getter=e.getter||this.DEFAULT_HTML_GETTER;
e.value=e.value||f[g];
this._configs[g]=new YAHOO.util.Attribute(e,this)
}};
YAHOO.augment(a,b);
YAHOO.util.Element=a
})();
YAHOO.register("element",YAHOO.util.Element,{version:"2.8.0r4",build:"2446"});