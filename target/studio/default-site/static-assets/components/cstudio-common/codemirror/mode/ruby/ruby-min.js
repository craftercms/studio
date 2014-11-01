CodeMirror.defineMode("ruby",function(e,h){function b(s){var r={};
for(var p=0,q=s.length;
p<q;
++p){r[s[p]]=true
}return r
}var j=b(["alias","and","BEGIN","begin","break","case","class","def","defined?","do","else","elsif","END","end","ensure","false","for","if","in","module","next","not","or","redo","rescue","retry","return","self","super","then","true","undef","unless","until","when","while","yield","nil","raise","throw","catch","fail","loop","callcc","caller","lambda","proc","public","protected","private","require","load","require_relative","extend","autoload"]);
var i=b(["def","class","case","for","while","do","module","then","catch","loop","proc","begin"]);
var d=b(["end","until"]);
var c={"[":"]","{":"}","(":")"};
var l;
function a(o,q,p){p.tokenize.push(o);
return o(q,p)
}function f(t,r){l=null;
if(t.sol()&&t.match("=begin")&&t.eol()){r.tokenize.push(k);
return"comment"
}if(t.eatSpace()){return null
}var q=t.next(),o;
if(q=="`"||q=="'"||q=='"'||(q=="/"&&!t.eol()&&t.peek()!=" ")){return a(m(q,"string",q=='"'||q=="`"),t,r)
}else{if(q=="%"){var p,u=false;
if(t.eat("s")){p="atom"
}else{if(t.eat(/[WQ]/)){p="string";
u=true
}else{if(t.eat(/[wxqr]/)){p="string"
}}}var s=t.eat(/[^\w\s]/);
if(!s){return"operator"
}if(c.propertyIsEnumerable(s)){s=c[s]
}return a(m(s,p,u,true),t,r)
}else{if(q=="#"){t.skipToEnd();
return"comment"
}else{if(q=="<"&&(o=t.match(/^<-?[\`\"\']?([a-zA-Z_?]\w*)[\`\"\']?(?:;|$)/))){return a(g(o[1]),t,r)
}else{if(q=="0"){if(t.eat("x")){t.eatWhile(/[\da-fA-F]/)
}else{if(t.eat("b")){t.eatWhile(/[01]/)
}else{t.eatWhile(/[0-7]/)
}}return"number"
}else{if(/\d/.test(q)){t.match(/^[\d_]*(?:\.[\d_]+)?(?:[eE][+\-]?[\d_]+)?/);
return"number"
}else{if(q=="?"){while(t.match(/^\\[CM]-/)){}if(t.eat("\\")){t.eatWhile(/\w/)
}else{t.next()
}return"string"
}else{if(q==":"){if(t.eat("'")){return a(m("'","atom",false),t,r)
}if(t.eat('"')){return a(m('"',"atom",true),t,r)
}t.eatWhile(/[\w\?]/);
return"atom"
}else{if(q=="@"){t.eat("@");
t.eatWhile(/[\w\?]/);
return"variable-2"
}else{if(q=="$"){t.next();
t.eatWhile(/[\w\?]/);
return"variable-3"
}else{if(/\w/.test(q)){t.eatWhile(/[\w\?]/);
if(t.eat(":")){return"atom"
}return"ident"
}else{if(q=="|"&&(r.varList||r.lastTok=="{"||r.lastTok=="do")){l="|";
return null
}else{if(/[\(\)\[\]{}\\;]/.test(q)){l=q;
return null
}else{if(q=="-"&&t.eat(">")){return"arrow"
}else{if(/[=+\-\/*:\.^%<>~|]/.test(q)){t.eatWhile(/[=+\-\/*:\.^%<>~|]/);
return"operator"
}else{return null
}}}}}}}}}}}}}}}}function n(){var o=1;
return function(q,p){if(q.peek()=="}"){o--;
if(o==0){p.tokenize.pop();
return p.tokenize[p.tokenize.length-1](q,p)
}}else{if(q.peek()=="{"){o++
}}return f(q,p)
}
}function m(o,p,r,q){return function(v,t){var u=false,s;
while((s=v.next())!=null){if(s==o&&(q||!u)){t.tokenize.pop();
break
}if(r&&s=="#"&&!u&&v.eat("{")){t.tokenize.push(n(arguments.callee));
break
}u=!u&&s=="\\"
}return p
}
}function g(o){return function(q,p){if(q.match(o)){p.tokenize.pop()
}else{q.skipToEnd()
}return"string"
}
}function k(p,o){if(p.sol()&&p.match("=end")&&p.eol()){o.tokenize.pop()
}p.skipToEnd();
return"comment"
}return{startState:function(){return{tokenize:[f],indented:0,context:{type:"top",indented:-e.indentUnit},continuedLine:false,lastTok:null,varList:false}
},token:function(s,p){if(s.sol()){p.indented=s.indentation()
}var o=p.tokenize[p.tokenize.length-1](s,p),r;
if(o=="ident"){var q=s.current();
o=j.propertyIsEnumerable(s.current())?"keyword":/^[A-Z]/.test(q)?"tag":(p.lastTok=="def"||p.lastTok=="class"||p.varList)?"def":"variable";
if(i.propertyIsEnumerable(q)){r="indent"
}else{if(d.propertyIsEnumerable(q)){r="dedent"
}else{if((q=="if"||q=="unless")&&s.column()==s.indentation()){r="indent"
}}}}if(l||(o&&o!="comment")){p.lastTok=q||l||o
}if(l=="|"){p.varList=!p.varList
}if(r=="indent"||/[\(\[\{]/.test(l)){p.context={prev:p.context,type:l||o,indented:p.indented}
}else{if((r=="dedent"||/[\)\]\}]/.test(l))&&p.context.prev){p.context=p.context.prev
}}if(s.eol()){p.continuedLine=(l=="\\"||o=="operator")
}return o
},indent:function(s,o){if(s.tokenize[s.tokenize.length-1]!=f){return 0
}var r=o&&o.charAt(0);
var q=s.context;
var p=q.type==c[r]||q.type=="keyword"&&/^(?:end|until|else|elsif|when|rescue)\b/.test(o);
return q.indented+(p?0:e.indentUnit)+(s.continuedLine?e.indentUnit:0)
},electricChars:"}de"}
});
CodeMirror.defineMIME("text/x-ruby","ruby");