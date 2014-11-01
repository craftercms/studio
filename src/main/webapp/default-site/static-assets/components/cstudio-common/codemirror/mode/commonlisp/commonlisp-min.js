CodeMirror.defineMode("commonlisp",function(b){var i=/^with|^def|^do|^prog|case$|^cond$|bind$|when$|unless$/;
var f=/^(?:[+\-]?(?:\d+|\d*\.\d+)(?:[efd][+\-]?\d+)?|[+\-]?\d+(?:\/[+\-]?\d+)?|#b[+\-]?[01]+|#o[+\-]?[0-7]+|#x[+\-]?[\da-f]+)/;
var c=/[^\s'`,@()\[\]";]/;
var g;
function e(k){var j;
while(j=k.next()){if(j=="\\"){k.next()
}else{if(!c.test(j)){k.backUp(1);
break
}}}return k.current()
}function a(m,l){if(m.eatSpace()){g="ws";
return null
}if(m.match(f)){return"number"
}var k=m.next();
if(k=="\\"){k=m.next()
}if(k=='"'){return(l.tokenize=d)(m,l)
}else{if(k=="("){g="open";
return"bracket"
}else{if(k==")"||k=="]"){g="close";
return"bracket"
}else{if(k==";"){m.skipToEnd();
g="ws";
return"comment"
}else{if(/['`,@]/.test(k)){return null
}else{if(k=="|"){if(m.skipTo("|")){m.next();
return"symbol"
}else{m.skipToEnd();
return"error"
}}else{if(k=="#"){var k=m.next();
if(k=="["){g="open";
return"bracket"
}else{if(/[+\-=\.']/.test(k)){return null
}else{if(/\d/.test(k)&&m.match(/^\d*#/)){return null
}else{if(k=="|"){return(l.tokenize=h)(m,l)
}else{if(k==":"){e(m);
return"meta"
}else{return"error"
}}}}}}else{var j=e(m);
if(j=="."){return null
}g="symbol";
if(j=="nil"||j=="t"){return"atom"
}if(j.charAt(0)==":"){return"keyword"
}if(j.charAt(0)=="&"){return"variable-2"
}return"variable"
}}}}}}}}function d(m,k){var l=false,j;
while(j=m.next()){if(j=='"'&&!l){k.tokenize=a;
break
}l=!l&&j=="\\"
}return"string"
}function h(m,l){var j,k;
while(j=m.next()){if(j=="#"&&k=="|"){l.tokenize=a;
break
}k=j
}g="ws";
return"comment"
}return{startState:function(){return{ctx:{prev:null,start:0,indentTo:0},tokenize:a}
},token:function(l,k){if(l.sol()&&typeof k.ctx.indentTo!="number"){k.ctx.indentTo=k.ctx.start+1
}g=null;
var j=k.tokenize(l,k);
if(g!="ws"){if(k.ctx.indentTo==null){if(g=="symbol"&&i.test(l.current())){k.ctx.indentTo=k.ctx.start+b.indentUnit
}else{k.ctx.indentTo="next"
}}else{if(k.ctx.indentTo=="next"){k.ctx.indentTo=l.column()
}}}if(g=="open"){k.ctx={prev:k.ctx,start:l.column(),indentTo:null}
}else{if(g=="close"){k.ctx=k.ctx.prev||k.ctx
}}return j
},indent:function(l,j){var k=l.ctx.indentTo;
return typeof k=="number"?k:l.ctx.start+1
}}
});
CodeMirror.defineMIME("text/x-common-lisp","commonlisp");