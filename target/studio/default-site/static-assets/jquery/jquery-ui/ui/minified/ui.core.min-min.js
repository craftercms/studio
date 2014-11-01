jQuery.ui||(function(p){var j=p.fn.remove,o=p.browser.mozilla&&(parseFloat(p.browser.version)<1.9);
p.ui={version:"1.7.2",plugin:{add:function(c,b,e){var a=p.ui[c].prototype;
for(var d in e){a.plugins[d]=a.plugins[d]||[];
a.plugins[d].push([b,e[d]])
}},call:function(d,b,c){var e=d.plugins[b];
if(!e||!d.element[0].parentNode){return
}for(var a=0;
a<e.length;
a++){if(d.options[e[a][0]]){e[a][1].apply(d.element,c)
}}}},contains:function(a,b){return document.compareDocumentPosition?a.compareDocumentPosition(b)&16:a!==b&&a.contains(b)
},hasScroll:function(a,c){if(p(a).css("overflow")=="hidden"){return false
}var d=(c&&c=="left")?"scrollLeft":"scrollTop",b=false;
if(a[d]>0){return true
}a[d]=1;
b=(a[d]>0);
a[d]=0;
return b
},isOverAxis:function(b,c,a){return(b>c)&&(b<(c+a))
},isOver:function(e,c,f,a,d,b){return p.ui.isOverAxis(e,f,d)&&p.ui.isOverAxis(c,a,b)
},keyCode:{BACKSPACE:8,CAPS_LOCK:20,COMMA:188,CONTROL:17,DELETE:46,DOWN:40,END:35,ENTER:13,ESCAPE:27,HOME:36,INSERT:45,LEFT:37,NUMPAD_ADD:107,NUMPAD_DECIMAL:110,NUMPAD_DIVIDE:111,NUMPAD_ENTER:108,NUMPAD_MULTIPLY:106,NUMPAD_SUBTRACT:109,PAGE_DOWN:34,PAGE_UP:33,PERIOD:190,RIGHT:39,SHIFT:16,SPACE:32,TAB:9,UP:38}};
if(o){var m=p.attr,n=p.fn.removeAttr,k="http://www.w3.org/2005/07/aaa",r=/^aria-/,q=/^wairole:/;
p.attr=function(c,d,b){var a=b!==undefined;
return(d=="role"?(a?m.call(this,c,d,"wairole:"+b):(m.apply(this,arguments)||"").replace(q,"")):(r.test(d)?(a?c.setAttributeNS(k,d.replace(r,"aaa:"),b):m.call(this,c,d.replace(r,"aaa:"))):m.apply(this,arguments)))
};
p.fn.removeAttr=function(a){return(r.test(a)?this.each(function(){this.removeAttributeNS(k,a.replace(r,""))
}):n.call(this,a))
}
}p.fn.extend({remove:function(){p("*",this).add(this).each(function(){p(this).triggerHandler("remove")
});
return j.apply(this,arguments)
},enableSelection:function(){return this.attr("unselectable","off").css("MozUserSelect","").unbind("selectstart.ui")
},disableSelection:function(){return this.attr("unselectable","on").css("MozUserSelect","none").bind("selectstart.ui",function(){return false
})
},scrollParent:function(){var a;
if((p.browser.msie&&(/(static|relative)/).test(this.css("position")))||(/absolute/).test(this.css("position"))){a=this.parents().filter(function(){return(/(relative|absolute|fixed)/).test(p.curCSS(this,"position",1))&&(/(auto|scroll)/).test(p.curCSS(this,"overflow",1)+p.curCSS(this,"overflow-y",1)+p.curCSS(this,"overflow-x",1))
}).eq(0)
}else{a=this.parents().filter(function(){return(/(auto|scroll)/).test(p.curCSS(this,"overflow",1)+p.curCSS(this,"overflow-y",1)+p.curCSS(this,"overflow-x",1))
}).eq(0)
}return(/fixed/).test(this.css("position"))||!a.length?p(document):a
}});
p.extend(p.expr[":"],{data:function(a,b,c){return !!p.data(a,c[3])
},focusable:function(b){var a=b.nodeName.toLowerCase(),c=p.attr(b,"tabindex");
return(/input|select|textarea|button|object/.test(a)?!b.disabled:"a"==a||"area"==a?b.href||!isNaN(c):!isNaN(c))&&!p(b)["area"==a?"parents":"closest"](":hidden").length
},tabbable:function(a){var b=p.attr(a,"tabindex");
return(isNaN(b)||b>=0)&&p(a).is(":focusable")
}});
function l(a,f,e,b){function c(g){var h=p[a][f][g]||[];
return(typeof h=="string"?h.split(/,?\s+/):h)
}var d=c("getter");
if(b.length==1&&typeof b[0]=="string"){d=d.concat(c("getterSetter"))
}return(p.inArray(e,d)!=-1)
}p.widget=function(b,c){var a=b.split(".")[0];
b=b.split(".")[1];
p.fn[b]=function(e){var g=(typeof e=="string"),f=Array.prototype.slice.call(arguments,1);
if(g&&e.substring(0,1)=="_"){return this
}if(g&&l(a,b,e,f)){var d=p.data(this[0],b);
return(d?d[e].apply(d,f):undefined)
}return this.each(function(){var h=p.data(this,b);
(!h&&!g&&p.data(this,b,new p[a][b](this,e))._init());
(h&&g&&p.isFunction(h[e])&&h[e].apply(h,f))
})
};
p[a]=p[a]||{};
p[a][b]=function(e,f){var d=this;
this.namespace=a;
this.widgetName=b;
this.widgetEventPrefix=p[a][b].eventPrefix||b;
this.widgetBaseClass=a+"-"+b;
this.options=p.extend({},p.widget.defaults,p[a][b].defaults,p.metadata&&p.metadata.get(e)[b],f);
this.element=p(e).bind("setData."+b,function(h,i,g){if(h.target==e){return d._setData(i,g)
}}).bind("getData."+b,function(g,h){if(g.target==e){return d._getData(h)
}}).bind("remove",function(){return d.destroy()
})
};
p[a][b].prototype=p.extend({},p.widget.prototype,c);
p[a][b].getterSetter="option"
};
p.widget.prototype={_init:function(){},destroy:function(){this.element.removeData(this.widgetName).removeClass(this.widgetBaseClass+"-disabled "+this.namespace+"-state-disabled").removeAttr("aria-disabled")
},option:function(b,a){var c=b,d=this;
if(typeof b=="string"){if(a===undefined){return this._getData(b)
}c={};
c[b]=a
}p.each(c,function(f,e){d._setData(f,e)
})
},_getData:function(a){return this.options[a]
},_setData:function(b,a){this.options[b]=a;
if(b=="disabled"){this.element[a?"addClass":"removeClass"](this.widgetBaseClass+"-disabled "+this.namespace+"-state-disabled").attr("aria-disabled",a)
}},enable:function(){this._setData("disabled",false)
},disable:function(){this._setData("disabled",true)
},_trigger:function(b,a,g){var e=this.options[b],d=(b==this.widgetEventPrefix?b:this.widgetEventPrefix+b);
a=p.Event(a);
a.type=d;
if(a.originalEvent){for(var c=p.event.props.length,f;
c;
){f=p.event.props[--c];
a[f]=a.originalEvent[f]
}}this.element.trigger(a,g);
return !(p.isFunction(e)&&e.call(this.element[0],a,g)===false||a.isDefaultPrevented())
}};
p.widget.defaults={disabled:false};
p.ui.mouse={_mouseInit:function(){var a=this;
this.element.bind("mousedown."+this.widgetName,function(b){return a._mouseDown(b)
}).bind("click."+this.widgetName,function(b){if(a._preventClickEvent){a._preventClickEvent=false;
b.stopImmediatePropagation();
return false
}});
if(p.browser.msie){this._mouseUnselectable=this.element.attr("unselectable");
this.element.attr("unselectable","on")
}this.started=false
},_mouseDestroy:function(){this.element.unbind("."+this.widgetName);
(p.browser.msie&&this.element.attr("unselectable",this._mouseUnselectable))
},_mouseDown:function(b){b.originalEvent=b.originalEvent||{};
if(b.originalEvent.mouseHandled){return
}(this._mouseStarted&&this._mouseUp(b));
this._mouseDownEvent=b;
var c=this,a=(b.which==1),d=(typeof this.options.cancel=="string"?p(b.target).parents().add(b.target).filter(this.options.cancel).length:false);
if(!a||d||!this._mouseCapture(b)){return true
}this.mouseDelayMet=!this.options.delay;
if(!this.mouseDelayMet){this._mouseDelayTimer=setTimeout(function(){c.mouseDelayMet=true
},this.options.delay)
}if(this._mouseDistanceMet(b)&&this._mouseDelayMet(b)){this._mouseStarted=(this._mouseStart(b)!==false);
if(!this._mouseStarted){b.preventDefault();
return true
}}this._mouseMoveDelegate=function(e){return c._mouseMove(e)
};
this._mouseUpDelegate=function(e){return c._mouseUp(e)
};
p(document).bind("mousemove."+this.widgetName,this._mouseMoveDelegate).bind("mouseup."+this.widgetName,this._mouseUpDelegate);
(p.browser.safari||b.preventDefault());
b.originalEvent.mouseHandled=true;
return true
},_mouseMove:function(a){if(p.browser.msie&&!a.button){return this._mouseUp(a)
}if(this._mouseStarted){this._mouseDrag(a);
return a.preventDefault()
}if(this._mouseDistanceMet(a)&&this._mouseDelayMet(a)){this._mouseStarted=(this._mouseStart(this._mouseDownEvent,a)!==false);
(this._mouseStarted?this._mouseDrag(a):this._mouseUp(a))
}return !this._mouseStarted
},_mouseUp:function(a){p(document).unbind("mousemove."+this.widgetName,this._mouseMoveDelegate).unbind("mouseup."+this.widgetName,this._mouseUpDelegate);
if(this._mouseStarted){this._mouseStarted=false;
this._preventClickEvent=(a.target==this._mouseDownEvent.target);
this._mouseStop(a)
}return false
},_mouseDistanceMet:function(a){return(Math.max(Math.abs(this._mouseDownEvent.pageX-a.pageX),Math.abs(this._mouseDownEvent.pageY-a.pageY))>=this.options.distance)
},_mouseDelayMet:function(a){return this.mouseDelayMet
},_mouseStart:function(a){},_mouseDrag:function(a){},_mouseStop:function(a){},_mouseCapture:function(a){return true
}};
p.ui.mouse.defaults={cancel:null,distance:1,delay:0}
})(jQuery);