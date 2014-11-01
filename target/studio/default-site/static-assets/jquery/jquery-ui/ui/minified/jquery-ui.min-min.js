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
(function(b){b.widget("ui.draggable",b.extend({},b.ui.mouse,{_init:function(){if(this.options.helper=="original"&&!(/^(?:r|a|f)/).test(this.element.css("position"))){this.element[0].style.position="relative"
}(this.options.addClasses&&this.element.addClass("ui-draggable"));
(this.options.disabled&&this.element.addClass("ui-draggable-disabled"));
this._mouseInit()
},destroy:function(){if(!this.element.data("draggable")){return
}this.element.removeData("draggable").unbind(".draggable").removeClass("ui-draggable ui-draggable-dragging ui-draggable-disabled");
this._mouseDestroy()
},_mouseCapture:function(a){var d=this.options;
if(this.helper||d.disabled||b(a.target).is(".ui-resizable-handle")){return false
}this.handle=this._getHandle(a);
if(!this.handle){return false
}return true
},_mouseStart:function(a){var d=this.options;
this.helper=this._createHelper(a);
this._cacheHelperProportions();
if(b.ui.ddmanager){b.ui.ddmanager.current=this
}this._cacheMargins();
this.cssPosition=this.helper.css("position");
this.scrollParent=this.helper.scrollParent();
this.offset=this.element.offset();
this.offset={top:this.offset.top-this.margins.top,left:this.offset.left-this.margins.left};
b.extend(this.offset,{click:{left:a.pageX-this.offset.left,top:a.pageY-this.offset.top},parent:this._getParentOffset(),relative:this._getRelativeOffset()});
this.originalPosition=this._generatePosition(a);
this.originalPageX=a.pageX;
this.originalPageY=a.pageY;
if(d.cursorAt){this._adjustOffsetFromHelper(d.cursorAt)
}if(d.containment){this._setContainment()
}this._trigger("start",a);
this._cacheHelperProportions();
if(b.ui.ddmanager&&!d.dropBehaviour){b.ui.ddmanager.prepareOffsets(this,a)
}this.helper.addClass("ui-draggable-dragging");
this._mouseDrag(a,true);
return true
},_mouseDrag:function(a,e){this.position=this._generatePosition(a);
this.positionAbs=this._convertPositionTo("absolute");
if(!e){var f=this._uiHash();
this._trigger("drag",a,f);
this.position=f.position
}if(!this.options.axis||this.options.axis!="y"){this.helper[0].style.left=this.position.left+"px"
}if(!this.options.axis||this.options.axis!="x"){this.helper[0].style.top=this.position.top+"px"
}if(b.ui.ddmanager){b.ui.ddmanager.drag(this,a)
}return false
},_mouseStop:function(f){var e=false;
if(b.ui.ddmanager&&!this.options.dropBehaviour){e=b.ui.ddmanager.drop(this,f)
}if(this.dropped){e=this.dropped;
this.dropped=false
}if((this.options.revert=="invalid"&&!e)||(this.options.revert=="valid"&&e)||this.options.revert===true||(b.isFunction(this.options.revert)&&this.options.revert.call(this.element,e))){var a=this;
b(this.helper).animate(this.originalPosition,parseInt(this.options.revertDuration,10),function(){a._trigger("stop",f);
a._clear()
})
}else{this._trigger("stop",f);
this._clear()
}return false
},_getHandle:function(a){var d=!this.options.handle||!b(this.options.handle,this.element).length?true:false;
b(this.options.handle,this.element).find("*").andSelf().each(function(){if(this==a.target){d=true
}});
return d
},_createHelper:function(f){var e=this.options;
var a=b.isFunction(e.helper)?b(e.helper.apply(this.element[0],[f])):(e.helper=="clone"?this.element.clone():this.element);
if(!a.parents("body").length){a.appendTo((e.appendTo=="parent"?this.element[0].parentNode:e.appendTo))
}if(a[0]!=this.element[0]&&!(/(fixed|absolute)/).test(a.css("position"))){a.css("position","absolute")
}return a
},_adjustOffsetFromHelper:function(a){if(a.left!=undefined){this.offset.click.left=a.left+this.margins.left
}if(a.right!=undefined){this.offset.click.left=this.helperProportions.width-a.right+this.margins.left
}if(a.top!=undefined){this.offset.click.top=a.top+this.margins.top
}if(a.bottom!=undefined){this.offset.click.top=this.helperProportions.height-a.bottom+this.margins.top
}},_getParentOffset:function(){this.offsetParent=this.helper.offsetParent();
var a=this.offsetParent.offset();
if(this.cssPosition=="absolute"&&this.scrollParent[0]!=document&&b.ui.contains(this.scrollParent[0],this.offsetParent[0])){a.left+=this.scrollParent.scrollLeft();
a.top+=this.scrollParent.scrollTop()
}if((this.offsetParent[0]==document.body)||(this.offsetParent[0].tagName&&this.offsetParent[0].tagName.toLowerCase()=="html"&&b.browser.msie)){a={top:0,left:0}
}return{top:a.top+(parseInt(this.offsetParent.css("borderTopWidth"),10)||0),left:a.left+(parseInt(this.offsetParent.css("borderLeftWidth"),10)||0)}
},_getRelativeOffset:function(){if(this.cssPosition=="relative"){var a=this.element.position();
return{top:a.top-(parseInt(this.helper.css("top"),10)||0)+this.scrollParent.scrollTop(),left:a.left-(parseInt(this.helper.css("left"),10)||0)+this.scrollParent.scrollLeft()}
}else{return{top:0,left:0}
}},_cacheMargins:function(){this.margins={left:(parseInt(this.element.css("marginLeft"),10)||0),top:(parseInt(this.element.css("marginTop"),10)||0)}
},_cacheHelperProportions:function(){this.helperProportions={width:this.helper.outerWidth(),height:this.helper.outerHeight()}
},_setContainment:function(){var f=this.options;
if(f.containment=="parent"){f.containment=this.helper[0].parentNode
}if(f.containment=="document"||f.containment=="window"){this.containment=[0-this.offset.relative.left-this.offset.parent.left,0-this.offset.relative.top-this.offset.parent.top,b(f.containment=="document"?document:window).width()-this.helperProportions.width-this.margins.left,(b(f.containment=="document"?document:window).height()||document.body.parentNode.scrollHeight)-this.helperProportions.height-this.margins.top]
}if(!(/^(document|window|parent)$/).test(f.containment)&&f.containment.constructor!=Array){var h=b(f.containment)[0];
if(!h){return
}var g=b(f.containment).offset();
var a=(b(h).css("overflow")!="hidden");
this.containment=[g.left+(parseInt(b(h).css("borderLeftWidth"),10)||0)+(parseInt(b(h).css("paddingLeft"),10)||0)-this.margins.left,g.top+(parseInt(b(h).css("borderTopWidth"),10)||0)+(parseInt(b(h).css("paddingTop"),10)||0)-this.margins.top,g.left+(a?Math.max(h.scrollWidth,h.offsetWidth):h.offsetWidth)-(parseInt(b(h).css("borderLeftWidth"),10)||0)-(parseInt(b(h).css("paddingRight"),10)||0)-this.helperProportions.width-this.margins.left,g.top+(a?Math.max(h.scrollHeight,h.offsetHeight):h.offsetHeight)-(parseInt(b(h).css("borderTopWidth"),10)||0)-(parseInt(b(h).css("paddingBottom"),10)||0)-this.helperProportions.height-this.margins.top]
}else{if(f.containment.constructor==Array){this.containment=f.containment
}}},_convertPositionTo:function(j,d){if(!d){d=this.position
}var l=j=="absolute"?1:-1;
var k=this.options,a=this.cssPosition=="absolute"&&!(this.scrollParent[0]!=document&&b.ui.contains(this.scrollParent[0],this.offsetParent[0]))?this.offsetParent:this.scrollParent,i=(/(html|body)/i).test(a[0].tagName);
return{top:(d.top+this.offset.relative.top*l+this.offset.parent.top*l-(b.browser.safari&&this.cssPosition=="fixed"?0:(this.cssPosition=="fixed"?-this.scrollParent.scrollTop():(i?0:a.scrollTop()))*l)),left:(d.left+this.offset.relative.left*l+this.offset.parent.left*l-(b.browser.safari&&this.cssPosition=="fixed"?0:(this.cssPosition=="fixed"?-this.scrollParent.scrollLeft():i?0:a.scrollLeft())*l))}
},_generatePosition:function(n){var k=this.options,a=this.cssPosition=="absolute"&&!(this.scrollParent[0]!=document&&b.ui.contains(this.scrollParent[0],this.offsetParent[0]))?this.offsetParent:this.scrollParent,j=(/(html|body)/i).test(a[0].tagName);
if(this.cssPosition=="relative"&&!(this.scrollParent[0]!=document&&this.scrollParent[0]!=this.offsetParent[0])){this.offset.relative=this._getRelativeOffset()
}var o=n.pageX;
var p=n.pageY;
if(this.originalPosition){if(this.containment){if(n.pageX-this.offset.click.left<this.containment[0]){o=this.containment[0]+this.offset.click.left
}if(n.pageY-this.offset.click.top<this.containment[1]){p=this.containment[1]+this.offset.click.top
}if(n.pageX-this.offset.click.left>this.containment[2]){o=this.containment[2]+this.offset.click.left
}if(n.pageY-this.offset.click.top>this.containment[3]){p=this.containment[3]+this.offset.click.top
}}if(k.grid){var l=this.originalPageY+Math.round((p-this.originalPageY)/k.grid[1])*k.grid[1];
p=this.containment?(!(l-this.offset.click.top<this.containment[1]||l-this.offset.click.top>this.containment[3])?l:(!(l-this.offset.click.top<this.containment[1])?l-k.grid[1]:l+k.grid[1])):l;
var m=this.originalPageX+Math.round((o-this.originalPageX)/k.grid[0])*k.grid[0];
o=this.containment?(!(m-this.offset.click.left<this.containment[0]||m-this.offset.click.left>this.containment[2])?m:(!(m-this.offset.click.left<this.containment[0])?m-k.grid[0]:m+k.grid[0])):m
}}return{top:(p-this.offset.click.top-this.offset.relative.top-this.offset.parent.top+(b.browser.safari&&this.cssPosition=="fixed"?0:(this.cssPosition=="fixed"?-this.scrollParent.scrollTop():(j?0:a.scrollTop())))),left:(o-this.offset.click.left-this.offset.relative.left-this.offset.parent.left+(b.browser.safari&&this.cssPosition=="fixed"?0:(this.cssPosition=="fixed"?-this.scrollParent.scrollLeft():j?0:a.scrollLeft())))}
},_clear:function(){this.helper.removeClass("ui-draggable-dragging");
if(this.helper[0]!=this.element[0]&&!this.cancelHelperRemoval){this.helper.remove()
}this.helper=null;
this.cancelHelperRemoval=false
},_trigger:function(a,f,e){e=e||this._uiHash();
b.ui.plugin.call(this,a,[f,e]);
if(a=="drag"){this.positionAbs=this._convertPositionTo("absolute")
}return b.widget.prototype._trigger.call(this,a,f,e)
},plugins:{},_uiHash:function(a){return{helper:this.helper,position:this.position,absolutePosition:this.positionAbs,offset:this.positionAbs}
}}));
b.extend(b.ui.draggable,{version:"1.7.2",eventPrefix:"drag",defaults:{addClasses:true,appendTo:"parent",axis:false,cancel:":input,option",connectToSortable:false,containment:false,cursor:"auto",cursorAt:false,delay:0,distance:1,grid:false,handle:false,helper:"original",iframeFix:false,opacity:false,refreshPositions:false,revert:false,revertDuration:500,scope:"default",scroll:true,scrollSensitivity:20,scrollSpeed:20,snap:false,snapMode:"both",snapTolerance:20,stack:false,zIndex:false}});
b.ui.plugin.add("draggable","connectToSortable",{start:function(j,h){var i=b(this).data("draggable"),g=i.options,a=b.extend({},h,{item:i.element});
i.sortables=[];
b(g.connectToSortable).each(function(){var c=b.data(this,"sortable");
if(c&&!c.options.disabled){i.sortables.push({instance:c,shouldRevert:c.options.revert});
c._refreshItems();
c._trigger("activate",j,a)
}})
},stop:function(h,f){var g=b(this).data("draggable"),a=b.extend({},f,{item:g.element});
b.each(g.sortables,function(){if(this.instance.isOver){this.instance.isOver=0;
g.cancelHelperRemoval=true;
this.instance.cancelHelperRemoval=false;
if(this.shouldRevert){this.instance.options.revert=true
}this.instance._mouseStop(h);
this.instance.options.helper=this.instance.options._helper;
if(g.options.helper=="original"){this.instance.currentItem.css({top:"auto",left:"auto"})
}}else{this.instance.cancelHelperRemoval=false;
this.instance._trigger("deactivate",h,a)
}})
},drag:function(j,g){var h=b(this).data("draggable"),a=this;
var i=function(r){var d=this.offset.click.top,e=this.offset.click.left;
var u=this.positionAbs.top,o=this.positionAbs.left;
var q=r.height,f=r.width;
var c=r.top,s=r.left;
return b.ui.isOver(u+d,o+e,c,s,q,f)
};
b.each(h.sortables,function(c){this.instance.positionAbs=h.positionAbs;
this.instance.helperProportions=h.helperProportions;
this.instance.offset.click=h.offset.click;
if(this.instance._intersectsWith(this.instance.containerCache)){if(!this.instance.isOver){this.instance.isOver=1;
this.instance.currentItem=b(a).clone().appendTo(this.instance.element).data("sortable-item",true);
this.instance.options._helper=this.instance.options.helper;
this.instance.options.helper=function(){return g.helper[0]
};
j.target=this.instance.currentItem[0];
this.instance._mouseCapture(j,true);
this.instance._mouseStart(j,true,true);
this.instance.offset.click.top=h.offset.click.top;
this.instance.offset.click.left=h.offset.click.left;
this.instance.offset.parent.left-=h.offset.parent.left-this.instance.offset.parent.left;
this.instance.offset.parent.top-=h.offset.parent.top-this.instance.offset.parent.top;
h._trigger("toSortable",j);
h.dropped=this.instance.element;
h.currentItem=h.element;
this.instance.fromOutside=h
}if(this.instance.currentItem){this.instance._mouseDrag(j)
}}else{if(this.instance.isOver){this.instance.isOver=0;
this.instance.cancelHelperRemoval=true;
this.instance.options.revert=false;
this.instance._trigger("out",j,this.instance._uiHash(this.instance));
this.instance._mouseStop(j,true);
this.instance.options.helper=this.instance.options._helper;
this.instance.currentItem.remove();
if(this.instance.placeholder){this.instance.placeholder.remove()
}h._trigger("fromSortable",j);
h.dropped=false
}}})
}});
b.ui.plugin.add("draggable","cursor",{start:function(h,g){var a=b("body"),f=b(this).data("draggable").options;
if(a.css("cursor")){f._cursor=a.css("cursor")
}a.css("cursor",f.cursor)
},stop:function(a,f){var e=b(this).data("draggable").options;
if(e._cursor){b("body").css("cursor",e._cursor)
}}});
b.ui.plugin.add("draggable","iframeFix",{start:function(a,f){var e=b(this).data("draggable").options;
b(e.iframeFix===true?"iframe":e.iframeFix).each(function(){b('<div class="ui-draggable-iframeFix" style="background: #fff;"></div>').css({width:this.offsetWidth+"px",height:this.offsetHeight+"px",position:"absolute",opacity:"0.001",zIndex:1000}).css(b(this).offset()).appendTo("body")
})
},stop:function(a,d){b("div.ui-draggable-iframeFix").each(function(){this.parentNode.removeChild(this)
})
}});
b.ui.plugin.add("draggable","opacity",{start:function(h,g){var a=b(g.helper),f=b(this).data("draggable").options;
if(a.css("opacity")){f._opacity=a.css("opacity")
}a.css("opacity",f.opacity)
},stop:function(a,f){var e=b(this).data("draggable").options;
if(e._opacity){b(f.helper).css("opacity",e._opacity)
}}});
b.ui.plugin.add("draggable","scroll",{start:function(f,e){var a=b(this).data("draggable");
if(a.scrollParent[0]!=document&&a.scrollParent[0].tagName!="HTML"){a.overflowOffset=a.scrollParent.offset()
}},drag:function(i,h){var j=b(this).data("draggable"),g=j.options,a=false;
if(j.scrollParent[0]!=document&&j.scrollParent[0].tagName!="HTML"){if(!g.axis||g.axis!="x"){if((j.overflowOffset.top+j.scrollParent[0].offsetHeight)-i.pageY<g.scrollSensitivity){j.scrollParent[0].scrollTop=a=j.scrollParent[0].scrollTop+g.scrollSpeed
}else{if(i.pageY-j.overflowOffset.top<g.scrollSensitivity){j.scrollParent[0].scrollTop=a=j.scrollParent[0].scrollTop-g.scrollSpeed
}}}if(!g.axis||g.axis!="y"){if((j.overflowOffset.left+j.scrollParent[0].offsetWidth)-i.pageX<g.scrollSensitivity){j.scrollParent[0].scrollLeft=a=j.scrollParent[0].scrollLeft+g.scrollSpeed
}else{if(i.pageX-j.overflowOffset.left<g.scrollSensitivity){j.scrollParent[0].scrollLeft=a=j.scrollParent[0].scrollLeft-g.scrollSpeed
}}}}else{if(!g.axis||g.axis!="x"){if(i.pageY-b(document).scrollTop()<g.scrollSensitivity){a=b(document).scrollTop(b(document).scrollTop()-g.scrollSpeed)
}else{if(b(window).height()-(i.pageY-b(document).scrollTop())<g.scrollSensitivity){a=b(document).scrollTop(b(document).scrollTop()+g.scrollSpeed)
}}}if(!g.axis||g.axis!="y"){if(i.pageX-b(document).scrollLeft()<g.scrollSensitivity){a=b(document).scrollLeft(b(document).scrollLeft()-g.scrollSpeed)
}else{if(b(window).width()-(i.pageX-b(document).scrollLeft())<g.scrollSensitivity){a=b(document).scrollLeft(b(document).scrollLeft()+g.scrollSpeed)
}}}}if(a!==false&&b.ui.ddmanager&&!g.dropBehaviour){b.ui.ddmanager.prepareOffsets(j,i)
}}});
b.ui.plugin.add("draggable","snap",{start:function(h,g){var a=b(this).data("draggable"),f=a.options;
a.snapElements=[];
b(f.snap.constructor!=String?(f.snap.items||":data(draggable)"):f.snap).each(function(){var c=b(this);
var d=c.offset();
if(this!=a.element[0]){a.snapElements.push({item:this,width:c.outerWidth(),height:c.outerHeight(),top:d.top,left:d.left})
}})
},drag:function(r,E){var K=b(this).data("draggable"),C=K.options;
var d=C.snapTolerance;
var i=E.offset.left,l=i+K.helperProportions.width,L=E.offset.top,M=L+K.helperProportions.height;
for(var o=K.snapElements.length-1;
o>=0;
o--){var B=K.snapElements[o].left,F=B+K.snapElements[o].width,G=K.snapElements[o].top,D=G+K.snapElements[o].height;
if(!((B-d<i&&i<F+d&&G-d<L&&L<D+d)||(B-d<i&&i<F+d&&G-d<M&&M<D+d)||(B-d<l&&l<F+d&&G-d<L&&L<D+d)||(B-d<l&&l<F+d&&G-d<M&&M<D+d))){if(K.snapElements[o].snapping){(K.options.snap.release&&K.options.snap.release.call(K.element,r,b.extend(K._uiHash(),{snapItem:K.snapElements[o].item})))
}K.snapElements[o].snapping=false;
continue
}if(C.snapMode!="inner"){var N=Math.abs(G-M)<=d;
var a=Math.abs(D-L)<=d;
var I=Math.abs(B-l)<=d;
var H=Math.abs(F-i)<=d;
if(N){E.position.top=K._convertPositionTo("relative",{top:G-K.helperProportions.height,left:0}).top-K.margins.top
}if(a){E.position.top=K._convertPositionTo("relative",{top:D,left:0}).top-K.margins.top
}if(I){E.position.left=K._convertPositionTo("relative",{top:0,left:B-K.helperProportions.width}).left-K.margins.left
}if(H){E.position.left=K._convertPositionTo("relative",{top:0,left:F}).left-K.margins.left
}}var J=(N||a||I||H);
if(C.snapMode!="outer"){var N=Math.abs(G-L)<=d;
var a=Math.abs(D-M)<=d;
var I=Math.abs(B-i)<=d;
var H=Math.abs(F-l)<=d;
if(N){E.position.top=K._convertPositionTo("relative",{top:G,left:0}).top-K.margins.top
}if(a){E.position.top=K._convertPositionTo("relative",{top:D-K.helperProportions.height,left:0}).top-K.margins.top
}if(I){E.position.left=K._convertPositionTo("relative",{top:0,left:B}).left-K.margins.left
}if(H){E.position.left=K._convertPositionTo("relative",{top:0,left:F-K.helperProportions.width}).left-K.margins.left
}}if(!K.snapElements[o].snapping&&(N||a||I||H||J)){(K.options.snap.snap&&K.options.snap.snap.call(K.element,r,b.extend(K._uiHash(),{snapItem:K.snapElements[o].item})))
}K.snapElements[o].snapping=(N||a||I||H||J)
}}});
b.ui.plugin.add("draggable","stack",{start:function(a,h){var f=b(this).data("draggable").options;
var g=b.makeArray(b(f.stack.group)).sort(function(c,d){return(parseInt(b(c).css("zIndex"),10)||f.stack.min)-(parseInt(b(d).css("zIndex"),10)||f.stack.min)
});
b(g).each(function(c){this.style.zIndex=f.stack.min+c
});
this[0].style.zIndex=f.stack.min+g.length
}});
b.ui.plugin.add("draggable","zIndex",{start:function(h,g){var a=b(g.helper),f=b(this).data("draggable").options;
if(a.css("zIndex")){f._zIndex=a.css("zIndex")
}a.css("zIndex",f.zIndex)
},stop:function(a,f){var e=b(this).data("draggable").options;
if(e._zIndex){b(f.helper).css("zIndex",e._zIndex)
}}})
})(jQuery);
(function(b){b.widget("ui.droppable",{_init:function(){var d=this.options,a=d.accept;
this.isover=0;
this.isout=1;
this.options.accept=this.options.accept&&b.isFunction(this.options.accept)?this.options.accept:function(c){return c.is(a)
};
this.proportions={width:this.element[0].offsetWidth,height:this.element[0].offsetHeight};
b.ui.ddmanager.droppables[this.options.scope]=b.ui.ddmanager.droppables[this.options.scope]||[];
b.ui.ddmanager.droppables[this.options.scope].push(this);
(this.options.addClasses&&this.element.addClass("ui-droppable"))
},destroy:function(){var a=b.ui.ddmanager.droppables[this.options.scope];
for(var d=0;
d<a.length;
d++){if(a[d]==this){a.splice(d,1)
}}this.element.removeClass("ui-droppable ui-droppable-disabled").removeData("droppable").unbind(".droppable")
},_setData:function(a,d){if(a=="accept"){this.options.accept=d&&b.isFunction(d)?d:function(c){return c.is(d)
}
}else{b.widget.prototype._setData.apply(this,arguments)
}},_activate:function(d){var a=b.ui.ddmanager.current;
if(this.options.activeClass){this.element.addClass(this.options.activeClass)
}(a&&this._trigger("activate",d,this.ui(a)))
},_deactivate:function(d){var a=b.ui.ddmanager.current;
if(this.options.activeClass){this.element.removeClass(this.options.activeClass)
}(a&&this._trigger("deactivate",d,this.ui(a)))
},_over:function(d){var a=b.ui.ddmanager.current;
if(!a||(a.currentItem||a.element)[0]==this.element[0]){return
}if(this.options.accept.call(this.element[0],(a.currentItem||a.element))){if(this.options.hoverClass){this.element.addClass(this.options.hoverClass)
}this._trigger("over",d,this.ui(a))
}},_out:function(d){var a=b.ui.ddmanager.current;
if(!a||(a.currentItem||a.element)[0]==this.element[0]){return
}if(this.options.accept.call(this.element[0],(a.currentItem||a.element))){if(this.options.hoverClass){this.element.removeClass(this.options.hoverClass)
}this._trigger("out",d,this.ui(a))
}},_drop:function(h,g){var a=g||b.ui.ddmanager.current;
if(!a||(a.currentItem||a.element)[0]==this.element[0]){return false
}var f=false;
this.element.find(":data(droppable)").not(".ui-draggable-dragging").each(function(){var c=b.data(this,"droppable");
if(c.options.greedy&&b.ui.intersect(a,b.extend(c,{offset:c.element.offset()}),c.options.tolerance)){f=true;
return false
}});
if(f){return false
}if(this.options.accept.call(this.element[0],(a.currentItem||a.element))){if(this.options.activeClass){this.element.removeClass(this.options.activeClass)
}if(this.options.hoverClass){this.element.removeClass(this.options.hoverClass)
}this._trigger("drop",h,this.ui(a));
return this.element
}return false
},ui:function(a){return{draggable:(a.currentItem||a.element),helper:a.helper,position:a.position,absolutePosition:a.positionAbs,offset:a.positionAbs}
}});
b.extend(b.ui.droppable,{version:"1.7.2",eventPrefix:"drop",defaults:{accept:"*",activeClass:false,addClasses:true,greedy:false,hoverClass:false,scope:"default",tolerance:"intersect"}});
b.ui.intersect=function(a,w,r){if(!w.offset){return false
}var B=(a.positionAbs||a.position.absolute).left,C=B+a.helperProportions.width,s=(a.positionAbs||a.position.absolute).top,u=s+a.helperProportions.height;
var z=w.offset.left,D=z+w.proportions.width,l=w.offset.top,v=l+w.proportions.height;
switch(r){case"fit":return(z<B&&C<D&&l<s&&u<v);
break;
case"intersect":return(z<B+(a.helperProportions.width/2)&&C-(a.helperProportions.width/2)<D&&l<s+(a.helperProportions.height/2)&&u-(a.helperProportions.height/2)<v);
break;
case"pointer":var y=((a.positionAbs||a.position.absolute).left+(a.clickOffset||a.offset.click).left),x=((a.positionAbs||a.position.absolute).top+(a.clickOffset||a.offset.click).top),A=b.ui.isOver(x,y,l,z,w.proportions.height,w.proportions.width);
return A;
break;
case"touch":return((s>=l&&s<=v)||(u>=l&&u<=v)||(s<l&&u>v))&&((B>=z&&B<=D)||(C>=z&&C<=D)||(B<z&&C>D));
break;
default:return false;
break
}};
b.ui.ddmanager={current:null,droppables:{"default":[]},prepareOffsets:function(l,j){var a=b.ui.ddmanager.droppables[l.options.scope];
var k=j?j.type:null;
var i=(l.currentItem||l.element).find(":data(droppable)").andSelf();
droppablesLoop:for(var m=0;
m<a.length;
m++){if(a[m].options.disabled||(l&&!a[m].options.accept.call(a[m].element[0],(l.currentItem||l.element)))){continue
}for(var n=0;
n<i.length;
n++){if(i[n]==a[m].element[0]){a[m].proportions.height=0;
continue droppablesLoop
}}a[m].visible=a[m].element.css("display")!="none";
if(!a[m].visible){continue
}a[m].offset=a[m].element.offset();
a[m].proportions={width:a[m].element[0].offsetWidth,height:a[m].element[0].offsetHeight};
if(k=="mousedown"){a[m]._activate.call(a[m],j)
}}},drop:function(a,f){var e=false;
b.each(b.ui.ddmanager.droppables[a.options.scope],function(){if(!this.options){return
}if(!this.options.disabled&&this.visible&&b.ui.intersect(a,this,this.options.tolerance)){e=this._drop.call(this,f)
}if(!this.options.disabled&&this.visible&&this.options.accept.call(this.element[0],(a.currentItem||a.element))){this.isout=1;
this.isover=0;
this._deactivate.call(this,f)
}});
return e
},drag:function(a,d){if(a.options.refreshPositions){b.ui.ddmanager.prepareOffsets(a,d)
}b.each(b.ui.ddmanager.droppables[a.options.scope],function(){if(this.options.disabled||this.greedyChild||!this.visible){return
}var i=b.ui.intersect(a,this,this.options.tolerance);
var c=!i&&this.isover==1?"isout":(i&&this.isover==0?"isover":null);
if(!c){return
}var h;
if(this.options.greedy){var j=this.element.parents(":data(droppable):eq(0)");
if(j.length){h=b.data(j[0],"droppable");
h.greedyChild=(c=="isover"?1:0)
}}if(h&&c=="isover"){h.isover=0;
h.isout=1;
h._out.call(h,d)
}this[c]=1;
this[c=="isout"?"isover":"isout"]=0;
this[c=="isover"?"_over":"_out"].call(this,d);
if(h&&c=="isout"){h.isout=0;
h.isover=1;
h._over.call(h,d)
}})
}}
})(jQuery);
(function(f){f.widget("ui.resizable",f.extend({},f.ui.mouse,{_init:function(){var m=this,b=this.options;
this.element.addClass("ui-resizable");
f.extend(this,{_aspectRatio:!!(b.aspectRatio),aspectRatio:b.aspectRatio,originalElement:this.element,_proportionallyResizeElements:[],_helper:b.helper||b.ghost||b.animate?b.helper||"ui-resizable-helper":null});
if(this.element[0].nodeName.match(/canvas|textarea|input|select|button|img/i)){if(/relative/.test(this.element.css("position"))&&f.browser.opera){this.element.css({position:"relative",top:"auto",left:"auto"})
}this.element.wrap(f('<div class="ui-wrapper" style="overflow: hidden;"></div>').css({position:this.element.css("position"),width:this.element.outerWidth(),height:this.element.outerHeight(),top:this.element.css("top"),left:this.element.css("left")}));
this.element=this.element.parent().data("resizable",this.element.data("resizable"));
this.elementIsWrapper=true;
this.element.css({marginLeft:this.originalElement.css("marginLeft"),marginTop:this.originalElement.css("marginTop"),marginRight:this.originalElement.css("marginRight"),marginBottom:this.originalElement.css("marginBottom")});
this.originalElement.css({marginLeft:0,marginTop:0,marginRight:0,marginBottom:0});
this.originalResizeStyle=this.originalElement.css("resize");
this.originalElement.css("resize","none");
this._proportionallyResizeElements.push(this.originalElement.css({position:"static",zoom:1,display:"block"}));
this.originalElement.css({margin:this.originalElement.css("margin")});
this._proportionallyResize()
}this.handles=b.handles||(!f(".ui-resizable-handle",this.element).length?"e,s,se":{n:".ui-resizable-n",e:".ui-resizable-e",s:".ui-resizable-s",w:".ui-resizable-w",se:".ui-resizable-se",sw:".ui-resizable-sw",ne:".ui-resizable-ne",nw:".ui-resizable-nw"});
if(this.handles.constructor==String){if(this.handles=="all"){this.handles="n,e,s,w,se,sw,ne,nw"
}var a=this.handles.split(",");
this.handles={};
for(var l=0;
l<a.length;
l++){var c=f.trim(a[l]),n="ui-resizable-"+c;
var i=f('<div class="ui-resizable-handle '+n+'"></div>');
if(/sw|se|ne|nw/.test(c)){i.css({zIndex:++b.zIndex})
}if("se"==c){i.addClass("ui-icon ui-icon-gripsmall-diagonal-se")
}this.handles[c]=".ui-resizable-"+c;
this.element.append(i)
}}this._renderAxis=function(j){j=j||this.element;
for(var g in this.handles){if(this.handles[g].constructor==String){this.handles[g]=f(this.handles[g],this.element).show()
}if(this.elementIsWrapper&&this.originalElement[0].nodeName.match(/textarea|input|select|button/i)){var q=f(this.handles[g],this.element),k=0;
k=/sw|ne|nw|se|n|s/.test(g)?q.outerHeight():q.outerWidth();
var h=["padding",/ne|nw|n/.test(g)?"Top":/se|sw|s/.test(g)?"Bottom":/^e$/.test(g)?"Right":"Left"].join("");
j.css(h,k);
this._proportionallyResize()
}if(!f(this.handles[g]).length){continue
}}};
this._renderAxis(this.element);
this._handles=f(".ui-resizable-handle",this.element).disableSelection();
this._handles.mouseover(function(){if(!m.resizing){if(this.className){var g=this.className.match(/ui-resizable-(se|sw|ne|nw|n|e|s|w)/i)
}m.axis=g&&g[1]?g[1]:"se"
}});
if(b.autoHide){this._handles.hide();
f(this.element).addClass("ui-resizable-autohide").hover(function(){f(this).removeClass("ui-resizable-autohide");
m._handles.show()
},function(){if(!m.resizing){f(this).addClass("ui-resizable-autohide");
m._handles.hide()
}})
}this._mouseInit()
},destroy:function(){this._mouseDestroy();
var b=function(c){f(c).removeClass("ui-resizable ui-resizable-disabled ui-resizable-resizing").removeData("resizable").unbind(".resizable").find(".ui-resizable-handle").remove()
};
if(this.elementIsWrapper){b(this.element);
var a=this.element;
a.parent().append(this.originalElement.css({position:a.css("position"),width:a.outerWidth(),height:a.outerHeight(),top:a.css("top"),left:a.css("left")})).end().remove()
}this.originalElement.css("resize",this.originalResizeStyle);
b(this.originalElement)
},_mouseCapture:function(b){var a=false;
for(var c in this.handles){if(f(this.handles[c])[0]==b.target){a=true
}}return this.options.disabled||!!a
},_mouseStart:function(l){var b=this.options,m=this.element.position(),n=this.element;
this.resizing=true;
this.documentScroll={top:f(document).scrollTop(),left:f(document).scrollLeft()};
if(n.is(".ui-draggable")||(/absolute/).test(n.css("position"))){n.css({position:"absolute",top:m.top,left:m.left})
}if(f.browser.opera&&(/relative/).test(n.css("position"))){n.css({position:"relative",top:"auto",left:"auto"})
}this._renderProxy();
var a=d(this.helper.css("left")),k=d(this.helper.css("top"));
if(b.containment){a+=f(b.containment).scrollLeft()||0;
k+=f(b.containment).scrollTop()||0
}this.offset=this.helper.offset();
this.position={left:a,top:k};
this.size=this._helper?{width:n.outerWidth(),height:n.outerHeight()}:{width:n.width(),height:n.height()};
this.originalSize=this._helper?{width:n.outerWidth(),height:n.outerHeight()}:{width:n.width(),height:n.height()};
this.originalPosition={left:a,top:k};
this.sizeDiff={width:n.outerWidth()-n.width(),height:n.outerHeight()-n.height()};
this.originalMousePosition={left:l.pageX,top:l.pageY};
this.aspectRatio=(typeof b.aspectRatio=="number")?b.aspectRatio:((this.originalSize.width/this.originalSize.height)||1);
var c=f(".ui-resizable-"+this.axis).css("cursor");
f("body").css("cursor",c=="auto"?this.axis+"-resize":c);
n.addClass("ui-resizable-resizing");
this._propagate("start",l);
return true
},_mouseDrag:function(A){var x=this.helper,y=this.options,r={},b=this,v=this.originalMousePosition,o=this.axis;
var a=(A.pageX-v.left)||0,c=(A.pageY-v.top)||0;
var w=this._change[o];
if(!w){return false
}var s=w.apply(this,[A,a,c]),u=f.browser.msie&&f.browser.version<7,z=this.sizeDiff;
if(this._aspectRatio||A.shiftKey){s=this._updateRatio(s,A)
}s=this._respectSize(s,A);
this._propagate("resize",A);
x.css({top:this.position.top+"px",left:this.position.left+"px",width:this.size.width+"px",height:this.size.height+"px"});
if(!this._helper&&this._proportionallyResizeElements.length){this._proportionallyResize()
}this._updateCache(s);
this._trigger("resize",A,this.ui());
return false
},_mouseStop:function(q){this.resizing=false;
var p=this.options,b=this;
if(this._helper){var r=this._proportionallyResizeElements,u=r.length&&(/textarea/i).test(r[0].nodeName),s=u&&f.ui.hasScroll(r[0],"left")?0:b.sizeDiff.height,n=u?0:b.sizeDiff.width;
var a={width:(b.size.width-n),height:(b.size.height-s)},o=(parseInt(b.element.css("left"),10)+(b.position.left-b.originalPosition.left))||null,c=(parseInt(b.element.css("top"),10)+(b.position.top-b.originalPosition.top))||null;
if(!p.animate){this.element.css(f.extend(a,{top:c,left:o}))
}b.helper.height(b.size.height);
b.helper.width(b.size.width);
if(this._helper&&!p.animate){this._proportionallyResize()
}}f("body").css("cursor","auto");
this.element.removeClass("ui-resizable-resizing");
this._propagate("stop",q);
if(this._helper){this.helper.remove()
}return false
},_updateCache:function(b){var a=this.options;
this.offset=this.helper.offset();
if(e(b.left)){this.position.left=b.left
}if(e(b.top)){this.position.top=b.top
}if(e(b.height)){this.size.height=b.height
}if(e(b.width)){this.size.width=b.width
}},_updateRatio:function(c,j){var b=this.options,a=this.position,k=this.size,l=this.axis;
if(c.height){c.width=(k.height*this.aspectRatio)
}else{if(c.width){c.height=(k.width/this.aspectRatio)
}}if(l=="sw"){c.left=a.left+(k.width-c.width);
c.top=null
}if(l=="nw"){c.top=a.top+(k.height-c.height);
c.left=a.left+(k.width-c.width)
}return c
},_respectSize:function(w,B){var y=this.helper,z=this.options,b=this._aspectRatio||B.shiftKey,c=this.axis,E=e(w.width)&&z.maxWidth&&(z.maxWidth<w.width),v=e(w.height)&&z.maxHeight&&(z.maxHeight<w.height),A=e(w.width)&&z.minWidth&&(z.minWidth>w.width),a=e(w.height)&&z.minHeight&&(z.minHeight>w.height);
if(A){w.width=z.minWidth
}if(a){w.height=z.minHeight
}if(E){w.width=z.maxWidth
}if(v){w.height=z.maxHeight
}var C=this.originalPosition.left+this.originalSize.width,o=this.position.top+this.size.height;
var x=/sw|nw|w/.test(c),D=/nw|ne|n/.test(c);
if(A&&x){w.left=C-z.minWidth
}if(E&&x){w.left=C-z.maxWidth
}if(a&&D){w.top=o-z.minHeight
}if(v&&D){w.top=o-z.maxHeight
}var u=!w.width&&!w.height;
if(u&&!w.left&&w.top){w.top=null
}else{if(u&&!w.top&&w.left){w.left=null
}}return w
},_proportionallyResize:function(){var a=this.options;
if(!this._proportionallyResizeElements.length){return
}var i=this.helper||this.element;
for(var k=0;
k<this._proportionallyResizeElements.length;
k++){var c=this._proportionallyResizeElements[k];
if(!this.borderDif){var l=[c.css("borderTopWidth"),c.css("borderRightWidth"),c.css("borderBottomWidth"),c.css("borderLeftWidth")],b=[c.css("paddingTop"),c.css("paddingRight"),c.css("paddingBottom"),c.css("paddingLeft")];
this.borderDif=f.map(l,function(j,g){var h=parseInt(j,10)||0,o=parseInt(b[g],10)||0;
return h+o
})
}if(f.browser.msie&&!(!(f(i).is(":hidden")||f(i).parents(":hidden").length))){continue
}c.css({height:(i.height()-this.borderDif[0]-this.borderDif[2])||0,width:(i.width()-this.borderDif[1]-this.borderDif[3])||0})
}},_renderProxy:function(){var i=this.element,a=this.options;
this.elementOffset=i.offset();
if(this._helper){this.helper=this.helper||f('<div style="overflow:hidden;"></div>');
var j=f.browser.msie&&f.browser.version<7,c=(j?1:0),b=(j?2:-1);
this.helper.addClass(this._helper).css({width:this.element.outerWidth()+b,height:this.element.outerHeight()+b,position:"absolute",left:this.elementOffset.left-c+"px",top:this.elementOffset.top-c+"px",zIndex:++a.zIndex});
this.helper.appendTo("body").disableSelection()
}else{this.helper=this.element
}},_change:{e:function(a,b,c){return{width:this.originalSize.width+b}
},w:function(c,k,l){var a=this.options,j=this.originalSize,b=this.originalPosition;
return{left:b.left+k,width:j.width-k}
},n:function(c,k,l){var a=this.options,j=this.originalSize,b=this.originalPosition;
return{top:b.top+l,height:j.height-l}
},s:function(a,b,c){return{height:this.originalSize.height+c}
},se:function(a,b,c){return f.extend(this._change.s.apply(this,arguments),this._change.e.apply(this,[a,b,c]))
},sw:function(a,b,c){return f.extend(this._change.s.apply(this,arguments),this._change.w.apply(this,[a,b,c]))
},ne:function(a,b,c){return f.extend(this._change.n.apply(this,arguments),this._change.e.apply(this,[a,b,c]))
},nw:function(a,b,c){return f.extend(this._change.n.apply(this,arguments),this._change.w.apply(this,[a,b,c]))
}},_propagate:function(a,b){f.ui.plugin.call(this,a,[b,this.ui()]);
(a!="resize"&&this._trigger(a,b,this.ui()))
},plugins:{},ui:function(){return{originalElement:this.originalElement,element:this.element,helper:this.helper,position:this.position,size:this.size,originalSize:this.originalSize,originalPosition:this.originalPosition}
}}));
f.extend(f.ui.resizable,{version:"1.7.2",eventPrefix:"resize",defaults:{alsoResize:false,animate:false,animateDuration:"slow",animateEasing:"swing",aspectRatio:false,autoHide:false,cancel:":input,option",containment:false,delay:0,distance:1,ghost:false,grid:false,handles:"e,s,se",helper:false,maxHeight:null,maxWidth:null,minHeight:10,minWidth:10,zIndex:1000}});
f.ui.plugin.add("resizable","alsoResize",{start:function(c,b){var h=f(this).data("resizable"),a=h.options;
_store=function(g){f(g).each(function(){f(this).data("resizable-alsoresize",{width:parseInt(f(this).width(),10),height:parseInt(f(this).height(),10),left:parseInt(f(this).css("left"),10),top:parseInt(f(this).css("top"),10)})
})
};
if(typeof(a.alsoResize)=="object"&&!a.alsoResize.parentNode){if(a.alsoResize.length){a.alsoResize=a.alsoResize[0];
_store(a.alsoResize)
}else{f.each(a.alsoResize,function(j,g){_store(j)
})
}}else{_store(a.alsoResize)
}},resize:function(n,l){var o=f(this).data("resizable"),c=o.options,m=o.originalSize,a=o.originalPosition;
var b={height:(o.size.height-m.height)||0,width:(o.size.width-m.width)||0,top:(o.position.top-a.top)||0,left:(o.position.left-a.left)||0},p=function(h,g){f(h).each(function(){var j=f(this),i=f(this).data("resizable-alsoresize"),k={},r=g&&g.length?g:["width","height","top","left"];
f.each(r||["width","height","top","left"],function(w,q){var v=(i[q]||0)+(b[q]||0);
if(v&&v>=0){k[q]=v||null
}});
if(/relative/.test(j.css("position"))&&f.browser.opera){o._revertToRelativePosition=true;
j.css({position:"absolute",top:"auto",left:"auto"})
}j.css(k)
})
};
if(typeof(c.alsoResize)=="object"&&!c.alsoResize.nodeType){f.each(c.alsoResize,function(h,g){p(h,g)
})
}else{p(c.alsoResize)
}},stop:function(b,a){var c=f(this).data("resizable");
if(c._revertToRelativePosition&&f.browser.opera){c._revertToRelativePosition=false;
el.css({position:"relative"})
}f(this).removeData("resizable-alsoresize-start")
}});
f.ui.plugin.add("resizable","animate",{stop:function(r,b){var a=f(this).data("resizable"),q=a.options;
var s=a._proportionallyResizeElements,w=s.length&&(/textarea/i).test(s[0].nodeName),v=w&&f.ui.hasScroll(s[0],"left")?0:a.sizeDiff.height,o=w?0:a.sizeDiff.width;
var u={width:(a.size.width-o),height:(a.size.height-v)},p=(parseInt(a.element.css("left"),10)+(a.position.left-a.originalPosition.left))||null,c=(parseInt(a.element.css("top"),10)+(a.position.top-a.originalPosition.top))||null;
a.element.animate(f.extend(u,c&&p?{top:c,left:p}:{}),{duration:q.animateDuration,easing:q.animateEasing,step:function(){var g={width:parseInt(a.element.css("width"),10),height:parseInt(a.element.css("height"),10),top:parseInt(a.element.css("top"),10),left:parseInt(a.element.css("left"),10)};
if(s&&s.length){f(s[0]).css({width:g.width,height:g.height})
}a._updateCache(g);
a._propagate("resize",r)
}})
}});
f.ui.plugin.add("resizable","containment",{start:function(A,b){var C=f(this).data("resizable"),w=C.options,u=C.element;
var z=w.containment,v=(z instanceof f)?z.get(0):(/parent/.test(z))?u.parent().get(0):z;
if(!v){return
}C.containerElement=f(v);
if(/document/.test(z)||z==document){C.containerOffset={left:0,top:0};
C.containerPosition={left:0,top:0};
C.parentData={element:f(document),left:0,top:0,width:f(document).width(),height:f(document).height()||document.body.parentNode.scrollHeight}
}else{var o=f(v),x=[];
f(["Top","Right","Left","Bottom"]).each(function(g,h){x[g]=d(o.css("padding"+h))
});
C.containerOffset=o.offset();
C.containerPosition=o.position();
C.containerSize={height:(o.innerHeight()-x[3]),width:(o.innerWidth()-x[1])};
var c=C.containerOffset,B=C.containerSize.height,p=C.containerSize.width,y=(f.ui.hasScroll(v,"left")?v.scrollWidth:p),a=(f.ui.hasScroll(v)?v.scrollHeight:B);
C.parentData={element:v,left:c.left,top:c.top,width:y,height:a}
}},resize:function(B,c){var E=f(this).data("resizable"),z=E.options,C=E.containerSize,o=E.containerOffset,v=E.size,u=E.position,b=E._aspectRatio||B.shiftKey,D={top:0,left:0},A=E.containerElement;
if(A[0]!=document&&(/static/).test(A.css("position"))){D=o
}if(u.left<(E._helper?o.left:0)){E.size.width=E.size.width+(E._helper?(E.position.left-o.left):(E.position.left-D.left));
if(b){E.size.height=E.size.width/z.aspectRatio
}E.position.left=z.helper?o.left:0
}if(u.top<(E._helper?o.top:0)){E.size.height=E.size.height+(E._helper?(E.position.top-o.top):E.position.top);
if(b){E.size.width=E.size.height*z.aspectRatio
}E.position.top=E._helper?o.top:0
}E.offset.left=E.parentData.left+E.position.left;
E.offset.top=E.parentData.top+E.position.top;
var w=Math.abs((E._helper?E.offset.left-D.left:(E.offset.left-D.left))+E.sizeDiff.width),a=Math.abs((E._helper?E.offset.top-D.top:(E.offset.top-o.top))+E.sizeDiff.height);
var x=E.containerElement.get(0)==E.element.parent().get(0),y=/relative|absolute/.test(E.containerElement.css("position"));
if(x&&y){w-=E.parentData.left
}if(w+E.size.width>=E.parentData.width){E.size.width=E.parentData.width-w;
if(b){E.size.height=E.size.width/E.aspectRatio
}}if(a+E.size.height>=E.parentData.height){E.size.height=E.parentData.height-a;
if(b){E.size.width=E.size.height*E.aspectRatio
}}},stop:function(x,h){var b=f(this).data("resizable"),w=b.options,r=b.position,o=b.containerOffset,y=b.containerPosition,v=b.containerElement;
var u=f(b.helper),a=u.offset(),c=u.outerWidth()-b.sizeDiff.width,s=u.outerHeight()-b.sizeDiff.height;
if(b._helper&&!w.animate&&(/relative/).test(v.css("position"))){f(this).css({left:a.left-y.left-o.left,width:c,height:s})
}if(b._helper&&!w.animate&&(/static/).test(v.css("position"))){f(this).css({left:a.left-y.left-o.left,width:c,height:s})
}}});
f.ui.plugin.add("resizable","ghost",{start:function(c,b){var j=f(this).data("resizable"),a=j.options,i=j.size;
j.ghost=j.originalElement.clone();
j.ghost.css({opacity:0.25,display:"block",position:"relative",height:i.height,width:i.width,margin:0,left:0,top:0}).addClass("ui-resizable-ghost").addClass(typeof a.ghost=="string"?a.ghost:"");
j.ghost.appendTo(j.helper)
},resize:function(c,b){var h=f(this).data("resizable"),a=h.options;
if(h.ghost){h.ghost.css({position:"relative",height:h.size.height,width:h.size.width})
}},stop:function(c,b){var h=f(this).data("resizable"),a=h.options;
if(h.ghost&&h.helper){h.helper.get(0).removeChild(h.ghost.get(0))
}}});
f.ui.plugin.add("resizable","grid",{resize:function(w,c){var a=f(this).data("resizable"),s=a.options,p=a.size,r=a.originalSize,q=a.originalPosition,b=a.axis,o=s._aspectRatio||w.shiftKey;
s.grid=typeof s.grid=="number"?[s.grid,s.grid]:s.grid;
var u=Math.round((p.width-r.width)/(s.grid[0]||1))*(s.grid[0]||1),v=Math.round((p.height-r.height)/(s.grid[1]||1))*(s.grid[1]||1);
if(/^(se|s|e)$/.test(b)){a.size.width=r.width+u;
a.size.height=r.height+v
}else{if(/^(ne)$/.test(b)){a.size.width=r.width+u;
a.size.height=r.height+v;
a.position.top=q.top-v
}else{if(/^(sw)$/.test(b)){a.size.width=r.width+u;
a.size.height=r.height+v;
a.position.left=q.left-u
}else{a.size.width=r.width+u;
a.size.height=r.height+v;
a.position.top=q.top-v;
a.position.left=q.left-u
}}}}});
var d=function(a){return parseInt(a,10)||0
};
var e=function(a){return !isNaN(parseInt(a,10))
}
})(jQuery);
(function(b){b.widget("ui.selectable",b.extend({},b.ui.mouse,{_init:function(){var a=this;
this.element.addClass("ui-selectable");
this.dragged=false;
var d;
this.refresh=function(){d=b(a.options.filter,a.element[0]);
d.each(function(){var f=b(this);
var c=f.offset();
b.data(this,"selectable-item",{element:this,$element:f,left:c.left,top:c.top,right:c.left+f.outerWidth(),bottom:c.top+f.outerHeight(),startselected:false,selected:f.hasClass("ui-selected"),selecting:f.hasClass("ui-selecting"),unselecting:f.hasClass("ui-unselecting")})
})
};
this.refresh();
this.selectees=d.addClass("ui-selectee");
this._mouseInit();
this.helper=b(document.createElement("div")).css({border:"1px dotted black"}).addClass("ui-selectable-helper")
},destroy:function(){this.element.removeClass("ui-selectable ui-selectable-disabled").removeData("selectable").unbind(".selectable");
this._mouseDestroy()
},_mouseStart:function(e){var a=this;
this.opos=[e.pageX,e.pageY];
if(this.options.disabled){return
}var f=this.options;
this.selectees=b(f.filter,this.element[0]);
this._trigger("start",e);
b(f.appendTo).append(this.helper);
this.helper.css({"z-index":100,position:"absolute",left:e.clientX,top:e.clientY,width:0,height:0});
if(f.autoRefresh){this.refresh()
}this.selectees.filter(".ui-selected").each(function(){var c=b.data(this,"selectable-item");
c.startselected=true;
if(!e.metaKey){c.$element.removeClass("ui-selected");
c.selected=false;
c.$element.addClass("ui-unselecting");
c.unselecting=true;
a._trigger("unselecting",e,{unselecting:c.element})
}});
b(e.target).parents().andSelf().each(function(){var c=b.data(this,"selectable-item");
if(c){c.$element.removeClass("ui-unselecting").addClass("ui-selecting");
c.unselecting=false;
c.selecting=true;
c.selected=true;
a._trigger("selecting",e,{selecting:c.element});
return false
}})
},_mouseDrag:function(j){var p=this;
this.dragged=true;
if(this.options.disabled){return
}var n=this.options;
var o=this.opos[0],k=this.opos[1],a=j.pageX,l=j.pageY;
if(o>a){var m=a;
a=o;
o=m
}if(k>l){var m=l;
l=k;
k=m
}this.helper.css({left:o,top:k,width:a-o,height:l-k});
this.selectees.each(function(){var d=b.data(this,"selectable-item");
if(!d||d.element==p.element[0]){return
}var c=false;
if(n.tolerance=="touch"){c=(!(d.left>a||d.right<o||d.top>l||d.bottom<k))
}else{if(n.tolerance=="fit"){c=(d.left>o&&d.right<a&&d.top>k&&d.bottom<l)
}}if(c){if(d.selected){d.$element.removeClass("ui-selected");
d.selected=false
}if(d.unselecting){d.$element.removeClass("ui-unselecting");
d.unselecting=false
}if(!d.selecting){d.$element.addClass("ui-selecting");
d.selecting=true;
p._trigger("selecting",j,{selecting:d.element})
}}else{if(d.selecting){if(j.metaKey&&d.startselected){d.$element.removeClass("ui-selecting");
d.selecting=false;
d.$element.addClass("ui-selected");
d.selected=true
}else{d.$element.removeClass("ui-selecting");
d.selecting=false;
if(d.startselected){d.$element.addClass("ui-unselecting");
d.unselecting=true
}p._trigger("unselecting",j,{unselecting:d.element})
}}if(d.selected){if(!j.metaKey&&!d.startselected){d.$element.removeClass("ui-selected");
d.selected=false;
d.$element.addClass("ui-unselecting");
d.unselecting=true;
p._trigger("unselecting",j,{unselecting:d.element})
}}}});
return false
},_mouseStop:function(e){var a=this;
this.dragged=false;
var f=this.options;
b(".ui-unselecting",this.element[0]).each(function(){var c=b.data(this,"selectable-item");
c.$element.removeClass("ui-unselecting");
c.unselecting=false;
c.startselected=false;
a._trigger("unselected",e,{unselected:c.element})
});
b(".ui-selecting",this.element[0]).each(function(){var c=b.data(this,"selectable-item");
c.$element.removeClass("ui-selecting").addClass("ui-selected");
c.selecting=false;
c.selected=true;
c.startselected=true;
a._trigger("selected",e,{selected:c.element})
});
this._trigger("stop",e);
this.helper.remove();
return false
}}));
b.extend(b.ui.selectable,{version:"1.7.2",defaults:{appendTo:"body",autoRefresh:true,cancel:":input,option",delay:0,distance:0,filter:"*",tolerance:"touch"}})
})(jQuery);
(function(b){b.widget("ui.sortable",b.extend({},b.ui.mouse,{_init:function(){var a=this.options;
this.containerCache={};
this.element.addClass("ui-sortable");
this.refresh();
this.floating=this.items.length?(/left|right/).test(this.items[0].item.css("float")):false;
this.offset=this.element.offset();
this._mouseInit()
},destroy:function(){this.element.removeClass("ui-sortable ui-sortable-disabled").removeData("sortable").unbind(".sortable");
this._mouseDestroy();
for(var a=this.items.length-1;
a>=0;
a--){this.items[a].item.removeData("sortable-item")
}},_mouseCapture:function(j,i){if(this.reverting){return false
}if(this.options.disabled||this.options.type=="static"){return false
}this._refreshItems(j);
var k=null,l=this,a=b(j.target).parents().each(function(){if(b.data(this,"sortable-item")==l){k=b(this);
return false
}});
if(b.data(j.target,"sortable-item")==l){k=b(j.target)
}if(!k){return false
}if(this.options.handle&&!i){var h=false;
b(this.options.handle,k).find("*").andSelf().each(function(){if(this==j.target){h=true
}});
if(!h){return false
}}this.currentItem=k;
this._removeCurrentsFromItems();
return true
},_mouseStart:function(j,i,a){var h=this.options,l=this;
this.currentContainer=this;
this.refreshPositions();
this.helper=this._createHelper(j);
this._cacheHelperProportions();
this._cacheMargins();
this.scrollParent=this.helper.scrollParent();
this.offset=this.currentItem.offset();
this.offset={top:this.offset.top-this.margins.top,left:this.offset.left-this.margins.left};
this.helper.css("position","absolute");
this.cssPosition=this.helper.css("position");
b.extend(this.offset,{click:{left:j.pageX-this.offset.left,top:j.pageY-this.offset.top},parent:this._getParentOffset(),relative:this._getRelativeOffset()});
this.originalPosition=this._generatePosition(j);
this.originalPageX=j.pageX;
this.originalPageY=j.pageY;
if(h.cursorAt){this._adjustOffsetFromHelper(h.cursorAt)
}this.domPosition={prev:this.currentItem.prev()[0],parent:this.currentItem.parent()[0]};
if(this.helper[0]!=this.currentItem[0]){this.currentItem.hide()
}this._createPlaceholder();
if(h.containment){this._setContainment()
}if(h.cursor){if(b("body").css("cursor")){this._storedCursor=b("body").css("cursor")
}b("body").css("cursor",h.cursor)
}if(h.opacity){if(this.helper.css("opacity")){this._storedOpacity=this.helper.css("opacity")
}this.helper.css("opacity",h.opacity)
}if(h.zIndex){if(this.helper.css("zIndex")){this._storedZIndex=this.helper.css("zIndex")
}this.helper.css("zIndex",h.zIndex)
}if(this.scrollParent[0]!=document&&this.scrollParent[0].tagName!="HTML"){this.overflowOffset=this.scrollParent.offset()
}this._trigger("start",j,this._uiHash());
if(!this._preserveHelperProportions){this._cacheHelperProportions()
}if(!a){for(var k=this.containers.length-1;
k>=0;
k--){this.containers[k]._trigger("activate",j,l._uiHash(this))
}}if(b.ui.ddmanager){b.ui.ddmanager.current=this
}if(b.ui.ddmanager&&!h.dropBehaviour){b.ui.ddmanager.prepareOffsets(this,j)
}this.dragging=true;
this.helper.addClass("ui-sortable-helper");
this._mouseDrag(j);
return true
},_mouseDrag:function(k){this.position=this._generatePosition(k);
this.positionAbs=this._convertPositionTo("absolute");
if(!this.lastPositionAbs){this.lastPositionAbs=this.positionAbs
}if(this.options.scroll){var j=this.options,a=false;
if(this.scrollParent[0]!=document&&this.scrollParent[0].tagName!="HTML"){if((this.overflowOffset.top+this.scrollParent[0].offsetHeight)-k.pageY<j.scrollSensitivity){this.scrollParent[0].scrollTop=a=this.scrollParent[0].scrollTop+j.scrollSpeed
}else{if(k.pageY-this.overflowOffset.top<j.scrollSensitivity){this.scrollParent[0].scrollTop=a=this.scrollParent[0].scrollTop-j.scrollSpeed
}}if((this.overflowOffset.left+this.scrollParent[0].offsetWidth)-k.pageX<j.scrollSensitivity){this.scrollParent[0].scrollLeft=a=this.scrollParent[0].scrollLeft+j.scrollSpeed
}else{if(k.pageX-this.overflowOffset.left<j.scrollSensitivity){this.scrollParent[0].scrollLeft=a=this.scrollParent[0].scrollLeft-j.scrollSpeed
}}}else{if(k.pageY-b(document).scrollTop()<j.scrollSensitivity){a=b(document).scrollTop(b(document).scrollTop()-j.scrollSpeed)
}else{if(b(window).height()-(k.pageY-b(document).scrollTop())<j.scrollSensitivity){a=b(document).scrollTop(b(document).scrollTop()+j.scrollSpeed)
}}if(k.pageX-b(document).scrollLeft()<j.scrollSensitivity){a=b(document).scrollLeft(b(document).scrollLeft()-j.scrollSpeed)
}else{if(b(window).width()-(k.pageX-b(document).scrollLeft())<j.scrollSensitivity){a=b(document).scrollLeft(b(document).scrollLeft()+j.scrollSpeed)
}}}if(a!==false&&b.ui.ddmanager&&!j.dropBehaviour){b.ui.ddmanager.prepareOffsets(this,k)
}}this.positionAbs=this._convertPositionTo("absolute");
if(!this.options.axis||this.options.axis!="y"){this.helper[0].style.left=this.position.left+"px"
}if(!this.options.axis||this.options.axis!="x"){this.helper[0].style.top=this.position.top+"px"
}for(var m=this.items.length-1;
m>=0;
m--){var l=this.items[m],n=l.item[0],i=this._intersectsWithPointer(l);
if(!i){continue
}if(n!=this.currentItem[0]&&this.placeholder[i==1?"next":"prev"]()[0]!=n&&!b.ui.contains(this.placeholder[0],n)&&(this.options.type=="semi-dynamic"?!b.ui.contains(this.element[0],n):true)){this.direction=i==1?"down":"up";
if(this.options.tolerance=="pointer"||this._intersectsWithSides(l)){this._rearrange(k,l)
}else{break
}this._trigger("change",k,this._uiHash());
break
}}this._contactContainers(k);
if(b.ui.ddmanager){b.ui.ddmanager.drag(this,k)
}this._trigger("sort",k,this._uiHash());
this.lastPositionAbs=this.positionAbs;
return false
},_mouseStop:function(h,g){if(!h){return
}if(b.ui.ddmanager&&!this.options.dropBehaviour){b.ui.ddmanager.drop(this,h)
}if(this.options.revert){var a=this;
var f=a.placeholder.offset();
a.reverting=true;
b(this.helper).animate({left:f.left-this.offset.parent.left-a.margins.left+(this.offsetParent[0]==document.body?0:this.offsetParent[0].scrollLeft),top:f.top-this.offset.parent.top-a.margins.top+(this.offsetParent[0]==document.body?0:this.offsetParent[0].scrollTop)},parseInt(this.options.revert,10)||500,function(){a._clear(h)
})
}else{this._clear(h,g)
}return false
},cancel:function(){var a=this;
if(this.dragging){this._mouseUp();
if(this.options.helper=="original"){this.currentItem.css(this._storedCSS).removeClass("ui-sortable-helper")
}else{this.currentItem.show()
}for(var d=this.containers.length-1;
d>=0;
d--){this.containers[d]._trigger("deactivate",null,a._uiHash(this));
if(this.containers[d].containerCache.over){this.containers[d]._trigger("out",null,a._uiHash(this));
this.containers[d].containerCache.over=0
}}}if(this.placeholder[0].parentNode){this.placeholder[0].parentNode.removeChild(this.placeholder[0])
}if(this.options.helper!="original"&&this.helper&&this.helper[0].parentNode){this.helper.remove()
}b.extend(this,{helper:null,dragging:false,reverting:false,_noFinalSort:null});
if(this.domPosition.prev){b(this.domPosition.prev).after(this.currentItem)
}else{b(this.domPosition.parent).prepend(this.currentItem)
}return true
},serialize:function(e){var a=this._getItemsAsjQuery(e&&e.connected);
var f=[];
e=e||{};
b(a).each(function(){var c=(b(e.item||this).attr(e.attribute||"id")||"").match(e.expression||(/(.+)[-=_](.+)/));
if(c){f.push((e.key||c[1]+"[]")+"="+(e.key&&e.expression?c[1]:c[2]))
}});
return f.join("&")
},toArray:function(e){var a=this._getItemsAsjQuery(e&&e.connected);
var f=[];
e=e||{};
a.each(function(){f.push(b(e.item||this).attr(e.attribute||"id")||"")
});
return f
},_intersectsWith:function(p){var x=this.positionAbs.left,y=x+this.helperProportions.width,q=this.positionAbs.top,r=q+this.helperProportions.height;
var w=p.left,z=w+p.width,l=p.top,s=l+p.height;
var a=this.offset.click.top,u=this.offset.click.left;
var v=(q+a)>l&&(q+a)<s&&(x+u)>w&&(x+u)<z;
if(this.options.tolerance=="pointer"||this.options.forcePointerForContainers||(this.options.tolerance!="pointer"&&this.helperProportions[this.floating?"width":"height"]>p[this.floating?"width":"height"])){return v
}else{return(w<x+(this.helperProportions.width/2)&&y-(this.helperProportions.width/2)<z&&l<q+(this.helperProportions.height/2)&&r-(this.helperProportions.height/2)<s)
}},_intersectsWithPointer:function(k){var j=b.ui.isOverAxis(this.positionAbs.top+this.offset.click.top,k.top,k.height),l=b.ui.isOverAxis(this.positionAbs.left+this.offset.click.left,k.left,k.width),h=j&&l,a=this._getDragVerticalDirection(),i=this._getDragHorizontalDirection();
if(!h){return false
}return this.floating?(((i&&i=="right")||a=="down")?2:1):(a&&(a=="down"?2:1))
},_intersectsWithSides:function(h){var j=b.ui.isOverAxis(this.positionAbs.top+this.offset.click.top,h.top+(h.height/2),h.height),i=b.ui.isOverAxis(this.positionAbs.left+this.offset.click.left,h.left+(h.width/2),h.width),a=this._getDragVerticalDirection(),g=this._getDragHorizontalDirection();
if(this.floating&&g){return((g=="right"&&i)||(g=="left"&&!i))
}else{return a&&((a=="down"&&j)||(a=="up"&&!j))
}},_getDragVerticalDirection:function(){var a=this.positionAbs.top-this.lastPositionAbs.top;
return a!=0&&(a>0?"down":"up")
},_getDragHorizontalDirection:function(){var a=this.positionAbs.left-this.lastPositionAbs.left;
return a!=0&&(a>0?"right":"left")
},refresh:function(a){this._refreshItems(a);
this.refreshPositions()
},_connectWith:function(){var a=this.options;
return a.connectWith.constructor==String?[a.connectWith]:a.connectWith
},_getItemsAsjQuery:function(r){var a=this;
var m=[];
var o=[];
var j=this._connectWith();
if(j&&r){for(var p=j.length-1;
p>=0;
p--){var i=b(j[p]);
for(var q=i.length-1;
q>=0;
q--){var n=b.data(i[q],"sortable");
if(n&&n!=this&&!n.options.disabled){o.push([b.isFunction(n.options.items)?n.options.items.call(n.element):b(n.options.items,n.element).not(".ui-sortable-helper"),n])
}}}}o.push([b.isFunction(this.options.items)?this.options.items.call(this.element,null,{options:this.options,item:this.currentItem}):b(this.options.items,this.element).not(".ui-sortable-helper"),this]);
for(var p=o.length-1;
p>=0;
p--){o[p][0].each(function(){m.push(this)
})
}return b(m)
},_removeCurrentsFromItems:function(){var e=this.currentItem.find(":data(sortable-item)");
for(var f=0;
f<this.items.length;
f++){for(var a=0;
a<e.length;
a++){if(e[a]==this.items[f].item[0]){this.items.splice(f,1)
}}}},_refreshItems:function(A){this.items=[];
this.containers=[this];
var u=this.items;
var a=this;
var w=[[b.isFunction(this.options.items)?this.options.items.call(this.element[0],A,{item:this.currentItem}):b(this.options.items,this.element),this]];
var r=this._connectWith();
if(r){for(var x=r.length-1;
x>=0;
x--){var q=b(r[x]);
for(var y=q.length-1;
y>=0;
y--){var v=b.data(q[y],"sortable");
if(v&&v!=this&&!v.options.disabled){w.push([b.isFunction(v.options.items)?v.options.items.call(v.element[0],A,{item:this.currentItem}):b(v.options.items,v.element),v]);
this.containers.push(v)
}}}}for(var x=w.length-1;
x>=0;
x--){var s=w[x][1];
var z=w[x][0];
for(var y=0,j=z.length;
y<j;
y++){var i=b(z[y]);
i.data("sortable-item",s);
u.push({item:i,instance:s,width:0,height:0,left:0,top:0})
}}},refreshPositions:function(a){if(this.offsetParent&&this.helper){this.offset.parent=this._getParentOffset()
}for(var i=this.items.length-1;
i>=0;
i--){var h=this.items[i];
if(h.instance!=this.currentContainer&&this.currentContainer&&h.item[0]!=this.currentItem[0]){continue
}var j=this.options.toleranceElement?b(this.options.toleranceElement,h.item):h.item;
if(!a){h.width=j.outerWidth();
h.height=j.outerHeight()
}var g=j.offset();
h.left=g.left;
h.top=g.top
}if(this.options.custom&&this.options.custom.refreshContainers){this.options.custom.refreshContainers.call(this)
}else{for(var i=this.containers.length-1;
i>=0;
i--){var g=this.containers[i].element.offset();
this.containers[i].containerCache.left=g.left;
this.containers[i].containerCache.top=g.top;
this.containers[i].containerCache.width=this.containers[i].element.outerWidth();
this.containers[i].containerCache.height=this.containers[i].element.outerHeight()
}}},_createPlaceholder:function(g){var a=g||this,f=a.options;
if(!f.placeholder||f.placeholder.constructor==String){var h=f.placeholder;
f.placeholder={element:function(){var c=b(document.createElement(a.currentItem[0].nodeName)).addClass(h||a.currentItem[0].className+" ui-sortable-placeholder").removeClass("ui-sortable-helper")[0];
if(!h){c.style.visibility="hidden"
}return c
},update:function(d,c){if(h&&!f.forcePlaceholderSize){return
}if(!c.height()){c.height(a.currentItem.innerHeight()-parseInt(a.currentItem.css("paddingTop")||0,10)-parseInt(a.currentItem.css("paddingBottom")||0,10))
}if(!c.width()){c.width(a.currentItem.innerWidth()-parseInt(a.currentItem.css("paddingLeft")||0,10)-parseInt(a.currentItem.css("paddingRight")||0,10))
}}}
}a.placeholder=b(f.placeholder.element.call(a.element,a.currentItem));
a.currentItem.after(a.placeholder);
f.placeholder.update(a,a.placeholder)
},_contactContainers:function(m){for(var n=this.containers.length-1;
n>=0;
n--){if(this._intersectsWith(this.containers[n].containerCache)){if(!this.containers[n].containerCache.over){if(this.currentContainer!=this.containers[n]){var i=10000;
var j=null;
var l=this.positionAbs[this.containers[n].floating?"left":"top"];
for(var a=this.items.length-1;
a>=0;
a--){if(!b.ui.contains(this.containers[n].element[0],this.items[a].item[0])){continue
}var k=this.items[a][this.containers[n].floating?"left":"top"];
if(Math.abs(k-l)<i){i=Math.abs(k-l);
j=this.items[a]
}}if(!j&&!this.options.dropOnEmpty){continue
}this.currentContainer=this.containers[n];
j?this._rearrange(m,j,null,true):this._rearrange(m,null,this.containers[n].element,true);
this._trigger("change",m,this._uiHash());
this.containers[n]._trigger("change",m,this._uiHash(this));
this.options.placeholder.update(this.currentContainer,this.placeholder)
}this.containers[n]._trigger("over",m,this._uiHash(this));
this.containers[n].containerCache.over=1
}}else{if(this.containers[n].containerCache.over){this.containers[n]._trigger("out",m,this._uiHash(this));
this.containers[n].containerCache.over=0
}}}},_createHelper:function(f){var e=this.options;
var a=b.isFunction(e.helper)?b(e.helper.apply(this.element[0],[f,this.currentItem])):(e.helper=="clone"?this.currentItem.clone():this.currentItem);
if(!a.parents("body").length){b(e.appendTo!="parent"?e.appendTo:this.currentItem[0].parentNode)[0].appendChild(a[0])
}if(a[0]==this.currentItem[0]){this._storedCSS={width:this.currentItem[0].style.width,height:this.currentItem[0].style.height,position:this.currentItem.css("position"),top:this.currentItem.css("top"),left:this.currentItem.css("left")}
}if(a[0].style.width==""||e.forceHelperSize){a.width(this.currentItem.width())
}if(a[0].style.height==""||e.forceHelperSize){a.height(this.currentItem.height())
}return a
},_adjustOffsetFromHelper:function(a){if(a.left!=undefined){this.offset.click.left=a.left+this.margins.left
}if(a.right!=undefined){this.offset.click.left=this.helperProportions.width-a.right+this.margins.left
}if(a.top!=undefined){this.offset.click.top=a.top+this.margins.top
}if(a.bottom!=undefined){this.offset.click.top=this.helperProportions.height-a.bottom+this.margins.top
}},_getParentOffset:function(){this.offsetParent=this.helper.offsetParent();
var a=this.offsetParent.offset();
if(this.cssPosition=="absolute"&&this.scrollParent[0]!=document&&b.ui.contains(this.scrollParent[0],this.offsetParent[0])){a.left+=this.scrollParent.scrollLeft();
a.top+=this.scrollParent.scrollTop()
}if((this.offsetParent[0]==document.body)||(this.offsetParent[0].tagName&&this.offsetParent[0].tagName.toLowerCase()=="html"&&b.browser.msie)){a={top:0,left:0}
}return{top:a.top+(parseInt(this.offsetParent.css("borderTopWidth"),10)||0),left:a.left+(parseInt(this.offsetParent.css("borderLeftWidth"),10)||0)}
},_getRelativeOffset:function(){if(this.cssPosition=="relative"){var a=this.currentItem.position();
return{top:a.top-(parseInt(this.helper.css("top"),10)||0)+this.scrollParent.scrollTop(),left:a.left-(parseInt(this.helper.css("left"),10)||0)+this.scrollParent.scrollLeft()}
}else{return{top:0,left:0}
}},_cacheMargins:function(){this.margins={left:(parseInt(this.currentItem.css("marginLeft"),10)||0),top:(parseInt(this.currentItem.css("marginTop"),10)||0)}
},_cacheHelperProportions:function(){this.helperProportions={width:this.helper.outerWidth(),height:this.helper.outerHeight()}
},_setContainment:function(){var f=this.options;
if(f.containment=="parent"){f.containment=this.helper[0].parentNode
}if(f.containment=="document"||f.containment=="window"){this.containment=[0-this.offset.relative.left-this.offset.parent.left,0-this.offset.relative.top-this.offset.parent.top,b(f.containment=="document"?document:window).width()-this.helperProportions.width-this.margins.left,(b(f.containment=="document"?document:window).height()||document.body.parentNode.scrollHeight)-this.helperProportions.height-this.margins.top]
}if(!(/^(document|window|parent)$/).test(f.containment)){var h=b(f.containment)[0];
var g=b(f.containment).offset();
var a=(b(h).css("overflow")!="hidden");
this.containment=[g.left+(parseInt(b(h).css("borderLeftWidth"),10)||0)+(parseInt(b(h).css("paddingLeft"),10)||0)-this.margins.left,g.top+(parseInt(b(h).css("borderTopWidth"),10)||0)+(parseInt(b(h).css("paddingTop"),10)||0)-this.margins.top,g.left+(a?Math.max(h.scrollWidth,h.offsetWidth):h.offsetWidth)-(parseInt(b(h).css("borderLeftWidth"),10)||0)-(parseInt(b(h).css("paddingRight"),10)||0)-this.helperProportions.width-this.margins.left,g.top+(a?Math.max(h.scrollHeight,h.offsetHeight):h.offsetHeight)-(parseInt(b(h).css("borderTopWidth"),10)||0)-(parseInt(b(h).css("paddingBottom"),10)||0)-this.helperProportions.height-this.margins.top]
}},_convertPositionTo:function(j,d){if(!d){d=this.position
}var l=j=="absolute"?1:-1;
var k=this.options,a=this.cssPosition=="absolute"&&!(this.scrollParent[0]!=document&&b.ui.contains(this.scrollParent[0],this.offsetParent[0]))?this.offsetParent:this.scrollParent,i=(/(html|body)/i).test(a[0].tagName);
return{top:(d.top+this.offset.relative.top*l+this.offset.parent.top*l-(b.browser.safari&&this.cssPosition=="fixed"?0:(this.cssPosition=="fixed"?-this.scrollParent.scrollTop():(i?0:a.scrollTop()))*l)),left:(d.left+this.offset.relative.left*l+this.offset.parent.left*l-(b.browser.safari&&this.cssPosition=="fixed"?0:(this.cssPosition=="fixed"?-this.scrollParent.scrollLeft():i?0:a.scrollLeft())*l))}
},_generatePosition:function(n){var k=this.options,a=this.cssPosition=="absolute"&&!(this.scrollParent[0]!=document&&b.ui.contains(this.scrollParent[0],this.offsetParent[0]))?this.offsetParent:this.scrollParent,j=(/(html|body)/i).test(a[0].tagName);
if(this.cssPosition=="relative"&&!(this.scrollParent[0]!=document&&this.scrollParent[0]!=this.offsetParent[0])){this.offset.relative=this._getRelativeOffset()
}var o=n.pageX;
var p=n.pageY;
if(this.originalPosition){if(this.containment){if(n.pageX-this.offset.click.left<this.containment[0]){o=this.containment[0]+this.offset.click.left
}if(n.pageY-this.offset.click.top<this.containment[1]){p=this.containment[1]+this.offset.click.top
}if(n.pageX-this.offset.click.left>this.containment[2]){o=this.containment[2]+this.offset.click.left
}if(n.pageY-this.offset.click.top>this.containment[3]){p=this.containment[3]+this.offset.click.top
}}if(k.grid){var l=this.originalPageY+Math.round((p-this.originalPageY)/k.grid[1])*k.grid[1];
p=this.containment?(!(l-this.offset.click.top<this.containment[1]||l-this.offset.click.top>this.containment[3])?l:(!(l-this.offset.click.top<this.containment[1])?l-k.grid[1]:l+k.grid[1])):l;
var m=this.originalPageX+Math.round((o-this.originalPageX)/k.grid[0])*k.grid[0];
o=this.containment?(!(m-this.offset.click.left<this.containment[0]||m-this.offset.click.left>this.containment[2])?m:(!(m-this.offset.click.left<this.containment[0])?m-k.grid[0]:m+k.grid[0])):m
}}return{top:(p-this.offset.click.top-this.offset.relative.top-this.offset.parent.top+(b.browser.safari&&this.cssPosition=="fixed"?0:(this.cssPosition=="fixed"?-this.scrollParent.scrollTop():(j?0:a.scrollTop())))),left:(o-this.offset.click.left-this.offset.relative.left-this.offset.parent.left+(b.browser.safari&&this.cssPosition=="fixed"?0:(this.cssPosition=="fixed"?-this.scrollParent.scrollLeft():j?0:a.scrollLeft())))}
},_rearrange:function(h,i,l,j){l?l[0].appendChild(this.placeholder[0]):i.item[0].parentNode.insertBefore(this.placeholder[0],(this.direction=="down"?i.item[0]:i.item[0].nextSibling));
this.counter=this.counter?++this.counter:1;
var k=this,a=this.counter;
window.setTimeout(function(){if(a==k.counter){k.refreshPositions(!j)
}},0)
},_clear:function(i,h){this.reverting=false;
var g=[],a=this;
if(!this._noFinalSort&&this.currentItem[0].parentNode){this.placeholder.before(this.currentItem)
}this._noFinalSort=null;
if(this.helper[0]==this.currentItem[0]){for(var j in this._storedCSS){if(this._storedCSS[j]=="auto"||this._storedCSS[j]=="static"){this._storedCSS[j]=""
}}this.currentItem.css(this._storedCSS).removeClass("ui-sortable-helper")
}else{this.currentItem.show()
}if(this.fromOutside&&!h){g.push(function(c){this._trigger("receive",c,this._uiHash(this.fromOutside))
})
}if((this.fromOutside||this.domPosition.prev!=this.currentItem.prev().not(".ui-sortable-helper")[0]||this.domPosition.parent!=this.currentItem.parent()[0])&&!h){g.push(function(c){this._trigger("update",c,this._uiHash())
})
}if(!b.ui.contains(this.element[0],this.currentItem[0])){if(!h){g.push(function(c){this._trigger("remove",c,this._uiHash())
})
}for(var j=this.containers.length-1;
j>=0;
j--){if(b.ui.contains(this.containers[j].element[0],this.currentItem[0])&&!h){g.push((function(c){return function(d){c._trigger("receive",d,this._uiHash(this))
}
}).call(this,this.containers[j]));
g.push((function(c){return function(d){c._trigger("update",d,this._uiHash(this))
}
}).call(this,this.containers[j]))
}}}for(var j=this.containers.length-1;
j>=0;
j--){if(!h){g.push((function(c){return function(d){c._trigger("deactivate",d,this._uiHash(this))
}
}).call(this,this.containers[j]))
}if(this.containers[j].containerCache.over){g.push((function(c){return function(d){c._trigger("out",d,this._uiHash(this))
}
}).call(this,this.containers[j]));
this.containers[j].containerCache.over=0
}}if(this._storedCursor){b("body").css("cursor",this._storedCursor)
}if(this._storedOpacity){this.helper.css("opacity",this._storedOpacity)
}if(this._storedZIndex){this.helper.css("zIndex",this._storedZIndex=="auto"?"":this._storedZIndex)
}this.dragging=false;
if(this.cancelHelperRemoval){if(!h){this._trigger("beforeStop",i,this._uiHash());
for(var j=0;
j<g.length;
j++){g[j].call(this,i)
}this._trigger("stop",i,this._uiHash())
}return false
}if(!h){this._trigger("beforeStop",i,this._uiHash())
}this.placeholder[0].parentNode.removeChild(this.placeholder[0]);
if(this.helper[0]!=this.currentItem[0]){this.helper.remove()
}this.helper=null;
if(!h){for(var j=0;
j<g.length;
j++){g[j].call(this,i)
}this._trigger("stop",i,this._uiHash())
}this.fromOutside=false;
return true
},_trigger:function(){if(b.widget.prototype._trigger.apply(this,arguments)===false){this.cancel()
}},_uiHash:function(d){var a=d||this;
return{helper:a.helper,placeholder:a.placeholder||b([]),position:a.position,absolutePosition:a.positionAbs,offset:a.positionAbs,item:a.currentItem,sender:d?d.element:null}
}}));
b.extend(b.ui.sortable,{getter:"serialize toArray",version:"1.7.2",eventPrefix:"sort",defaults:{appendTo:"parent",axis:false,cancel:":input,option",connectWith:false,containment:false,cursor:"auto",cursorAt:false,delay:0,distance:1,dropOnEmpty:true,forcePlaceholderSize:false,forceHelperSize:false,grid:false,handle:false,helper:"original",items:"> *",opacity:false,placeholder:false,revert:false,scroll:true,scrollSensitivity:20,scrollSpeed:20,scope:"default",tolerance:"intersect",zIndex:1000}})
})(jQuery);
jQuery.effects||(function(i){i.effects={version:"1.7.2",save:function(b,a){for(var c=0;
c<a.length;
c++){if(a[c]!==null){b.data("ec.storage."+a[c],b[0].style[a[c]])
}}},restore:function(b,a){for(var c=0;
c<a.length;
c++){if(a[c]!==null){b.css(a[c],b.data("ec.storage."+a[c]))
}}},setMode:function(b,a){if(a=="toggle"){a=b.is(":hidden")?"show":"hide"
}return a
},getBaseline:function(c,b){var a,d;
switch(c[0]){case"top":a=0;
break;
case"middle":a=0.5;
break;
case"bottom":a=1;
break;
default:a=c[0]/b.height
}switch(c[1]){case"left":d=0;
break;
case"center":d=0.5;
break;
case"right":d=1;
break;
default:d=c[1]/b.width
}return{x:d,y:a}
},createWrapper:function(e){if(e.parent().is(".ui-effects-wrapper")){return e.parent()
}var d={width:e.outerWidth(true),height:e.outerHeight(true),"float":e.css("float")};
e.wrap('<div class="ui-effects-wrapper" style="font-size:100%;background:transparent;border:none;margin:0;padding:0"></div>');
var a=e.parent();
if(e.css("position")=="static"){a.css({position:"relative"});
e.css({position:"relative"})
}else{var b=e.css("top");
if(isNaN(parseInt(b,10))){b="auto"
}var c=e.css("left");
if(isNaN(parseInt(c,10))){c="auto"
}a.css({position:e.css("position"),top:b,left:c,zIndex:e.css("z-index")}).show();
e.css({position:"relative",top:0,left:0})
}a.css(d);
return a
},removeWrapper:function(a){if(a.parent().is(".ui-effects-wrapper")){return a.parent().replaceWith(a)
}return a
},setTransition:function(c,a,d,b){b=b||{};
i.each(a,function(e,l){unit=c.cssUnit(l);
if(unit[0]>0){b[l]=unit[0]*d+unit[1]
}});
return b
},animateClass:function(d,c,a,b){var l=(typeof a=="function"?a:(b?b:null));
var e=(typeof a=="string"?a:null);
return this.each(function(){var u={};
var w=i(this);
var v=w.attr("style")||"";
if(typeof v=="object"){v=v.cssText
}if(d.toggle){w.hasClass(d.toggle)?d.remove=d.toggle:d.add=d.toggle
}var n=i.extend({},(document.defaultView?document.defaultView.getComputedStyle(this,null):this.currentStyle));
if(d.add){w.addClass(d.add)
}if(d.remove){w.removeClass(d.remove)
}var k=i.extend({},(document.defaultView?document.defaultView.getComputedStyle(this,null):this.currentStyle));
if(d.add){w.removeClass(d.add)
}if(d.remove){w.addClass(d.remove)
}for(var s in k){if(typeof k[s]!="function"&&k[s]&&s.indexOf("Moz")==-1&&s.indexOf("length")==-1&&k[s]!=n[s]&&(s.match(/color/i)||(!s.match(/color/i)&&!isNaN(parseInt(k[s],10))))&&(n.position!="static"||(n.position=="static"&&!s.match(/left|top|bottom|right/)))){u[s]=k[s]
}}w.animate(u,c,e,function(){if(typeof i(this).attr("style")=="object"){i(this).attr("style")["cssText"]="";
i(this).attr("style")["cssText"]=v
}else{i(this).attr("style",v)
}if(d.add){i(this).addClass(d.add)
}if(d.remove){i(this).removeClass(d.remove)
}if(l){l.apply(this,arguments)
}})
})
}};
function j(d,e){var b=d[1]&&d[1].constructor==Object?d[1]:{};
if(e){b.mode=e
}var c=d[1]&&d[1].constructor!=Object?d[1]:(b.duration?b.duration:d[2]);
c=i.fx.off?0:typeof c==="number"?c:i.fx.speeds[c]||i.fx.speeds._default;
var a=b.callback||(i.isFunction(d[1])&&d[1])||(i.isFunction(d[2])&&d[2])||(i.isFunction(d[3])&&d[3]);
return[d[0],b,c,a]
}i.fn.extend({_show:i.fn.show,_hide:i.fn.hide,__toggle:i.fn.toggle,_addClass:i.fn.addClass,_removeClass:i.fn.removeClass,_toggleClass:i.fn.toggleClass,effect:function(c,d,b,a){return i.effects[c]?i.effects[c].call(this,{method:c,options:d||{},duration:b,callback:a}):null
},show:function(){if(!arguments[0]||(arguments[0].constructor==Number||(/(slow|normal|fast)/).test(arguments[0]))){return this._show.apply(this,arguments)
}else{return this.effect.apply(this,j(arguments,"show"))
}},hide:function(){if(!arguments[0]||(arguments[0].constructor==Number||(/(slow|normal|fast)/).test(arguments[0]))){return this._hide.apply(this,arguments)
}else{return this.effect.apply(this,j(arguments,"hide"))
}},toggle:function(){if(!arguments[0]||(arguments[0].constructor==Number||(/(slow|normal|fast)/).test(arguments[0]))||(i.isFunction(arguments[0])||typeof arguments[0]=="boolean")){return this.__toggle.apply(this,arguments)
}else{return this.effect.apply(this,j(arguments,"toggle"))
}},addClass:function(c,d,a,b){return d?i.effects.animateClass.apply(this,[{add:c},d,a,b]):this._addClass(c)
},removeClass:function(c,d,a,b){return d?i.effects.animateClass.apply(this,[{remove:c},d,a,b]):this._removeClass(c)
},toggleClass:function(c,d,a,b){return((typeof d!=="boolean")&&d)?i.effects.animateClass.apply(this,[{toggle:c},d,a,b]):this._toggleClass(c,d)
},morph:function(e,c,d,a,b){return i.effects.animateClass.apply(this,[{add:c,remove:e},d,a,b])
},switchClass:function(){return this.morph.apply(this,arguments)
},cssUnit:function(c){var b=this.css(c),a=[];
i.each(["em","px","%","pt"],function(e,d){if(b.indexOf(d)>0){a=[parseFloat(b),d]
}});
return a
}});
i.each(["backgroundColor","borderBottomColor","borderLeftColor","borderRightColor","borderTopColor","color","outlineColor"],function(a,b){i.fx.step[b]=function(c){if(c.state==0){c.start=h(c.elem,b);
c.end=f(c.end)
}c.elem.style[b]="rgb("+[Math.max(Math.min(parseInt((c.pos*(c.end[0]-c.start[0]))+c.start[0],10),255),0),Math.max(Math.min(parseInt((c.pos*(c.end[1]-c.start[1]))+c.start[1],10),255),0),Math.max(Math.min(parseInt((c.pos*(c.end[2]-c.start[2]))+c.start[2],10),255),0)].join(",")+")"
}
});
function f(a){var b;
if(a&&a.constructor==Array&&a.length==3){return a
}if(b=/rgb\(\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*\)/.exec(a)){return[parseInt(b[1],10),parseInt(b[2],10),parseInt(b[3],10)]
}if(b=/rgb\(\s*([0-9]+(?:\.[0-9]+)?)\%\s*,\s*([0-9]+(?:\.[0-9]+)?)\%\s*,\s*([0-9]+(?:\.[0-9]+)?)\%\s*\)/.exec(a)){return[parseFloat(b[1])*2.55,parseFloat(b[2])*2.55,parseFloat(b[3])*2.55]
}if(b=/#([a-fA-F0-9]{2})([a-fA-F0-9]{2})([a-fA-F0-9]{2})/.exec(a)){return[parseInt(b[1],16),parseInt(b[2],16),parseInt(b[3],16)]
}if(b=/#([a-fA-F0-9])([a-fA-F0-9])([a-fA-F0-9])/.exec(a)){return[parseInt(b[1]+b[1],16),parseInt(b[2]+b[2],16),parseInt(b[3]+b[3],16)]
}if(b=/rgba\(0, 0, 0, 0\)/.exec(a)){return g.transparent
}return g[i.trim(a).toLowerCase()]
}function h(a,c){var b;
do{b=i.curCSS(a,c);
if(b!=""&&b!="transparent"||i.nodeName(a,"body")){break
}c="backgroundColor"
}while(a=a.parentNode);
return f(b)
}var g={aqua:[0,255,255],azure:[240,255,255],beige:[245,245,220],black:[0,0,0],blue:[0,0,255],brown:[165,42,42],cyan:[0,255,255],darkblue:[0,0,139],darkcyan:[0,139,139],darkgrey:[169,169,169],darkgreen:[0,100,0],darkkhaki:[189,183,107],darkmagenta:[139,0,139],darkolivegreen:[85,107,47],darkorange:[255,140,0],darkorchid:[153,50,204],darkred:[139,0,0],darksalmon:[233,150,122],darkviolet:[148,0,211],fuchsia:[255,0,255],gold:[255,215,0],green:[0,128,0],indigo:[75,0,130],khaki:[240,230,140],lightblue:[173,216,230],lightcyan:[224,255,255],lightgreen:[144,238,144],lightgrey:[211,211,211],lightpink:[255,182,193],lightyellow:[255,255,224],lime:[0,255,0],magenta:[255,0,255],maroon:[128,0,0],navy:[0,0,128],olive:[128,128,0],orange:[255,165,0],pink:[255,192,203],purple:[128,0,128],violet:[128,0,128],red:[255,0,0],silver:[192,192,192],white:[255,255,255],yellow:[255,255,0],transparent:[255,255,255]};
i.easing.jswing=i.easing.swing;
i.extend(i.easing,{def:"easeOutQuad",swing:function(d,c,e,a,b){return i.easing[i.easing.def](d,c,e,a,b)
},easeInQuad:function(d,c,e,a,b){return a*(c/=b)*c+e
},easeOutQuad:function(d,c,e,a,b){return -a*(c/=b)*(c-2)+e
},easeInOutQuad:function(d,c,e,a,b){if((c/=b/2)<1){return a/2*c*c+e
}return -a/2*((--c)*(c-2)-1)+e
},easeInCubic:function(d,c,e,a,b){return a*(c/=b)*c*c+e
},easeOutCubic:function(d,c,e,a,b){return a*((c=c/b-1)*c*c+1)+e
},easeInOutCubic:function(d,c,e,a,b){if((c/=b/2)<1){return a/2*c*c*c+e
}return a/2*((c-=2)*c*c+2)+e
},easeInQuart:function(d,c,e,a,b){return a*(c/=b)*c*c*c+e
},easeOutQuart:function(d,c,e,a,b){return -a*((c=c/b-1)*c*c*c-1)+e
},easeInOutQuart:function(d,c,e,a,b){if((c/=b/2)<1){return a/2*c*c*c*c+e
}return -a/2*((c-=2)*c*c*c-2)+e
},easeInQuint:function(d,c,e,a,b){return a*(c/=b)*c*c*c*c+e
},easeOutQuint:function(d,c,e,a,b){return a*((c=c/b-1)*c*c*c*c+1)+e
},easeInOutQuint:function(d,c,e,a,b){if((c/=b/2)<1){return a/2*c*c*c*c*c+e
}return a/2*((c-=2)*c*c*c*c+2)+e
},easeInSine:function(d,c,e,a,b){return -a*Math.cos(c/b*(Math.PI/2))+a+e
},easeOutSine:function(d,c,e,a,b){return a*Math.sin(c/b*(Math.PI/2))+e
},easeInOutSine:function(d,c,e,a,b){return -a/2*(Math.cos(Math.PI*c/b)-1)+e
},easeInExpo:function(d,c,e,a,b){return(c==0)?e:a*Math.pow(2,10*(c/b-1))+e
},easeOutExpo:function(d,c,e,a,b){return(c==b)?e+a:a*(-Math.pow(2,-10*c/b)+1)+e
},easeInOutExpo:function(d,c,e,a,b){if(c==0){return e
}if(c==b){return e+a
}if((c/=b/2)<1){return a/2*Math.pow(2,10*(c-1))+e
}return a/2*(-Math.pow(2,-10*--c)+2)+e
},easeInCirc:function(d,c,e,a,b){return -a*(Math.sqrt(1-(c/=b)*c)-1)+e
},easeOutCirc:function(d,c,e,a,b){return a*Math.sqrt(1-(c=c/b-1)*c)+e
},easeInOutCirc:function(d,c,e,a,b){if((c/=b/2)<1){return -a/2*(Math.sqrt(1-c*c)-1)+e
}return a/2*(Math.sqrt(1-(c-=2)*c)+1)+e
},easeInElastic:function(o,e,p,a,b){var d=1.70158;
var c=0;
var n=a;
if(e==0){return p
}if((e/=b)==1){return p+a
}if(!c){c=b*0.3
}if(n<Math.abs(a)){n=a;
var d=c/4
}else{var d=c/(2*Math.PI)*Math.asin(a/n)
}return -(n*Math.pow(2,10*(e-=1))*Math.sin((e*b-d)*(2*Math.PI)/c))+p
},easeOutElastic:function(o,e,p,a,b){var d=1.70158;
var c=0;
var n=a;
if(e==0){return p
}if((e/=b)==1){return p+a
}if(!c){c=b*0.3
}if(n<Math.abs(a)){n=a;
var d=c/4
}else{var d=c/(2*Math.PI)*Math.asin(a/n)
}return n*Math.pow(2,-10*e)*Math.sin((e*b-d)*(2*Math.PI)/c)+a+p
},easeInOutElastic:function(o,e,p,a,b){var d=1.70158;
var c=0;
var n=a;
if(e==0){return p
}if((e/=b/2)==2){return p+a
}if(!c){c=b*(0.3*1.5)
}if(n<Math.abs(a)){n=a;
var d=c/4
}else{var d=c/(2*Math.PI)*Math.asin(a/n)
}if(e<1){return -0.5*(n*Math.pow(2,10*(e-=1))*Math.sin((e*b-d)*(2*Math.PI)/c))+p
}return n*Math.pow(2,-10*(e-=1))*Math.sin((e*b-d)*(2*Math.PI)/c)*0.5+a+p
},easeInBack:function(e,d,l,a,b,c){if(c==undefined){c=1.70158
}return a*(d/=b)*d*((c+1)*d-c)+l
},easeOutBack:function(e,d,l,a,b,c){if(c==undefined){c=1.70158
}return a*((d=d/b-1)*d*((c+1)*d+c)+1)+l
},easeInOutBack:function(e,d,l,a,b,c){if(c==undefined){c=1.70158
}if((d/=b/2)<1){return a/2*(d*d*(((c*=(1.525))+1)*d-c))+l
}return a/2*((d-=2)*d*(((c*=(1.525))+1)*d+c)+2)+l
},easeInBounce:function(d,c,e,a,b){return a-i.easing.easeOutBounce(d,b-c,0,a,b)+e
},easeOutBounce:function(d,c,e,a,b){if((c/=b)<(1/2.75)){return a*(7.5625*c*c)+e
}else{if(c<(2/2.75)){return a*(7.5625*(c-=(1.5/2.75))*c+0.75)+e
}else{if(c<(2.5/2.75)){return a*(7.5625*(c-=(2.25/2.75))*c+0.9375)+e
}else{return a*(7.5625*(c-=(2.625/2.75))*c+0.984375)+e
}}}},easeInOutBounce:function(d,c,e,a,b){if(c<b/2){return i.easing.easeInBounce(d,c*2,0,a,b)*0.5+e
}return i.easing.easeOutBounce(d,c*2-b,0,a,b)*0.5+a*0.5+e
}})
})(jQuery);
(function(b){b.effects.blind=function(a){return this.queue(function(){var q=b(this),r=["position","top","left"];
var m=b.effects.setMode(q,a.options.mode||"hide");
var n=a.options.direction||"vertical";
b.effects.save(q,r);
q.show();
var k=b.effects.createWrapper(q).css({overflow:"hidden"});
var p=(n=="vertical")?"height":"width";
var l=(n=="vertical")?k.height():k.width();
if(m=="show"){k.css(p,0)
}var o={};
o[p]=m=="show"?l:0;
k.animate(o,a.duration,a.options.easing,function(){if(m=="hide"){q.hide()
}b.effects.restore(q,r);
b.effects.removeWrapper(q);
if(a.callback){a.callback.apply(q[0],arguments)
}q.dequeue()
})
})
}
})(jQuery);
(function(b){b.effects.bounce=function(a){return this.queue(function(){var A=b(this),u=["position","top","left"];
var v=b.effects.setMode(A,a.options.mode||"effect");
var r=a.options.direction||"up";
var C=a.options.distance||20;
var B=a.options.times||5;
var y=a.duration||250;
if(/show|hide/.test(v)){u.push("opacity")
}b.effects.save(A,u);
A.show();
b.effects.createWrapper(A);
var z=(r=="up"||r=="down")?"top":"left";
var i=(r=="up"||r=="left")?"pos":"neg";
var C=a.options.distance||(z=="top"?A.outerHeight({margin:true})/3:A.outerWidth({margin:true})/3);
if(v=="show"){A.css("opacity",0).css(z,i=="pos"?-C:C)
}if(v=="hide"){C=C/(B*2)
}if(v!="hide"){B--
}if(v=="show"){var x={opacity:1};
x[z]=(i=="pos"?"+=":"-=")+C;
A.animate(x,y/2,a.options.easing);
C=C/2;
B--
}for(var w=0;
w<B;
w++){var q={},s={};
q[z]=(i=="pos"?"-=":"+=")+C;
s[z]=(i=="pos"?"+=":"-=")+C;
A.animate(q,y/2,a.options.easing).animate(s,y/2,a.options.easing);
C=(v=="hide")?C*2:C/2
}if(v=="hide"){var x={opacity:0};
x[z]=(i=="pos"?"-=":"+=")+C;
A.animate(x,y/2,a.options.easing,function(){A.hide();
b.effects.restore(A,u);
b.effects.removeWrapper(A);
if(a.callback){a.callback.apply(this,arguments)
}})
}else{var q={},s={};
q[z]=(i=="pos"?"-=":"+=")+C;
s[z]=(i=="pos"?"+=":"-=")+C;
A.animate(q,y/2,a.options.easing).animate(s,y/2,a.options.easing,function(){b.effects.restore(A,u);
b.effects.removeWrapper(A);
if(a.callback){a.callback.apply(this,arguments)
}})
}A.queue("fx",function(){A.dequeue()
});
A.dequeue()
})
}
})(jQuery);
(function(b){b.effects.clip=function(a){return this.queue(function(){var q=b(this),m=["position","top","left","height","width"];
var n=b.effects.setMode(q,a.options.mode||"hide");
var l=a.options.direction||"vertical";
b.effects.save(q,m);
q.show();
var u=b.effects.createWrapper(q).css({overflow:"hidden"});
var r=q[0].tagName=="IMG"?u:q;
var p={size:(l=="vertical")?"height":"width",position:(l=="vertical")?"top":"left"};
var s=(l=="vertical")?r.height():r.width();
if(n=="show"){r.css(p.size,0);
r.css(p.position,s/2)
}var o={};
o[p.size]=n=="show"?s:0;
o[p.position]=n=="show"?0:s/2;
r.animate(o,{queue:false,duration:a.duration,easing:a.options.easing,complete:function(){if(n=="hide"){q.hide()
}b.effects.restore(q,m);
b.effects.removeWrapper(q);
if(a.callback){a.callback.apply(q[0],arguments)
}q.dequeue()
}})
})
}
})(jQuery);
(function(b){b.effects.drop=function(a){return this.queue(function(){var p=b(this),q=["position","top","left","opacity"];
var l=b.effects.setMode(p,a.options.mode||"hide");
var m=a.options.direction||"left";
b.effects.save(p,q);
p.show();
b.effects.createWrapper(p);
var o=(m=="up"||m=="down")?"top":"left";
var r=(m=="up"||m=="left")?"pos":"neg";
var k=a.options.distance||(o=="top"?p.outerHeight({margin:true})/2:p.outerWidth({margin:true})/2);
if(l=="show"){p.css("opacity",0).css(o,r=="pos"?-k:k)
}var n={opacity:l=="show"?1:0};
n[o]=(l=="show"?(r=="pos"?"+=":"-="):(r=="pos"?"-=":"+="))+k;
p.animate(n,{queue:false,duration:a.duration,easing:a.options.easing,complete:function(){if(l=="hide"){p.hide()
}b.effects.restore(p,q);
b.effects.removeWrapper(p);
if(a.callback){a.callback.apply(this,arguments)
}p.dequeue()
}})
})
}
})(jQuery);
(function(b){b.effects.explode=function(a){return this.queue(function(){var j=a.options.pieces?Math.round(Math.sqrt(a.options.pieces)):3;
var p=a.options.pieces?Math.round(Math.sqrt(a.options.pieces)):3;
a.options.mode=a.options.mode=="toggle"?(b(this).is(":visible")?"hide":"show"):a.options.mode;
var m=b(this).show().css("visibility","hidden");
var i=m.offset();
i.top-=parseInt(m.css("marginTop"),10)||0;
i.left-=parseInt(m.css("marginLeft"),10)||0;
var n=m.outerWidth(true);
var r=m.outerHeight(true);
for(var o=0;
o<j;
o++){for(var q=0;
q<p;
q++){m.clone().appendTo("body").wrap("<div></div>").css({position:"absolute",visibility:"visible",left:-q*(n/p),top:-o*(r/j)}).parent().addClass("ui-effects-explode").css({position:"absolute",overflow:"hidden",width:n/p,height:r/j,left:i.left+q*(n/p)+(a.options.mode=="show"?(q-Math.floor(p/2))*(n/p):0),top:i.top+o*(r/j)+(a.options.mode=="show"?(o-Math.floor(j/2))*(r/j):0),opacity:a.options.mode=="show"?0:1}).animate({left:i.left+q*(n/p)+(a.options.mode=="show"?0:(q-Math.floor(p/2))*(n/p)),top:i.top+o*(r/j)+(a.options.mode=="show"?0:(o-Math.floor(j/2))*(r/j)),opacity:a.options.mode=="show"?1:0},a.duration||500)
}}setTimeout(function(){a.options.mode=="show"?m.css({visibility:"visible"}):m.css({visibility:"visible"}).hide();
if(a.callback){a.callback.apply(m[0])
}m.dequeue();
b("div.ui-effects-explode").remove()
},a.duration||500)
})
}
})(jQuery);
(function(b){b.effects.fold=function(a){return this.queue(function(){var A=b(this),u=["position","top","left"];
var x=b.effects.setMode(A,a.options.mode||"hide");
var p=a.options.size||15;
var q=!(!a.options.horizFirst);
var y=a.duration?a.duration/2:b.fx.speeds._default/2;
b.effects.save(A,u);
A.show();
var B=b.effects.createWrapper(A).css({overflow:"hidden"});
var w=((x=="show")!=q);
var z=w?["width","height"]:["height","width"];
var C=w?[B.width(),B.height()]:[B.height(),B.width()];
var v=/([0-9]+)%/.exec(p);
if(v){p=parseInt(v[1],10)/100*C[x=="hide"?0:1]
}if(x=="show"){B.css(q?{height:0,width:p}:{height:p,width:0})
}var r={},s={};
r[z[0]]=x=="show"?C[0]:p;
s[z[1]]=x=="show"?C[1]:0;
B.animate(r,y,a.options.easing).animate(s,y,a.options.easing,function(){if(x=="hide"){A.hide()
}b.effects.restore(A,u);
b.effects.removeWrapper(A);
if(a.callback){a.callback.apply(A[0],arguments)
}A.dequeue()
})
})
}
})(jQuery);
(function(b){b.effects.highlight=function(a){return this.queue(function(){var l=b(this),m=["backgroundImage","backgroundColor","opacity"];
var i=b.effects.setMode(l,a.options.mode||"show");
var n=a.options.color||"#ffff99";
var j=l.css("backgroundColor");
b.effects.save(l,m);
l.show();
l.css({backgroundImage:"none",backgroundColor:n});
var k={backgroundColor:j};
if(i=="hide"){k.opacity=0
}l.animate(k,{queue:false,duration:a.duration,easing:a.options.easing,complete:function(){if(i=="hide"){l.hide()
}b.effects.restore(l,m);
if(i=="show"&&b.browser.msie){this.style.removeAttribute("filter")
}if(a.callback){a.callback.apply(this,arguments)
}l.dequeue()
}})
})
}
})(jQuery);
(function(b){b.effects.pulsate=function(a){return this.queue(function(){var k=b(this);
var h=b.effects.setMode(k,a.options.mode||"show");
var i=a.options.times||5;
var j=a.duration?a.duration/2:b.fx.speeds._default/2;
if(h=="hide"){i--
}if(k.is(":hidden")){k.css("opacity",0);
k.show();
k.animate({opacity:1},j,a.options.easing);
i=i-2
}for(var l=0;
l<i;
l++){k.animate({opacity:0},j,a.options.easing).animate({opacity:1},j,a.options.easing)
}if(h=="hide"){k.animate({opacity:0},j,a.options.easing,function(){k.hide();
if(a.callback){a.callback.apply(this,arguments)
}})
}else{k.animate({opacity:0},j,a.options.easing).animate({opacity:1},j,a.options.easing,function(){if(a.callback){a.callback.apply(this,arguments)
}})
}k.queue("fx",function(){k.dequeue()
});
k.dequeue()
})
}
})(jQuery);
(function(b){b.effects.puff=function(a){return this.queue(function(){var k=b(this);
var n=b.extend(true,{},a.options);
var i=b.effects.setMode(k,a.options.mode||"hide");
var j=parseInt(a.options.percent,10)||150;
n.fade=true;
var l={height:k.height(),width:k.width()};
var m=j/100;
k.from=(i=="hide")?l:{height:l.height*m,width:l.width*m};
n.from=k.from;
n.percent=(i=="hide")?j:100;
n.mode=i;
k.effect("scale",n,a.duration,a.callback);
k.dequeue()
})
};
b.effects.scale=function(a){return this.queue(function(){var n=b(this);
var q=b.extend(true,{},a.options);
var k=b.effects.setMode(n,a.options.mode||"effect");
var m=parseInt(a.options.percent,10)||(parseInt(a.options.percent,10)==0?0:(k=="hide"?0:100));
var l=a.options.direction||"both";
var r=a.options.origin;
if(k!="effect"){q.origin=r||["middle","center"];
q.restore=true
}var o={height:n.height(),width:n.width()};
n.from=a.options.from||(k=="show"?{height:0,width:0}:o);
var p={y:l!="horizontal"?(m/100):1,x:l!="vertical"?(m/100):1};
n.to={height:o.height*p.y,width:o.width*p.x};
if(a.options.fade){if(k=="show"){n.from.opacity=0;
n.to.opacity=1
}if(k=="hide"){n.from.opacity=1;
n.to.opacity=0
}}q.from=n.from;
q.to=n.to;
q.mode=k;
n.effect("size",q,a.duration,a.callback);
n.dequeue()
})
};
b.effects.size=function(a){return this.queue(function(){var E=b(this),s=["position","top","left","width","height","overflow","opacity"];
var u=["position","top","left","overflow","opacity"];
var x=["width","height","overflow"];
var q=["fontSize"];
var w=["borderTopWidth","borderBottomWidth","paddingTop","paddingBottom"];
var B=["borderLeftWidth","borderRightWidth","paddingLeft","paddingRight"];
var A=b.effects.setMode(E,a.options.mode||"effect");
var y=a.options.restore||false;
var C=a.options.scale||"both";
var r=a.options.origin;
var D={height:E.height(),width:E.width()};
E.from=a.options.from||D;
E.to=a.options.to||D;
if(r){var z=b.effects.getBaseline(r,D);
E.from.top=(D.height-E.from.height)*z.y;
E.from.left=(D.width-E.from.width)*z.x;
E.to.top=(D.height-E.to.height)*z.y;
E.to.left=(D.width-E.to.width)*z.x
}var v={from:{y:E.from.height/D.height,x:E.from.width/D.width},to:{y:E.to.height/D.height,x:E.to.width/D.width}};
if(C=="box"||C=="both"){if(v.from.y!=v.to.y){s=s.concat(w);
E.from=b.effects.setTransition(E,w,v.from.y,E.from);
E.to=b.effects.setTransition(E,w,v.to.y,E.to)
}if(v.from.x!=v.to.x){s=s.concat(B);
E.from=b.effects.setTransition(E,B,v.from.x,E.from);
E.to=b.effects.setTransition(E,B,v.to.x,E.to)
}}if(C=="content"||C=="both"){if(v.from.y!=v.to.y){s=s.concat(q);
E.from=b.effects.setTransition(E,q,v.from.y,E.from);
E.to=b.effects.setTransition(E,q,v.to.y,E.to)
}}b.effects.save(E,y?s:u);
E.show();
b.effects.createWrapper(E);
E.css("overflow","hidden").css(E.from);
if(C=="content"||C=="both"){w=w.concat(["marginTop","marginBottom"]).concat(q);
B=B.concat(["marginLeft","marginRight"]);
x=s.concat(w).concat(B);
E.find("*[width]").each(function(){child=b(this);
if(y){b.effects.save(child,x)
}var c={height:child.height(),width:child.width()};
child.from={height:c.height*v.from.y,width:c.width*v.from.x};
child.to={height:c.height*v.to.y,width:c.width*v.to.x};
if(v.from.y!=v.to.y){child.from=b.effects.setTransition(child,w,v.from.y,child.from);
child.to=b.effects.setTransition(child,w,v.to.y,child.to)
}if(v.from.x!=v.to.x){child.from=b.effects.setTransition(child,B,v.from.x,child.from);
child.to=b.effects.setTransition(child,B,v.to.x,child.to)
}child.css(child.from);
child.animate(child.to,a.duration,a.options.easing,function(){if(y){b.effects.restore(child,x)
}})
})
}E.animate(E.to,{queue:false,duration:a.duration,easing:a.options.easing,complete:function(){if(A=="hide"){E.hide()
}b.effects.restore(E,y?s:u);
b.effects.removeWrapper(E);
if(a.callback){a.callback.apply(this,arguments)
}E.dequeue()
}})
})
}
})(jQuery);
(function(b){b.effects.shake=function(a){return this.queue(function(){var A=b(this),u=["position","top","left"];
var v=b.effects.setMode(A,a.options.mode||"effect");
var r=a.options.direction||"left";
var C=a.options.distance||20;
var B=a.options.times||3;
var y=a.duration||a.options.duration||140;
b.effects.save(A,u);
A.show();
b.effects.createWrapper(A);
var z=(r=="up"||r=="down")?"top":"left";
var i=(r=="up"||r=="left")?"pos":"neg";
var x={},q={},s={};
x[z]=(i=="pos"?"-=":"+=")+C;
q[z]=(i=="pos"?"+=":"-=")+C*2;
s[z]=(i=="pos"?"-=":"+=")+C*2;
A.animate(x,y,a.options.easing);
for(var w=1;
w<B;
w++){A.animate(q,y,a.options.easing).animate(s,y,a.options.easing)
}A.animate(q,y,a.options.easing).animate(x,y/2,a.options.easing,function(){b.effects.restore(A,u);
b.effects.removeWrapper(A);
if(a.callback){a.callback.apply(this,arguments)
}});
A.queue("fx",function(){A.dequeue()
});
A.dequeue()
})
}
})(jQuery);
(function(b){b.effects.slide=function(a){return this.queue(function(){var p=b(this),q=["position","top","left"];
var l=b.effects.setMode(p,a.options.mode||"show");
var m=a.options.direction||"left";
b.effects.save(p,q);
p.show();
b.effects.createWrapper(p).css({overflow:"hidden"});
var o=(m=="up"||m=="down")?"top":"left";
var r=(m=="up"||m=="left")?"pos":"neg";
var k=a.options.distance||(o=="top"?p.outerHeight({margin:true}):p.outerWidth({margin:true}));
if(l=="show"){p.css(o,r=="pos"?-k:k)
}var n={};
n[o]=(l=="show"?(r=="pos"?"+=":"-="):(r=="pos"?"-=":"+="))+k;
p.animate(n,{queue:false,duration:a.duration,easing:a.options.easing,complete:function(){if(l=="hide"){p.hide()
}b.effects.restore(p,q);
b.effects.removeWrapper(p);
if(a.callback){a.callback.apply(this,arguments)
}p.dequeue()
}})
})
}
})(jQuery);
(function(b){b.effects.transfer=function(a){return this.queue(function(){var k=b(this),i=b(a.options.to),l=i.offset(),j={top:l.top,left:l.left,height:i.innerHeight(),width:i.innerWidth()},m=k.offset(),n=b('<div class="ui-effects-transfer"></div>').appendTo(document.body).addClass(a.options.className).css({top:m.top,left:m.left,height:k.innerHeight(),width:k.innerWidth(),position:"absolute"}).animate(j,a.duration,a.options.easing,function(){n.remove();
(a.callback&&a.callback.apply(k[0],arguments));
k.dequeue()
})
})
}
})(jQuery);
(function(b){b.widget("ui.accordion",{_init:function(){var e=this.options,a=this;
this.running=0;
if(e.collapsible==b.ui.accordion.defaults.collapsible&&e.alwaysOpen!=b.ui.accordion.defaults.alwaysOpen){e.collapsible=!e.alwaysOpen
}if(e.navigation){var f=this.element.find("a").filter(e.navigationFilter);
if(f.length){if(f.filter(e.header).length){this.active=f
}else{this.active=f.parent().parent().prev();
f.addClass("ui-accordion-content-active")
}}}this.element.addClass("ui-accordion ui-widget ui-helper-reset");
if(this.element[0].nodeName=="UL"){this.element.children("li").addClass("ui-accordion-li-fix")
}this.headers=this.element.find(e.header).addClass("ui-accordion-header ui-helper-reset ui-state-default ui-corner-all").bind("mouseenter.accordion",function(){b(this).addClass("ui-state-hover")
}).bind("mouseleave.accordion",function(){b(this).removeClass("ui-state-hover")
}).bind("focus.accordion",function(){b(this).addClass("ui-state-focus")
}).bind("blur.accordion",function(){b(this).removeClass("ui-state-focus")
});
this.headers.next().addClass("ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom");
this.active=this._findActive(this.active||e.active).toggleClass("ui-state-default").toggleClass("ui-state-active").toggleClass("ui-corner-all").toggleClass("ui-corner-top");
this.active.next().addClass("ui-accordion-content-active");
b("<span/>").addClass("ui-icon "+e.icons.header).prependTo(this.headers);
this.active.find(".ui-icon").toggleClass(e.icons.header).toggleClass(e.icons.headerSelected);
if(b.browser.msie){this.element.find("a").css("zoom","1")
}this.resize();
this.element.attr("role","tablist");
this.headers.attr("role","tab").bind("keydown",function(c){return a._keydown(c)
}).next().attr("role","tabpanel");
this.headers.not(this.active||"").attr("aria-expanded","false").attr("tabIndex","-1").next().hide();
if(!this.active.length){this.headers.eq(0).attr("tabIndex","0")
}else{this.active.attr("aria-expanded","true").attr("tabIndex","0")
}if(!b.browser.safari){this.headers.find("a").attr("tabIndex","-1")
}if(e.event){this.headers.bind((e.event)+".accordion",function(c){return a._clickHandler.call(a,c,this)
})
}},destroy:function(){var d=this.options;
this.element.removeClass("ui-accordion ui-widget ui-helper-reset").removeAttr("role").unbind(".accordion").removeData("accordion");
this.headers.unbind(".accordion").removeClass("ui-accordion-header ui-helper-reset ui-state-default ui-corner-all ui-state-active ui-corner-top").removeAttr("role").removeAttr("aria-expanded").removeAttr("tabindex");
this.headers.find("a").removeAttr("tabindex");
this.headers.children(".ui-icon").remove();
var a=this.headers.next().css("display","").removeAttr("role").removeClass("ui-helper-reset ui-widget-content ui-corner-bottom ui-accordion-content ui-accordion-content-active");
if(d.autoHeight||d.fillHeight){a.css("height","")
}},_setData:function(a,d){if(a=="alwaysOpen"){a="collapsible";
d=!d
}b.widget.prototype._setData.apply(this,arguments)
},_keydown:function(j){var h=this.options,i=b.ui.keyCode;
if(h.disabled||j.altKey||j.ctrlKey){return
}var k=this.headers.length;
var a=this.headers.index(j.target);
var l=false;
switch(j.keyCode){case i.RIGHT:case i.DOWN:l=this.headers[(a+1)%k];
break;
case i.LEFT:case i.UP:l=this.headers[(a-1+k)%k];
break;
case i.SPACE:case i.ENTER:return this._clickHandler({target:j.target},j.target)
}if(l){b(j.target).attr("tabIndex","-1");
b(l).attr("tabIndex","0");
l.focus();
return false
}return true
},resize:function(){var f=this.options,g;
if(f.fillSpace){if(b.browser.msie){var a=this.element.parent().css("overflow");
this.element.parent().css("overflow","hidden")
}g=this.element.parent().height();
if(b.browser.msie){this.element.parent().css("overflow",a)
}this.headers.each(function(){g-=b(this).outerHeight()
});
var h=0;
this.headers.next().each(function(){h=Math.max(h,b(this).innerHeight()-b(this).height())
}).height(Math.max(0,g-h)).css("overflow","auto")
}else{if(f.autoHeight){g=0;
this.headers.next().each(function(){g=Math.max(g,b(this).outerHeight())
}).height(g)
}}},activate:function(a){var d=this._findActive(a)[0];
this._clickHandler({target:d},d)
},_findActive:function(a){return a?typeof a=="number"?this.headers.filter(":eq("+a+")"):this.headers.not(this.headers.not(a)):a===false?b([]):this.headers.filter(":eq(0)")
},_clickHandler:function(r,n){var p=this.options;
if(p.disabled){return false
}if(!r.target&&p.collapsible){this.active.removeClass("ui-state-active ui-corner-top").addClass("ui-state-default ui-corner-all").find(".ui-icon").removeClass(p.icons.headerSelected).addClass(p.icons.header);
this.active.next().addClass("ui-accordion-content-active");
var l=this.active.next(),o={options:p,newHeader:b([]),oldHeader:p.active,newContent:b([]),oldContent:l},q=(this.active=b([]));
this._toggle(q,l,o);
return false
}var m=b(r.currentTarget||n);
var k=m[0]==this.active[0];
if(this.running||(!p.collapsible&&k)){return false
}this.active.removeClass("ui-state-active ui-corner-top").addClass("ui-state-default ui-corner-all").find(".ui-icon").removeClass(p.icons.headerSelected).addClass(p.icons.header);
this.active.next().addClass("ui-accordion-content-active");
if(!k){m.removeClass("ui-state-default ui-corner-all").addClass("ui-state-active ui-corner-top").find(".ui-icon").removeClass(p.icons.header).addClass(p.icons.headerSelected);
m.next().addClass("ui-accordion-content-active")
}var q=m.next(),l=this.active.next(),o={options:p,newHeader:k&&p.collapsible?b([]):m,oldHeader:this.active,newContent:k&&p.collapsible?b([]):q.find("> *"),oldContent:l.find("> *")},a=this.headers.index(this.active[0])>this.headers.index(m[0]);
this.active=k?b([]):m;
this._toggle(q,l,o,k,a);
return false
},_toggle:function(y,q,s,p,o){var w=this.options,a=this;
this.toShow=y;
this.toHide=q;
this.data=s;
var x=function(){if(!a){return
}return a._completed.apply(a,arguments)
};
this._trigger("changestart",null,this.data);
this.running=q.size()===0?y.size():q.size();
if(w.animated){var u={};
if(w.collapsible&&p){u={toShow:b([]),toHide:q,complete:x,down:o,autoHeight:w.autoHeight||w.fillSpace}
}else{u={toShow:y,toHide:q,complete:x,down:o,autoHeight:w.autoHeight||w.fillSpace}
}if(!w.proxied){w.proxied=w.animated
}if(!w.proxiedDuration){w.proxiedDuration=w.duration
}w.animated=b.isFunction(w.proxied)?w.proxied(u):w.proxied;
w.duration=b.isFunction(w.proxiedDuration)?w.proxiedDuration(u):w.proxiedDuration;
var n=b.ui.accordion.animations,v=w.duration,r=w.animated;
if(!n[r]){n[r]=function(c){this.slide(c,{easing:r,duration:v||700})
}
}n[r](u)
}else{if(w.collapsible&&p){y.toggle()
}else{q.hide();
y.show()
}x(true)
}q.prev().attr("aria-expanded","false").attr("tabIndex","-1").blur();
y.prev().attr("aria-expanded","true").attr("tabIndex","0").focus()
},_completed:function(a){var d=this.options;
this.running=a?0:--this.running;
if(this.running){return
}if(d.clearStyle){this.toShow.add(this.toHide).css({height:"",overflow:""})
}this._trigger("change",null,this.data)
}});
b.extend(b.ui.accordion,{version:"1.7.2",defaults:{active:null,alwaysOpen:true,animated:"slide",autoHeight:true,clearStyle:false,collapsible:false,event:"click",fillSpace:false,header:"> li > :first-child,> :not(li):even",icons:{header:"ui-icon-triangle-1-e",headerSelected:"ui-icon-triangle-1-s"},navigation:false,navigationFilter:function(){return this.href.toLowerCase()==location.href.toLowerCase()
}},animations:{slide:function(a,l){a=b.extend({easing:"swing",duration:300},a,l);
if(!a.toHide.size()){a.toShow.animate({height:"show"},a);
return
}if(!a.toShow.size()){a.toHide.animate({height:"hide"},a);
return
}var q=a.toShow.css("overflow"),m,p={},n={},o=["height","paddingTop","paddingBottom"],r;
var k=a.toShow;
r=k[0].style.width;
k.width(parseInt(k.parent().width(),10)-parseInt(k.css("paddingLeft"),10)-parseInt(k.css("paddingRight"),10)-(parseInt(k.css("borderLeftWidth"),10)||0)-(parseInt(k.css("borderRightWidth"),10)||0));
b.each(o,function(e,c){n[c]="hide";
var d=(""+b.css(a.toShow[0],c)).match(/^([\d+-.]+)(.*)$/);
p[c]={value:d[1],unit:d[2]||"px"}
});
a.toShow.css({height:0,overflow:"hidden"}).show();
a.toHide.filter(":hidden").each(a.complete).end().filter(":visible").animate(n,{step:function(d,c){if(c.prop=="height"){m=(c.now-c.start)/(c.end-c.start)
}a.toShow[0].style[c.prop]=(m*p[c.prop].value)+p[c.prop].unit
},duration:a.duration,easing:a.easing,complete:function(){if(!a.autoHeight){a.toShow.css("height","")
}a.toShow.css("width",r);
a.toShow.css({overflow:q});
a.complete()
}})
},bounceslide:function(a){this.slide(a,{easing:a.down?"easeOutBounce":"swing",duration:a.down?1000:200})
},easeslide:function(a){this.slide(a,{easing:"easeinout",duration:700})
}}})
})(jQuery);
(function($){$.extend($.ui,{datepicker:{version:"1.7.2"}});
var PROP_NAME="datepicker";
function Datepicker(){this.debug=false;
this._curInst=null;
this._keyEvent=false;
this._disabledInputs=[];
this._datepickerShowing=false;
this._inDialog=false;
this._mainDivId="ui-datepicker-div";
this._inlineClass="ui-datepicker-inline";
this._appendClass="ui-datepicker-append";
this._triggerClass="ui-datepicker-trigger";
this._dialogClass="ui-datepicker-dialog";
this._disableClass="ui-datepicker-disabled";
this._unselectableClass="ui-datepicker-unselectable";
this._currentClass="ui-datepicker-current-day";
this._dayOverClass="ui-datepicker-days-cell-over";
this.regional=[];
this.regional[""]={closeText:"Done",prevText:"Prev",nextText:"Next",currentText:"Today",monthNames:["January","February","March","April","May","June","July","August","September","October","November","December"],monthNamesShort:["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"],dayNames:["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],dayNamesShort:["Sun","Mon","Tue","Wed","Thu","Fri","Sat"],dayNamesMin:["Su","Mo","Tu","We","Th","Fr","Sa"],dateFormat:"mm/dd/yy",firstDay:0,isRTL:false};
this._defaults={showOn:"focus",showAnim:"show",showOptions:{},defaultDate:null,appendText:"",buttonText:"...",buttonImage:"",buttonImageOnly:false,hideIfNoPrevNext:false,navigationAsDateFormat:false,gotoCurrent:false,changeMonth:false,changeYear:false,showMonthAfterYear:false,yearRange:"-10:+10",showOtherMonths:false,calculateWeek:this.iso8601Week,shortYearCutoff:"+10",minDate:null,maxDate:null,duration:"normal",beforeShowDay:null,beforeShow:null,onSelect:null,onChangeMonthYear:null,onClose:null,numberOfMonths:1,showCurrentAtPos:0,stepMonths:1,stepBigMonths:12,altField:"",altFormat:"",constrainInput:true,showButtonPanel:false};
$.extend(this._defaults,this.regional[""]);
this.dpDiv=$('<div id="'+this._mainDivId+'" class="ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all ui-helper-hidden-accessible"></div>')
}$.extend(Datepicker.prototype,{markerClassName:"hasDatepicker",log:function(){if(this.debug){console.log.apply("",arguments)
}},setDefaults:function(settings){extendRemove(this._defaults,settings||{});
return this
},_attachDatepicker:function(target,settings){var inlineSettings=null;
for(var attrName in this._defaults){var attrValue=target.getAttribute("date:"+attrName);
if(attrValue){inlineSettings=inlineSettings||{};
try{inlineSettings[attrName]=eval(attrValue)
}catch(err){inlineSettings[attrName]=attrValue
}}}var nodeName=target.nodeName.toLowerCase();
var inline=(nodeName=="div"||nodeName=="span");
if(!target.id){target.id="dp"+(++this.uuid)
}var inst=this._newInst($(target),inline);
inst.settings=$.extend({},settings||{},inlineSettings||{});
if(nodeName=="input"){this._connectDatepicker(target,inst)
}else{if(inline){this._inlineDatepicker(target,inst)
}}},_newInst:function(target,inline){var id=target[0].id.replace(/([:\[\]\.])/g,"\\\\$1");
return{id:id,input:target,selectedDay:0,selectedMonth:0,selectedYear:0,drawMonth:0,drawYear:0,inline:inline,dpDiv:(!inline?this.dpDiv:$('<div class="'+this._inlineClass+' ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all"></div>'))}
},_connectDatepicker:function(target,inst){var input=$(target);
inst.append=$([]);
inst.trigger=$([]);
if(input.hasClass(this.markerClassName)){return
}var appendText=this._get(inst,"appendText");
var isRTL=this._get(inst,"isRTL");
if(appendText){inst.append=$('<span class="'+this._appendClass+'">'+appendText+"</span>");
input[isRTL?"before":"after"](inst.append)
}var showOn=this._get(inst,"showOn");
if(showOn=="focus"||showOn=="both"){input.focus(this._showDatepicker)
}if(showOn=="button"||showOn=="both"){var buttonText=this._get(inst,"buttonText");
var buttonImage=this._get(inst,"buttonImage");
inst.trigger=$(this._get(inst,"buttonImageOnly")?$("<img/>").addClass(this._triggerClass).attr({src:buttonImage,alt:buttonText,title:buttonText}):$('<button type="button"></button>').addClass(this._triggerClass).html(buttonImage==""?buttonText:$("<img/>").attr({src:buttonImage,alt:buttonText,title:buttonText})));
input[isRTL?"before":"after"](inst.trigger);
inst.trigger.click(function(){if($.datepicker._datepickerShowing&&$.datepicker._lastInput==target){$.datepicker._hideDatepicker()
}else{$.datepicker._showDatepicker(target)
}return false
})
}input.addClass(this.markerClassName).keydown(this._doKeyDown).keypress(this._doKeyPress).bind("setData.datepicker",function(event,key,value){inst.settings[key]=value
}).bind("getData.datepicker",function(event,key){return this._get(inst,key)
});
$.data(target,PROP_NAME,inst)
},_inlineDatepicker:function(target,inst){var divSpan=$(target);
if(divSpan.hasClass(this.markerClassName)){return
}divSpan.addClass(this.markerClassName).append(inst.dpDiv).bind("setData.datepicker",function(event,key,value){inst.settings[key]=value
}).bind("getData.datepicker",function(event,key){return this._get(inst,key)
});
$.data(target,PROP_NAME,inst);
this._setDate(inst,this._getDefaultDate(inst));
this._updateDatepicker(inst);
this._updateAlternate(inst)
},_dialogDatepicker:function(input,dateText,onSelect,settings,pos){var inst=this._dialogInst;
if(!inst){var id="dp"+(++this.uuid);
this._dialogInput=$('<input type="text" id="'+id+'" size="1" style="position: absolute; top: -100px;"/>');
this._dialogInput.keydown(this._doKeyDown);
$("body").append(this._dialogInput);
inst=this._dialogInst=this._newInst(this._dialogInput,false);
inst.settings={};
$.data(this._dialogInput[0],PROP_NAME,inst)
}extendRemove(inst.settings,settings||{});
this._dialogInput.val(dateText);
this._pos=(pos?(pos.length?pos:[pos.pageX,pos.pageY]):null);
if(!this._pos){var browserWidth=window.innerWidth||document.documentElement.clientWidth||document.body.clientWidth;
var browserHeight=window.innerHeight||document.documentElement.clientHeight||document.body.clientHeight;
var scrollX=document.documentElement.scrollLeft||document.body.scrollLeft;
var scrollY=document.documentElement.scrollTop||document.body.scrollTop;
this._pos=[(browserWidth/2)-100+scrollX,(browserHeight/2)-150+scrollY]
}this._dialogInput.css("left",this._pos[0]+"px").css("top",this._pos[1]+"px");
inst.settings.onSelect=onSelect;
this._inDialog=true;
this.dpDiv.addClass(this._dialogClass);
this._showDatepicker(this._dialogInput[0]);
if($.blockUI){$.blockUI(this.dpDiv)
}$.data(this._dialogInput[0],PROP_NAME,inst);
return this
},_destroyDatepicker:function(target){var $target=$(target);
var inst=$.data(target,PROP_NAME);
if(!$target.hasClass(this.markerClassName)){return
}var nodeName=target.nodeName.toLowerCase();
$.removeData(target,PROP_NAME);
if(nodeName=="input"){inst.append.remove();
inst.trigger.remove();
$target.removeClass(this.markerClassName).unbind("focus",this._showDatepicker).unbind("keydown",this._doKeyDown).unbind("keypress",this._doKeyPress)
}else{if(nodeName=="div"||nodeName=="span"){$target.removeClass(this.markerClassName).empty()
}}},_enableDatepicker:function(target){var $target=$(target);
var inst=$.data(target,PROP_NAME);
if(!$target.hasClass(this.markerClassName)){return
}var nodeName=target.nodeName.toLowerCase();
if(nodeName=="input"){target.disabled=false;
inst.trigger.filter("button").each(function(){this.disabled=false
}).end().filter("img").css({opacity:"1.0",cursor:""})
}else{if(nodeName=="div"||nodeName=="span"){var inline=$target.children("."+this._inlineClass);
inline.children().removeClass("ui-state-disabled")
}}this._disabledInputs=$.map(this._disabledInputs,function(value){return(value==target?null:value)
})
},_disableDatepicker:function(target){var $target=$(target);
var inst=$.data(target,PROP_NAME);
if(!$target.hasClass(this.markerClassName)){return
}var nodeName=target.nodeName.toLowerCase();
if(nodeName=="input"){target.disabled=true;
inst.trigger.filter("button").each(function(){this.disabled=true
}).end().filter("img").css({opacity:"0.5",cursor:"default"})
}else{if(nodeName=="div"||nodeName=="span"){var inline=$target.children("."+this._inlineClass);
inline.children().addClass("ui-state-disabled")
}}this._disabledInputs=$.map(this._disabledInputs,function(value){return(value==target?null:value)
});
this._disabledInputs[this._disabledInputs.length]=target
},_isDisabledDatepicker:function(target){if(!target){return false
}for(var i=0;
i<this._disabledInputs.length;
i++){if(this._disabledInputs[i]==target){return true
}}return false
},_getInst:function(target){try{return $.data(target,PROP_NAME)
}catch(err){throw"Missing instance data for this datepicker"
}},_optionDatepicker:function(target,name,value){var inst=this._getInst(target);
if(arguments.length==2&&typeof name=="string"){return(name=="defaults"?$.extend({},$.datepicker._defaults):(inst?(name=="all"?$.extend({},inst.settings):this._get(inst,name)):null))
}var settings=name||{};
if(typeof name=="string"){settings={};
settings[name]=value
}if(inst){if(this._curInst==inst){this._hideDatepicker(null)
}var date=this._getDateDatepicker(target);
extendRemove(inst.settings,settings);
this._setDateDatepicker(target,date);
this._updateDatepicker(inst)
}},_changeDatepicker:function(target,name,value){this._optionDatepicker(target,name,value)
},_refreshDatepicker:function(target){var inst=this._getInst(target);
if(inst){this._updateDatepicker(inst)
}},_setDateDatepicker:function(target,date,endDate){var inst=this._getInst(target);
if(inst){this._setDate(inst,date,endDate);
this._updateDatepicker(inst);
this._updateAlternate(inst)
}},_getDateDatepicker:function(target){var inst=this._getInst(target);
if(inst&&!inst.inline){this._setDateFromField(inst)
}return(inst?this._getDate(inst):null)
},_doKeyDown:function(event){var inst=$.datepicker._getInst(event.target);
var handled=true;
var isRTL=inst.dpDiv.is(".ui-datepicker-rtl");
inst._keyEvent=true;
if($.datepicker._datepickerShowing){switch(event.keyCode){case 9:$.datepicker._hideDatepicker(null,"");
break;
case 13:var sel=$("td."+$.datepicker._dayOverClass+", td."+$.datepicker._currentClass,inst.dpDiv);
if(sel[0]){$.datepicker._selectDay(event.target,inst.selectedMonth,inst.selectedYear,sel[0])
}else{$.datepicker._hideDatepicker(null,$.datepicker._get(inst,"duration"))
}return false;
break;
case 27:$.datepicker._hideDatepicker(null,$.datepicker._get(inst,"duration"));
break;
case 33:$.datepicker._adjustDate(event.target,(event.ctrlKey?-$.datepicker._get(inst,"stepBigMonths"):-$.datepicker._get(inst,"stepMonths")),"M");
break;
case 34:$.datepicker._adjustDate(event.target,(event.ctrlKey?+$.datepicker._get(inst,"stepBigMonths"):+$.datepicker._get(inst,"stepMonths")),"M");
break;
case 35:if(event.ctrlKey||event.metaKey){$.datepicker._clearDate(event.target)
}handled=event.ctrlKey||event.metaKey;
break;
case 36:if(event.ctrlKey||event.metaKey){$.datepicker._gotoToday(event.target)
}handled=event.ctrlKey||event.metaKey;
break;
case 37:if(event.ctrlKey||event.metaKey){$.datepicker._adjustDate(event.target,(isRTL?+1:-1),"D")
}handled=event.ctrlKey||event.metaKey;
if(event.originalEvent.altKey){$.datepicker._adjustDate(event.target,(event.ctrlKey?-$.datepicker._get(inst,"stepBigMonths"):-$.datepicker._get(inst,"stepMonths")),"M")
}break;
case 38:if(event.ctrlKey||event.metaKey){$.datepicker._adjustDate(event.target,-7,"D")
}handled=event.ctrlKey||event.metaKey;
break;
case 39:if(event.ctrlKey||event.metaKey){$.datepicker._adjustDate(event.target,(isRTL?-1:+1),"D")
}handled=event.ctrlKey||event.metaKey;
if(event.originalEvent.altKey){$.datepicker._adjustDate(event.target,(event.ctrlKey?+$.datepicker._get(inst,"stepBigMonths"):+$.datepicker._get(inst,"stepMonths")),"M")
}break;
case 40:if(event.ctrlKey||event.metaKey){$.datepicker._adjustDate(event.target,+7,"D")
}handled=event.ctrlKey||event.metaKey;
break;
default:handled=false
}}else{if(event.keyCode==36&&event.ctrlKey){$.datepicker._showDatepicker(this)
}else{handled=false
}}if(handled){event.preventDefault();
event.stopPropagation()
}},_doKeyPress:function(event){var inst=$.datepicker._getInst(event.target);
if($.datepicker._get(inst,"constrainInput")){var chars=$.datepicker._possibleChars($.datepicker._get(inst,"dateFormat"));
var chr=String.fromCharCode(event.charCode==undefined?event.keyCode:event.charCode);
return event.ctrlKey||(chr<" "||!chars||chars.indexOf(chr)>-1)
}},_showDatepicker:function(input){input=input.target||input;
if(input.nodeName.toLowerCase()!="input"){input=$("input",input.parentNode)[0]
}if($.datepicker._isDisabledDatepicker(input)||$.datepicker._lastInput==input){return
}var inst=$.datepicker._getInst(input);
var beforeShow=$.datepicker._get(inst,"beforeShow");
extendRemove(inst.settings,(beforeShow?beforeShow.apply(input,[input,inst]):{}));
$.datepicker._hideDatepicker(null,"");
$.datepicker._lastInput=input;
$.datepicker._setDateFromField(inst);
if($.datepicker._inDialog){input.value=""
}if(!$.datepicker._pos){$.datepicker._pos=$.datepicker._findPos(input);
$.datepicker._pos[1]+=input.offsetHeight
}var isFixed=false;
$(input).parents().each(function(){isFixed|=$(this).css("position")=="fixed";
return !isFixed
});
if(isFixed&&$.browser.opera){$.datepicker._pos[0]-=document.documentElement.scrollLeft;
$.datepicker._pos[1]-=document.documentElement.scrollTop
}var offset={left:$.datepicker._pos[0],top:$.datepicker._pos[1]};
$.datepicker._pos=null;
inst.rangeStart=null;
inst.dpDiv.css({position:"absolute",display:"block",top:"-1000px"});
$.datepicker._updateDatepicker(inst);
offset=$.datepicker._checkOffset(inst,offset,isFixed);
inst.dpDiv.css({position:($.datepicker._inDialog&&$.blockUI?"static":(isFixed?"fixed":"absolute")),display:"none",left:offset.left+"px",top:offset.top+"px"});
if(!inst.inline){var showAnim=$.datepicker._get(inst,"showAnim")||"show";
var duration=$.datepicker._get(inst,"duration");
var postProcess=function(){$.datepicker._datepickerShowing=true;
if($.browser.msie&&parseInt($.browser.version,10)<7){$("iframe.ui-datepicker-cover").css({width:inst.dpDiv.width()+4,height:inst.dpDiv.height()+4})
}};
if($.effects&&$.effects[showAnim]){inst.dpDiv.show(showAnim,$.datepicker._get(inst,"showOptions"),duration,postProcess)
}else{inst.dpDiv[showAnim](duration,postProcess)
}if(duration==""){postProcess()
}if(inst.input[0].type!="hidden"){inst.input[0].focus()
}$.datepicker._curInst=inst
}},_updateDatepicker:function(inst){var dims={width:inst.dpDiv.width()+4,height:inst.dpDiv.height()+4};
var self=this;
inst.dpDiv.empty().append(this._generateHTML(inst)).find("iframe.ui-datepicker-cover").css({width:dims.width,height:dims.height}).end().find("button, .ui-datepicker-prev, .ui-datepicker-next, .ui-datepicker-calendar td a").bind("mouseout",function(){$(this).removeClass("ui-state-hover");
if(this.className.indexOf("ui-datepicker-prev")!=-1){$(this).removeClass("ui-datepicker-prev-hover")
}if(this.className.indexOf("ui-datepicker-next")!=-1){$(this).removeClass("ui-datepicker-next-hover")
}}).bind("mouseover",function(){if(!self._isDisabledDatepicker(inst.inline?inst.dpDiv.parent()[0]:inst.input[0])){$(this).parents(".ui-datepicker-calendar").find("a").removeClass("ui-state-hover");
$(this).addClass("ui-state-hover");
if(this.className.indexOf("ui-datepicker-prev")!=-1){$(this).addClass("ui-datepicker-prev-hover")
}if(this.className.indexOf("ui-datepicker-next")!=-1){$(this).addClass("ui-datepicker-next-hover")
}}}).end().find("."+this._dayOverClass+" a").trigger("mouseover").end();
var numMonths=this._getNumberOfMonths(inst);
var cols=numMonths[1];
var width=17;
if(cols>1){inst.dpDiv.addClass("ui-datepicker-multi-"+cols).css("width",(width*cols)+"em")
}else{inst.dpDiv.removeClass("ui-datepicker-multi-2 ui-datepicker-multi-3 ui-datepicker-multi-4").width("")
}inst.dpDiv[(numMonths[0]!=1||numMonths[1]!=1?"add":"remove")+"Class"]("ui-datepicker-multi");
inst.dpDiv[(this._get(inst,"isRTL")?"add":"remove")+"Class"]("ui-datepicker-rtl");
if(inst.input&&inst.input[0].type!="hidden"&&inst==$.datepicker._curInst){$(inst.input[0]).focus()
}},_checkOffset:function(inst,offset,isFixed){var dpWidth=inst.dpDiv.outerWidth();
var dpHeight=inst.dpDiv.outerHeight();
var inputWidth=inst.input?inst.input.outerWidth():0;
var inputHeight=inst.input?inst.input.outerHeight():0;
var viewWidth=(window.innerWidth||document.documentElement.clientWidth||document.body.clientWidth)+$(document).scrollLeft();
var viewHeight=(window.innerHeight||document.documentElement.clientHeight||document.body.clientHeight)+$(document).scrollTop();
offset.left-=(this._get(inst,"isRTL")?(dpWidth-inputWidth):0);
offset.left-=(isFixed&&offset.left==inst.input.offset().left)?$(document).scrollLeft():0;
offset.top-=(isFixed&&offset.top==(inst.input.offset().top+inputHeight))?$(document).scrollTop():0;
offset.left-=(offset.left+dpWidth>viewWidth&&viewWidth>dpWidth)?Math.abs(offset.left+dpWidth-viewWidth):0;
offset.top-=(offset.top+dpHeight>viewHeight&&viewHeight>dpHeight)?Math.abs(offset.top+dpHeight+inputHeight*2-viewHeight):0;
return offset
},_findPos:function(obj){while(obj&&(obj.type=="hidden"||obj.nodeType!=1)){obj=obj.nextSibling
}var position=$(obj).offset();
return[position.left,position.top]
},_hideDatepicker:function(input,duration){var inst=this._curInst;
if(!inst||(input&&inst!=$.data(input,PROP_NAME))){return
}if(inst.stayOpen){this._selectDate("#"+inst.id,this._formatDate(inst,inst.currentDay,inst.currentMonth,inst.currentYear))
}inst.stayOpen=false;
if(this._datepickerShowing){duration=(duration!=null?duration:this._get(inst,"duration"));
var showAnim=this._get(inst,"showAnim");
var postProcess=function(){$.datepicker._tidyDialog(inst)
};
if(duration!=""&&$.effects&&$.effects[showAnim]){inst.dpDiv.hide(showAnim,$.datepicker._get(inst,"showOptions"),duration,postProcess)
}else{inst.dpDiv[(duration==""?"hide":(showAnim=="slideDown"?"slideUp":(showAnim=="fadeIn"?"fadeOut":"hide")))](duration,postProcess)
}if(duration==""){this._tidyDialog(inst)
}var onClose=this._get(inst,"onClose");
if(onClose){onClose.apply((inst.input?inst.input[0]:null),[(inst.input?inst.input.val():""),inst])
}this._datepickerShowing=false;
this._lastInput=null;
if(this._inDialog){this._dialogInput.css({position:"absolute",left:"0",top:"-100px"});
if($.blockUI){$.unblockUI();
$("body").append(this.dpDiv)
}}this._inDialog=false
}this._curInst=null
},_tidyDialog:function(inst){inst.dpDiv.removeClass(this._dialogClass).unbind(".ui-datepicker-calendar")
},_checkExternalClick:function(event){if(!$.datepicker._curInst){return
}var $target=$(event.target);
if(($target.parents("#"+$.datepicker._mainDivId).length==0)&&!$target.hasClass($.datepicker.markerClassName)&&!$target.hasClass($.datepicker._triggerClass)&&$.datepicker._datepickerShowing&&!($.datepicker._inDialog&&$.blockUI)){$.datepicker._hideDatepicker(null,"")
}},_adjustDate:function(id,offset,period){var target=$(id);
var inst=this._getInst(target[0]);
if(this._isDisabledDatepicker(target[0])){return
}this._adjustInstDate(inst,offset+(period=="M"?this._get(inst,"showCurrentAtPos"):0),period);
this._updateDatepicker(inst)
},_gotoToday:function(id){var target=$(id);
var inst=this._getInst(target[0]);
if(this._get(inst,"gotoCurrent")&&inst.currentDay){inst.selectedDay=inst.currentDay;
inst.drawMonth=inst.selectedMonth=inst.currentMonth;
inst.drawYear=inst.selectedYear=inst.currentYear
}else{var date=new Date();
inst.selectedDay=date.getDate();
inst.drawMonth=inst.selectedMonth=date.getMonth();
inst.drawYear=inst.selectedYear=date.getFullYear()
}this._notifyChange(inst);
this._adjustDate(target)
},_selectMonthYear:function(id,select,period){var target=$(id);
var inst=this._getInst(target[0]);
inst._selectingMonthYear=false;
inst["selected"+(period=="M"?"Month":"Year")]=inst["draw"+(period=="M"?"Month":"Year")]=parseInt(select.options[select.selectedIndex].value,10);
this._notifyChange(inst);
this._adjustDate(target)
},_clickMonthYear:function(id){var target=$(id);
var inst=this._getInst(target[0]);
if(inst.input&&inst._selectingMonthYear&&!$.browser.msie){inst.input[0].focus()
}inst._selectingMonthYear=!inst._selectingMonthYear
},_selectDay:function(id,month,year,td){var target=$(id);
if($(td).hasClass(this._unselectableClass)||this._isDisabledDatepicker(target[0])){return
}var inst=this._getInst(target[0]);
inst.selectedDay=inst.currentDay=$("a",td).html();
inst.selectedMonth=inst.currentMonth=month;
inst.selectedYear=inst.currentYear=year;
if(inst.stayOpen){inst.endDay=inst.endMonth=inst.endYear=null
}this._selectDate(id,this._formatDate(inst,inst.currentDay,inst.currentMonth,inst.currentYear));
if(inst.stayOpen){inst.rangeStart=this._daylightSavingAdjust(new Date(inst.currentYear,inst.currentMonth,inst.currentDay));
this._updateDatepicker(inst)
}},_clearDate:function(id){var target=$(id);
var inst=this._getInst(target[0]);
inst.stayOpen=false;
inst.endDay=inst.endMonth=inst.endYear=inst.rangeStart=null;
this._selectDate(target,"")
},_selectDate:function(id,dateStr){var target=$(id);
var inst=this._getInst(target[0]);
dateStr=(dateStr!=null?dateStr:this._formatDate(inst));
if(inst.input){inst.input.val(dateStr)
}this._updateAlternate(inst);
var onSelect=this._get(inst,"onSelect");
if(onSelect){onSelect.apply((inst.input?inst.input[0]:null),[dateStr,inst])
}else{if(inst.input){inst.input.trigger("change")
}}if(inst.inline){this._updateDatepicker(inst)
}else{if(!inst.stayOpen){this._hideDatepicker(null,this._get(inst,"duration"));
this._lastInput=inst.input[0];
if(typeof(inst.input[0])!="object"){inst.input[0].focus()
}this._lastInput=null
}}},_updateAlternate:function(inst){var altField=this._get(inst,"altField");
if(altField){var altFormat=this._get(inst,"altFormat")||this._get(inst,"dateFormat");
var date=this._getDate(inst);
dateStr=this.formatDate(altFormat,date,this._getFormatConfig(inst));
$(altField).each(function(){$(this).val(dateStr)
})
}},noWeekends:function(date){var day=date.getDay();
return[(day>0&&day<6),""]
},iso8601Week:function(date){var checkDate=new Date(date.getFullYear(),date.getMonth(),date.getDate());
var firstMon=new Date(checkDate.getFullYear(),1-1,4);
var firstDay=firstMon.getDay()||7;
firstMon.setDate(firstMon.getDate()+1-firstDay);
if(firstDay<4&&checkDate<firstMon){checkDate.setDate(checkDate.getDate()-3);
return $.datepicker.iso8601Week(checkDate)
}else{if(checkDate>new Date(checkDate.getFullYear(),12-1,28)){firstDay=new Date(checkDate.getFullYear()+1,1-1,4).getDay()||7;
if(firstDay>4&&(checkDate.getDay()||7)<firstDay-3){return 1
}}}return Math.floor(((checkDate-firstMon)/86400000)/7)+1
},parseDate:function(format,value,settings){if(format==null||value==null){throw"Invalid arguments"
}value=(typeof value=="object"?value.toString():value+"");
if(value==""){return null
}var shortYearCutoff=(settings?settings.shortYearCutoff:null)||this._defaults.shortYearCutoff;
var dayNamesShort=(settings?settings.dayNamesShort:null)||this._defaults.dayNamesShort;
var dayNames=(settings?settings.dayNames:null)||this._defaults.dayNames;
var monthNamesShort=(settings?settings.monthNamesShort:null)||this._defaults.monthNamesShort;
var monthNames=(settings?settings.monthNames:null)||this._defaults.monthNames;
var year=-1;
var month=-1;
var day=-1;
var doy=-1;
var literal=false;
var lookAhead=function(match){var matches=(iFormat+1<format.length&&format.charAt(iFormat+1)==match);
if(matches){iFormat++
}return matches
};
var getNumber=function(match){lookAhead(match);
var origSize=(match=="@"?14:(match=="y"?4:(match=="o"?3:2)));
var size=origSize;
var num=0;
while(size>0&&iValue<value.length&&value.charAt(iValue)>="0"&&value.charAt(iValue)<="9"){num=num*10+parseInt(value.charAt(iValue++),10);
size--
}if(size==origSize){throw"Missing number at position "+iValue
}return num
};
var getName=function(match,shortNames,longNames){var names=(lookAhead(match)?longNames:shortNames);
var size=0;
for(var j=0;
j<names.length;
j++){size=Math.max(size,names[j].length)
}var name="";
var iInit=iValue;
while(size>0&&iValue<value.length){name+=value.charAt(iValue++);
for(var i=0;
i<names.length;
i++){if(name==names[i]){return i+1
}}size--
}throw"Unknown name at position "+iInit
};
var checkLiteral=function(){if(value.charAt(iValue)!=format.charAt(iFormat)){throw"Unexpected literal at position "+iValue
}iValue++
};
var iValue=0;
for(var iFormat=0;
iFormat<format.length;
iFormat++){if(literal){if(format.charAt(iFormat)=="'"&&!lookAhead("'")){literal=false
}else{checkLiteral()
}}else{switch(format.charAt(iFormat)){case"d":day=getNumber("d");
break;
case"D":getName("D",dayNamesShort,dayNames);
break;
case"o":doy=getNumber("o");
break;
case"m":month=getNumber("m");
break;
case"M":month=getName("M",monthNamesShort,monthNames);
break;
case"y":year=getNumber("y");
break;
case"@":var date=new Date(getNumber("@"));
year=date.getFullYear();
month=date.getMonth()+1;
day=date.getDate();
break;
case"'":if(lookAhead("'")){checkLiteral()
}else{literal=true
}break;
default:checkLiteral()
}}}if(year==-1){year=new Date().getFullYear()
}else{if(year<100){year+=new Date().getFullYear()-new Date().getFullYear()%100+(year<=shortYearCutoff?0:-100)
}}if(doy>-1){month=1;
day=doy;
do{var dim=this._getDaysInMonth(year,month-1);
if(day<=dim){break
}month++;
day-=dim
}while(true)
}var date=this._daylightSavingAdjust(new Date(year,month-1,day));
if(date.getFullYear()!=year||date.getMonth()+1!=month||date.getDate()!=day){throw"Invalid date"
}return date
},ATOM:"yy-mm-dd",COOKIE:"D, dd M yy",ISO_8601:"yy-mm-dd",RFC_822:"D, d M y",RFC_850:"DD, dd-M-y",RFC_1036:"D, d M y",RFC_1123:"D, d M yy",RFC_2822:"D, d M yy",RSS:"D, d M y",TIMESTAMP:"@",W3C:"yy-mm-dd",formatDate:function(format,date,settings){if(!date){return""
}var dayNamesShort=(settings?settings.dayNamesShort:null)||this._defaults.dayNamesShort;
var dayNames=(settings?settings.dayNames:null)||this._defaults.dayNames;
var monthNamesShort=(settings?settings.monthNamesShort:null)||this._defaults.monthNamesShort;
var monthNames=(settings?settings.monthNames:null)||this._defaults.monthNames;
var lookAhead=function(match){var matches=(iFormat+1<format.length&&format.charAt(iFormat+1)==match);
if(matches){iFormat++
}return matches
};
var formatNumber=function(match,value,len){var num=""+value;
if(lookAhead(match)){while(num.length<len){num="0"+num
}}return num
};
var formatName=function(match,value,shortNames,longNames){return(lookAhead(match)?longNames[value]:shortNames[value])
};
var output="";
var literal=false;
if(date){for(var iFormat=0;
iFormat<format.length;
iFormat++){if(literal){if(format.charAt(iFormat)=="'"&&!lookAhead("'")){literal=false
}else{output+=format.charAt(iFormat)
}}else{switch(format.charAt(iFormat)){case"d":output+=formatNumber("d",date.getDate(),2);
break;
case"D":output+=formatName("D",date.getDay(),dayNamesShort,dayNames);
break;
case"o":var doy=date.getDate();
for(var m=date.getMonth()-1;
m>=0;
m--){doy+=this._getDaysInMonth(date.getFullYear(),m)
}output+=formatNumber("o",doy,3);
break;
case"m":output+=formatNumber("m",date.getMonth()+1,2);
break;
case"M":output+=formatName("M",date.getMonth(),monthNamesShort,monthNames);
break;
case"y":output+=(lookAhead("y")?date.getFullYear():(date.getYear()%100<10?"0":"")+date.getYear()%100);
break;
case"@":output+=date.getTime();
break;
case"'":if(lookAhead("'")){output+="'"
}else{literal=true
}break;
default:output+=format.charAt(iFormat)
}}}}return output
},_possibleChars:function(format){var chars="";
var literal=false;
for(var iFormat=0;
iFormat<format.length;
iFormat++){if(literal){if(format.charAt(iFormat)=="'"&&!lookAhead("'")){literal=false
}else{chars+=format.charAt(iFormat)
}}else{switch(format.charAt(iFormat)){case"d":case"m":case"y":case"@":chars+="0123456789";
break;
case"D":case"M":return null;
case"'":if(lookAhead("'")){chars+="'"
}else{literal=true
}break;
default:chars+=format.charAt(iFormat)
}}}return chars
},_get:function(inst,name){return inst.settings[name]!==undefined?inst.settings[name]:this._defaults[name]
},_setDateFromField:function(inst){var dateFormat=this._get(inst,"dateFormat");
var dates=inst.input?inst.input.val():null;
inst.endDay=inst.endMonth=inst.endYear=null;
var date=defaultDate=this._getDefaultDate(inst);
var settings=this._getFormatConfig(inst);
try{date=this.parseDate(dateFormat,dates,settings)||defaultDate
}catch(event){this.log(event);
date=defaultDate
}inst.selectedDay=date.getDate();
inst.drawMonth=inst.selectedMonth=date.getMonth();
inst.drawYear=inst.selectedYear=date.getFullYear();
inst.currentDay=(dates?date.getDate():0);
inst.currentMonth=(dates?date.getMonth():0);
inst.currentYear=(dates?date.getFullYear():0);
this._adjustInstDate(inst)
},_getDefaultDate:function(inst){var date=this._determineDate(this._get(inst,"defaultDate"),new Date());
var minDate=this._getMinMaxDate(inst,"min",true);
var maxDate=this._getMinMaxDate(inst,"max");
date=(minDate&&date<minDate?minDate:date);
date=(maxDate&&date>maxDate?maxDate:date);
return date
},_determineDate:function(date,defaultDate){var offsetNumeric=function(offset){var date=new Date();
date.setDate(date.getDate()+offset);
return date
};
var offsetString=function(offset,getDaysInMonth){var date=new Date();
var year=date.getFullYear();
var month=date.getMonth();
var day=date.getDate();
var pattern=/([+-]?[0-9]+)\s*(d|D|w|W|m|M|y|Y)?/g;
var matches=pattern.exec(offset);
while(matches){switch(matches[2]||"d"){case"d":case"D":day+=parseInt(matches[1],10);
break;
case"w":case"W":day+=parseInt(matches[1],10)*7;
break;
case"m":case"M":month+=parseInt(matches[1],10);
day=Math.min(day,getDaysInMonth(year,month));
break;
case"y":case"Y":year+=parseInt(matches[1],10);
day=Math.min(day,getDaysInMonth(year,month));
break
}matches=pattern.exec(offset)
}return new Date(year,month,day)
};
date=(date==null?defaultDate:(typeof date=="string"?offsetString(date,this._getDaysInMonth):(typeof date=="number"?(isNaN(date)?defaultDate:offsetNumeric(date)):date)));
date=(date&&date.toString()=="Invalid Date"?defaultDate:date);
if(date){date.setHours(0);
date.setMinutes(0);
date.setSeconds(0);
date.setMilliseconds(0)
}return this._daylightSavingAdjust(date)
},_daylightSavingAdjust:function(date){if(!date){return null
}date.setHours(date.getHours()>12?date.getHours()+2:0);
return date
},_setDate:function(inst,date,endDate){var clear=!(date);
var origMonth=inst.selectedMonth;
var origYear=inst.selectedYear;
date=this._determineDate(date,new Date());
inst.selectedDay=inst.currentDay=date.getDate();
inst.drawMonth=inst.selectedMonth=inst.currentMonth=date.getMonth();
inst.drawYear=inst.selectedYear=inst.currentYear=date.getFullYear();
if(origMonth!=inst.selectedMonth||origYear!=inst.selectedYear){this._notifyChange(inst)
}this._adjustInstDate(inst);
if(inst.input){inst.input.val(clear?"":this._formatDate(inst))
}},_getDate:function(inst){var startDate=(!inst.currentYear||(inst.input&&inst.input.val()=="")?null:this._daylightSavingAdjust(new Date(inst.currentYear,inst.currentMonth,inst.currentDay)));
return startDate
},_generateHTML:function(inst){var today=new Date();
today=this._daylightSavingAdjust(new Date(today.getFullYear(),today.getMonth(),today.getDate()));
var isRTL=this._get(inst,"isRTL");
var showButtonPanel=this._get(inst,"showButtonPanel");
var hideIfNoPrevNext=this._get(inst,"hideIfNoPrevNext");
var navigationAsDateFormat=this._get(inst,"navigationAsDateFormat");
var numMonths=this._getNumberOfMonths(inst);
var showCurrentAtPos=this._get(inst,"showCurrentAtPos");
var stepMonths=this._get(inst,"stepMonths");
var stepBigMonths=this._get(inst,"stepBigMonths");
var isMultiMonth=(numMonths[0]!=1||numMonths[1]!=1);
var currentDate=this._daylightSavingAdjust((!inst.currentDay?new Date(9999,9,9):new Date(inst.currentYear,inst.currentMonth,inst.currentDay)));
var minDate=this._getMinMaxDate(inst,"min",true);
var maxDate=this._getMinMaxDate(inst,"max");
var drawMonth=inst.drawMonth-showCurrentAtPos;
var drawYear=inst.drawYear;
if(drawMonth<0){drawMonth+=12;
drawYear--
}if(maxDate){var maxDraw=this._daylightSavingAdjust(new Date(maxDate.getFullYear(),maxDate.getMonth()-numMonths[1]+1,maxDate.getDate()));
maxDraw=(minDate&&maxDraw<minDate?minDate:maxDraw);
while(this._daylightSavingAdjust(new Date(drawYear,drawMonth,1))>maxDraw){drawMonth--;
if(drawMonth<0){drawMonth=11;
drawYear--
}}}inst.drawMonth=drawMonth;
inst.drawYear=drawYear;
var prevText=this._get(inst,"prevText");
prevText=(!navigationAsDateFormat?prevText:this.formatDate(prevText,this._daylightSavingAdjust(new Date(drawYear,drawMonth-stepMonths,1)),this._getFormatConfig(inst)));
var prev=(this._canAdjustMonth(inst,-1,drawYear,drawMonth)?'<a class="ui-datepicker-prev ui-corner-all" onclick="DP_jQuery.datepicker._adjustDate(\'#'+inst.id+"', -"+stepMonths+", 'M');\" title=\""+prevText+'"><span class="ui-icon ui-icon-circle-triangle-'+(isRTL?"e":"w")+'">'+prevText+"</span></a>":(hideIfNoPrevNext?"":'<a class="ui-datepicker-prev ui-corner-all ui-state-disabled" title="'+prevText+'"><span class="ui-icon ui-icon-circle-triangle-'+(isRTL?"e":"w")+'">'+prevText+"</span></a>"));
var nextText=this._get(inst,"nextText");
nextText=(!navigationAsDateFormat?nextText:this.formatDate(nextText,this._daylightSavingAdjust(new Date(drawYear,drawMonth+stepMonths,1)),this._getFormatConfig(inst)));
var next=(this._canAdjustMonth(inst,+1,drawYear,drawMonth)?'<a class="ui-datepicker-next ui-corner-all" onclick="DP_jQuery.datepicker._adjustDate(\'#'+inst.id+"', +"+stepMonths+", 'M');\" title=\""+nextText+'"><span class="ui-icon ui-icon-circle-triangle-'+(isRTL?"w":"e")+'">'+nextText+"</span></a>":(hideIfNoPrevNext?"":'<a class="ui-datepicker-next ui-corner-all ui-state-disabled" title="'+nextText+'"><span class="ui-icon ui-icon-circle-triangle-'+(isRTL?"w":"e")+'">'+nextText+"</span></a>"));
var currentText=this._get(inst,"currentText");
var gotoDate=(this._get(inst,"gotoCurrent")&&inst.currentDay?currentDate:today);
currentText=(!navigationAsDateFormat?currentText:this.formatDate(currentText,gotoDate,this._getFormatConfig(inst)));
var controls=(!inst.inline?'<button type="button" class="ui-datepicker-close ui-state-default ui-priority-primary ui-corner-all" onclick="DP_jQuery.datepicker._hideDatepicker();">'+this._get(inst,"closeText")+"</button>":"");
var buttonPanel=(showButtonPanel)?'<div class="ui-datepicker-buttonpane ui-widget-content">'+(isRTL?controls:"")+(this._isInRange(inst,gotoDate)?'<button type="button" class="ui-datepicker-current ui-state-default ui-priority-secondary ui-corner-all" onclick="DP_jQuery.datepicker._gotoToday(\'#'+inst.id+"');\">"+currentText+"</button>":"")+(isRTL?"":controls)+"</div>":"";
var firstDay=parseInt(this._get(inst,"firstDay"),10);
firstDay=(isNaN(firstDay)?0:firstDay);
var dayNames=this._get(inst,"dayNames");
var dayNamesShort=this._get(inst,"dayNamesShort");
var dayNamesMin=this._get(inst,"dayNamesMin");
var monthNames=this._get(inst,"monthNames");
var monthNamesShort=this._get(inst,"monthNamesShort");
var beforeShowDay=this._get(inst,"beforeShowDay");
var showOtherMonths=this._get(inst,"showOtherMonths");
var calculateWeek=this._get(inst,"calculateWeek")||this.iso8601Week;
var endDate=inst.endDay?this._daylightSavingAdjust(new Date(inst.endYear,inst.endMonth,inst.endDay)):currentDate;
var defaultDate=this._getDefaultDate(inst);
var html="";
for(var row=0;
row<numMonths[0];
row++){var group="";
for(var col=0;
col<numMonths[1];
col++){var selectedDate=this._daylightSavingAdjust(new Date(drawYear,drawMonth,inst.selectedDay));
var cornerClass=" ui-corner-all";
var calender="";
if(isMultiMonth){calender+='<div class="ui-datepicker-group ui-datepicker-group-';
switch(col){case 0:calender+="first";
cornerClass=" ui-corner-"+(isRTL?"right":"left");
break;
case numMonths[1]-1:calender+="last";
cornerClass=" ui-corner-"+(isRTL?"left":"right");
break;
default:calender+="middle";
cornerClass="";
break
}calender+='">'
}calender+='<div class="ui-datepicker-header ui-widget-header ui-helper-clearfix'+cornerClass+'">'+(/all|left/.test(cornerClass)&&row==0?(isRTL?next:prev):"")+(/all|right/.test(cornerClass)&&row==0?(isRTL?prev:next):"")+this._generateMonthYearHeader(inst,drawMonth,drawYear,minDate,maxDate,selectedDate,row>0||col>0,monthNames,monthNamesShort)+'</div><table class="ui-datepicker-calendar"><thead><tr>';
var thead="";
for(var dow=0;
dow<7;
dow++){var day=(dow+firstDay)%7;
thead+="<th"+((dow+firstDay+6)%7>=5?' class="ui-datepicker-week-end"':"")+'><span title="'+dayNames[day]+'">'+dayNamesMin[day]+"</span></th>"
}calender+=thead+"</tr></thead><tbody>";
var daysInMonth=this._getDaysInMonth(drawYear,drawMonth);
if(drawYear==inst.selectedYear&&drawMonth==inst.selectedMonth){inst.selectedDay=Math.min(inst.selectedDay,daysInMonth)
}var leadDays=(this._getFirstDayOfMonth(drawYear,drawMonth)-firstDay+7)%7;
var numRows=(isMultiMonth?6:Math.ceil((leadDays+daysInMonth)/7));
var printDate=this._daylightSavingAdjust(new Date(drawYear,drawMonth,1-leadDays));
for(var dRow=0;
dRow<numRows;
dRow++){calender+="<tr>";
var tbody="";
for(var dow=0;
dow<7;
dow++){var daySettings=(beforeShowDay?beforeShowDay.apply((inst.input?inst.input[0]:null),[printDate]):[true,""]);
var otherMonth=(printDate.getMonth()!=drawMonth);
var unselectable=otherMonth||!daySettings[0]||(minDate&&printDate<minDate)||(maxDate&&printDate>maxDate);
tbody+='<td class="'+((dow+firstDay+6)%7>=5?" ui-datepicker-week-end":"")+(otherMonth?" ui-datepicker-other-month":"")+((printDate.getTime()==selectedDate.getTime()&&drawMonth==inst.selectedMonth&&inst._keyEvent)||(defaultDate.getTime()==printDate.getTime()&&defaultDate.getTime()==selectedDate.getTime())?" "+this._dayOverClass:"")+(unselectable?" "+this._unselectableClass+" ui-state-disabled":"")+(otherMonth&&!showOtherMonths?"":" "+daySettings[1]+(printDate.getTime()>=currentDate.getTime()&&printDate.getTime()<=endDate.getTime()?" "+this._currentClass:"")+(printDate.getTime()==today.getTime()?" ui-datepicker-today":""))+'"'+((!otherMonth||showOtherMonths)&&daySettings[2]?' title="'+daySettings[2]+'"':"")+(unselectable?"":" onclick=\"DP_jQuery.datepicker._selectDay('#"+inst.id+"',"+drawMonth+","+drawYear+', this);return false;"')+">"+(otherMonth?(showOtherMonths?printDate.getDate():"&#xa0;"):(unselectable?'<span class="ui-state-default">'+printDate.getDate()+"</span>":'<a class="ui-state-default'+(printDate.getTime()==today.getTime()?" ui-state-highlight":"")+(printDate.getTime()>=currentDate.getTime()&&printDate.getTime()<=endDate.getTime()?" ui-state-active":"")+'" href="#">'+printDate.getDate()+"</a>"))+"</td>";
printDate.setDate(printDate.getDate()+1);
printDate=this._daylightSavingAdjust(printDate)
}calender+=tbody+"</tr>"
}drawMonth++;
if(drawMonth>11){drawMonth=0;
drawYear++
}calender+="</tbody></table>"+(isMultiMonth?"</div>"+((numMonths[0]>0&&col==numMonths[1]-1)?'<div class="ui-datepicker-row-break"></div>':""):"");
group+=calender
}html+=group
}html+=buttonPanel+($.browser.msie&&parseInt($.browser.version,10)<7&&!inst.inline?'<iframe src="javascript:false;" class="ui-datepicker-cover" frameborder="0"></iframe>':"");
inst._keyEvent=false;
return html
},_generateMonthYearHeader:function(inst,drawMonth,drawYear,minDate,maxDate,selectedDate,secondary,monthNames,monthNamesShort){minDate=(inst.rangeStart&&minDate&&selectedDate<minDate?selectedDate:minDate);
var changeMonth=this._get(inst,"changeMonth");
var changeYear=this._get(inst,"changeYear");
var showMonthAfterYear=this._get(inst,"showMonthAfterYear");
var html='<div class="ui-datepicker-title">';
var monthHtml="";
if(secondary||!changeMonth){monthHtml+='<span class="ui-datepicker-month">'+monthNames[drawMonth]+"</span> "
}else{var inMinYear=(minDate&&minDate.getFullYear()==drawYear);
var inMaxYear=(maxDate&&maxDate.getFullYear()==drawYear);
monthHtml+='<select class="ui-datepicker-month" onchange="DP_jQuery.datepicker._selectMonthYear(\'#'+inst.id+"', this, 'M');\" onclick=\"DP_jQuery.datepicker._clickMonthYear('#"+inst.id+"');\">";
for(var month=0;
month<12;
month++){if((!inMinYear||month>=minDate.getMonth())&&(!inMaxYear||month<=maxDate.getMonth())){monthHtml+='<option value="'+month+'"'+(month==drawMonth?' selected="selected"':"")+">"+monthNamesShort[month]+"</option>"
}}monthHtml+="</select>"
}if(!showMonthAfterYear){html+=monthHtml+((secondary||changeMonth||changeYear)&&(!(changeMonth&&changeYear))?"&#xa0;":"")
}if(secondary||!changeYear){html+='<span class="ui-datepicker-year">'+drawYear+"</span>"
}else{var years=this._get(inst,"yearRange").split(":");
var year=0;
var endYear=0;
if(years.length!=2){year=drawYear-10;
endYear=drawYear+10
}else{if(years[0].charAt(0)=="+"||years[0].charAt(0)=="-"){year=drawYear+parseInt(years[0],10);
endYear=drawYear+parseInt(years[1],10)
}else{year=parseInt(years[0],10);
endYear=parseInt(years[1],10)
}}year=(minDate?Math.max(year,minDate.getFullYear()):year);
endYear=(maxDate?Math.min(endYear,maxDate.getFullYear()):endYear);
html+='<select class="ui-datepicker-year" onchange="DP_jQuery.datepicker._selectMonthYear(\'#'+inst.id+"', this, 'Y');\" onclick=\"DP_jQuery.datepicker._clickMonthYear('#"+inst.id+"');\">";
for(;
year<=endYear;
year++){html+='<option value="'+year+'"'+(year==drawYear?' selected="selected"':"")+">"+year+"</option>"
}html+="</select>"
}if(showMonthAfterYear){html+=(secondary||changeMonth||changeYear?"&#xa0;":"")+monthHtml
}html+="</div>";
return html
},_adjustInstDate:function(inst,offset,period){var year=inst.drawYear+(period=="Y"?offset:0);
var month=inst.drawMonth+(period=="M"?offset:0);
var day=Math.min(inst.selectedDay,this._getDaysInMonth(year,month))+(period=="D"?offset:0);
var date=this._daylightSavingAdjust(new Date(year,month,day));
var minDate=this._getMinMaxDate(inst,"min",true);
var maxDate=this._getMinMaxDate(inst,"max");
date=(minDate&&date<minDate?minDate:date);
date=(maxDate&&date>maxDate?maxDate:date);
inst.selectedDay=date.getDate();
inst.drawMonth=inst.selectedMonth=date.getMonth();
inst.drawYear=inst.selectedYear=date.getFullYear();
if(period=="M"||period=="Y"){this._notifyChange(inst)
}},_notifyChange:function(inst){var onChange=this._get(inst,"onChangeMonthYear");
if(onChange){onChange.apply((inst.input?inst.input[0]:null),[inst.selectedYear,inst.selectedMonth+1,inst])
}},_getNumberOfMonths:function(inst){var numMonths=this._get(inst,"numberOfMonths");
return(numMonths==null?[1,1]:(typeof numMonths=="number"?[1,numMonths]:numMonths))
},_getMinMaxDate:function(inst,minMax,checkRange){var date=this._determineDate(this._get(inst,minMax+"Date"),null);
return(!checkRange||!inst.rangeStart?date:(!date||inst.rangeStart>date?inst.rangeStart:date))
},_getDaysInMonth:function(year,month){return 32-new Date(year,month,32).getDate()
},_getFirstDayOfMonth:function(year,month){return new Date(year,month,1).getDay()
},_canAdjustMonth:function(inst,offset,curYear,curMonth){var numMonths=this._getNumberOfMonths(inst);
var date=this._daylightSavingAdjust(new Date(curYear,curMonth+(offset<0?offset:numMonths[1]),1));
if(offset<0){date.setDate(this._getDaysInMonth(date.getFullYear(),date.getMonth()))
}return this._isInRange(inst,date)
},_isInRange:function(inst,date){var newMinDate=(!inst.rangeStart?null:this._daylightSavingAdjust(new Date(inst.selectedYear,inst.selectedMonth,inst.selectedDay)));
newMinDate=(newMinDate&&inst.rangeStart<newMinDate?inst.rangeStart:newMinDate);
var minDate=newMinDate||this._getMinMaxDate(inst,"min");
var maxDate=this._getMinMaxDate(inst,"max");
return((!minDate||date>=minDate)&&(!maxDate||date<=maxDate))
},_getFormatConfig:function(inst){var shortYearCutoff=this._get(inst,"shortYearCutoff");
shortYearCutoff=(typeof shortYearCutoff!="string"?shortYearCutoff:new Date().getFullYear()%100+parseInt(shortYearCutoff,10));
return{shortYearCutoff:shortYearCutoff,dayNamesShort:this._get(inst,"dayNamesShort"),dayNames:this._get(inst,"dayNames"),monthNamesShort:this._get(inst,"monthNamesShort"),monthNames:this._get(inst,"monthNames")}
},_formatDate:function(inst,day,month,year){if(!day){inst.currentDay=inst.selectedDay;
inst.currentMonth=inst.selectedMonth;
inst.currentYear=inst.selectedYear
}var date=(day?(typeof day=="object"?day:this._daylightSavingAdjust(new Date(year,month,day))):this._daylightSavingAdjust(new Date(inst.currentYear,inst.currentMonth,inst.currentDay)));
return this.formatDate(this._get(inst,"dateFormat"),date,this._getFormatConfig(inst))
}});
function extendRemove(target,props){$.extend(target,props);
for(var name in props){if(props[name]==null||props[name]==undefined){target[name]=props[name]
}}return target
}function isArray(a){return(a&&(($.browser.safari&&typeof a=="object"&&a.length)||(a.constructor&&a.constructor.toString().match(/\Array\(\)/))))
}$.fn.datepicker=function(options){if(!$.datepicker.initialized){$(document).mousedown($.datepicker._checkExternalClick).find("body").append($.datepicker.dpDiv);
$.datepicker.initialized=true
}var otherArgs=Array.prototype.slice.call(arguments,1);
if(typeof options=="string"&&(options=="isDisabled"||options=="getDate")){return $.datepicker["_"+options+"Datepicker"].apply($.datepicker,[this[0]].concat(otherArgs))
}if(options=="option"&&arguments.length==2&&typeof arguments[1]=="string"){return $.datepicker["_"+options+"Datepicker"].apply($.datepicker,[this[0]].concat(otherArgs))
}return this.each(function(){typeof options=="string"?$.datepicker["_"+options+"Datepicker"].apply($.datepicker,[this].concat(otherArgs)):$.datepicker._attachDatepicker(this,options)
})
};
$.datepicker=new Datepicker();
$.datepicker.initialized=false;
$.datepicker.uuid=new Date().getTime();
$.datepicker.version="1.7.2";
window.DP_jQuery=$
})(jQuery);
(function(f){var d={dragStart:"start.draggable",drag:"drag.draggable",dragStop:"stop.draggable",maxHeight:"maxHeight.resizable",minHeight:"minHeight.resizable",maxWidth:"maxWidth.resizable",minWidth:"minWidth.resizable",resizeStart:"start.resizable",resize:"drag.resizable",resizeStop:"stop.resizable"},e="ui-dialog ui-widget ui-widget-content ui-corner-all ";
f.widget("ui.dialog",{_init:function(){this.originalTitle=this.element.attr("title");
var b=this,a=this.options,n=a.title||this.originalTitle||"&nbsp;",s=f.ui.dialog.getTitleId(this.element),c=(this.uiDialog=f("<div/>")).appendTo(document.body).hide().addClass(e+a.dialogClass).css({position:"absolute",overflow:"hidden",zIndex:a.zIndex}).attr("tabIndex",-1).css("outline",0).keydown(function(g){(a.closeOnEscape&&g.keyCode&&g.keyCode==f.ui.keyCode.ESCAPE&&b.close(g))
}).attr({role:"dialog","aria-labelledby":s}).mousedown(function(g){b.moveToTop(false,g)
}),q=this.element.show().removeAttr("title").addClass("ui-dialog-content ui-widget-content").appendTo(c),r=(this.uiDialogTitlebar=f("<div></div>")).addClass("ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix").prependTo(c),o=f('<a href="#"/>').addClass("ui-dialog-titlebar-close ui-corner-all").attr("role","button").hover(function(){o.addClass("ui-state-hover")
},function(){o.removeClass("ui-state-hover")
}).focus(function(){o.addClass("ui-state-focus")
}).blur(function(){o.removeClass("ui-state-focus")
}).mousedown(function(g){g.stopPropagation()
}).click(function(g){b.close(g);
return false
}).appendTo(r),p=(this.uiDialogTitlebarCloseText=f("<span/>")).addClass("ui-icon ui-icon-closethick").text(a.closeText).appendTo(o),u=f("<span/>").addClass("ui-dialog-title").attr("id",s).html(n).prependTo(r);
r.find("*").add(r).disableSelection();
(a.draggable&&f.fn.draggable&&this._makeDraggable());
(a.resizable&&f.fn.resizable&&this._makeResizable());
this._createButtons(a.buttons);
this._isOpen=false;
(a.bgiframe&&f.fn.bgiframe&&c.bgiframe());
(a.autoOpen&&this.open())
},destroy:function(){(this.overlay&&this.overlay.destroy());
this.uiDialog.hide();
this.element.unbind(".dialog").removeData("dialog").removeClass("ui-dialog-content ui-widget-content").hide().appendTo("body");
this.uiDialog.remove();
(this.originalTitle&&this.element.attr("title",this.originalTitle))
},close:function(a){var c=this;
if(false===c._trigger("beforeclose",a)){return
}(c.overlay&&c.overlay.destroy());
c.uiDialog.unbind("keypress.ui-dialog");
(c.options.hide?c.uiDialog.hide(c.options.hide,function(){c._trigger("close",a)
}):c.uiDialog.hide()&&c._trigger("close",a));
f.ui.dialog.overlay.resize();
c._isOpen=false;
if(c.options.modal){var b=0;
f(".ui-dialog").each(function(){if(this!=c.uiDialog[0]){b=Math.max(b,f(this).css("z-index"))
}});
f.ui.dialog.maxZ=b
}},isOpen:function(){return this._isOpen
},moveToTop:function(a,b){if((this.options.modal&&!a)||(!this.options.stack&&!this.options.modal)){return this._trigger("focus",b)
}if(this.options.zIndex>f.ui.dialog.maxZ){f.ui.dialog.maxZ=this.options.zIndex
}(this.overlay&&this.overlay.$el.css("z-index",f.ui.dialog.overlay.maxZ=++f.ui.dialog.maxZ));
var c={scrollTop:this.element.attr("scrollTop"),scrollLeft:this.element.attr("scrollLeft")};
this.uiDialog.css("z-index",++f.ui.dialog.maxZ);
this.element.attr(c);
this._trigger("focus",b)
},open:function(){if(this._isOpen){return
}var a=this.options,b=this.uiDialog;
this.overlay=a.modal?new f.ui.dialog.overlay(this):null;
(b.next().length&&b.appendTo("body"));
this._size();
this._position(a.position);
b.show(a.show);
this.moveToTop(true);
(a.modal&&b.bind("keypress.ui-dialog",function(j){if(j.keyCode!=f.ui.keyCode.TAB){return
}var k=f(":tabbable",this),c=k.filter(":first")[0],l=k.filter(":last")[0];
if(j.target==l&&!j.shiftKey){setTimeout(function(){c.focus()
},1)
}else{if(j.target==c&&j.shiftKey){setTimeout(function(){l.focus()
},1)
}}}));
f([]).add(b.find(".ui-dialog-content :tabbable:first")).add(b.find(".ui-dialog-buttonpane :tabbable:first")).add(b).filter(":first").focus();
this._trigger("open");
this._isOpen=true
},_createButtons:function(a){var b=this,h=false,c=f("<div></div>").addClass("ui-dialog-buttonpane ui-widget-content ui-helper-clearfix");
this.uiDialog.find(".ui-dialog-buttonpane").remove();
(typeof a=="object"&&a!==null&&f.each(a,function(){return !(h=true)
}));
if(h){f.each(a,function(j,g){f('<button type="button"></button>').addClass("ui-state-default ui-corner-all").text(j).click(function(){g.apply(b.element[0],arguments)
}).hover(function(){f(this).addClass("ui-state-hover")
},function(){f(this).removeClass("ui-state-hover")
}).focus(function(){f(this).addClass("ui-state-focus")
}).blur(function(){f(this).removeClass("ui-state-focus")
}).appendTo(c)
});
c.appendTo(this.uiDialog)
}},_makeDraggable:function(){var c=this,a=this.options,b;
this.uiDialog.draggable({cancel:".ui-dialog-content",handle:".ui-dialog-titlebar",containment:"document",start:function(){b=a.height;
f(this).height(f(this).height()).addClass("ui-dialog-dragging");
(a.dragStart&&a.dragStart.apply(c.element[0],arguments))
},drag:function(){(a.drag&&a.drag.apply(c.element[0],arguments))
},stop:function(){f(this).removeClass("ui-dialog-dragging").height(b);
(a.dragStop&&a.dragStop.apply(c.element[0],arguments));
f.ui.dialog.overlay.resize()
}})
},_makeResizable:function(a){a=(a===undefined?this.options.resizable:a);
var h=this,b=this.options,c=typeof a=="string"?a:"n,e,s,w,se,sw,ne,nw";
this.uiDialog.resizable({cancel:".ui-dialog-content",alsoResize:this.element,maxWidth:b.maxWidth,maxHeight:b.maxHeight,minWidth:b.minWidth,minHeight:b.minHeight,start:function(){f(this).addClass("ui-dialog-resizing");
(b.resizeStart&&b.resizeStart.apply(h.element[0],arguments))
},resize:function(){(b.resize&&b.resize.apply(h.element[0],arguments))
},handles:c,stop:function(){f(this).removeClass("ui-dialog-resizing");
b.height=f(this).height();
b.width=f(this).width();
(b.resizeStop&&b.resizeStop.apply(h.element[0],arguments));
f.ui.dialog.overlay.resize()
}}).find(".ui-resizable-se").addClass("ui-icon ui-icon-grip-diagonal-se")
},_position:function(a){var k=f(window),j=f(document),c=j.scrollTop(),l=j.scrollLeft(),b=c;
if(f.inArray(a,["center","top","right","bottom","left"])>=0){a=[a=="right"||a=="left"?a:"center",a=="top"||a=="bottom"?a:"middle"]
}if(a.constructor!=Array){a=["center","middle"]
}if(a[0].constructor==Number){l+=a[0]
}else{switch(a[0]){case"left":l+=0;
break;
case"right":l+=k.width()-this.uiDialog.outerWidth();
break;
default:case"center":l+=(k.width()-this.uiDialog.outerWidth())/2
}}if(a[1].constructor==Number){c+=a[1]
}else{switch(a[1]){case"top":c+=0;
break;
case"bottom":c+=k.height()-this.uiDialog.outerHeight();
break;
default:case"middle":c+=(k.height()-this.uiDialog.outerHeight())/2
}}c=Math.max(c,b);
this.uiDialog.css({top:c,left:l})
},_setData:function(c,b){(d[c]&&this.uiDialog.data(d[c],b));
switch(c){case"buttons":this._createButtons(b);
break;
case"closeText":this.uiDialogTitlebarCloseText.text(b);
break;
case"dialogClass":this.uiDialog.removeClass(this.options.dialogClass).addClass(e+b);
break;
case"draggable":(b?this._makeDraggable():this.uiDialog.draggable("destroy"));
break;
case"height":this.uiDialog.height(b);
break;
case"position":this._position(b);
break;
case"resizable":var h=this.uiDialog,a=this.uiDialog.is(":data(resizable)");
(a&&!b&&h.resizable("destroy"));
(a&&typeof b=="string"&&h.resizable("option","handles",b));
(a||this._makeResizable(b));
break;
case"title":f(".ui-dialog-title",this.uiDialogTitlebar).html(b||"&nbsp;");
break;
case"width":this.uiDialog.width(b);
break
}f.widget.prototype._setData.apply(this,arguments)
},_size:function(){var a=this.options;
this.element.css({height:0,minHeight:0,width:"auto"});
var b=this.uiDialog.css({height:"auto",width:a.width}).height();
this.element.css({minHeight:Math.max(a.minHeight-b,0),height:a.height=="auto"?"auto":Math.max(a.height-b,0)})
}});
f.extend(f.ui.dialog,{version:"1.7.2",defaults:{autoOpen:true,bgiframe:false,buttons:{},closeOnEscape:true,closeText:"close",dialogClass:"",draggable:true,hide:null,height:"auto",maxHeight:false,maxWidth:false,minHeight:150,minWidth:150,modal:false,position:"center",resizable:true,show:null,stack:true,title:"",width:300,zIndex:1000},getter:"isOpen",uuid:0,maxZ:0,getTitleId:function(a){return"ui-dialog-title-"+(a.attr("id")||++this.uuid)
},overlay:function(a){this.$el=f.ui.dialog.overlay.create(a)
}});
f.extend(f.ui.dialog.overlay,{instances:[],maxZ:0,events:f.map("focus,mousedown,mouseup,keydown,keypress,click".split(","),function(a){return a+".dialog-overlay"
}).join(" "),create:function(a){if(this.instances.length===0){setTimeout(function(){if(f.ui.dialog.overlay.instances.length){f(document).bind(f.ui.dialog.overlay.events,function(h){var c=f(h.target).parents(".ui-dialog").css("zIndex")||0;
return(c>f.ui.dialog.overlay.maxZ)
})
}},1);
f(document).bind("keydown.dialog-overlay",function(c){(a.options.closeOnEscape&&c.keyCode&&c.keyCode==f.ui.keyCode.ESCAPE&&a.close(c))
});
f(window).bind("resize.dialog-overlay",f.ui.dialog.overlay.resize)
}var b=f("<div></div>").appendTo(document.body).addClass("ui-widget-overlay").css({width:this.width(),height:this.height()});
(a.options.bgiframe&&f.fn.bgiframe&&b.bgiframe());
this.instances.push(b);
return b
},destroy:function(b){this.instances.splice(f.inArray(this.instances,b),1);
if(this.instances.length===0){f([document,window]).unbind(".dialog-overlay")
}b.remove();
var a=0;
f.each(this.instances,function(){a=Math.max(a,this.css("z-index"))
});
this.maxZ=a
},height:function(){if(f.browser.msie&&f.browser.version<7){var a=Math.max(document.documentElement.scrollHeight,document.body.scrollHeight);
var b=Math.max(document.documentElement.offsetHeight,document.body.offsetHeight);
if(a<b){return f(window).height()+"px"
}else{return a+"px"
}}else{return f(document).height()+"px"
}},width:function(){if(f.browser.msie&&f.browser.version<7){var b=Math.max(document.documentElement.scrollWidth,document.body.scrollWidth);
var a=Math.max(document.documentElement.offsetWidth,document.body.offsetWidth);
if(b<a){return f(window).width()+"px"
}else{return b+"px"
}}else{return f(document).width()+"px"
}},resize:function(){var a=f([]);
f.each(f.ui.dialog.overlay.instances,function(){a=a.add(this)
});
a.css({width:0,height:0}).css({width:f.ui.dialog.overlay.width(),height:f.ui.dialog.overlay.height()})
}});
f.extend(f.ui.dialog.overlay.prototype,{destroy:function(){f.ui.dialog.overlay.destroy(this.$el)
}})
})(jQuery);
(function(b){b.widget("ui.progressbar",{_init:function(){this.element.addClass("ui-progressbar ui-widget ui-widget-content ui-corner-all").attr({role:"progressbar","aria-valuemin":this._valueMin(),"aria-valuemax":this._valueMax(),"aria-valuenow":this._value()});
this.valueDiv=b('<div class="ui-progressbar-value ui-widget-header ui-corner-left"></div>').appendTo(this.element);
this._refreshValue()
},destroy:function(){this.element.removeClass("ui-progressbar ui-widget ui-widget-content ui-corner-all").removeAttr("role").removeAttr("aria-valuemin").removeAttr("aria-valuemax").removeAttr("aria-valuenow").removeData("progressbar").unbind(".progressbar");
this.valueDiv.remove();
b.widget.prototype.destroy.apply(this,arguments)
},value:function(a){if(a===undefined){return this._value()
}this._setData("value",a);
return this
},_setData:function(a,d){switch(a){case"value":this.options.value=d;
this._refreshValue();
this._trigger("change",null,{});
break
}b.widget.prototype._setData.apply(this,arguments)
},_value:function(){var a=this.options.value;
if(a<this._valueMin()){a=this._valueMin()
}if(a>this._valueMax()){a=this._valueMax()
}return a
},_valueMin:function(){var a=0;
return a
},_valueMax:function(){var a=100;
return a
},_refreshValue:function(){var a=this.value();
this.valueDiv[a==this._valueMax()?"addClass":"removeClass"]("ui-corner-right");
this.valueDiv.width(a+"%");
this.element.attr("aria-valuenow",a)
}});
b.extend(b.ui.progressbar,{version:"1.7.2",defaults:{value:0}})
})(jQuery);
(function(b){b.widget("ui.slider",b.extend({},b.ui.mouse,{_init:function(){var a=this,d=this.options;
this._keySliding=false;
this._handleIndex=null;
this._detectOrientation();
this._mouseInit();
this.element.addClass("ui-slider ui-slider-"+this.orientation+" ui-widget ui-widget-content ui-corner-all");
this.range=b([]);
if(d.range){if(d.range===true){this.range=b("<div></div>");
if(!d.values){d.values=[this._valueMin(),this._valueMin()]
}if(d.values.length&&d.values.length!=2){d.values=[d.values[0],d.values[0]]
}}else{this.range=b("<div></div>")
}this.range.appendTo(this.element).addClass("ui-slider-range");
if(d.range=="min"||d.range=="max"){this.range.addClass("ui-slider-range-"+d.range)
}this.range.addClass("ui-widget-header")
}if(b(".ui-slider-handle",this.element).length==0){b('<a href="#"></a>').appendTo(this.element).addClass("ui-slider-handle")
}if(d.values&&d.values.length){while(b(".ui-slider-handle",this.element).length<d.values.length){b('<a href="#"></a>').appendTo(this.element).addClass("ui-slider-handle")
}}this.handles=b(".ui-slider-handle",this.element).addClass("ui-state-default ui-corner-all");
this.handle=this.handles.eq(0);
this.handles.add(this.range).filter("a").click(function(c){c.preventDefault()
}).hover(function(){if(!d.disabled){b(this).addClass("ui-state-hover")
}},function(){b(this).removeClass("ui-state-hover")
}).focus(function(){if(!d.disabled){b(".ui-slider .ui-state-focus").removeClass("ui-state-focus");
b(this).addClass("ui-state-focus")
}else{b(this).blur()
}}).blur(function(){b(this).removeClass("ui-state-focus")
});
this.handles.each(function(c){b(this).data("index.ui-slider-handle",c)
});
this.handles.keydown(function(c){var l=true;
var m=b(this).data("index.ui-slider-handle");
if(a.options.disabled){return
}switch(c.keyCode){case b.ui.keyCode.HOME:case b.ui.keyCode.END:case b.ui.keyCode.UP:case b.ui.keyCode.RIGHT:case b.ui.keyCode.DOWN:case b.ui.keyCode.LEFT:l=false;
if(!a._keySliding){a._keySliding=true;
b(this).addClass("ui-state-active");
a._start(c,m)
}break
}var k,n,j=a._step();
if(a.options.values&&a.options.values.length){k=n=a.values(m)
}else{k=n=a.value()
}switch(c.keyCode){case b.ui.keyCode.HOME:n=a._valueMin();
break;
case b.ui.keyCode.END:n=a._valueMax();
break;
case b.ui.keyCode.UP:case b.ui.keyCode.RIGHT:if(k==a._valueMax()){return
}n=k+j;
break;
case b.ui.keyCode.DOWN:case b.ui.keyCode.LEFT:if(k==a._valueMin()){return
}n=k-j;
break
}a._slide(c,m,n);
return l
}).keyup(function(c){var f=b(this).data("index.ui-slider-handle");
if(a._keySliding){a._stop(c,f);
a._change(c,f);
a._keySliding=false;
b(this).removeClass("ui-state-active")
}});
this._refreshValue()
},destroy:function(){this.handles.remove();
this.range.remove();
this.element.removeClass("ui-slider ui-slider-horizontal ui-slider-vertical ui-slider-disabled ui-widget ui-widget-content ui-corner-all").removeData("slider").unbind(".slider");
this._mouseDestroy()
},_mouseCapture:function(r){var q=this.options;
if(q.disabled){return false
}this.elementSize={width:this.element.outerWidth(),height:this.element.outerHeight()};
this.elementOffset=this.element.offset();
var n={x:r.pageX,y:r.pageY};
var l=this._normValueFromMouse(n);
var s=this._valueMax()-this._valueMin()+1,p;
var a=this,m;
this.handles.each(function(d){var c=Math.abs(l-a.values(d));
if(s>c){s=c;
p=b(this);
m=d
}});
if(q.range==true&&this.values(1)==q.min){p=b(this.handles[++m])
}this._start(r,m);
a._handleIndex=m;
p.addClass("ui-state-active").focus();
var o=p.offset();
var u=!b(r.target).parents().andSelf().is(".ui-slider-handle");
this._clickOffset=u?{left:0,top:0}:{left:r.pageX-o.left-(p.width()/2),top:r.pageY-o.top-(p.height()/2)-(parseInt(p.css("borderTopWidth"),10)||0)-(parseInt(p.css("borderBottomWidth"),10)||0)+(parseInt(p.css("marginTop"),10)||0)};
l=this._normValueFromMouse(n);
this._slide(r,m,l);
return true
},_mouseStart:function(a){return true
},_mouseDrag:function(e){var a={x:e.pageX,y:e.pageY};
var f=this._normValueFromMouse(a);
this._slide(e,this._handleIndex,f);
return false
},_mouseStop:function(a){this.handles.removeClass("ui-state-active");
this._stop(a,this._handleIndex);
this._change(a,this._handleIndex);
this._handleIndex=null;
this._clickOffset=null;
return false
},_detectOrientation:function(){this.orientation=this.options.orientation=="vertical"?"vertical":"horizontal"
},_normValueFromMouse:function(o){var p,k;
if("horizontal"==this.orientation){p=this.elementSize.width;
k=o.x-this.elementOffset.left-(this._clickOffset?this._clickOffset.left:0)
}else{p=this.elementSize.height;
k=o.y-this.elementOffset.top-(this._clickOffset?this._clickOffset.top:0)
}var m=(k/p);
if(m>1){m=1
}if(m<0){m=0
}if("vertical"==this.orientation){m=1-m
}var n=this._valueMax()-this._valueMin(),j=m*n,a=j%this.options.step,l=this._valueMin()+j-a;
if(a>(this.options.step/2)){l+=this.options.step
}return parseFloat(l.toFixed(5))
},_start:function(e,f){var a={handle:this.handles[f],value:this.value()};
if(this.options.values&&this.options.values.length){a.value=this.values(f);
a.values=this.values()
}this._trigger("start",e,a)
},_slide:function(k,l,m){var j=this.handles[l];
if(this.options.values&&this.options.values.length){var a=this.values(l?0:1);
if((this.options.values.length==2&&this.options.range===true)&&((l==0&&m>a)||(l==1&&m<a))){m=a
}if(m!=this.values(l)){var n=this.values();
n[l]=m;
var i=this._trigger("slide",k,{handle:this.handles[l],value:m,values:n});
var a=this.values(l?0:1);
if(i!==false){this.values(l,m,(k.type=="mousedown"&&this.options.animate),true)
}}}else{if(m!=this.value()){var i=this._trigger("slide",k,{handle:this.handles[l],value:m});
if(i!==false){this._setData("value",m,(k.type=="mousedown"&&this.options.animate))
}}}},_stop:function(e,f){var a={handle:this.handles[f],value:this.value()};
if(this.options.values&&this.options.values.length){a.value=this.values(f);
a.values=this.values()
}this._trigger("stop",e,a)
},_change:function(e,f){var a={handle:this.handles[f],value:this.value()};
if(this.options.values&&this.options.values.length){a.value=this.values(f);
a.values=this.values()
}this._trigger("change",e,a)
},value:function(a){if(arguments.length){this._setData("value",a);
this._change(null,0)
}return this._value()
},values:function(a,f,h,g){if(arguments.length>1){this.options.values[a]=f;
this._refreshValue(h);
if(!g){this._change(null,a)
}}if(arguments.length){if(this.options.values&&this.options.values.length){return this._values(a)
}else{return this.value()
}}else{return this._values()
}},_setData:function(a,e,f){b.widget.prototype._setData.apply(this,arguments);
switch(a){case"disabled":if(e){this.handles.filter(".ui-state-focus").blur();
this.handles.removeClass("ui-state-hover");
this.handles.attr("disabled","disabled")
}else{this.handles.removeAttr("disabled")
}case"orientation":this._detectOrientation();
this.element.removeClass("ui-slider-horizontal ui-slider-vertical").addClass("ui-slider-"+this.orientation);
this._refreshValue(f);
break;
case"value":this._refreshValue(f);
break
}},_step:function(){var a=this.options.step;
return a
},_value:function(){var a=this.options.value;
if(a<this._valueMin()){a=this._valueMin()
}if(a>this._valueMax()){a=this._valueMax()
}return a
},_values:function(a){if(arguments.length){var d=this.options.values[a];
if(d<this._valueMin()){d=this._valueMin()
}if(d>this._valueMax()){d=this._valueMax()
}return d
}else{return this.options.values
}},_valueMin:function(){var a=this.options.min;
return a
},_valueMax:function(){var a=this.options.max;
return a
},_refreshValue:function(v){var r=this.options.range,u=this.options,a=this;
if(this.options.values&&this.options.values.length){var o,p;
this.handles.each(function(d,f){var e=(a.values(d)-a._valueMin())/(a._valueMax()-a._valueMin())*100;
var c={};
c[a.orientation=="horizontal"?"left":"bottom"]=e+"%";
b(this).stop(1,1)[v?"animate":"css"](c,u.animate);
if(a.options.range===true){if(a.orientation=="horizontal"){(d==0)&&a.range.stop(1,1)[v?"animate":"css"]({left:e+"%"},u.animate);
(d==1)&&a.range[v?"animate":"css"]({width:(e-lastValPercent)+"%"},{queue:false,duration:u.animate})
}else{(d==0)&&a.range.stop(1,1)[v?"animate":"css"]({bottom:(e)+"%"},u.animate);
(d==1)&&a.range[v?"animate":"css"]({height:(e-lastValPercent)+"%"},{queue:false,duration:u.animate})
}}lastValPercent=e
})
}else{var n=this.value(),q=this._valueMin(),m=this._valueMax(),s=m!=q?(n-q)/(m-q)*100:0;
var w={};
w[a.orientation=="horizontal"?"left":"bottom"]=s+"%";
this.handle.stop(1,1)[v?"animate":"css"](w,u.animate);
(r=="min")&&(this.orientation=="horizontal")&&this.range.stop(1,1)[v?"animate":"css"]({width:s+"%"},u.animate);
(r=="max")&&(this.orientation=="horizontal")&&this.range[v?"animate":"css"]({width:(100-s)+"%"},{queue:false,duration:u.animate});
(r=="min")&&(this.orientation=="vertical")&&this.range.stop(1,1)[v?"animate":"css"]({height:s+"%"},u.animate);
(r=="max")&&(this.orientation=="vertical")&&this.range[v?"animate":"css"]({height:(100-s)+"%"},{queue:false,duration:u.animate})
}}}));
b.extend(b.ui.slider,{getter:"value values",version:"1.7.2",eventPrefix:"slide",defaults:{animate:false,delay:0,distance:0,max:100,min:0,orientation:"horizontal",range:false,step:1,value:0,values:null}})
})(jQuery);
(function(b){b.widget("ui.tabs",{_init:function(){if(this.options.deselectable!==undefined){this.options.collapsible=this.options.deselectable
}this._tabify(true)
},_setData:function(a,d){if(a=="selected"){if(this.options.collapsible&&d==this.options.selected){return
}this.select(d)
}else{this.options[a]=d;
if(a=="deselectable"){this.options.collapsible=d
}this._tabify()
}},_tabId:function(a){return a.title&&a.title.replace(/\s/g,"_").replace(/[^A-Za-z0-9\-_:\.]/g,"")||this.options.idPrefix+b.data(a)
},_sanitizeSelector:function(a){return a.replace(/:/g,"\\:")
},_cookie:function(){var a=this.cookie||(this.cookie=this.options.cookie.name||"ui-tabs-"+b.data(this.list[0]));
return b.cookie.apply(null,[a].concat(b.makeArray(arguments)))
},_ui:function(d,a){return{tab:d,panel:a,index:this.anchors.index(d)}
},_cleanup:function(){this.lis.filter(".ui-state-processing").removeClass("ui-state-processing").find("span:data(label.tabs)").each(function(){var a=b(this);
a.html(a.data("label.tabs")).removeData("label.tabs")
})
},_tabify:function(i){this.list=this.element.children("ul:first");
this.lis=b("li:has(a[href])",this.list);
this.anchors=this.lis.map(function(){return b("a",this)[0]
});
this.panels=b([]);
var a=this,y=this.options;
var z=/^#.+/;
this.anchors.each(function(g,j){var h=b(j).attr("href");
var f=h.split("#")[0],e;
if(f&&(f===location.toString().split("#")[0]||(e=b("base")[0])&&f===e.href)){h=j.hash;
j.href=h
}if(z.test(h)){a.panels=a.panels.add(a._sanitizeSelector(h))
}else{if(h!="#"){b.data(j,"href.tabs",h);
b.data(j,"load.tabs",h.replace(/#.*$/,""));
var c=a._tabId(j);
j.href="#"+c;
var d=b("#"+c);
if(!d.length){d=b(y.panelTemplate).attr("id",c).addClass("ui-tabs-panel ui-widget-content ui-corner-bottom").insertAfter(a.panels[g-1]||a.list);
d.data("destroy.tabs",true)
}a.panels=a.panels.add(d)
}else{y.disabled.push(g)
}}});
if(i){this.element.addClass("ui-tabs ui-widget ui-widget-content ui-corner-all");
this.list.addClass("ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all");
this.lis.addClass("ui-state-default ui-corner-top");
this.panels.addClass("ui-tabs-panel ui-widget-content ui-corner-bottom");
if(y.selected===undefined){if(location.hash){this.anchors.each(function(c,d){if(d.hash==location.hash){y.selected=c;
return false
}})
}if(typeof y.selected!="number"&&y.cookie){y.selected=parseInt(a._cookie(),10)
}if(typeof y.selected!="number"&&this.lis.filter(".ui-tabs-selected").length){y.selected=this.lis.index(this.lis.filter(".ui-tabs-selected"))
}y.selected=y.selected||0
}else{if(y.selected===null){y.selected=-1
}}y.selected=((y.selected>=0&&this.anchors[y.selected])||y.selected<0)?y.selected:0;
y.disabled=b.unique(y.disabled.concat(b.map(this.lis.filter(".ui-state-disabled"),function(c,d){return a.lis.index(c)
}))).sort();
if(b.inArray(y.selected,y.disabled)!=-1){y.disabled.splice(b.inArray(y.selected,y.disabled),1)
}this.panels.addClass("ui-tabs-hide");
this.lis.removeClass("ui-tabs-selected ui-state-active");
if(y.selected>=0&&this.anchors.length){this.panels.eq(y.selected).removeClass("ui-tabs-hide");
this.lis.eq(y.selected).addClass("ui-tabs-selected ui-state-active");
a.element.queue("tabs",function(){a._trigger("show",null,a._ui(a.anchors[y.selected],a.panels[y.selected]))
});
this.load(y.selected)
}b(window).bind("unload",function(){a.lis.add(a.anchors).unbind(".tabs");
a.lis=a.anchors=a.panels=null
})
}else{y.selected=this.lis.index(this.lis.filter(".ui-tabs-selected"))
}this.element[y.collapsible?"addClass":"removeClass"]("ui-tabs-collapsible");
if(y.cookie){this._cookie(y.selected,y.cookie)
}for(var v=0,o;
(o=this.lis[v]);
v++){b(o)[b.inArray(v,y.disabled)!=-1&&!b(o).hasClass("ui-tabs-selected")?"addClass":"removeClass"]("ui-state-disabled")
}if(y.cache===false){this.anchors.removeData("cache.tabs")
}this.lis.add(this.anchors).unbind(".tabs");
if(y.event!="mouseover"){var w=function(d,c){if(c.is(":not(.ui-state-disabled)")){c.addClass("ui-state-"+d)
}};
var s=function(d,c){c.removeClass("ui-state-"+d)
};
this.lis.bind("mouseover.tabs",function(){w("hover",b(this))
});
this.lis.bind("mouseout.tabs",function(){s("hover",b(this))
});
this.anchors.bind("focus.tabs",function(){w("focus",b(this).closest("li"))
});
this.anchors.bind("blur.tabs",function(){s("focus",b(this).closest("li"))
})
}var A,u;
if(y.fx){if(b.isArray(y.fx)){A=y.fx[0];
u=y.fx[1]
}else{A=u=y.fx
}}function x(c,d){c.css({display:""});
if(b.browser.msie&&d.opacity){c[0].style.removeAttribute("filter")
}}var r=u?function(c,d){b(c).closest("li").removeClass("ui-state-default").addClass("ui-tabs-selected ui-state-active");
d.hide().removeClass("ui-tabs-hide").animate(u,u.duration||"normal",function(){x(d,u);
a._trigger("show",null,a._ui(c,d[0]))
})
}:function(c,d){b(c).closest("li").removeClass("ui-state-default").addClass("ui-tabs-selected ui-state-active");
d.removeClass("ui-tabs-hide");
a._trigger("show",null,a._ui(c,d[0]))
};
var q=A?function(d,c){c.animate(A,A.duration||"normal",function(){a.lis.removeClass("ui-tabs-selected ui-state-active").addClass("ui-state-default");
c.addClass("ui-tabs-hide");
x(c,A);
a.element.dequeue("tabs")
})
}:function(e,c,d){a.lis.removeClass("ui-tabs-selected ui-state-active").addClass("ui-state-default");
c.addClass("ui-tabs-hide");
a.element.dequeue("tabs")
};
this.anchors.bind(y.event+".tabs",function(){var f=this,d=b(this).closest("li"),c=a.panels.filter(":not(.ui-tabs-hide)"),e=b(a._sanitizeSelector(this.hash));
if((d.hasClass("ui-tabs-selected")&&!y.collapsible)||d.hasClass("ui-state-disabled")||d.hasClass("ui-state-processing")||a._trigger("select",null,a._ui(this,e[0]))===false){this.blur();
return false
}y.selected=a.anchors.index(this);
a.abort();
if(y.collapsible){if(d.hasClass("ui-tabs-selected")){y.selected=-1;
if(y.cookie){a._cookie(y.selected,y.cookie)
}a.element.queue("tabs",function(){q(f,c)
}).dequeue("tabs");
this.blur();
return false
}else{if(!c.length){if(y.cookie){a._cookie(y.selected,y.cookie)
}a.element.queue("tabs",function(){r(f,e)
});
a.load(a.anchors.index(this));
this.blur();
return false
}}}if(y.cookie){a._cookie(y.selected,y.cookie)
}if(e.length){if(c.length){a.element.queue("tabs",function(){q(f,c)
})
}a.element.queue("tabs",function(){r(f,e)
});
a.load(a.anchors.index(this))
}else{throw"jQuery UI Tabs: Mismatching fragment identifier."
}if(b.browser.msie){this.blur()
}});
this.anchors.bind("click.tabs",function(){return false
})
},destroy:function(){var a=this.options;
this.abort();
this.element.unbind(".tabs").removeClass("ui-tabs ui-widget ui-widget-content ui-corner-all ui-tabs-collapsible").removeData("tabs");
this.list.removeClass("ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all");
this.anchors.each(function(){var f=b.data(this,"href.tabs");
if(f){this.href=f
}var e=b(this).unbind(".tabs");
b.each(["href","load","cache"],function(d,c){e.removeData(c+".tabs")
})
});
this.lis.unbind(".tabs").add(this.panels).each(function(){if(b.data(this,"destroy.tabs")){b(this).remove()
}else{b(this).removeClass(["ui-state-default","ui-corner-top","ui-tabs-selected","ui-state-active","ui-state-hover","ui-state-focus","ui-state-disabled","ui-tabs-panel","ui-widget-content","ui-corner-bottom","ui-tabs-hide"].join(" "))
}});
if(a.cookie){this._cookie(null,a.cookie)
}},add:function(n,o,p){if(p===undefined){p=this.anchors.length
}var a=this,l=this.options,j=b(l.tabTemplate.replace(/#\{href\}/g,n).replace(/#\{label\}/g,o)),k=!n.indexOf("#")?n.replace("#",""):this._tabId(b("a",j)[0]);
j.addClass("ui-state-default ui-corner-top").data("destroy.tabs",true);
var m=b("#"+k);
if(!m.length){m=b(l.panelTemplate).attr("id",k).data("destroy.tabs",true)
}m.addClass("ui-tabs-panel ui-widget-content ui-corner-bottom ui-tabs-hide");
if(p>=this.lis.length){j.appendTo(this.list);
m.appendTo(this.list[0].parentNode)
}else{j.insertBefore(this.lis[p]);
m.insertBefore(this.panels[p])
}l.disabled=b.map(l.disabled,function(c,d){return c>=p?++c:c
});
this._tabify();
if(this.anchors.length==1){j.addClass("ui-tabs-selected ui-state-active");
m.removeClass("ui-tabs-hide");
this.element.queue("tabs",function(){a._trigger("show",null,a._ui(a.anchors[0],a.panels[0]))
});
this.load(0)
}this._trigger("add",null,this._ui(this.anchors[p],this.panels[p]))
},remove:function(a){var g=this.options,f=this.lis.eq(a).remove(),h=this.panels.eq(a).remove();
if(f.hasClass("ui-tabs-selected")&&this.anchors.length>1){this.select(a+(a+1<this.anchors.length?1:-1))
}g.disabled=b.map(b.grep(g.disabled,function(c,d){return c!=a
}),function(c,d){return c>=a?--c:c
});
this._tabify();
this._trigger("remove",null,this._ui(f.find("a")[0],h[0]))
},enable:function(a){var d=this.options;
if(b.inArray(a,d.disabled)==-1){return
}this.lis.eq(a).removeClass("ui-state-disabled");
d.disabled=b.grep(d.disabled,function(c,f){return c!=a
});
this._trigger("enable",null,this._ui(this.anchors[a],this.panels[a]))
},disable:function(f){var a=this,e=this.options;
if(f!=e.selected){this.lis.eq(f).addClass("ui-state-disabled");
e.disabled.push(f);
e.disabled.sort();
this._trigger("disable",null,this._ui(this.anchors[f],this.panels[f]))
}},select:function(a){if(typeof a=="string"){a=this.anchors.index(this.anchors.filter("[href$="+a+"]"))
}else{if(a===null){a=-1
}}if(a==-1&&this.options.collapsible){a=this.options.selected
}this.anchors.eq(a).trigger(this.options.event+".tabs")
},load:function(j){var l=this,h=this.options,a=this.anchors.eq(j)[0],k=b.data(a,"load.tabs");
this.abort();
if(!k||this.element.queue("tabs").length!==0&&b.data(a,"cache.tabs")){this.element.dequeue("tabs");
return
}this.lis.eq(j).addClass("ui-state-processing");
if(h.spinner){var i=b("span",a);
i.data("label.tabs",i.html()).html(h.spinner)
}this.xhr=b.ajax(b.extend({},h.ajaxOptions,{url:k,success:function(d,e){b(l._sanitizeSelector(a.hash)).html(d);
l._cleanup();
if(h.cache){b.data(a,"cache.tabs",true)
}l._trigger("load",null,l._ui(l.anchors[j],l.panels[j]));
try{h.ajaxOptions.success(d,e)
}catch(c){}l.element.dequeue("tabs")
}}))
},abort:function(){this.element.queue([]);
this.panels.stop(false,true);
if(this.xhr){this.xhr.abort();
delete this.xhr
}this._cleanup()
},url:function(d,a){this.anchors.eq(d).removeData("cache.tabs").data("load.tabs",a)
},length:function(){return this.anchors.length
}});
b.extend(b.ui.tabs,{version:"1.7.2",getter:"length",defaults:{ajaxOptions:null,cache:false,cookie:null,collapsible:false,disabled:[],event:"click",fx:null,idPrefix:"ui-tabs-",panelTemplate:"<div></div>",spinner:"<em>Loading&#8230;</em>",tabTemplate:'<li><a href="#{href}"><span>#{label}</span></a></li>'}});
b.extend(b.ui.tabs.prototype,{rotation:null,rotate:function(k,i){var a=this,h=this.options;
var l=a._rotate||(a._rotate=function(c){clearTimeout(a.rotation);
a.rotation=setTimeout(function(){var d=h.selected;
a.select(++d<a.anchors.length?d:0)
},k);
if(c){c.stopPropagation()
}});
var j=a._unrotate||(a._unrotate=!i?function(c){if(c.clientX){a.rotate(null)
}}:function(c){t=h.selected;
l()
});
if(k){this.element.bind("tabsshow",l);
this.anchors.bind(h.event+".tabs",j);
l()
}else{clearTimeout(a.rotation);
this.element.unbind("tabsshow",l);
this.anchors.unbind(h.event+".tabs",j);
delete this._rotate;
delete this._unrotate
}}})
})(jQuery);