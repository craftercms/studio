(function(h){var g=/^\s*|\s*$/g,f,i="B".replace(/A(.)|B/,"$1")==="$1";
var j={majorVersion:"3",minorVersion:"5.10",releaseDate:"2013-10-24",_init:function(){var x=this,b=document,c=navigator,u=c.userAgent,d,v,e,n,p,a;
x.isIE11=u.indexOf("Trident/")!=-1&&(u.indexOf("rv:")!=-1||c.appName.indexOf("Netscape")!=-1);
x.isOpera=h.opera&&opera.buildNumber;
x.isWebKit=/WebKit/.test(u);
x.isIE=!x.isWebKit&&!x.isOpera&&(/MSIE/gi).test(u)&&(/Explorer/gi).test(c.appName)||x.isIE11;
x.isIE6=x.isIE&&/MSIE [56]/.test(u);
x.isIE7=x.isIE&&/MSIE [7]/.test(u);
x.isIE8=x.isIE&&/MSIE [8]/.test(u);
x.isIE9=x.isIE&&/MSIE [9]/.test(u);
x.isGecko=!x.isWebKit&&!x.isIE11&&/Gecko/.test(u);
x.isMac=u.indexOf("Mac")!=-1;
x.isAir=/adobeair/i.test(u);
x.isIDevice=/(iPad|iPhone)/.test(u);
x.isIOS5=x.isIDevice&&u.match(/AppleWebKit\/(\d*)/)[1]>=534;
if(h.tinyMCEPreInit){x.suffix=tinyMCEPreInit.suffix;
x.baseURL=tinyMCEPreInit.base;
x.query=tinyMCEPreInit.query;
return
}x.suffix="";
v=b.getElementsByTagName("base");
for(d=0;
d<v.length;
d++){a=v[d].href;
if(a){if(/^https?:\/\/[^\/]+$/.test(a)){a+="/"
}n=a?a.match(/.*\//)[0]:""
}}function t(k){if(k.src&&/tiny_mce(|_gzip|_jquery|_prototype|_full)(_dev|_src)?.js/.test(k.src)){if(/_(src|dev)\.js/g.test(k.src)){x.suffix="_src"
}if((p=k.src.indexOf("?"))!=-1){x.query=k.src.substring(p+1)
}x.baseURL=k.src.substring(0,k.src.lastIndexOf("/"));
if(n&&x.baseURL.indexOf("://")==-1&&x.baseURL.indexOf("/")!==0){x.baseURL=n+x.baseURL
}return x.baseURL
}return null
}v=b.getElementsByTagName("script");
for(d=0;
d<v.length;
d++){if(t(v[d])){return
}}e=b.getElementsByTagName("head")[0];
if(e){v=e.getElementsByTagName("script");
for(d=0;
d<v.length;
d++){if(t(v[d])){return
}}}return
},is:function(a,b){if(!b){return a!==f
}if(b=="array"&&j.isArray(a)){return true
}return typeof(a)==b
},isArray:Array.isArray||function(a){return Object.prototype.toString.call(a)==="[object Array]"
},makeMap:function(d,a,b){var c;
d=d||[];
a=a||",";
if(typeof(d)=="string"){d=d.split(a)
}b=b||{};
c=d.length;
while(c--){b[d[c]]={}
}return b
},each:function(b,e,c){var a,d;
if(!b){return 0
}c=c||b;
if(b.length!==f){for(a=0,d=b.length;
a<d;
a++){if(e.call(c,b[a],a,b)===false){return 0
}}}else{for(a in b){if(b.hasOwnProperty(a)){if(e.call(c,b[a],a,b)===false){return 0
}}}}return 1
},map:function(c,b){var a=[];
j.each(c,function(d){a.push(b(d))
});
return a
},grep:function(c,b){var a=[];
j.each(c,function(d){if(!b||b(d)){a.push(d)
}});
return a
},inArray:function(c,b){var a,d;
if(c){for(a=0,d=c.length;
a<d;
a++){if(c[a]===b){return a
}}}return -1
},extend:function(o,b){var c,l,d,e=arguments,a;
for(c=1,l=e.length;
c<l;
c++){b=e[c];
for(d in b){if(b.hasOwnProperty(d)){a=b[d];
if(a!==f){o[d]=a
}}}}return o
},trim:function(a){return(a?""+a:"").replace(g,"")
},create:function(a,t,p){var b=this,s,q,e,d,r,c=0;
a=/^((static) )?([\w.]+)(:([\w.]+))?/.exec(a);
e=a[3].match(/(^|\.)(\w+)$/i)[2];
q=b.createNS(a[3].replace(/\.\w+$/,""),p);
if(q[e]){return
}if(a[2]=="static"){q[e]=t;
if(this.onCreate){this.onCreate(a[2],a[3],q[e])
}return
}if(!t[e]){t[e]=function(){};
c=1
}q[e]=t[e];
b.extend(q[e].prototype,t);
if(a[5]){s=b.resolve(a[5]).prototype;
d=a[5].match(/\.(\w+)$/i)[1];
r=q[e];
if(c){q[e]=function(){return s[d].apply(this,arguments)
}
}else{q[e]=function(){this.parent=s[d];
return r.apply(this,arguments)
}
}q[e].prototype[e]=q[e];
b.each(s,function(l,k){q[e].prototype[k]=s[k]
});
b.each(t,function(l,k){if(s[k]){q[e].prototype[k]=function(){this.parent=s[k];
return l.apply(this,arguments)
}
}else{if(k!=e){q[e].prototype[k]=l
}}})
}b.each(t["static"],function(l,k){q[e][k]=l
});
if(this.onCreate){this.onCreate(a[2],a[3],q[e].prototype)
}},walk:function(b,c,a,d){d=d||this;
if(b){if(a){b=b[a]
}j.each(b,function(e,l){if(c.call(d,e,l,a)===false){return false
}j.walk(e,c,a,d)
})
}},createNS:function(a,b){var c,d;
b=b||h;
a=a.split(".");
for(c=0;
c<a.length;
c++){d=a[c];
if(!b[d]){b[d]={}
}b=b[d]
}return b
},resolve:function(a,b){var c,d;
b=b||h;
a=a.split(".");
for(c=0,d=a.length;
c<d;
c++){b=b[a[c]];
if(!b){break
}}return b
},addUnload:function(b,c){var d=this,e;
e=function(){var o=d.unloads,n,k;
if(o){for(k in o){n=o[k];
if(n&&n.func){n.func.call(n.scope,1)
}}if(h.detachEvent){h.detachEvent("onbeforeunload",a);
h.detachEvent("onunload",e)
}else{if(h.removeEventListener){h.removeEventListener("unload",e,false)
}}d.unloads=n=o=w=e=0;
if(h.CollectGarbage){CollectGarbage()
}}};
function a(){var k=document;
function m(){k.detachEvent("onstop",m);
if(e){e()
}k=0
}if(k.readyState=="interactive"){if(k){k.attachEvent("onstop",m)
}h.setTimeout(function(){if(k){k.detachEvent("onstop",m)
}},0)
}}b={func:b,scope:c||this};
if(!d.unloads){if(h.attachEvent){h.attachEvent("onunload",e);
h.attachEvent("onbeforeunload",a)
}else{if(h.addEventListener){h.addEventListener("unload",e,false)
}}d.unloads=[b]
}else{d.unloads.push(b)
}return b
},removeUnload:function(a){var c=this.unloads,b=null;
j.each(c,function(d,e){if(d&&d.func==a){c.splice(e,1);
b=a;
return false
}});
return b
},explode:function(b,a){if(!b||j.is(b,"array")){return b
}return j.map(b.split(a||","),j.trim)
},_addVer:function(a){var b;
if(!this.query){return a
}b=(a.indexOf("?")==-1?"?":"&")+this.query;
if(a.indexOf("#")==-1){return a+b
}return a.replace("#",b+"#")
},_replace:function(a,c,b){if(i){return b.replace(a,function(){var d=c,m=arguments,e;
for(e=0;
e<m.length-2;
e++){if(m[e]===f){d=d.replace(new RegExp("\\$"+e,"g"),"")
}else{d=d.replace(new RegExp("\\$"+e,"g"),m[e])
}}return d
})
}return b.replace(a,c)
}};
j._init();
h.tinymce=h.tinyMCE=j
})(window);
tinymce.create("tinymce.util.Dispatcher",{scope:null,listeners:null,inDispatch:false,Dispatcher:function(b){this.scope=b||this;
this.listeners=[]
},add:function(c,d){this.listeners.push({cb:c,scope:d||this.scope});
return c
},addToTop:function(g,e){var f=this,h={cb:g,scope:e||f.scope};
if(f.inDispatch){f.listeners=[h].concat(f.listeners)
}else{f.listeners.unshift(h)
}return g
},remove:function(f){var d=this.listeners,e=null;
tinymce.each(d,function(a,b){if(f==a.cb){e=a;
d.splice(b,1);
return false
}});
return e
},dispatch:function(){var h=this,j,g=arguments,l,k=h.listeners,i;
h.inDispatch=true;
for(l=0;
l<k.length;
l++){i=k[l];
j=i.cb.apply(i.scope,g.length>0?g:[i.scope]);
if(j===false){break
}}h.inDispatch=false;
return j
}});
(function(){var b=tinymce.each;
tinymce.create("tinymce.util.URI",{URI:function(m,k){var l=this,a,n,o,j;
m=tinymce.trim(m);
k=l.settings=k||{};
if(/^([\w\-]+):([^\/]{2})/i.test(m)||/^\s*#/.test(m)){l.source=m;
return
}if(m.indexOf("/")===0&&m.indexOf("//")!==0){m=(k.base_uri?k.base_uri.protocol||"http":"http")+"://mce_host"+m
}if(!/^[\w\-]*:?\/\//.test(m)){j=k.base_uri?k.base_uri.path:new tinymce.util.URI(location.href).directory;
m=((k.base_uri&&k.base_uri.protocol)||"http")+"://mce_host"+l.toAbsPath(j,m)
}m=m.replace(/@@/g,"(mce_at)");
m=/^(?:(?![^:@]+:[^:@\/]*@)([^:\/?#.]+):)?(?:\/\/)?((?:(([^:@\/]*):?([^:@\/]*))?@)?([^:\/?#]*)(?::(\d*))?)(((\/(?:[^?#](?![^?#\/]*\.[^?#\/.]+(?:[?#]|$)))*\/?)?([^?#\/]*))(?:\?([^#]*))?(?:#(.*))?)/.exec(m);
b(["source","protocol","authority","userInfo","user","password","host","port","relative","path","directory","file","query","anchor"],function(c,e){var d=m[e];
if(d){d=d.replace(/\(mce_at\)/g,"@@")
}l[c]=d
});
o=k.base_uri;
if(o){if(!l.protocol){l.protocol=o.protocol
}if(!l.userInfo){l.userInfo=o.userInfo
}if(!l.port&&l.host==="mce_host"){l.port=o.port
}if(!l.host||l.host==="mce_host"){l.host=o.host
}l.source=""
}},setPath:function(d){var a=this;
d=/^(.*?)\/?(\w+)?$/.exec(d);
a.path=d[0];
a.directory=d[1];
a.file=d[2];
a.source="";
a.getURI()
},toRelative:function(a){var i=this,g;
if(a==="./"){return a
}a=new tinymce.util.URI(a,{base_uri:i});
if((a.host!="mce_host"&&i.host!=a.host&&a.host)||i.port!=a.port||i.protocol!=a.protocol){return a.getURI()
}var j=i.getURI(),h=a.getURI();
if(j==h||(j.charAt(j.length-1)=="/"&&j.substr(0,j.length-1)==h)){return j
}g=i.toRelPath(i.path,a.path);
if(a.query){g+="?"+a.query
}if(a.anchor){g+="#"+a.anchor
}return g
},toAbsolute:function(a,d){a=new tinymce.util.URI(a,{base_uri:this});
return a.getURI(this.host==a.host&&this.protocol==a.protocol?d:0)
},toRelPath:function(j,i){var n,k=0,m="",l,a;
j=j.substring(0,j.lastIndexOf("/"));
j=j.split("/");
n=i.split("/");
if(j.length>=n.length){for(l=0,a=j.length;
l<a;
l++){if(l>=n.length||j[l]!=n[l]){k=l+1;
break
}}}if(j.length<n.length){for(l=0,a=n.length;
l<a;
l++){if(l>=j.length||j[l]!=n[l]){k=l+1;
break
}}}if(k===1){return i
}for(l=0,a=j.length-(k-1);
l<a;
l++){m+="../"
}for(l=k-1,a=n.length;
l<a;
l++){if(l!=k-1){m+="/"+n[l]
}else{m+=n[l]
}}return m
},toAbsPath:function(l,k){var n,a=0,i=[],m,j;
m=/\/$/.test(k)?"/":"";
l=l.split("/");
k=k.split("/");
b(l,function(c){if(c){i.push(c)
}});
l=i;
for(n=k.length-1,i=[];
n>=0;
n--){if(k[n].length===0||k[n]==="."){continue
}if(k[n]===".."){a++;
continue
}if(a>0){a--;
continue
}i.push(k[n])
}n=l.length-a;
if(n<=0){j=i.reverse().join("/")
}else{j=l.slice(0,n).join("/")+"/"+i.reverse().join("/")
}if(j.indexOf("/")!==0){j="/"+j
}if(m&&j.lastIndexOf("/")!==j.length-1){j+=m
}return j
},getURI:function(e){var f,a=this;
if(!a.source||e){f="";
if(!e){if(a.protocol){f+=a.protocol+"://"
}if(a.userInfo){f+=a.userInfo+"@"
}if(a.host){f+=a.host
}if(a.port){f+=":"+a.port
}}if(a.path){f+=a.path
}if(a.query){f+="?"+a.query
}if(a.anchor){f+="#"+a.anchor
}a.source=f
}return a.source
}})
})();
(function(){var b=tinymce.each;
tinymce.create("static tinymce.util.Cookie",{getHash:function(e){var a=this.get(e),f;
if(a){b(a.split("&"),function(c){c=c.split("=");
f=f||{};
f[unescape(c[0])]=unescape(c[1])
})
}return f
},setHash:function(d,a,l,m,e,n){var k="";
b(a,function(c,f){k+=(!k?"":"&")+escape(f)+"="+escape(c)
});
this.set(d,k,l,m,e,n)
},get:function(a){var c=document.cookie,e,j=a+"=",k;
if(!c){return
}k=c.indexOf("; "+j);
if(k==-1){k=c.indexOf(j);
if(k!==0){return null
}}else{k+=2
}e=c.indexOf(";",k);
if(e==-1){e=c.length
}return unescape(c.substring(k+j.length,e))
},set:function(d,a,j,k,e,l){document.cookie=d+"="+escape(a)+((j)?"; expires="+j.toGMTString():"")+((k)?"; path="+escape(k):"")+((e)?"; domain="+e:"")+((l)?"; secure":"")
},remove:function(h,f,g){var a=new Date();
a.setTime(a.getTime()-1000);
this.set(h,"",a,f,g)
}})
})();
(function(){function serialize(o,quote){var i,v,t,name;
quote=quote||'"';
if(o==null){return"null"
}t=typeof o;
if(t=="string"){v="\bb\tt\nn\ff\rr\"\"''\\\\";
return quote+o.replace(/([\u0080-\uFFFF\x00-\x1f\"\'\\])/g,function(a,b){if(quote==='"'&&a==="'"){return a
}i=v.indexOf(b);
if(i+1){return"\\"+v.charAt(i+1)
}a=b.charCodeAt().toString(16);
return"\\u"+"0000".substring(a.length)+a
})+quote
}if(t=="object"){if(o.hasOwnProperty&&Object.prototype.toString.call(o)==="[object Array]"){for(i=0,v="[";
i<o.length;
i++){v+=(i>0?",":"")+serialize(o[i],quote)
}return v+"]"
}v="{";
for(name in o){if(o.hasOwnProperty(name)){v+=typeof o[name]!="function"?(v.length>1?","+quote:quote)+name+quote+":"+serialize(o[name],quote):""
}}return v+"}"
}return""+o
}tinymce.util.JSON={serialize:serialize,parse:function(s){try{return eval("("+s+")")
}catch(ex){}}}
})();
tinymce.create("static tinymce.util.XHR",{send:function(k){var i,m,c=window,j=0;
function l(){if(!k.async||i.readyState==4||j++>10000){if(k.success&&j<10000&&i.status==200){k.success.call(k.success_scope,""+i.responseText,i,k)
}else{if(k.error){k.error.call(k.error_scope,j>10000?"TIMED_OUT":"GENERAL",i,k)
}}i=null
}else{c.setTimeout(l,10)
}}k.scope=k.scope||this;
k.success_scope=k.success_scope||k.scope;
k.error_scope=k.error_scope||k.scope;
k.async=k.async===false?false:true;
k.data=k.data||"";
function n(a){i=0;
try{i=new ActiveXObject(a)
}catch(b){}return i
}i=c.XMLHttpRequest?new XMLHttpRequest():n("Microsoft.XMLHTTP")||n("Msxml2.XMLHTTP");
if(i){if(i.overrideMimeType){i.overrideMimeType(k.content_type)
}i.open(k.type||(k.data?"POST":"GET"),k.url,k.async);
if(k.content_type){i.setRequestHeader("Content-Type",k.content_type)
}i.setRequestHeader("X-Requested-With","XMLHttpRequest");
i.send(k.data);
if(!k.async){return l()
}m=c.setTimeout(l,10)
}}});
(function(){var f=tinymce.extend,d=tinymce.util.JSON,e=tinymce.util.XHR;
tinymce.create("tinymce.util.JSONRequest",{JSONRequest:function(a){this.settings=f({},a);
this.count=0
},send:function(a){var b=a.error,c=a.success;
a=f(this.settings,a);
a.success=function(i,j){i=d.parse(i);
if(typeof(i)=="undefined"){i={error:"JSON Parse error."}
}if(i.error){b.call(a.error_scope||a.scope,i.error,j)
}else{c.call(a.success_scope||a.scope,i.result)
}};
a.error=function(i,j){if(b){b.call(a.error_scope||a.scope,i,j)
}};
a.data=d.serialize({id:a.id||"c"+(this.count++),method:a.method,params:a.params});
a.content_type="application/json";
e.send(a)
},"static":{sendRPC:function(a){return new tinymce.util.JSONRequest().send(a)
}}})
}());
(function(b){b.VK={BACKSPACE:8,DELETE:46,DOWN:40,ENTER:13,LEFT:37,RIGHT:39,SPACEBAR:32,TAB:9,UP:38,modifierPressed:function(a){return a.shiftKey||a.ctrlKey||a.altKey
},metaKeyPressed:function(a){return b.isMac?a.metaKey:a.ctrlKey&&!a.altKey
}}
})(tinymce);
tinymce.util.Quirks=function(ax){var an=tinymce.VK,ar=an.BACKSPACE,am=an.DELETE,at=ax.dom,ak=ax.selection,P=ax.settings,aa=ax.parser,ah=ax.serializer,S=tinymce.each;
function W(a,b){try{ax.getDoc().execCommand(a,false,b)
}catch(c){}}function ai(){var a=ax.getDoc().documentMode;
return a?a:6
}function X(a){return a.isDefaultPrevented()
}function N(){function a(f){var j,h,k,e,i,g,d;
function c(){if(i.nodeType==3){if(f&&g==i.length){return true
}if(!f&&g===0){return true
}}}j=ak.getRng();
var b=[j.startContainer,j.startOffset,j.endContainer,j.endOffset];
if(!j.collapsed){f=true
}i=j[(f?"start":"end")+"Container"];
g=j[(f?"start":"end")+"Offset"];
if(i.nodeType==3){h=at.getParent(j.startContainer,at.isBlock);
if(f){h=at.getNext(h,at.isBlock)
}if(h&&(c()||!j.collapsed)){k=at.create("em",{id:"__mceDel"});
S(tinymce.grep(h.childNodes),function(l){k.appendChild(l)
});
h.appendChild(k)
}}j=at.createRng();
j.setStart(b[0],b[1]);
j.setEnd(b[2],b[3]);
ak.setRng(j);
ax.getDoc().execCommand(f?"ForwardDelete":"Delete",false,null);
if(k){e=ak.getBookmark();
while(d=at.get("__mceDel")){at.remove(d,true)
}ak.moveToBookmark(e)
}}ax.onKeyDown.add(function(d,b){var c;
c=b.keyCode==am;
if(!X(b)&&(c||b.keyCode==ar)&&!an.modifierPressed(b)){b.preventDefault();
a(c)
}});
ax.addCommand("Delete",function(){a()
})
}function af(){function b(d){var e=at.create("body");
var c=d.cloneContents();
e.appendChild(c);
return ak.serializer.serialize(e,{format:"html"})
}function a(f){var d=b(f);
var c=at.createRng();
c.selectNode(ax.getBody());
var e=b(c);
return d===e
}ax.onKeyDown.add(function(e,c){var d=c.keyCode,f;
if(!X(c)&&(d==am||d==ar)){f=e.selection.isCollapsed();
if(f&&!at.isEmpty(e.getBody())){return
}if(tinymce.isIE&&!f){return
}if(!f&&!a(e.selection.getRng())){return
}e.setContent("");
e.selection.setCursorLocation(e.getBody(),0);
e.nodeChanged()
}})
}function O(){ax.onKeyDown.add(function(b,a){if(!X(a)&&a.keyCode==65&&an.metaKeyPressed(a)){a.preventDefault();
b.execCommand("SelectAll")
}})
}function M(){if(!ax.settings.content_editable){at.bind(ax.getDoc(),"focusin",function(a){ak.setRng(ak.getRng())
});
at.bind(ax.getDoc(),"mousedown",function(a){if(a.target==ax.getDoc().documentElement){ax.getWin().focus();
ak.setRng(ak.getRng())
}})
}}function V(){ax.onKeyDown.add(function(d,a){if(!X(a)&&a.keyCode===ar){if(ak.isCollapsed()&&ak.getRng(true).startOffset===0){var b=ak.getNode();
var c=b.previousSibling;
if(c&&c.nodeName&&c.nodeName.toLowerCase()==="hr"){at.remove(c);
tinymce.dom.Event.cancel(a)
}}}})
}function Y(){if(!Range.prototype.getClientRects){ax.onMouseDown.add(function(b,a){if(!X(a)&&a.target.nodeName==="HTML"){var c=b.getBody();
c.blur();
setTimeout(function(){c.focus()
},0)
}})
}}function ap(){ax.onClick.add(function(b,a){a=a.target;
if(/^(IMG|HR)$/.test(a.nodeName)){ak.getSel().setBaseAndExtent(a,0,a,1)
}if(a.nodeName=="A"&&at.hasClass(a,"mceItemAnchor")){ak.select(a)
}b.nodeChanged()
})
}function av(){function b(){var d=at.getAttribs(ak.getStart().cloneNode(false));
return function(){var e=ak.getStart();
if(e!==ax.getBody()){at.setAttrib(e,"style",null);
S(d,function(f){e.setAttributeNode(f.cloneNode(true))
})
}}
}function c(){return !ak.isCollapsed()&&at.getParent(ak.getStart(),at.isBlock)!=at.getParent(ak.getEnd(),at.isBlock)
}function a(e,d){d.preventDefault();
return false
}ax.onKeyPress.add(function(f,d){var e;
if(!X(d)&&(d.keyCode==8||d.keyCode==46)&&c()){e=b();
f.getDoc().execCommand("delete",false,null);
e();
d.preventDefault();
return false
}});
at.bind(ax.getDoc(),"cut",function(d){var e;
if(!X(d)&&c()){e=b();
ax.onKeyUp.addToTop(a);
setTimeout(function(){e();
ax.onKeyUp.remove(a)
},0)
}})
}function aw(){var a,b;
at.bind(ax.getDoc(),"selectionchange",function(){if(b){clearTimeout(b);
b=0
}b=window.setTimeout(function(){var c=ak.getRng();
if(!a||!tinymce.dom.RangeUtils.compareRanges(c,a)){ax.nodeChanged();
a=c
}},50)
})
}function Z(){document.body.setAttribute("role","application")
}function ac(){ax.onKeyDown.add(function(c,a){if(!X(a)&&a.keyCode===ar){if(ak.isCollapsed()&&ak.getRng(true).startOffset===0){var b=ak.getNode().previousSibling;
if(b&&b.nodeName&&b.nodeName.toLowerCase()==="table"){return tinymce.dom.Event.cancel(a)
}}}})
}function U(){if(ai()>7){return
}W("RespectVisibilityInDesign",true);
ax.contentStyles.push(".mceHideBrInPre pre br {display: none}");
at.addClass(ax.getBody(),"mceHideBrInPre");
aa.addNodeFilter("pre",function(g,e){var d=g.length,b,f,a,c;
while(d--){b=g[d].getAll("br");
f=b.length;
while(f--){a=b[f];
c=a.prev;
if(c&&c.type===3&&c.value.charAt(c.value-1)!="\n"){c.value+="\n"
}else{a.parent.insert(new tinymce.html.Node("#text",3),a,true).value="\n"
}}}});
ah.addNodeFilter("pre",function(g,e){var d=g.length,b,f,a,c;
while(d--){b=g[d].getAll("br");
f=b.length;
while(f--){a=b[f];
c=a.prev;
if(c&&c.type==3){c.value=c.value.replace(/\r?\n$/,"")
}}}})
}function aq(){at.bind(ax.getBody(),"mouseup",function(a){var b,c=ak.getNode();
if(c.nodeName=="IMG"){if(b=at.getStyle(c,"width")){at.setAttrib(c,"width",b.replace(/[^0-9%]+/g,""));
at.setStyle(c,"width","")
}if(b=at.getStyle(c,"height")){at.setAttrib(c,"height",b.replace(/[^0-9%]+/g,""));
at.setStyle(c,"height","")
}}})
}function au(){ax.onKeyDown.add(function(c,b){var d,i,h,f,e,a,g;
d=b.keyCode==am;
if(!X(b)&&(d||b.keyCode==ar)&&!an.modifierPressed(b)){i=ak.getRng();
h=i.startContainer;
f=i.startOffset;
g=i.collapsed;
if(h.nodeType==3&&h.nodeValue.length>0&&((f===0&&!g)||(g&&f===(d?0:1)))){a=h.previousSibling;
if(a&&a.nodeName=="IMG"){return
}nonEmptyElements=c.schema.getNonEmptyElements();
b.preventDefault();
e=at.create("br",{id:"__tmp"});
h.parentNode.insertBefore(e,h);
c.getDoc().execCommand(d?"ForwardDelete":"Delete",false,null);
h=ak.getRng().startContainer;
a=h.previousSibling;
if(a&&a.nodeType==1&&!at.isBlock(a)&&at.isEmpty(a)&&!nonEmptyElements[a.nodeName.toLowerCase()]){at.remove(a)
}at.remove("__tmp")
}}})
}function Q(){ax.onKeyDown.add(function(c,b){var e,f,a,g,d;
if(X(b)||b.keyCode!=an.BACKSPACE){return
}e=ak.getRng();
f=e.startContainer;
a=e.startOffset;
g=at.getRoot();
d=f;
if(!e.collapsed||a!==0){return
}while(d&&d.parentNode&&d.parentNode.firstChild==d&&d.parentNode!=g){d=d.parentNode
}if(d.tagName==="BLOCKQUOTE"){c.formatter.toggle("blockquote",null,d);
e=at.createRng();
e.setStart(f,0);
e.setEnd(f,0);
ak.setRng(e)
}})
}function R(){function a(){ax._refreshContentEditable();
W("StyleWithCSS",false);
W("enableInlineTableEditing",false);
if(!P.object_resizing){W("enableObjectResizing",false)
}}if(!P.readonly){ax.onBeforeExecCommand.add(a);
ax.onMouseDown.add(a)
}}function ad(){function a(c,b){S(at.select("a"),function(d){var f=d.parentNode,e=at.getRoot();
if(f.lastChild===d){while(f&&!at.isBlock(f)){if(f.parentNode.lastChild!==f||f===e){return
}f=f.parentNode
}at.add(f,"br",{"data-mce-bogus":1})
}})
}ax.onExecCommand.add(function(c,b){if(b==="CreateLink"){a(c)
}});
ax.onSetContent.add(ak.onSetContent.add(a))
}function aj(){if(P.forced_root_block){ax.onInit.add(function(){W("DefaultParagraphSeparator",P.forced_root_block)
})
}}function ag(){function a(b,c){if(!b||!c.initial){ax.execCommand("mceRepaint")
}}ax.onUndo.add(a);
ax.onRedo.add(a);
ax.onSetContent.add(a)
}function ao(){ax.onKeyDown.add(function(b,a){var c;
if(!X(a)&&a.keyCode==ar){c=b.getDoc().selection.createRange();
if(c&&c.item){a.preventDefault();
b.undoManager.beforeChange();
at.remove(c.item(0));
b.undoManager.add()
}}})
}function ae(){var a;
if(ai()>=10){a="";
S("p div h1 h2 h3 h4 h5 h6".split(" "),function(c,b){a+=(b>0?",":"")+c+":empty"
});
ax.contentStyles.push(a+"{padding-right: 1px !important}")
}}function ab(){var l,m,p,n,a,r,t,q,k,j,s,c,d,b=document,f=ax.getDoc();
if(!P.object_resizing||P.webkit_fake_resize===false){return
}W("enableObjectResizing",false);
s={n:[0.5,0,0,-1],e:[1,0.5,1,0],s:[0.5,1,0,1],w:[0,0.5,-1,0],nw:[0,0,-1,-1],ne:[1,0,1,-1],se:[1,1,1,1],sw:[0,1,-1,1]};
function h(u){var v,x;
v=u.screenX-r;
x=u.screenY-t;
c=v*a[2]+q;
d=x*a[3]+k;
c=c<5?5:c;
d=d<5?5:d;
if(an.modifierPressed(u)||(p.nodeName=="IMG"&&a[2]*a[3]!==0)){c=Math.round(d/j);
d=Math.round(c*j)
}at.setStyles(n,{width:c,height:d});
if(a[2]<0&&n.clientWidth<=c){at.setStyle(n,"left",l+(q-c))
}if(a[3]<0&&n.clientHeight<=d){at.setStyle(n,"top",m+(k-d))
}}function o(){function u(x,v){if(v){if(p.style[x]||!ax.schema.isValid(p.nodeName.toLowerCase(),x)){at.setStyle(p,x,v)
}else{at.setAttrib(p,x,v)
}}}u("width",c);
u("height",d);
at.unbind(f,"mousemove",h);
at.unbind(f,"mouseup",o);
if(b!=f){at.unbind(b,"mousemove",h);
at.unbind(b,"mouseup",o)
}at.remove(n);
i(p)
}function i(y){var v,u,x;
g();
v=at.getPos(y);
l=v.x;
m=v.y;
u=y.offsetWidth;
x=y.offsetHeight;
if(p!=y){p=y;
c=d=0
}S(s,function(z,B){var A;
A=at.get("mceResizeHandle"+B);
if(!A){A=at.add(f.documentElement,"div",{id:"mceResizeHandle"+B,"class":"mceResizeHandle",style:"cursor:"+B+"-resize; margin:0; padding:0"});
at.bind(A,"mousedown",function(C){C.preventDefault();
o();
r=C.screenX;
t=C.screenY;
q=p.clientWidth;
k=p.clientHeight;
j=k/q;
a=z;
n=p.cloneNode(true);
at.addClass(n,"mceClonedResizable");
at.setStyles(n,{left:l,top:m,margin:0});
f.documentElement.appendChild(n);
at.bind(f,"mousemove",h);
at.bind(f,"mouseup",o);
if(b!=f){at.bind(b,"mousemove",h);
at.bind(b,"mouseup",o)
}})
}else{at.show(A)
}at.setStyles(A,{left:(u*z[0]+l)-(A.offsetWidth/2),top:(x*z[1]+m)-(A.offsetHeight/2)})
});
if(!tinymce.isOpera&&p.nodeName=="IMG"){p.setAttribute("data-mce-selected","1")
}}function g(){if(p){p.removeAttribute("data-mce-selected")
}for(var u in s){at.hide("mceResizeHandle"+u)
}}ax.contentStyles.push(".mceResizeHandle {position: absolute;border: 1px solid black;background: #FFF;width: 5px;height: 5px;z-index: 10000}.mceResizeHandle:hover {background: #000}img[data-mce-selected] {outline: 1px solid black}img.mceClonedResizable, table.mceClonedResizable {position: absolute;outline: 1px dashed black;opacity: .5;z-index: 10000}");
function e(){var u=at.getParent(ak.getNode(),"table,img");
S(at.select("img[data-mce-selected]"),function(v){v.removeAttribute("data-mce-selected")
});
if(u){i(u)
}else{g()
}}ax.onNodeChange.add(e);
at.bind(f,"selectionchange",e);
ax.serializer.addAttributeFilter("data-mce-selected",function(x,v){var u=x.length;
while(u--){x[u].attr(v,null)
}})
}function T(){if(ai()<9){aa.addNodeFilter("noscript",function(d){var c=d.length,b,a;
while(c--){b=d[c];
a=b.firstChild;
if(a){b.attr("data-mce-innertext",a.value)
}}});
ah.addNodeFilter("noscript",function(e){var d=e.length,c,a,b;
while(d--){c=e[d];
a=e[d].firstChild;
if(a){a.value=tinymce.html.Entities.decode(a.value)
}else{b=c.attributes.map["data-mce-innertext"];
if(b){c.attr("data-mce-innertext",null);
a=new tinymce.html.Node("#text",3);
a.value=b;
a.raw=true;
c.append(a)
}}}})
}}function al(){ax.contentStyles.push("body {min-height: 100px}");
ax.onClick.add(function(b,a){if(a.target.nodeName=="HTML"){ax.execCommand("SelectAll");
ax.selection.collapse(true);
ax.nodeChanged()
}})
}ac();
Q();
af();
if(tinymce.isWebKit){au();
N();
M();
ap();
aj();
if(tinymce.isIDevice){aw()
}else{ab();
O()
}}if(tinymce.isIE&&!tinymce.isIE11){V();
Z();
U();
aq();
ao();
ae();
T()
}if(tinymce.isIE11){al()
}if(tinymce.isGecko&&!tinymce.isIE11){V();
Y();
av();
R();
ad();
ag()
}if(tinymce.isOpera){ab()
}};
(function(m){var v,p,s,l=/[&<>\"\u007E-\uD7FF\uE000-\uFFEF]|[\uD800-\uDBFF][\uDC00-\uDFFF]/g,u=/[<>&\u007E-\uD7FF\uE000-\uFFEF]|[\uD800-\uDBFF][\uDC00-\uDFFF]/g,q=/[<>&\"\']/g,t=/&(#x|#)?([\w]+);/g,n={128:"\u20AC",130:"\u201A",131:"\u0192",132:"\u201E",133:"\u2026",134:"\u2020",135:"\u2021",136:"\u02C6",137:"\u2030",138:"\u0160",139:"\u2039",140:"\u0152",142:"\u017D",145:"\u2018",146:"\u2019",147:"\u201C",148:"\u201D",149:"\u2022",150:"\u2013",151:"\u2014",152:"\u02DC",153:"\u2122",154:"\u0161",155:"\u203A",156:"\u0153",158:"\u017E",159:"\u0178"};
p={'"':"&quot;","'":"&#39;","<":"&lt;",">":"&gt;","&":"&amp;"};
s={"&lt;":"<","&gt;":">","&amp;":"&","&quot;":'"',"&apos;":"'"};
function o(b){var a;
a=document.createElement("div");
a.innerHTML=b;
return a.textContent||a.innerText||b
}function r(a,d){var f,e,b,c={};
if(a){a=a.split(",");
d=d||10;
for(f=0;
f<a.length;
f+=2){e=String.fromCharCode(parseInt(a[f],d));
if(!p[e]){b="&"+a[f+1]+";";
c[e]=b;
c[b]=e
}}return c
}}v=r("50,nbsp,51,iexcl,52,cent,53,pound,54,curren,55,yen,56,brvbar,57,sect,58,uml,59,copy,5a,ordf,5b,laquo,5c,not,5d,shy,5e,reg,5f,macr,5g,deg,5h,plusmn,5i,sup2,5j,sup3,5k,acute,5l,micro,5m,para,5n,middot,5o,cedil,5p,sup1,5q,ordm,5r,raquo,5s,frac14,5t,frac12,5u,frac34,5v,iquest,60,Agrave,61,Aacute,62,Acirc,63,Atilde,64,Auml,65,Aring,66,AElig,67,Ccedil,68,Egrave,69,Eacute,6a,Ecirc,6b,Euml,6c,Igrave,6d,Iacute,6e,Icirc,6f,Iuml,6g,ETH,6h,Ntilde,6i,Ograve,6j,Oacute,6k,Ocirc,6l,Otilde,6m,Ouml,6n,times,6o,Oslash,6p,Ugrave,6q,Uacute,6r,Ucirc,6s,Uuml,6t,Yacute,6u,THORN,6v,szlig,70,agrave,71,aacute,72,acirc,73,atilde,74,auml,75,aring,76,aelig,77,ccedil,78,egrave,79,eacute,7a,ecirc,7b,euml,7c,igrave,7d,iacute,7e,icirc,7f,iuml,7g,eth,7h,ntilde,7i,ograve,7j,oacute,7k,ocirc,7l,otilde,7m,ouml,7n,divide,7o,oslash,7p,ugrave,7q,uacute,7r,ucirc,7s,uuml,7t,yacute,7u,thorn,7v,yuml,ci,fnof,sh,Alpha,si,Beta,sj,Gamma,sk,Delta,sl,Epsilon,sm,Zeta,sn,Eta,so,Theta,sp,Iota,sq,Kappa,sr,Lambda,ss,Mu,st,Nu,su,Xi,sv,Omicron,t0,Pi,t1,Rho,t3,Sigma,t4,Tau,t5,Upsilon,t6,Phi,t7,Chi,t8,Psi,t9,Omega,th,alpha,ti,beta,tj,gamma,tk,delta,tl,epsilon,tm,zeta,tn,eta,to,theta,tp,iota,tq,kappa,tr,lambda,ts,mu,tt,nu,tu,xi,tv,omicron,u0,pi,u1,rho,u2,sigmaf,u3,sigma,u4,tau,u5,upsilon,u6,phi,u7,chi,u8,psi,u9,omega,uh,thetasym,ui,upsih,um,piv,812,bull,816,hellip,81i,prime,81j,Prime,81u,oline,824,frasl,88o,weierp,88h,image,88s,real,892,trade,89l,alefsym,8cg,larr,8ch,uarr,8ci,rarr,8cj,darr,8ck,harr,8dl,crarr,8eg,lArr,8eh,uArr,8ei,rArr,8ej,dArr,8ek,hArr,8g0,forall,8g2,part,8g3,exist,8g5,empty,8g7,nabla,8g8,isin,8g9,notin,8gb,ni,8gf,prod,8gh,sum,8gi,minus,8gn,lowast,8gq,radic,8gt,prop,8gu,infin,8h0,ang,8h7,and,8h8,or,8h9,cap,8ha,cup,8hb,int,8hk,there4,8hs,sim,8i5,cong,8i8,asymp,8j0,ne,8j1,equiv,8j4,le,8j5,ge,8k2,sub,8k3,sup,8k4,nsub,8k6,sube,8k7,supe,8kl,oplus,8kn,otimes,8l5,perp,8m5,sdot,8o8,lceil,8o9,rceil,8oa,lfloor,8ob,rfloor,8p9,lang,8pa,rang,9ea,loz,9j0,spades,9j3,clubs,9j5,hearts,9j6,diams,ai,OElig,aj,oelig,b0,Scaron,b1,scaron,bo,Yuml,m6,circ,ms,tilde,802,ensp,803,emsp,809,thinsp,80c,zwnj,80d,zwj,80e,lrm,80f,rlm,80j,ndash,80k,mdash,80o,lsquo,80p,rsquo,80q,sbquo,80s,ldquo,80t,rdquo,80u,bdquo,810,dagger,811,Dagger,81g,permil,81p,lsaquo,81q,rsaquo,85c,euro",32);
m.html=m.html||{};
m.html.Entities={encodeRaw:function(a,b){return a.replace(b?l:u,function(c){return p[c]||c
})
},encodeAllRaw:function(a){return(""+a).replace(q,function(b){return p[b]||b
})
},encodeNumeric:function(a,b){return a.replace(b?l:u,function(c){if(c.length>1){return"&#"+(((c.charCodeAt(0)-55296)*1024)+(c.charCodeAt(1)-56320)+65536)+";"
}return p[c]||"&#"+c.charCodeAt(0)+";"
})
},encodeNamed:function(c,b,a){a=a||v;
return c.replace(b?l:u,function(d){return p[d]||a[d]||d
})
},getEncodeFunc:function(b,d){var c=m.html.Entities;
d=r(d)||v;
function a(f,g){return f.replace(g?l:u,function(h){return p[h]||d[h]||"&#"+h.charCodeAt(0)+";"||h
})
}function e(f,g){return c.encodeNamed(f,g,d)
}b=m.makeMap(b.replace(/\+/g,","));
if(b.named&&b.numeric){return a
}if(b.named){if(d){return e
}return c.encodeNamed
}if(b.numeric){return c.encodeNumeric
}return c.encodeRaw
},decode:function(a){return a.replace(t,function(d,b,c){if(b){c=parseInt(c,b.length===2?16:10);
if(c>65535){c-=65536;
return String.fromCharCode(55296+(c>>10),56320+(c&1023))
}else{return n[c]||String.fromCharCode(c)
}}return s[d]||v[d]||o(d)
})
}}
})(tinymce);
tinymce.html.Styles=function(u,s){var o=/rgb\s*\(\s*([0-9]+)\s*,\s*([0-9]+)\s*,\s*([0-9]+)\s*\)/gi,q=/(?:url(?:(?:\(\s*\"([^\"]+)\"\s*\))|(?:\(\s*\'([^\']+)\'\s*\))|(?:\(\s*([^)\s]+)\s*\))))|(?:\'([^\']+)\')|(?:\"([^\"]+)\")/gi,x=/\s*([^:]+):\s*([^;]+);?/g,n=/\s+$/,i=/rgb/,t,r,y={},p;
u=u||{};
p="\\\" \\' \\; \\: ; : \uFEFF".split(" ");
for(r=0;
r<p.length;
r++){y[p[r]]="\uFEFF"+r;
y["\uFEFF"+r]=p[r]
}function v(e,b,c,a){function d(f){f=parseInt(f).toString(16);
return f.length>1?f:"0"+f
}return"#"+d(b)+d(c)+d(a)
}return{toHex:function(a){return a.replace(o,v)
},parse:function(C){var h={},b,f,k,a,l=u.url_converter,j=u.url_converter_scope||this;
function c(I,z){var A,J,K,H;
if(h["border-image"]==="none"){delete h["border-image"]
}A=h[I+"-top"+z];
if(!A){return
}J=h[I+"-right"+z];
if(A!=J){return
}K=h[I+"-bottom"+z];
if(J!=K){return
}H=h[I+"-left"+z];
if(K!=H){return
}h[I+z]=H;
delete h[I+"-top"+z];
delete h[I+"-right"+z];
delete h[I+"-bottom"+z];
delete h[I+"-left"+z]
}function m(A){var z=h[A],E;
if(!z||z.indexOf(" ")<0){return
}z=z.split(" ");
E=z.length;
while(E--){if(z[E]!==z[0]){return false
}}h[A]=z[0];
return true
}function e(A,F,G,z){if(!m(F)){return
}if(!m(G)){return
}if(!m(z)){return
}h[A]=h[F]+" "+h[G]+" "+h[z];
delete h[F];
delete h[G];
delete h[z]
}function B(z){a=true;
return y[z]
}function g(z,A){if(a){z=z.replace(/\uFEFF[0-9]/g,function(E){return y[E]
})
}if(!A){z=z.replace(/\\([\'\";:])/g,"$1")
}return z
}function d(J,K,A,H,z,I){z=z||I;
if(z){z=g(z);
return"'"+z.replace(/\'/g,"\\'")+"'"
}K=g(K||A||H);
if(l){K=l.call(j,K,"style")
}return"url('"+K.replace(/\'/g,"\\'")+"')"
}if(C){C=C.replace(/\\[\"\';:\uFEFF]/g,B).replace(/\"[^\"]+\"|\'[^\']+\'/g,function(z){return z.replace(/[;:]/g,B)
});
while(b=x.exec(C)){f=b[1].replace(n,"").toLowerCase();
k=b[2].replace(n,"");
if(f&&k.length>0){if(f==="font-weight"&&k==="700"){k="bold"
}else{if(f==="color"||f==="background-color"){k=k.toLowerCase()
}}k=k.replace(o,v);
k=k.replace(q,d);
h[f]=a?g(k,true):k
}x.lastIndex=b.index+b[0].length
}c("border","");
c("border","-width");
c("border","-color");
c("border","-style");
c("padding","");
c("margin","");
e("border","border-width","border-style","border-color");
if(h.border==="medium none"){delete h.border
}}return h
},serialize:function(d,b){var e="",f,c;
function a(k){var g,j,l,h;
g=s.styles[k];
if(g){for(j=0,l=g.length;
j<l;
j++){k=g[j];
h=d[k];
if(h!==t&&h.length>0){e+=(e.length>0?" ":"")+k+": "+h+";"
}}}}if(b&&s&&s.styles){a("*");
a(b)
}else{for(f in d){c=d[f];
if(c!==t&&c.length>0){e+=(e.length>0?" ":"")+f+": "+c+";"
}}}return e
}}
};
(function(m){var j={},n=m.makeMap,l=m.each;
function o(a,b){return a.split(b||",")
}function k(a,b){var d,c={};
function e(f){return f.replace(/[A-Z]+/g,function(g){return e(a[g])
})
}for(d in a){if(a.hasOwnProperty(d)){a[d]=e(a[d])
}}e(b).replace(/#/g,"#text").replace(/(\w+)\[([^\]]+)\]\[([^\]]*)\]/g,function(f,h,r,g){r=o(r,"|");
c[h]={attributes:n(r),attributesOrder:r,children:n(g,"|",{"#comment":{}})}
});
return c
}function i(){var a=j.html5;
if(!a){a=j.html5=k({A:"id|accesskey|class|dir|draggable|item|hidden|itemprop|role|spellcheck|style|subject|title|onclick|ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup",B:"#|a|abbr|area|audio|b|bdo|br|button|canvas|cite|code|command|datalist|del|dfn|em|embed|i|iframe|img|input|ins|kbd|keygen|label|link|map|mark|meta|meter|noscript|object|output|progress|q|ruby|samp|script|select|small|span|strong|sub|sup|svg|textarea|time|var|video|wbr",C:"#|a|abbr|area|address|article|aside|audio|b|bdo|blockquote|br|button|canvas|cite|code|command|datalist|del|details|dfn|dialog|div|dl|em|embed|fieldset|figure|footer|form|h1|h2|h3|h4|h5|h6|header|hgroup|hr|i|iframe|img|input|ins|kbd|keygen|label|link|map|mark|menu|meta|meter|nav|noscript|ol|object|output|p|pre|progress|q|ruby|samp|script|section|select|small|span|strong|style|sub|sup|svg|table|textarea|time|ul|var|video"},"html[A|manifest][body|head]head[A][base|command|link|meta|noscript|script|style|title]title[A][#]base[A|href|target][]link[A|href|rel|media|type|sizes][]meta[A|http-equiv|name|content|charset][]style[A|type|media|scoped][#]script[A|charset|type|src|defer|async][#]noscript[A][C]body[A][C]section[A][C]nav[A][C]article[A][C]aside[A][C]h1[A][B]h2[A][B]h3[A][B]h4[A][B]h5[A][B]h6[A][B]hgroup[A][h1|h2|h3|h4|h5|h6]header[A][C]footer[A][C]address[A][C]p[A][B]br[A][]pre[A][B]dialog[A][dd|dt]blockquote[A|cite][C]ol[A|start|reversed][li]ul[A][li]li[A|value][C]dl[A][dd|dt]dt[A][B]dd[A][C]a[A|href|target|ping|rel|media|type][B]em[A][B]strong[A][B]small[A][B]cite[A][B]q[A|cite][B]dfn[A][B]abbr[A][B]code[A][B]var[A][B]samp[A][B]kbd[A][B]sub[A][B]sup[A][B]i[A][B]b[A][B]mark[A][B]progress[A|value|max][B]meter[A|value|min|max|low|high|optimum][B]time[A|datetime][B]ruby[A][B|rt|rp]rt[A][B]rp[A][B]bdo[A][B]span[A][B]ins[A|cite|datetime][B]del[A|cite|datetime][B]figure[A][C|legend|figcaption]figcaption[A][C]img[A|alt|src|height|width|usemap|ismap][]iframe[A|name|src|height|width|sandbox|seamless][]embed[A|src|height|width|type][]object[A|data|type|height|width|usemap|name|form|classid][param]param[A|name|value][]details[A|open][C|legend]command[A|type|label|icon|disabled|checked|radiogroup][]menu[A|type|label][C|li]legend[A][C|B]div[A][C]source[A|src|type|media][]audio[A|src|autobuffer|autoplay|loop|controls][source]video[A|src|autobuffer|autoplay|loop|controls|width|height|poster][source]hr[A][]form[A|accept-charset|action|autocomplete|enctype|method|name|novalidate|target][C]fieldset[A|disabled|form|name][C|legend]label[A|form|for][B]input[A|type|accept|alt|autocomplete|autofocus|checked|disabled|form|formaction|formenctype|formmethod|formnovalidate|formtarget|height|list|max|maxlength|min|multiple|pattern|placeholder|readonly|required|size|src|step|width|files|value|name][]button[A|autofocus|disabled|form|formaction|formenctype|formmethod|formnovalidate|formtarget|name|value|type][B]select[A|autofocus|disabled|form|multiple|name|size][option|optgroup]datalist[A][B|option]optgroup[A|disabled|label][option]option[A|disabled|selected|label|value][]textarea[A|autofocus|disabled|form|maxlength|name|placeholder|readonly|required|rows|cols|wrap][]keygen[A|autofocus|challenge|disabled|form|keytype|name][]output[A|for|form|name][B]canvas[A|width|height][]map[A|name][B|C]area[A|shape|coords|href|alt|target|media|rel|ping|type][]mathml[A][]svg[A][]table[A|border][caption|colgroup|thead|tfoot|tbody|tr]caption[A][C]colgroup[A|span][col]col[A|span][]thead[A][tr]tfoot[A][tr]tbody[A][tr]tr[A][th|td]th[A|headers|rowspan|colspan|scope][B]td[A|headers|rowspan|colspan][C]wbr[A][]")
}return a
}function p(){var a=j.html4;
if(!a){a=j.html4=k({Z:"H|K|N|O|P",Y:"X|form|R|Q",ZG:"E|span|width|align|char|charoff|valign",X:"p|T|div|U|W|isindex|fieldset|table",ZF:"E|align|char|charoff|valign",W:"pre|hr|blockquote|address|center|noframes",ZE:"abbr|axis|headers|scope|rowspan|colspan|align|char|charoff|valign|nowrap|bgcolor|width|height",ZD:"[E][S]",U:"ul|ol|dl|menu|dir",ZC:"p|Y|div|U|W|table|br|span|bdo|object|applet|img|map|K|N|Q",T:"h1|h2|h3|h4|h5|h6",ZB:"X|S|Q",S:"R|P",ZA:"a|G|J|M|O|P",R:"a|H|K|N|O",Q:"noscript|P",P:"ins|del|script",O:"input|select|textarea|label|button",N:"M|L",M:"em|strong|dfn|code|q|samp|kbd|var|cite|abbr|acronym",L:"sub|sup",K:"J|I",J:"tt|i|b|u|s|strike",I:"big|small|font|basefont",H:"G|F",G:"br|span|bdo",F:"object|applet|img|map|iframe",E:"A|B|C",D:"accesskey|tabindex|onfocus|onblur",C:"onclick|ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup",B:"lang|xml:lang|dir",A:"id|class|style|title"},"script[id|charset|type|language|src|defer|xml:space][]style[B|id|type|media|title|xml:space][]object[E|declare|classid|codebase|data|type|codetype|archive|standby|width|height|usemap|name|tabindex|align|border|hspace|vspace][#|param|Y]param[id|name|value|valuetype|type][]p[E|align][#|S]a[E|D|charset|type|name|href|hreflang|rel|rev|shape|coords|target][#|Z]br[A|clear][]span[E][#|S]bdo[A|C|B][#|S]applet[A|codebase|archive|code|object|alt|name|width|height|align|hspace|vspace][#|param|Y]h1[E|align][#|S]img[E|src|alt|name|longdesc|width|height|usemap|ismap|align|border|hspace|vspace][]map[B|C|A|name][X|form|Q|area]h2[E|align][#|S]iframe[A|longdesc|name|src|frameborder|marginwidth|marginheight|scrolling|align|width|height][#|Y]h3[E|align][#|S]tt[E][#|S]i[E][#|S]b[E][#|S]u[E][#|S]s[E][#|S]strike[E][#|S]big[E][#|S]small[E][#|S]font[A|B|size|color|face][#|S]basefont[id|size|color|face][]em[E][#|S]strong[E][#|S]dfn[E][#|S]code[E][#|S]q[E|cite][#|S]samp[E][#|S]kbd[E][#|S]var[E][#|S]cite[E][#|S]abbr[E][#|S]acronym[E][#|S]sub[E][#|S]sup[E][#|S]input[E|D|type|name|value|checked|disabled|readonly|size|maxlength|src|alt|usemap|onselect|onchange|accept|align][]select[E|name|size|multiple|disabled|tabindex|onfocus|onblur|onchange][optgroup|option]optgroup[E|disabled|label][option]option[E|selected|disabled|label|value][]textarea[E|D|name|rows|cols|disabled|readonly|onselect|onchange][]label[E|for|accesskey|onfocus|onblur][#|S]button[E|D|name|value|type|disabled][#|p|T|div|U|W|table|G|object|applet|img|map|K|N|Q]h4[E|align][#|S]ins[E|cite|datetime][#|Y]h5[E|align][#|S]del[E|cite|datetime][#|Y]h6[E|align][#|S]div[E|align][#|Y]ul[E|type|compact][li]li[E|type|value][#|Y]ol[E|type|compact|start][li]dl[E|compact][dt|dd]dt[E][#|S]dd[E][#|Y]menu[E|compact][li]dir[E|compact][li]pre[E|width|xml:space][#|ZA]hr[E|align|noshade|size|width][]blockquote[E|cite][#|Y]address[E][#|S|p]center[E][#|Y]noframes[E][#|Y]isindex[A|B|prompt][]fieldset[E][#|legend|Y]legend[E|accesskey|align][#|S]table[E|summary|width|border|frame|rules|cellspacing|cellpadding|align|bgcolor][caption|col|colgroup|thead|tfoot|tbody|tr]caption[E|align][#|S]col[ZG][]colgroup[ZG][col]thead[ZF][tr]tr[ZF|bgcolor][th|td]th[E|ZE][#|Y]form[E|action|method|name|enctype|onsubmit|onreset|accept|accept-charset|target][#|X|R|Q]noscript[E][#|Y]td[E|ZE][#|Y]tfoot[ZF][tr]tbody[ZF][tr]area[E|D|shape|coords|href|nohref|alt|target][]base[id|href|target][]body[E|onload|onunload|background|bgcolor|text|link|vlink|alink][#|Y]")
}return a
}m.html.Schema=function(I){var e=this,g={},O={},P=[],h,b;
var K,H,a,F,d,L,J={};
function M(s,t,q){var r=I[s];
if(!r){r=j[s];
if(!r){r=n(t," ",n(t.toUpperCase()," "));
r=m.extend(r,q);
j[s]=r
}}else{r=n(r,",",n(r.toUpperCase()," "))
}return r
}I=I||{};
b=I.schema=="html5"?i():p();
if(I.verify_html===false){I.valid_elements="*[*]"
}if(I.valid_styles){h={};
l(I.valid_styles,function(q,r){h[r]=m.explode(q)
})
}K=M("whitespace_elements","pre script noscript style textarea");
H=M("self_closing_elements","colgroup dd dt li option p td tfoot th thead tr");
a=M("short_ended_elements","area base basefont br col frame hr img input isindex link meta param embed source wbr");
F=M("boolean_attributes","checked compact declare defer disabled ismap multiple nohref noresize noshade nowrap readonly selected autoplay loop controls");
L=M("non_empty_elements","td th iframe video audio object script",a);
textBlockElementsMap=M("text_block_elements","h1 h2 h3 h4 h5 h6 p div address pre form blockquote center dir fieldset header footer article section hgroup aside nav figure");
d=M("block_elements","hr table tbody thead tfoot th tr td li ol ul caption dl dt dd noscript menu isindex samp option datalist select optgroup",textBlockElementsMap);
function Q(q){return new RegExp("^"+q.replace(/([?+*])/g,".$1")+"$")
}function E(af){var ag,ak,q,u,aq,al,ai,v,s,A,r,ao,C,ah,t,am,y,aj,ap,an,B,x,D=/^([#+\-])?([^\[\/]+)(?:\/([^\[]+))?(?:\[([^\]]+)\])?$/,z=/^([!\-])?(\w+::\w+|[^=:<]+)?(?:([=:<])(.*))?$/,ae=/[*?+]/;
if(af){af=o(af);
if(g["@"]){y=g["@"].attributes;
aj=g["@"].attributesOrder
}for(ag=0,ak=af.length;
ag<ak;
ag++){al=D.exec(af[ag]);
if(al){t=al[1];
A=al[2];
am=al[3];
s=al[4];
C={};
ah=[];
ai={attributes:C,attributesOrder:ah};
if(t==="#"){ai.paddEmpty=true
}if(t==="-"){ai.removeEmpty=true
}if(y){for(an in y){C[an]=y[an]
}ah.push.apply(ah,aj)
}if(s){s=o(s,"|");
for(q=0,u=s.length;
q<u;
q++){al=z.exec(s[q]);
if(al){v={};
ao=al[1];
r=al[2].replace(/::/g,":");
t=al[3];
x=al[4];
if(ao==="!"){ai.attributesRequired=ai.attributesRequired||[];
ai.attributesRequired.push(r);
v.required=true
}if(ao==="-"){delete C[r];
ah.splice(m.inArray(ah,r),1);
continue
}if(t){if(t==="="){ai.attributesDefault=ai.attributesDefault||[];
ai.attributesDefault.push({name:r,value:x});
v.defaultValue=x
}if(t===":"){ai.attributesForced=ai.attributesForced||[];
ai.attributesForced.push({name:r,value:x});
v.forcedValue=x
}if(t==="<"){v.validValues=n(x,"?")
}}if(ae.test(r)){ai.attributePatterns=ai.attributePatterns||[];
v.pattern=Q(r);
ai.attributePatterns.push(v)
}else{if(!C[r]){ah.push(r)
}C[r]=v
}}}}if(!y&&A=="@"){y=C;
aj=ah
}if(am){ai.outputName=A;
g[am]=ai
}if(ae.test(A)){ai.pattern=Q(A);
P.push(ai)
}else{g[A]=ai
}}}}}function f(q){g={};
P=[];
E(q);
l(b,function(r,s){O[s]=r.children
})
}function N(q){var r=/^(~)?(.+)$/;
if(q){l(o(q),function(t){var v=r.exec(t),u=v[1]==="~",s=u?"span":"div",x=v[2];
O[x]=O[s];
J[x]=s;
if(!u){d[x.toUpperCase()]={};
d[x]={}
}if(!g[x]){g[x]=g[s]
}l(O,function(y,z){if(y[s]){y[x]=y[s]
}})
})
}}function c(q){var r=/^([+\-]?)(\w+)\[([^\]]+)\]$/;
if(q){l(o(q),function(s){var t=r.exec(s),v,u;
if(t){u=t[1];
if(u){v=O[t[2]]
}else{v=O[t[2]]={"#comment":{}}
}v=O[t[2]];
l(o(t[3],"|"),function(x){if(u==="-"){delete v[x]
}else{v[x]={}
}})
}})
}}function G(s){var q=g[s],r;
if(q){return q
}r=P.length;
while(r--){q=P[r];
if(q.pattern.test(s)){return q
}}}if(!I.valid_elements){l(b,function(q,r){g[r]={attributes:q.attributes,attributesOrder:q.attributesOrder};
O[r]=q.children
});
if(I.schema!="html5"){l(o("strong/b,em/i"),function(q){q=o(q,"/");
g[q[1]].outputName=q[0]
})
}g.img.attributesDefault=[{name:"alt",value:""}];
l(o("ol,ul,sub,sup,blockquote,span,font,a,table,tbody,tr,strong,em,b,i"),function(q){if(g[q]){g[q].removeEmpty=true
}});
l(o("p,h1,h2,h3,h4,h5,h6,th,td,pre,div,address,caption"),function(q){g[q].paddEmpty=true
})
}else{f(I.valid_elements)
}N(I.custom_elements);
c(I.valid_children);
E(I.extended_valid_elements);
c("+ol[ul|ol],+ul[ul|ol]");
if(I.invalid_elements){m.each(m.explode(I.invalid_elements),function(q){if(g[q]){delete g[q]
}})
}if(!G("span")){E("span[!data-mce-type|*]")
}e.children=O;
e.styles=h;
e.getBoolAttrs=function(){return F
};
e.getBlockElements=function(){return d
};
e.getTextBlockElements=function(){return textBlockElementsMap
};
e.getShortEndedElements=function(){return a
};
e.getSelfClosingElements=function(){return H
};
e.getNonEmptyElements=function(){return L
};
e.getWhiteSpaceElements=function(){return K
};
e.isValidChild=function(s,q){var r=O[s];
return !!(r&&r[q])
};
e.isValid=function(t,u){var r,s,q=G(t);
if(q){if(u){if(q.attributes[u]){return true
}r=q.attributePatterns;
if(r){s=r.length;
while(s--){if(r[s].pattern.test(t)){return true
}}}}else{return true
}}return false
};
e.getElementRule=G;
e.getCustomElements=function(){return J
};
e.addValidElements=E;
e.setValidElements=f;
e.addCustomElements=N;
e.addValidChildren=c;
e.elements=g
}
})(tinymce);
(function(b){b.html.SaxParser=function(h,f){var a=this,g=function(){};
h=h||{};
a.schema=f=f||new b.html.Schema();
if(h.fix_self_closing!==false){h.fix_self_closing=true
}b.each("comment cdata text start end pi doctype".split(" "),function(c){if(c){a[c]=h[c]||g
}});
a.parse=function(ad){var au=this,aA,ab=0,Z,ag,ah=[],U,e,af,ap,ai,ao,V,aa,T,al,av,ax,an,d,at,i,ac,c,W,aB,Y,aw,ae,X,az,ak=0,ay=b.html.Entities.decode,aj,aq;
function am(l){var j,k;
j=ah.length;
while(j--){if(ah[j].name===l){break
}}if(j>=0){for(k=ah.length-1;
k>=j;
k--){l=ah[k];
if(l.valid){au.end(l.name)
}}ah.length=j
}}function ar(l,m,o,p,j){var n,k;
m=m.toLowerCase();
o=m in aa?m:ay(o||p||j||"");
if(al&&!ai&&m.indexOf("data-")!==0){n=i[m];
if(!n&&ac){k=ac.length;
while(k--){n=ac[k];
if(n.pattern.test(m)){break
}}if(k===-1){n=null
}}if(!n){return
}if(n.validValues&&!(o in n.validValues)){return
}}U.map[m]=o;
U.push({name:m,value:o})
}aw=new RegExp("<(?:(?:!--([\\w\\W]*?)-->)|(?:!\\[CDATA\\[([\\w\\W]*?)\\]\\]>)|(?:!DOCTYPE([\\w\\W]*?)>)|(?:\\?([^\\s\\/<>]+) ?([\\w\\W]*?)[?/]>)|(?:\\/([^>]+)>)|(?:([A-Za-z0-9\\-\\:\\.]+)((?:\\s+[^\"'>]+(?:(?:\"[^\"]*\")|(?:'[^']*')|[^>]*))*|\\/|\\s+)>))","g");
ae=/([\w:\-]+)(?:\s*=\s*(?:(?:\"((?:[^\"])*)\")|(?:\'((?:[^\'])*)\')|([^>\s]+)))?/g;
X={script:/<\/script[^>]*>/gi,style:/<\/style[^>]*>/gi,noscript:/<\/noscript[^>]*>/gi};
V=f.getShortEndedElements();
Y=h.self_closing_elements||f.getSelfClosingElements();
aa=f.getBoolAttrs();
al=h.validate;
ao=h.remove_internals;
aj=h.fix_self_closing;
aq=b.isIE;
at=/^:/;
while(aA=aw.exec(ad)){if(ab<aA.index){au.text(ay(ad.substr(ab,aA.index-ab)))
}if(Z=aA[6]){Z=Z.toLowerCase();
if(aq&&at.test(Z)){Z=Z.substr(1)
}am(Z)
}else{if(Z=aA[7]){Z=Z.toLowerCase();
if(aq&&at.test(Z)){Z=Z.substr(1)
}T=Z in V;
if(aj&&Y[Z]&&ah.length>0&&ah[ah.length-1].name===Z){am(Z)
}if(!al||(av=f.getElementRule(Z))){ax=true;
if(al){i=av.attributes;
ac=av.attributePatterns
}if(d=aA[8]){ai=d.indexOf("data-mce-type")!==-1;
if(ai&&ao){ax=false
}U=[];
U.map={};
d.replace(ae,ar)
}else{U=[];
U.map={}
}if(al&&!ai){c=av.attributesRequired;
W=av.attributesDefault;
aB=av.attributesForced;
if(aB){e=aB.length;
while(e--){an=aB[e];
ap=an.name;
az=an.value;
if(az==="{$uid}"){az="mce_"+ak++
}U.map[ap]=az;
U.push({name:ap,value:az})
}}if(W){e=W.length;
while(e--){an=W[e];
ap=an.name;
if(!(ap in U.map)){az=an.value;
if(az==="{$uid}"){az="mce_"+ak++
}U.map[ap]=az;
U.push({name:ap,value:az})
}}}if(c){e=c.length;
while(e--){if(c[e] in U.map){break
}}if(e===-1){ax=false
}}if(U.map["data-mce-bogus"]){ax=false
}}if(ax){au.start(Z,U,T)
}}else{ax=false
}if(ag=X[Z]){ag.lastIndex=ab=aA.index+aA[0].length;
if(aA=ag.exec(ad)){if(ax){af=ad.substr(ab,aA.index-ab)
}ab=aA.index+aA[0].length
}else{af=ad.substr(ab);
ab=ad.length
}if(ax&&af.length>0){au.text(af,true)
}if(ax){au.end(Z)
}aw.lastIndex=ab;
continue
}if(!T){if(!d||d.indexOf("/")!=d.length-1){ah.push({name:Z,valid:ax})
}else{if(ax){au.end(Z)
}}}}else{if(Z=aA[1]){au.comment(Z)
}else{if(Z=aA[2]){au.cdata(Z)
}else{if(Z=aA[3]){au.doctype(Z)
}else{if(Z=aA[4]){au.pi(Z,aA[5])
}}}}}}ab=aA.index+aA[0].length
}if(ab<ad.length){au.text(ay(ad.substr(ab)))
}for(e=ah.length-1;
e>=0;
e--){Z=ah[e];
if(Z.valid){au.end(Z.name)
}}}
}
})(tinymce);
(function(i){var j=/^[ \t\r\n]*$/,h={"#text":3,"#comment":8,"#cdata":4,"#pi":7,"#doctype":10,"#document-fragment":11};
function g(b,a,c){var d,e,n=c?"lastChild":"firstChild",m=c?"prev":"next";
if(b[n]){return b[n]
}if(b!==a){d=b[m];
if(d){return d
}for(e=b.parent;
e&&e!==a;
e=e.parent){d=e[m];
if(d){return d
}}}}function f(b,a){this.name=b;
this.type=a;
if(a===1){this.attributes=[];
this.attributes.map={}
}}i.extend(f.prototype,{replace:function(a){var b=this;
if(a.parent){a.remove()
}b.insert(a,b);
b.remove();
return b
},attr:function(d,a){var m=this,e,c,b;
if(typeof d!=="string"){for(c in d){m.attr(c,d[c])
}return m
}if(e=m.attributes){if(a!==b){if(a===null){if(d in e.map){delete e.map[d];
c=e.length;
while(c--){if(e[c].name===d){e=e.splice(c,1);
return m
}}}return m
}if(d in e.map){c=e.length;
while(c--){if(e[c].name===d){e[c].value=a;
break
}}}else{e.push({name:d,value:a})
}e.map[d]=a;
return m
}else{return e.map[d]
}}},clone:function(){var e=this,o=new f(e.name,e.type),d,l,a,c,b;
if(a=e.attributes){b=[];
b.map={};
for(d=0,l=a.length;
d<l;
d++){c=a[d];
if(c.name!=="id"){b[b.length]={name:c.name,value:c.value};
b.map[c.name]=c.value
}}o.attributes=b
}o.value=e.value;
o.shortEnded=e.shortEnded;
return o
},wrap:function(a){var b=this;
b.parent.insert(a,b);
a.append(b);
return b
},unwrap:function(){var c=this,a,b;
for(a=c.firstChild;
a;
){b=a.next;
c.insert(a,c,true);
a=b
}c.remove()
},remove:function(){var d=this,b=d.parent,c=d.next,a=d.prev;
if(b){if(b.firstChild===d){b.firstChild=c;
if(c){c.prev=null
}}else{a.next=c
}if(b.lastChild===d){b.lastChild=a;
if(a){a.next=null
}}else{c.prev=a
}d.parent=d.next=d.prev=null
}return d
},append:function(a){var c=this,b;
if(a.parent){a.remove()
}b=c.lastChild;
if(b){b.next=a;
a.prev=b;
c.lastChild=a
}else{c.lastChild=c.firstChild=a
}a.parent=c;
return a
},insert:function(b,d,a){var c;
if(b.parent){b.remove()
}c=d.parent||this;
if(a){if(d===c.firstChild){c.firstChild=b
}else{d.prev.next=b
}b.prev=d.prev;
b.next=d;
d.prev=b
}else{if(d===c.lastChild){c.lastChild=b
}else{d.next.prev=b
}b.next=d.next;
b.prev=d;
d.next=b
}b.parent=c;
return b
},getAll:function(c){var d=this,b,a=[];
for(b=d.firstChild;
b;
b=g(b,d)){if(b.name===c){a.push(b)
}}return a
},empty:function(){var c=this,d,b,a;
if(c.firstChild){d=[];
for(a=c.firstChild;
a;
a=g(a,c)){d.push(a)
}b=d.length;
while(b--){a=d[b];
a.parent=a.firstChild=a.lastChild=a.next=a.prev=null
}}c.firstChild=c.lastChild=null;
return c
},isEmpty:function(a){var e=this,b=e.firstChild,c,d;
if(b){do{if(b.type===1){if(b.attributes.map["data-mce-bogus"]){continue
}if(a[b.name]){return false
}c=b.attributes.length;
while(c--){d=b.attributes[c].name;
if(d==="name"||d.indexOf("data-mce-")===0){return false
}}}if(b.type===8){return false
}if((b.type===3&&!j.test(b.value))){return false
}}while(b=g(b,e))
}return true
},walk:function(a){return g(this,null,a)
}});
i.extend(f,{create:function(c,d){var a,b;
a=new f(c,h[c]||1);
if(d){for(b in d){a.attr(b,d[b])
}}return a
}});
i.html.Node=f
})(tinymce);
(function(c){var d=c.html.Node;
c.html.DomParser=function(l,k){var m=this,n={},o=[],b={},p={};
l=l||{};
l.validate="validate" in l?l.validate:true;
l.root_name=l.root_name||"body";
m.schema=k=k||new c.html.Schema();
function a(j){var g,f,F,G,i,h,e,C,I,H,D,J,B,E,K;
J=c.makeMap("tr,td,th,tbody,thead,tfoot,table");
D=k.getNonEmptyElements();
B=k.getTextBlockElements();
for(g=0;
g<j.length;
g++){f=j[g];
if(!f.parent||f.fixed){continue
}if(B[f.name]&&f.parent.name=="li"){E=f.next;
while(E){if(B[E.name]){E.name="li";
E.fixed=true;
f.parent.insert(E,f.parent)
}else{break
}E=E.next
}f.unwrap(f);
continue
}G=[f];
for(F=f.parent;
F&&!k.isValidChild(F.name,f.name)&&!J[F.name];
F=F.parent){G.push(F)
}if(F&&G.length>1){G.reverse();
i=h=m.filterNode(G[0].clone());
for(I=0;
I<G.length-1;
I++){if(k.isValidChild(h.name,G[I].name)){e=m.filterNode(G[I].clone());
h.append(e)
}else{e=h
}for(C=G[I].firstChild;
C&&C!=G[I+1];
){K=C.next;
e.append(C);
C=K
}h=e
}if(!i.isEmpty(D)){F.insert(i,G[0],true);
F.insert(f,i)
}else{F.insert(f,G[0],true)
}F=G[0];
if(F.isEmpty(D)||F.firstChild===F.lastChild&&F.firstChild.name==="br"){F.empty().remove()
}}else{if(f.parent){if(f.name==="li"){E=f.prev;
if(E&&(E.name==="ul"||E.name==="ul")){E.append(f);
continue
}E=f.next;
if(E&&(E.name==="ul"||E.name==="ul")){E.insert(f,E.firstChild,true);
continue
}f.wrap(m.filterNode(new d("ul",1)));
continue
}if(k.isValidChild(f.parent.name,"div")&&k.isValidChild("div",f.name)){f.wrap(m.filterNode(new d("div",1)))
}else{if(f.name==="style"||f.name==="script"){f.empty().remove()
}else{f.unwrap()
}}}}}}m.filterNode=function(e){var f,g,h;
if(g in n){h=b[g];
if(h){h.push(e)
}else{b[g]=[e]
}}f=o.length;
while(f--){g=o[f].name;
if(g in e.attributes.map){h=p[g];
if(h){h.push(e)
}else{p[g]=[e]
}}}return e
};
m.addNodeFilter=function(f,e){c.each(c.explode(f),function(g){var h=n[g];
if(!h){n[g]=h=[]
}h.push(e)
})
};
m.addAttributeFilter=function(f,e){c.each(c.explode(f),function(g){var h;
for(h=0;
h<o.length;
h++){if(o[h].name===g){o[h].callbacks.push(e);
return
}}o.push({name:g,callbacks:[e]})
})
};
m.parse=function(T,ai){var ah,O,ad,af,Z,aa,Q,ab,V,e,i,ag,X,f=[],g,W,aj,j,Y,ae,U,ac;
ai=ai||{};
b={};
p={};
ag=c.extend(c.makeMap("script,style,head,html,body,title,meta,param"),k.getBlockElements());
U=k.getNonEmptyElements();
ae=k.children;
i=l.validate;
ac="forced_root_block" in ai?ai.forced_root_block:l.forced_root_block;
Y=k.getWhiteSpaceElements();
X=/^[ \t\r\n]+/;
W=/[ \t\r\n]+$/;
aj=/[ \t\r\n]+/g;
j=/^[ \t\r\n]+$/;
function S(){var s=O.firstChild,q,r;
while(s){q=s.next;
if(s.type==3||(s.type==1&&s.name!=="p"&&!ag[s.name]&&!s.attr("data-mce-type"))){if(!r){r=h(ac,1);
O.insert(r,s);
r.append(s)
}else{r.append(s)
}}else{r=null
}s=q
}}function h(q,t){var s=new d(q,t),r;
if(q in n){r=b[q];
if(r){r.push(s)
}else{b[q]=[s]
}}return s
}function P(s){var r,q,t;
for(r=s.prev;
r&&r.type===3;
){q=r.value.replace(W,"");
if(q.length>0){r.value=q;
r=r.prev
}else{t=r.prev;
r.remove();
r=t
}}}function R(s){var r,q={};
for(r in s){if(r!=="li"&&r!="p"){q[r]=s[r]
}}return q
}ah=new c.html.SaxParser({validate:i,self_closing_elements:R(k.getSelfClosingElements()),cdata:function(q){ad.append(h("#cdata",4)).value=q
},text:function(r,q){var s;
if(!g){r=r.replace(aj," ");
if(ad.lastChild&&ag[ad.lastChild.name]){r=r.replace(X,"")
}}if(r.length!==0){s=h("#text",3);
s.raw=!!q;
ad.append(s).value=r
}},comment:function(q){ad.append(h("#comment",8)).value=q
},pi:function(q,r){ad.append(h(q,7)).value=r;
P(ad)
},doctype:function(r){var q;
q=ad.append(h("#doctype",10));
q.value=r;
P(ad)
},start:function(u,r,A){var t,y,z,B,x,q,s,v;
z=i?k.getElementRule(u):{};
if(z){t=h(z.outputName||u,1);
t.attributes=r;
t.shortEnded=A;
ad.append(t);
v=ae[ad.name];
if(v&&ae[t.name]&&!v[t.name]){f.push(t)
}y=o.length;
while(y--){x=o[y].name;
if(x in r.map){V=p[x];
if(V){V.push(t)
}else{p[x]=[t]
}}}if(ag[u]){P(t)
}if(!A){ad=t
}if(!g&&Y[u]){g=true
}}},end:function(q){var r,u,s,v,t;
u=i?k.getElementRule(q):{};
if(u){if(ag[q]){if(!g){r=ad.firstChild;
if(r&&r.type===3){s=r.value.replace(X,"");
if(s.length>0){r.value=s;
r=r.next
}else{v=r.next;
r.remove();
r=v
}while(r&&r.type===3){s=r.value;
v=r.next;
if(s.length===0||j.test(s)){r.remove();
r=v
}r=v
}}r=ad.lastChild;
if(r&&r.type===3){s=r.value.replace(W,"");
if(s.length>0){r.value=s;
r=r.prev
}else{v=r.prev;
r.remove();
r=v
}while(r&&r.type===3){s=r.value;
v=r.prev;
if(s.length===0||j.test(s)){r.remove();
r=v
}r=v
}}}}if(g&&Y[q]){g=false
}if(u.removeEmpty||u.paddEmpty){if(ad.isEmpty(U)){if(u.paddEmpty){ad.empty().append(new d("#text","3")).value="\u00a0"
}else{if(!ad.attributes.map.name&&!ad.attributes.map.id){t=ad.parent;
ad.empty().remove();
ad=t;
return
}}}}ad=ad.parent
}}},k);
O=ad=new d(ai.context||l.root_name,11);
ah.parse(T);
if(i&&f.length){if(!ai.context){a(f)
}else{ai.invalid=true
}}if(ac&&O.name=="body"){S()
}if(!ai.invalid){for(e in b){V=n[e];
af=b[e];
Q=af.length;
while(Q--){if(!af[Q].parent){af.splice(Q,1)
}}for(Z=0,aa=V.length;
Z<aa;
Z++){V[Z](af,e,ai)
}}for(Z=0,aa=o.length;
Z<aa;
Z++){V=o[Z];
if(V.name in p){af=p[V.name];
Q=af.length;
while(Q--){if(!af[Q].parent){af.splice(Q,1)
}}for(Q=0,ab=V.callbacks.length;
Q<ab;
Q++){V.callbacks[Q](af,V.name,ai)
}}}}return O
};
if(l.remove_trailing_brs){m.addNodeFilter("br",function(i,j){var e,f=i.length,h,y=c.extend({},k.getBlockElements()),x=k.getNonEmptyElements(),A,B,g,z;
y.body=1;
for(e=0;
e<f;
e++){h=i[e];
A=h.parent;
if(y[h.parent.name]&&h===A.lastChild){g=h.prev;
while(g){z=g.name;
if(z!=="span"||g.attr("data-mce-type")!=="bookmark"){if(z!=="br"){break
}if(z==="br"){h=null;
break
}}g=g.prev
}if(h){h.remove();
if(A.isEmpty(x)){elementRule=k.getElementRule(A.name);
if(elementRule){if(elementRule.removeEmpty){A.remove()
}else{if(elementRule.paddEmpty){A.empty().append(new c.html.Node("#text",3)).value="\u00a0"
}}}}}}else{B=h;
while(A.firstChild===B&&A.lastChild===B){B=A;
if(y[A.name]){break
}A=A.parent
}if(B===A){textNode=new c.html.Node("#text",3);
textNode.value="\u00a0";
h.replace(textNode)
}}}})
}if(!l.allow_html_in_named_anchor){m.addAttributeFilter("id,name",function(g,f){var r=g.length,i,e,j,h;
while(r--){h=g[r];
if(h.name==="a"&&h.firstChild&&!h.attr("href")){j=h.parent;
i=h.lastChild;
do{e=i.prev;
j.insert(i,h);
i=e
}while(i)
}}})
}}
})(tinymce);
tinymce.html.Writer=function(l){var n=[],i,h,m,k,j;
l=l||{};
i=l.indent;
h=tinymce.makeMap(l.indent_before||"");
m=tinymce.makeMap(l.indent_after||"");
k=tinymce.html.Entities.getEncodeFunc(l.entity_encoding||"raw",l.entities);
j=l.element_format=="html";
return{start:function(a,b,e){var g,c,d,f;
if(i&&h[a]&&n.length>0){f=n[n.length-1];
if(f.length>0&&f!=="\n"){n.push("\n")
}}n.push("<",a);
if(b){for(g=0,c=b.length;
g<c;
g++){d=b[g];
n.push(" ",d.name,'="',k(d.value,true),'"')
}}if(!e||j){n[n.length]=">"
}else{n[n.length]=" />"
}if(e&&i&&m[a]&&n.length>0){f=n[n.length-1];
if(f.length>0&&f!=="\n"){n.push("\n")
}}},end:function(b){var a;
n.push("</",b,">");
if(i&&m[b]&&n.length>0){a=n[n.length-1];
if(a.length>0&&a!=="\n"){n.push("\n")
}}},text:function(a,b){if(a.length>0){n[n.length]=b?a:k(a)
}},cdata:function(a){n.push("<![CDATA[",a,"]]>")
},comment:function(a){n.push("<!--",a,"-->")
},pi:function(b,a){if(a){n.push("<?",b," ",a,"?>")
}else{n.push("<?",b,"?>")
}if(i){n.push("\n")
}},doctype:function(a){n.push("<!DOCTYPE",a,">",i?"\n":"")
},reset:function(){n.length=0
},getContent:function(){return n.join("").replace(/\n$/,"")
}}
};
(function(b){b.html.Serializer=function(h,g){var a=this,f=new b.html.Writer(h);
h=h||{};
h.validate="validate" in h?h.validate:true;
a.schema=g=g||new b.html.Schema();
a.writer=f;
a.serialize=function(d){var e,c;
c=h.validate;
e={3:function(i,l){f.text(i.value,i.raw)
},8:function(i){f.comment(i.value)
},7:function(i){f.pi(i.name,i.value)
},10:function(i){f.doctype(i.value)
},4:function(i){f.cdata(i.value)
},11:function(i){if((i=i.firstChild)){do{j(i)
}while(i=i.next)
}}};
f.reset();
function j(A){var D=e[A.type],B,x,E,i,v,C,y,z,l;
if(!D){B=A.name;
x=A.shortEnded;
E=A.attributes;
if(c&&E&&E.length>1){C=[];
C.map={};
l=g.getElementRule(A.name);
for(y=0,z=l.attributesOrder.length;
y<z;
y++){i=l.attributesOrder[y];
if(i in E.map){v=E.map[i];
C.map[i]=v;
C.push({name:i,value:v})
}}for(y=0,z=E.length;
y<z;
y++){i=E[y].name;
if(!(i in C.map)){v=E.map[i];
C.map[i]=v;
C.push({name:i,value:v})
}}E=C
}f.start(A.name,E,x);
if(!x){if((A=A.firstChild)){do{j(A)
}while(A=A.next)
}f.end(B)
}}else{D(A)
}}if(d.type==1&&!h.inner){j(d)
}else{e[11](d)
}return f.getContent()
}
}
})(tinymce);
tinymce.dom={};
(function(i,k){var l=!!document.addEventListener;
function p(b,c,a,d){if(b.addEventListener){b.addEventListener(c,a,d||false)
}else{if(b.attachEvent){b.attachEvent("on"+c,a)
}}}function n(b,c,a,d){if(b.removeEventListener){b.removeEventListener(c,a,d||false)
}else{if(b.detachEvent){b.detachEvent("on"+c,a)
}}}function j(f,b){var e,c=b||{};
function d(){return false
}function a(){return true
}for(e in f){if(e!=="layerX"&&e!=="layerY"){c[e]=f[e]
}}if(!c.target){c.target=c.srcElement||document
}c.preventDefault=function(){c.isDefaultPrevented=a;
if(f){if(f.preventDefault){f.preventDefault()
}else{f.returnValue=false
}}};
c.stopPropagation=function(){c.isPropagationStopped=a;
if(f){if(f.stopPropagation){f.stopPropagation()
}else{f.cancelBubble=true
}}};
c.stopImmediatePropagation=function(){c.isImmediatePropagationStopped=a;
c.stopPropagation()
};
if(!c.isDefaultPrevented){c.isDefaultPrevented=d;
c.isPropagationStopped=d;
c.isImmediatePropagationStopped=d
}return c
}function o(a,f,b){var c=a.document,d={type:"ready"};
function e(){if(!b.domLoaded){b.domLoaded=true;
f(d)
}}if(c.readyState=="complete"){e();
return
}if(l){p(a,"DOMContentLoaded",e)
}else{p(c,"readystatechange",function(){if(c.readyState==="complete"){n(c,"readystatechange",arguments.callee);
e()
}});
if(c.documentElement.doScroll&&a===a.top){(function(){try{c.documentElement.doScroll("left")
}catch(g){setTimeout(arguments.callee,0);
return
}e()
})()
}}p(a,"load",e)
}function m(g){var a=this,b={},r,c,d,e,f;
e="onmouseenter" in document.documentElement;
d="onfocusin" in document.documentElement;
f={mouseenter:"mouseover",mouseleave:"mouseout"};
r=1;
a.domLoaded=false;
a.events=b;
function h(A,q){var B,z,C,y;
B=b[q][A.type];
if(B){for(z=0,C=B.length;
z<C;
z++){y=B[z];
if(y&&y.func.call(y.scope,A)===false){A.preventDefault()
}if(A.isImmediatePropagationStopped()){return
}}}}a.bind=function(L,I,q,P){var Q,O,N,F,H,J,G,M=window;
function K(s){h(j(s||M.event),Q)
}if(!L||L.nodeType===3||L.nodeType===8){return
}if(!L[k]){Q=r++;
L[k]=Q;
b[Q]={}
}else{Q=L[k];
if(!b[Q]){b[Q]={}
}}P=P||L;
I=I.split(" ");
N=I.length;
while(N--){F=I[N];
J=K;
H=G=false;
if(F==="DOMContentLoaded"){F="ready"
}if((a.domLoaded||L.readyState=="complete")&&F==="ready"){a.domLoaded=true;
q.call(P,j({type:F}));
continue
}if(!e){H=f[F];
if(H){J=function(u){var s,t;
s=u.currentTarget;
t=u.relatedTarget;
if(t&&s.contains){t=s.contains(t)
}else{while(t&&t!==s){t=t.parentNode
}}if(!t){u=j(u||M.event);
u.type=u.type==="mouseout"?"mouseleave":"mouseenter";
u.target=s;
h(u,Q)
}}
}}if(!d&&(F==="focusin"||F==="focusout")){G=true;
H=F==="focusin"?"focus":"blur";
J=function(s){s=j(s||M.event);
s.type=s.type==="focus"?"focusin":"focusout";
h(s,Q)
}
}O=b[Q][F];
if(!O){b[Q][F]=O=[{func:q,scope:P}];
O.fakeName=H;
O.capture=G;
O.nativeHandler=J;
if(!l){O.proxyHandler=g(Q)
}if(F==="ready"){o(L,J,a)
}else{p(L,H||F,l?J:O.proxyHandler,G)
}}else{O.push({func:q,scope:P})
}}L=O=0;
return q
};
a.unbind=function(G,E,D){var K,I,H,C,q,J;
if(!G||G.nodeType===3||G.nodeType===8){return a
}K=G[k];
if(K){J=b[K];
if(E){E=E.split(" ");
H=E.length;
while(H--){q=E[H];
I=J[q];
if(I){if(D){C=I.length;
while(C--){if(I[C].func===D){I.splice(C,1)
}}}if(!D||I.length===0){delete J[q];
n(G,I.fakeName||q,l?I.nativeHandler:I.proxyHandler,I.capture)
}}}}else{for(q in J){I=J[q];
n(G,I.fakeName||q,l?I.nativeHandler:I.proxyHandler,I.capture)
}J={}
}for(q in J){return a
}delete b[K];
try{delete G[k]
}catch(F){G[k]=null
}}return a
};
a.fire=function(x,z,A){var q,y;
if(!x||x.nodeType===3||x.nodeType===8){return a
}y=j(null,A);
y.type=z;
do{q=x[k];
if(q){h(y,q)
}x=x.parentNode||x.ownerDocument||x.defaultView||x.parentWindow
}while(x&&!y.isPropagationStopped());
return a
};
a.clean=function(q){var x,y,v=a.unbind;
if(!q||q.nodeType===3||q.nodeType===8){return a
}if(q[k]){v(q)
}if(!q.getElementsByTagName){q=q.document
}if(q&&q.getElementsByTagName){v(q);
y=q.getElementsByTagName("*");
x=y.length;
while(x--){q=y[x];
if(q[k]){v(q)
}}}return a
};
a.callNativeHandler=function(q,t){if(b){b[q][t.type].nativeHandler(t)
}};
a.destory=function(){b={}
};
a.add=function(q,z,x,y){if(typeof(q)==="string"){q=document.getElementById(q)
}if(q&&q instanceof Array){var A=q.length;
while(A--){a.add(q[A],z,x,y)
}return
}if(z==="init"){z="ready"
}return a.bind(q,z instanceof Array?z.join(" "):z,x,y)
};
a.remove=function(q,z,x,y){if(!q){return a
}if(typeof(q)==="string"){q=document.getElementById(q)
}if(q instanceof Array){var A=q.length;
while(A--){a.remove(q[A],z,x,y)
}return a
}return a.unbind(q,z instanceof Array?z.join(" "):z,x)
};
a.clear=function(q){if(typeof(q)==="string"){q=document.getElementById(q)
}return a.clean(q)
};
a.cancel=function(q){if(q){a.prevent(q);
a.stop(q)
}return false
};
a.prevent=function(q){if(!q.preventDefault){q=j(q)
}q.preventDefault();
return false
};
a.stop=function(q){if(!q.stopPropagation){q=j(q)
}q.stopPropagation();
return false
}
}i.EventUtils=m;
i.Event=new m(function(a){return function(b){tinymce.dom.Event.callNativeHandler(a,b)
}
});
i.Event.bind(window,"ready",function(){});
i=0
})(tinymce.dom,"data-mce-expando");
tinymce.dom.TreeWalker=function(f,h){var e=f;
function g(b,k,l,a){var c,d;
if(b){if(!a&&b[k]){return b[k]
}if(b!=h){c=b[l];
if(c){return c
}for(d=b.parentNode;
d&&d!=h;
d=d.parentNode){c=d[l];
if(c){return c
}}}}}this.current=function(){return e
};
this.next=function(a){return(e=g(e,"firstChild","nextSibling",a))
};
this.prev=function(a){return(e=g(e,"lastChild","previousSibling",a))
}
};
(function(n){var l=n.each,o=n.is,m=n.isWebKit,i=n.isIE,k=n.html.Entities,p=/^([a-z0-9],?)+$/i,j=/^[ \t\r\n]*$/;
n.create("tinymce.dom.DOMUtils",{doc:null,root:null,files:null,pixelStyles:/^(top|left|bottom|right|width|height|borderWidth)$/,props:{"for":"htmlFor","class":"className",className:"className",checked:"checked",disabled:"disabled",maxlength:"maxLength",readonly:"readOnly",selected:"selected",value:"value",id:"id",name:"name",type:"type"},DOMUtils:function(f,b){var c=this,e,d,g;
c.doc=f;
c.win=window;
c.files={};
c.cssFlicker=false;
c.counter=0;
c.stdMode=!n.isIE||f.documentMode>=8;
c.boxModel=!n.isIE||f.compatMode=="CSS1Compat"||c.stdMode;
c.hasOuterHTML="outerHTML" in f.createElement("a");
c.settings=b=n.extend({keep_values:false,hex_colors:1},b);
c.schema=b.schema;
c.styles=new n.html.Styles({url_converter:b.url_converter,url_converter_scope:b.url_converter_scope},b.schema);
if(n.isIE6){try{f.execCommand("BackgroundImageCache",false,true)
}catch(a){c.cssFlicker=true
}}c.fixDoc(f);
c.events=b.ownEvents?new n.dom.EventUtils(b.proxy):n.dom.Event;
n.addUnload(c.destroy,c);
g=b.schema?b.schema.getBlockElements():{};
c.isBlock=function(h){if(!h){return false
}var r=h.nodeType;
if(r){return !!(r===1&&g[h.nodeName])
}return !!g[h]
}
},fixDoc:function(a){var b=this.settings,c;
if(i&&!n.isIE11&&b.schema){("abbr article aside audio canvas details figcaption figure footer header hgroup mark menu meter nav output progress section summary time video").replace(/\w+/g,function(d){a.createElement(d)
});
for(c in b.schema.getCustomElements()){a.createElement(c)
}}},clone:function(c,e){var d=this,a,b;
if(!i||n.isIE11||c.nodeType!==1||e){return c.cloneNode(e)
}b=d.doc;
if(!e){a=b.createElement(c.nodeName);
l(d.getAttribs(c),function(f){d.setAttrib(a,f.nodeName,d.getAttrib(c,f.nodeName))
});
return a
}return a.firstChild
},getRoot:function(){var b=this,a=b.settings;
return(a&&b.get(a.root_element))||b.doc.body
},getViewPort:function(b){var a,c;
b=!b?this.win:b;
a=b.document;
c=this.boxModel?a.documentElement:a.body;
return{x:b.pageXOffset||c.scrollLeft,y:b.pageYOffset||c.scrollTop,w:b.innerWidth||c.clientWidth,h:b.innerHeight||c.clientHeight}
},getRect:function(a){var b,d=this,c;
a=d.get(a);
b=d.getPos(a);
c=d.getSize(a);
return{x:b.x,y:b.y,w:c.w,h:c.h}
},getSize:function(a){var c=this,d,b;
a=c.get(a);
d=c.getStyle(a,"width");
b=c.getStyle(a,"height");
if(d.indexOf("px")===-1){d=0
}if(b.indexOf("px")===-1){b=0
}return{w:parseInt(d,10)||a.offsetWidth||a.clientWidth,h:parseInt(b,10)||a.offsetHeight||a.clientHeight}
},getParent:function(a,b,c){return this.getParents(a,b,c,false)
},getParents:function(f,a,c,g){var d=this,e,b=d.settings,h=[];
f=d.get(f);
g=g===undefined;
if(b.strict_root){c=c||d.getRoot()
}if(o(a,"string")){e=a;
if(a==="*"){a=function(q){return q.nodeType==1
}
}else{a=function(q){return d.is(q,e)
}
}}while(f){if(f==c||!f.nodeType||f.nodeType===9){break
}if(!a||a(f)){if(g){h.push(f)
}else{return f
}}f=f.parentNode
}return g?h:null
},get:function(b){var a;
if(b&&this.doc&&typeof(b)=="string"){a=b;
b=this.doc.getElementById(b);
if(b&&b.id!==a){return this.doc.getElementsByName(a)[1]
}}return b
},getNext:function(a,b){return this._findSib(a,b,"nextSibling")
},getPrev:function(a,b){return this._findSib(a,b,"previousSibling")
},select:function(a,b){var c=this;
return n.dom.Sizzle(a,c.get(b)||c.get(c.settings.root_element)||c.doc,[])
},is:function(a,c){var b;
if(a.length===undefined){if(c==="*"){return a.nodeType==1
}if(p.test(c)){c=c.toLowerCase().split(/,/);
a=a.nodeName.toLowerCase();
for(b=c.length-1;
b>=0;
b--){if(c[b]==a){return true
}}return false
}}return n.dom.Sizzle.matches(c,a.nodeType?[a]:a).length>0
},add:function(b,f,e,c,a){var d=this;
return this.run(b,function(g){var h,s;
h=o(f,"string")?d.doc.createElement(f):f;
d.setAttribs(h,e);
if(c){if(c.nodeType){h.appendChild(c)
}else{d.setHTML(h,c)
}}return !a?g.appendChild(h):h
})
},create:function(a,c,b){return this.add(this.doc.createElement(a),a,c,b,1)
},createHTML:function(e,d,a){var f="",b=this,c;
f+="<"+e;
for(c in d){if(d.hasOwnProperty(c)){f+=" "+c+'="'+b.encode(d[c])+'"'
}}if(typeof(a)!="undefined"){return f+">"+a+"</"+e+">"
}return f+" />"
},remove:function(b,a){return this.run(b,function(d){var c,e=d.parentNode;
if(!e){return null
}if(a){while(c=d.firstChild){if(!n.isIE||c.nodeType!==3||c.nodeValue){e.insertBefore(c,d)
}else{d.removeChild(c)
}}}return e.removeChild(d)
})
},setStyle:function(a,d,c){var b=this;
return b.run(a,function(f){var g,e;
g=f.style;
d=d.replace(/-(\D)/g,function(h,r){return r.toUpperCase()
});
if(b.pixelStyles.test(d)&&(n.is(c,"number")||/^[\-0-9\.]+$/.test(c))){c+="px"
}switch(d){case"opacity":if(i&&!n.isIE11){g.filter=c===""?"":"alpha(opacity="+(c*100)+")";
if(!a.currentStyle||!a.currentStyle.hasLayout){g.display="inline-block"
}}g[d]=g["-moz-opacity"]=g["-khtml-opacity"]=c||"";
break;
case"float":(i&&!n.isIE11)?g.styleFloat=c:g.cssFloat=c;
break;
default:g[d]=c||""
}if(b.settings.update_styles){b.setAttrib(f,"data-mce-style")
}})
},getStyle:function(a,d,b){a=this.get(a);
if(!a){return
}if(this.doc.defaultView&&b){d=d.replace(/[A-Z]/g,function(e){return"-"+e
});
try{return this.doc.defaultView.getComputedStyle(a,null).getPropertyValue(d)
}catch(c){return null
}}d=d.replace(/-(\D)/g,function(f,e){return e.toUpperCase()
});
if(d=="float"){d=i?"styleFloat":"cssFloat"
}if(a.currentStyle&&b){return a.currentStyle[d]
}return a.style?a.style[d]:undefined
},setStyles:function(b,a){var d=this,c=d.settings,e;
e=c.update_styles;
c.update_styles=0;
l(a,function(g,f){d.setStyle(b,f,g)
});
c.update_styles=e;
if(c.update_styles){d.setAttrib(b,c.cssText)
}},removeAllAttribs:function(a){return this.run(a,function(b){var c,d=b.attributes;
for(c=d.length-1;
c>=0;
c--){b.removeAttributeNode(d.item(c))
}})
},setAttrib:function(b,a,d){var c=this;
if(!b||!a){return
}if(c.settings.strict){a=a.toLowerCase()
}return this.run(b,function(f){var g=c.settings;
var e=f.getAttribute(a);
if(d!==null){switch(a){case"style":if(!o(d,"string")){l(d,function(t,s){c.setStyle(f,s,t)
});
return
}if(g.keep_values){if(d&&!c._isRes(d)){f.setAttribute("data-mce-style",d,2)
}else{f.removeAttribute("data-mce-style",2)
}}f.style.cssText=d;
break;
case"class":f.className=d||"";
break;
case"src":case"href":if(g.keep_values){if(g.url_converter){d=g.url_converter.call(g.url_converter_scope||c,d,a,f)
}c.setAttrib(f,"data-mce-"+a,d,2)
}break;
case"shape":f.setAttribute("data-mce-style",d);
break
}}if(o(d)&&d!==null&&d.length!==0){f.setAttribute(a,""+d,2)
}else{f.removeAttribute(a,2)
}if(tinyMCE.activeEditor&&e!=d){var h=tinyMCE.activeEditor;
h.onSetAttrib.dispatch(h,f,a,d)
}})
},setAttribs:function(b,a){var c=this;
return this.run(b,function(d){l(a,function(e,f){c.setAttrib(d,f,e)
})
})
},getAttrib:function(a,f,c){var e,d=this,b;
a=d.get(a);
if(!a||a.nodeType!==1){return c===b?false:c
}if(!o(c)){c=""
}if(/^(src|href|style|coords|shape)$/.test(f)){e=a.getAttribute("data-mce-"+f);
if(e){return e
}}if(i&&d.props[f]){e=a[d.props[f]];
e=e&&e.nodeValue?e.nodeValue:e
}if(!e){e=a.getAttribute(f,2)
}if(/^(checked|compact|declare|defer|disabled|ismap|multiple|nohref|noshade|nowrap|readonly|selected)$/.test(f)){if(a[d.props[f]]===true&&e===""){return f
}return e?f:""
}if(a.nodeName==="FORM"&&a.getAttributeNode(f)){return a.getAttributeNode(f).nodeValue
}if(f==="style"){e=e||a.style.cssText;
if(e){e=d.serializeStyle(d.parseStyle(e),a.nodeName);
if(d.settings.keep_values&&!d._isRes(e)){a.setAttribute("data-mce-style",e)
}}}if(m&&f==="class"&&e){e=e.replace(/(apple|webkit)\-[a-z\-]+/gi,"")
}if(i){switch(f){case"rowspan":case"colspan":if(e===1){e=""
}break;
case"size":if(e==="+0"||e===20||e===0){e=""
}break;
case"width":case"height":case"vspace":case"checked":case"disabled":case"readonly":if(e===0){e=""
}break;
case"hspace":if(e===-1){e=""
}break;
case"maxlength":case"tabindex":if(e===32768||e===2147483647||e==="32768"){e=""
}break;
case"multiple":case"compact":case"noshade":case"nowrap":if(e===65535){return f
}return c;
case"shape":e=e.toLowerCase();
break;
default:if(f.indexOf("on")===0&&e){e=n._replace(/^function\s+\w+\(\)\s+\{\s+(.*)\s+\}$/,"$1",""+e)
}}}return(e!==b&&e!==null&&e!=="")?""+e:c
},getPos:function(f,b){var d=this,e=0,g=0,a,h=d.doc,c;
f=d.get(f);
b=b||h.body;
if(f){if(f.getBoundingClientRect){f=f.getBoundingClientRect();
a=d.boxModel?h.documentElement:h.body;
e=f.left+(h.documentElement.scrollLeft||h.body.scrollLeft)-a.clientTop;
g=f.top+(h.documentElement.scrollTop||h.body.scrollTop)-a.clientLeft;
return{x:e,y:g}
}c=f;
while(c&&c!=b&&c.nodeType){e+=c.offsetLeft||0;
g+=c.offsetTop||0;
c=c.offsetParent
}c=f.parentNode;
while(c&&c!=b&&c.nodeType){e-=c.scrollLeft||0;
g-=c.scrollTop||0;
c=c.parentNode
}}return{x:e,y:g}
},parseStyle:function(a){return this.styles.parse(a)
},serializeStyle:function(a,b){return this.styles.serialize(a,b)
},addStyle:function(b){var a=this.doc,c;
styleElm=a.getElementById("mceDefaultStyles");
if(!styleElm){styleElm=a.createElement("style"),styleElm.id="mceDefaultStyles";
styleElm.type="text/css";
c=a.getElementsByTagName("head")[0];
if(c.firstChild){c.insertBefore(styleElm,c.firstChild)
}else{c.appendChild(styleElm)
}}if(styleElm.styleSheet){styleElm.styleSheet.cssText+=b
}else{styleElm.appendChild(a.createTextNode(b))
}},loadCSS:function(d){var b=this,a=b.doc,c;
if(!d){d=""
}c=a.getElementsByTagName("head")[0];
l(d.split(","),function(e){var f;
if(b.files[e]){return
}b.files[e]=true;
f=b.create("link",{rel:"stylesheet",href:n._addVer(e)});
if(i&&!n.isIE11&&a.documentMode&&a.recalc){f.onload=function(){if(a.recalc){a.recalc()
}f.onload=null
}
}c.appendChild(f)
})
},addClass:function(b,a){return this.run(b,function(d){var c;
if(!a){return 0
}if(this.hasClass(d,a)){return d.className
}c=this.removeClass(d,a);
return d.className=(c!=""?(c+" "):"")+a
})
},removeClass:function(b,a){var d=this,c;
return d.run(b,function(f){var e;
if(d.hasClass(f,a)){if(!c){c=new RegExp("(^|\\s+)"+a+"(\\s+|$)","g")
}e=f.className.replace(c," ");
e=n.trim(e!=" "?e:"");
f.className=e;
if(!e){f.removeAttribute("class");
f.removeAttribute("className")
}return e
}return f.className
})
},hasClass:function(a,b){a=this.get(a);
if(!a||!b){return false
}return(" "+a.className+" ").indexOf(" "+b+" ")!==-1
},show:function(a){return this.setStyle(a,"display","block")
},hide:function(a){return this.setStyle(a,"display","none")
},isHidden:function(a){a=this.get(a);
return !a||a.style.display=="none"||this.getStyle(a,"display")=="none"
},uniqueId:function(a){return(!a?"mce_":a)+(this.counter++)
},setHTML:function(a,b){var c=this;
return c.run(a,function(d){if(i){while(d.firstChild){d.removeChild(d.firstChild)
}try{d.innerHTML="<br />"+b;
d.removeChild(d.firstChild)
}catch(e){var f=c.create("div");
f.innerHTML="<br />"+b;
l(n.grep(f.childNodes),function(g,h){if(h&&d.canHaveHTML){d.appendChild(g)
}})
}}else{d.innerHTML=b
}return b
})
},getOuterHTML:function(a){var b,c=this;
a=c.get(a);
if(!a){return null
}if(a.nodeType===1&&c.hasOuterHTML){return a.outerHTML
}b=(a.ownerDocument||c.doc).createElement("body");
b.appendChild(a.cloneNode(true));
return b.innerHTML
},setOuterHTML:function(b,d,a){var e=this;
function c(t,u,g){var f,h;
h=g.createElement("body");
h.innerHTML=u;
f=h.lastChild;
while(f){e.insertAfter(f.cloneNode(true),t);
f=f.previousSibling
}e.remove(t)
}return this.run(b,function(f){f=e.get(f);
if(f.nodeType==1){a=a||f.ownerDocument||e.doc;
if(i){try{if(i&&f.nodeType==1){f.outerHTML=d
}else{c(f,d,a)
}}catch(g){c(f,d,a)
}}else{c(f,d,a)
}}})
},decode:k.decode,encode:k.encodeAllRaw,insertAfter:function(b,a){a=this.get(a);
return this.run(b,function(d){var e,c;
e=a.parentNode;
c=a.nextSibling;
if(c){e.insertBefore(d,c)
}else{e.appendChild(d)
}return d
})
},replace:function(a,b,d){var c=this;
if(o(b,"array")){a=a.cloneNode(true)
}return c.run(b,function(e){if(d){l(n.grep(e.childNodes),function(f){a.appendChild(f)
})
}return e.parentNode.replaceChild(a,e)
})
},rename:function(a,d){var b=this,c;
if(a.nodeName!=d.toUpperCase()){c=b.create(d);
l(b.getAttribs(a),function(e){b.setAttrib(c,e.nodeName,b.getAttrib(a,e.nodeName))
});
b.replace(c,a,1)
}return c||a
},findCommonAncestor:function(b,d){var a=b,c;
while(a){c=d;
while(c&&a!=c){c=c.parentNode
}if(a==c){break
}a=a.parentNode
}if(!a&&b.ownerDocument){return b.ownerDocument.documentElement
}return a
},toHex:function(c){var a=/^\s*rgb\s*?\(\s*?([0-9]+)\s*?,\s*?([0-9]+)\s*?,\s*?([0-9]+)\s*?\)\s*$/i.exec(c);
function b(d){d=parseInt(d,10).toString(16);
return d.length>1?d:"0"+d
}if(a){c="#"+b(a[1])+b(a[2])+b(a[3]);
return c
}return c
},getClasses:function(){var h=this,d=[],a,g={},f=h.settings.class_filter,b;
if(h.classes){return h.classes
}function e(q){l(q.imports,function(r){e(r)
});
l(q.cssRules||q.rules,function(r){switch(r.type||1){case 1:if(r.selectorText){l(r.selectorText.split(","),function(s){s=s.replace(/^\s*|\s*$|^\s\./g,"");
if(/\.mce/.test(s)||!/\.[\w\-]+$/.test(s)){return
}b=s;
s=n._replace(/.*\.([a-z0-9_\-]+).*/i,"$1",s);
if(f&&!(s=f(s,b))){return
}if(!g[s]){d.push({"class":s});
g[s]=1
}})
}break;
case 3:try{e(r.styleSheet)
}catch(u){}break
}})
}try{l(h.doc.styleSheets,e)
}catch(c){}if(d.length>0){h.classes=d
}return d
},run:function(b,c,d){var e=this,a;
if(e.doc&&typeof(b)==="string"){b=e.get(b)
}if(!b){return false
}d=d||this;
if(!b.nodeType&&(b.length||b.length===0)){a=[];
l(b,function(f,g){if(f){if(typeof(f)=="string"){f=e.doc.getElementById(f)
}a.push(c.call(d,f,g))
}});
return a
}return c.call(d,b)
},getAttribs:function(a){var b;
a=this.get(a);
if(!a){return[]
}if(i){b=[];
if(a.nodeName=="OBJECT"){return a.attributes
}if(a.nodeName==="OPTION"&&this.getAttrib(a,"selected")){b.push({specified:1,nodeName:"selected"})
}a.cloneNode(false).outerHTML.replace(/<\/?[\w:\-]+ ?|=[\"][^\"]+\"|=\'[^\']+\'|=[\w\-]+|>/gi,"").replace(/[\w:\-]+/gi,function(c){b.push({specified:1,nodeName:c})
});
return b
}return a.attributes
},isEmpty:function(f,h){var a=this,d,e,b,s,g,c=0;
f=f.firstChild;
if(f){s=new n.dom.TreeWalker(f,f.parentNode);
h=h||a.schema?a.schema.getNonEmptyElements():null;
do{b=f.nodeType;
if(b===1){if(f.getAttribute("data-mce-bogus")){continue
}g=f.nodeName.toLowerCase();
if(h&&h[g]){if(g==="br"){c++;
continue
}return false
}e=a.getAttribs(f);
d=f.attributes.length;
while(d--){g=f.attributes[d].nodeName;
if(g==="name"||g==="data-mce-bookmark"){return false
}}}if(b==8){return false
}if((b===3&&!j.test(f.nodeValue))){return false
}}while(f=s.next())
}return c<=1
},destroy:function(a){var b=this;
b.win=b.doc=b.root=b.events=b.frag=null;
if(!a){n.removeUnload(b.destroy)
}},createRng:function(){var a=this.doc;
return a.createRange?a.createRange():new n.dom.Range(this)
},nodeIndex:function(a,f){var e=0,c,b,d;
if(a){for(c=a.nodeType,a=a.previousSibling,b=a;
a;
a=a.previousSibling){d=a.nodeType;
if(f&&d==3){if(d==c||!a.nodeValue.length){continue
}}e++;
c=d
}}return e
},split:function(e,f,b){var a=this,r=a.createRng(),d,g,c;
function h(y){var A,B=y.childNodes,z=y.nodeType;
function q(s){var t=s.previousSibling&&s.previousSibling.nodeName=="SPAN";
var u=s.nextSibling&&s.nextSibling.nodeName=="SPAN";
return t&&u
}if(z==1&&y.getAttribute("data-mce-type")=="bookmark"){return
}for(A=B.length-1;
A>=0;
A--){h(B[A])
}if(z!=9){if(z==3&&y.nodeValue.length>0){var C=n.trim(y.nodeValue).length;
if(!a.isBlock(y.parentNode)||C>0||C===0&&q(y)){return
}}else{if(z==1){B=y.childNodes;
if(B.length==1&&B[0]&&B[0].nodeType==1&&B[0].getAttribute("data-mce-type")=="bookmark"){y.parentNode.insertBefore(B[0],y)
}if(B.length||/^(br|hr|input|img)$/i.test(y.nodeName)){return
}}}a.remove(y)
}return y
}if(e&&f){r.setStart(e.parentNode,a.nodeIndex(e));
r.setEnd(f.parentNode,a.nodeIndex(f));
d=r.extractContents();
r=a.createRng();
r.setStart(f.parentNode,a.nodeIndex(f)+1);
r.setEnd(e.parentNode,a.nodeIndex(e)+1);
g=r.extractContents();
c=e.parentNode;
c.insertBefore(h(d),e);
if(b){c.replaceChild(b,f)
}else{c.insertBefore(f,e)
}c.insertBefore(h(g),e);
a.remove(e);
return b||f
}},bind:function(a,d,b,c){return this.events.add(a,d,b,c||this)
},unbind:function(a,c,b){return this.events.remove(a,c,b)
},fire:function(a,b,c){return this.events.fire(a,b,c)
},getContentEditable:function(a){var b;
if(a.nodeType!=1){return null
}b=a.getAttribute("data-mce-contenteditable");
if(b&&b!=="inherit"){return b
}return a.contentEditable!=="inherit"?a.contentEditable:null
},_findSib:function(b,e,d){var c=this,a=e;
if(b){if(o(a,"string")){a=function(f){return c.is(f,e)
}
}for(b=b[d];
b;
b=b[d]){if(a(b)){return b
}}}return null
},_isRes:function(a){return/^(top|left|bottom|right|width|height)/i.test(a)||/;\s*(top|left|bottom|right|width|height)/i.test(a)
}});
n.DOM=new n.dom.DOMUtils(document,{process_html:0})
})(tinymce);
(function(d){function c(aN){var ac=this,aL=aN.doc,t=0,al=1,aG=2,am=true,Y=false,a="startOffset",aI="startContainer",aa="endContainer",aq="endOffset",aF=tinymce.extend,aC=aN.nodeIndex;
aF(ac,{startContainer:aL,startOffset:0,endContainer:aL,endOffset:0,collapsed:am,commonAncestorContainer:aL,START_TO_START:0,START_TO_END:1,END_TO_END:2,END_TO_START:3,setStart:az,setEnd:ax,setStartBefore:aJ,setStartAfter:ah,setEndBefore:ag,setEndAfter:aw,collapse:ap,selectNode:at,selectNodeContents:ak,compareBoundaryPoints:av,deleteContents:aA,extractContents:ai,cloneContents:aM,insertNode:an,surroundContents:ad,cloneRange:af,toStringIE:X});
function au(){return aL.createDocumentFragment()
}function az(f,e){ao(am,f,e)
}function ax(f,e){ao(Y,f,e)
}function aJ(e){az(e.parentNode,aC(e))
}function ah(e){az(e.parentNode,aC(e)+1)
}function ag(e){ax(e.parentNode,aC(e))
}function aw(e){ax(e.parentNode,aC(e)+1)
}function ap(e){if(e){ac[aa]=ac[aI];
ac[aq]=ac[a]
}else{ac[aI]=ac[aa];
ac[a]=ac[aq]
}ac.collapsed=am
}function at(e){aJ(e);
aw(e)
}function ak(e){az(e,0);
ax(e,e.nodeType===1?e.childNodes.length:e.nodeValue.length)
}function av(e,m){var k=ac[aI],g=ac[a],l=ac[aa],h=ac[aq],n=m.startContainer,i=m.startOffset,f=m.endContainer,j=m.endOffset;
if(e===0){return aj(k,g,n,i)
}if(e===1){return aj(l,h,n,i)
}if(e===2){return aj(l,h,f,j)
}if(e===3){return aj(k,g,f,j)
}}function aA(){aE(aG)
}function ai(){return aE(t)
}function aM(){return aE(al)
}function an(g){var i=this[aI],e=this[a],f,h;
if((i.nodeType===3||i.nodeType===4)&&i.nodeValue){if(!e){i.parentNode.insertBefore(g,i)
}else{if(e>=i.nodeValue.length){aN.insertAfter(g,i)
}else{f=i.splitText(e);
i.parentNode.insertBefore(g,f)
}}}else{if(i.childNodes.length>0){h=i.childNodes[e]
}if(h){i.insertBefore(g,h)
}else{i.appendChild(g)
}}}function ad(f){var e=ac.extractContents();
ac.insertNode(f);
f.appendChild(e);
ac.selectNode(f)
}function af(){return aF(new c(aN),{startContainer:ac[aI],startOffset:ac[a],endContainer:ac[aa],endOffset:ac[aq],collapsed:ac.collapsed,commonAncestorContainer:ac.commonAncestorContainer})
}function ab(e,g){var f;
if(e.nodeType==3){return e
}if(g<0){return e
}f=e.firstChild;
while(f&&g>0){--g;
f=f.nextSibling
}if(f){return f
}return e
}function aD(){return(ac[aI]==ac[aa]&&ac[a]==ac[aq])
}function aj(f,n,h,e){var l,g,m,k,i,j;
if(f==h){if(n==e){return 0
}if(n<e){return -1
}return 1
}l=h;
while(l&&l.parentNode!=f){l=l.parentNode
}if(l){g=0;
m=f.firstChild;
while(m!=l&&g<n){g++;
m=m.nextSibling
}if(n<=g){return -1
}return 1
}l=f;
while(l&&l.parentNode!=h){l=l.parentNode
}if(l){g=0;
m=h.firstChild;
while(m!=l&&g<e){g++;
m=m.nextSibling
}if(g<e){return -1
}return 1
}k=aN.findCommonAncestor(f,h);
i=f;
while(i&&i.parentNode!=k){i=i.parentNode
}if(!i){i=k
}j=h;
while(j&&j.parentNode!=k){j=j.parentNode
}if(!j){j=k
}if(i==j){return 0
}m=k.firstChild;
while(m){if(m==i){return -1
}if(m==j){return 1
}m=m.nextSibling
}}function ao(i,g,f){var e,h;
if(i){ac[aI]=g;
ac[a]=f
}else{ac[aa]=g;
ac[aq]=f
}e=ac[aa];
while(e.parentNode){e=e.parentNode
}h=ac[aI];
while(h.parentNode){h=h.parentNode
}if(h==e){if(aj(ac[aI],ac[a],ac[aa],ac[aq])>0){ac.collapse(i)
}}else{ac.collapse(i)
}ac.collapsed=aD();
ac.commonAncestorContainer=aN.findCommonAncestor(ac[aI],ac[aa])
}function aE(k){var m,f=0,i=0,h,n,g,e,l,j;
if(ac[aI]==ac[aa]){return aK(k)
}for(m=ac[aa],h=m.parentNode;
h;
m=h,h=h.parentNode){if(h==ac[aI]){return ay(m,k)
}++f
}for(m=ac[aI],h=m.parentNode;
h;
m=h,h=h.parentNode){if(h==ac[aa]){return b(m,k)
}++i
}n=i-f;
g=ac[aI];
while(n>0){g=g.parentNode;
n--
}e=ac[aa];
while(n<0){e=e.parentNode;
n++
}for(l=g.parentNode,j=e.parentNode;
l!=j;
l=l.parentNode,j=j.parentNode){g=l;
e=j
}return aB(g,e,k)
}function aK(l){var j,i,m,g,f,k,e,h,n;
if(l!=aG){j=au()
}if(ac[a]==ac[aq]){return j
}if(ac[aI].nodeType==3){i=ac[aI].nodeValue;
m=i.substring(ac[a],ac[aq]);
if(l!=al){g=ac[aI];
h=ac[a];
n=ac[aq]-ac[a];
if(h===0&&n>=g.nodeValue.length-1){g.parentNode.removeChild(g)
}else{g.deleteData(h,n)
}ac.collapse(am)
}if(l==aG){return
}if(m.length>0){j.appendChild(aL.createTextNode(m))
}return j
}g=ab(ac[aI],ac[a]);
f=ac[aq]-ac[a];
while(g&&f>0){k=g.nextSibling;
e=ar(g,l);
if(j){j.appendChild(e)
}--f;
g=k
}if(l!=al){ac.collapse(am)
}return j
}function ay(f,j){var g,h,l,e,i,k;
if(j!=aG){g=au()
}h=aH(f,j);
if(g){g.appendChild(h)
}l=aC(f);
e=l-ac[a];
if(e<=0){if(j!=al){ac.setEndBefore(f);
ac.collapse(Y)
}return g
}h=f.previousSibling;
while(e>0){i=h.previousSibling;
k=ar(h,j);
if(g){g.insertBefore(k,g.firstChild)
}--e;
h=i
}if(j!=al){ac.setEndBefore(f);
ac.collapse(Y)
}return g
}function b(h,j){var f,l,g,e,i,k;
if(j!=aG){f=au()
}g=Z(h,j);
if(f){f.appendChild(g)
}l=aC(h);
++l;
e=ac[aq]-l;
g=h.nextSibling;
while(g&&e>0){i=g.nextSibling;
k=ar(g,j);
if(f){f.appendChild(k)
}--e;
g=i
}if(j!=al){ac.setStartAfter(h);
ac.collapse(am)
}return f
}function aB(o,n,k){var g,i,e,m,l,h,j,f;
if(k!=aG){i=au()
}g=Z(o,k);
if(i){i.appendChild(g)
}e=o.parentNode;
m=aC(o);
l=aC(n);
++m;
h=l-m;
j=o.nextSibling;
while(h>0){f=j.nextSibling;
g=ar(j,k);
if(i){i.appendChild(g)
}j=f;
--h
}g=aH(n,k);
if(i){i.appendChild(g)
}if(k!=al){ac.setStartAfter(o);
ac.collapse(am)
}return i
}function aH(k,j){var g=ab(ac[aa],ac[aq]-1),i,m,e,l,h,f=g!=ac[aa];
if(g==k){return ae(g,f,Y,j)
}i=g.parentNode;
m=ae(i,Y,Y,j);
while(i){while(g){e=g.previousSibling;
l=ae(g,f,Y,j);
if(j!=aG){m.insertBefore(l,m.firstChild)
}f=am;
g=e
}if(i==k){return m
}g=i.previousSibling;
i=i.parentNode;
h=ae(i,Y,Y,j);
if(j!=aG){h.appendChild(m)
}m=h
}}function Z(k,j){var f=ab(ac[aI],ac[a]),e=f!=ac[aI],i,m,g,l,h;
if(f==k){return ae(f,e,am,j)
}i=f.parentNode;
m=ae(i,Y,am,j);
while(i){while(f){g=f.nextSibling;
l=ae(f,e,am,j);
if(j!=aG){m.appendChild(l)
}e=am;
f=g
}if(i==k){return m
}f=i.nextSibling;
i=i.parentNode;
h=ae(i,Y,am,j);
if(j!=aG){h.appendChild(m)
}m=h
}}function ae(l,e,j,i){var f,g,m,h,k;
if(e){return ar(l,i)
}if(l.nodeType==3){f=l.nodeValue;
if(j){h=ac[a];
g=f.substring(h);
m=f.substring(0,h)
}else{h=ac[aq];
g=f.substring(0,h);
m=f.substring(h)
}if(i!=al){l.nodeValue=m
}if(i==aG){return
}k=aN.clone(l,Y);
k.nodeValue=g;
return k
}if(i==aG){return
}return aN.clone(l,Y)
}function ar(f,e){if(e!=aG){return e==al?aN.clone(f,am):f
}f.parentNode.removeChild(f)
}function X(){return aN.create("body",null,aM()).outerText
}return ac
}d.Range=c;
c.prototype.toString=function(){return this.toStringIE()
}
})(tinymce.dom);
(function(){function b(m){var a=this,i=m.dom,n=true,k=false;
function l(y,x){var v,z=0,d,g,h,u,f,c,e=-1,A;
v=y.duplicate();
v.collapse(x);
A=v.parentElement();
if(A.ownerDocument!==m.dom.doc){return
}while(A.contentEditable==="false"){A=A.parentNode
}if(!A.hasChildNodes()){return{node:A,inside:1}
}h=A.children;
d=h.length-1;
while(z<=d){c=Math.floor((z+d)/2);
u=h[c];
v.moveToElementText(u);
e=v.compareEndPoints(x?"StartToStart":"EndToEnd",y);
if(e>0){d=c-1
}else{if(e<0){z=c+1
}else{return{node:u}
}}}if(e<0){if(!u){v.moveToElementText(A);
v.collapse(true);
u=A;
g=true
}else{v.collapse(false)
}f=0;
while(v.compareEndPoints(x?"StartToStart":"StartToEnd",y)!==0){if(v.move("character",1)===0||A!=v.parentElement()){break
}f++
}}else{v.collapse(true);
f=0;
while(v.compareEndPoints(x?"StartToStart":"StartToEnd",y)!==0){if(v.move("character",-1)===0||A!=v.parentElement()){break
}f++
}}return{node:u,position:e,offset:f,inside:g}
}function j(){var v=m.getRng(),c=i.createRng(),s,t,e,d,h,u;
s=v.item?v.item(0):v.parentElement();
if(s.ownerDocument!=i.doc){return c
}t=m.isCollapsed();
if(v.item){c.setStart(s.parentNode,i.nodeIndex(s));
c.setEnd(c.startContainer,c.startOffset+1);
return c
}function f(p){var r=l(v,p),C,E,D=0,o,q,B;
C=r.node;
E=r.offset;
if(r.inside&&!C.hasChildNodes()){c[p?"setStart":"setEnd"](C,0);
return
}if(E===q){c[p?"setStartBefore":"setEndAfter"](C);
return
}if(r.position<0){o=r.inside?C.firstChild:C.nextSibling;
if(!o){c[p?"setStartAfter":"setEndAfter"](C);
return
}if(!E){if(o.nodeType==3){c[p?"setStart":"setEnd"](o,0)
}else{c[p?"setStartBefore":"setEndBefore"](o)
}return
}while(o){B=o.nodeValue;
D+=B.length;
if(D>=E){C=o;
D-=E;
D=B.length-D;
break
}o=o.nextSibling
}}else{o=C.previousSibling;
if(!o){return c[p?"setStartBefore":"setEndBefore"](C)
}if(!E){if(C.nodeType==3){c[p?"setStart":"setEnd"](o,C.nodeValue.length)
}else{c[p?"setStartAfter":"setEndAfter"](o)
}return
}while(o){D+=o.nodeValue.length;
if(D>=E){C=o;
D-=E;
break
}o=o.previousSibling
}}c[p?"setStart":"setEnd"](C,D)
}try{f(true);
if(!t){f()
}}catch(g){if(g.number==-2147024809){h=a.getBookmark(2);
e=v.duplicate();
e.collapse(true);
s=e.parentElement();
if(!t){e=v.duplicate();
e.collapse(false);
d=e.parentElement();
d.innerHTML=d.innerHTML
}s.innerHTML=s.innerHTML;
a.moveToBookmark(h);
v=m.getRng();
f(true);
if(!t){f()
}}else{throw g
}}return c
}this.getBookmark=function(c){var f=m.getRng(),h,g,d={};
function p(o){var v,A,x,y,z=[];
v=o.parentNode;
A=i.getRoot().parentNode;
while(v!=A&&v.nodeType!==9){x=v.children;
y=x.length;
while(y--){if(o===x[y]){z.push(y);
break
}}o=v;
v=v.parentNode
}return z
}function e(o){var r;
r=l(f,o);
if(r){return{position:r.position,offset:r.offset,indexes:p(r.node),inside:r.inside}
}}if(c===2){if(!f.item){d.start=e(true);
if(!m.isCollapsed()){d.end=e()
}}else{d.start={ctrl:true,indexes:p(f.item(0))}
}}return d
};
this.moveToBookmark=function(e){var f,g=i.doc.body;
function c(u){var h,s,v,t;
h=i.getRoot();
for(s=u.length-1;
s>=0;
s--){t=h.children;
v=u[s];
if(v<=t.length-1){h=t[v]
}}return h
}function d(h){var v=e[h?"start":"end"],s,t,u;
if(v){s=v.position>0;
t=g.createTextRange();
t.moveToElementText(c(v.indexes));
offset=v.offset;
if(offset!==u){t.collapse(v.inside||s);
t.moveStart("character",s?-offset:offset)
}else{t.collapse(h)
}f.setEndPoint(h?"StartToStart":"EndToStart",t);
if(h){f.collapse(true)
}}}if(e.start){if(e.start.ctrl){f=g.createControlRange();
f.addElement(c(e.start.indexes));
f.select()
}else{f=g.createTextRange();
d(true);
d();
f.select()
}}};
this.addRange=function(A){var g,x,y,e,B,d,D,E=m.dom.doc,h=E.body,c,C;
function z(r){var t,s,p,o,q;
p=i.create("a");
t=r?y:B;
s=r?e:d;
o=g.duplicate();
if(t==E||t==E.documentElement){t=h;
s=0
}if(t.nodeType==3){t.parentNode.insertBefore(p,t);
o.moveToElementText(p);
o.moveStart("character",s);
i.remove(p);
g.setEndPoint(r?"StartToStart":"EndToEnd",o)
}else{q=t.childNodes;
if(q.length){if(s>=q.length){i.insertAfter(p,q[q.length-1])
}else{t.insertBefore(p,q[s])
}o.moveToElementText(p)
}else{if(t.canHaveHTML){t.innerHTML="<span>\uFEFF</span>";
p=t.firstChild;
o.moveToElementText(p);
o.collapse(k)
}}g.setEndPoint(r?"StartToStart":"EndToEnd",o);
i.remove(p)
}}y=A.startContainer;
e=A.startOffset;
B=A.endContainer;
d=A.endOffset;
g=h.createTextRange();
if(y==B&&y.nodeType==1){if(e==d&&!y.hasChildNodes()){if(y.canHaveHTML){D=y.previousSibling;
if(D&&!D.hasChildNodes()&&i.isBlock(D)){D.innerHTML="\uFEFF"
}else{D=null
}y.innerHTML="<span>\uFEFF</span><span>\uFEFF</span>";
g.moveToElementText(y.lastChild);
g.select();
i.doc.selection.clear();
y.innerHTML="";
if(D){D.innerHTML=""
}return
}else{e=i.nodeIndex(y);
y=y.parentNode
}}if(e==d-1){try{C=y.childNodes[e];
x=h.createControlRange();
x.addElement(C);
x.select();
c=m.getRng();
if(c.item&&C===c.item(0)){return
}}catch(f){}}}z(true);
z();
g.select()
};
this.getRangeAt=j
}tinymce.dom.TridentSelection=b
})();
(function(){var G=/((?:\((?:\([^()]+\)|[^()]+)+\)|\[(?:\[[^\[\]]*\]|['"][^'"]*['"]|[^\[\]'"]+)+\]|\\.|[^ >+~,(\[\\]+)+|[>+~])(\s*,\s*)?((?:.|\r|\n)*)/g,L="sizcache",F=0,C=Object.prototype.toString,M=false,N=true,D=/\\/g,z=/\r\n/g,e=/\W/;
[0,0].sort(function(){N=false;
return 0
});
var P=function(b,g,q,p){q=q||[];
g=g||document;
var n=g;
if(g.nodeType!==1&&g.nodeType!==9){return[]
}if(!b||typeof b!=="string"){return q
}var j,l,f,k,m,h,i,r,c=true,d=P.isXML(g),a=[],o=b;
do{G.exec("");
j=G.exec(o);
if(j){o=j[3];
a.push(j[1]);
if(j[2]){k=j[3];
break
}}}while(j);
if(a.length>1&&K.exec(b)){if(a.length===2&&J.relative[a[0]]){l=B(a[0]+a[1],g,p)
}else{l=J.relative[a[0]]?[g]:P(a.shift(),g);
while(a.length){b=a.shift();
if(J.relative[b]){b+=a.shift()
}l=B(b,l,p)
}}}else{if(!p&&a.length>1&&g.nodeType===9&&!d&&J.match.ID.test(a[0])&&!J.match.ID.test(a[a.length-1])){m=P.find(a.shift(),g,d);
g=m.expr?P.filter(m.expr,m.set)[0]:m.set[0]
}if(g){m=p?{expr:a.pop(),set:I(p)}:P.find(a.pop(),a.length===1&&(a[0]==="~"||a[0]==="+")&&g.parentNode?g.parentNode:g,d);
l=m.expr?P.filter(m.expr,m.set):m.set;
if(a.length>0){f=I(l)
}else{c=false
}while(a.length){h=a.pop();
i=h;
if(!J.relative[h]){h=""
}else{i=a.pop()
}if(i==null){i=g
}J.relative[h](f,i,d)
}}else{f=a=[]
}}if(!f){f=l
}if(!f){P.error(h||b)
}if(C.call(f)==="[object Array]"){if(!c){q.push.apply(q,f)
}else{if(g&&g.nodeType===1){for(r=0;
f[r]!=null;
r++){if(f[r]&&(f[r]===true||f[r].nodeType===1&&P.contains(g,f[r]))){q.push(l[r])
}}}else{for(r=0;
f[r]!=null;
r++){if(f[r]&&f[r].nodeType===1){q.push(l[r])
}}}}}else{I(f,q)
}if(k){P(k,n,q,p);
P.uniqueSort(q)
}return q
};
P.uniqueSort=function(b){if(E){M=N;
b.sort(E);
if(M){for(var a=1;
a<b.length;
a++){if(b[a]===b[a-1]){b.splice(a--,1)
}}}}return b
};
P.matches=function(a,b){return P(a,null,null,b)
};
P.matchesSelector=function(a,b){return P(b,null,null,[a]).length>0
};
P.find=function(j,f,i){var a,g,c,d,b,h;
if(!j){return[]
}for(g=0,c=J.order.length;
g<c;
g++){b=J.order[g];
if((d=J.leftMatch[b].exec(j))){h=d[1];
d.splice(1,1);
if(h.substr(h.length-1)!=="\\"){d[1]=(d[1]||"").replace(D,"");
a=J.find[b](d,f,i);
if(a!=null){j=j.replace(J.match[b],"");
break
}}}}if(!a){a=typeof f.getElementsByTagName!=="undefined"?f.getElementsByTagName("*"):[]
}return{set:a,expr:j}
};
P.filter=function(n,o,j,c){var a,g,p,f,l,k,d,b,m,i=n,h=[],q=o,r=o&&o[0]&&P.isXML(o[0]);
while(n&&o.length){for(p in J.filter){if((a=J.leftMatch[p].exec(n))!=null&&a[2]){k=J.filter[p];
d=a[1];
g=false;
a.splice(1,1);
if(d.substr(d.length-1)==="\\"){continue
}if(q===h){h=[]
}if(J.preFilter[p]){a=J.preFilter[p](a,q,j,h,c,r);
if(!a){g=f=true
}else{if(a===true){continue
}}}if(a){for(b=0;
(l=q[b])!=null;
b++){if(l){f=k(l,a,b,q);
m=c^f;
if(j&&f!=null){if(m){g=true
}else{q[b]=false
}}else{if(m){h.push(l);
g=true
}}}}}if(f!==undefined){if(!j){q=h
}n=n.replace(J.match[p],"");
if(!g){return[]
}break
}}}if(n===i){if(g==null){P.error(n)
}else{break
}}i=n
}return q
};
P.error=function(a){throw new Error("Syntax error, unrecognized expression: "+a)
};
var R=P.getText=function(f){var c,a,b=f.nodeType,d="";
if(b){if(b===1||b===9||b===11){if(typeof f.textContent==="string"){return f.textContent
}else{if(typeof f.innerText==="string"){return f.innerText.replace(z,"")
}else{for(f=f.firstChild;
f;
f=f.nextSibling){d+=R(f)
}}}}else{if(b===3||b===4){return f.nodeValue
}}}else{for(c=0;
(a=f[c]);
c++){if(a.nodeType!==8){d+=R(a)
}}}return d
};
var J=P.selectors={order:["ID","NAME","TAG"],match:{ID:/#((?:[\w\u00c0-\uFFFF\-]|\\.)+)/,CLASS:/\.((?:[\w\u00c0-\uFFFF\-]|\\.)+)/,NAME:/\[name=['"]*((?:[\w\u00c0-\uFFFF\-]|\\.)+)['"]*\]/,ATTR:/\[\s*((?:[\w\u00c0-\uFFFF\-]|\\.)+)\s*(?:(\S?=)\s*(?:(['"])(.*?)\3|(#?(?:[\w\u00c0-\uFFFF\-]|\\.)*)|)|)\s*\]/,TAG:/^((?:[\w\u00c0-\uFFFF\*\-]|\\.)+)/,CHILD:/:(only|nth|last|first)-child(?:\(\s*(even|odd|(?:[+\-]?\d+|(?:[+\-]?\d*)?n\s*(?:[+\-]\s*\d+)?))\s*\))?/,POS:/:(nth|eq|gt|lt|first|last|even|odd)(?:\((\d*)\))?(?=[^\-]|$)/,PSEUDO:/:((?:[\w\u00c0-\uFFFF\-]|\\.)+)(?:\((['"]?)((?:\([^\)]+\)|[^\(\)]*)+)\2\))?/},leftMatch:{},attrMap:{"class":"className","for":"htmlFor"},attrHandle:{href:function(a){return a.getAttribute("href")
},type:function(a){return a.getAttribute("type")
}},relative:{"+":function(d,i){var a=typeof i==="string",g=a&&!e.test(i),b=a&&!g;
if(g){i=i.toLowerCase()
}for(var f=0,c=d.length,h;
f<c;
f++){if((h=d[f])){while((h=h.previousSibling)&&h.nodeType!==1){}d[f]=b||h&&h.nodeName.toLowerCase()===i?h||false:h===i
}}if(b){P.filter(i,d,true)
}},">":function(c,h){var f,g=typeof h==="string",d=0,b=c.length;
if(g&&!e.test(h)){h=h.toLowerCase();
for(;
d<b;
d++){f=c[d];
if(f){var a=f.parentNode;
c[d]=a.nodeName.toLowerCase()===h?a:false
}}}else{for(;
d<b;
d++){f=c[d];
if(f){c[d]=g?f.parentNode:f.parentNode===h
}}if(g){P.filter(h,c,true)
}}},"":function(a,g,d){var f,c=F++,b=A;
if(typeof g==="string"&&!e.test(g)){g=g.toLowerCase();
f=g;
b=S
}b("parentNode",g,c,a,f,d)
},"~":function(a,g,d){var f,c=F++,b=A;
if(typeof g==="string"&&!e.test(g)){g=g.toLowerCase();
f=g;
b=S
}b("previousSibling",g,c,a,f,d)
}},find:{ID:function(d,c,a){if(typeof c.getElementById!=="undefined"&&!a){var b=c.getElementById(d[1]);
return b&&b.parentNode?[b]:[]
}},NAME:function(d,c){if(typeof c.getElementsByName!=="undefined"){var g=[],f=c.getElementsByName(d[1]);
for(var a=0,b=f.length;
a<b;
a++){if(f[a].getAttribute("name")===d[1]){g.push(f[a])
}}return g.length===0?null:g
}},TAG:function(a,b){if(typeof b.getElementsByTagName!=="undefined"){return b.getElementsByTagName(a[1])
}}},preFilter:{CLASS:function(a,i,g,d,c,b){a=" "+a[1].replace(D,"")+" ";
if(b){return a
}for(var h=0,f;
(f=i[h])!=null;
h++){if(f){if(c^(f.className&&(" "+f.className+" ").replace(/[\t\n\r]/g," ").indexOf(a)>=0)){if(!g){d.push(f)
}}else{if(g){i[h]=false
}}}}return false
},ID:function(a){return a[1].replace(D,"")
},TAG:function(b,a){return b[1].replace(D,"").toLowerCase()
},CHILD:function(a){if(a[1]==="nth"){if(!a[2]){P.error(a[0])
}a[2]=a[2].replace(/^\+|\s*/g,"");
var b=/(-?)(\d*)(?:n([+\-]?\d*))?/.exec(a[2]==="even"&&"2n"||a[2]==="odd"&&"2n+1"||!/\D/.test(a[2])&&"0n+"+a[2]||a[2]);
a[2]=(b[1]+(b[2]||1))-0;
a[3]=b[3]-0
}else{if(a[2]){P.error(a[0])
}}a[0]=F++;
return a
},ATTR:function(h,g,f,c,d,b){var a=h[1]=h[1].replace(D,"");
if(!b&&J.attrMap[a]){h[1]=J.attrMap[a]
}h[4]=(h[4]||h[5]||"").replace(D,"");
if(h[2]==="~="){h[4]=" "+h[4]+" "
}return h
},PSEUDO:function(g,f,d,b,c){if(g[1]==="not"){if((G.exec(g[3])||"").length>1||/^\w/.test(g[3])){g[3]=P(g[3],null,null,f)
}else{var a=P.filter(g[3],f,d,true^c);
if(!d){b.push.apply(b,a)
}return false
}}else{if(J.match.POS.test(g[0])||J.match.CHILD.test(g[0])){return true
}}return g
},POS:function(a){a.unshift(true);
return a
}},filters:{enabled:function(a){return a.disabled===false&&a.type!=="hidden"
},disabled:function(a){return a.disabled===true
},checked:function(a){return a.checked===true
},selected:function(a){if(a.parentNode){a.parentNode.selectedIndex
}return a.selected===true
},parent:function(a){return !!a.firstChild
},empty:function(a){return !a.firstChild
},has:function(b,c,a){return !!P(a[3],b).length
},header:function(a){return(/h\d/i).test(a.nodeName)
},text:function(b){var a=b.getAttribute("type"),c=b.type;
return b.nodeName.toLowerCase()==="input"&&"text"===c&&(a===c||a===null)
},radio:function(a){return a.nodeName.toLowerCase()==="input"&&"radio"===a.type
},checkbox:function(a){return a.nodeName.toLowerCase()==="input"&&"checkbox"===a.type
},file:function(a){return a.nodeName.toLowerCase()==="input"&&"file"===a.type
},password:function(a){return a.nodeName.toLowerCase()==="input"&&"password"===a.type
},submit:function(b){var a=b.nodeName.toLowerCase();
return(a==="input"||a==="button")&&"submit"===b.type
},image:function(a){return a.nodeName.toLowerCase()==="input"&&"image"===a.type
},reset:function(b){var a=b.nodeName.toLowerCase();
return(a==="input"||a==="button")&&"reset"===b.type
},button:function(b){var a=b.nodeName.toLowerCase();
return a==="input"&&"button"===b.type||a==="button"
},input:function(a){return(/input|select|textarea|button/i).test(a.nodeName)
},focus:function(a){return a===a.ownerDocument.activeElement
}},setFilters:{first:function(b,a){return a===0
},last:function(c,d,b,a){return d===a.length-1
},even:function(b,a){return a%2===0
},odd:function(b,a){return a%2===1
},lt:function(b,c,a){return c<a[3]-0
},gt:function(b,c,a){return c>a[3]-0
},nth:function(b,c,a){return a[3]-0===c
},eq:function(b,c,a){return a[3]-0===c
}},filter:{PSEUDO:function(g,j,a,i){var f=j[1],h=J.filters[f];
if(h){return h(g,a,j,i)
}else{if(f==="contains"){return(g.textContent||g.innerText||R([g])||"").indexOf(j[3])>=0
}else{if(f==="not"){var d=j[3];
for(var b=0,c=d.length;
b<c;
b++){if(d[b]===g){return false
}}return true
}else{P.error(f)
}}}},CHILD:function(g,c){var d,i,a,j,f,b,k,l=c[1],h=g;
switch(l){case"only":case"first":while((h=h.previousSibling)){if(h.nodeType===1){return false
}}if(l==="first"){return true
}h=g;
case"last":while((h=h.nextSibling)){if(h.nodeType===1){return false
}}return true;
case"nth":d=c[2];
i=c[3];
if(d===1&&i===0){return true
}a=c[0];
j=g.parentNode;
if(j&&(j[L]!==a||!g.nodeIndex)){b=0;
for(h=j.firstChild;
h;
h=h.nextSibling){if(h.nodeType===1){h.nodeIndex=++b
}}j[L]=a
}k=g.nodeIndex-i;
if(d===0){return k===0
}else{return(k%d===0&&k/d>=0)
}}},ID:function(b,a){return b.nodeType===1&&b.getAttribute("id")===a
},TAG:function(b,a){return(a==="*"&&b.nodeType===1)||!!b.nodeName&&b.nodeName.toLowerCase()===a
},CLASS:function(b,a){return(" "+(b.className||b.getAttribute("class"))+" ").indexOf(a)>-1
},ATTR:function(f,a){var d=a[1],c=P.attr?P.attr(f,d):J.attrHandle[d]?J.attrHandle[d](f):f[d]!=null?f[d]:f.getAttribute(d),b=c+"",h=a[2],g=a[4];
return c==null?h==="!=":!h&&P.attr?c!=null:h==="="?b===g:h==="*="?b.indexOf(g)>=0:h==="~="?(" "+b+" ").indexOf(g)>=0:!g?b&&c!==false:h==="!="?b!==g:h==="^="?b.indexOf(g)===0:h==="$="?b.substr(b.length-g.length)===g:h==="|="?b===g||b.substr(0,g.length+1)===g+"-":false
},POS:function(g,f,d,c){var b=f[2],a=J.setFilters[b];
if(a){return a(g,d,f,c)
}}}};
var K=J.match.POS,Q=function(b,a){return"\\"+(a-0+1)
};
for(var O in J.match){J.match[O]=new RegExp(J.match[O].source+(/(?![^\[]*\])(?![^\(]*\))/.source));
J.leftMatch[O]=new RegExp(/(^(?:.|\r|\n)*?)/.source+J.match[O].source.replace(/\\(\d+)/g,Q))
}J.match.globalPOS=K;
var I=function(b,a){b=Array.prototype.slice.call(b,0);
if(a){a.push.apply(a,b);
return a
}return b
};
try{Array.prototype.slice.call(document.documentElement.childNodes,0)[0].nodeType
}catch(y){I=function(f,a){var c=0,d=a||[];
if(C.call(f)==="[object Array]"){Array.prototype.push.apply(d,f)
}else{if(typeof f.length==="number"){for(var b=f.length;
c<b;
c++){d.push(f[c])
}}else{for(;
f[c];
c++){d.push(f[c])
}}}return d
}
}var E,H;
if(document.documentElement.compareDocumentPosition){E=function(b,a){if(b===a){M=true;
return 0
}if(!b.compareDocumentPosition||!a.compareDocumentPosition){return b.compareDocumentPosition?-1:1
}return b.compareDocumentPosition(a)&4?-1:1
}
}else{E=function(j,k){if(j===k){M=true;
return 0
}else{if(j.sourceIndex&&k.sourceIndex){return j.sourceIndex-k.sourceIndex
}}var b,h,g=[],f=[],c=j.parentNode,a=k.parentNode,i=c;
if(c===a){return H(j,k)
}else{if(!c){return -1
}else{if(!a){return 1
}}}while(i){g.unshift(i);
i=i.parentNode
}i=a;
while(i){f.unshift(i);
i=i.parentNode
}b=g.length;
h=f.length;
for(var d=0;
d<b&&d<h;
d++){if(g[d]!==f[d]){return H(g[d],f[d])
}}return d===b?H(j,f[d],-1):H(g[d],k,1)
};
H=function(d,b,c){if(d===b){return c
}var a=d.nextSibling;
while(a){if(a===b){return -1
}a=a.nextSibling
}return 1
}
}(function(){var c=document.createElement("div"),b="script"+(new Date()).getTime(),a=document.documentElement;
c.innerHTML="<a name='"+b+"'/>";
a.insertBefore(c,a.firstChild);
if(document.getElementById(b)){J.find.ID=function(h,g,f){if(typeof g.getElementById!=="undefined"&&!f){var d=g.getElementById(h[1]);
return d?d.id===h[1]||typeof d.getAttributeNode!=="undefined"&&d.getAttributeNode("id").nodeValue===h[1]?[d]:undefined:[]
}};
J.filter.ID=function(f,d){var g=typeof f.getAttributeNode!=="undefined"&&f.getAttributeNode("id");
return f.nodeType===1&&g&&g.nodeValue===d
}
}a.removeChild(c);
a=c=null
})();
(function(){var a=document.createElement("div");
a.appendChild(document.createComment(""));
if(a.getElementsByTagName("*").length>0){J.find.TAG=function(g,d){var f=d.getElementsByTagName(g[1]);
if(g[1]==="*"){var b=[];
for(var c=0;
f[c];
c++){if(f[c].nodeType===1){b.push(f[c])
}}f=b
}return f
}
}a.innerHTML="<a href='#'></a>";
if(a.firstChild&&typeof a.firstChild.getAttribute!=="undefined"&&a.firstChild.getAttribute("href")!=="#"){J.attrHandle.href=function(b){return b.getAttribute("href",2)
}
}a=null
})();
if(document.querySelectorAll){(function(){var b=P,a=document.createElement("div"),c="__sizzle__";
a.innerHTML="<p class='TEST'></p>";
if(a.querySelectorAll&&a.querySelectorAll(".TEST").length===0){return
}P=function(k,g,p,l){g=g||document;
if(!l&&!P.isXML(g)){var m=/^(\w+$)|^\.([\w\-]+$)|^#([\w\-]+$)/.exec(k);
if(m&&(g.nodeType===1||g.nodeType===9)){if(m[1]){return I(g.getElementsByTagName(k),p)
}else{if(m[2]&&J.find.CLASS&&g.getElementsByClassName){return I(g.getElementsByClassName(m[2]),p)
}}}if(g.nodeType===9){if(k==="body"&&g.body){return I([g.body],p)
}else{if(m&&m[3]){var q=g.getElementById(m[3]);
if(q&&q.parentNode){if(q.id===m[3]){return I([q],p)
}}else{return I([],p)
}}}try{return I(g.querySelectorAll(k),p)
}catch(o){}}else{if(g.nodeType===1&&g.nodeName.toLowerCase()!=="object"){var f=g,r=g.getAttribute("id"),h=r||c,i=g.parentNode,j=/^\s*[+~]/.test(k);
if(!r){g.setAttribute("id",h)
}else{h=h.replace(/'/g,"\\$&")
}if(j&&i){g=g.parentNode
}try{if(!j||i){return I(g.querySelectorAll("[id='"+h+"'] "+k),p)
}}catch(n){}finally{if(!r){f.removeAttribute("id")
}}}}}return b(k,g,p,l)
};
for(var d in b){P[d]=b[d]
}a=null
})()
}(function(){var b=document.documentElement,c=b.matchesSelector||b.mozMatchesSelector||b.webkitMatchesSelector||b.msMatchesSelector;
if(c){var f=!c.call(document.createElement("div"),"div"),d=false;
try{c.call(document.documentElement,"[test!='']:sizzle")
}catch(a){d=true
}P.matchesSelector=function(i,g){g=g.replace(/\=\s*([^'"\]]*)\s*\]/g,"='$1']");
if(!P.isXML(i)){try{if(d||!J.match.PSEUDO.test(g)&&!/!=/.test(g)){var j=c.call(i,g);
if(j||!f||i.document&&i.document.nodeType!==11){return j
}}}catch(h){}}return P(g,null,null,[i]).length>0
}
}})();
(function(){var a=document.createElement("div");
a.innerHTML="<div class='test e'></div><div class='test'></div>";
if(!a.getElementsByClassName||a.getElementsByClassName("e").length===0){return
}a.lastChild.className="e";
if(a.getElementsByClassName("e").length===1){return
}J.order.splice(1,0,"CLASS");
J.find.CLASS=function(d,c,b){if(typeof c.getElementsByClassName!=="undefined"&&!b){return c.getElementsByClassName(d[1])
}};
a=null
})();
function S(h,a,b,i,k,j){for(var d=0,g=i.length;
d<g;
d++){var f=i[d];
if(f){var c=false;
f=f[h];
while(f){if(f[L]===b){c=i[f.sizset];
break
}if(f.nodeType===1&&!j){f[L]=b;
f.sizset=d
}if(f.nodeName.toLowerCase()===a){c=f;
break
}f=f[h]
}i[d]=c
}}}function A(h,a,b,i,k,j){for(var d=0,g=i.length;
d<g;
d++){var f=i[d];
if(f){var c=false;
f=f[h];
while(f){if(f[L]===b){c=i[f.sizset];
break
}if(f.nodeType===1){if(!j){f[L]=b;
f.sizset=d
}if(typeof a!=="string"){if(f===a){c=true;
break
}}else{if(P.filter(a,[f]).length>0){c=f;
break
}}}f=f[h]
}i[d]=c
}}}if(document.documentElement.contains){P.contains=function(b,a){return b!==a&&(b.contains?b.contains(a):true)
}
}else{if(document.documentElement.compareDocumentPosition){P.contains=function(b,a){return !!(b.compareDocumentPosition(a)&16)
}
}else{P.contains=function(){return false
}
}}P.isXML=function(a){var b=(a?a.ownerDocument||a:0).documentElement;
return b?b.nodeName!=="HTML":false
};
var B=function(g,f,a){var b,j=[],c="",i=f.nodeType?[f]:f;
while((b=J.match.PSEUDO.exec(g))){c+=b[0];
g=g.replace(J.match.PSEUDO,"")
}g=J.relative[g]?g+"*":g;
for(var d=0,h=i.length;
d<h;
d++){P(g,i[d],j,a)
}return P.filter(c,j)
};
window.tinymce.dom.Sizzle=P
})();
(function(b){b.dom.Element=function(g,i){var a=this,h,j;
a.settings=i=i||{};
a.id=g;
a.dom=h=i.dom||b.DOM;
if(!b.isIE){j=h.get(a.id)
}b.each(("getPos,getRect,getParent,add,setStyle,getStyle,setStyles,setAttrib,setAttribs,getAttrib,addClass,removeClass,hasClass,getOuterHTML,setOuterHTML,remove,show,hide,isHidden,setHTML,get").split(/,/),function(c){a[c]=function(){var e=[g],d;
for(d=0;
d<arguments.length;
d++){e.push(arguments[d])
}e=h[c].apply(h,e);
a.update(c);
return e
}
});
b.extend(a,{on:function(c,d,e){return b.dom.Event.add(a.id,c,d,e)
},getXY:function(){return{x:parseInt(a.getStyle("left")),y:parseInt(a.getStyle("top"))}
},getSize:function(){var c=h.get(a.id);
return{w:parseInt(a.getStyle("width")||c.clientWidth),h:parseInt(a.getStyle("height")||c.clientHeight)}
},moveTo:function(d,c){a.setStyles({left:d,top:c})
},moveBy:function(e,c){var d=a.getXY();
a.moveTo(d.x+e,d.y+c)
},resizeTo:function(d,c){a.setStyles({width:d,height:c})
},resizeBy:function(e,c){var d=a.getSize();
a.resizeTo(d.w+e,d.h+c)
},update:function(c){var d;
if(b.isIE6&&i.blocker){c=c||"";
if(c.indexOf("get")===0||c.indexOf("has")===0||c.indexOf("is")===0){return
}if(c=="remove"){h.remove(a.blocker);
return
}if(!a.blocker){a.blocker=h.uniqueId();
d=h.add(i.container||h.getRoot(),"iframe",{id:a.blocker,style:"position:absolute;",frameBorder:0,src:'javascript:""'});
h.setStyle(d,"opacity",0)
}else{d=h.get(a.blocker)
}h.setStyles(d,{left:a.getStyle("left",1),top:a.getStyle("top",1),width:a.getStyle("width",1),height:a.getStyle("height",1),display:a.getStyle("display",1),zIndex:parseInt(a.getStyle("zIndex",1)||0)-1})
}}})
}
})(tinymce);
(function(k){function i(a){return a.replace(/[\n\r]+/g,"")
}var l=k.is,g=k.isIE,j=k.each,h=k.dom.TreeWalker;
k.create("tinymce.dom.Selection",{Selection:function(a,b,c,d){var e=this;
e.dom=a;
e.win=b;
e.serializer=c;
e.editor=d;
j(["onBeforeSetContent","onBeforeGetContent","onSetContent","onGetContent"],function(f){e[f]=new k.util.Dispatcher(e)
});
if(!e.win.getSelection){e.tridentSel=new k.dom.TridentSelection(e)
}if(k.isIE&&!k.isIE11&&a.boxModel){this._fixIESelection()
}k.addUnload(e.destroy,e)
},setCursorLocation:function(b,a){var d=this;
var c=d.dom.createRng();
c.setStart(b,a);
c.setEnd(b,a);
d.setRng(c);
d.collapse(false)
},getContent:function(f){var n=this,e=n.getRng(),a=n.dom.create("body"),c=n.getSel(),d,b,p;
f=f||{};
d=b="";
f.get=true;
f.format=f.format||"html";
f.forced_root_block="";
n.onBeforeGetContent.dispatch(n,f);
if(f.format=="text"){return n.isCollapsed()?"":(e.text||(c.toString?c.toString():""))
}if(e.cloneContents){p=e.cloneContents();
if(p){a.appendChild(p)
}}else{if(l(e.item)||l(e.htmlText)){a.innerHTML="<br>"+(e.item?e.item(0).outerHTML:e.htmlText);
a.removeChild(a.firstChild)
}else{a.innerHTML=e.toString()
}}if(/^\s/.test(a.innerHTML)){d=" "
}if(/\s+$/.test(a.innerHTML)){b=" "
}f.getInner=true;
f.content=n.isCollapsed()?"":d+n.serializer.serialize(a,f)+b;
n.onGetContent.dispatch(n,f);
return f.content
},setContent:function(q,f){var a=this,r=a.getRng(),e,d=a.win.document,b,c;
f=f||{format:"html"};
f.set=true;
q=f.content=q;
if(!f.no_events){a.onBeforeSetContent.dispatch(a,f)
}q=f.content;
if(r.insertNode){q+='<span id="__caret">_</span>';
if(r.startContainer==d&&r.endContainer==d){d.body.innerHTML=q
}else{r.deleteContents();
if(d.body.childNodes.length===0){d.body.innerHTML=q
}else{if(r.createContextualFragment){r.insertNode(r.createContextualFragment(q))
}else{b=d.createDocumentFragment();
c=d.createElement("div");
b.appendChild(c);
c.outerHTML=q;
r.insertNode(b)
}}}e=a.dom.get("__caret");
r=d.createRange();
r.setStartBefore(e);
r.setEndBefore(e);
a.setRng(r);
a.dom.remove("__caret");
try{a.setRng(r)
}catch(p){}}else{if(r.item){d.execCommand("Delete",false,null);
r=a.getRng()
}if(/^\s+/.test(q)){r.pasteHTML('<span id="__mce_tmp">_</span>'+q);
a.dom.remove("__mce_tmp")
}else{r.pasteHTML(q)
}}if(!f.no_events){a.onSetContent.dispatch(a,f)
}},getStart:function(){var d=this,e=d.getRng(),c,f,a,b;
if(e.duplicate||e.item){if(e.item){return e.item(0)
}a=e.duplicate();
a.collapse(1);
c=a.parentElement();
if(c.ownerDocument!==d.dom.doc){c=d.dom.getRoot()
}f=b=e.parentElement();
while(b=b.parentNode){if(b==c){c=f;
break
}}return c
}else{c=e.startContainer;
if(c.nodeType==1&&c.hasChildNodes()){c=c.childNodes[Math.min(c.childNodes.length-1,e.startOffset)]
}if(c&&c.nodeType==3){return c.parentNode
}return c
}},getEnd:function(){var c=this,d=c.getRng(),a,b;
if(d.duplicate||d.item){if(d.item){return d.item(0)
}d=d.duplicate();
d.collapse(0);
a=d.parentElement();
if(a.ownerDocument!==c.dom.doc){a=c.dom.getRoot()
}if(a&&a.nodeName=="BODY"){return a.lastChild||a
}return a
}else{a=d.endContainer;
b=d.endOffset;
if(a.nodeType==1&&a.hasChildNodes()){a=a.childNodes[b>0?b-1:b]
}if(a&&a.nodeType==3){return a.parentNode
}return a
}},getBookmark:function(I,G){var E=this,e=E.dom,C,z,A,d,B,c,b,f="\uFEFF",F;
function D(o,m){var n=0;
j(e.select(o),function(p,q){if(p==m){n=q
}});
return n
}function H(m){function n(p){var o,q,r,s=p?"start":"end";
o=m[s+"Container"];
q=m[s+"Offset"];
if(o.nodeType==1&&o.nodeName=="TR"){r=o.childNodes;
o=r[Math.min(p?q:q-1,r.length-1)];
if(o){q=p?0:o.childNodes.length;
m["set"+(p?"Start":"End")](o,q)
}}}n(true);
n();
return m
}function t(){var o=E.getRng(true),n=e.getRoot(),m={};
function p(x,q){var y=x[q?"startContainer":"endContainer"],r=x[q?"startOffset":"endOffset"],K=[],v,s,u=0;
if(y.nodeType==3){if(G){for(v=y.previousSibling;
v&&v.nodeType==3;
v=v.previousSibling){r+=v.nodeValue.length
}}K.push(r)
}else{s=y.childNodes;
if(r>=s.length&&s.length){u=1;
r=Math.max(0,s.length-1)
}K.push(E.dom.nodeIndex(s[r],G)+u)
}for(;
y&&y!=n;
y=y.parentNode){K.push(E.dom.nodeIndex(y,G))
}return K
}m.start=p(o,true);
if(!E.isCollapsed()){m.end=p(o)
}return m
}if(I==2){if(E.tridentSel){return E.tridentSel.getBookmark(I)
}return t()
}if(I){C=E.getRng();
if(C.setStart){C={startContainer:C.startContainer,startOffset:C.startOffset,endContainer:C.endContainer,endOffset:C.endOffset}
}return{rng:C}
}C=E.getRng();
A=e.uniqueId();
d=tinyMCE.activeEditor.selection.isCollapsed();
F="overflow:hidden;line-height:0px";
if(C.duplicate||C.item){if(!C.item){z=C.duplicate();
try{C.collapse();
C.pasteHTML('<span data-mce-type="bookmark" id="'+A+'_start" style="'+F+'">'+f+"</span>");
if(!d){z.collapse(false);
C.moveToElementText(z.parentElement());
if(C.compareEndPoints("StartToEnd",z)===0){z.move("character",-1)
}z.pasteHTML('<span data-mce-type="bookmark" id="'+A+'_end" style="'+F+'">'+f+"</span>")
}}catch(a){return null
}}else{c=C.item(0);
B=c.nodeName;
return{name:B,index:D(B,c)}
}}else{c=E.getNode();
B=c.nodeName;
if(B=="IMG"){return{name:B,index:D(B,c)}
}z=H(C.cloneRange());
if(!d){z.collapse(false);
z.insertNode(e.create("span",{"data-mce-type":"bookmark",id:A+"_end",style:F},f))
}C=H(C);
C.collapse(true);
C.insertNode(e.create("span",{"data-mce-type":"bookmark",id:A+"_start",style:F},f))
}E.moveToBookmark({id:A,keep:1});
return{id:A}
},moveToBookmark:function(b){var E=this,e=E.dom,t,z,C,A,F,y,D,a,G;
function B(p){var n=b[p?"start":"end"],o,m,r,q;
if(n){r=n[0];
for(m=F,o=n.length-1;
o>=1;
o--){q=m.childNodes;
if(n[o]>q.length-1){return
}m=q[n[o]]
}if(m.nodeType===3){r=Math.min(n[0],m.nodeValue.length)
}if(m.nodeType===1){r=Math.min(n[0],m.childNodes.length)
}if(p){C.setStart(m,r)
}else{C.setEnd(m,r)
}}return true
}function f(o){var s=e.get(b.id+"_"+o),q,n,m,r,p=b.keep;
if(s){q=s.parentNode;
if(o=="start"){if(!p){n=e.nodeIndex(s)
}else{q=s.firstChild;
n=1
}y=D=q;
a=G=n
}else{if(!p){n=e.nodeIndex(s)
}else{q=s.firstChild;
n=1
}D=q;
G=n
}if(!p){r=s.previousSibling;
m=s.nextSibling;
j(k.grep(s.childNodes),function(u){if(u.nodeType==3){u.nodeValue=u.nodeValue.replace(/\uFEFF/g,"")
}});
while(s=e.get(b.id+"_"+o)){e.remove(s,1)
}if(r&&m&&r.nodeType==m.nodeType&&r.nodeType==3&&!k.isOpera){n=r.nodeValue.length;
r.appendData(m.nodeValue);
e.remove(m);
if(o=="start"){y=D=r;
a=G=n
}else{D=r;
G=n
}}}}}function d(m){if(e.isBlock(m)&&!m.innerHTML&&!g){m.innerHTML='<br data-mce-bogus="1" />'
}return m
}if(b){if(b.start){C=e.createRng();
F=e.getRoot();
if(E.tridentSel){return E.tridentSel.moveToBookmark(b)
}if(B(true)&&B()){E.setRng(C)
}}else{if(b.id){f("start");
f("end");
if(y){C=e.createRng();
C.setStart(d(y),a);
C.setEnd(d(D),G);
E.setRng(C)
}}else{if(b.name){E.select(e.select(b.name)[b.index])
}else{if(b.rng){C=b.rng;
if(C.startContainer){A=E.dom.createRng();
try{A.setStart(C.startContainer,C.startOffset);
A.setEnd(C.endContainer,C.endOffset)
}catch(c){}C=A
}E.setRng(C)
}}}}}},select:function(b,c){var d=this,a=d.dom,f=a.createRng(),n;
function e(r,m){var q=new h(r,r);
do{if(r.nodeType==3&&k.trim(r.nodeValue).length!==0){if(m){f.setStart(r,0)
}else{f.setEnd(r,r.nodeValue.length)
}return
}if(r.nodeName=="BR"){if(m){f.setStartBefore(r)
}else{f.setEndBefore(r)
}return
}}while(r=(m?q.next():q.prev()))
}if(b){n=a.nodeIndex(b);
f.setStart(b.parentNode,n);
f.setEnd(b.parentNode,n+1);
if(c){e(b,1);
e(b)
}d.setRng(f)
}return b
},isCollapsed:function(){var c=this,a=c.getRng(),b=c.getSel();
if(!a||a.item){return false
}if(a.compareEndPoints){return a.compareEndPoints("StartToEnd",a)===0
}return !b||a.collapsed
},collapse:function(d){var b=this,c=b.getRng(),a;
if(c.item){a=c.item(0);
c=b.win.document.body.createTextRange();
c.moveToElementText(a)
}c.collapse(!!d);
b.setRng(c)
},getSel:function(){var a=this,b=this.win;
return b.getSelection?b.getSelection():b.document.selection
},getRng:function(a){var f=this,d,n,b,c=f.win.document;
if(a&&f.tridentSel){return f.tridentSel.getRangeAt(0)
}try{if(d=f.getSel()){n=d.rangeCount>0?d.getRangeAt(0):(d.createRange?d.createRange():c.createRange())
}}catch(e){}if(k.isIE&&!k.isIE11&&n&&n.setStart&&c.selection.createRange().item){b=c.selection.createRange().item(0);
n=c.createRange();
n.setStartBefore(b);
n.setEndAfter(b)
}if(!n){n=c.createRange?c.createRange():c.body.createTextRange()
}if(n.setStart&&n.startContainer.nodeType===9&&n.collapsed){b=f.dom.getRoot();
n.setStart(b,0);
n.setEnd(b,0)
}if(f.selectedRange&&f.explicitRange){if(n.compareBoundaryPoints(n.START_TO_START,f.selectedRange)===0&&n.compareBoundaryPoints(n.END_TO_END,f.selectedRange)===0){n=f.explicitRange
}else{f.selectedRange=null;
f.explicitRange=null
}}return n
},setRng:function(a,e){var b,c=this;
if(!c.tridentSel){b=c.getSel();
if(b){c.explicitRange=a;
try{b.removeAllRanges()
}catch(d){}b.addRange(a);
if(e===false&&b.extend){b.collapse(a.endContainer,a.endOffset);
b.extend(a.startContainer,a.startOffset)
}c.selectedRange=b.rangeCount>0?b.getRangeAt(0):null
}}else{if(a.cloneRange){try{c.tridentSel.addRange(a);
return
}catch(d){}}try{a.select()
}catch(d){}}},setNode:function(a){var b=this;
b.setContent(b.dom.getOuterHTML(a));
return a
},getNode:function(){var e=this,f=e.getRng(),d=e.getSel(),a,b=f.startContainer,n=f.endContainer;
function c(m,s){var r=m;
while(m&&m.nodeType===3&&m.length===0){m=s?m.nextSibling:m.previousSibling
}return m||r
}if(!f){return e.dom.getRoot()
}if(f.setStart){a=f.commonAncestorContainer;
if(!f.collapsed){if(f.startContainer==f.endContainer){if(f.endOffset-f.startOffset<2){if(f.startContainer.hasChildNodes()){a=f.startContainer.childNodes[f.startOffset]
}}}if(b.nodeType===3&&n.nodeType===3){if(b.length===f.startOffset){b=c(b.nextSibling,true)
}else{b=b.parentNode
}if(f.endOffset===0){n=c(n.previousSibling,false)
}else{n=n.parentNode
}if(b&&b===n){return b
}}}if(a&&a.nodeType==3){return a.parentNode
}return a
}return f.item?f.item(0):f.parentElement()
},getSelectedBlocks:function(a,q){var b=this,e=b.dom,c,d,n,f=[];
c=e.getParent(a||b.getStart(),e.isBlock);
d=e.getParent(q||b.getEnd(),e.isBlock);
if(c){f.push(c)
}if(c&&d&&c!=d){n=c;
var r=new h(c,e.getRoot());
while((n=r.next())&&n!=d){if(e.isBlock(n)){f.push(n)
}}}if(d&&c!=d){f.push(d)
}return f
},isForward:function(){var b=this.dom,d=this.getSel(),a,c;
if(!d||d.anchorNode==null||d.focusNode==null){return true
}a=b.createRng();
a.setStart(d.anchorNode,d.anchorOffset);
a.collapse(true);
c=b.createRng();
c.setStart(d.focusNode,d.focusOffset);
c.collapse(true);
return a.compareBoundaryPoints(a.START_TO_START,c)<=0
},normalize:function(){var f=this,n,a,b,d,e;
function c(A){var B,m,C,I=f.dom,G=I.getRoot(),z,H,F;
function D(p,o){var q=new h(p,I.getParent(p.parentNode,I.isBlock)||G);
while(p=q[o?"prev":"next"]()){if(p.nodeName==="BR"){return true
}}}function E(r,q){var p,o;
q=q||B;
p=new h(q,I.getParent(q.parentNode,I.isBlock)||G);
while(z=p[r?"prev":"next"]()){if(z.nodeType===3&&z.nodeValue.length>0){B=z;
m=r?z.nodeValue.length:0;
a=true;
return
}if(I.isBlock(z)||H[z.nodeName.toLowerCase()]){return
}o=z
}if(b&&o){B=o;
a=true;
m=0
}}B=n[(A?"start":"end")+"Container"];
m=n[(A?"start":"end")+"Offset"];
H=I.schema.getNonEmptyElements();
if(B.nodeType===9){B=I.getRoot();
m=0
}if(B===G){if(A){z=B.childNodes[m>0?m-1:0];
if(z){F=z.nodeName.toLowerCase();
if(H[z.nodeName]||z.nodeName=="TABLE"){return
}}}if(B.hasChildNodes()){B=B.childNodes[Math.min(!A&&m>0?m-1:m,B.childNodes.length-1)];
m=0;
if(B.hasChildNodes()&&!/TABLE/.test(B.nodeName)){z=B;
C=new h(B,G);
do{if(z.nodeType===3&&z.nodeValue.length>0){m=A?0:z.nodeValue.length;
B=z;
a=true;
break
}if(H[z.nodeName.toLowerCase()]){m=I.nodeIndex(z);
B=z.parentNode;
if(z.nodeName=="IMG"&&!A){m++
}a=true;
break
}}while(z=(A?C.next():C.prev()))
}}}if(b){if(B.nodeType===3&&m===0){E(true)
}if(B.nodeType===1){z=B.childNodes[m];
if(z&&z.nodeName==="BR"&&!D(z)&&!D(z,true)){E(true,B.childNodes[m])
}}}if(A&&!b&&B.nodeType===3&&m===B.nodeValue.length){E(false)
}if(a){n["set"+(A?"Start":"End")](B,m)
}}if(k.isIE){return
}n=f.getRng();
b=n.collapsed;
c(true);
if(!b){c()
}if(a){if(b){n.collapse(true)
}f.setRng(n,f.isForward())
}},selectorChanged:function(d,a){var c=this,b;
if(!c.selectorChangedData){c.selectorChangedData={};
b={};
c.editor.onNodeChange.addToTop(function(f,q,s){var r=c.dom,e=r.getParents(s,null,r.getRoot()),t={};
j(c.selectorChangedData,function(m,n){j(e,function(o){if(r.is(o,n)){if(!b[n]){j(m,function(p){p(true,{node:o,selector:n,parents:e})
});
b[n]=m
}t[n]=m;
return false
}})
});
j(b,function(m,n){if(!t[n]){delete b[n];
j(m,function(o){o(false,{node:s,selector:n,parents:e})
})
}})
})
}if(!c.selectorChangedData[d]){c.selectorChangedData[d]=[]
}c.selectorChangedData[d].push(a);
return c
},scrollIntoView:function(a){var b,d,e=this,c=e.dom;
d=c.getViewPort(e.editor.getWin());
b=c.getPos(a).y;
if(b<d.y||b+25>d.y+d.h){e.editor.getWin().scrollTo(0,b<d.y?b:b-d.h+25)
}},destroy:function(a){var b=this;
b.win=null;
if(!a){k.removeUnload(b.destroy)
}},_fixIESelection:function(){var q=this.dom,b=q.doc,p=b.body,e,a,r;
function f(t,m){var o=p.createTextRange();
try{o.moveToPoint(t,m)
}catch(n){o=null
}return o
}function c(m){var n;
if(m.button){n=f(m.x,m.y);
if(n){if(n.compareEndPoints("StartToStart",a)>0){n.setEndPoint("StartToStart",a)
}else{n.setEndPoint("EndToEnd",a)
}n.select()
}}else{d()
}}function d(){var m=b.selection.createRange();
if(a&&!m.item&&m.compareEndPoints("StartToEnd",m)===0){a.select()
}q.unbind(b,"mouseup",d);
q.unbind(b,"mousemove",c);
a=e=0
}b.documentElement.unselectable=true;
q.bind(b,["mousedown","contextmenu"],function(m){if(m.target.nodeName==="HTML"){if(e){d()
}r=b.documentElement;
if(r.scrollHeight>r.clientHeight){return
}e=1;
a=f(m.x,m.y);
if(a){q.bind(b,"mouseup",d);
q.bind(b,"mousemove",c);
q.win.focus();
a.select()
}}})
}})
})(tinymce);
(function(b){b.dom.Serializer=function(n,j,m){var k,a,o=b.isIE,l=b.each,p;
if(!n.apply_source_formatting){n.indent=false
}j=j||b.DOM;
m=m||new b.html.Schema(n);
n.entity_encoding=n.entity_encoding||"named";
n.remove_trailing_brs="remove_trailing_brs" in n?n.remove_trailing_brs:true;
k=new b.util.Dispatcher(self);
a=new b.util.Dispatcher(self);
p=new b.html.DomParser(n,m);
p.addAttributeFilter("src,href,style",function(s,t){var f=s.length,i,d,g="data-mce-"+t,e=n.url_converter,c=n.url_converter_scope,h;
while(f--){i=s[f];
d=i.attributes.map[g];
if(d!==h){i.attr(t,d.length>0?d:null);
i.attr(g,null)
}else{d=i.attributes.map[t];
if(t==="style"){d=j.serializeStyle(j.parseStyle(d),i.name)
}else{if(e){d=e.call(c,d,t,i.name)
}}i.attr(t,d.length>0?d:null)
}}});
p.addAttributeFilter("class",function(f,e){var d=f.length,c,g;
while(d--){c=f[d];
g=c.attr("class").replace(/(?:^|\s)mce(Item\w+|Selected)(?!\S)/g,"");
c.attr("class",g.length>0?g:null)
}});
p.addAttributeFilter("data-mce-type",function(f,d,e){var c=f.length,g;
while(c--){g=f[c];
if(g.attributes.map["data-mce-type"]==="bookmark"&&!e.cleanup){g.remove()
}}});
p.addAttributeFilter("data-mce-expando",function(f,d,e){var c=f.length;
while(c--){f[c].attr(d,null)
}});
p.addNodeFilter("noscript",function(e){var d=e.length,c;
while(d--){c=e[d].firstChild;
if(c){c.value=b.html.Entities.decode(c.value)
}}});
p.addNodeFilter("script,style",function(e,d){var c=e.length,h,g;
function f(i){return i.replace(/(<!--\[CDATA\[|\]\]-->)/g,"\n").replace(/^[\r\n]*|[\r\n]*$/g,"").replace(/^\s*((<!--)?(\s*\/\/)?\s*<!\[CDATA\[|(<!--\s*)?\/\*\s*<!\[CDATA\[\s*\*\/|(\/\/)?\s*<!--|\/\*\s*<!--\s*\*\/)\s*[\r\n]*/gi,"").replace(/\s*(\/\*\s*\]\]>\s*\*\/(-->)?|\s*\/\/\s*\]\]>(-->)?|\/\/\s*(-->)?|\]\]>|\/\*\s*-->\s*\*\/|\s*-->\s*)\s*$/g,"")
}while(c--){h=e[c];
g=h.firstChild?h.firstChild.value:"";
if(d==="script"){h.attr("type",(h.attr("type")||"text/javascript").replace(/^mce\-/,""));
if(g.length>0){h.firstChild.value="// <![CDATA[\n"+f(g)+"\n// ]]>"
}}else{if(g.length>0){h.firstChild.value="<!--\n"+f(g)+"\n-->"
}}}});
p.addNodeFilter("#comment",function(f,e){var d=f.length,c;
while(d--){c=f[d];
if(c.value.indexOf("[CDATA[")===0){c.name="#cdata";
c.type=4;
c.value=c.value.replace(/^\[CDATA\[|\]\]$/g,"")
}else{if(c.value.indexOf("mce:protected ")===0){c.name="#text";
c.type=3;
c.raw=true;
c.value=unescape(c.value).substr(14)
}}}});
p.addNodeFilter("xml:namespace,input",function(f,e){var d=f.length,c;
while(d--){c=f[d];
if(c.type===7){c.remove()
}else{if(c.type===1){if(e==="input"&&!("type" in c.attributes.map)){c.attr("type","text")
}}}}});
if(n.fix_list_elements){p.addNodeFilter("ul,ol",function(e,d){var c=e.length,g,f;
while(c--){g=e[c];
f=g.parent;
if(f.name==="ul"||f.name==="ol"){if(g.prev&&g.prev.name==="li"){g.prev.append(g)
}}}})
}p.addAttributeFilter("data-mce-src,data-mce-href,data-mce-style",function(e,d){var c=e.length;
while(c--){e[c].attr(d,null)
}});
return{schema:m,addNodeFilter:p.addNodeFilter,addAttributeFilter:p.addAttributeFilter,onPreProcess:k,onPostProcess:a,serialize:function(h,c){var d,g,e,f,i;
if(o&&j.select("script,style,select,map").length>0){i=h.innerHTML;
h=h.cloneNode(false);
j.setHTML(h,i)
}else{h=h.cloneNode(true)
}d=h.ownerDocument.implementation;
if(d.createHTMLDocument){g=d.createHTMLDocument("");
l(h.nodeName=="BODY"?h.childNodes:[h],function(r){g.body.appendChild(g.importNode(r,true))
});
if(h.nodeName!="BODY"){h=g.body.firstChild
}else{h=g.body
}e=j.doc;
j.doc=g
}c=c||{};
c.format=c.format||"html";
if(!c.no_events){c.node=h;
k.dispatch(self,c)
}f=new b.html.Serializer(n,m);
c.content=f.serialize(p.parse(b.trim(c.getInner?h.innerHTML:j.getOuterHTML(h)),c));
if(!c.cleanup){c.content=c.content.replace(/\uFEFF/g,"")
}if(!c.no_events){a.dispatch(self,c)
}if(e){j.doc=e
}c.node=null;
return c.content
},addRules:function(c){m.addValidElements(c)
},setRules:function(c){m.setValidElements(c)
}}
}
})(tinymce);
(function(b){b.dom.ScriptLoader=function(p){var u=0,m=1,o=2,a={},n=[],s={},t=[],q=0,r;
function v(h,j){var i=this,d=b.DOM,l,f,c,g;
function e(){d.remove(g);
if(l){l.onreadystatechange=l.onload=l=null
}j()
}function k(){if(typeof(console)!=="undefined"&&console.log){console.log("Failed to load: "+h)
}}g=d.uniqueId();
if(b.isIE6){f=new b.util.URI(h);
c=location;
if(f.host==c.hostname&&f.port==c.port&&(f.protocol+":")==c.protocol&&f.protocol.toLowerCase()!="file"){b.util.XHR.send({url:b._addVer(f.getURI()),success:function(z){var x=d.create("script",{type:"text/javascript"});
x.text=z;
document.getElementsByTagName("head")[0].appendChild(x);
d.remove(x);
e()
},error:k});
return
}}l=document.createElement("script");
l.id=g;
l.type="text/javascript";
l.src=b._addVer(h);
if(!b.isIE||b.isIE11){l.onload=e
}l.onerror=k;
if(!b.isOpera){l.onreadystatechange=function(){var x=l.readyState;
if(x=="complete"||x=="loaded"){e()
}}
}(document.getElementsByTagName("head")[0]||document.body).appendChild(l)
}this.isDone=function(c){return a[c]==o
};
this.markDone=function(c){a[c]=o
};
this.add=this.load=function(c,d,g){var f,e=a[c];
if(e==r){n.push(c);
a[c]=u
}if(d){if(!s[c]){s[c]=[]
}s[c].push({func:d,scope:g||this})
}};
this.loadQueue=function(d,c){this.loadScripts(n,d,c)
};
this.loadScripts=function(c,d,e){var f;
function g(h){b.each(s[h],function(i){i.func.call(i.scope)
});
s[h]=r
}t.push({func:d,scope:e||this});
f=function(){var h=b.grep(c);
c.length=0;
b.each(h,function(i){if(a[i]==o){g(i);
return
}if(a[i]!=m){a[i]=m;
q++;
v(i,function(){a[i]=o;
q--;
g(i);
f()
})
}});
if(!q){b.each(t,function(i){i.func.call(i.scope)
});
t.length=0
}};
f()
}
};
b.ScriptLoader=new b.dom.ScriptLoader()
})(tinymce);
(function(b){b.dom.RangeUtils=function(d){var a="\uFEFF";
this.walk=function(I,K){var D=I.startContainer,A=I.startOffset,J=I.endContainer,z=I.endOffset,C,F,x,E,c,u,H;
H=d.select("td.mceSelected,th.mceSelected");
if(H.length>0){b.each(H,function(e){K([e])
});
return
}function G(f){var e;
e=f[0];
if(e.nodeType===3&&e===D&&A>=e.nodeValue.length){f.splice(0,1)
}e=f[f.length-1];
if(z===0&&f.length>0&&e===J&&e.nodeType===3){f.splice(f.length-1,1)
}return f
}function v(e,f,g){var h=[];
for(;
e&&e!=g;
e=e[f]){h.push(e)
}return h
}function y(e,f){do{if(e.parentNode==f){return e
}e=e.parentNode
}while(e)
}function B(e,f,h){var g=h?"nextSibling":"previousSibling";
for(E=e,c=E.parentNode;
E&&E!=f;
E=c){c=E.parentNode;
u=v(E==e?E:E[g],g);
if(u.length){if(!h){u.reverse()
}K(G(u))
}}}if(D.nodeType==1&&D.hasChildNodes()){D=D.childNodes[A]
}if(J.nodeType==1&&J.hasChildNodes()){J=J.childNodes[Math.min(z-1,J.childNodes.length-1)]
}if(D==J){return K(G([D]))
}C=d.findCommonAncestor(D,J);
for(E=D;
E;
E=E.parentNode){if(E===J){return B(D,C,true)
}if(E===C){break
}}for(E=J;
E;
E=E.parentNode){if(E===D){return B(J,C)
}if(E===C){break
}}F=y(D,C)||D;
x=y(J,C)||J;
B(D,F,true);
u=v(F==D?F:F.nextSibling,"nextSibling",x==J?x.nextSibling:x);
if(u.length){K(G(u))
}B(J,x)
};
this.split=function(m){var j=m.startContainer,n=m.startOffset,c=m.endContainer,k=m.endOffset;
function l(f,e){return f.splitText(e)
}if(j==c&&j.nodeType==3){if(n>0&&n<j.nodeValue.length){c=l(j,n);
j=c.previousSibling;
if(k>n){k=k-n;
j=c=l(c,k).previousSibling;
k=c.nodeValue.length;
n=0
}else{k=0
}}}else{if(j.nodeType==3&&n>0&&n<j.nodeValue.length){j=l(j,n);
n=0
}if(c.nodeType==3&&k>0&&k<c.nodeValue.length){c=l(c,k).previousSibling;
k=c.nodeValue.length
}}return{startContainer:j,startOffset:n,endContainer:c,endOffset:k}
}
};
b.dom.RangeUtils.compareRanges=function(d,a){if(d&&a){if(d.item||d.duplicate){if(d.item&&a.item&&d.item(0)===a.item(0)){return true
}if(d.isEqual&&a.isEqual&&a.isEqual(d)){return true
}}else{return d.startContainer==a.startContainer&&d.startOffset==a.startOffset
}}return false
}
})(tinymce);
(function(d){var e=d.dom.Event,f=d.each;
d.create("tinymce.ui.KeyboardNavigation",{KeyboardNavigation:function(B,A){var a=this,r=B.root,s=B.items,c=B.enableUpDown,x=B.enableLeftRight||!B.enableUpDown,t=B.excludeFromTabOrder,u,y,b,C,z;
A=A||d.DOM;
u=function(g){z=g.target.id
};
y=function(g){A.setAttrib(g.target.id,"tabindex","-1")
};
C=function(h){var g=A.get(z);
A.setAttrib(g,"tabindex","0");
g.focus()
};
a.focus=function(){A.get(z).focus()
};
a.destroy=function(){f(s,function(i){var h=A.get(i.id);
A.unbind(h,"focus",u);
A.unbind(h,"blur",y)
});
var g=A.get(r);
A.unbind(g,"focus",C);
A.unbind(g,"keydown",b);
s=A=r=a.focus=u=y=b=C=null;
a.destroy=function(){}
};
a.moveFocus=function(g,j){var k=-1,h=a.controls,i;
if(!z){return
}f(s,function(m,l){if(m.id===z){k=l;
return false
}});
k+=g;
if(k<0){k=s.length-1
}else{if(k>=s.length){k=0
}}i=s[k];
A.setAttrib(z,"tabindex","-1");
A.setAttrib(i.id,"tabindex","0");
A.get(i.id).focus();
if(B.actOnFocus){B.onAction(i.id)
}if(j){e.cancel(j)
}};
b=function(i){var l=37,m=39,j=38,h=40,g=27,n=14,o=13,k=32;
switch(i.keyCode){case l:if(x){a.moveFocus(-1)
}e.cancel(i);
break;
case m:if(x){a.moveFocus(1)
}e.cancel(i);
break;
case j:if(c){a.moveFocus(-1)
}e.cancel(i);
break;
case h:if(c){a.moveFocus(1)
}e.cancel(i);
break;
case g:if(B.onCancel){B.onCancel();
e.cancel(i)
}break;
case n:case o:case k:if(B.onAction){B.onAction(z);
e.cancel(i)
}break
}};
f(s,function(h,j){var i,g;
if(!h.id){h.id=A.uniqueId("_mce_item_")
}g=A.get(h.id);
if(t){A.bind(g,"blur",y);
i="-1"
}else{i=(j===0?"0":"-1")
}g.setAttribute("tabindex",i);
A.bind(g,"focus",u)
});
if(s[0]){z=s[0].id
}A.setAttrib(r,"tabindex","-1");
var v=A.get(r);
A.bind(v,"focus",C);
A.bind(v,"keydown",b)
}})
})(tinymce);
(function(f){var d=f.DOM,e=f.is;
f.create("tinymce.ui.Control",{Control:function(a,b,c){this.id=a;
this.settings=b=b||{};
this.rendered=false;
this.onRender=new f.util.Dispatcher(this);
this.classPrefix="";
this.scope=b.scope||this;
this.disabled=0;
this.active=0;
this.editor=c
},setAriaProperty:function(a,b){var c=d.get(this.id+"_aria")||d.get(this.id);
if(c){d.setAttrib(c,"aria-"+a,!!b)
}},focus:function(){d.get(this.id).focus()
},setDisabled:function(a){if(a!=this.disabled){this.setAriaProperty("disabled",a);
this.setState("Disabled",a);
this.setState("Enabled",!a);
this.disabled=a
}},isDisabled:function(){return this.disabled
},setActive:function(a){if(a!=this.active){this.setState("Active",a);
this.active=a;
this.setAriaProperty("pressed",a)
}},isActive:function(){return this.active
},setState:function(a,c){var b=d.get(this.id);
a=this.classPrefix+a;
if(c){d.addClass(b,a)
}else{d.removeClass(b,a)
}},isRendered:function(){return this.rendered
},renderHTML:function(){},renderTo:function(a){d.setHTML(a,this.renderHTML())
},postRender:function(){var a=this,b;
if(e(a.disabled)){b=a.disabled;
a.disabled=-1;
a.setDisabled(b)
}if(e(a.active)){b=a.active;
a.active=-1;
a.setActive(b)
}},remove:function(){d.remove(this.id);
this.destroy()
},destroy:function(){f.dom.Event.clear(this.id)
}})
})(tinymce);
tinymce.create("tinymce.ui.Container:tinymce.ui.Control",{Container:function(f,d,e){this.parent(f,d,e);
this.controls=[];
this.lookup={}
},add:function(b){this.lookup[b.id]=b;
this.controls.push(b);
return b
},get:function(b){return this.lookup[b]
}});
tinymce.create("tinymce.ui.Separator:tinymce.ui.Control",{Separator:function(c,d){this.parent(c,d);
this.classPrefix="mceSeparator";
this.setDisabled(true)
},renderHTML:function(){return tinymce.DOM.createHTML("span",{"class":this.classPrefix,role:"separator","aria-orientation":"vertical",tabindex:"-1"})
}});
(function(i){var j=i.is,f=i.DOM,h=i.each,g=i.walk;
i.create("tinymce.ui.MenuItem:tinymce.ui.Control",{MenuItem:function(a,b){this.parent(a,b);
this.classPrefix="mceMenuItem"
},setSelected:function(a){this.setState("Selected",a);
this.setAriaProperty("checked",!!a);
this.selected=a
},isSelected:function(){return this.selected
},postRender:function(){var a=this;
a.parent();
if(j(a.selected)){a.setSelected(a.selected)
}}})
})(tinymce);
(function(i){var j=i.is,f=i.DOM,h=i.each,g=i.walk;
i.create("tinymce.ui.Menu:tinymce.ui.MenuItem",{Menu:function(a,b){var c=this;
c.parent(a,b);
c.items={};
c.collapsed=false;
c.menuCount=0;
c.onAddItem=new i.util.Dispatcher(this)
},expand:function(a){var b=this;
if(a){g(b,function(c){if(c.expand){c.expand()
}},"items",b)
}b.collapsed=false
},collapse:function(a){var b=this;
if(a){g(b,function(c){if(c.collapse){c.collapse()
}},"items",b)
}b.collapsed=true
},isCollapsed:function(){return this.collapsed
},add:function(a){if(!a.settings){a=new i.ui.MenuItem(a.id||f.uniqueId(),a)
}this.onAddItem.dispatch(this,a);
return this.items[a.id]=a
},addSeparator:function(){return this.add({separator:true})
},addMenu:function(a){if(!a.collapse){a=this.createMenu(a)
}this.menuCount++;
return this.add(a)
},hasMenus:function(){return this.menuCount!==0
},remove:function(a){delete this.items[a.id]
},removeAll:function(){var a=this;
g(a,function(b){if(b.removeAll){b.removeAll()
}else{b.remove()
}b.destroy()
},"items",a);
a.items={}
},createMenu:function(a){var b=new i.ui.Menu(a.id||f.uniqueId(),a);
b.onAddItem.add(this.onAddItem.dispatch,this.onAddItem);
return b
}})
})(tinymce);
(function(j){var k=j.is,l=j.DOM,i=j.each,h=j.dom.Event,g=j.dom.Element;
j.create("tinymce.ui.DropMenu:tinymce.ui.Menu",{DropMenu:function(a,b){b=b||{};
b.container=b.container||l.doc.body;
b.offset_x=b.offset_x||0;
b.offset_y=b.offset_y||0;
b.vp_offset_x=b.vp_offset_x||0;
b.vp_offset_y=b.vp_offset_y||0;
if(k(b.icons)&&!b.icons){b["class"]+=" mceNoIcons"
}this.parent(a,b);
this.onShowMenu=new j.util.Dispatcher(this);
this.onHideMenu=new j.util.Dispatcher(this);
this.classPrefix="mceMenu"
},createMenu:function(a){var c=this,b=c.settings,d;
a.container=a.container||b.container;
a.parent=c;
a.constrain=a.constrain||b.constrain;
a["class"]=a["class"]||b["class"];
a.vp_offset_x=a.vp_offset_x||b.vp_offset_x;
a.vp_offset_y=a.vp_offset_y||b.vp_offset_y;
a.keyboard_focus=b.keyboard_focus;
d=new j.ui.DropMenu(a.id||l.uniqueId(),a);
d.onAddItem.add(c.onAddItem.dispatch,c.onAddItem);
return d
},focus:function(){var a=this;
if(a.keyboardNav){a.keyboardNav.focus()
}},update:function(){var d=this,c=d.settings,f=l.get("menu_"+d.id+"_tbl"),a=l.get("menu_"+d.id+"_co"),e,b;
e=c.max_width?Math.min(f.offsetWidth,c.max_width):f.offsetWidth;
b=c.max_height?Math.min(f.offsetHeight,c.max_height):f.offsetHeight;
if(!l.boxModel){d.element.setStyles({width:e+2,height:b+2})
}else{d.element.setStyles({width:e,height:b})
}if(c.max_width){l.setStyle(a,"width",e)
}if(c.max_height){l.setStyle(a,"height",b);
if(f.clientHeight<c.max_height){l.setStyle(a,"overflow","hidden")
}}},showMenu:function(c,f,a){var D=this,e=D.settings,d,C=l.getViewPort(),F,t,E,b,B=2,x,y,s=D.classPrefix;
D.collapse(1);
if(D.isMenuVisible){return
}if(!D.rendered){d=l.add(D.settings.container,D.renderNode());
i(D.items,function(m){m.postRender()
});
D.element=new g("menu_"+D.id,{blocker:1,container:e.container})
}else{d=l.get("menu_"+D.id)
}if(!j.isOpera){l.setStyles(d,{left:-65535,top:-65535})
}l.show(d);
D.update();
c+=e.offset_x||0;
f+=e.offset_y||0;
C.w-=4;
C.h-=4;
if(e.constrain){F=d.clientWidth-B;
t=d.clientHeight-B;
E=C.x+C.w;
b=C.y+C.h;
if((c+e.vp_offset_x+F)>E){c=a?a-F:Math.max(0,(E-e.vp_offset_x)-F)
}if((f+e.vp_offset_y+t)>b){f=Math.max(0,(b-e.vp_offset_y)-t)
}}l.setStyles(d,{left:c,top:f});
D.element.update();
D.isMenuVisible=1;
D.mouseClickFunc=h.add(d,"click",function(n){var m;
n=n.target;
if(n&&(n=l.getParent(n,"tr"))&&!l.hasClass(n,s+"ItemSub")){m=D.items[n.id];
if(m.isDisabled()){return
}x=D;
while(x){if(x.hideMenu){x.hideMenu()
}x=x.settings.parent
}if(m.settings.onclick){m.settings.onclick(n)
}return false
}});
if(D.hasMenus()){D.mouseOverFunc=h.add(d,"mouseover",function(m){var p,n,o;
m=m.target;
if(m&&(m=l.getParent(m,"tr"))){p=D.items[m.id];
if(D.lastMenu){D.lastMenu.collapse(1)
}if(p.isDisabled()){return
}if(m&&l.hasClass(m,s+"ItemSub")){n=l.getRect(m);
p.showMenu((n.x+n.w-B),n.y-B,n.x);
D.lastMenu=p;
l.addClass(l.get(p.id).firstChild,s+"ItemActive")
}}})
}h.add(d,"keydown",D._keyHandler,D);
D.onShowMenu.dispatch(D);
if(e.keyboard_focus){D._setupKeyboardNav()
}},hideMenu:function(a){var d=this,b=l.get("menu_"+d.id),c;
if(!d.isMenuVisible){return
}if(d.keyboardNav){d.keyboardNav.destroy()
}h.remove(b,"mouseover",d.mouseOverFunc);
h.remove(b,"click",d.mouseClickFunc);
h.remove(b,"keydown",d._keyHandler);
l.hide(b);
d.isMenuVisible=0;
if(!a){d.collapse(1)
}if(d.element){d.element.hide()
}if(c=l.get(d.id)){l.removeClass(c.firstChild,d.classPrefix+"ItemActive")
}d.onHideMenu.dispatch(d)
},add:function(a){var c=this,b;
a=c.parent(a);
if(c.isRendered&&(b=l.get("menu_"+c.id))){c._add(l.select("tbody",b)[0],a)
}return a
},collapse:function(a){this.parent(a);
this.hideMenu(1)
},remove:function(a){l.remove(a.id);
this.destroy();
return this.parent(a)
},destroy:function(){var b=this,a=l.get("menu_"+b.id);
if(b.keyboardNav){b.keyboardNav.destroy()
}h.remove(a,"mouseover",b.mouseOverFunc);
h.remove(l.select("a",a),"focus",b.mouseOverFunc);
h.remove(a,"click",b.mouseClickFunc);
h.remove(a,"keydown",b._keyHandler);
if(b.element){b.element.remove()
}l.remove(a)
},renderNode:function(){var d=this,c=d.settings,a,e,b,f;
f=l.create("div",{role:"listbox",id:"menu_"+d.id,"class":c["class"],style:"position:absolute;left:0;top:0;z-index:200000;outline:0"});
if(d.settings.parent){l.setAttrib(f,"aria-parent","menu_"+d.settings.parent.id)
}b=l.add(f,"div",{role:"presentation",id:"menu_"+d.id+"_co","class":d.classPrefix+(c["class"]?" "+c["class"]:"")});
d.element=new g("menu_"+d.id,{blocker:1,container:c.container});
if(c.menu_line){l.add(b,"span",{"class":d.classPrefix+"Line"})
}a=l.add(b,"table",{role:"presentation",id:"menu_"+d.id+"_tbl",border:0,cellPadding:0,cellSpacing:0});
e=l.add(a,"tbody");
i(d.items,function(n){d._add(e,n)
});
d.rendered=true;
return f
},_setupKeyboardNav:function(){var a,b,c=this;
a=l.get("menu_"+c.id);
b=l.select("a[role=option]","menu_"+c.id);
b.splice(0,0,a);
c.keyboardNav=new j.ui.KeyboardNavigation({root:"menu_"+c.id,items:b,onCancel:function(){c.hideMenu()
},enableUpDown:true});
a.focus()
},_keyHandler:function(c){var b=this,a;
switch(c.keyCode){case 37:if(b.settings.parent){b.hideMenu();
b.settings.parent.focus();
h.cancel(c)
}break;
case 39:if(b.mouseOverFunc){b.mouseOverFunc(c)
}break
}},_add:function(f,o){var n,a=o.settings,b,d,e,c=this.classPrefix,r;
if(a.separator){d=l.add(f,"tr",{id:o.id,"class":c+"ItemSeparator"});
l.add(d,"td",{"class":c+"ItemSeparator"});
if(n=d.previousSibling){l.addClass(n,"mceLast")
}return
}n=d=l.add(f,"tr",{id:o.id,"class":c+"Item "+c+"ItemEnabled"});
n=e=l.add(n,a.titleItem?"th":"td");
n=b=l.add(n,"a",{id:o.id+"_aria",role:a.titleItem?"presentation":"option",href:"javascript:;",onclick:"return false;",onmousedown:"return false;"});
if(a.parent){l.setAttrib(b,"aria-haspopup","true");
l.setAttrib(b,"aria-owns","menu_"+o.id)
}l.addClass(e,a["class"]);
r=l.add(n,"span",{"class":"mceIcon"+(a.icon?" mce_"+a.icon:"")});
if(a.icon_src){l.add(r,"img",{src:a.icon_src})
}n=l.add(n,a.element||"span",{"class":"mceText",title:o.settings.title},o.settings.title);
if(o.settings.style){if(typeof o.settings.style=="function"){o.settings.style=o.settings.style()
}l.setAttrib(n,"style",o.settings.style)
}if(f.childNodes.length==1){l.addClass(d,"mceFirst")
}if((n=d.previousSibling)&&l.hasClass(n,c+"ItemSeparator")){l.addClass(d,"mceFirst")
}if(o.collapse){l.addClass(d,c+"ItemSub")
}if(n=d.previousSibling){l.removeClass(n,"mceLast")
}l.addClass(d,"mceLast")
}})
})(tinymce);
(function(c){var d=c.DOM;
c.create("tinymce.ui.Button:tinymce.ui.Control",{Button:function(a,b,f){this.parent(a,b,f);
this.classPrefix="mceButton"
},renderHTML:function(){var a=this.classPrefix,b=this.settings,g,h;
h=d.encode(b.label||"");
g='<a role="button" id="'+this.id+'" href="javascript:;" class="'+a+" "+a+"Enabled "+b["class"]+(h?" "+a+"Labeled":"")+'" onmousedown="return false;" onclick="return false;" aria-labelledby="'+this.id+'_voice" title="'+d.encode(b.title)+'">';
if(b.image&&!(this.editor&&this.editor.forcedHighContrastMode)){g+='<span class="mceIcon '+b["class"]+'"><img class="mceIcon" src="'+b.image+'" alt="'+d.encode(b.title)+'" /></span>'+(h?'<span class="'+a+'Label">'+h+"</span>":"")
}else{g+='<span class="mceIcon '+b["class"]+'"></span>'+(h?'<span class="'+a+'Label">'+h+"</span>":"")
}g+='<span class="mceVoiceLabel mceIconOnly" style="display: none;" id="'+this.id+'_voice">'+b.title+"</span>";
g+="</a>";
return g
},postRender:function(){var b=this,a=b.settings,f;
if(c.isIE&&b.editor){c.dom.Event.add(b.id,"mousedown",function(h){var e=b.editor.selection.getNode().nodeName;
f=e==="IMG"?b.editor.selection.getBookmark():null
})
}c.dom.Event.add(b.id,"click",function(e){if(!b.isDisabled()){if(c.isIE&&b.editor&&f!==null){b.editor.selection.moveToBookmark(f)
}return a.onclick.call(a.scope,e)
}});
c.dom.Event.add(b.id,"keydown",function(e){if(!b.isDisabled()&&e.keyCode==c.VK.SPACEBAR){c.dom.Event.cancel(e);
return a.onclick.call(a.scope,e)
}})
}})
})(tinymce);
(function(j){var k=j.DOM,g=j.dom.Event,i=j.each,h=j.util.Dispatcher,l;
j.create("tinymce.ui.ListBox:tinymce.ui.Control",{ListBox:function(a,b,d){var c=this;
c.parent(a,b,d);
c.items=[];
c.onChange=new h(c);
c.onPostRender=new h(c);
c.onAdd=new h(c);
c.onRenderMenu=new j.util.Dispatcher(this);
c.classPrefix="mceListBox";
c.marked={}
},select:function(c){var d=this,a,b;
d.marked={};
if(c==l){return d.selectByIndex(-1)
}if(c&&typeof(c)=="function"){b=c
}else{b=function(e){return e==c
}
}if(c!=d.selectedValue){i(d.items,function(e,f){if(b(e.value)){a=1;
d.selectByIndex(f);
return false
}});
if(!a){d.selectByIndex(-1)
}}},selectByIndex:function(e){var c=this,b,a,d;
c.marked={};
if(e!=c.selectedIndex){b=k.get(c.id+"_text");
d=k.get(c.id+"_voiceDesc");
a=c.items[e];
if(a){c.selectedValue=a.value;
c.selectedIndex=e;
k.setHTML(b,k.encode(a.title));
k.setHTML(d,c.settings.title+" - "+a.title);
k.removeClass(b,"mceTitle");
k.setAttrib(c.id,"aria-valuenow",a.title)
}else{k.setHTML(b,k.encode(c.settings.title));
k.setHTML(d,k.encode(c.settings.title));
k.addClass(b,"mceTitle");
c.selectedValue=c.selectedIndex=null;
k.setAttrib(c.id,"aria-valuenow",c.settings.title)
}b=0
}},mark:function(a){this.marked[a]=true
},add:function(a,d,b){var c=this;
b=b||{};
b=j.extend(b,{title:a,value:d});
c.items.push(b);
c.onAdd.dispatch(c,b)
},getLength:function(){return this.items.length
},renderHTML:function(){var b="",d=this,c=d.settings,a=d.classPrefix;
b='<span role="listbox" aria-haspopup="true" aria-labelledby="'+d.id+'_voiceDesc" aria-describedby="'+d.id+'_voiceDesc"><table role="presentation" tabindex="0" id="'+d.id+'" cellpadding="0" cellspacing="0" class="'+a+" "+a+"Enabled"+(c["class"]?(" "+c["class"]):"")+'"><tbody><tr>';
b+="<td>"+k.createHTML("span",{id:d.id+"_voiceDesc","class":"voiceLabel",style:"display:none;"},d.settings.title);
b+=k.createHTML("a",{id:d.id+"_text",tabindex:-1,href:"javascript:;","class":"mceText",onclick:"return false;",onmousedown:"return false;"},k.encode(d.settings.title))+"</td>";
b+="<td>"+k.createHTML("a",{id:d.id+"_open",tabindex:-1,href:"javascript:;","class":"mceOpen",onclick:"return false;",onmousedown:"return false;"},'<span><span style="display:none;" class="mceIconOnly" aria-hidden="true">\u25BC</span></span>')+"</td>";
b+="</tr></tbody></table></span>";
return b
},showMenu:function(){var c=this,a,b=k.get(this.id),d;
if(c.isDisabled()||c.items.length===0){return
}if(c.menu&&c.menu.isMenuVisible){return c.hideMenu()
}if(!c.isMenuRendered){c.renderMenu();
c.isMenuRendered=true
}a=k.getPos(b);
d=c.menu;
d.settings.offset_x=a.x;
d.settings.offset_y=a.y;
d.settings.keyboard_focus=!j.isOpera;
i(c.items,function(e){if(d.items[e.id]){d.items[e.id].setSelected(0)
}});
i(c.items,function(e){if(d.items[e.id]&&c.marked[e.value]){d.items[e.id].setSelected(1)
}if(e.value===c.selectedValue){d.items[e.id].setSelected(1)
}});
d.showMenu(0,b.clientHeight);
g.add(k.doc,"mousedown",c.hideMenu,c);
k.addClass(c.id,c.classPrefix+"Selected")
},hideMenu:function(a){var b=this;
if(b.menu&&b.menu.isMenuVisible){k.removeClass(b.id,b.classPrefix+"Selected");
if(a&&a.type=="mousedown"&&(a.target.id==b.id+"_text"||a.target.id==b.id+"_open")){return
}if(!a||!k.getParent(a.target,".mceMenu")){k.removeClass(b.id,b.classPrefix+"Selected");
g.remove(k.doc,"mousedown",b.hideMenu,b);
b.menu.hideMenu()
}}},renderMenu:function(){var a=this,b;
b=a.settings.control_manager.createDropMenu(a.id+"_menu",{menu_line:1,"class":a.classPrefix+"Menu mceNoIcons",max_width:250,max_height:150});
b.onHideMenu.add(function(){a.hideMenu();
a.focus()
});
b.add({title:a.settings.title,"class":"mceMenuItemTitle",onclick:function(){if(a.settings.onselect("")!==false){a.select("")
}}});
i(a.items,function(c){if(c.value===l){b.add({title:c.title,role:"option","class":"mceMenuItemTitle",onclick:function(){if(a.settings.onselect("")!==false){a.select("")
}}})
}else{c.id=k.uniqueId();
c.role="option";
c.onclick=function(){if(a.settings.onselect(c.value)!==false){a.select(c.value)
}};
b.add(c)
}});
a.onRenderMenu.dispatch(a,b);
a.menu=b
},postRender:function(){var b=this,a=b.classPrefix;
g.add(b.id,"click",b.showMenu,b);
g.add(b.id,"keydown",function(c){if(c.keyCode==32){b.showMenu(c);
g.cancel(c)
}});
g.add(b.id,"focus",function(){if(!b._focused){b.keyDownHandler=g.add(b.id,"keydown",function(c){if(c.keyCode==40){b.showMenu();
g.cancel(c)
}});
b.keyPressHandler=g.add(b.id,"keypress",function(c){var d;
if(c.keyCode==13){d=b.selectedValue;
b.selectedValue=null;
g.cancel(c);
b.settings.onselect(d)
}})
}b._focused=1
});
g.add(b.id,"blur",function(){g.remove(b.id,"keydown",b.keyDownHandler);
g.remove(b.id,"keypress",b.keyPressHandler);
b._focused=0
});
if(j.isIE6||!k.boxModel){g.add(b.id,"mouseover",function(){if(!k.hasClass(b.id,a+"Disabled")){k.addClass(b.id,a+"Hover")
}});
g.add(b.id,"mouseout",function(){if(!k.hasClass(b.id,a+"Disabled")){k.removeClass(b.id,a+"Hover")
}})
}b.onPostRender.dispatch(b,k.get(b.id))
},destroy:function(){this.parent();
g.clear(this.id+"_text");
g.clear(this.id+"_open")
}})
})(tinymce);
(function(j){var k=j.DOM,g=j.dom.Event,i=j.each,h=j.util.Dispatcher,l;
j.create("tinymce.ui.NativeListBox:tinymce.ui.ListBox",{NativeListBox:function(a,b){this.parent(a,b);
this.classPrefix="mceNativeListBox"
},setDisabled:function(a){k.get(this.id).disabled=a;
this.setAriaProperty("disabled",a)
},isDisabled:function(){return k.get(this.id).disabled
},select:function(c){var d=this,a,b;
if(c==l){return d.selectByIndex(-1)
}if(c&&typeof(c)=="function"){b=c
}else{b=function(e){return e==c
}
}if(c!=d.selectedValue){i(d.items,function(e,f){if(b(e.value)){a=1;
d.selectByIndex(f);
return false
}});
if(!a){d.selectByIndex(-1)
}}},selectByIndex:function(a){k.get(this.id).selectedIndex=a+1;
this.selectedValue=this.items[a]?this.items[a].value:null
},add:function(a,d,e){var b,c=this;
e=e||{};
e.value=d;
if(c.isRendered()){k.add(k.get(this.id),"option",e,a)
}b={title:a,value:d,attribs:e};
c.items.push(b);
c.onAdd.dispatch(c,b)
},getLength:function(){return this.items.length
},renderHTML:function(){var a,b=this;
a=k.createHTML("option",{value:""},"-- "+b.settings.title+" --");
i(b.items,function(c){a+=k.createHTML("option",{value:c.value},c.title)
});
a=k.createHTML("select",{id:b.id,"class":"mceNativeListBox","aria-labelledby":b.id+"_aria"},a);
a+=k.createHTML("span",{id:b.id+"_aria",style:"display: none"},b.settings.title);
return a
},postRender:function(){var c=this,b,a=true;
c.rendered=true;
function d(e){var f=c.items[e.target.selectedIndex-1];
if(f&&(f=f.value)){c.onChange.dispatch(c,f);
if(c.settings.onselect){c.settings.onselect(f)
}}}g.add(c.id,"change",d);
g.add(c.id,"keydown",function(u){var y,v=37,e=39,f=38,t=40,s=13,x=32;
g.remove(c.id,"change",b);
a=false;
y=g.add(c.id,"blur",function(){if(a){return
}a=true;
g.add(c.id,"change",d);
g.remove(c.id,"blur",y)
});
if(u.keyCode==s||u.keyCode==x){d(u);
return g.cancel(u)
}else{if(u.keyCode==t||u.keyCode==f){u.stopImmediatePropagation()
}}});
c.onPostRender.dispatch(c,k.get(c.id))
}})
})(tinymce);
(function(h){var e=h.DOM,f=h.dom.Event,g=h.each;
h.create("tinymce.ui.MenuButton:tinymce.ui.Button",{MenuButton:function(a,b,c){this.parent(a,b,c);
this.onRenderMenu=new h.util.Dispatcher(this);
b.menu_container=b.menu_container||e.doc.body
},showMenu:function(){var d=this,a,b,c=e.get(d.id),k;
if(d.isDisabled()){return
}if(!d.isMenuRendered){d.renderMenu();
d.isMenuRendered=true
}if(d.isMenuVisible){return d.hideMenu()
}a=e.getPos(d.settings.menu_container);
b=e.getPos(c);
k=d.menu;
k.settings.offset_x=b.x;
k.settings.offset_y=b.y;
k.settings.vp_offset_x=b.x;
k.settings.vp_offset_y=b.y;
k.settings.keyboard_focus=d._focused;
k.showMenu(0,c.firstChild.clientHeight);
f.add(e.doc,"mousedown",d.hideMenu,d);
d.setState("Selected",1);
d.isMenuVisible=1
},renderMenu:function(){var a=this,b;
b=a.settings.control_manager.createDropMenu(a.id+"_menu",{menu_line:1,"class":this.classPrefix+"Menu",icons:a.settings.icons});
b.onHideMenu.add(function(){a.hideMenu();
a.focus()
});
a.onRenderMenu.dispatch(a,b);
a.menu=b
},hideMenu:function(a){var b=this;
if(a&&a.type=="mousedown"&&e.getParent(a.target,function(c){return c.id===b.id||c.id===b.id+"_open"
})){return
}if(!a||!e.getParent(a.target,".mceMenu")){b.setState("Selected",0);
f.remove(e.doc,"mousedown",b.hideMenu,b);
if(b.menu){b.menu.hideMenu()
}}b.isMenuVisible=0
},postRender:function(){var b=this,a=b.settings;
f.add(b.id,"click",function(){if(!b.isDisabled()){if(a.onclick){a.onclick(b.value)
}b.showMenu()
}})
}})
})(tinymce);
(function(h){var e=h.DOM,f=h.dom.Event,g=h.each;
h.create("tinymce.ui.SplitButton:tinymce.ui.MenuButton",{SplitButton:function(a,b,c){this.parent(a,b,c);
this.classPrefix="mceSplitButton"
},renderHTML:function(){var a,c=this,b=c.settings,d;
a="<tbody><tr>";
if(b.image){d=e.createHTML("img ",{src:b.image,role:"presentation","class":"mceAction "+b["class"]})
}else{d=e.createHTML("span",{"class":"mceAction "+b["class"]},"")
}d+=e.createHTML("span",{"class":"mceVoiceLabel mceIconOnly",id:c.id+"_voice",style:"display:none;"},b.title);
a+="<td >"+e.createHTML("a",{role:"button",id:c.id+"_action",tabindex:"-1",href:"javascript:;","class":"mceAction "+b["class"],onclick:"return false;",onmousedown:"return false;",title:b.title},d)+"</td>";
d=e.createHTML("span",{"class":"mceOpen "+b["class"]},'<span style="display:none;" class="mceIconOnly" aria-hidden="true">\u25BC</span>');
a+="<td >"+e.createHTML("a",{role:"button",id:c.id+"_open",tabindex:"-1",href:"javascript:;","class":"mceOpen "+b["class"],onclick:"return false;",onmousedown:"return false;",title:b.title},d)+"</td>";
a+="</tr></tbody>";
a=e.createHTML("table",{role:"presentation","class":"mceSplitButton mceSplitButtonEnabled "+b["class"],cellpadding:"0",cellspacing:"0",title:b.title},a);
return e.createHTML("div",{id:c.id,role:"button",tabindex:"0","aria-labelledby":c.id+"_voice","aria-haspopup":"true"},a)
},postRender:function(){var c=this,a=c.settings,b;
if(a.onclick){b=function(d){if(!c.isDisabled()){a.onclick(c.value);
f.cancel(d)
}};
f.add(c.id+"_action","click",b);
f.add(c.id,["click","keydown"],function(r){var o=32,d=14,q=13,p=38,n=40;
if((r.keyCode===32||r.keyCode===13||r.keyCode===14)&&!r.altKey&&!r.ctrlKey&&!r.metaKey){b();
f.cancel(r)
}else{if(r.type==="click"||r.keyCode===n){c.showMenu();
f.cancel(r)
}}})
}f.add(c.id+"_open","click",function(d){c.showMenu();
f.cancel(d)
});
f.add([c.id,c.id+"_open"],"focus",function(){c._focused=1
});
f.add([c.id,c.id+"_open"],"blur",function(){c._focused=0
});
if(h.isIE6||!e.boxModel){f.add(c.id,"mouseover",function(){if(!e.hasClass(c.id,"mceSplitButtonDisabled")){e.addClass(c.id,"mceSplitButtonHover")
}});
f.add(c.id,"mouseout",function(){if(!e.hasClass(c.id,"mceSplitButtonDisabled")){e.removeClass(c.id,"mceSplitButtonHover")
}})
}},destroy:function(){this.parent();
f.clear(this.id+"_action");
f.clear(this.id+"_open");
f.clear(this.id)
}})
})(tinymce);
(function(i){var j=i.DOM,g=i.dom.Event,f=i.is,h=i.each;
i.create("tinymce.ui.ColorSplitButton:tinymce.ui.SplitButton",{ColorSplitButton:function(a,b,d){var c=this;
c.parent(a,b,d);
c.settings=b=i.extend({colors:"000000,993300,333300,003300,003366,000080,333399,333333,800000,FF6600,808000,008000,008080,0000FF,666699,808080,FF0000,FF9900,99CC00,339966,33CCCC,3366FF,800080,999999,FF00FF,FFCC00,FFFF00,00FF00,00FFFF,00CCFF,993366,C0C0C0,FF99CC,FFCC99,FFFF99,CCFFCC,CCFFFF,99CCFF,CC99FF,FFFFFF",grid_width:8,default_color:"#888888"},c.settings);
c.onShowMenu=new i.util.Dispatcher(c);
c.onHideMenu=new i.util.Dispatcher(c);
c.value=b.default_color
},showMenu:function(){var e=this,d,a,b,c;
if(e.isDisabled()){return
}if(!e.isMenuRendered){e.renderMenu();
e.isMenuRendered=true
}if(e.isMenuVisible){return e.hideMenu()
}b=j.get(e.id);
j.show(e.id+"_menu");
j.addClass(b,"mceSplitButtonSelected");
c=j.getPos(b);
j.setStyles(e.id+"_menu",{left:c.x,top:c.y+b.firstChild.clientHeight,zIndex:200000});
b=0;
g.add(j.doc,"mousedown",e.hideMenu,e);
e.onShowMenu.dispatch(e);
if(e._focused){e._keyHandler=g.add(e.id+"_menu","keydown",function(l){if(l.keyCode==27){e.hideMenu()
}});
j.select("a",e.id+"_menu")[0].focus()
}e.keyboardNav=new i.ui.KeyboardNavigation({root:e.id+"_menu",items:j.select("a",e.id+"_menu"),onCancel:function(){e.hideMenu();
e.focus()
}});
e.keyboardNav.focus();
e.isMenuVisible=1
},hideMenu:function(a){var b=this;
if(b.isMenuVisible){if(a&&a.type=="mousedown"&&j.getParent(a.target,function(c){return c.id===b.id+"_open"
})){return
}if(!a||!j.getParent(a.target,".mceSplitButtonMenu")){j.removeClass(b.id,"mceSplitButtonSelected");
g.remove(j.doc,"mousedown",b.hideMenu,b);
g.remove(b.id+"_menu","keydown",b._keyHandler);
j.hide(b.id+"_menu")
}b.isMenuVisible=0;
b.onHideMenu.dispatch();
if(b.keyboardNav){b.keyboardNav.destroy()
}}},renderMenu:function(){var b=this,n,e=0,a=b.settings,r,m,d,c,s;
c=j.add(a.menu_container,"div",{role:"listbox",id:b.id+"_menu","class":a.menu_class+" "+a["class"],style:"position:absolute;left:0;top:-1000px;"});
n=j.add(c,"div",{"class":a["class"]+" mceSplitButtonMenu"});
j.add(n,"span",{"class":"mceMenuLine"});
r=j.add(n,"table",{role:"presentation","class":"mceColorSplitMenu"});
m=j.add(r,"tbody");
e=0;
h(f(a.colors,"array")?a.colors:a.colors.split(","),function(k){k=k.replace(/^#/,"");
if(!e--){d=j.add(m,"tr");
e=a.grid_width-1
}r=j.add(d,"td");
var l={href:"javascript:;",style:{backgroundColor:"#"+k},title:b.editor.getLang("colors."+k,k),"data-mce-color":"#"+k};
if(!i.isIE){l.role="option"
}r=j.add(r,"a",l);
if(b.editor.forcedHighContrastMode){r=j.add(r,"canvas",{width:16,height:16,"aria-hidden":"true"});
if(r.getContext&&(s=r.getContext("2d"))){s.fillStyle="#"+k;
s.fillRect(0,0,16,16)
}else{j.remove(r)
}}});
if(a.more_colors_func){r=j.add(m,"tr");
r=j.add(r,"td",{colspan:a.grid_width,"class":"mceMoreColors"});
r=j.add(r,"a",{role:"option",id:b.id+"_more",href:"javascript:;",onclick:"return false;","class":"mceMoreColors"},a.more_colors_title);
g.add(r,"click",function(k){a.more_colors_func.call(a.more_colors_scope||this);
return g.cancel(k)
})
}j.addClass(n,"mceColorSplitMenu");
g.add(b.id+"_menu","mousedown",function(k){return g.cancel(k)
});
g.add(b.id+"_menu","click",function(l){var k;
l=j.getParent(l.target,"a",m);
if(l&&l.nodeName.toLowerCase()=="a"&&(k=l.getAttribute("data-mce-color"))){b.setColor(k)
}return false
});
return c
},setColor:function(a){this.displayColor(a);
this.hideMenu();
this.settings.onselect(a)
},displayColor:function(a){var b=this;
j.setStyle(b.id+"_preview","backgroundColor",a);
b.value=a
},postRender:function(){var b=this,a=b.id;
b.parent();
j.add(a+"_action","div",{id:a+"_preview","class":"mceColorPreview"});
j.setStyle(b.id+"_preview","backgroundColor",b.value)
},destroy:function(){var a=this;
a.parent();
g.clear(a.id+"_menu");
g.clear(a.id+"_more");
j.remove(a.id+"_menu");
if(a.keyboardNav){a.keyboardNav.destroy()
}}})
})(tinymce);
(function(e){var g=e.DOM,h=e.each,f=e.dom.Event;
e.create("tinymce.ui.ToolbarGroup:tinymce.ui.Container",{renderHTML:function(){var d=this,b=[],k=d.controls,a=e.each,c=d.settings;
b.push('<div id="'+d.id+'" role="group" aria-labelledby="'+d.id+'_voice">');
b.push("<span role='application'>");
b.push('<span id="'+d.id+'_voice" class="mceVoiceLabel" style="display:none;">'+g.encode(c.name)+"</span>");
a(k,function(i){b.push(i.renderHTML())
});
b.push("</span>");
b.push("</div>");
return b.join("")
},focus:function(){var a=this;
g.get(a.id).focus()
},postRender:function(){var a=this,b=[];
h(a.controls,function(c){h(c.controls,function(d){if(d.id){b.push(d)
}})
});
a.keyNav=new e.ui.KeyboardNavigation({root:a.id,items:b,onCancel:function(){if(e.isWebKit){g.get(a.editor.id+"_ifr").focus()
}a.editor.focus()
},excludeFromTabOrder:!a.settings.tab_focus_toolbar})
},destroy:function(){var a=this;
a.parent();
a.keyNav.destroy();
f.clear(a.id)
}})
})(tinymce);
(function(e){var f=e.DOM,d=e.each;
e.create("tinymce.ui.Toolbar:tinymce.ui.Container",{renderHTML:function(){var b=this,p="",i,h,a=b.settings,q,r,o,c;
c=b.controls;
for(q=0;
q<c.length;
q++){h=c[q];
r=c[q-1];
o=c[q+1];
if(q===0){i="mceToolbarStart";
if(h.Button){i+=" mceToolbarStartButton"
}else{if(h.SplitButton){i+=" mceToolbarStartSplitButton"
}else{if(h.ListBox){i+=" mceToolbarStartListBox"
}}}p+=f.createHTML("td",{"class":i},f.createHTML("span",null,"<!-- IE -->"))
}if(r&&h.ListBox){if(r.Button||r.SplitButton){p+=f.createHTML("td",{"class":"mceToolbarEnd"},f.createHTML("span",null,"<!-- IE -->"))
}}if(f.stdMode){p+='<td style="position: relative">'+h.renderHTML()+"</td>"
}else{p+="<td>"+h.renderHTML()+"</td>"
}if(o&&h.ListBox){if(o.Button||o.SplitButton){p+=f.createHTML("td",{"class":"mceToolbarStart"},f.createHTML("span",null,"<!-- IE -->"))
}}}i="mceToolbarEnd";
if(h.Button){i+=" mceToolbarEndButton"
}else{if(h.SplitButton){i+=" mceToolbarEndSplitButton"
}else{if(h.ListBox){i+=" mceToolbarEndListBox"
}}}p+=f.createHTML("td",{"class":i},f.createHTML("span",null,"<!-- IE -->"));
return f.createHTML("table",{id:b.id,"class":"mceToolbar"+(a["class"]?" "+a["class"]:""),cellpadding:"0",cellspacing:"0",align:b.settings.align||"",role:"presentation",tabindex:"-1"},"<tbody><tr>"+p+"</tr></tbody>")
}})
})(tinymce);
(function(d){var e=d.util.Dispatcher,f=d.each;
d.create("tinymce.AddOnManager",{AddOnManager:function(){var a=this;
a.items=[];
a.urls={};
a.lookup={};
a.onAdd=new e(a)
},get:function(a){if(this.lookup[a]){return this.lookup[a].instance
}else{return undefined
}},dependencies:function(a){var b;
if(this.lookup[a]){b=this.lookup[a].dependencies
}return b||[]
},requireLangPack:function(a){var b=d.settings;
if(b&&b.language&&b.language_load!==false){d.ScriptLoader.add(this.urls[a]+"/langs/"+b.language+".js")
}},add:function(a,b,c){this.items.push(b);
this.lookup[a]={instance:b,dependencies:c};
this.onAdd.dispatch(this,a,b);
return b
},createUrl:function(b,a){if(typeof a==="object"){return a
}else{return{prefix:b.prefix,resource:a,suffix:b.suffix}
}},addComponents:function(a,c){var b=this.urls[a];
d.each(c,function(h){d.ScriptLoader.add(b+"/"+h)
})
},load:function(a,l,n,c){var k=this,m=l;
function b(){var g=k.dependencies(a);
d.each(g,function(h){var i=k.createUrl(l,h);
k.load(i.resource,i,undefined,undefined)
});
if(n){if(c){n.call(c)
}else{n.call(d.ScriptLoader)
}}}if(k.urls[a]){return
}if(typeof l==="object"){m=l.prefix+l.resource+l.suffix
}if(m.indexOf("/")!==0&&m.indexOf("://")==-1){m=d.baseURL+"/"+m
}k.urls[a]=m.substring(0,m.lastIndexOf("/"));
if(k.lookup[a]){b()
}else{d.ScriptLoader.add(m,b,c)
}}});
d.PluginManager=new d.AddOnManager();
d.ThemeManager=new d.AddOnManager()
}(tinymce));
(function(m){var p=m.each,s=m.extend,l=m.DOM,n=m.dom.Event,q=m.ThemeManager,u=m.PluginManager,r=m.explode,o=m.util.Dispatcher,v,t=0;
m.documentBaseURL=window.location.href.replace(/[\?#].*$/,"").replace(/[\/\\][^\/]+$/,"");
if(!/[\/\\]$/.test(m.documentBaseURL)){m.documentBaseURL+="/"
}m.baseURL=new m.util.URI(m.documentBaseURL).toAbsolute(m.baseURL);
m.baseURI=new m.util.URI(m.baseURL);
m.onBeforeUnload=new o(m);
n.add(window,"beforeunload",function(a){m.onBeforeUnload.dispatch(m,a)
});
m.onAddEditor=new o(m);
m.onRemoveEditor=new o(m);
m.EditorManager=s(m,{editors:[],i18n:{},activeEditor:null,init:function(h){var i=this,d,e=m.ScriptLoader,j,g=[],a;
function b(k){var x=k.id;
if(!x){x=k.name;
if(x&&!l.get(x)){x=k.name
}else{x=l.uniqueId()
}k.setAttribute("id",x)
}return x
}function f(B,k,x){var C=B[k];
if(!C){return
}if(m.is(C,"string")){x=C.replace(/\.\w+$/,"");
x=x?m.resolve(x):0;
C=m.resolve(C)
}return C.apply(x||this,Array.prototype.slice.call(arguments,2))
}function c(k,x){return x.constructor===RegExp?x.test(k.className):l.hasClass(k,x)
}i.settings=h;
n.bind(window,"ready",function(){var x,k;
f(h,"onpageload");
switch(h.mode){case"exact":x=h.elements||"";
if(x.length>0){p(r(x),function(z){if(l.get(z)){a=new m.Editor(z,h);
g.push(a);
a.render(1)
}else{p(document.forms,function(y){p(y.elements,function(B){if(B.name===z){z="mce_editor_"+t++;
l.setAttrib(B,"id",z);
a=new m.Editor(z,h);
g.push(a);
a.render(1)
}})
})
}})
}break;
case"textareas":case"specific_textareas":p(l.select("textarea"),function(z){if(h.editor_deselector&&c(z,h.editor_deselector)){return
}if(!h.editor_selector||c(z,h.editor_selector)){a=new m.Editor(b(z),h);
g.push(a);
a.render(1)
}});
break;
default:if(h.types){p(h.types,function(z){p(l.select(z.selector),function(y){var B=new m.Editor(b(y),m.extend({},h,z));
g.push(B);
B.render(1)
})
})
}else{if(h.selector){p(l.select(h.selector),function(A){var B=new m.Editor(b(A),h);
g.push(B);
B.render(1)
})
}}}if(h.oninit){x=k=0;
p(g,function(z){k++;
if(!z.initialized){z.onInit.add(function(){x++;
if(x==k){f(h,"oninit")
}})
}else{x++
}if(x==k){f(h,"oninit")
}})
}})
},get:function(a){if(a===v){return this.editors
}if(!this.editors.hasOwnProperty(a)){return v
}return this.editors[a]
},getInstanceById:function(a){return this.get(a)
},add:function(a){var b=this,c=b.editors;
c[a.id]=a;
c.push(a);
b._setActive(a);
b.onAddEditor.dispatch(b,a);
return a
},remove:function(d){var a=this,b,c=a.editors;
if(!c[d.id]){return null
}delete c[d.id];
for(b=0;
b<c.length;
b++){if(c[b]==d){c.splice(b,1);
break
}}if(a.activeEditor==d){a._setActive(c[0])
}d.destroy();
a.onRemoveEditor.dispatch(a,d);
return d
},execCommand:function(c,e,f){var d=this,g=d.get(f),b;
function a(){g.destroy();
b.detachEvent("onunload",a);
b=b.tinyMCE=b.tinymce=null
}switch(c){case"mceFocus":g.focus();
return true;
case"mceAddEditor":case"mceAddControl":if(!d.get(f)){new m.Editor(f,d.settings).render()
}return true;
case"mceAddFrameControl":b=f.window;
b.tinyMCE=tinyMCE;
b.tinymce=m;
m.DOM.doc=b.document;
m.DOM.win=b;
g=new m.Editor(f.element_id,f);
g.render();
if(m.isIE&&!m.isIE11){b.attachEvent("onunload",a)
}f.page_window=null;
return true;
case"mceRemoveEditor":case"mceRemoveControl":if(g){g.remove()
}return true;
case"mceToggleEditor":if(!g){d.execCommand("mceAddControl",0,f);
return true
}if(g.isHidden()){g.show()
}else{g.hide()
}return true
}if(d.activeEditor){return d.activeEditor.execCommand(c,e,f)
}return false
},execInstanceCommand:function(c,d,e,a){var b=this.get(c);
if(b){return b.execCommand(d,e,a)
}return false
},triggerSave:function(){p(this.editors,function(a){a.save()
})
},addI18n:function(d,c){var b,a=this.i18n;
if(!m.is(d,"string")){p(d,function(e,f){p(e,function(g,h){p(g,function(i,j){if(h==="common"){a[f+"."+j]=i
}else{a[f+"."+h+"."+j]=i
}})
})
})
}else{p(c,function(e,f){a[d+"."+f]=e
})
}},_setActive:function(a){this.selectedInstance=this.activeEditor=a
}})
})(tinymce);
(function(n){var m=n.DOM,o=n.dom.Event,s=n.extend,p=n.each,y=n.isGecko,x=n.isIE,t=n.isWebKit,u=n.is,q=n.ThemeManager,v=n.PluginManager,r=n.explode;
n.create("tinymce.Editor",{Editor:function(b,c){var a=this,d=true;
a.settings=c=s({id:b,language:"en",theme:"advanced",skin:"default",delta_width:0,delta_height:0,popup_css:"",plugins:"",document_base_url:n.documentBaseURL,add_form_submit_trigger:d,submit_patch:d,add_unload_trigger:d,convert_urls:d,relative_urls:d,remove_script_host:d,table_inline_editing:false,object_resizing:d,accessibility_focus:d,doctype:n.isIE6?'<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">':"<!DOCTYPE>",visual:d,font_size_style_values:"xx-small,x-small,small,medium,large,x-large,xx-large",font_size_legacy_values:"xx-small,small,medium,large,x-large,xx-large,300%",apply_source_formatting:d,directionality:"ltr",forced_root_block:"p",hidden_input:d,padd_empty_editor:d,render_ui:d,indentation:"30px",fix_table_elements:d,inline_styles:d,convert_fonts_to_spans:d,indent:"simple",indent_before:"p,h1,h2,h3,h4,h5,h6,blockquote,div,title,style,pre,script,td,ul,li,area,table,thead,tfoot,tbody,tr,section,article,hgroup,aside,figure,option,optgroup,datalist",indent_after:"p,h1,h2,h3,h4,h5,h6,blockquote,div,title,style,pre,script,td,ul,li,area,table,thead,tfoot,tbody,tr,section,article,hgroup,aside,figure,option,optgroup,datalist",validate:d,entity_encoding:"named",url_converter:a.convertURL,url_converter_scope:a,ie7_compat:d},c);
a.id=a.editorId=b;
a.isNotDirty=false;
a.plugins={};
a.documentBaseURI=new n.util.URI(c.document_base_url||n.documentBaseURL,{base_uri:tinyMCE.baseURI});
a.baseURI=n.baseURI;
a.contentCSS=[];
a.contentStyles=[];
a.setupEvents();
a.execCommands={};
a.queryStateCommands={};
a.queryValueCommands={};
a.execCallback("setup",a)
},render:function(e){var d=this,c=d.settings,b=d.id,a=n.ScriptLoader;
if(!o.domLoaded){o.add(window,"ready",function(){d.render()
});
return
}tinyMCE.settings=c;
if(!d.getElement()){return
}if(n.isIDevice&&!n.isIOS5){return
}if(!/TEXTAREA|INPUT/i.test(d.getElement().nodeName)&&c.hidden_input&&m.getParent(b,"form")){m.insertAfter(m.create("input",{type:"hidden",name:b}),b)
}if(!c.content_editable){d.orgVisibility=d.getElement().style.visibility;
d.getElement().style.visibility="hidden"
}if(n.WindowManager){d.windowManager=new n.WindowManager(d)
}if(c.encoding=="xml"){d.onGetContent.add(function(h,g){if(g.save){g.content=m.encode(g.content)
}})
}if(c.add_form_submit_trigger){d.onSubmit.addToTop(function(){if(d.initialized){d.save();
d.isNotDirty=1
}})
}if(c.add_unload_trigger){d._beforeUnload=tinyMCE.onBeforeUnload.add(function(){if(d.initialized&&!d.destroyed&&!d.isHidden()){d.save({format:"raw",no_events:true})
}})
}n.addUnload(d.destroy,d);
if(c.submit_patch){d.onBeforeRenderUI.add(function(){var g=d.getElement().form;
if(!g){return
}if(g._mceOldSubmit){return
}if(!g.submit.nodeType&&!g.submit.length){d.formElement=g;
g._mceOldSubmit=g.submit;
g.submit=function(){n.triggerSave();
d.isNotDirty=1;
return d.formElement._mceOldSubmit(d.formElement)
}
}g=null
})
}function f(){if(c.language&&c.language_load!==false){a.add(n.baseURL+"/langs/"+c.language+".js")
}if(c.theme&&typeof c.theme!="function"&&c.theme.charAt(0)!="-"&&!q.urls[c.theme]){q.load(c.theme,"themes/"+c.theme+"/editor_template"+n.suffix+".js")
}p(r(c.plugins),function(g){if(g&&!v.urls[g]){if(g.charAt(0)=="-"){g=g.substr(1,g.length);
var h=v.dependencies(g);
p(h,function(i){var j={prefix:"plugins/",resource:i,suffix:"/editor_plugin"+n.suffix+".js"};
i=v.createUrl(j,i);
v.load(i.resource,i)
})
}else{if(g=="safari"){return
}v.load(g,{prefix:"plugins/",resource:g,suffix:"/editor_plugin"+n.suffix+".js"})
}}});
a.loadQueue(function(){if(!d.removed){d.init()
}})
}f()
},init:function(){var d,J=this,I=J.settings,b,j,i,c=J.getElement(),f,h,L,l,e,K,k,a=[];
n.add(J);
I.aria_label=I.aria_label||m.getAttrib(c,"aria-label",J.getLang("aria.rich_text_area"));
if(I.theme){if(typeof I.theme!="function"){I.theme=I.theme.replace(/-/,"");
f=q.get(I.theme);
J.theme=new f();
if(J.theme.init){J.theme.init(J,q.urls[I.theme]||n.documentBaseURL.replace(/\/$/,""))
}}else{J.theme=I.theme
}}function g(A){var z=v.get(A),B=v.urls[A]||n.documentBaseURL.replace(/\/$/,""),C;
if(z&&n.inArray(a,A)===-1){p(v.dependencies(A),function(D){g(D)
});
C=new z(J,B);
J.plugins[A]=C;
if(C.init){C.init(J,B);
a.push(A)
}}}p(r(I.plugins.replace(/\-/g,"")),g);
if(I.popup_css!==false){if(I.popup_css){I.popup_css=J.documentBaseURI.toAbsolute(I.popup_css)
}else{I.popup_css=J.baseURI.toAbsolute("themes/"+I.theme+"/skins/"+I.skin+"/dialog.css")
}}if(I.popup_css_add){I.popup_css+=","+J.documentBaseURI.toAbsolute(I.popup_css_add)
}J.controlManager=new n.ControlManager(J);
J.onBeforeRenderUI.dispatch(J,J.controlManager);
if(I.render_ui&&J.theme){J.orgDisplay=c.style.display;
if(typeof I.theme!="function"){b=I.width||c.style.width||c.offsetWidth;
j=I.height||c.style.height||c.offsetHeight;
i=I.min_height||100;
K=/^[0-9\.]+(|px)$/i;
if(K.test(""+b)){b=Math.max(parseInt(b,10)+(f.deltaWidth||0),100)
}if(K.test(""+j)){j=Math.max(parseInt(j,10)+(f.deltaHeight||0),i)
}f=J.theme.renderUI({targetNode:c,width:b,height:j,deltaWidth:I.delta_width,deltaHeight:I.delta_height});
m.setStyles(f.sizeContainer||f.editorContainer,{width:b,height:j});
j=(f.iframeHeight||j)+(typeof(j)=="number"?(f.deltaHeight||0):"");
if(j<i){j=i
}}else{f=I.theme(J,c);
if(f.editorContainer.nodeType){f.editorContainer=f.editorContainer.id=f.editorContainer.id||J.id+"_parent"
}if(f.iframeContainer.nodeType){f.iframeContainer=f.iframeContainer.id=f.iframeContainer.id||J.id+"_iframecontainer"
}j=f.iframeHeight||c.offsetHeight;
if(x){J.onInit.add(function(z){z.dom.bind(z.getBody(),"beforedeactivate keydown keyup",function(){z.bookmark=z.selection.getBookmark(1)
})
});
J.onNodeChange.add(function(z){if(document.activeElement.id==z.id+"_ifr"){z.bookmark=z.selection.getBookmark(1)
}})
}}J.editorContainer=f.editorContainer
}if(I.content_css){p(r(I.content_css),function(z){J.contentCSS.push(J.documentBaseURI.toAbsolute(z))
})
}if(I.content_style){J.contentStyles.push(I.content_style)
}if(I.content_editable){c=d=f=null;
return J.initContentBody()
}if(document.domain&&location.hostname!=document.domain){n.relaxedDomain=document.domain
}J.iframeHTML=I.doctype+'<html><head xmlns="http://www.w3.org/1999/xhtml">';
if(I.document_base_url!=n.documentBaseURL){J.iframeHTML+='<base href="'+J.documentBaseURI.getURI()+'" />'
}if(n.isIE8){if(I.ie7_compat){J.iframeHTML+='<meta http-equiv="X-UA-Compatible" content="IE=7" />'
}else{J.iframeHTML+='<meta http-equiv="X-UA-Compatible" content="IE=edge" />'
}}J.iframeHTML+='<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />';
for(k=0;
k<J.contentCSS.length;
k++){J.iframeHTML+='<link type="text/css" rel="stylesheet" href="'+J.contentCSS[k]+'" />'
}J.contentCSS=[];
l=I.body_id||"tinymce";
if(l.indexOf("=")!=-1){l=J.getParam("body_id","","hash");
l=l[J.id]||l
}e=I.body_class||"";
if(e.indexOf("=")!=-1){e=J.getParam("body_class","","hash");
e=e[J.id]||""
}J.iframeHTML+='</head><body id="'+l+'" class="mceContentBody '+e+'" onload="window.parent.tinyMCE.get(\''+J.id+"').onLoad.dispatch();\"><br></body></html>";
if(n.relaxedDomain&&(x||(n.isOpera&&parseFloat(opera.version())<11))){L='javascript:(function(){document.open();document.domain="'+document.domain+'";var ed = window.parent.tinyMCE.get("'+J.id+'");document.write(ed.iframeHTML);document.close();ed.initContentBody();})()'
}d=m.add(f.iframeContainer,"iframe",{id:J.id+"_ifr",src:L||'javascript:""',frameBorder:"0",allowTransparency:"true",title:I.aria_label,style:{width:"100%",height:j,display:"block"}});
J.contentAreaContainer=f.iframeContainer;
if(f.editorContainer){m.get(f.editorContainer).style.display=J.orgDisplay
}c.style.visibility=J.orgVisibility;
m.get(J.id).style.display="none";
m.setAttrib(J.id,"aria-hidden",true);
if(!n.relaxedDomain||!L){J.initContentBody()
}c=d=f=null
},initContentBody:function(){var g=this,e=g.settings,d=m.get(g.id),c=g.getDoc(),f,a,b;
if((!x||!n.relaxedDomain)&&!e.content_editable){c.open();
c.write(g.iframeHTML);
c.close();
if(n.relaxedDomain){c.domain=n.relaxedDomain
}}if(e.content_editable){m.addClass(d,"mceContentBody");
g.contentDocument=c=e.content_document||document;
g.contentWindow=e.content_window||window;
g.bodyElement=d;
e.content_document=e.content_window=null
}a=g.getBody();
a.disabled=true;
if(!e.readonly){a.contentEditable=g.getParam("content_editable_state",true)
}a.disabled=false;
g.schema=new n.html.Schema(e);
g.dom=new n.dom.DOMUtils(c,{keep_values:true,url_converter:g.convertURL,url_converter_scope:g,hex_colors:e.force_hex_style_colors,class_filter:e.class_filter,update_styles:true,root_element:e.content_editable?g.id:null,schema:g.schema});
g.parser=new n.html.DomParser(e,g.schema);
g.parser.addAttributeFilter("src,href,style",function(l,k){var j=l.length,C,i=g.dom,B,h;
while(j--){C=l[j];
B=C.attr(k);
h="data-mce-"+k;
if(!C.attributes.map[h]){if(k==="style"){C.attr(h,i.serializeStyle(i.parseStyle(B),C.name))
}else{C.attr(h,g.convertURL(B,k,C.name))
}}}});
g.parser.addNodeFilter("script",function(k,j){var i=k.length,h;
while(i--){h=k[i];
h.attr("type","mce-"+(h.attr("type")||"text/javascript"))
}});
g.parser.addNodeFilter("#cdata",function(k,j){var i=k.length,h;
while(i--){h=k[i];
h.type=8;
h.name="#comment";
h.value="[CDATA["+h.value+"]]"
}});
g.parser.addNodeFilter("p,h1,h2,h3,h4,h5,h6,div",function(j,i){var h=j.length,l,k=g.schema.getNonEmptyElements();
while(h--){l=j[h];
if(l.isEmpty(k)){l.empty().append(new n.html.Node("br",1)).shortEnded=true
}}});
g.serializer=new n.dom.Serializer(e,g.dom,g.schema);
g.selection=new n.dom.Selection(g.dom,g.getWin(),g.serializer,g);
g.formatter=new n.Formatter(g);
g.undoManager=new n.UndoManager(g);
g.forceBlocks=new n.ForceBlocks(g);
g.enterKey=new n.EnterKey(g);
g.editorCommands=new n.EditorCommands(g);
g.onExecCommand.add(function(i,h){if(!/^(FontName|FontSize)$/.test(h)){g.nodeChanged()
}});
g.serializer.onPreProcess.add(function(i,h){return g.onPreProcess.dispatch(g,h,i)
});
g.serializer.onPostProcess.add(function(i,h){return g.onPostProcess.dispatch(g,h,i)
});
g.onPreInit.dispatch(g);
if(!e.browser_spellcheck&&!e.gecko_spellcheck){c.body.spellcheck=false
}if(!e.readonly){g.bindNativeEvents()
}g.controlManager.onPostRender.dispatch(g,g.controlManager);
g.onPostRender.dispatch(g);
g.quirks=n.util.Quirks(g);
if(e.directionality){a.dir=e.directionality
}if(e.nowrap){a.style.whiteSpace="nowrap"
}if(e.protect){g.onBeforeSetContent.add(function(i,h){p(e.protect,function(j){h.content=h.content.replace(j,function(k){return"<!--mce:protected "+escape(k)+"-->"
})
})
})
}g.onSetContent.add(function(){g.addVisual(g.getBody())
});
if(e.padd_empty_editor){g.onPostProcess.add(function(i,h){h.content=h.content.replace(/^(<p[^>]*>(&nbsp;|&#160;|\s|\u00a0|)<\/p>[\r\n]*|<br \/>[\r\n]*)$/,"")
})
}g.load({initial:true,format:"html"});
g.startContent=g.getContent({format:"raw"});
g.initialized=true;
g.onInit.dispatch(g);
g.execCallback("setupcontent_callback",g.id,a,c);
g.execCallback("init_instance_callback",g);
g.focus(true);
g.nodeChanged({initial:true});
if(g.contentStyles.length>0){b="";
p(g.contentStyles,function(h){b+=h+"\r\n"
});
g.dom.addStyle(b)
}p(g.contentCSS,function(h){g.dom.loadCSS(h)
});
if(e.auto_focus){setTimeout(function(){var h=n.get(e.auto_focus);
h.selection.select(h.getBody(),1);
h.selection.collapse(1);
h.getBody().focus();
h.getWin().focus()
},100)
}d=c=a=null
},focus:function(c){var d,g=this,h=g.selection,b=g.settings.content_editable,e,a,i=g.getDoc(),f;
if(!c){if(g.bookmark){h.moveToBookmark(g.bookmark);
g.bookmark=null
}e=h.getRng();
if(e.item){a=e.item(0)
}g._refreshContentEditable();
if(!b){g.getWin().focus()
}if(n.isGecko||b){f=g.getBody();
if(f.setActive&&!n.isIE11){f.setActive()
}else{f.focus()
}if(b){h.normalize()
}}if(a&&a.ownerDocument==i){e=i.body.createControlRange();
e.addElement(a);
e.select()
}}if(n.activeEditor!=g){if((d=n.activeEditor)!=null){d.onDeactivate.dispatch(d,g)
}g.onActivate.dispatch(g,d)
}n._setActive(g)
},execCallback:function(b){var a=this,c=a.settings[b],d;
if(!c){return
}if(a.callbackLookup&&(d=a.callbackLookup[b])){c=d.func;
d=d.scope
}if(u(c,"string")){d=c.replace(/\.\w+$/,"");
d=d?n.resolve(d):0;
c=n.resolve(c);
a.callbackLookup=a.callbackLookup||{};
a.callbackLookup[b]={func:c,scope:d}
}return c.apply(d||a,Array.prototype.slice.call(arguments,1))
},translate:function(a){var b=this.settings.language||"en",c=n.i18n;
if(!a){return""
}return c[b+"."+a]||a.replace(/\{\#([^\}]+)\}/g,function(d,e){return c[b+"."+e]||"{#"+e+"}"
})
},getLang:function(b,a){return n.i18n[(this.settings.language||"en")+"."+b]||(u(a)?a:"{#"+b+"}")
},getParam:function(b,e,a){var d=n.trim,f=u(this.settings[b])?this.settings[b]:e,c;
if(a==="hash"){c={};
if(u(f,"string")){p(f.indexOf("=")>0?f.split(/[;,](?![^=;,]*(?:[;,]|$))/):f.split(","),function(g){g=g.split("=");
if(g.length>1){c[d(g[0])]=d(g[1])
}else{c[d(g[0])]=d(g)
}})
}else{c=f
}return c
}return f
},nodeChanged:function(b){var a=this,d=a.selection,c;
if(a.initialized){b=b||{};
c=d.getStart()||a.getBody();
c=x&&c.ownerDocument!=a.getDoc()?a.getBody():c;
b.parents=[];
a.dom.getParent(c,function(e){if(e.nodeName=="BODY"){return true
}b.parents.push(e)
});
a.onNodeChange.dispatch(a,b?b.controlManager||a.controlManager:a.controlManager,c,d.isCollapsed(),b)
}},addButton:function(c,b){var a=this;
a.buttons=a.buttons||{};
a.buttons[c]=b
},addCommand:function(a,b,c){this.execCommands[a]={func:b,scope:c||this}
},addQueryStateHandler:function(a,b,c){this.queryStateCommands[a]={func:b,scope:c||this}
},addQueryValueHandler:function(a,b,c){this.queryValueCommands[a]={func:b,scope:c||this}
},addShortcut:function(e,c,a,d){var f=this,b;
if(f.settings.custom_shortcuts===false){return false
}f.shortcuts=f.shortcuts||{};
if(u(a,"string")){b=a;
a=function(){f.execCommand(b,false,null)
}
}if(u(a,"object")){b=a;
a=function(){f.execCommand(b[0],b[1],b[2])
}
}p(r(e),function(h){var g={func:a,scope:d||this,desc:f.translate(c),alt:false,ctrl:false,shift:false};
p(r(h,"+"),function(i){switch(i){case"alt":case"ctrl":case"shift":g[i]=true;
break;
default:g.charCode=i.charCodeAt(0);
g.keyCode=i.toUpperCase().charCodeAt(0)
}});
f.shortcuts[(g.ctrl?"ctrl":"")+","+(g.alt?"alt":"")+","+(g.shift?"shift":"")+","+g.keyCode]=g
});
return true
},execCommand:function(d,e,b,a){var g=this,f=0,c,h;
if(!/^(mceAddUndoLevel|mceEndUndoLevel|mceBeginUndoLevel|mceRepaint|SelectAll)$/.test(d)&&(!a||!a.skip_focus)){g.focus()
}a=s({},a);
g.onBeforeExecCommand.dispatch(g,d,e,b,a);
if(a.terminate){return false
}if(g.execCallback("execcommand_callback",g.id,g.selection.getNode(),d,e,b)){g.onExecCommand.dispatch(g,d,e,b,a);
return true
}if(c=g.execCommands[d]){h=c.func.call(c.scope,e,b);
if(h!==true){g.onExecCommand.dispatch(g,d,e,b,a);
return h
}}p(g.plugins,function(i){if(i.execCommand&&i.execCommand(d,e,b)){g.onExecCommand.dispatch(g,d,e,b,a);
f=1;
return false
}});
if(f){return true
}if(g.theme&&g.theme.execCommand&&g.theme.execCommand(d,e,b)){g.onExecCommand.dispatch(g,d,e,b,a);
return true
}if(g.editorCommands.execCommand(d,e,b)){g.onExecCommand.dispatch(g,d,e,b,a);
return true
}g.getDoc().execCommand(d,e,b);
g.onExecCommand.dispatch(g,d,e,b,a)
},queryCommandState:function(c){var e=this,b,d;
if(e._isHidden()){return
}if(b=e.queryStateCommands[c]){d=b.func.call(b.scope);
if(d!==true){return d
}}b=e.editorCommands.queryCommandState(c);
if(b!==-1){return b
}try{return this.getDoc().queryCommandState(c)
}catch(a){}},queryCommandValue:function(b){var e=this,c,d;
if(e._isHidden()){return
}if(c=e.queryValueCommands[b]){d=c.func.call(c.scope);
if(d!==true){return d
}}c=e.editorCommands.queryCommandValue(b);
if(u(c)){return c
}try{return this.getDoc().queryCommandValue(b)
}catch(a){}},show:function(){var a=this;
m.show(a.getContainer());
m.hide(a.id);
a.load()
},hide:function(){var a=this,b=a.getDoc();
if(x&&b){b.execCommand("SelectAll")
}a.save();
m.hide(a.getContainer());
m.setStyle(a.id,"display",a.orgDisplay)
},isHidden:function(){return !m.isHidden(this.id)
},setProgressState:function(a,c,b){this.onSetProgressState.dispatch(this,a,c,b);
return a
},load:function(b){var a=this,c=a.getElement(),d;
if(c){b=b||{};
b.load=true;
d=a.setContent(u(c.value)?c.value:c.innerHTML,b);
b.element=c;
if(!b.no_events){a.onLoadContent.dispatch(a,b)
}b.element=c=null;
return d
}},save:function(b){var a=this,c=a.getElement(),e,d;
if(!c||!a.initialized){return
}b=b||{};
b.save=true;
b.element=c;
e=b.content=a.getContent(b);
if(!b.no_events){a.onSaveContent.dispatch(a,b)
}e=b.content;
if(!/TEXTAREA|INPUT/i.test(c.nodeName)){c.innerHTML=e;
if(d=m.getParent(a.id,"form")){p(d.elements,function(f){if(f.name==a.id){f.value=e;
return false
}})
}}else{c.value=e
}b.element=c=null;
return e
},setContent:function(b,d){var e=this,f,a=e.getBody(),c;
d=d||{};
d.format=d.format||"html";
d.set=true;
d.content=b;
if(!d.no_events){e.onBeforeSetContent.dispatch(e,d)
}b=d.content;
if(!n.isIE&&(b.length===0||/^\s+$/.test(b))){c=e.settings.forced_root_block;
if(c){b="<"+c+'><br data-mce-bogus="1"></'+c+">"
}else{b='<br data-mce-bogus="1">'
}a.innerHTML=b;
e.selection.select(a,true);
e.selection.collapse(true);
return
}if(d.format!=="raw"){b=new n.html.Serializer({},e.schema).serialize(e.parser.parse(b))
}d.content=n.trim(b);
e.dom.setHTML(a,d.content);
if(!d.no_events){e.onSetContent.dispatch(e,d)
}if(!e.settings.content_editable||document.activeElement===e.getBody()){e.selection.normalize()
}return d.content
},getContent:function(c){var d=this,b,a=d.getBody();
c=c||{};
c.format=c.format||"html";
c.get=true;
c.getInner=true;
if(!c.no_events){d.onBeforeGetContent.dispatch(d,c)
}if(c.format=="raw"){b=a.innerHTML
}else{if(c.format=="text"){b=a.innerText||a.textContent
}else{b=d.serializer.serialize(a,c)
}}if(c.format!="text"){c.content=n.trim(b)
}else{c.content=b
}if(!c.no_events){d.onGetContent.dispatch(d,c)
}return c.content
},isDirty:function(){var a=this;
return n.trim(a.startContent)!=n.trim(a.getContent({format:"raw",no_events:1}))&&!a.isNotDirty
},getContainer:function(){var a=this;
if(!a.container){a.container=m.get(a.editorContainer||a.id+"_parent")
}return a.container
},getContentAreaContainer:function(){return this.contentAreaContainer
},getElement:function(){return m.get(this.settings.content_element||this.id)
},getWin:function(){var a=this,b;
if(!a.contentWindow){b=m.get(a.id+"_ifr");
if(b){a.contentWindow=b.contentWindow
}}return a.contentWindow
},getDoc:function(){var a=this,b;
if(!a.contentDocument){b=a.getWin();
if(b){a.contentDocument=b.document
}}return a.contentDocument
},getBody:function(){return this.bodyElement||this.getDoc().body
},convertURL:function(d,e,b){var a=this,c=a.settings;
if(c.urlconverter_callback){return a.execCallback("urlconverter_callback",d,b,true,e)
}if(!c.convert_urls||(b&&b.nodeName=="LINK")||d.indexOf("file:")===0){return d
}if(c.relative_urls){return a.documentBaseURI.toRelative(d)
}d=a.documentBaseURI.toAbsolute(d,c.remove_script_host);
return d
},addVisual:function(b){var e=this,d=e.settings,c=e.dom,a;
b=b||e.getBody();
if(!u(e.hasVisual)){e.hasVisual=d.visual
}p(c.select("table,a",b),function(f){var g;
switch(f.nodeName){case"TABLE":a=d.visual_table_class||"mceItemTable";
g=c.getAttrib(f,"border");
if(!g||g=="0"){if(e.hasVisual){c.addClass(f,a)
}else{c.removeClass(f,a)
}}return;
case"A":if(!c.getAttrib(f,"href",false)){g=c.getAttrib(f,"name")||f.id;
a="mceItemAnchor";
if(g){if(e.hasVisual){c.addClass(f,a)
}else{c.removeClass(f,a)
}}}return
}});
e.onVisualAid.dispatch(e,b,e.hasVisual)
},remove:function(){var a=this,b=a.getContainer(),c=a.getDoc();
if(!a.removed){a.removed=1;
if(x&&c){c.execCommand("SelectAll")
}a.save();
m.setStyle(a.id,"display",a.orgDisplay);
if(!a.settings.content_editable){o.unbind(a.getWin());
o.unbind(a.getDoc())
}o.unbind(a.getBody());
o.clear(b);
a.execCallback("remove_instance_callback",a);
a.onRemove.dispatch(a);
a.onExecCommand.listeners=[];
n.remove(a);
m.remove(b)
}},destroy:function(b){var a=this;
if(a.destroyed){return
}if(y){o.unbind(a.getDoc());
o.unbind(a.getWin());
o.unbind(a.getBody())
}if(!b){n.removeUnload(a.destroy);
tinyMCE.onBeforeUnload.remove(a._beforeUnload);
if(a.theme&&a.theme.destroy){a.theme.destroy()
}a.controlManager.destroy();
a.selection.destroy();
a.dom.destroy()
}if(a.formElement){a.formElement.submit=a.formElement._mceOldSubmit;
a.formElement._mceOldSubmit=null
}a.contentAreaContainer=a.formElement=a.container=a.settings.content_element=a.bodyElement=a.contentDocument=a.contentWindow=null;
if(a.selection){a.selection=a.selection.win=a.selection.dom=a.selection.dom.doc=null
}a.destroyed=1
},_refreshContentEditable:function(){var c=this,a,b;
if(c._isHidden()){a=c.getBody();
b=a.parentNode;
b.removeChild(a);
b.appendChild(a);
a.focus()
}},_isHidden:function(){var a;
if(!y){return 0
}a=this.selection.getSel();
return(!a||!a.rangeCount||a.rangeCount===0)
}})
})(tinymce);
(function(d){var c=d.each;
d.Editor.prototype.setupEvents=function(){var b=this,a=b.settings;
c(["onPreInit","onBeforeRenderUI","onPostRender","onLoad","onInit","onRemove","onActivate","onDeactivate","onClick","onEvent","onMouseUp","onMouseDown","onDblClick","onKeyDown","onKeyUp","onKeyPress","onContextMenu","onSubmit","onReset","onPaste","onPreProcess","onPostProcess","onBeforeSetContent","onBeforeGetContent","onSetContent","onGetContent","onLoadContent","onSaveContent","onNodeChange","onChange","onBeforeExecCommand","onExecCommand","onUndo","onRedo","onVisualAid","onSetProgressState","onSetAttrib"],function(f){b[f]=new d.util.Dispatcher(b)
});
if(a.cleanup_callback){b.onBeforeSetContent.add(function(h,g){g.content=h.execCallback("cleanup_callback","insert_to_editor",g.content,g)
});
b.onPreProcess.add(function(h,g){if(g.set){h.execCallback("cleanup_callback","insert_to_editor_dom",g.node,g)
}if(g.get){h.execCallback("cleanup_callback","get_from_editor_dom",g.node,g)
}});
b.onPostProcess.add(function(h,g){if(g.set){g.content=h.execCallback("cleanup_callback","insert_to_editor",g.content,g)
}if(g.get){g.content=h.execCallback("cleanup_callback","get_from_editor",g.content,g)
}})
}if(a.save_callback){b.onGetContent.add(function(h,g){if(g.save){g.content=h.execCallback("save_callback",h.id,g.content,h.getBody())
}})
}if(a.handle_event_callback){b.onEvent.add(function(j,i,e){if(b.execCallback("handle_event_callback",i,j,e)===false){i.preventDefault();
i.stopPropagation()
}})
}if(a.handle_node_change_callback){b.onNodeChange.add(function(i,j,h){i.execCallback("handle_node_change_callback",i.id,h,-1,-1,true,i.selection.isCollapsed())
})
}if(a.save_callback){b.onSaveContent.add(function(j,h){var i=j.execCallback("save_callback",j.id,h.content,j.getBody());
if(i){h.content=i
}})
}if(a.onchange_callback){b.onChange.add(function(g,h){g.execCallback("onchange_callback",g,h)
})
}};
d.Editor.prototype.bindNativeEvents=function(){var a=this,o,q=a.settings,p=a.dom,m;
m={mouseup:"onMouseUp",mousedown:"onMouseDown",click:"onClick",keyup:"onKeyUp",keydown:"onKeyDown",keypress:"onKeyPress",submit:"onSubmit",reset:"onReset",contextmenu:"onContextMenu",dblclick:"onDblClick",paste:"onPaste"};
function r(f,e){var g=f.type;
if(a.removed){return
}if(a.onEvent.dispatch(a,f,e)!==false){a[m[f.fakeType||f.type]].dispatch(a,f,e)
}}function i(e){a.focus(true)
}function b(f,e){if(e.keyCode!=65||!d.VK.metaKeyPressed(e)){a.selection.normalize()
}a.nodeChanged()
}c(m,function(e,g){var f=q.content_editable?a.getBody():a.getDoc();
switch(g){case"contextmenu":p.bind(f,g,r);
break;
case"paste":p.bind(a.getBody(),g,r);
break;
case"submit":case"reset":p.bind(a.getElement().form||d.DOM.getParent(a.id,"form"),g,r);
break;
default:p.bind(f,g,r)
}});
p.bind(q.content_editable?a.getBody():(d.isGecko?a.getDoc():a.getWin()),"focus",function(e){a.focus(true)
});
if(q.content_editable&&d.isOpera){p.bind(a.getBody(),"click",i);
p.bind(a.getBody(),"keydown",i)
}a.onMouseUp.add(b);
a.onKeyUp.add(function(f,g){var e=g.keyCode;
if((e>=33&&e<=36)||(e>=37&&e<=40)||e==13||e==45||e==46||e==8||(d.isMac&&(e==91||e==93))||g.ctrlKey){b(f,g)
}});
a.onReset.add(function(){a.setContent(a.startContent,{format:"raw"})
});
function n(e,f){if(e.altKey||e.ctrlKey||e.metaKey){c(a.shortcuts,function(h){var g=d.isMac?e.metaKey:e.ctrlKey;
if(h.ctrl!=g||h.alt!=e.altKey||h.shift!=e.shiftKey){return
}if(e.keyCode==h.keyCode||(e.charCode&&e.charCode==h.charCode)){e.preventDefault();
if(f){h.func.call(h.scope)
}return true
}})
}}a.onKeyUp.add(function(f,e){n(e)
});
a.onKeyPress.add(function(f,e){n(e)
});
a.onKeyDown.add(function(f,e){n(e,true)
});
if(d.isOpera){a.onClick.add(function(f,e){e.preventDefault()
})
}}
})(tinymce);
(function(i){var h=i.each,f,g=true,j=false;
i.EditorCommands=function(e){var v=e.dom,c=e.selection,z={state:{},exec:{},value:{}},y=e.settings,b=e.formatter,d;
function a(m,n,k){var l;
m=m.toLowerCase();
if(l=z.exec[m]){l(m,n,k);
return g
}return j
}function x(k){var l;
k=k.toLowerCase();
if(l=z.state[k]){return l(k)
}return -1
}function B(k){var l;
k=k.toLowerCase();
if(l=z.value[k]){return l(k)
}return j
}function E(l,k){k=k||"exec";
h(l,function(m,n){h(n.toLowerCase().split(","),function(o){z[k][o]=m
})
})
}i.extend(this,{execCommand:a,queryCommandState:x,queryCommandValue:B,addCommands:E});
function D(m,k,l){if(k===f){k=j
}if(l===f){l=null
}return e.getDoc().execCommand(m,k,l)
}function F(k){return b.match(k)
}function G(l,k){b.toggle(l,k?{value:k}:f)
}function A(k){d=c.getBookmark(k)
}function C(){c.moveToBookmark(d)
}E({"mceResetDesignMode,mceBeginUndoLevel":function(){},"mceEndUndoLevel,mceAddUndoLevel":function(){e.undoManager.add()
},"Cut,Copy,Paste":function(m){var n=e.getDoc(),l;
try{D(m)
}catch(k){l=g
}if(l||!n.queryCommandSupported(m)){if(i.isGecko){e.windowManager.confirm(e.getLang("clipboard_msg"),function(o){if(o){open("http://www.mozilla.org/editor/midasdemo/securityprefs.html","_blank")
}})
}else{e.windowManager.alert(e.getLang("clipboard_no_support"))
}}},unlink:function(k){if(c.isCollapsed()){c.select(c.getNode())
}D(k);
c.collapse(j)
},"JustifyLeft,JustifyCenter,JustifyRight,JustifyFull":function(l){var k=l.substring(7);
h("left,center,right,full".split(","),function(m){if(k!=m){b.remove("align"+m)
}});
G("align"+k);
a("mceRepaint")
},"InsertUnorderedList,InsertOrderedList":function(m){var l,k;
D(m);
l=v.getParent(c.getNode(),"ol,ul");
if(l){k=l.parentNode;
if(/^(H[1-6]|P|ADDRESS|PRE)$/.test(k.nodeName)){A();
v.split(k,l);
C()
}}},"Bold,Italic,Underline,Strikethrough,Superscript,Subscript":function(k){G(k)
},"ForeColor,HiliteColor,FontName":function(m,k,l){G(m,l)
},FontSize:function(n,o,l){var m,k;
if(l>=1&&l<=7){k=i.explode(y.font_size_style_values);
m=i.explode(y.font_size_classes);
if(m){l=m[l-1]||l
}else{l=k[l-1]||l
}}G(n,l)
},RemoveFormat:function(k){b.remove(k)
},mceBlockQuote:function(k){G("blockquote")
},FormatBlock:function(m,k,l){return G(l||"p")
},mceCleanup:function(){var k=c.getBookmark();
e.setContent(e.getContent({cleanup:g}),{cleanup:g});
c.moveToBookmark(k)
},mceRemoveNode:function(m,n,k){var l=k||c.getNode();
if(l!=e.getBody()){A();
e.dom.remove(l,g);
C()
}},mceSelectNodeDepth:function(m,n,k){var l=0;
v.getParent(c.getNode(),function(o){if(o.nodeType==1&&l++==k){c.select(o);
return j
}},e.getBody())
},mceSelectNode:function(m,k,l){c.select(l)
},mceInsertContent:function(m,N,s){var r,u,S,q,R,Q,k,l,p,t,n,o,P,O;
r=e.parser;
u=new i.html.Serializer({},e.schema);
P='<span id="mce_marker" data-mce-type="bookmark">\uFEFF</span>';
Q={content:s,format:"html"};
c.onBeforeSetContent.dispatch(c,Q);
s=Q.content;
if(s.indexOf("{$caret}")==-1){s+="{$caret}"
}s=s.replace(/\{\$caret\}/,P);
if(!c.isCollapsed()){e.getDoc().execCommand("Delete",false,null)
}S=c.getNode();
Q={context:S.nodeName.toLowerCase()};
R=r.parse(s,Q);
n=R.lastChild;
if(n.attr("id")=="mce_marker"){k=n;
for(n=n.prev;
n;
n=n.walk(true)){if(n.type==3||!v.isBlock(n.name)){n.parent.insert(k,n,n.name==="br");
break
}}}if(!Q.invalid){s=u.serialize(R);
n=S.firstChild;
o=S.lastChild;
if(!n||(n===o&&n.nodeName==="BR")){v.setHTML(S,s)
}else{c.setContent(s)
}}else{c.setContent(P);
S=c.getNode();
q=e.getBody();
if(S.nodeType==9){S=n=q
}else{n=S
}while(n!==q){S=n;
n=n.parentNode
}s=S==q?q.innerHTML:v.getOuterHTML(S);
s=u.serialize(r.parse(s.replace(/<span (id="mce_marker"|id=mce_marker).+?<\/span>/i,function(){return u.serialize(R)
})));
if(S==q){v.setHTML(q,s)
}else{v.setOuterHTML(S,s)
}}k=v.get("mce_marker");
l=v.getRect(k);
p=v.getViewPort(e.getWin());
if((l.y+l.h>p.y+p.h||l.y<p.y)||(l.x>p.x+p.w||l.x<p.x)){O=i.isIE?e.getDoc().documentElement:e.getBody();
O.scrollLeft=l.x;
O.scrollTop=l.y-p.h+25
}t=v.createRng();
n=k.previousSibling;
if(n&&n.nodeType==3){t.setStart(n,n.nodeValue.length)
}else{t.setStartBefore(k);
t.setEndBefore(k)
}v.remove(k);
c.setRng(t);
c.onSetContent.dispatch(c,Q);
e.addVisual()
},mceInsertRawHTML:function(m,k,l){c.setContent("tiny_mce_marker");
e.setContent(e.getContent().replace(/tiny_mce_marker/g,function(){return l
}))
},mceToggleFormat:function(m,k,l){G(l)
},mceSetContent:function(m,k,l){e.setContent(l)
},"Indent,Outdent":function(m){var k,l,n;
k=y.indentation;
l=/[a-z%]+$/i.exec(k);
k=parseInt(k);
if(!x("InsertUnorderedList")&&!x("InsertOrderedList")){if(!y.forced_root_block&&!v.getParent(c.getNode(),v.isBlock)){b.apply("div")
}h(c.getSelectedBlocks(),function(o){if(m=="outdent"){n=Math.max(0,parseInt(o.style.paddingLeft||0)-k);
v.setStyle(o,"paddingLeft",n?n+l:"")
}else{v.setStyle(o,"paddingLeft",(parseInt(o.style.paddingLeft||0)+k)+l)
}})
}else{D(m)
}},mceRepaint:function(){var k;
if(i.isGecko){try{A(g);
if(c.getSel()){c.getSel().selectAllChildren(e.getBody())
}c.collapse(g);
C()
}catch(l){}}},mceToggleFormat:function(m,k,l){b.toggle(l)
},InsertHorizontalRule:function(){e.execCommand("mceInsertContent",false,"<hr />")
},mceToggleVisualAid:function(){e.hasVisual=!e.hasVisual;
e.addVisual()
},mceReplaceContent:function(m,k,l){e.execCommand("mceInsertContent",false,l.replace(/\{\$selection\}/g,c.getContent({format:"text"})))
},mceInsertLink:function(m,n,k){var l;
if(typeof(k)=="string"){k={href:k}
}l=v.getParent(c.getNode(),"a");
k.href=k.href.replace(" ","%20");
if(!l||!k.href){b.remove("link")
}if(k.href){b.apply("link",k,l)
}},selectAll:function(){var k=v.getRoot(),l=v.createRng();
if(c.getRng().setStart){l.setStart(k,0);
l.setEnd(k,k.childNodes.length);
c.setRng(l)
}else{D("SelectAll")
}}});
E({"JustifyLeft,JustifyCenter,JustifyRight,JustifyFull":function(m){var k="align"+m.substring(7);
var l=c.isCollapsed()?[v.getParent(c.getNode(),v.isBlock)]:c.getSelectedBlocks();
var n=i.map(l,function(o){return !!b.matchNode(o,k)
});
return i.inArray(n,g)!==-1
},"Bold,Italic,Underline,Strikethrough,Superscript,Subscript":function(k){return F(k)
},mceBlockQuote:function(){return F("blockquote")
},Outdent:function(){var k;
if(y.inline_styles){if((k=v.getParent(c.getStart(),v.isBlock))&&parseInt(k.style.paddingLeft)>0){return g
}if((k=v.getParent(c.getEnd(),v.isBlock))&&parseInt(k.style.paddingLeft)>0){return g
}}return x("InsertUnorderedList")||x("InsertOrderedList")||(!y.inline_styles&&!!v.getParent(c.getNode(),"BLOCKQUOTE"))
},"InsertUnorderedList,InsertOrderedList":function(k){var l=v.getParent(c.getNode(),"ul,ol");
return l&&(k==="insertunorderedlist"&&l.tagName==="UL"||k==="insertorderedlist"&&l.tagName==="OL")
}},"state");
E({"FontSize,FontName":function(m){var k=0,l;
if(l=v.getParent(c.getNode(),"span")){if(m=="fontsize"){k=l.style.fontSize
}else{k=l.style.fontFamily.replace(/, /g,",").replace(/[\'\"]/g,"").toLowerCase()
}}return k
}},"value");
E({Undo:function(){e.undoManager.undo()
},Redo:function(){e.undoManager.redo()
}})
}
})(tinymce);
(function(c){var d=c.util.Dispatcher;
c.UndoManager=function(o){var a,n=0,r=[],p,b,m,q;
function t(){return c.trim(o.getContent({format:"raw",no_events:1}).replace(/<span[^>]+data-mce-bogus[^>]+>[\u200B\uFEFF]+<\/span>/g,""))
}function s(){a.typing=false;
a.add()
}onBeforeAdd=new d(a);
b=new d(a);
m=new d(a);
q=new d(a);
b.add(function(e,f){if(e.hasUndo()){return o.onChange.dispatch(o,f,e)
}});
m.add(function(e,f){return o.onUndo.dispatch(o,f,e)
});
q.add(function(e,f){return o.onRedo.dispatch(o,f,e)
});
o.onInit.add(function(){a.add()
});
o.onBeforeExecCommand.add(function(e,g,h,f,i){if(g!="Undo"&&g!="Redo"&&g!="mceRepaint"&&(!i||!i.skip_undo)){a.beforeChange()
}});
o.onExecCommand.add(function(e,g,h,f,i){if(g!="Undo"&&g!="Redo"&&g!="mceRepaint"&&(!i||!i.skip_undo)){a.add()
}});
o.onSaveContent.add(s);
o.dom.bind(o.dom.getRoot(),"dragend",s);
o.dom.bind(o.getBody(),"focusout",function(e){if(!o.removed&&a.typing){s()
}});
o.onKeyUp.add(function(e,f){var g=f.keyCode;
if((g>=33&&g<=36)||(g>=37&&g<=40)||g==45||g==13||f.ctrlKey){s()
}});
o.onKeyDown.add(function(e,f){var g=f.keyCode;
if((g>=33&&g<=36)||(g>=37&&g<=40)||g==45){if(a.typing){s()
}return
}if((g<16||g>20)&&g!=224&&g!=91&&!a.typing){a.beforeChange();
a.typing=true;
a.add()
}});
o.onMouseDown.add(function(e,f){if(a.typing){s()
}});
o.addShortcut("ctrl+z","undo_desc","Undo");
o.addShortcut("ctrl+y","redo_desc","Redo");
a={data:r,typing:false,onBeforeAdd:onBeforeAdd,onAdd:b,onUndo:m,onRedo:q,beforeChange:function(){p=o.selection.getBookmark(2,true)
},add:function(f){var e,h=o.settings,g;
f=f||{};
f.content=t();
a.onBeforeAdd.dispatch(a,f);
g=r[n];
if(g&&g.content==f.content){return null
}if(r[n]){r[n].beforeBookmark=p
}if(h.custom_undo_redo_levels){if(r.length>h.custom_undo_redo_levels){for(e=0;
e<r.length-1;
e++){r[e]=r[e+1]
}r.length--;
n=r.length
}}f.bookmark=o.selection.getBookmark(2,true);
if(n<r.length-1){r.length=n+1
}r.push(f);
n=r.length-1;
a.onAdd.dispatch(a,f);
o.isNotDirty=0;
return f
},undo:function(){var f,e;
if(a.typing){a.add();
a.typing=false
}if(n>0){f=r[--n];
o.setContent(f.content,{format:"raw"});
o.selection.moveToBookmark(f.beforeBookmark);
a.onUndo.dispatch(a,f)
}return f
},redo:function(){var e;
if(n<r.length-1){e=r[++n];
o.setContent(e.content,{format:"raw"});
o.selection.moveToBookmark(e.bookmark);
a.onRedo.dispatch(a,e)
}return e
},clear:function(){r=[];
n=0;
a.typing=false
},hasUndo:function(){return n>0||this.typing
},hasRedo:function(){return n<r.length-1&&!this.typing
}};
return a
}
})(tinymce);
tinymce.ForceBlocks=function(l){var g=l.settings,j=l.dom,h=l.selection,k=l.schema.getBlockElements();
function i(){var v=h.getStart(),y=l.getBody(),z,u,d,A,b,x,t,f=-16777215,c,a;
if(!v||v.nodeType!==1||!g.forced_root_block){return
}while(v&&v!=y){if(k[v.nodeName]){return
}v=v.parentNode
}z=h.getRng();
if(z.setStart){u=z.startContainer;
d=z.startOffset;
A=z.endContainer;
b=z.endOffset
}else{if(z.item){v=z.item(0);
z=l.getDoc().body.createTextRange();
z.moveToElementText(v)
}a=z.parentElement().ownerDocument===l.getDoc();
tmpRng=z.duplicate();
tmpRng.collapse(true);
d=tmpRng.move("character",f)*-1;
if(!tmpRng.collapsed){tmpRng=z.duplicate();
tmpRng.collapse(false);
b=(tmpRng.move("character",f)*-1)-d
}}v=y.firstChild;
while(v){if(v.nodeType===3||(v.nodeType==1&&!k[v.nodeName])){if(v.nodeType===3&&v.nodeValue.length==0){t=v;
v=v.nextSibling;
j.remove(t);
continue
}if(!x){x=j.create(g.forced_root_block);
v.parentNode.insertBefore(x,v);
c=true
}t=v;
v=v.nextSibling;
x.appendChild(t)
}else{x=null;
v=v.nextSibling
}}if(c){if(z.setStart){z.setStart(u,d);
z.setEnd(A,b);
h.setRng(z)
}else{if(a){try{z=l.getDoc().body.createTextRange();
z.moveToElementText(y);
z.collapse(true);
z.moveStart("character",d);
if(b>0){z.moveEnd("character",b)
}z.select()
}catch(e){}}}l.nodeChanged()
}}if(g.forced_root_block){l.onKeyUp.add(i);
l.onNodeChange.add(i)
}};
(function(j){var f=j.DOM,g=j.dom.Event,i=j.each,h=j.extend;
j.create("tinymce.ControlManager",{ControlManager:function(d,a){var b=this,c;
a=a||{};
b.editor=d;
b.controls={};
b.onAdd=new j.util.Dispatcher(b);
b.onPostRender=new j.util.Dispatcher(b);
b.prefix=a.prefix||d.id+"_";
b._cls={};
b.onPostRender.add(function(){i(b.controls,function(e){e.postRender()
})
})
},get:function(a){return this.controls[this.prefix+a]||this.controls[a]
},setActive:function(a,c){var b=null;
if(b=this.get(a)){b.setActive(c)
}return b
},setDisabled:function(a,c){var b=null;
if(b=this.get(a)){b.setDisabled(c)
}return b
},add:function(a){var b=this;
if(a){b.controls[a.id]=a;
b.onAdd.dispatch(a,b)
}return a
},createControl:function(c){var p,b,e,d=this,a=d.editor,q,l;
if(!d.controlFactories){d.controlFactories=[];
i(a.plugins,function(k){if(k.createControl){d.controlFactories.push(k)
}})
}q=d.controlFactories;
for(b=0,e=q.length;
b<e;
b++){p=q[b].createControl(c,d);
if(p){return d.add(p)
}}if(c==="|"||c==="separator"){return d.createSeparator()
}if(a.buttons&&(p=a.buttons[c])){return d.createButton(c,p)
}return d.add(p)
},createDropMenu:function(r,a,p){var b=this,o=b.editor,e,q,d,c;
a=h({"class":"mceDropDown",constrain:o.settings.constrain_menus},a);
a["class"]=a["class"]+" "+o.getParam("skin")+"Skin";
if(d=o.getParam("skin_variant")){a["class"]+=" "+o.getParam("skin")+"Skin"+d.substring(0,1).toUpperCase()+d.substring(1)
}a["class"]+=o.settings.directionality=="rtl"?" mceRtl":"";
r=b.prefix+r;
c=p||b._cls.dropmenu||j.ui.DropMenu;
e=b.controls[r]=new c(r,a);
e.onAddItem.add(function(k,l){var m=l.settings;
m.title=o.getLang(m.title,m.title);
if(!m.onclick){m.onclick=function(n){if(m.cmd){o.execCommand(m.cmd,m.ui||false,m.value)
}}
}});
o.onRemove.add(function(){e.destroy()
});
if(j.isIE){e.onShowMenu.add(function(){o.focus();
q=o.selection.getBookmark(1)
});
e.onHideMenu.add(function(){if(q){o.selection.moveToBookmark(q);
q=0
}})
}return b.add(e)
},createListBox:function(r,a,p){var c=this,e=c.editor,o,d,b;
if(c.get(r)){return null
}a.title=e.translate(a.title);
a.scope=a.scope||e;
if(!a.onselect){a.onselect=function(k){e.execCommand(a.cmd,a.ui||false,k||a.value)
}
}a=h({title:a.title,"class":"mce_"+r,scope:a.scope,control_manager:c},a);
r=c.prefix+r;
function q(k){return k.settings.use_accessible_selects&&!j.isGecko
}if(e.settings.use_native_selects||q(e)){d=new j.ui.NativeListBox(r,a)
}else{b=p||c._cls.listbox||j.ui.ListBox;
d=new b(r,a,e)
}c.controls[r]=d;
if(j.isWebKit){d.onPostRender.add(function(k,l){g.add(l,"mousedown",function(){e.bookmark=e.selection.getBookmark(1)
});
g.add(l,"focus",function(){e.selection.moveToBookmark(e.bookmark);
e.bookmark=null
})
})
}if(d.hideMenu){e.onMouseDown.add(d.hideMenu,d)
}return c.add(d)
},createButton:function(a,e,b){var n=this,o=n.editor,d,c,p;
if(n.get(a)){return null
}e.title=o.translate(e.title);
e.label=o.translate(e.label);
e.scope=e.scope||o;
if(!e.onclick&&!e.menu_button){e.onclick=function(){o.execCommand(e.cmd,e.ui||false,e.value)
}
}e=h({title:e.title,"class":"mce_"+a,unavailable_prefix:o.getLang("unavailable",""),scope:e.scope,control_manager:n},e);
a=n.prefix+a;
if(e.menu_button){p=b||n._cls.menubutton||j.ui.MenuButton;
c=new p(a,e,o);
o.onMouseDown.add(c.hideMenu,c)
}else{p=n._cls.button||j.ui.Button;
c=new p(a,e,o)
}return n.add(c)
},createMenuButton:function(a,c,b){c=c||{};
c.menu_button=1;
return this.createButton(a,c,b)
},createSplitButton:function(a,e,b){var n=this,o=n.editor,d,c,p;
if(n.get(a)){return null
}e.title=o.translate(e.title);
e.scope=e.scope||o;
if(!e.onclick){e.onclick=function(k){o.execCommand(e.cmd,e.ui||false,k||e.value)
}
}if(!e.onselect){e.onselect=function(k){o.execCommand(e.cmd,e.ui||false,k||e.value)
}
}e=h({title:e.title,"class":"mce_"+a,scope:e.scope,control_manager:n},e);
a=n.prefix+a;
p=b||n._cls.splitbutton||j.ui.SplitButton;
c=n.add(new p(a,e,o));
o.onMouseDown.add(c.hideMenu,c);
return c
},createColorSplitButton:function(r,a,p){var c=this,e=c.editor,o,d,b,q;
if(c.get(r)){return null
}a.title=e.translate(a.title);
a.scope=a.scope||e;
if(!a.onclick){a.onclick=function(k){if(j.isIE){q=e.selection.getBookmark(1)
}e.execCommand(a.cmd,a.ui||false,k||a.value)
}
}if(!a.onselect){a.onselect=function(k){e.execCommand(a.cmd,a.ui||false,k||a.value)
}
}a=h({title:a.title,"class":"mce_"+r,menu_class:e.getParam("skin")+"Skin",scope:a.scope,more_colors_title:e.getLang("more_colors")},a);
r=c.prefix+r;
b=p||c._cls.colorsplitbutton||j.ui.ColorSplitButton;
d=new b(r,a,e);
e.onMouseDown.add(d.hideMenu,d);
e.onRemove.add(function(){d.destroy()
});
if(j.isIE){d.onShowMenu.add(function(){e.focus();
q=e.selection.getBookmark(1)
});
d.onHideMenu.add(function(){if(q){e.selection.moveToBookmark(q);
q=0
}})
}return c.add(d)
},createToolbar:function(a,d,b){var c,e=this,l;
a=e.prefix+a;
l=b||e._cls.toolbar||j.ui.Toolbar;
c=new l(a,d,e.editor);
if(e.get(a)){return null
}return e.add(c)
},createToolbarGroup:function(a,d,b){var c,e=this,l;
a=e.prefix+a;
l=b||this._cls.toolbarGroup||j.ui.ToolbarGroup;
c=new l(a,d,e.editor);
if(e.get(a)){return null
}return e.add(c)
},createSeparator:function(a){var b=a||this._cls.separator||j.ui.Separator;
return new b()
},setControlType:function(a,b){return this._cls[a.toLowerCase()]=b
},destroy:function(){i(this.controls,function(a){a.destroy()
});
this.controls=null
}})
})(tinymce);
(function(i){var g=i.util.Dispatcher,h=i.each,j=i.isIE,f=i.isOpera;
i.create("tinymce.WindowManager",{WindowManager:function(b){var a=this;
a.editor=b;
a.onOpen=new g(a);
a.onClose=new g(a);
a.params={};
a.features={}
},open:function(A,x){var B=this,s="",d,e,u=B.editor.settings.dialog_type=="modal",b,c,t,y=i.DOM.getViewPort(),a;
A=A||{};
x=x||{};
c=f?y.w:screen.width;
t=f?y.h:screen.height;
A.name=A.name||"mc_"+new Date().getTime();
A.width=parseInt(A.width||320);
A.height=parseInt(A.height||240);
A.resizable=true;
A.left=A.left||parseInt(c/2)-(A.width/2);
A.top=A.top||parseInt(t/2)-(A.height/2);
x.inline=false;
x.mce_width=A.width;
x.mce_height=A.height;
x.mce_auto_focus=A.auto_focus;
if(u){if(j){A.center=true;
A.help=false;
A.dialogWidth=A.width+"px";
A.dialogHeight=A.height+"px";
A.scroll=A.scrollbars||false
}}h(A,function(l,k){if(i.is(l,"boolean")){l=l?"yes":"no"
}if(!/^(name|url)$/.test(k)){if(j&&u){s+=(s?";":"")+k+":"+l
}else{s+=(s?",":"")+k+"="+l
}}});
B.features=A;
B.params=x;
B.onOpen.dispatch(B,A,x);
a=A.url||A.file;
a=i._addVer(a);
try{if(j&&u){b=1;
window.showModalDialog(a,window,s)
}else{b=window.open(a,A.name,s)
}}catch(p){}if(!b){alert(B.editor.getLang("popup_blocked"))
}},close:function(a){a.close();
this.onClose.dispatch(this)
},createInstance:function(e,n,o,a,b,c){var d=i.resolve(e);
return new d(n,o,a,b,c)
},confirm:function(b,d,a,c){c=c||window;
d.call(a||this,c.confirm(this._decode(this.editor.getLang(b,b))))
},alert:function(c,e,a,d){var b=this;
d=d||window;
d.alert(b._decode(b.editor.getLang(c,c)));
if(e){e.call(a||b)
}},resizeBy:function(c,b,a){a.resizeBy(c,b)
},_decode:function(a){return i.DOM.decode(a).replace(/\\n/g,"\n")
}})
}(tinymce));
(function(b){b.Formatter=function(aB){var aj={},ag=b.each,aY=aB.dom,aJ=aB.selection,aH=b.dom.TreeWalker,am=new b.dom.RangeUtils(aY),aX=aB.schema.isValidChild,aA=b.isArray,at=aY.isBlock,aO=aB.settings.forced_root_block,aI=aY.nodeIndex,au="\uFEFF",aW=/^(src|href|style)$/,ac=false,ay=true,ak,ax,aE=aY.getContentEditable;
function ar(c){if(c.nodeType){c=c.nodeName
}return !!aB.schema.getTextBlockElements()[c.toLowerCase()]
}function aN(c,d){return aY.getParents(c,d,aY.getRoot())
}function aZ(c){return c.nodeType===1&&c.id==="_mce_caret"
}function aR(){aP({alignleft:[{selector:"figure,p,h1,h2,h3,h4,h5,h6,td,th,div,ul,ol,li",styles:{textAlign:"left"},defaultBlock:"div"},{selector:"img,table",collapsed:false,styles:{"float":"left"}}],aligncenter:[{selector:"figure,p,h1,h2,h3,h4,h5,h6,td,th,div,ul,ol,li",styles:{textAlign:"center"},defaultBlock:"div"},{selector:"img",collapsed:false,styles:{display:"block",marginLeft:"auto",marginRight:"auto"}},{selector:"table",collapsed:false,styles:{marginLeft:"auto",marginRight:"auto"}}],alignright:[{selector:"figure,p,h1,h2,h3,h4,h5,h6,td,th,div,ul,ol,li",styles:{textAlign:"right"},defaultBlock:"div"},{selector:"img,table",collapsed:false,styles:{"float":"right"}}],alignfull:[{selector:"figure,p,h1,h2,h3,h4,h5,h6,td,th,div,ul,ol,li",styles:{textAlign:"justify"},defaultBlock:"div"}],bold:[{inline:"strong",remove:"all"},{inline:"span",styles:{fontWeight:"bold"}},{inline:"b",remove:"all"}],italic:[{inline:"em",remove:"all"},{inline:"span",styles:{fontStyle:"italic"}},{inline:"i",remove:"all"}],underline:[{inline:"span",styles:{textDecoration:"underline"},exact:true},{inline:"u",remove:"all"}],strikethrough:[{inline:"span",styles:{textDecoration:"line-through"},exact:true},{inline:"strike",remove:"all"}],forecolor:{inline:"span",styles:{color:"%value"},wrap_links:false},hilitecolor:{inline:"span",styles:{backgroundColor:"%value"},wrap_links:false},fontname:{inline:"span",styles:{fontFamily:"%value"}},fontsize:{inline:"span",styles:{fontSize:"%value"}},fontsize_class:{inline:"span",attributes:{"class":"%value"}},blockquote:{block:"blockquote",wrapper:1,remove:"all"},subscript:{inline:"sub"},superscript:{inline:"sup"},link:{inline:"a",selector:"a",remove:"all",split:true,deep:true,onmatch:function(c){return true
},onformat:function(c,e,d){ag(d,function(f,g){aY.setAttrib(c,g,f)
})
}},removeformat:[{selector:"b,strong,em,i,font,u,strike",remove:"all",split:true,expand:false,block_expand:true,deep:true},{selector:"span",attributes:["style","class"],remove:"empty",split:true,expand:false,deep:true},{selector:"*",attributes:["style","class"],split:false,expand:false,deep:true}]});
ag("p h1 h2 h3 h4 h5 h6 div address pre div code dt dd samp".split(/\s/),function(c){aP(c,{block:c,remove:"all"})
});
aP(aB.settings.formats)
}function ad(){aB.addShortcut("ctrl+b","bold_desc","Bold");
aB.addShortcut("ctrl+i","italic_desc","Italic");
aB.addShortcut("ctrl+u","underline_desc","Underline");
for(var c=1;
c<=6;
c++){aB.addShortcut("ctrl+"+c,"",["FormatBlock",false,"h"+c])
}aB.addShortcut("ctrl+7","",["FormatBlock",false,"p"]);
aB.addShortcut("ctrl+8","",["FormatBlock",false,"div"]);
aB.addShortcut("ctrl+9","",["FormatBlock",false,"address"])
}function ae(c){return c?aj[c]:aj
}function aP(d,c){if(d){if(typeof(d)!=="string"){ag(d,function(e,f){aP(f,e)
})
}else{c=c.length?c:[c];
ag(c,function(e){if(e.deep===ax){e.deep=!e.selector
}if(e.split===ax){e.split=!e.selector||e.inline
}if(e.remove===ax&&e.selector&&!e.inline){e.remove="none"
}if(e.selector&&e.inline){e.mixed=true;
e.block_expand=true
}if(typeof(e.classes)==="string"){e.classes=e.classes.split(/\s+/)
}});
aj[d]=c
}}}var aS=function(c){var d;
aB.dom.getParent(c,function(e){d=aB.dom.getStyle(e,"text-decoration");
return d&&d!=="none"
});
return d
};
var ao=function(d){var c;
if(d.nodeType===1&&d.parentNode&&d.parentNode.nodeType===1){c=aS(d.parentNode);
if(aB.dom.getStyle(d,"color")&&c){aB.dom.setStyle(d,"text-decoration",c)
}else{if(aB.dom.getStyle(d,"textdecoration")===c){aB.dom.setStyle(d,"text-decoration",null)
}}}};
function ab(m,f,k){var j=ae(m),e=j[0],g,o,h,i=aJ.isCollapsed();
function p(q,r){r=r||e;
if(q){if(r.onformat){r.onformat(q,r,f,k)
}ag(r.styles,function(s,t){aY.setStyle(q,t,aK(s,f))
});
ag(r.attributes,function(s,t){aY.setAttrib(q,t,aK(s,f))
});
ag(r.classes,function(s){s=aK(s,f);
if(!aY.hasClass(q,s)){aY.addClass(q,s)
}})
}}function l(){function r(x,z){var y=new aH(z);
for(k=y.current();
k;
k=y.prev()){if(k.childNodes.length>1||k==x||k.tagName=="BR"){return k
}}}var s=aB.selection.getRng();
var u=s.startContainer;
var t=s.endContainer;
if(u!=t&&s.endOffset===0){var v=r(u,t);
var q=v.nodeType==3?v.length:v.childNodes.length;
s.setEnd(v,q)
}return s
}function n(u,B,r,s,x){var y=[],v=-1,q,z=-1,t=-1,A;
ag(u.childNodes,function(C,D){if(C.nodeName==="UL"||C.nodeName==="OL"){v=D;
q=C;
return false
}});
ag(u.childNodes,function(C,D){if(C.nodeName==="SPAN"&&aY.getAttrib(C,"data-mce-type")=="bookmark"){if(C.id==B.id+"_start"){z=D
}else{if(C.id==B.id+"_end"){t=D
}}}});
if(v<=0||(z<v&&t>v)){ag(b.grep(u.childNodes),x);
return 0
}else{A=aY.clone(r,ac);
ag(b.grep(u.childNodes),function(C,D){if((z<v&&D<v)||(z>v&&D>v)){y.push(C);
C.parentNode.removeChild(C)
}});
if(z<v){u.insertBefore(A,q)
}else{if(z>v){u.insertBefore(A,q.nextSibling)
}}s.push(A);
ag(y,function(C){A.appendChild(C)
});
return A
}}function d(s,q,u){var t=[],v,r,x=true;
v=e.inline||e.block;
r=aY.create(v);
p(r);
am.walk(s,function(A){var z;
function y(G){var B,D,F,E,C;
C=x;
B=G.nodeName.toLowerCase();
D=G.parentNode.nodeName.toLowerCase();
if(G.nodeType===1&&aE(G)){C=x;
x=aE(G)==="true";
E=true
}if(aU(B,"br")){z=0;
if(e.block){aY.remove(G)
}return
}if(e.wrapper&&aD(G,m,f)){z=0;
return
}if(x&&!E&&e.block&&!e.wrapper&&ar(B)){G=aY.rename(G,v);
p(G);
t.push(G);
z=0;
return
}if(e.selector){ag(j,function(H){if("collapsed" in H&&H.collapsed!==i){return
}if(aY.is(G,H.selector)&&!aZ(G)){p(G,H);
F=true
}});
if(!e.inline||F){z=0;
return
}}if(x&&!E&&aX(v,B)&&aX(D,v)&&!(!u&&G.nodeType===3&&G.nodeValue.length===1&&G.nodeValue.charCodeAt(0)===65279)&&!aZ(G)&&(!e.inline||!at(G))){if(!z){z=aY.clone(r,ac);
G.parentNode.insertBefore(z,G);
t.push(z)
}z.appendChild(G)
}else{if(B=="li"&&q){z=n(G,q,r,t,y)
}else{z=0;
ag(b.grep(G.childNodes),y);
if(E){x=C
}z=0
}}}ag(A,y)
});
if(e.wrap_links===false){ag(t,function(z){function y(B){var C,D,A;
if(B.nodeName==="A"){D=aY.clone(r,ac);
t.push(D);
A=b.grep(B.childNodes);
for(C=0;
C<A.length;
C++){D.appendChild(A[C])
}B.appendChild(D)
}ag(b.grep(B.childNodes),y)
}y(z)
})
}ag(t,function(y){var B;
function A(C){var D=0;
ag(C.childNodes,function(E){if(!aV(E)&&!ap(E)){D++
}});
return D
}function z(E){var C,D;
ag(E.childNodes,function(F){if(F.nodeType==1&&!ap(F)&&!aZ(F)){C=F;
return ac
}});
if(C&&aT(C,e)){D=aY.clone(C,ac);
p(D);
aY.replace(D,E,ay);
aY.remove(C,1)
}return D||E
}B=A(y);
if((t.length>1||!at(y))&&B===0){aY.remove(y,1);
return
}if(e.inline||e.wrapper){if(!e.exact&&B===1){y=z(y)
}ag(j,function(C){ag(aY.select(C.inline,y),function(D){var E;
if(C.wrap_links===false){E=D.parentNode;
do{if(E.nodeName==="A"){return
}}while(E=E.parentNode)
}a(C,f,D,C.exact?D:null)
})
});
if(aD(y.parentNode,m,f)){aY.remove(y,1);
y=0;
return ay
}if(e.merge_with_parents){aY.getParent(y.parentNode,function(C){if(aD(C,m,f)){aY.remove(y,1);
y=0;
return ay
}})
}if(y&&e.merge_siblings!==false){y=aG(aw(y),y);
y=aG(y,aw(y,ay))
}}})
}if(e){if(k){if(k.nodeType){o=aY.createRng();
o.setStartBefore(k);
o.setEndAfter(k);
d(aL(o,j),null,true)
}else{d(k,null,true)
}}else{if(!i||!e.inline||aY.select("td.mceSelected,th.mceSelected").length){var c=aB.selection.getNode();
if(!aO&&j[0].defaultBlock&&!aY.getParent(c,aY.isBlock)){ab(j[0].defaultBlock)
}aB.selection.setRng(l());
g=aJ.getBookmark();
d(aL(aJ.getRng(ay),j),g);
if(e.styles&&(e.styles.color||e.styles.textDecoration)){b.walk(c,ao,"childNodes");
ao(c)
}aJ.moveToBookmark(g);
ai(aJ.getRng(ay));
aB.nodeChanged()
}else{af("apply",m,f)
}}}}function az(o,f,m){var l=ae(o),d=l[0],h,i,p,g=true;
function n(x){var y,r,s,t,u,v;
if(x.nodeType===3){return
}if(x.nodeType===1&&aE(x)){u=g;
g=aE(x)==="true";
v=true
}y=b.grep(x.childNodes);
if(g&&!v){for(r=0,s=l.length;
r<s;
r++){if(a(l[r],f,x,x)){break
}}}if(d.deep){if(y.length){for(r=0,s=y.length;
r<s;
r++){n(y[r])
}if(v){g=u
}}}}function k(s){var r;
ag(aN(s.parentNode).reverse(),function(t){var u;
if(!r&&t.id!="_start"&&t.id!="_end"){u=aD(t,o,f);
if(u&&u.split!==false){r=t
}}});
return r
}function q(u,y,s,A){var z,B,r,v,t,x;
if(u){x=u.parentNode;
for(z=y.parentNode;
z&&z!=x;
z=z.parentNode){B=aY.clone(z,ac);
for(t=0;
t<l.length;
t++){if(a(l[t],f,B,B)){B=0;
break
}}if(B){if(r){B.appendChild(r)
}if(!v){v=B
}r=B
}}if(A&&(!d.mixed||!at(u))){y=aY.split(u,y)
}if(r){s.parentNode.insertBefore(r,s);
v.appendChild(s)
}}return y
}function e(r){return q(k(r),r,r,true)
}function j(r){var s=aY.get(r?"_start":"_end"),t=s[r?"firstChild":"lastChild"];
if(ap(t)){t=t[r?"firstChild":"lastChild"]
}aY.remove(s,true);
return t
}function c(t){var r,u,s;
t=aL(t,l,ay);
if(d.split){r=an(t,ay);
u=an(t);
if(r!=u){if(/^(TR|TD)$/.test(r.nodeName)&&r.firstChild){r=(r.nodeName=="TD"?r.firstChild:r.firstChild.firstChild)||r
}r=ah(r,"span",{id:"_start","data-mce-type":"bookmark"});
u=ah(u,"span",{id:"_end","data-mce-type":"bookmark"});
e(r);
e(u);
r=j(ay);
u=j()
}else{r=u=e(r)
}t.startContainer=r.parentNode;
t.startOffset=aI(r);
t.endContainer=u.parentNode;
t.endOffset=aI(u)+1
}am.walk(t,function(v){ag(v,function(x){n(x);
if(x.nodeType===1&&aB.dom.getStyle(x,"text-decoration")==="underline"&&x.parentNode&&aS(x.parentNode)==="underline"){a({deep:false,exact:true,inline:"span",styles:{textDecoration:"underline"}},null,x)
}})
})
}if(m){if(m.nodeType){p=aY.createRng();
p.setStartBefore(m);
p.setEndAfter(m);
c(p)
}else{c(m)
}return
}if(!aJ.isCollapsed()||!d.inline||aY.select("td.mceSelected,th.mceSelected").length){h=aJ.getBookmark();
c(aJ.getRng(ay));
aJ.moveToBookmark(h);
if(d.inline&&aQ(o,f,aJ.getStart())){ai(aJ.getRng(true))
}aB.nodeChanged()
}else{af("remove",o,f)
}}function av(e,c,d){var f=ae(e);
if(aQ(e,c,d)&&(!("toggle" in f[0])||f[0].toggle)){az(e,c,d)
}else{ab(e,c,d)
}}function aD(j,k,e,g){var i=ae(k),d,f,h;
function c(o,m,l){var p,n,r=m[l],q;
if(m.onmatch){return m.onmatch(o,m,l)
}if(r){if(r.length===ax){for(p in r){if(r.hasOwnProperty(p)){if(l==="attributes"){n=aY.getAttrib(o,p)
}else{n=al(o,p)
}if(g&&!n&&!m.exact){return
}if((!g||m.exact)&&!aU(n,aK(r[p],e))){return
}}}}else{for(q=0;
q<r.length;
q++){if(l==="attributes"?aY.getAttrib(o,r[q]):al(o,r[q])){return m
}}}}return m
}if(i&&j){for(f=0;
f<i.length;
f++){d=i[f];
if(aT(j,d)&&c(j,d,"attributes")&&c(j,d,"styles")){if(h=d.classes){for(f=0;
f<h.length;
f++){if(!aY.hasClass(j,h[f])){return
}}}return d
}}}}function aQ(e,c,d){var f;
function g(h){h=aY.getParent(h,function(i){return !!aD(i,e,c,true)
});
return aD(h,e,c)
}if(d){return g(d)
}d=aJ.getNode();
if(g(d)){return ay
}f=aJ.getStart();
if(f!=d){if(g(f)){return ay
}}return ac
}function aF(c,d){var f,e=[],g={},h,i,j;
f=aJ.getStart();
aY.getParent(f,function(k){var l,m;
for(l=0;
l<c.length;
l++){m=c[l];
if(!g[m]&&aD(k,m,d)){g[m]=true;
e.push(m)
}}},aY.getRoot());
return e
}function aC(e){var c=ae(e),f,g,d,h,i;
if(c){f=aJ.getStart();
g=aN(f);
for(h=c.length-1;
h>=0;
h--){i=c[h].selector;
if(!i){return ay
}for(d=g.length-1;
d>=0;
d--){if(aY.is(g[d],i)){return ay
}}}}return ac
}function aq(f,c,e){var d;
if(!ak){ak={};
d={};
aB.onNodeChange.addToTop(function(i,j,g){var h=aN(g),k={};
ag(ak,function(m,l){ag(h,function(n){if(aD(n,l,{},m.similar)){if(!d[l]){ag(m,function(o){o(true,{node:n,format:l,parents:h})
});
d[l]=m
}k[l]=m;
return false
}})
});
ag(d,function(m,l){if(!k[l]){delete d[l];
ag(m,function(n){n(false,{node:g,format:l,parents:h})
})
}})
})
}ag(f.split(","),function(g){if(!ak[g]){ak[g]=[];
ak[g].similar=e
}ak[g].push(c)
});
return this
}b.extend(this,{get:ae,register:aP,apply:ab,remove:az,toggle:av,match:aQ,matchAll:aF,matchNode:aD,canApply:aC,formatChanged:aq});
aR();
ad();
function aT(d,c){if(aU(d,c.inline)){return ay
}if(aU(d,c.block)){return ay
}if(c.selector){return aY.is(d,c.selector)
}}function aU(c,d){c=c||"";
d=d||"";
c=""+(c.nodeName||c);
d=""+(d.nodeName||d);
return c.toLowerCase()==d.toLowerCase()
}function al(d,e){var c=aY.getStyle(d,e);
if(e=="color"||e=="backgroundColor"){c=aY.toHex(c)
}if(e=="fontWeight"&&c==700){c="bold"
}return""+c
}function aK(d,c){if(typeof(d)!="string"){d=d(c)
}else{if(c){d=d.replace(/%(\w+)/g,function(e,f){return c[f]||e
})
}}return d
}function aV(c){return c&&c.nodeType===3&&/^([\t \r\n]+|)$/.test(c.nodeValue)
}function ah(d,e,f){var c=aY.create(e,f);
d.parentNode.insertBefore(c,d);
c.appendChild(d);
return c
}function aL(s,h,p){var e,g,m,i,q=s.startContainer,l=s.startOffset,c=s.endContainer,j=s.endOffset;
function f(y){var B,x,u,z,A,t;
B=x=y?q:c;
A=y?"previousSibling":"nextSibling";
t=aY.getRoot();
function v(C){return C.nodeName=="BR"&&C.getAttribute("data-mce-bogus")&&!C.nextSibling
}if(B.nodeType==3&&!aV(B)){if(y?l>0:j<B.nodeValue.length){return B
}}for(;
;
){if(!h[0].block_expand&&at(x)){return x
}for(z=x[A];
z;
z=z[A]){if(!ap(z)&&!aV(z)&&!v(z)){return x
}}if(x.parentNode==t){B=x;
break
}x=x.parentNode
}return B
}function n(t,u){if(u===ax){u=t.nodeType===3?t.length:t.childNodes.length
}while(t&&t.hasChildNodes()){t=t.childNodes[u];
if(t){u=t.nodeType===3?t.length:t.childNodes.length
}}return{node:t,offset:u}
}if(q.nodeType==1&&q.hasChildNodes()){g=q.childNodes.length-1;
q=q.childNodes[l>g?g:l];
if(q.nodeType==3){l=0
}}if(c.nodeType==1&&c.hasChildNodes()){g=c.childNodes.length-1;
c=c.childNodes[j>g?g:j-1];
if(c.nodeType==3){j=c.nodeValue.length
}}function d(u){var t=u;
while(t){if(t.nodeType===1&&aE(t)){return aE(t)==="false"?t:u
}t=t.parentNode
}return u
}function k(B,v,y){var x,A,u,t;
function z(F,D){var C,G,E=F.nodeValue;
if(typeof(D)=="undefined"){D=y?E.length:0
}if(y){C=E.lastIndexOf(" ",D);
G=E.lastIndexOf("\u00a0",D);
C=C>G?C:G;
if(C!==-1&&!p){C++
}}else{C=E.indexOf(" ",D);
G=E.indexOf("\u00a0",D);
C=C!==-1&&(G===-1||C<G)?C:G
}return C
}if(B.nodeType===3){u=z(B,v);
if(u!==-1){return{container:B,offset:u}
}t=B
}x=new aH(B,aY.getParent(B,at)||aB.getBody());
while(A=x[y?"prev":"next"]()){if(A.nodeType===3){t=A;
u=z(A);
if(u!==-1){return{container:A,offset:u}
}}else{if(at(A)){break
}}}if(t){if(y){v=0
}else{v=t.length
}return{container:t,offset:v}
}}function o(z,t){var y,x,u,v;
if(z.nodeType==3&&z.nodeValue.length===0&&z[t]){z=z[t]
}y=aN(z);
for(x=0;
x<y.length;
x++){for(u=0;
u<h.length;
u++){v=h[u];
if("collapsed" in v&&v.collapsed!==s.collapsed){continue
}if(aY.is(y[x],v.selector)){return y[x]
}}}return z
}function r(x,t,u){var v;
if(!h[0].wrapper){v=aY.getParent(x,h[0].block)
}if(!v){v=aY.getParent(x.nodeType==3?x.parentNode:x,ar)
}if(v&&h[0].wrapper){v=aN(v,"ul,ol").reverse()[0]||v
}if(!v){v=x;
while(v[t]&&!at(v[t])){v=v[t];
if(aU(v,"br")){break
}}}return v||x
}q=d(q);
c=d(c);
if(ap(q.parentNode)||ap(q)){q=ap(q)?q:q.parentNode;
q=q.nextSibling||q;
if(q.nodeType==3){l=0
}}if(ap(c.parentNode)||ap(c)){c=ap(c)?c:c.parentNode;
c=c.previousSibling||c;
if(c.nodeType==3){j=c.length
}}if(h[0].inline){if(s.collapsed){i=k(q,l,true);
if(i){q=i.container;
l=i.offset
}i=k(c,j);
if(i){c=i.container;
j=i.offset
}}m=n(c,j);
if(m.node){while(m.node&&m.offset===0&&m.node.previousSibling){m=n(m.node.previousSibling)
}if(m.node&&m.offset>0&&m.node.nodeType===3&&m.node.nodeValue.charAt(m.offset-1)===" "){if(m.offset>1){c=m.node;
c.splitText(m.offset-1)
}}}}if(h[0].inline||h[0].block_expand){if(!h[0].inline||(q.nodeType!=3||l===0)){q=f(true)
}if(!h[0].inline||(c.nodeType!=3||j===c.nodeValue.length)){c=f()
}}if(h[0].selector&&h[0].expand!==ac&&!h[0].inline){q=o(q,"previousSibling");
c=o(c,"nextSibling")
}if(h[0].block||h[0].selector){q=r(q,"previousSibling");
c=r(c,"nextSibling");
if(h[0].block){if(!at(q)){q=f(true)
}if(!at(c)){c=f()
}}}if(q.nodeType==1){l=aI(q);
q=q.parentNode
}if(c.nodeType==1){j=aI(c)+1;
c=c.parentNode
}return{startContainer:q,startOffset:l,endContainer:c,endOffset:j}
}function a(c,d,f,i){var g,h,e;
if(!aT(f,c)){return ac
}if(c.remove!="all"){ag(c.styles,function(k,j){k=aK(k,d);
if(typeof(j)==="number"){j=k;
i=0
}if(!i||aU(al(i,j),k)){aY.setStyle(f,j,"")
}e=1
});
if(e&&aY.getAttrib(f,"style")==""){f.removeAttribute("style");
f.removeAttribute("data-mce-style")
}ag(c.attributes,function(k,j){var l;
k=aK(k,d);
if(typeof(j)==="number"){j=k;
i=0
}if(!i||aU(aY.getAttrib(i,j),k)){if(j=="class"){k=aY.getAttrib(f,j);
if(k){l="";
ag(k.split(/\s+/),function(m){if(/mce\w+/.test(m)){l+=(l?" ":"")+m
}});
if(l){aY.setAttrib(f,j,l);
return
}}}if(j=="class"){f.removeAttribute("className")
}if(aW.test(j)){f.removeAttribute("data-mce-"+j)
}f.removeAttribute(j)
}});
ag(c.classes,function(j){j=aK(j,d);
if(!i||aY.hasClass(i,j)){aY.removeClass(f,j)
}});
h=aY.getAttribs(f);
for(g=0;
g<h.length;
g++){if(h[g].nodeName.indexOf("_")!==0){return ac
}}}if(c.remove!="none"){aM(f,c);
return ay
}}function aM(e,d){var g=e.parentNode,f;
function c(i,j,h){i=aw(i,j,h);
return !i||(i.nodeName=="BR"||at(i))
}if(d.block){if(!aO){if(at(e)&&!at(g)){if(!c(e,ac)&&!c(e.firstChild,ay,1)){e.insertBefore(aY.create("br"),e.firstChild)
}if(!c(e,ay)&&!c(e.lastChild,ac,1)){e.appendChild(aY.create("br"))
}}}else{if(g==aY.getRoot()){if(!d.list_block||!aU(e,d.list_block)){ag(b.grep(e.childNodes),function(h){if(aX(aO,h.nodeName.toLowerCase())){if(!f){f=ah(h,aO)
}else{f.appendChild(h)
}}else{f=0
}})
}}}}if(d.selector&&d.inline&&!aU(d.inline,e)){return
}aY.remove(e,1)
}function aw(d,e,c){if(d){e=e?"nextSibling":"previousSibling";
for(d=c?d:d[e];
d;
d=d[e]){if(d.nodeType==1||!aV(d)){return d
}}}}function ap(c){return c&&c.nodeType==1&&c.getAttribute("data-mce-type")=="bookmark"
}function aG(e,f){var i,g,h;
function c(l,m){if(l.nodeName!=m.nodeName){return ac
}function j(o){var n={};
ag(aY.getAttribs(o),function(q){var p=q.nodeName.toLowerCase();
if(p.indexOf("_")!==0&&p!=="style"){n[p]=aY.getAttrib(o,p)
}});
return n
}function k(n,o){var p,q;
for(q in n){if(n.hasOwnProperty(q)){p=o[q];
if(p===ax){return ac
}if(n[q]!=p){return ac
}delete o[q]
}}for(q in o){if(o.hasOwnProperty(q)){return ac
}}return ay
}if(!k(j(l),j(m))){return ac
}if(!k(aY.parseStyle(aY.getAttrib(l,"style")),aY.parseStyle(aY.getAttrib(m,"style")))){return ac
}return ay
}function d(k,j){for(g=k;
g;
g=g[j]){if(g.nodeType==3&&g.nodeValue.length!==0){return k
}if(g.nodeType==1&&!ap(g)){return g
}}return k
}if(e&&f){e=d(e,"previousSibling");
f=d(f,"nextSibling");
if(c(e,f)){for(g=e.nextSibling;
g&&g!=f;
){h=g;
g=g.nextSibling;
e.appendChild(h)
}aY.remove(f);
ag(b.grep(f.childNodes),function(j){e.appendChild(j)
});
return e
}}return f
}function an(g,c){var h,d,f,e;
h=g[c?"startContainer":"endContainer"];
d=g[c?"startOffset":"endOffset"];
if(h.nodeType==1){f=h.childNodes.length-1;
if(!c&&d){d--
}h=h.childNodes[d>f?f:d]
}if(h.nodeType===3&&c&&d>=h.nodeValue.length){h=new aH(h,aB.getBody()).next()||h
}if(h.nodeType===3&&!c&&d===0){h=new aH(h,aB.getBody()).prev()||h
}return h
}function af(f,o,h){var e="_mce_caret",n=aB.settings.caret_debug;
function m(p){var q=aY.create("span",{id:e,"data-mce-bogus":true,style:n?"color:red":""});
if(p){q.appendChild(aB.getDoc().createTextNode(au))
}return q
}function g(p,q){while(p){if((p.nodeType===3&&p.nodeValue!==au)||p.childNodes.length>1){return false
}if(q&&p.nodeType===1){q.push(p)
}p=p.firstChild
}return true
}function j(p){while(p){if(p.id===e){return p
}p=p.parentNode
}}function k(q){var p;
if(q){p=new aH(q,q);
for(q=p.current();
q;
q=p.next()){if(q.nodeType===3){return q
}}}}function l(q,r){var p,s;
if(!q){q=j(aJ.getStart());
if(!q){while(q=aY.get(e)){l(q,false)
}}}else{s=aJ.getRng(true);
if(g(q)){if(r!==false){s.setStartBefore(q);
s.setEndBefore(q)
}aY.remove(q)
}else{p=k(q);
if(p.nodeValue.charAt(0)===au){p=p.deleteData(0,1)
}aY.remove(q,1)
}aJ.setRng(s)
}}function i(){var r,t,u,v,q,s,p;
r=aJ.getRng(true);
v=r.startOffset;
s=r.startContainer;
p=s.nodeValue;
t=j(aJ.getStart());
if(t){u=k(t)
}if(p&&v>0&&v<p.length&&/\w/.test(p.charAt(v))&&/\w/.test(p.charAt(v-1))){q=aJ.getBookmark();
r.collapse(true);
r=aL(r,ae(o));
r=am.split(r);
ab(o,h,r);
aJ.moveToBookmark(q)
}else{if(!t||u.nodeValue!==au){t=m(true);
u=t.firstChild;
r.insertNode(t);
v=1;
ab(o,h,t)
}else{ab(o,h,t)
}aJ.setCursorLocation(u,v)
}}function d(){var y=aJ.getRng(true),x,t,q,r,v,z,A=[],s,p;
x=y.startContainer;
t=y.startOffset;
v=x;
if(x.nodeType==3){if(t!=x.nodeValue.length||x.nodeValue===au){r=true
}v=v.parentNode
}while(v){if(aD(v,o,h)){z=v;
break
}if(v.nextSibling){r=true
}A.push(v);
v=v.parentNode
}if(!z){return
}if(r){q=aJ.getBookmark();
y.collapse(true);
y=aL(y,ae(o),true);
y=am.split(y);
az(o,h,y);
aJ.moveToBookmark(q)
}else{p=m();
v=p;
for(s=A.length-1;
s>=0;
s--){v.appendChild(aY.clone(A[s],false));
v=v.firstChild
}v.appendChild(aY.doc.createTextNode(au));
v=v.firstChild;
var u=aY.getParent(z,ar);
if(u&&aY.isEmpty(u)){z.parentNode.replaceChild(p,z)
}else{aY.insertAfter(p,z)
}aJ.setCursorLocation(v,1);
if(aY.isEmpty(z)){aY.remove(z)
}}}function c(){var q,r,p;
r=j(aJ.getStart());
if(r&&!aY.isEmpty(r)){b.walk(r,function(s){if(s.nodeType==1&&s.id!==e&&!aY.isEmpty(s)){aY.setAttrib(s,"data-mce-bogus",null)
}},"childNodes")
}}if(!self._hasCaretEvents){aB.onBeforeGetContent.addToTop(function(){var q=[],p;
if(g(j(aJ.getStart()),q)){p=q.length;
while(p--){aY.setAttrib(q[p],"data-mce-bogus","1")
}}});
b.each("onMouseUp onKeyUp".split(" "),function(p){aB[p].addToTop(function(){l();
c()
})
});
aB.onKeyDown.addToTop(function(r,p){var q=p.keyCode;
if(q==8||q==37||q==39){l(j(aJ.getStart()))
}c()
});
aJ.onSetContent.add(c);
self._hasCaretEvents=true
}if(f=="apply"){i()
}else{d()
}}function ai(i){var j=i.startContainer,c=i.startOffset,g,d,e,h,f;
if(j.nodeType==3&&c>=j.nodeValue.length){c=aI(j);
j=j.parentNode;
g=true
}if(j.nodeType==1){h=j.childNodes;
j=h[Math.min(c,h.length-1)];
d=new aH(j,aY.getParent(j,aY.isBlock));
if(c>h.length-1||g){d.next()
}for(e=d.current();
e;
e=d.next()){if(e.nodeType==3&&!aV(e)){f=aY.create("a",null,au);
e.parentNode.insertBefore(f,e);
i.setStart(e,0);
aJ.setRng(i);
aY.remove(f);
return
}}}}}
})(tinymce);
tinymce.onAddEditor.add(function(n,j){var o,k,l,p=j.settings;
function i(a,b){n.each(b,function(c,d){if(c){l.setStyle(a,d,c)
}});
l.rename(a,"span")
}function m(b,a){l=b.dom;
if(p.convert_fonts_to_spans){n.each(l.select("font,u,strike",a.node),function(c){o[c.nodeName.toLowerCase()](j.dom,c)
})
}}if(p.inline_styles){k=n.explode(p.font_size_legacy_values);
o={font:function(a,b){i(b,{backgroundColor:b.style.backgroundColor,color:b.color,fontFamily:b.face,fontSize:k[parseInt(b.size,10)-1]})
},u:function(a,b){i(b,{textDecoration:"underline"})
},strike:function(a,b){i(b,{textDecoration:"line-through"})
}};
j.onPreProcess.add(m);
j.onSetContent.add(m);
j.onInit.add(function(){j.selection.onSetContent.add(m)
})
}});
(function(c){var d=c.dom.TreeWalker;
c.EnterKey=function(k){var a=k.dom,l=k.selection,m=k.settings,b=k.undoManager,n=k.schema.getNonEmptyElements();
function j(ac){var S=l.getRng(true),R,ak,ad,U,ae,e,aa,af,aj,ag,W,N,P,Y;
function V(o){return o&&a.isBlock(o)&&!/^(TD|TH|CAPTION|FORM)$/.test(o.nodeName)&&!/^(fixed|absolute)/i.test(o.style.position)&&a.getContentEditable(o)!=="true"
}function T(o){var p;
if(c.isIE&&!c.isIE11&&a.isBlock(o)){p=l.getRng();
o.appendChild(a.create("span",null,"\u00a0"));
l.select(o);
o.lastChild.outerHTML="";
l.setRng(p)
}}function h(p){var q=p,o=[],r;
while(q=q.firstChild){if(a.isBlock(q)){return
}if(q.nodeType==1&&!n[q.nodeName.toLowerCase()]){o.push(q)
}}r=o.length;
while(r--){q=o[r];
if(!q.hasChildNodes()||(q.firstChild==q.lastChild&&q.firstChild.nodeValue==="")){a.remove(q)
}else{if(q.nodeName=="A"&&(q.innerText||q.textContent)===" "){a.remove(q)
}}}}function ah(u){var p,r,v,o,q,s=u,t;
v=a.createRng();
if(u.hasChildNodes()){p=new d(u,u);
while(r=p.current()){if(r.nodeType==3){v.setStart(r,0);
v.setEnd(r,0);
break
}if(n[r.nodeName.toLowerCase()]){v.setStartBefore(r);
v.setEndBefore(r);
break
}s=r;
r=p.next()
}if(!r){v.setStart(s,0);
v.setEnd(s,0)
}}else{if(u.nodeName=="BR"){if(u.nextSibling&&a.isBlock(u.nextSibling)){if(!e||e<9){t=a.create("br");
u.parentNode.insertBefore(t,u)
}v.setStartBefore(u);
v.setEndBefore(u)
}else{v.setStartAfter(u);
v.setEndAfter(u)
}}else{v.setStart(u,0);
v.setEnd(u,0)
}}l.setRng(v);
a.remove(t);
q=a.getViewPort(k.getWin());
o=a.getPos(u).y;
if(o<q.y||o+25>q.y+q.h){k.getWin().scrollTo(0,o<q.y?o:o-q.h+25)
}}function Z(r){var q=ad,o,p,s;
o=r||W=="TABLE"?a.create(r||P):ae.cloneNode(false);
s=o;
if(m.keep_styles!==false){do{if(/^(SPAN|STRONG|B|EM|I|FONT|STRIKE|U)$/.test(q.nodeName)){if(q.id=="_mce_caret"){continue
}p=q.cloneNode(false);
a.setAttrib(p,"id","");
if(o.hasChildNodes()){p.appendChild(o.firstChild);
o.appendChild(p)
}else{s=p;
o.appendChild(p)
}}}while(q=q.parentNode)
}if(!c.isIE||c.isIE11){s.innerHTML='<br data-mce-bogus="1">'
}return o
}function ab(o){var p,q,r;
if(ad.nodeType==3&&(o?U>0:U<ad.nodeValue.length)){return false
}if(ad.parentNode==ae&&Y&&!o){return true
}if(o&&ad.nodeType==1&&ad==ae.firstChild){return true
}if(ad.nodeName==="TABLE"||(ad.previousSibling&&ad.previousSibling.nodeName=="TABLE")){return(Y&&!o)||(!Y&&o)
}p=new d(ad,ae);
if(ad.nodeType==3){if(o&&U==0){p.prev()
}else{if(!o&&U==ad.nodeValue.length){p.next()
}}}while(q=p.current()){if(q.nodeType===1){if(!q.getAttribute("data-mce-bogus")){r=q.nodeName.toLowerCase();
if(n[r]&&r!=="br"){return false
}}}else{if(q.nodeType===3&&!/^[ \t\r\n]*$/.test(q.nodeValue)){return false
}}if(o){p.prev()
}else{p.next()
}}return true
}function ai(v,p){var o,q,t,r,s,u=P||"P";
q=a.getParent(v,a.isBlock);
if(!q||!V(q)){q=q||ak;
if(!q.hasChildNodes()){o=a.create(u);
q.appendChild(o);
S.setStart(o,0);
S.setEnd(o,0);
return o
}r=v;
while(r.parentNode!=q){r=r.parentNode
}while(r&&!a.isBlock(r)){t=r;
r=r.previousSibling
}if(t){o=a.create(u);
t.parentNode.insertBefore(o,t);
r=t;
while(r&&!a.isBlock(r)){s=r.nextSibling;
o.appendChild(r);
r=s
}S.setStart(v,p);
S.setEnd(v,p)
}}return v
}function Q(){function o(p){var q=ag[p?"firstChild":"lastChild"];
while(q){if(q.nodeType==1){break
}q=q[p?"nextSibling":"previousSibling"]
}return q===ae
}af=P?Z(P):a.create("BR");
if(o(true)&&o()){a.replace(af,ag)
}else{if(o(true)){ag.parentNode.insertBefore(af,ag)
}else{if(o()){a.insertAfter(af,ag);
T(af)
}else{R=S.cloneRange();
R.setStartAfter(ae);
R.setEndAfter(ag);
aj=R.extractContents();
a.insertAfter(aj,ag);
a.insertAfter(af,ag)
}}}a.remove(ae);
ah(af);
b.add()
}function i(){var o=new d(ad,ae),p;
while(p=o.next()){if(n[p.nodeName.toLowerCase()]||p.length>0){return true
}}}function f(){var o,p,q;
if(ad&&ad.nodeType==3&&U>=ad.nodeValue.length){if((!c.isIE||c.isIE11)&&!i()){o=a.create("br");
S.insertNode(o);
S.setStartAfter(o);
S.setEndAfter(o);
p=true
}}o=a.create("br");
S.insertNode(o);
if((c.isIE&&!c.isIE11)&&W=="PRE"&&(!e||e<8)){o.parentNode.insertBefore(a.doc.createTextNode("\r"),o)
}q=a.create("span",{},"&nbsp;");
o.parentNode.insertBefore(q,o);
l.scrollIntoView(q);
a.remove(q);
if(!p){S.setStartAfter(o);
S.setEndAfter(o)
}else{S.setStartBefore(o);
S.setEndBefore(o)
}l.setRng(S);
b.add()
}function X(o){do{if(o.nodeType===3){o.nodeValue=o.nodeValue.replace(/^[\r\n]+/,"")
}o=o.firstChild
}while(o)
}function g(p){var r=a.getRoot(),q,o;
q=p;
while(q!==r&&a.getContentEditable(q)!=="false"){if(a.getContentEditable(q)==="true"){o=q
}q=q.parentNode
}return q!==r?o:r
}function O(o){var p;
if(!c.isIE||c.isIE11){o.normalize();
p=o.lastChild;
if(!p||(/^(left|right)$/gi.test(a.getStyle(p,"float",true)))){a.add(o,"br")
}}}if(!S.collapsed){k.execCommand("Delete");
return
}if(ac.isDefaultPrevented()){return
}ad=S.startContainer;
U=S.startOffset;
P=(m.force_p_newlines?"p":"")||m.forced_root_block;
P=P?P.toUpperCase():"";
e=a.doc.documentMode;
aa=ac.shiftKey;
if(ad.nodeType==1&&ad.hasChildNodes()){Y=U>ad.childNodes.length-1;
ad=ad.childNodes[Math.min(U,ad.childNodes.length-1)]||ad;
if(Y&&ad.nodeType==3){U=ad.nodeValue.length
}else{U=0
}}ak=g(ad);
if(!ak){return
}b.beforeChange();
if(!a.isBlock(ak)&&ak!=a.getRoot()){if(!P||aa){f()
}return
}if((P&&!aa)||(!P&&aa)){ad=ai(ad,U)
}ae=a.getParent(ad,a.isBlock);
ag=ae?a.getParent(ae.parentNode,a.isBlock):null;
W=ae?ae.nodeName.toUpperCase():"";
N=ag?ag.nodeName.toUpperCase():"";
if(N=="LI"&&!ac.ctrlKey){ae=ag;
W=N
}if(W=="LI"){if(!P&&aa){f();
return
}if(a.isEmpty(ae)){if(/^(UL|OL|LI)$/.test(ag.parentNode.nodeName)){return false
}Q();
return
}}if(W=="PRE"&&m.br_in_pre!==false){if(!aa){f();
return
}}else{if((!P&&!aa&&W!="LI")||(P&&aa)){f();
return
}}P=P||"P";
if(ab()){if(/^(H[1-6]|PRE)$/.test(W)&&N!="HGROUP"){af=Z(P)
}else{af=Z()
}if(m.end_container_on_empty_block&&V(ag)&&a.isEmpty(ae)){af=a.split(ag,ae)
}else{a.insertAfter(af,ae)
}ah(af)
}else{if(ab(true)){af=ae.parentNode.insertBefore(Z(),ae);
T(af)
}else{R=S.cloneRange();
R.setEndAfter(ae);
aj=R.extractContents();
X(aj);
af=aj.firstChild;
a.insertAfter(aj,ae);
h(af);
O(ae);
ah(af)
}}a.setAttrib(af,"id","");
b.add()
}k.onKeyDown.add(function(e,f){if(f.keyCode==13){if(j(f)!==false){f.preventDefault()
}}})
}
})(tinymce);