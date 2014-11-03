YAHOO.namespace("plugin","behavior");
(function(){var c=YAHOO.util,a=YAHOO.util.Event,b=YAHOO.util.Dom,d=YAHOO.lang,e=YAHOO.util.Dom.get;
YAHOO.Bubbling=function(){var k={},h=navigator.userAgent.toLowerCase(),g=(h.indexOf("opera")>-1);
var f=function(n,l){var o=l[1].anchor;
if(!(l[1].flagged||l[1].decrepitate)&&o){var p=o.getAttribute("rel"),m=o.getAttribute("target");
if((!m||(m===""))&&(p=="external")){o.setAttribute("target","blank")
}}};
var j=function(m,l){k.processingAction(m,l,k.defaultActions)
};
var i=function(m){var n=k.getOwnerByClassName(m,"yui-button"),l=null,o=null;
if(d.isObject(n)&&YAHOO.widget.Button){l=YAHOO.widget.Button.getButton(n.id)
}return l
};
k.ready=false;
k.force2alfa=false;
k.bubble={};
k.onReady=new c.CustomEvent("bubblingOnReady",k,true);
k.getOwnerByClassName=function(m,l){return(b.hasClass(m,l)?m:b.getAncestorByClassName(m,l))
};
k.getOwnerByTagName=function(m,l){m=b.get(m);
if(!m){return null
}return(m.tagName&&m.tagName.toUpperCase()==l.toUpperCase()?m:b.getAncestorByTagName(m,l))
};
k.getAncestorByClassName=k.getOwnerByClassName;
k.getAncestorByTagName=k.getOwnerByTagName;
k.onKeyPressedTrigger=function(o,p,n){var l="key";
p=p||a.getEvent();
n=n||{};
n.action=l;
n.target=o.target||(p?a.getTarget(p):null);
n.flagged=false;
n.decrepitate=false;
n.event=p;
n.stop=false;
n.type=o.type;
n.keyCode=o.keyCode;
n.charCode=o.charCode;
n.ctrlKey=o.ctrlKey;
n.shiftKey=o.shiftKey;
n.altKey=o.altKey;
this.bubble.key.fire(p,n);
if(n.stop){a.stopEvent(p)
}return n.stop
};
k.onEventTrigger=function(n,o,l){o=o||a.getEvent();
l=l||{};
l.action=n;
l.target=(o?a.getTarget(o):null);
l.flagged=false;
l.decrepitate=false;
l.event=o;
l.stop=false;
this.bubble[n].fire(o,l);
if(l.stop){a.stopEvent(o)
}return l.stop
};
k.onNavigate=function(m){var l={anchor:this.getOwnerByTagName(a.getTarget(m),"A"),button:i(a.getTarget(m))};
if(!l.anchor&&!l.button){l.input=this.getOwnerByTagName(a.getTarget(m),"INPUT")
}if(l.button){l.value=l.button.get("value")
}else{if(l.input){l.value=l.input.getAttribute("value")
}}if(!this.onEventTrigger("navigate",m,l)){this.onEventTrigger("god",m,l)
}};
k.onProperty=function(l){this.onEventTrigger("property",l,{anchor:this.getOwnerByTagName(a.getTarget(l),"A"),button:i(a.getTarget(l))})
};
k._timeoutId=0;
k.onRepaint=function(l){clearTimeout(k._timeoutId);
k._timeoutId=setTimeout(function(){var o="repaint",p={target:document.body},n={action:o,target:null,event:p,flagged:false,decrepitate:false,stop:false};
k.bubble[o].fire(p,n);
if(n.stop){a.stopEvent(p)
}},150)
};
k.onRollOver=function(l){this.onEventTrigger("rollover",l,{anchor:this.getOwnerByTagName(a.getTarget(l),"A")})
};
k.onRollOut=function(l){this.onEventTrigger("rollout",l,{anchor:this.getOwnerByTagName(a.getTarget(l),"A")})
};
k.onKeyPressed=function(l){this.onKeyPressedTrigger(l)
};
k.getActionName=function(m,q){q=q||{};
var l=null,n=null,o=(b.inDocument(m)?function(r){return b.hasClass(m,r)
}:function(r){return m.hasClass(r)
});
if(m&&(d.isObject(m)||(m=e(m)))){try{n=m.getAttribute("rel")
}catch(p){}for(l in q){if((q.hasOwnProperty(l))&&(o(l)||(l===n))){return l
}}}return null
};
k.getFirstChildByTagName=function(o,n){if(o&&(d.isObject(o)||(o=e(o)))&&n){var m=o.getElementsByTagName(n);
if(m.length>0){return m[0]
}}return null
};
k.virtualTarget=function(n,m){if(m&&(d.isObject(m)||(m=e(m)))&&d.isObject(n)){var l=a.getRelatedTarget(n);
if(d.isObject(l)){while((l.parentNode)&&d.isObject(l.parentNode)&&(l.parentNode.tagName!=="BODY")){if(l.parentNode===m){return true
}l=l.parentNode
}}}return false
};
k.addLayer=function(o,n){var l=false;
o=(d.isArray(o)?o:[o]);
n=n||window;
for(var m=0;
m<o.length;
++m){if(o[m]&&!this.bubble.hasOwnProperty(o[m])){this.bubble[o[m]]=new c.CustomEvent(o[m],n,true);
l=true
}}return l
};
k.subscribe=function(m,l,n){var o=this.addLayer(m);
if(m){if(d.isObject(n)){this.bubble[m].subscribe(l,n,true)
}else{this.bubble[m].subscribe(l)
}}return o
};
k.on=k.subscribe;
k.fire=function(l,m){m=m||{};
m.action=l;
m.flagged=false;
m.decrepitate=false;
m.stop=false;
if(this.bubble.hasOwnProperty(l)){this.bubble[l].fire(null,m)
}return m.stop
};
k.processingAction=function(n,l,q,p){var o=null,m;
if(!(l[1].flagged||l[1].decrepitate)||p){m=l[1].anchor||l[1].input||l[1].button;
if(m){o=this.getActionName(m,q);
l[1].el=m
}if(o&&(q[o].apply(l[1],[n,l]))){a.stopEvent(l[0]);
l[1].flagged=true;
l[1].decrepitate=true;
l[1].stop=true
}}};
k.defaultActions={};
k.addDefaultAction=function(o,m,l){if(o&&m&&(!this.defaultActions.hasOwnProperty(o)||l)){this.defaultActions[o]=m
}};
a.addListener(window,"resize",k.onRepaint,k,true);
k.on("navigate",f);
k.on("navigate",j);
k.initMonitors=function(m){var l=function(){var n=new YAHOO.widget.Module("yui-cms-font-monitor",{monitorresize:true,visible:false});
n.render(document.body);
YAHOO.widget.Module.textResizeEvent.subscribe(k.onRepaint,k,true);
YAHOO.widget.Overlay.windowScrollEvent.subscribe(k.onRepaint,k,true)
};
if(d.isFunction(YAHOO.widget.Module)){a.onDOMReady(l,k,true)
}};
k.init=function(){if(!this.ready){var l=document.body;
a.addListener(l,"click",k.onNavigate,k,true);
a.addListener(l,(g?"mousedown":"contextmenu"),k.onProperty,k,true);
if(g){a.addListener(l,"click",k.onProperty,k,true)
}a.addListener(l,"mouseover",k.onRollOver,k,true);
a.addListener(l,"mouseout",k.onRollOut,k,true);
a.addListener(document,"keyup",k.onKeyPressed,k,true);
a.addListener(document,"keydown",k.onKeyPressed,k,true);
this.ready=true;
k.onReady.fire()
}};
a.onDOMReady(k.init,k,true);
k.addLayer(["navigate","god","property","key","repaint","rollover","rollout"]);
return k
}()
})();
YAHOO.register("bubbling",YAHOO.Bubbling,{version:"1.5.0",build:"222"});