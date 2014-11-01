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
},_toggle:function(x,q,s,p,o){var v=this.options,a=this;
this.toShow=x;
this.toHide=q;
this.data=s;
var w=function(){if(!a){return
}return a._completed.apply(a,arguments)
};
this._trigger("changestart",null,this.data);
this.running=q.size()===0?x.size():q.size();
if(v.animated){var t={};
if(v.collapsible&&p){t={toShow:b([]),toHide:q,complete:w,down:o,autoHeight:v.autoHeight||v.fillSpace}
}else{t={toShow:x,toHide:q,complete:w,down:o,autoHeight:v.autoHeight||v.fillSpace}
}if(!v.proxied){v.proxied=v.animated
}if(!v.proxiedDuration){v.proxiedDuration=v.duration
}v.animated=b.isFunction(v.proxied)?v.proxied(t):v.proxied;
v.duration=b.isFunction(v.proxiedDuration)?v.proxiedDuration(t):v.proxiedDuration;
var n=b.ui.accordion.animations,u=v.duration,r=v.animated;
if(!n[r]){n[r]=function(c){this.slide(c,{easing:r,duration:u||700})
}
}n[r](t)
}else{if(v.collapsible&&p){x.toggle()
}else{q.hide();
x.show()
}w(true)
}q.prev().attr("aria-expanded","false").attr("tabIndex","-1").blur();
x.prev().attr("aria-expanded","true").attr("tabIndex","0").focus()
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