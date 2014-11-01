(function(){YAHOO.widget.TabView=function(j,k){k=k||{};
if(arguments.length==1&&!YAHOO.lang.isString(j)&&!j.nodeName){k=j;
j=k.element||null
}if(!j&&!k.element){j=e.call(this,k)
}YAHOO.widget.TabView.superclass.constructor.call(this,j,k)
};
YAHOO.extend(YAHOO.widget.TabView,YAHOO.util.Element);
var h=YAHOO.widget.TabView.prototype;
var i=YAHOO.util.Dom;
var f=YAHOO.util.Event;
var a=YAHOO.widget.Tab;
h.CLASSNAME="yui-navset";
h.TAB_PARENT_CLASSNAME="yui-nav";
h.CONTENT_PARENT_CLASSNAME="yui-content";
h._tabParent=null;
h._contentParent=null;
h.addTab=function(q,o){var n=this.get("tabs");
if(!n){this._queue[this._queue.length]=["addTab",arguments];
return false
}o=(o===undefined)?n.length:o;
var l=this.getTab(o);
var j=this;
var r=this.get("element");
var k=this._tabParent;
var m=this._contentParent;
var t=q.get("element");
var s=q.get("contentEl");
if(l){k.insertBefore(t,l.get("element"))
}else{k.appendChild(t)
}if(s&&!i.isAncestor(m,s)){m.appendChild(s)
}if(!q.get("active")){q.set("contentVisible",false,true)
}else{this.set("activeTab",q,true)
}var p=function(u){YAHOO.util.Event.preventDefault(u);
var v=false;
if(this==j.get("activeTab")){v=true
}j.set("activeTab",this,v)
};
q.addListener(q.get("activationEvent"),p);
q.addListener("activationEventChange",function(u){if(u.prevValue!=u.newValue){q.removeListener(u.prevValue,p);
q.addListener(u.newValue,p)
}});
n.splice(o,0,q)
};
h.DOMEventHandler=function(m){var r=this.get("element");
var l=YAHOO.util.Event.getTarget(m);
var j=this._tabParent;
if(i.isAncestor(j,l)){var q;
var p=null;
var s;
var k=this.get("tabs");
for(var o=0,n=k.length;
o<n;
o++){q=k[o].get("element");
s=k[o].get("contentEl");
if(l==q||i.isAncestor(q,l)){p=k[o];
break
}}if(p){p.fireEvent(m.type,m)
}}};
h.getTab=function(j){return this.get("tabs")[j]
};
h.getTabIndex=function(m){var k=null;
var n=this.get("tabs");
for(var j=0,l=n.length;
j<l;
++j){if(m==n[j]){k=j;
break
}}return k
};
h.removeTab=function(m){var j=this.get("tabs").length;
var k=this.getTabIndex(m);
var l=k+1;
if(m==this.get("activeTab")){if(j>1){if(k+1==j){this.set("activeIndex",k-1)
}else{this.set("activeIndex",k+1)
}}}this._tabParent.removeChild(m.get("element"));
this._contentParent.removeChild(m.get("contentEl"));
this._configs.tabs.value.splice(k,1)
};
h.toString=function(){var j=this.get("id")||this.get("tagName");
return"TabView "+j
};
h.contentTransition=function(j,k){j.set("contentVisible",true);
k.set("contentVisible",false)
};
h.initAttributes=function(l){YAHOO.widget.TabView.superclass.initAttributes.call(this,l);
if(!l.orientation){l.orientation="top"
}var j=this.get("element");
if(!YAHOO.util.Dom.hasClass(j,this.CLASSNAME)){YAHOO.util.Dom.addClass(j,this.CLASSNAME)
}this.setAttributeConfig("tabs",{value:[],readOnly:true});
this._tabParent=this.getElementsByClassName(this.TAB_PARENT_CLASSNAME,"ul")[0]||g.call(this);
this._contentParent=this.getElementsByClassName(this.CONTENT_PARENT_CLASSNAME,"div")[0]||b.call(this);
this.setAttributeConfig("orientation",{value:l.orientation,method:function(n){var m=this.get("orientation");
this.addClass("yui-navset-"+n);
if(m!=n){this.removeClass("yui-navset-"+m)
}switch(n){case"bottom":this.appendChild(this._tabParent);
break
}}});
this.setAttributeConfig("activeIndex",{value:l.activeIndex,method:function(m){this.set("activeTab",this.getTab(m))
},validator:function(m){return !this.getTab(m).get("disabled")
}});
this.setAttributeConfig("activeTab",{value:l.activeTab,method:function(m){var n=this.get("activeTab");
if(m){m.set("active",true);
this._configs.activeIndex.value=this.getTabIndex(m)
}if(n&&n!=m){n.set("active",false)
}if(n&&m!=n){this.contentTransition(m,n)
}else{if(m){m.set("contentVisible",true)
}}},validator:function(m){return !m.get("disabled")
}});
if(this._tabParent){c.call(this)
}this.DOM_EVENTS.submit=false;
this.DOM_EVENTS.focus=false;
this.DOM_EVENTS.blur=false;
for(var k in this.DOM_EVENTS){if(YAHOO.lang.hasOwnProperty(this.DOM_EVENTS,k)){this.addListener.call(this,k,this.DOMEventHandler)
}}};
var c=function(){var m,j,n;
var o=this.get("element");
var p=d(this._tabParent);
var k=d(this._contentParent);
for(var q=0,l=p.length;
q<l;
++q){j={};
if(k[q]){j.contentEl=k[q]
}m=new YAHOO.widget.Tab(p[q],j);
this.addTab(m);
if(m.hasClass(m.ACTIVE_CLASSNAME)){this._configs.activeTab.value=m;
this._configs.activeIndex.value=this.getTabIndex(m)
}}};
var e=function(k){var j=document.createElement("div");
if(this.CLASSNAME){j.className=this.CLASSNAME
}return j
};
var g=function(k){var j=document.createElement("ul");
if(this.TAB_PARENT_CLASSNAME){j.className=this.TAB_PARENT_CLASSNAME
}this.get("element").appendChild(j);
return j
};
var b=function(k){var j=document.createElement("div");
if(this.CONTENT_PARENT_CLASSNAME){j.className=this.CONTENT_PARENT_CLASSNAME
}this.get("element").appendChild(j);
return j
};
var d=function(n){var k=[];
var m=n.childNodes;
for(var j=0,l=m.length;
j<l;
++j){if(m[j].nodeType==1){k[k.length]=m[j]
}}return k
}
})();
(function(){var j=YAHOO.util.Dom,e=YAHOO.util.Event;
var c=function(k,l){l=l||{};
if(arguments.length==1&&!YAHOO.lang.isString(k)&&!k.nodeName){l=k;
k=l.element
}if(!k&&!l.element){k=g.call(this,l)
}this.loadHandler={success:function(m){this.set("content",m.responseText)
},failure:function(m){}};
c.superclass.constructor.call(this,k,l);
this.DOM_EVENTS={}
};
YAHOO.extend(c,YAHOO.util.Element);
var i=c.prototype;
i.LABEL_TAGNAME="em";
i.ACTIVE_CLASSNAME="selected";
i.ACTIVE_TITLE="active";
i.DISABLED_CLASSNAME="disabled";
i.LOADING_CLASSNAME="loading";
i.dataConnection=null;
i.loadHandler=null;
i._loading=false;
i.toString=function(){var l=this.get("element");
var k=l.id||l.tagName;
return"Tab "+k
};
i.initAttributes=function(l){l=l||{};
c.superclass.initAttributes.call(this,l);
var m=this.get("element");
this.setAttributeConfig("activationEvent",{value:l.activationEvent||"click"});
this.setAttributeConfig("labelEl",{value:l.labelEl||h.call(this),method:function(o){var n=this.get("labelEl");
if(n){if(n==o){return false
}this.replaceChild(o,n)
}else{if(m.firstChild){this.insertBefore(o,m.firstChild)
}else{this.appendChild(o)
}}}});
this.setAttributeConfig("label",{value:l.label||a.call(this),method:function(n){var o=this.get("labelEl");
if(!o){this.set("labelEl",f.call(this))
}b.call(this,n)
}});
this.setAttributeConfig("contentEl",{value:l.contentEl||document.createElement("div"),method:function(o){var n=this.get("contentEl");
if(n){if(n==o){return false
}this.replaceChild(o,n)
}}});
this.setAttributeConfig("content",{value:l.content,method:function(n){this.get("contentEl").innerHTML=n
}});
var k=false;
this.setAttributeConfig("dataSrc",{value:l.dataSrc});
this.setAttributeConfig("cacheData",{value:l.cacheData||false,validator:YAHOO.lang.isBoolean});
this.setAttributeConfig("loadMethod",{value:l.loadMethod||"GET",validator:YAHOO.lang.isString});
this.setAttributeConfig("dataLoaded",{value:false,validator:YAHOO.lang.isBoolean,writeOnce:true});
this.setAttributeConfig("dataTimeout",{value:l.dataTimeout||null,validator:YAHOO.lang.isNumber});
this.setAttributeConfig("active",{value:l.active||this.hasClass(this.ACTIVE_CLASSNAME),method:function(n){if(n===true){this.addClass(this.ACTIVE_CLASSNAME);
this.set("title",this.ACTIVE_TITLE)
}else{this.removeClass(this.ACTIVE_CLASSNAME);
this.set("title","")
}},validator:function(n){return YAHOO.lang.isBoolean(n)&&!this.get("disabled")
}});
this.setAttributeConfig("disabled",{value:l.disabled||this.hasClass(this.DISABLED_CLASSNAME),method:function(n){if(n===true){j.addClass(this.get("element"),this.DISABLED_CLASSNAME)
}else{j.removeClass(this.get("element"),this.DISABLED_CLASSNAME)
}},validator:YAHOO.lang.isBoolean});
this.setAttributeConfig("href",{value:l.href||this.getElementsByTagName("a")[0].getAttribute("href",2)||"#",method:function(n){this.getElementsByTagName("a")[0].href=n
},validator:YAHOO.lang.isString});
this.setAttributeConfig("contentVisible",{value:l.contentVisible,method:function(n){if(n){this.get("contentEl").style.display="block";
if(this.get("dataSrc")){if(!this._loading&&!(this.get("dataLoaded")&&this.get("cacheData"))){d.call(this)
}}}else{this.get("contentEl").style.display="none"
}},validator:YAHOO.lang.isBoolean})
};
var g=function(l){var m=document.createElement("li");
var k=document.createElement("a");
k.href=l.href||"#";
m.appendChild(k);
var n=l.label||null;
var o=l.labelEl||null;
if(o){if(!n){n=a.call(this,o)
}}else{o=f.call(this)
}k.appendChild(o);
return m
};
var h=function(){return this.getElementsByTagName(this.LABEL_TAGNAME)[0]
};
var f=function(){var k=document.createElement(this.LABEL_TAGNAME);
return k
};
var b=function(l){var k=this.get("labelEl");
k.innerHTML=l
};
var a=function(){var l,k=this.get("labelEl");
if(!k){return undefined
}return k.innerHTML
};
var d=function(){if(!YAHOO.util.Connect){return false
}j.addClass(this.get("contentEl").parentNode,this.LOADING_CLASSNAME);
this._loading=true;
this.dataConnection=YAHOO.util.Connect.asyncRequest(this.get("loadMethod"),this.get("dataSrc"),{success:function(k){this.loadHandler.success.call(this,k);
this.set("dataLoaded",true);
this.dataConnection=null;
j.removeClass(this.get("contentEl").parentNode,this.LOADING_CLASSNAME);
this._loading=false
},failure:function(k){this.loadHandler.failure.call(this,k);
this.dataConnection=null;
j.removeClass(this.get("contentEl").parentNode,this.LOADING_CLASSNAME);
this._loading=false
},scope:this,timeout:this.get("dataTimeout")})
};
YAHOO.widget.Tab=c
})();
YAHOO.register("tabview",YAHOO.widget.TabView,{version:"2.5.2",build:"1076"});