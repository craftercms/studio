(function(){var c=YAHOO.util,o=YAHOO.lang,i,k,j={},n={},g=window.document;
YAHOO.env._id_counter=YAHOO.env._id_counter||0;
var b=YAHOO.env.ua.opera,h=YAHOO.env.ua.webkit,d=YAHOO.env.ua.gecko,m=YAHOO.env.ua.ie;
var p={HYPHEN:/(-[a-z])/i,ROOT_TAG:/^body|html$/i,OP_SCROLL:/^(?:inline|table-row)$/i};
var f=function(r){if(!p.HYPHEN.test(r)){return r
}if(j[r]){return j[r]
}var q=r;
while(p.HYPHEN.exec(q)){q=q.replace(RegExp.$1,RegExp.$1.substr(1).toUpperCase())
}j[r]=q;
return q
};
var e=function(q){var r=n[q];
if(!r){r=new RegExp("(?:^|\\s+)"+q+"(?:\\s+|$)");
n[q]=r
}return r
};
if(g.defaultView&&g.defaultView.getComputedStyle){i=function(t,q){var r=null;
if(q=="float"){q="cssFloat"
}var s=t.ownerDocument.defaultView.getComputedStyle(t,"");
if(s){r=s[f(q)]
}return t.style[q]||r
}
}else{if(g.documentElement.currentStyle&&m){i=function(u,s){switch(f(s)){case"opacity":var q=100;
try{q=u.filters["DXImageTransform.Microsoft.Alpha"].opacity
}catch(r){try{q=u.filters("alpha").opacity
}catch(r){}}return q/100;
case"float":s="styleFloat";
default:var t=u.currentStyle?u.currentStyle[s]:null;
return(u.style[s]||t)
}}
}else{i=function(r,q){return r.style[q]
}
}}if(m){k=function(s,r,q){switch(r){case"opacity":if(o.isString(s.style.filter)){s.style.filter="alpha(opacity="+q*100+")";
if(!s.currentStyle||!s.currentStyle.hasLayout){s.style.zoom=1
}}break;
case"float":r="styleFloat";
default:s.style[r]=q
}}
}else{k=function(s,r,q){if(r=="float"){r="cssFloat"
}s.style[r]=q
}
}var a=function(r,q){return r&&r.nodeType==1&&(!q||q(r))
};
YAHOO.util.Dom={get:function(r){if(r){if(r.nodeType||r.item){return r
}if(typeof r==="string"){return g.getElementById(r)
}if("length" in r){var q=[];
for(var s=0,t=r.length;
s<t;
++s){q[q.length]=c.Dom.get(r[s])
}return q
}return r
}return null
},getStyle:function(s,q){q=f(q);
var r=function(t){return i(t,q)
};
return c.Dom.batch(s,r,c.Dom,true)
},setStyle:function(t,r,q){r=f(r);
var s=function(u){k(u,r,q)
};
c.Dom.batch(t,s,c.Dom,true)
},getXY:function(r){var q=function(s){if((s.parentNode===null||s.offsetParent===null||this.getStyle(s,"display")=="none")&&s!=s.ownerDocument.body){return false
}return l(s)
};
return c.Dom.batch(r,q,c.Dom,true)
},getX:function(r){var q=function(s){return c.Dom.getXY(s)[0]
};
return c.Dom.batch(r,q,c.Dom,true)
},getY:function(r){var q=function(s){return c.Dom.getXY(s)[1]
};
return c.Dom.batch(r,q,c.Dom,true)
},setXY:function(t,q,r){var s=function(u){var v=this.getStyle(u,"position");
if(v=="static"){this.setStyle(u,"position","relative");
v="relative"
}var x=this.getXY(u);
if(x===false){return false
}var y=[parseInt(this.getStyle(u,"left"),10),parseInt(this.getStyle(u,"top"),10)];
if(isNaN(y[0])){y[0]=(v=="relative")?0:u.offsetLeft
}if(isNaN(y[1])){y[1]=(v=="relative")?0:u.offsetTop
}if(q[0]!==null){u.style.left=q[0]-x[0]+y[0]+"px"
}if(q[1]!==null){u.style.top=q[1]-x[1]+y[1]+"px"
}if(!r){var w=this.getXY(u);
if((q[0]!==null&&w[0]!=q[0])||(q[1]!==null&&w[1]!=q[1])){this.setXY(u,q,true)
}}};
c.Dom.batch(t,s,c.Dom,true)
},setX:function(q,r){c.Dom.setXY(q,[r,null])
},setY:function(r,q){c.Dom.setXY(r,[null,q])
},getRegion:function(r){var q=function(t){if((t.parentNode===null||t.offsetParent===null||this.getStyle(t,"display")=="none")&&t!=t.ownerDocument.body){return false
}var s=c.Region.getRegion(t);
return s
};
return c.Dom.batch(r,q,c.Dom,true)
},getClientWidth:function(){return c.Dom.getViewportWidth()
},getClientHeight:function(){return c.Dom.getViewportHeight()
},getElementsByClassName:function(u,q,t,s){u=o.trim(u);
q=q||"*";
t=(t)?c.Dom.get(t):null||g;
if(!t){return[]
}var x=[],y=t.getElementsByTagName(q),r=e(u);
for(var w=0,v=y.length;
w<v;
++w){if(r.test(y[w].className)){x[x.length]=y[w];
if(s){s.call(y[w],y[w])
}}}return x
},hasClass:function(r,s){var t=e(s);
var q=function(u){return t.test(u.className)
};
return c.Dom.batch(r,q,c.Dom,true)
},addClass:function(r,s){var q=function(t){if(this.hasClass(t,s)){return false
}t.className=o.trim([t.className,s].join(" "));
return true
};
return c.Dom.batch(r,q,c.Dom,true)
},removeClass:function(r,s){var t=e(s);
var q=function(u){var v=false,x=u.className;
if(s&&x&&this.hasClass(u,s)){u.className=x.replace(t," ");
if(this.hasClass(u,s)){this.removeClass(u,s)
}u.className=o.trim(u.className);
if(u.className===""){var w=(u.hasAttribute)?"class":"className";
u.removeAttribute(w)
}v=true
}return v
};
return c.Dom.batch(r,q,c.Dom,true)
},replaceClass:function(r,t,u){if(!u||t===u){return false
}var s=e(t);
var q=function(v){if(!this.hasClass(v,t)){this.addClass(v,u);
return true
}v.className=v.className.replace(s," "+u+" ");
if(this.hasClass(v,t)){this.removeClass(v,t)
}v.className=o.trim(v.className);
return true
};
return c.Dom.batch(r,q,c.Dom,true)
},generateId:function(s,q){q=q||"yui-gen";
var r=function(u){if(u&&u.id){return u.id
}var t=q+YAHOO.env._id_counter++;
if(u){u.id=t
}return t
};
return c.Dom.batch(s,r,c.Dom,true)||r.apply(c.Dom,arguments)
},isAncestor:function(r,q){r=c.Dom.get(r);
q=c.Dom.get(q);
var s=false;
if((r&&q)&&(r.nodeType&&q.nodeType)){if(r.contains&&r!==q){s=r.contains(q)
}else{if(r.compareDocumentPosition){s=!!(r.compareDocumentPosition(q)&16)
}}}else{}return s
},inDocument:function(q){return this.isAncestor(g.documentElement,q)
},getElementsBy:function(x,v,u,s){v=v||"*";
u=(u)?c.Dom.get(u):null||g;
if(!u){return[]
}var t=[],q=u.getElementsByTagName(v);
for(var r=0,w=q.length;
r<w;
++r){if(x(q[r])){t[t.length]=q[r];
if(s){s(q[r])
}}}return t
},batch:function(s,x,q,u){s=(s&&(s.tagName||s.item))?s:c.Dom.get(s);
if(!s||!x){return false
}var t=(u)?q:window;
if(s.tagName||s.length===undefined){return x.call(t,s,q)
}var r=[];
for(var v=0,w=s.length;
v<w;
++v){r[r.length]=x.call(t,s[v],q)
}return r
},getDocumentHeight:function(){var q=(g.compatMode!="CSS1Compat")?g.body.scrollHeight:g.documentElement.scrollHeight;
var r=Math.max(q,c.Dom.getViewportHeight());
return r
},getDocumentWidth:function(){var q=(g.compatMode!="CSS1Compat")?g.body.scrollWidth:g.documentElement.scrollWidth;
var r=Math.max(q,c.Dom.getViewportWidth());
return r
},getViewportHeight:function(){var r=self.innerHeight;
var q=g.compatMode;
if((q||m)&&!b){r=(q=="CSS1Compat")?g.documentElement.clientHeight:g.body.clientHeight
}return r
},getViewportWidth:function(){var r=self.innerWidth;
var q=g.compatMode;
if(q||m){r=(q=="CSS1Compat")?g.documentElement.clientWidth:g.body.clientWidth
}return r
},getAncestorBy:function(r,q){while((r=r.parentNode)){if(a(r,q)){return r
}}return null
},getAncestorByClassName:function(r,s){r=c.Dom.get(r);
if(!r){return null
}var q=function(t){return c.Dom.hasClass(t,s)
};
return c.Dom.getAncestorBy(r,q)
},getAncestorByTagName:function(r,s){r=c.Dom.get(r);
if(!r){return null
}var q=function(t){return t.tagName&&t.tagName.toUpperCase()==s.toUpperCase()
};
return c.Dom.getAncestorBy(r,q)
},getPreviousSiblingBy:function(r,q){while(r){r=r.previousSibling;
if(a(r,q)){return r
}}return null
},getPreviousSibling:function(q){q=c.Dom.get(q);
if(!q){return null
}return c.Dom.getPreviousSiblingBy(q)
},getNextSiblingBy:function(r,q){while(r){r=r.nextSibling;
if(a(r,q)){return r
}}return null
},getNextSibling:function(q){q=c.Dom.get(q);
if(!q){return null
}return c.Dom.getNextSiblingBy(q)
},getFirstChildBy:function(s,q){var r=(a(s.firstChild,q))?s.firstChild:null;
return r||c.Dom.getNextSiblingBy(s.firstChild,q)
},getFirstChild:function(r,q){r=c.Dom.get(r);
if(!r){return null
}return c.Dom.getFirstChildBy(r)
},getLastChildBy:function(s,q){if(!s){return null
}var r=(a(s.lastChild,q))?s.lastChild:null;
return r||c.Dom.getPreviousSiblingBy(s.lastChild,q)
},getLastChild:function(q){q=c.Dom.get(q);
return c.Dom.getLastChildBy(q)
},getChildrenBy:function(s,q){var r=c.Dom.getFirstChildBy(s,q);
var t=r?[r]:[];
c.Dom.getNextSiblingBy(r,function(u){if(!q||q(u)){t[t.length]=u
}return false
});
return t
},getChildren:function(q){q=c.Dom.get(q);
if(!q){}return c.Dom.getChildrenBy(q)
},getDocumentScrollLeft:function(q){q=q||g;
return Math.max(q.documentElement.scrollLeft,q.body.scrollLeft)
},getDocumentScrollTop:function(q){q=q||g;
return Math.max(q.documentElement.scrollTop,q.body.scrollTop)
},insertBefore:function(q,r){q=c.Dom.get(q);
r=c.Dom.get(r);
if(!q||!r||!r.parentNode){return null
}return r.parentNode.insertBefore(q,r)
},insertAfter:function(q,r){q=c.Dom.get(q);
r=c.Dom.get(r);
if(!q||!r||!r.parentNode){return null
}if(r.nextSibling){return r.parentNode.insertBefore(q,r.nextSibling)
}else{return r.parentNode.appendChild(q)
}},getClientRegion:function(){var r=c.Dom.getDocumentScrollTop(),s=c.Dom.getDocumentScrollLeft(),q=c.Dom.getViewportWidth()+s,t=c.Dom.getViewportHeight()+r;
return new c.Region(r,q,t,s)
}};
var l=function(){if(g.documentElement.getBoundingClientRect){return function(r){var q=r.getBoundingClientRect(),s=Math.round;
var t=r.ownerDocument;
return[s(q.left+c.Dom.getDocumentScrollLeft(t)),s(q.top+c.Dom.getDocumentScrollTop(t))]
}
}else{return function(r){var q=[r.offsetLeft,r.offsetTop];
var s=r.offsetParent;
var t=(h&&c.Dom.getStyle(r,"position")=="absolute"&&r.offsetParent==r.ownerDocument.body);
if(s!=r){while(s){q[0]+=s.offsetLeft;
q[1]+=s.offsetTop;
if(!t&&h&&c.Dom.getStyle(s,"position")=="absolute"){t=true
}s=s.offsetParent
}}if(t){q[0]-=r.ownerDocument.body.offsetLeft;
q[1]-=r.ownerDocument.body.offsetTop
}s=r.parentNode;
while(s.tagName&&!p.ROOT_TAG.test(s.tagName)){if(s.scrollTop||s.scrollLeft){q[0]-=s.scrollLeft;
q[1]-=s.scrollTop
}s=s.parentNode
}return q
}
}}()
})();
YAHOO.util.Region=function(c,b,a,d){this.top=c;
this[1]=c;
this.right=b;
this.bottom=a;
this.left=d;
this[0]=d
};
YAHOO.util.Region.prototype.contains=function(a){return(a.left>=this.left&&a.right<=this.right&&a.top>=this.top&&a.bottom<=this.bottom)
};
YAHOO.util.Region.prototype.getArea=function(){return((this.bottom-this.top)*(this.right-this.left))
};
YAHOO.util.Region.prototype.intersect=function(b){var d=Math.max(this.top,b.top);
var c=Math.min(this.right,b.right);
var a=Math.min(this.bottom,b.bottom);
var e=Math.max(this.left,b.left);
if(a>=d&&c>=e){return new YAHOO.util.Region(d,c,a,e)
}else{return null
}};
YAHOO.util.Region.prototype.union=function(b){var d=Math.min(this.top,b.top);
var c=Math.max(this.right,b.right);
var a=Math.max(this.bottom,b.bottom);
var e=Math.min(this.left,b.left);
return new YAHOO.util.Region(d,c,a,e)
};
YAHOO.util.Region.prototype.toString=function(){return("Region {top: "+this.top+", right: "+this.right+", bottom: "+this.bottom+", left: "+this.left+"}")
};
YAHOO.util.Region.getRegion=function(d){var b=YAHOO.util.Dom.getXY(d);
var e=b[1];
var c=b[0]+d.offsetWidth;
var a=b[1]+d.offsetHeight;
var f=b[0];
return new YAHOO.util.Region(e,c,a,f)
};
YAHOO.util.Point=function(a,b){if(YAHOO.lang.isArray(a)){b=a[1];
a=a[0]
}this.x=this.right=this.left=this[0]=a;
this.y=this.top=this.bottom=this[1]=b
};
YAHOO.util.Point.prototype=new YAHOO.util.Region();
YAHOO.register("dom",YAHOO.util.Dom,{version:"2.6.0",build:"1321"});