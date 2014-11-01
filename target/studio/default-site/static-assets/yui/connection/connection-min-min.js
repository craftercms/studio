YAHOO.util.Connect={_msxml_progid:["Microsoft.XMLHTTP","MSXML2.XMLHTTP.3.0","MSXML2.XMLHTTP"],_http_headers:{},_has_http_headers:false,_use_default_post_header:true,_default_post_header:"application/x-www-form-urlencoded; charset=UTF-8",_default_form_header:"application/x-www-form-urlencoded",_use_default_xhr_header:true,_default_xhr_header:"XMLHttpRequest",_has_default_headers:true,_default_headers:{},_isFormSubmit:false,_isFileUpload:false,_formNode:null,_sFormData:null,_poll:{},_timeOut:{},_polling_interval:50,_transaction_id:0,_submitElementValue:null,_hasSubmitListener:(function(){if(YAHOO.util.Event){YAHOO.util.Event.addListener(document,"click",function(b){var a=YAHOO.util.Event.getTarget(b);
if(a.nodeName.toLowerCase()=="input"&&(a.type&&a.type.toLowerCase()=="submit")){YAHOO.util.Connect._submitElementValue=encodeURIComponent(a.name)+"="+encodeURIComponent(a.value)
}});
return true
}return false
})(),startEvent:new YAHOO.util.CustomEvent("start"),completeEvent:new YAHOO.util.CustomEvent("complete"),successEvent:new YAHOO.util.CustomEvent("success"),failureEvent:new YAHOO.util.CustomEvent("failure"),uploadEvent:new YAHOO.util.CustomEvent("upload"),abortEvent:new YAHOO.util.CustomEvent("abort"),_customEvents:{onStart:["startEvent","start"],onComplete:["completeEvent","complete"],onSuccess:["successEvent","success"],onFailure:["failureEvent","failure"],onUpload:["uploadEvent","upload"],onAbort:["abortEvent","abort"]},setProgId:function(a){this._msxml_progid.unshift(a)
},setDefaultPostHeader:function(a){if(typeof a=="string"){this._default_post_header=a
}else{if(typeof a=="boolean"){this._use_default_post_header=a
}}},setDefaultXhrHeader:function(a){if(typeof a=="string"){this._default_xhr_header=a
}else{this._use_default_xhr_header=a
}},setPollingInterval:function(a){if(typeof a=="number"&&isFinite(a)){this._polling_interval=a
}},createXhrObject:function(b){var c,a;
try{a=new XMLHttpRequest();
c={conn:a,tId:b}
}catch(d){for(var f=0;
f<this._msxml_progid.length;
++f){try{a=new ActiveXObject(this._msxml_progid[f]);
c={conn:a,tId:b};
break
}catch(e){}}}finally{return c
}},getConnectionObject:function(a){var c;
var b=this._transaction_id;
try{if(!a){c=this.createXhrObject(b)
}else{c={};
c.tId=b;
c.isUpload=true
}if(c){this._transaction_id++
}}catch(d){}finally{return c
}},asyncRequest:function(b,e,c,a){var d=(this._isFileUpload)?this.getConnectionObject(true):this.getConnectionObject();
var f=(c&&c.argument)?c.argument:null;
if(!d){return null
}else{if(c&&c.customevents){this.initCustomEvents(d,c)
}if(this._isFormSubmit){if(this._isFileUpload){this.uploadFile(d,c,e,a);
return d
}if(b.toUpperCase()=="GET"){if(this._sFormData.length!==0){e+=((e.indexOf("?")==-1)?"?":"&")+this._sFormData
}}else{if(b.toUpperCase()=="POST"){a=a?this._sFormData+"&"+a:this._sFormData
}}}if(b.toUpperCase()=="GET"&&(c&&c.cache===false)){e+=((e.indexOf("?")==-1)?"?":"&")+"rnd="+new Date().valueOf().toString()
}d.conn.open(b,e,true);
if(this._use_default_xhr_header){if(!this._default_headers["X-Requested-With"]){this.initHeader("X-Requested-With",this._default_xhr_header,true)
}}if((b.toUpperCase()==="POST"&&this._use_default_post_header)&&this._isFormSubmit===false){this.initHeader("Content-Type",this._default_post_header)
}if(this._has_default_headers||this._has_http_headers){this.setHeader(d)
}this.handleReadyState(d,c);
d.conn.send(a||"");
if(this._isFormSubmit===true){this.resetFormState()
}this.startEvent.fire(d,f);
if(d.startEvent){d.startEvent.fire(d,f)
}return d
}},initCustomEvents:function(a,b){var c;
for(c in b.customevents){if(this._customEvents[c][0]){a[this._customEvents[c][0]]=new YAHOO.util.CustomEvent(this._customEvents[c][1],(b.scope)?b.scope:null);
a[this._customEvents[c][0]].subscribe(b.customevents[c])
}}},handleReadyState:function(c,b){var d=this;
var a=(b&&b.argument)?b.argument:null;
if(b&&b.timeout){this._timeOut[c.tId]=window.setTimeout(function(){d.abort(c,b,true)
},b.timeout)
}this._poll[c.tId]=window.setInterval(function(){if(c.conn&&c.conn.readyState===4){window.clearInterval(d._poll[c.tId]);
delete d._poll[c.tId];
if(b&&b.timeout){window.clearTimeout(d._timeOut[c.tId]);
delete d._timeOut[c.tId]
}d.completeEvent.fire(c,a);
if(c.completeEvent){c.completeEvent.fire(c,a)
}d.handleTransactionResponse(c,b)
}},this._polling_interval)
},handleTransactionResponse:function(c,b,a){var e,f;
var g=(b&&b.argument)?b.argument:null;
try{if(c.conn.status!==undefined&&c.conn.status!==0){e=c.conn.status
}else{e=13030
}}catch(d){e=13030
}if(e>=200&&e<300||e===1223){f=this.createResponseObject(c,g);
if(b&&b.success){if(!b.scope){b.success(f)
}else{b.success.apply(b.scope,[f])
}}this.successEvent.fire(f);
if(c.successEvent){c.successEvent.fire(f)
}}else{switch(e){case 12002:case 12029:case 12030:case 12031:case 12152:case 13030:f=this.createExceptionObject(c.tId,g,(a?a:false));
if(b&&b.failure){if(!b.scope){b.failure(f)
}else{b.failure.apply(b.scope,[f])
}}break;
default:f=this.createResponseObject(c,g);
if(b&&b.failure){if(!b.scope){b.failure(f)
}else{b.failure.apply(b.scope,[f])
}}}this.failureEvent.fire(f);
if(c.failureEvent){c.failureEvent.fire(f)
}}this.releaseObject(c);
f=null
},createResponseObject:function(d,g){var a={};
var e={};
try{var b=d.conn.getAllResponseHeaders();
var h=b.split("\n");
for(var i=0;
i<h.length;
i++){var c=h[i].indexOf(":");
if(c!=-1){e[h[i].substring(0,c)]=h[i].substring(c+2)
}}}catch(f){}a.tId=d.tId;
a.status=(d.conn.status==1223)?204:d.conn.status;
a.statusText=(d.conn.status==1223)?"No Content":d.conn.statusText;
a.getResponseHeader=e;
a.getAllResponseHeaders=b;
a.responseText=d.conn.responseText;
a.responseXML=d.conn.responseXML;
if(g){a.argument=g
}return a
},createExceptionObject:function(b,f,a){var d=0;
var c="communication failure";
var g=-1;
var h="transaction aborted";
var e={};
e.tId=b;
if(a){e.status=g;
e.statusText=h
}else{e.status=d;
e.statusText=c
}if(f){e.argument=f
}return e
},initHeader:function(a,b,c){var d=(c)?this._default_headers:this._http_headers;
d[a]=b;
if(c){this._has_default_headers=true
}else{this._has_http_headers=true
}},setHeader:function(a){var b;
if(this._has_default_headers){for(b in this._default_headers){if(YAHOO.lang.hasOwnProperty(this._default_headers,b)){a.conn.setRequestHeader(b,this._default_headers[b])
}}}if(this._has_http_headers){for(b in this._http_headers){if(YAHOO.lang.hasOwnProperty(this._http_headers,b)){a.conn.setRequestHeader(b,this._http_headers[b])
}}delete this._http_headers;
this._http_headers={};
this._has_http_headers=false
}},resetDefaultHeaders:function(){delete this._default_headers;
this._default_headers={};
this._has_default_headers=false
},setForm:function(h,m,b){var i,c,j,l,e,k=false,o=[],f=0,p,n,a,g,d;
this.resetFormState();
if(typeof h=="string"){i=(document.getElementById(h)||document.forms[h])
}else{if(typeof h=="object"){i=h
}else{return
}}if(m){this.createFrame(b?b:null);
this._isFormSubmit=true;
this._isFileUpload=true;
this._formNode=i;
return
}for(p=0,n=i.elements.length;
p<n;
++p){c=i.elements[p];
e=c.disabled;
j=c.name;
if(!e&&j){j=encodeURIComponent(j)+"=";
l=encodeURIComponent(c.value);
switch(c.type){case"select-one":if(c.selectedIndex>-1){d=c.options[c.selectedIndex];
o[f++]=j+encodeURIComponent((d.attributes.value&&d.attributes.value.specified)?d.value:d.text)
}break;
case"select-multiple":if(c.selectedIndex>-1){for(a=c.selectedIndex,g=c.options.length;
a<g;
++a){d=c.options[a];
if(d.selected){o[f++]=j+encodeURIComponent((d.attributes.value&&d.attributes.value.specified)?d.value:d.text)
}}}break;
case"radio":case"checkbox":if(c.checked){o[f++]=j+l
}break;
case"file":case undefined:case"reset":case"button":break;
case"submit":if(k===false){if(this._hasSubmitListener&&this._submitElementValue){o[f++]=this._submitElementValue
}else{o[f++]=j+l
}k=true
}break;
default:o[f++]=j+l
}}}this._isFormSubmit=true;
this._sFormData=o.join("&");
this.initHeader("Content-Type",this._default_form_header);
return this._sFormData
},resetFormState:function(){this._isFormSubmit=false;
this._isFileUpload=false;
this._formNode=null;
this._sFormData=""
},createFrame:function(a){var e="yuiIO"+this._transaction_id;
var b=document.documentMode?document.documentMode:false;
var d=(b>=9)?true:false;
var c;
if(YAHOO.env.ua.ie&&!d){c=document.createElement('<iframe id="'+e+'" name="'+e+'" />');
if(typeof a=="boolean"){c.src="javascript:false"
}}else{c=document.createElement("iframe");
c.id=e;
c.name=e
}c.style.position="absolute";
c.style.top="-1000px";
c.style.left="-1000px";
document.body.appendChild(c)
},appendPostData:function(a){var c=[],e=a.split("&"),d,b;
for(d=0;
d<e.length;
d++){b=e[d].indexOf("=");
if(b!=-1){c[d]=document.createElement("input");
c[d].type="hidden";
c[d].name=decodeURIComponent(e[d].substring(0,b));
c[d].value=decodeURIComponent(e[d].substring(b+1));
this._formNode.appendChild(c[d])
}}return c
},uploadFile:function(a,g,q,b){var l="yuiIO"+a.tId,k="multipart/form-data",i=document.getElementById(l),f=this,j=(g&&g.argument)?g.argument:null,h,n,c,o;
var m=document.documentMode?document.documentMode:false;
var e=(m>=9)?true:false;
var d={action:this._formNode.getAttribute("action"),method:this._formNode.getAttribute("method"),target:this._formNode.getAttribute("target")};
this._formNode.setAttribute("action",q);
this._formNode.setAttribute("method","POST");
this._formNode.setAttribute("target",l);
if(YAHOO.env.ua.ie&&!e){this._formNode.setAttribute("encoding",k)
}else{this._formNode.setAttribute("enctype",k)
}if(b){h=this.appendPostData(b)
}this._formNode.submit();
this.startEvent.fire(a,j);
if(a.startEvent){a.startEvent.fire(a,j)
}if(g&&g.timeout){this._timeOut[a.tId]=window.setTimeout(function(){f.abort(a,g,true)
},g.timeout)
}if(h&&h.length>0){for(n=0;
n<h.length;
n++){this._formNode.removeChild(h[n])
}}for(c in d){if(YAHOO.lang.hasOwnProperty(d,c)){if(d[c]){this._formNode.setAttribute(c,d[c])
}else{this._formNode.removeAttribute(c)
}}}this.resetFormState();
var p=function(){if(g&&g.timeout){window.clearTimeout(f._timeOut[a.tId]);
delete f._timeOut[a.tId]
}f.completeEvent.fire(a,j);
if(a.completeEvent){a.completeEvent.fire(a,j)
}o={tId:a.tId,argument:g.argument};
try{o.responseText=i.contentWindow.document.body?i.contentWindow.document.body.innerHTML:i.contentWindow.document.documentElement.textContent;
o.responseXML=i.contentWindow.document.XMLDocument?i.contentWindow.document.XMLDocument:i.contentWindow.document
}catch(r){}if(g&&g.upload){if(!g.scope){g.upload(o)
}else{g.upload.apply(g.scope,[o])
}}f.uploadEvent.fire(o);
if(a.uploadEvent){a.uploadEvent.fire(o)
}YAHOO.util.Event.removeListener(i,"load",p);
setTimeout(function(){document.body.removeChild(i);
f.releaseObject(a)
},100)
};
YAHOO.util.Event.addListener(i,"load",p)
},abort:function(d,b,a){var e;
var g=(b&&b.argument)?b.argument:null;
if(d&&d.conn){if(this.isCallInProgress(d)){d.conn.abort();
window.clearInterval(this._poll[d.tId]);
delete this._poll[d.tId];
if(a){window.clearTimeout(this._timeOut[d.tId]);
delete this._timeOut[d.tId]
}e=true
}}else{if(d&&d.isUpload===true){var f="yuiIO"+d.tId;
var c=document.getElementById(f);
if(c){YAHOO.util.Event.removeListener(c,"load");
document.body.removeChild(c);
if(a){window.clearTimeout(this._timeOut[d.tId]);
delete this._timeOut[d.tId]
}e=true
}}else{e=false
}}if(e===true){this.abortEvent.fire(d,g);
if(d.abortEvent){d.abortEvent.fire(d,g)
}this.handleTransactionResponse(d,b,true)
}return e
},isCallInProgress:function(b){if(b&&b.conn){return b.conn.readyState!==4&&b.conn.readyState!==0
}else{if(b&&b.isUpload===true){var a="yuiIO"+b.tId;
return document.getElementById(a)?true:false
}else{return false
}}},releaseObject:function(a){if(a&&a.conn){a.conn=null;
a=null
}}};
YAHOO.register("connection",YAHOO.util.Connect,{version:"2.6.0",build:"1321"});