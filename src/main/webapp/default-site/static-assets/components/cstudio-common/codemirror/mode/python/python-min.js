CodeMirror.defineMode("python",function(l,u){var c="error";
function p(z){return new RegExp("^(("+z.join(")|(")+"))\\b")
}var o=new RegExp("^[\\+\\-\\*/%&|\\^~<>!]");
var q=new RegExp("^[\\(\\)\\[\\]\\{\\}@,:`=;\\.]");
var a=new RegExp("^((==)|(!=)|(<=)|(>=)|(<>)|(<<)|(>>)|(//)|(\\*\\*))");
var j=new RegExp("^((\\+=)|(\\-=)|(\\*=)|(%=)|(/=)|(&=)|(\\|=)|(\\^=))");
var w=new RegExp("^((//=)|(>>=)|(<<=)|(\\*\\*=))");
var i=new RegExp("^[_A-Za-z][_A-Za-z0-9]*");
var e=p(["and","or","not","is","in"]);
var t=["as","assert","break","class","continue","def","del","elif","else","except","finally","for","from","global","if","import","lambda","pass","raise","return","try","while","with","yield"];
var r=["abs","all","any","bin","bool","bytearray","callable","chr","classmethod","compile","complex","delattr","dict","dir","divmod","enumerate","eval","filter","float","format","frozenset","getattr","globals","hasattr","hash","help","hex","id","input","int","isinstance","issubclass","iter","len","list","locals","map","max","memoryview","min","next","object","oct","open","ord","pow","property","range","repr","reversed","round","set","setattr","slice","sorted","staticmethod","str","sum","super","tuple","type","vars","zip","__import__","NotImplemented","Ellipsis","__debug__"];
var d={builtins:["apply","basestring","buffer","cmp","coerce","execfile","file","intern","long","raw_input","reduce","reload","unichr","unicode","xrange","False","True","None"],keywords:["exec","print"]};
var b={builtins:["ascii","bytes","exec","print"],keywords:["nonlocal","False","True","None"]};
if(!!u.version&&parseInt(u.version,10)===3){t=t.concat(b.keywords);
r=r.concat(b.builtins);
var m=new RegExp("^(([rb]|(br))?('{3}|\"{3}|['\"]))","i")
}else{t=t.concat(d.keywords);
r=r.concat(d.builtins);
var m=new RegExp("^(([rub]|(ur)|(br))?('{3}|\"{3}|['\"]))","i")
}var k=p(t);
var f=p(r);
var v=null;
function y(F,E){if(F.sol()){var A=E.scopes[0].offset;
if(F.eatSpace()){var C=F.indentation();
if(C>A){v="indent"
}else{if(C<A){v="dedent"
}}return null
}else{if(A>0){g(F,E)
}}}if(F.eatSpace()){return null
}var D=F.peek();
if(D==="#"){F.skipToEnd();
return"comment"
}if(F.match(/^[0-9\.]/,false)){var B=false;
if(F.match(/^\d*\.\d+(e[\+\-]?\d+)?/i)){B=true
}if(F.match(/^\d+\.\d*/)){B=true
}if(F.match(/^\.\d+/)){B=true
}if(B){F.eat(/J/i);
return"number"
}var z=false;
if(F.match(/^0x[0-9a-f]+/i)){z=true
}if(F.match(/^0b[01]+/i)){z=true
}if(F.match(/^0o[0-7]+/i)){z=true
}if(F.match(/^[1-9]\d*(e[\+\-]?\d+)?/)){F.eat(/J/i);
z=true
}if(F.match(/^0(?![\dx])/i)){z=true
}if(z){F.eat(/L/i);
return"number"
}}if(F.match(m)){E.tokenize=s(F.current());
return E.tokenize(F,E)
}if(F.match(w)||F.match(j)){return null
}if(F.match(a)||F.match(o)||F.match(e)){return"operator"
}if(F.match(q)){return null
}if(F.match(k)){return"keyword"
}if(F.match(f)){return"builtin"
}if(F.match(i)){return"variable"
}F.next();
return c
}function s(z){while("rub".indexOf(z.charAt(0).toLowerCase())>=0){z=z.substr(1)
}var B=z.length==1;
var A="string";
return function C(E,D){while(!E.eol()){E.eatWhile(/[^'"\\]/);
if(E.eat("\\")){E.next();
if(B&&E.eol()){return A
}}else{if(E.match(z)){D.tokenize=y;
return A
}else{E.eat(/['"]/)
}}}if(B){if(u.singleLineStringErrors){return c
}else{D.tokenize=y
}}return A
}
}function n(D,C,B){B=B||"py";
var z=0;
if(B==="py"){if(C.scopes[0].type!=="py"){C.scopes[0].offset=D.indentation();
return
}for(var A=0;
A<C.scopes.length;
++A){if(C.scopes[A].type==="py"){z=C.scopes[A].offset+l.indentUnit;
break
}}}else{z=D.column()+D.current().length
}C.scopes.unshift({offset:z,type:B})
}function g(E,D,B){B=B||"py";
if(D.scopes.length==1){return
}if(D.scopes[0].type==="py"){var A=E.indentation();
var C=-1;
for(var z=0;
z<D.scopes.length;
++z){if(A===D.scopes[z].offset){C=z;
break
}}if(C===-1){return true
}while(D.scopes[0].offset!==A){D.scopes.shift()
}return false
}else{if(B==="py"){D.scopes[0].offset=E.indentation();
return false
}else{if(D.scopes[0].type!=B){return true
}D.scopes.shift();
return false
}}}function x(D,B){v=null;
var A=B.tokenize(D,B);
var C=D.current();
if(C==="."){A=D.match(i,false)?null:c;
if(A===null&&B.lastToken==="meta"){A="meta"
}return A
}if(C==="@"){return D.match(i,false)?"meta":c
}if((A==="variable"||A==="builtin")&&B.lastToken==="meta"){A="meta"
}if(C==="pass"||C==="return"){B.dedent+=1
}if(C==="lambda"){B.lambda=true
}if((C===":"&&!B.lambda&&B.scopes[0].type=="py")||v==="indent"){n(D,B)
}var z="[({".indexOf(C);
if(z!==-1){n(D,B,"])}".slice(z,z+1))
}if(v==="dedent"){if(g(D,B)){return c
}}z="])}".indexOf(C);
if(z!==-1){if(g(D,B,C)){return c
}}if(B.dedent>0&&D.eol()&&B.scopes[0].type=="py"){if(B.scopes.length>1){B.scopes.shift()
}B.dedent-=1
}return A
}var h={startState:function(z){return{tokenize:y,scopes:[{offset:z||0,type:"py"}],lastToken:null,lambda:false,dedent:0}
},token:function(B,A){var z=x(B,A);
A.lastToken=z;
if(B.eol()&&B.lambda){A.lambda=false
}return z
},indent:function(A,z){if(A.tokenize!=y){return 0
}return A.scopes[0].offset
}};
return h
});
CodeMirror.defineMIME("text/x-python","python");