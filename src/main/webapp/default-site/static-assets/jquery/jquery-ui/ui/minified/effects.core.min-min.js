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
return this.each(function(){var t={};
var v=i(this);
var u=v.attr("style")||"";
if(typeof u=="object"){u=u.cssText
}if(d.toggle){v.hasClass(d.toggle)?d.remove=d.toggle:d.add=d.toggle
}var n=i.extend({},(document.defaultView?document.defaultView.getComputedStyle(this,null):this.currentStyle));
if(d.add){v.addClass(d.add)
}if(d.remove){v.removeClass(d.remove)
}var k=i.extend({},(document.defaultView?document.defaultView.getComputedStyle(this,null):this.currentStyle));
if(d.add){v.removeClass(d.add)
}if(d.remove){v.addClass(d.remove)
}for(var s in k){if(typeof k[s]!="function"&&k[s]&&s.indexOf("Moz")==-1&&s.indexOf("length")==-1&&k[s]!=n[s]&&(s.match(/color/i)||(!s.match(/color/i)&&!isNaN(parseInt(k[s],10))))&&(n.position!="static"||(n.position=="static"&&!s.match(/left|top|bottom|right/)))){t[s]=k[s]
}}v.animate(t,c,e,function(){if(typeof i(this).attr("style")=="object"){i(this).attr("style")["cssText"]="";
i(this).attr("style")["cssText"]=u
}else{i(this).attr("style",u)
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