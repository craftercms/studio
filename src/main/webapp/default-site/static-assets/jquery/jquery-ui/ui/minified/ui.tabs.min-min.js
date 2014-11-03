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