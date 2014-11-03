CodeMirror.defineMode("sieve",function(a){function f(m){var k={},l=m.split(" ");
for(var j=0;
j<l.length;
++j){k[l[j]]=true
}return k
}var c=f("if elsif else stop require");
var e=f("true false not");
var d=a.indentUnit;
function b(m,k){var j=m.next();
if(j=="/"&&m.eat("*")){k.tokenize=h;
return h(m,k)
}if(j==="#"){m.skipToEnd();
return"comment"
}if(j=='"'){k.tokenize=g(j);
return k.tokenize(m,k)
}if(j==="{"){k._indent++;
return null
}if(j==="}"){k._indent--;
return null
}if(/[{}\(\),;]/.test(j)){return null
}if(/\d/.test(j)){m.eatWhile(/[\d]/);
m.eat(/[KkMmGg]/);
return"number"
}if(j==":"){m.eatWhile(/[a-zA-Z_]/);
m.eatWhile(/[a-zA-Z0-9_]/);
return"operator"
}m.eatWhile(/[\w\$_]/);
var l=m.current();
if((l=="text")&&m.eat(":")){k.tokenize=i;
return"string"
}if(c.propertyIsEnumerable(l)){return"keyword"
}if(e.propertyIsEnumerable(l)){return"atom"
}}function i(k,j){j._multiLineString=true;
if(!k.sol()){k.eatSpace();
if(k.peek()=="#"){k.skipToEnd();
return"comment"
}k.skipToEnd();
return"string"
}if((k.next()==".")&&(k.eol())){j._multiLineString=false;
j.tokenize=b
}return"string"
}function h(m,l){var j=false,k;
while((k=m.next())!=null){if(j&&k=="/"){l.tokenize=b;
break
}j=(k=="*")
}return"comment"
}function g(j){return function(n,l){var m=false,k;
while((k=n.next())!=null){if(k==j&&!m){break
}m=!m&&k=="\\"
}if(!m){l.tokenize=b
}return"string"
}
}return{startState:function(j){return{tokenize:b,baseIndent:j||0,_indent:0}
},token:function(k,j){if(k.eatSpace()){return null
}return(j.tokenize||b)(k,j)
},indent:function(k,j){return k.baseIndent+k._indent*d
},electricChars:"}"}
});
CodeMirror.defineMIME("application/sieve","sieve");