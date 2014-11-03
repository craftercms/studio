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
var t=!b(r.target).parents().andSelf().is(".ui-slider-handle");
this._clickOffset=t?{left:0,top:0}:{left:r.pageX-o.left-(p.width()/2),top:r.pageY-o.top-(p.height()/2)-(parseInt(p.css("borderTopWidth"),10)||0)-(parseInt(p.css("borderBottomWidth"),10)||0)+(parseInt(p.css("marginTop"),10)||0)};
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
},_refreshValue:function(u){var r=this.options.range,t=this.options,a=this;
if(this.options.values&&this.options.values.length){var o,p;
this.handles.each(function(d,f){var e=(a.values(d)-a._valueMin())/(a._valueMax()-a._valueMin())*100;
var c={};
c[a.orientation=="horizontal"?"left":"bottom"]=e+"%";
b(this).stop(1,1)[u?"animate":"css"](c,t.animate);
if(a.options.range===true){if(a.orientation=="horizontal"){(d==0)&&a.range.stop(1,1)[u?"animate":"css"]({left:e+"%"},t.animate);
(d==1)&&a.range[u?"animate":"css"]({width:(e-lastValPercent)+"%"},{queue:false,duration:t.animate})
}else{(d==0)&&a.range.stop(1,1)[u?"animate":"css"]({bottom:(e)+"%"},t.animate);
(d==1)&&a.range[u?"animate":"css"]({height:(e-lastValPercent)+"%"},{queue:false,duration:t.animate})
}}lastValPercent=e
})
}else{var n=this.value(),q=this._valueMin(),m=this._valueMax(),s=m!=q?(n-q)/(m-q)*100:0;
var v={};
v[a.orientation=="horizontal"?"left":"bottom"]=s+"%";
this.handle.stop(1,1)[u?"animate":"css"](v,t.animate);
(r=="min")&&(this.orientation=="horizontal")&&this.range.stop(1,1)[u?"animate":"css"]({width:s+"%"},t.animate);
(r=="max")&&(this.orientation=="horizontal")&&this.range[u?"animate":"css"]({width:(100-s)+"%"},{queue:false,duration:t.animate});
(r=="min")&&(this.orientation=="vertical")&&this.range.stop(1,1)[u?"animate":"css"]({height:s+"%"},t.animate);
(r=="max")&&(this.orientation=="vertical")&&this.range[u?"animate":"css"]({height:(100-s)+"%"},{queue:false,duration:t.animate})
}}}));
b.extend(b.ui.slider,{getter:"value values",version:"1.7.2",eventPrefix:"slide",defaults:{animate:false,delay:0,distance:0,max:100,min:0,orientation:"horizontal",range:false,step:1,value:0,values:null}})
})(jQuery);