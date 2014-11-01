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
b.ui.intersect=function(a,v,r){if(!v.offset){return false
}var A=(a.positionAbs||a.position.absolute).left,B=A+a.helperProportions.width,s=(a.positionAbs||a.position.absolute).top,t=s+a.helperProportions.height;
var y=v.offset.left,C=y+v.proportions.width,l=v.offset.top,u=l+v.proportions.height;
switch(r){case"fit":return(y<A&&B<C&&l<s&&t<u);
break;
case"intersect":return(y<A+(a.helperProportions.width/2)&&B-(a.helperProportions.width/2)<C&&l<s+(a.helperProportions.height/2)&&t-(a.helperProportions.height/2)<u);
break;
case"pointer":var x=((a.positionAbs||a.position.absolute).left+(a.clickOffset||a.offset.click).left),w=((a.positionAbs||a.position.absolute).top+(a.clickOffset||a.offset.click).top),z=b.ui.isOver(w,x,l,y,v.proportions.height,v.proportions.width);
return z;
break;
case"touch":return((s>=l&&s<=u)||(t>=l&&t<=u)||(s<l&&t>u))&&((A>=y&&A<=C)||(B>=y&&B<=C)||(A<y&&B>C));
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