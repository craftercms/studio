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
},_intersectsWith:function(p){var w=this.positionAbs.left,x=w+this.helperProportions.width,q=this.positionAbs.top,r=q+this.helperProportions.height;
var v=p.left,y=v+p.width,l=p.top,s=l+p.height;
var a=this.offset.click.top,t=this.offset.click.left;
var u=(q+a)>l&&(q+a)<s&&(w+t)>v&&(w+t)<y;
if(this.options.tolerance=="pointer"||this.options.forcePointerForContainers||(this.options.tolerance!="pointer"&&this.helperProportions[this.floating?"width":"height"]>p[this.floating?"width":"height"])){return u
}else{return(v<w+(this.helperProportions.width/2)&&x-(this.helperProportions.width/2)<y&&l<q+(this.helperProportions.height/2)&&r-(this.helperProportions.height/2)<s)
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
}}}},_refreshItems:function(z){this.items=[];
this.containers=[this];
var t=this.items;
var a=this;
var v=[[b.isFunction(this.options.items)?this.options.items.call(this.element[0],z,{item:this.currentItem}):b(this.options.items,this.element),this]];
var r=this._connectWith();
if(r){for(var w=r.length-1;
w>=0;
w--){var q=b(r[w]);
for(var x=q.length-1;
x>=0;
x--){var u=b.data(q[x],"sortable");
if(u&&u!=this&&!u.options.disabled){v.push([b.isFunction(u.options.items)?u.options.items.call(u.element[0],z,{item:this.currentItem}):b(u.options.items,u.element),u]);
this.containers.push(u)
}}}}for(var w=v.length-1;
w>=0;
w--){var s=v[w][1];
var y=v[w][0];
for(var x=0,j=y.length;
x<j;
x++){var i=b(y[x]);
i.data("sortable-item",s);
t.push({item:i,instance:s,width:0,height:0,left:0,top:0})
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