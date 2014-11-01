CodeMirror.defineMode("gfm",function(c,h){var f=CodeMirror.getMode(c,"markdown");
var a={html:"htmlmixed",js:"javascript",json:"application/json",c:"text/x-csrc","c++":"text/x-c++src",java:"text/x-java",csharp:"text/x-csharp","c#":"text/x-csharp"};
var b=(function(){var l,p={},j={},o;
var n=CodeMirror.listModes();
for(l=0;
l<n.length;
l++){p[n[l]]=n[l]
}var m=CodeMirror.listMIMEs();
for(l=0;
l<m.length;
l++){o=m[l].mime;
j[o]=m[l].mime
}for(var k in a){if(a[k] in p||a[k] in j){p[k]=a[k]
}}return function(i){return p[i]?CodeMirror.getMode(c,p[i]):null
}
}());
function e(j,i){if(j.sol()&&j.match(/^```([\w+#]*)/)){i.localMode=b(RegExp.$1);
if(i.localMode){i.localState=i.localMode.startState()
}i.token=d;
return"code"
}return f.token(j,i.mdState)
}function d(j,i){if(j.sol()&&j.match(/^```/)){i.localMode=i.localState=null;
i.token=e;
return"code"
}else{if(i.localMode){return i.localMode.token(j,i.localState)
}else{j.skipToEnd();
return"code"
}}}function g(l,j){var i;
if(l.match(/^\w+:\/\/\S+/)){return"link"
}if(l.match(/^[^\[*\\<>` _][^\[*\\<>` ]*[^\[*\\<>` _]/)){return f.getType(j)
}if(i=l.match(/^[^\[*\\<>` ]+/)){var k=i[0];
if(k[0]==="_"&&k[k.length-1]==="_"){l.backUp(k.length);
return undefined
}return f.getType(j)
}if(l.eatSpace()){return null
}}return{startState:function(){var i=f.startState();
i.text=g;
return{token:e,mode:"markdown",mdState:i,localMode:null,localState:null}
},copyState:function(i){return{token:i.token,mdState:CodeMirror.copyState(f,i.mdState),localMode:i.localMode,localState:i.localMode?CodeMirror.copyState(i.localMode,i.localState):null}
},token:function(k,j){var i;
if((i=k.peek())!=undefined&&i=="["){k.next();
if((i=k.peek())==undefined||i!="["){k.backUp(1);
return j.token(k,j)
}while((i=k.next())!=undefined&&i!="]"){}if(i=="]"&&(i=k.next())!=undefined&&i=="]"){return"link"
}k.backUp(1)
}if(k.match(/^\$[^\$]+\$/)){return"string"
}if(k.match(/^\\\((.*?)\\\)/)){return"string"
}if(k.match(/^\$\$[^\$]+\$\$/)){return"string"
}if(k.match(/^\\\[(.*?)\\\]/)){return"string"
}return j.token(k,j)
},innerMode:function(i){if(i.token==e){return{state:i.mdState,mode:f}
}else{return{state:i.localState,mode:i.localMode}
}}}
},"markdown");