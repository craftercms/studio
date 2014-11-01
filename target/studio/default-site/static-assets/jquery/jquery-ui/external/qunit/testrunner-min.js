(function(g){var d=function(){var G;
var H=[];
function q(I){if(typeof I==="string"){return"string"
}else{if(typeof I==="boolean"){return"boolean"
}else{if(typeof I==="number"){if(isNaN(I)){return"nan"
}else{return"number"
}}else{if(typeof I==="undefined"){return"undefined"
}else{if(I===null){return"null"
}else{if(I instanceof Array){return"array"
}else{if(I instanceof Date){return"date"
}else{if(I instanceof RegExp){return"regexp"
}else{if(typeof I==="object"){return"object"
}else{if(I instanceof Function){return"function"
}}}}}}}}}}}function F(K,J,I){var L=q(K);
if(L){if(q(J[L])==="function"){return J[L].apply(J,I)
}else{return J[L]
}}}var t=function(){function I(J,K){return K===J
}return{string:I,"boolean":I,number:I,"null":I,"undefined":I,nan:function(J){return isNaN(J)
},date:function(J,K){return q(J)==="date"&&K.valueOf()===J.valueOf()
},regexp:function(J,K){return q(J)==="regexp"&&K.source===J.source&&K.global===J.global&&K.ignoreCase===J.ignoreCase&&K.multiline===J.multiline
},"function":function(){var J=H[H.length-1];
return J!==Object&&typeof J!=="undefined"
},array:function(K,L){var M;
var J;
if(!(q(K)==="array")){return false
}J=L.length;
if(J!==K.length){return false
}for(M=0;
M<J;
M++){if(!G(L[M],K[M])){return false
}}return true
},object:function(K,L){var N;
var J=true;
var M=[],O=[];
if(L.constructor!==K.constructor){return false
}H.push(L.constructor);
for(N in L){M.push(N);
if(!G(L[N],K[N])){J=false
}}H.pop();
for(N in K){O.push(N)
}return J&&G(M.sort(),O.sort())
}}
}();
G=function(){var I=Array.prototype.slice.apply(arguments);
if(I.length<2){return true
}return(function(K,J){if(K===J){return true
}else{if(typeof K!==typeof J||K===null||J===null||typeof K==="undefined"||typeof J==="undefined"){return false
}else{return F(K,t,[J,K])
}}})(I[0],I[1])&&arguments.callee.apply(this,I.splice(1,I.length-1))
};
return G
}();
var p=g.map(location.search.slice(1).split("&"),decodeURIComponent),j=g.inArray("noglobals",p),o=j!==-1;
if(o){p.splice(j,1)
}var E={stats:{all:0,bad:0},queue:[],blocking:true,filters:p,isLocal:!!(window.location.protocol=="file:")};
g.extend(window,{test:C,module:a,expect:e,ok:r,equals:l,start:h,stop:y,reset:D,isLocal:E.isLocal,same:function(t,q,F){i(d(t,q),t,q,F)
},QUnit:{equiv:d,ok:r,done:function(q,t){},log:function(q,t){}},isSet:A,isObj:z,compare:function(){throw"compare is deprecated - use same() instead"
},compare2:function(){throw"compare2 is deprecated - use same() instead"
},serialArray:function(){throw"serialArray is deprecated - use jsDump.parse() instead"
},q:w,t:s,url:f,triggerEvent:c});
g(window).load(function(){g("#userAgent").html(navigator.userAgent);
var q=g('<div class="testrunner-toolbar"><label for="filter-pass">Hide passed tests</label></div>').insertAfter("#userAgent");
g('<input type="checkbox" id="filter-pass" />').attr("disabled",true).prependTo(q).click(function(){g("li.pass")[this.checked?"hide":"show"]()
});
g('<input type="checkbox" id="filter-missing">').attr("disabled",true).appendTo(q).click(function(){g("li.fail:contains('missing test - untested code is broken code')").parent("ol").parent("li.fail")[this.checked?"hide":"show"]()
});
g("#filter-missing").after('<label for="filter-missing">Hide missing tests (untested code is broken code)</label>');
n()
});
function m(q){E.queue.push(q);
if(!E.blocking){x()
}}function x(){while(E.queue.length&&!E.blocking){E.queue.shift()()
}}function y(q){E.blocking=true;
if(q){E.timeout=setTimeout(function(){QUnit.ok(false,"Test timed out");
h()
},q)
}}function h(){setTimeout(function(){if(E.timeout){clearTimeout(E.timeout)
}E.blocking=false;
x()
},13)
}function k(q){var t=E.filters.length,H=false;
if(!t){return true
}while(t--){var F=E.filters[t],G=F.charAt(0)=="!";
if(G){F=F.slice(1)
}if(q.indexOf(F)!=-1){return !G
}if(G){H=true
}}return H
}function n(){E.blocking=false;
var q=+new Date;
E.fixture=document.getElementById("main").innerHTML;
E.ajaxSettings=g.ajaxSettings;
m(function(){g('<p id="testresult" class="result"/>').html(["Tests completed in ",+new Date-q," milliseconds.<br/>",'<span class="bad">',E.stats.bad,'</span> tests of <span class="all">',E.stats.all,"</span> failed."].join("")).appendTo("body");
g("#banner").addClass(E.stats.bad?"fail":"pass");
QUnit.done(E.stats.bad,E.stats.all)
})
}var b;
function B(){b=[];
if(o){for(var q in window){b.push(q)
}}}function v(t){var q=b;
B();
if(b.length>q.length){r(false,"Introduced global variable(s): "+u(q,b).join(", "));
E.expected++
}}function u(q,t){return g.grep(t,function(F){return g.inArray(F,q)==-1
})
}function C(t,F){if(E.currentModule){t=E.currentModule+" module: "+t
}var q=g.extend({setup:function(){},teardown:function(){}},E.moduleLifecycle);
if(!k(t)){return
}m(function(){E.assertions=[];
E.expected=null;
try{if(!b){B()
}q.setup()
}catch(G){QUnit.ok(false,"Setup failed on "+t+": "+G.message)
}});
m(function(){try{F()
}catch(G){if(typeof console!="undefined"&&console.error&&console.warn){console.error("Test "+t+" died, exception and test follows");
console.error(G);
console.warn(F.toString())
}QUnit.ok(false,"Died on test #"+(E.assertions.length+1)+": "+G.message);
B()
}});
m(function(){try{v();
q.teardown()
}catch(G){QUnit.ok(false,"Teardown failed on "+t+": "+G.message)
}});
m(function(){try{D()
}catch(L){if(typeof console!="undefined"&&console.error&&console.warn){console.error("reset() failed, following Test "+t+", exception and reset fn follows");
console.error(L);
console.warn(D.toString())
}}if(E.expected&&E.expected!=E.assertions.length){QUnit.ok(false,"Expected "+E.expected+" assertions, but "+E.assertions.length+" were run")
}var K=0,M=0;
var I=g("<ol/>").hide();
E.stats.all+=E.assertions.length;
for(var J=0;
J<E.assertions.length;
J++){var H=E.assertions[J];
g("<li/>").addClass(H.result?"pass":"fail").text(H.message||"(no message)").appendTo(I);
H.result?K++:M++
}E.stats.bad+=M;
var G=g("<strong/>").html(t+" <b style='color:black;'>(<b class='fail'>"+M+"</b>, <b class='pass'>"+K+"</b>, "+E.assertions.length+")</b>").click(function(){g(this).next().toggle()
}).dblclick(function(N){var O=g(N.target).filter("strong").clone();
if(O.length){O.children().remove();
location.href=location.href.match(/^(.+?)(\?.*)?$/)[1]+"?"+encodeURIComponent(g.trim(O.text()))
}});
g("<li/>").addClass(M?"fail":"pass").append(G).append(I).appendTo("#tests");
if(M){g("#filter-pass").attr("disabled",null);
g("#filter-missing").attr("disabled",null)
}})
}function a(t,q){E.currentModule=t;
E.moduleLifecycle=q
}function e(q){E.expected=q
}function D(){g("#main").html(E.fixture);
g.event.global={};
g.ajaxSettings=g.extend({},E.ajaxSettings)
}function r(q,t){QUnit.log(q,t);
E.assertions.push({result:!!q,message:t})
}function A(t,q,I){function H(J){var L=[];
if(J&&J.length){for(var K=0;
K<J.length;
K++){var M=J[K].nodeName;
if(M){M=M.toLowerCase();
if(J[K].id){M+="#"+J[K].id
}}else{M=J[K]
}L.push(M)
}}return"[ "+L.join(", ")+" ]"
}var F=true;
if(t&&q&&t.length!=undefined&&t.length==q.length){for(var G=0;
G<t.length;
G++){if(t[G]!=q[G]){F=false
}}}else{F=false
}QUnit.ok(F,!F?(I+" expected: "+H(q)+" result: "+H(t)):I)
}function z(t,q,H){var F=true;
if(t&&q){for(var G in t){if(t[G]!=q[G]){F=false
}}for(G in q){if(t[G]!=q[G]){F=false
}}}else{F=false
}QUnit.ok(F,H)
}function w(){var t=[];
for(var q=0;
q<arguments.length;
q++){t.push(document.getElementById(arguments[q]))
}return t
}function s(t,q,I){var H=g(q);
var G="";
for(var F=0;
F<H.length;
F++){G+=(G&&",")+'"'+H[F].id+'"'
}A(H,w.apply(w,I),t+" ("+q+")")
}function f(q){return q+(/\?/.test(q)?"&":"?")+new Date().getTime()+""+parseInt(Math.random()*100000)
}function l(F,t,q){i(t==F,F,t,q)
}function i(q,G,F,t){t=t||(q?"okay":"failed");
QUnit.ok(q,q?t+": "+F:t+", expected: "+jsDump.parse(F)+" result: "+jsDump.parse(G))
}function c(F,q,t){if(g.browser.mozilla||g.browser.opera){t=document.createEvent("MouseEvents");
t.initMouseEvent(q,true,true,F.ownerDocument.defaultView,0,0,0,0,0,false,false,false,false,0,null);
F.dispatchEvent(t)
}else{if(g.browser.msie){F.fireEvent("on"+q)
}}}})(jQuery);
(function(){function a(g){return'"'+g.toString().replace(/"/g,'\\"')+'"'
}function b(g){return g+""
}function d(k,g,h){var i=c.separator(),j=c.indent();
inner=c.indent(1);
if(g.join){g=g.join(","+i+inner)
}if(!g){return k+h
}return[k,inner+g,j+h].join(i)
}function f(g){var j=g.length,h=Array(j);
this.up();
while(j--){h[j]=this.parse(g[j])
}this.down();
return d("[",h,"]")
}var e=/^function (\w+)/;
var c=window.jsDump={parse:function(h,g){var i=this.parsers[g||this.typeOf(h)];
g=typeof i;
return g=="function"?i.call(this,h):g=="string"?i:this.parsers.error
},typeOf:function(i){var g=typeof i,h="function";
return g!="object"&&g!=h?g:!i?"null":i.exec?"regexp":i.getHours?"date":i.scrollBy?"window":i.nodeName=="#document"?"document":i.nodeName?"node":i.item?"nodelist":i.callee?"arguments":i.call||i.constructor!=Array&&(i+"").indexOf(h)!=-1?h:"length" in i?"array":g
},separator:function(){return this.multiline?this.HTML?"<br />":"\n":this.HTML?"&nbsp;":" "
},indent:function(g){if(!this.multiline){return""
}var h=this.indentChar;
if(this.HTML){h=h.replace(/\t/g,"   ").replace(/ /g,"&nbsp;")
}return Array(this._depth_+(g||0)).join(h)
},up:function(g){this._depth_+=g||1
},down:function(g){this._depth_-=g||1
},setParser:function(g,h){this.parsers[g]=h
},quote:a,literal:b,join:d,_depth_:1,parsers:{window:"[Window]",document:"[Document]",error:"[ERROR]",unknown:"[Unknown]","null":"null",undefined:"undefined","function":function(i){var h="function",g="name" in i?i.name:(e.exec(i)||[])[1];
if(g){h+=" "+g
}h+="(";
h=[h,this.parse(i,"functionArgs"),"){"].join("");
return d(h,this.parse(i,"functionCode"),"}")
},array:f,nodelist:f,arguments:f,object:function(i){var g=[];
this.up();
for(var h in i){g.push(this.parse(h,"key")+": "+this.parse(i[h]))
}this.down();
return d("{",g,"}")
},node:function(k){var j=this.HTML?"&lt;":"<",m=this.HTML?"&gt;":">";
var g=k.nodeName.toLowerCase(),i=j+g;
for(var h in this.DOMAttrs){var l=k[this.DOMAttrs[h]];
if(l){i+=" "+h+"="+this.parse(l,"attribute")
}}return i+m+j+"/"+g+m
},functionArgs:function(i){var g=i.length;
if(!g){return""
}var h=Array(g);
while(g--){h[g]=String.fromCharCode(97+g)
}return" "+h.join(", ")+" "
},key:a,functionCode:"[code]",attribute:a,string:a,date:a,regexp:b,number:b,"boolean":b},DOMAttrs:{id:"id",name:"name","class":"className"},HTML:false,indentChar:"   ",multiline:true}
})();