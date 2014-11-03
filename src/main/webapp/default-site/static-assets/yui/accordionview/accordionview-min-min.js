(function(){var h=YAHOO.util.Dom,j=YAHOO.util.Event,f=YAHOO.util.Anim;
var d=function(l,m){l=h.get(l);
m=m||{};
if(!l){l=document.createElement(this.CONFIG.TAG_NAME)
}if(l.id){m.id=l.id
}YAHOO.widget.AccordionView.superclass.constructor.call(this,l,m);
this.initList(l,m);
this.refresh(["id","width","hoverActivated"],true)
};
var a="panelClose";
var k="panelOpen";
var c="afterPanelClose";
var e="afterPanelOpen";
var b="stateChanged";
var g="beforeStateChange";
YAHOO.widget.AccordionView=d;
YAHOO.extend(d,YAHOO.util.Element,{initAttributes:function(m){d.superclass.initAttributes.call(this,m);
var l=(YAHOO.env.modules.animation)?true:false;
this.setAttributeConfig("id",{writeOnce:true,validator:function(n){return(/^[a-zA-Z][\w0-9\-_.:]*$/.test(n))
},value:h.generateId(),method:function(n){this.get("element").id=n
}});
this.setAttributeConfig("width",{value:"400px",method:function(n){this.setStyle("width",n)
}});
this.setAttributeConfig("animationSpeed",{value:0.7});
this.setAttributeConfig("animate",{value:l,validator:YAHOO.lang.isBoolean});
this.setAttributeConfig("collapsible",{value:false,validator:YAHOO.lang.isBoolean});
this.setAttributeConfig("expandable",{value:false,validator:YAHOO.lang.isBoolean});
this.setAttributeConfig("effect",{value:YAHOO.util.Easing.easeBoth,validator:YAHOO.lang.isString});
this.setAttributeConfig("hoverActivated",{value:false,validator:YAHOO.lang.isBoolean,method:function(n){if(n){j.on(this,"mouseover",this._onMouseOver,this,true)
}else{j.removeListener(this,"mouseover",this._onMouseOver)
}}});
this.setAttributeConfig("_hoverTimeout",{value:500,validator:YAHOO.lang.isInteger})
},CONFIG:{TAG_NAME:"UL",ITEM_WRAPPER_TAG_NAME:"LI",CONTENT_WRAPPER_TAG_NAME:"DIV"},CLASSES:{ACCORDION:"yui-accordionview",PANEL:"yui-accordion-panel",TOGGLE:"yui-accordion-toggle",CONTENT:"yui-accordion-content",ACTIVE:"active",HIDDEN:"hidden",INDICATOR:"indicator"},_idCounter:"1",_hoverTimer:null,_panels:null,_opening:false,_closing:false,_ff2:(YAHOO.env.ua.gecko>0&&YAHOO.env.ua.gecko<1.9),_ie:(YAHOO.env.ua.ie<8&&YAHOO.env.ua.ie>0),_ARIACapable:(YAHOO.env.ua.ie>7||YAHOO.env.ua.gecko>=1.9),initList:function(q,m){h.addClass(q,this.CLASSES.ACCORDION);
this._setARIA(q,"role","tree");
var r=[];
var o=q.getElementsByTagName(this.CONFIG.ITEM_WRAPPER_TAG_NAME);
for(var s=0;
s<o.length;
s++){if(h.hasClass(o[s],"nopanel")){r.push({label:"SINGLE_LINK",content:o[s].innerHTML.replace(/^\s\s*/,"").replace(/\s\s*$/,"")})
}else{if(o[s].parentNode===q){for(var p=o[s].firstChild;
p&&p.nodeType!=1;
p=p.nextSibling){}if(p){for(var n=p.nextSibling;
n&&n.nodeType!=1;
n=n.nextSibling){}r.push({label:p.innerHTML,content:(n&&n.innerHTML)})
}}}}q.innerHTML="";
if(r.length>0){this.addPanels(r)
}if((m.expandItem===0)||(m.expandItem>0)){var l=this._panels[m.expandItem].firstChild;
var n=this._panels[m.expandItem].firstChild.nextSibling;
h.removeClass(n,this.CLASSES.HIDDEN);
if(l&&n){h.addClass(l,this.CLASSES.ACTIVE);
l.tabIndex=0;
this._setARIA(l,"aria-expanded","true");
this._setARIA(n,"aria-hidden","false")
}}this.initEvents()
},initEvents:function(){if(true===this.get("hoverActivated")){this.on("mouseover",this._onMouseOver,this,true);
this.on("mouseout",this._onMouseOut,this,true)
}this.on("click",this._onClick,this,true);
this.on("keydown",this._onKeydown,this,true);
this.on("panelOpen",function(){this._opening=true
},this,true);
this.on("panelClose",function(){this._closing=true
},this,true);
this.on("afterPanelClose",function(){this._closing=false;
if(!this._closing&&!this._opening){this._fixTabIndexes()
}},this,true);
this.on("afterPanelOpen",function(){this._opening=false;
if(!this._closing&&!this._opening){this._fixTabIndexes()
}},this,true);
if(this._ARIACapable){this.on("keypress",function(m){var l=h.getAncestorByClassName(j.getTarget(m),this.CLASSES.PANEL);
var n=j.getCharCode(m);
if(n===13){this._onClick(l.firstChild);
return false
}})
}},_setARIA:function(l,m,n){if(this._ARIACapable){l.setAttribute(m,n)
}},_collapseAccordion:function(){h.batch(this._panels,function(l){var m=this.firstChild.nextSibling;
if(m){h.removeClass(l.firstChild,this.CLASSES.ACTIVE);
h.addClass(m,this.CLASSES.HIDDEN);
this._setARIA(m,"aria-hidden","true")
}},this)
},_fixTabIndexes:function(){var n=this._panels.length;
var m=true;
for(var l=0;
l<n;
l++){if(h.hasClass(this._panels[l].firstChild,this.CLASSES.ACTIVE)){this._panels[l].firstChild.tabIndex=0;
m=false
}else{this._panels[l].firstChild.tabIndex=-1
}}if(m){this._panels[0].firstChild.tabIndex=0
}this.fireEvent(b)
},addPanel:function(r,s){var t=document.createElement(this.CONFIG.ITEM_WRAPPER_TAG_NAME);
h.addClass(t,this.CLASSES.PANEL);
if(r.label==="SINGLE_LINK"){t.innerHTML=r.content;
h.addClass(t.firstChild,this.CLASSES.TOGGLE);
h.addClass(t.firstChild,"link")
}else{var u=document.createElement("span");
h.addClass(u,this.CLASSES.INDICATOR);
var p=t.appendChild(document.createElement("A"));
p.id=this.get("element").id+"-"+this._idCounter+"-label";
p.innerHTML=r.label||"";
p.appendChild(u);
if(this._ARIACapable){if(r.href){p.href=r.href
}}else{p.href=r.href||"#toggle"
}p.tabIndex=-1;
h.addClass(p,this.CLASSES.TOGGLE);
var o=document.createElement(this.CONFIG.CONTENT_WRAPPER_TAG_NAME);
o.innerHTML=r.content||"";
h.addClass(o,this.CLASSES.CONTENT);
t.appendChild(o);
this._setARIA(t,"role","presentation");
this._setARIA(p,"role","treeitem");
this._setARIA(o,"aria-labelledby",p.id);
this._setARIA(u,"role","presentation")
}this._idCounter++;
if(this._panels===null){this._panels=[]
}if((s!==null)&&(s!==undefined)){var q=this.getPanel(s);
this.insertBefore(t,q);
var n=this._panels.slice(0,s);
var l=this._panels.slice(s);
n.push(t);
for(i=0;
i<l.length;
i++){n.push(l[i])
}this._panels=n
}else{this.appendChild(t);
if(this.get("element")===t.parentNode){this._panels[this._panels.length]=t
}}if(r.label!=="SINGLE_LINK"){if(r.expand){if(!this.get("expandable")){this._collapseAccordion()
}h.removeClass(o,this.CLASSES.HIDDEN);
h.addClass(p,this.CLASSES.ACTIVE);
this._setARIA(o,"aria-hidden","false");
this._setARIA(p,"aria-expanded","true")
}else{h.addClass(o,"hidden");
this._setARIA(o,"aria-hidden","true");
this._setARIA(p,"aria-expanded","false")
}}var m=YAHOO.lang.later(0,this,function(){this._fixTabIndexes();
this.fireEvent(b)
})
},addPanels:function(l){for(var m=0;
m<l.length;
m++){this.addPanel(l[m])
}},removePanel:function(m){this.removeChild(h.getElementsByClassName(this.CLASSES.PANEL,this.CONFIG.ITEM_WRAPPER_TAG_NAME,this)[m]);
var o=[];
var n=this._panels.length;
for(var p=0;
p<n;
p++){if(p!==m){o.push(this._panels[p])
}}this._panels=o;
var l=YAHOO.lang.later(0,this,function(){this._fixTabIndexes();
this.fireEvent(b)
})
},getPanel:function(l){return this._panels[l]
},getPanels:function(){return this._panels
},openPanel:function(m){var l=this._panels[m];
if(!l){return false
}if(h.hasClass(l.firstChild,this.CLASSES.ACTIVE)){return false
}this._onClick(l.firstChild);
return true
},closePanel:function(m){var l=this._panels;
var n=l[m];
if(!n){return false
}var o=n.firstChild;
if(!h.hasClass(o,this.CLASSES.ACTIVE)){return true
}if(this.get("collapsible")===false){if(this.get("expandable")===true){this.set("collapsible",true);
for(var p=0;
p<l.length;
p++){if((h.hasClass(l[p].firstChild,this.CLASSES.ACTIVE)&&p!==m)){this._onClick(o);
this.set("collapsible",false);
return true
}}this.set("collapsible",false)
}}this._onClick(o);
return true
},_onKeydown:function(l){var o=h.getAncestorByClassName(j.getTarget(l),this.CLASSES.PANEL);
var n=j.getCharCode(l);
var p=this._panels.length;
if(n===37||n===38){for(var m=0;
m<p;
m++){if((o===this._panels[m])&&m>0){this._panels[m-1].firstChild.focus();
return
}}}if(n===39||n===40){for(var m=0;
m<p;
m++){if((o===this._panels[m])&&m<p-1){this._panels[m+1].firstChild.focus();
return
}}}},_onMouseOver:function(m){j.stopPropagation(m);
var l=j.getTarget(m);
this._hoverTimer=YAHOO.lang.later(this.get("_hoverTimeout"),this,function(){this._onClick(l)
})
},_onMouseOut:function(){if(this._hoverTimer){this._hoverTimer.cancel();
this._hoverTimer=null
}},_onClick:function(m){var p;
if(m.nodeType===undefined){p=j.getTarget(m);
if(!h.hasClass(p,this.CLASSES.TOGGLE)&&!h.hasClass(p,this.CLASSES.INDICATOR)){return false
}if(h.hasClass(p,"link")){return true
}j.preventDefault(m);
j.stopPropagation(m)
}else{p=m
}var o=p;
var r=this;
function n(x,y){if(r._ie){var w=h.getElementsByClassName(r.CLASSES.ACCORDION,r.CONFIG.TAG_NAME,x);
if(w[0]){h.setStyle(w[0],"visibility",y)
}}}function q(z,x){var w=this;
function B(H,J){if(!h.hasClass(J,w.CLASSES.PANEL)){J=h.getAncestorByClassName(J,w.CLASSES.PANEL)
}for(var I=0,G=J;
G.previousSibling;
I++){G=G.previousSibling
}return w.fireEvent(H,{panel:J,index:I})
}if(!x){if(!z){return false
}x=z.parentNode.firstChild
}var E={};
var D=0;
var F=(!h.hasClass(z,this.CLASSES.HIDDEN));
if(this.get("animate")){if(!F){if(this._ff2){h.addClass(z,"almosthidden");
h.setStyle(z,"width",this.get("width"))
}h.removeClass(z,this.CLASSES.HIDDEN);
D=z.offsetHeight;
h.setStyle(z,"height",0);
if(this._ff2){h.removeClass(z,"almosthidden");
h.setStyle(z,"width","auto")
}E={height:{from:0,to:D}}
}else{D=z.offsetHeight;
E={height:{from:D,to:0}}
}var C=(this.get("animationSpeed"))?this.get("animationSpeed"):0.5;
var y=(this.get("effect"))?this.get("effect"):YAHOO.util.Easing.easeBoth;
var A=new f(z,E,C,y);
if(F){if(this.fireEvent(a,z)===false){return
}h.removeClass(x,w.CLASSES.ACTIVE);
x.tabIndex=-1;
n(z,"hidden");
w._setARIA(z,"aria-hidden","true");
w._setARIA(x,"aria-expanded","false");
A.onComplete.subscribe(function(){h.addClass(z,w.CLASSES.HIDDEN);
h.setStyle(z,"height","auto");
B("afterPanelClose",z)
})
}else{if(B(k,z)===false){return
}n(z,"hidden");
A.onComplete.subscribe(function(){h.setStyle(z,"height","auto");
n(z,"visible");
w._setARIA(z,"aria-hidden","false");
w._setARIA(x,"aria-expanded","true");
x.tabIndex=0;
B(e,z)
});
h.addClass(x,this.CLASSES.ACTIVE)
}A.animate()
}else{if(F){if(B(a,z)===false){return
}h.addClass(z,w.CLASSES.HIDDEN);
h.setStyle(z,"height","auto");
h.removeClass(x,w.CLASSES.ACTIVE);
w._setARIA(z,"aria-hidden","true");
w._setARIA(x,"aria-expanded","false");
x.tabIndex=-1;
B(c,z)
}else{if(B(k,z)===false){return
}h.removeClass(z,w.CLASSES.HIDDEN);
h.setStyle(z,"height","auto");
h.addClass(x,w.CLASSES.ACTIVE);
w._setARIA(z,"aria-hidden","false");
w._setARIA(x,"aria-expanded","true");
x.tabIndex=0;
B(e,z)
}}return true
}var v=(o.nodeName.toUpperCase()==="SPAN")?o.parentNode.parentNode:o.parentNode;
var s=h.getElementsByClassName(this.CLASSES.CONTENT,this.CONFIG.CONTENT_WRAPPER_TAG_NAME,v)[0];
if(this.fireEvent(g,this)===false){return
}if(this.get("collapsible")===false){if(!h.hasClass(s,this.CLASSES.HIDDEN)){return false
}}else{if(!h.hasClass(s,this.CLASSES.HIDDEN)){q.call(this,s);
return false
}}if(this.get("expandable")!==true){var l=this._panels.length;
for(var t=0;
t<l;
t++){var u=h.hasClass(this._panels[t].firstChild.nextSibling,this.CLASSES.HIDDEN);
if(!u){q.call(this,this._panels[t].firstChild.nextSibling)
}}}if(o.nodeName.toUpperCase()==="SPAN"){q.call(this,s,o.parentNode)
}else{q.call(this,s,o)
}return true
},toString:function(){var l=this.get("id")||this.get("tagName");
return"AccordionView "+l
}})
})();
YAHOO.register("accordionview",YAHOO.widget.AccordionView,{version:"0.99",build:"33"});