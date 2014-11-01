(function(){function e(k,m){for(var l=0,n=k.length;
l<n;
++l){m(k[l])
}}function c(k,m){if(!Array.prototype.indexOf){var l=k.length;
while(l--){if(k[l]===m){return true
}}return false
}return k.indexOf(m)!=-1
}function f(o,n,r){var p=o.getCursor(),m=r(o,p),k=m;
if(!/^[\w$_]*$/.test(m.string)){m=k={start:p.ch,end:p.ch,string:"",state:m.state,className:m.string=="."?"property":null}
}while(k.className=="property"){k=r(o,{line:p.line,ch:k.start});
if(k.string!="."){return
}k=r(o,{line:p.line,ch:k.start});
if(k.string==")"){var q=1;
do{k=r(o,{line:p.line,ch:k.start});
switch(k.string){case")":q++;
break;
case"(":q--;
break;
default:break
}}while(q>0);
k=r(o,{line:p.line,ch:k.start});
if(k.className=="variable"){k.className="function"
}else{return
}}if(!l){var l=[]
}l.push(k)
}return{list:g(m,l,n),from:{line:p.line,ch:m.start},to:{line:p.line,ch:m.end}}
}CodeMirror.javascriptHint=function(k){return f(k,b,function(l,m){return l.getTokenAt(m)
})
};
function a(l,m){var k=l.getTokenAt(m);
if(m.ch==k.start+1&&k.string.charAt(0)=="."){k.end=k.start;
k.string=".";
k.className="property"
}else{if(/^\.[\w$_]*$/.test(k.string)){k.className="property";
k.start++;
k.string=k.string.replace(/\./,"")
}}return k
}CodeMirror.coffeescriptHint=function(k){return f(k,i,a)
};
var h=("charAt charCodeAt indexOf lastIndexOf substring substr slice trim trimLeft trimRight toUpperCase toLowerCase split concat match replace search").split(" ");
var j=("length concat join splice push pop shift unshift slice reverse sort indexOf lastIndexOf every some filter forEach map reduce reduceRight ").split(" ");
var d="prototype apply call bind".split(" ");
var b=("break case catch continue debugger default delete do else false finally for function if in instanceof new null return switch throw true try typeof var void while with").split(" ");
var i=("and break catch class continue delete do else extends false finally for if in instanceof isnt new no not null of off on or return switch then throw true try typeof until void while with yes").split(" ");
function g(o,m,r){var t=[],l=o.string;
function p(u){if(u.indexOf(l)==0&&!c(t,u)){t.push(u)
}}function n(v){if(typeof v=="string"){e(h,p)
}else{if(v instanceof Array){e(j,p)
}else{if(v instanceof Function){e(d,p)
}}}for(var u in v){p(u)
}}if(m){var q=m.pop(),k;
if(q.className=="variable"){k=window[q.string]
}else{if(q.className=="string"){k=""
}else{if(q.className=="atom"){k=1
}else{if(q.className=="function"){if(window.jQuery!=null&&(q.string=="$"||q.string=="jQuery")&&(typeof window.jQuery=="function")){k=window.jQuery()
}else{if(window._!=null&&(q.string=="_")&&(typeof window._=="function")){k=window._()
}}}}}}while(k!=null&&m.length){k=k[m.pop().string]
}if(k!=null){n(k)
}}else{for(var s=o.state.localVars;
s;
s=s.next){p(s.name)
}n(window);
e(r,p)
}return t
}})();