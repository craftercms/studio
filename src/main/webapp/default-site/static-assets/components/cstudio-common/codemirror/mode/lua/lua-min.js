CodeMirror.defineMode("lua",function(e,g){var i=e.indentUnit;
function b(p){return new RegExp("^(?:"+p.join("|")+")","i")
}function f(p){return new RegExp("^(?:"+p.join("|")+")$","i")
}var l=f(g.specials||[]);
var a=f(["_G","_VERSION","assert","collectgarbage","dofile","error","getfenv","getmetatable","ipairs","load","loadfile","loadstring","module","next","pairs","pcall","print","rawequal","rawget","rawset","require","select","setfenv","setmetatable","tonumber","tostring","type","unpack","xpcall","coroutine.create","coroutine.resume","coroutine.running","coroutine.status","coroutine.wrap","coroutine.yield","debug.debug","debug.getfenv","debug.gethook","debug.getinfo","debug.getlocal","debug.getmetatable","debug.getregistry","debug.getupvalue","debug.setfenv","debug.sethook","debug.setlocal","debug.setmetatable","debug.setupvalue","debug.traceback","close","flush","lines","read","seek","setvbuf","write","io.close","io.flush","io.input","io.lines","io.open","io.output","io.popen","io.read","io.stderr","io.stdin","io.stdout","io.tmpfile","io.type","io.write","math.abs","math.acos","math.asin","math.atan","math.atan2","math.ceil","math.cos","math.cosh","math.deg","math.exp","math.floor","math.fmod","math.frexp","math.huge","math.ldexp","math.log","math.log10","math.max","math.min","math.modf","math.pi","math.pow","math.rad","math.random","math.randomseed","math.sin","math.sinh","math.sqrt","math.tan","math.tanh","os.clock","os.date","os.difftime","os.execute","os.exit","os.getenv","os.remove","os.rename","os.setlocale","os.time","os.tmpname","package.cpath","package.loaded","package.loaders","package.loadlib","package.path","package.preload","package.seeall","string.byte","string.char","string.dump","string.find","string.format","string.gmatch","string.gsub","string.len","string.lower","string.match","string.rep","string.reverse","string.sub","string.upper","table.concat","table.insert","table.maxn","table.remove","table.sort"]);
var h=f(["and","break","elseif","false","nil","not","or","return","true","function","end","if","then","else","do","while","repeat","until","for","in","local"]);
var o=f(["function","if","repeat","do","\\(","{"]);
var m=f(["end","until","\\)","}"]);
var c=b(["end","until","\\)","}","else","elseif"]);
function d(p){var q=0;
while(p.eat("=")){++q
}p.eat("[");
return q
}function k(r,q){var p=r.next();
if(p=="-"&&r.eat("-")){if(r.eat("[")){return(q.cur=n(d(r),"comment"))(r,q)
}r.skipToEnd();
return"comment"
}if(p=='"'||p=="'"){return(q.cur=j(p))(r,q)
}if(p=="["&&/[\[=]/.test(r.peek())){return(q.cur=n(d(r),"string"))(r,q)
}if(/\d/.test(p)){r.eatWhile(/[\w.%]/);
return"number"
}if(/[\w_]/.test(p)){r.eatWhile(/[\w\\\-_.]/);
return"variable"
}return null
}function n(q,p){return function(u,t){var r=null,s;
while((s=u.next())!=null){if(r==null){if(s=="]"){r=0
}}else{if(s=="="){++r
}else{if(s=="]"&&r==q){t.cur=k;
break
}else{r=null
}}}}return p
}
}function j(p){return function(t,r){var s=false,q;
while((q=t.next())!=null){if(q==p&&!s){break
}s=!s&&q=="\\"
}if(!s){r.cur=k
}return"string"
}
}return{startState:function(p){return{basecol:p||0,indentDepth:0,cur:k}
},token:function(s,q){if(s.eatSpace()){return null
}var p=q.cur(s,q);
var r=s.current();
if(p=="variable"){if(h.test(r)){p="keyword"
}else{if(a.test(r)){p="builtin"
}else{if(l.test(r)){p="variable-2"
}}}}if((p!="comment")&&(p!="string")){if(o.test(r)){++q.indentDepth
}else{if(m.test(r)){--q.indentDepth
}}}return p
},indent:function(r,p){var q=c.test(p);
return r.basecol+i*(r.indentDepth-(q?1:0))
}}
});
CodeMirror.defineMIME("text/x-lua","lua");