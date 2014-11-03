(function(){var a=YAHOO.util.Event,c=YAHOO.lang,d=[],b=function(e,h,g){var f;
if(!e||e===g){f=false
}else{f=YAHOO.util.Selector.test(e,h)?e:b(e.parentNode,h,g)
}return f
};
c.augmentObject(a,{_createDelegate:function(g,h,f,e){return function(q){var p=this,l=a.getTarget(q),n=h,j=(p.nodeType===9),i,o,k,m;
if(c.isFunction(h)){i=h(l)
}else{if(c.isString(h)){if(!j){k=p.id;
if(!k){k=a.generateId(p)
}m=("#"+k+" ");
n=(m+h).replace(/,/gi,(","+m))
}if(YAHOO.util.Selector.test(l,n)){i=l
}else{if(YAHOO.util.Selector.test(l,((n.replace(/,/gi," *,"))+" *"))){i=b(l,n,p)
}}}}if(i){o=i;
if(e){if(e===true){o=f
}else{o=e
}}return g.call(o,q,i,p,f)
}}
},delegate:function(l,h,f,k,j,i){var m=h,g,e;
if(c.isString(k)&&!YAHOO.util.Selector){return false
}if(h=="mouseenter"||h=="mouseleave"){if(!a._createMouseDelegate){return false
}m=a._getType(h);
g=a._createMouseDelegate(f,j,i);
e=a._createDelegate(function(n,o,p){return g.call(o,n,p)
},k,j,i)
}else{e=a._createDelegate(f,k,j,i)
}d.push([l,m,f,e]);
return a.on(l,m,e)
},removeDelegate:function(j,f,g){var e=f,h=false,i,k;
if(f=="mouseenter"||f=="mouseleave"){e=a._getType(f)
}i=a._getCacheIndex(d,j,e,g);
if(i>=0){k=d[i]
}if(j&&k){h=a.removeListener(k[0],k[1],k[3]);
if(h){delete d[i][2];
delete d[i][3];
d.splice(i,1)
}}return h
}})
}());
YAHOO.register("event-delegate",YAHOO.util.Event,{version:"2.8.0r4",build:"2449"});