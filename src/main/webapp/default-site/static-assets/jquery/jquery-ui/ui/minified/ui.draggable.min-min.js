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
var t=this.positionAbs.top,o=this.positionAbs.left;
var q=r.height,f=r.width;
var c=r.top,s=r.left;
return b.ui.isOver(t+d,o+e,c,s,q,f)
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
},drag:function(r,D){var J=b(this).data("draggable"),B=J.options;
var d=B.snapTolerance;
var i=D.offset.left,l=i+J.helperProportions.width,K=D.offset.top,L=K+J.helperProportions.height;
for(var o=J.snapElements.length-1;
o>=0;
o--){var t=J.snapElements[o].left,E=t+J.snapElements[o].width,F=J.snapElements[o].top,C=F+J.snapElements[o].height;
if(!((t-d<i&&i<E+d&&F-d<K&&K<C+d)||(t-d<i&&i<E+d&&F-d<L&&L<C+d)||(t-d<l&&l<E+d&&F-d<K&&K<C+d)||(t-d<l&&l<E+d&&F-d<L&&L<C+d))){if(J.snapElements[o].snapping){(J.options.snap.release&&J.options.snap.release.call(J.element,r,b.extend(J._uiHash(),{snapItem:J.snapElements[o].item})))
}J.snapElements[o].snapping=false;
continue
}if(B.snapMode!="inner"){var M=Math.abs(F-L)<=d;
var a=Math.abs(C-K)<=d;
var H=Math.abs(t-l)<=d;
var G=Math.abs(E-i)<=d;
if(M){D.position.top=J._convertPositionTo("relative",{top:F-J.helperProportions.height,left:0}).top-J.margins.top
}if(a){D.position.top=J._convertPositionTo("relative",{top:C,left:0}).top-J.margins.top
}if(H){D.position.left=J._convertPositionTo("relative",{top:0,left:t-J.helperProportions.width}).left-J.margins.left
}if(G){D.position.left=J._convertPositionTo("relative",{top:0,left:E}).left-J.margins.left
}}var I=(M||a||H||G);
if(B.snapMode!="outer"){var M=Math.abs(F-K)<=d;
var a=Math.abs(C-L)<=d;
var H=Math.abs(t-i)<=d;
var G=Math.abs(E-l)<=d;
if(M){D.position.top=J._convertPositionTo("relative",{top:F,left:0}).top-J.margins.top
}if(a){D.position.top=J._convertPositionTo("relative",{top:C-J.helperProportions.height,left:0}).top-J.margins.top
}if(H){D.position.left=J._convertPositionTo("relative",{top:0,left:t}).left-J.margins.left
}if(G){D.position.left=J._convertPositionTo("relative",{top:0,left:E-J.helperProportions.width}).left-J.margins.left
}}if(!J.snapElements[o].snapping&&(M||a||H||G||I)){(J.options.snap.snap&&J.options.snap.snap.call(J.element,r,b.extend(J._uiHash(),{snapItem:J.snapElements[o].item})))
}J.snapElements[o].snapping=(M||a||H||G||I)
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