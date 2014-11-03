(function(){var a=YAHOO.util.Event,c=YAHOO.lang,b=[],d=function(h,e,f){var g;
if(!h||h===f){g=false
}else{g=YAHOO.util.Selector.test(h,e)?h:d(h.parentNode,e,f)
}return g
};
c.augmentObject(a,{_createDelegate:function(f,e,g,h){return function(i){var j=this,n=a.getTarget(i),l=e,p=(j.nodeType===9),q,k,o,m;
if(c.isFunction(e)){q=e(n)
}else{if(c.isString(e)){if(!p){o=j.id;
if(!o){o=a.generateId(j)
}m=("#"+o+" ");
l=(m+e).replace(/,/gi,(","+m))
}if(YAHOO.util.Selector.test(n,l)){q=n
}else{if(YAHOO.util.Selector.test(n,((l.replace(/,/gi," *,"))+" *"))){q=d(n,l,j)
}}}}if(q){k=q;
if(h){if(h===true){k=g
}else{k=h
}}return f.call(k,i,q,j,g)
}}
},delegate:function(f,j,l,g,h,i){var e=j,k,m;
if(c.isString(g)&&!YAHOO.util.Selector){YAHOO.log("Using a CSS selector to define the filtering criteria for a delegated listener requires the Selector Utility.","error","Event");
return false
}if(j=="mouseenter"||j=="mouseleave"){if(!a._createMouseDelegate){YAHOO.log("Delegating a "+j+" event requires the event-mouseenter module.","error","Event");
return false
}e=a._getType(j);
k=a._createMouseDelegate(l,h,i);
m=a._createDelegate(function(p,o,n){return k.call(o,p,n)
},g,h,i)
}else{m=a._createDelegate(l,g,h,i)
}b.push([f,e,l,m]);
return a.on(f,e,m)
},removeDelegate:function(f,j,i){var k=j,h=false,g,e;
if(j=="mouseenter"||j=="mouseleave"){k=a._getType(j)
}g=a._getCacheIndex(b,f,k,i);
if(g>=0){e=b[g]
}if(f&&e){h=a.removeListener(e[0],e[1],e[3]);
if(h){delete b[g][2];
delete b[g][3];
b.splice(g,1)
}}return h
}})
}());
YAHOO.register("event-delegate",YAHOO.util.Event,{version:"2.8.0r4",build:"2449"});