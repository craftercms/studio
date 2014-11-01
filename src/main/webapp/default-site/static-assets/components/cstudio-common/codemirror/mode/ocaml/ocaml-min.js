CodeMirror.defineMode("ocaml",function(a){var d={"true":"atom","false":"atom",let:"keyword",rec:"keyword","in":"keyword",of:"keyword",and:"keyword",succ:"keyword","if":"keyword",then:"keyword","else":"keyword","for":"keyword",to:"keyword","while":"keyword","do":"keyword",done:"keyword",fun:"keyword","function":"keyword",val:"keyword",type:"keyword",mutable:"keyword",match:"keyword","with":"keyword","try":"keyword",raise:"keyword",begin:"keyword",end:"keyword",open:"builtin",trace:"builtin",ignore:"builtin",exit:"builtin",print_string:"builtin",print_endline:"builtin"};
function e(j,h){var g=j.sol();
var f=j.next();
if(f==='"'){h.tokenize=c;
return h.tokenize(j,h)
}if(f==="("){if(j.eat("*")){h.commentLevel++;
h.tokenize=b;
return h.tokenize(j,h)
}}if(f==="~"){j.eatWhile(/\w/);
return"variable-2"
}if(f==="`"){j.eatWhile(/\w/);
return"quote"
}if(/\d/.test(f)){j.eatWhile(/[\d]/);
if(j.eat(".")){j.eatWhile(/[\d]/)
}return"number"
}if(/[+\-*&%=<>!?|]/.test(f)){return"operator"
}j.eatWhile(/\w/);
var i=j.current();
return d[i]||"variable"
}function c(j,h){var g,f=false,i=false;
while((g=j.next())!=null){if(g==='"'&&!i){f=true;
break
}i=!i&&g==="\\"
}if(f&&!i){h.tokenize=e
}return"string"
}function b(i,h){var g,f;
while(h.commentLevel>0&&(f=i.next())!=null){if(g==="("&&f==="*"){h.commentLevel++
}if(g==="*"&&f===")"){h.commentLevel--
}g=f
}if(h.commentLevel<=0){h.tokenize=e
}return"comment"
}return{startState:function(){return{tokenize:e,commentLevel:0}
},token:function(g,f){if(g.eatSpace()){return null
}return f.tokenize(g,f)
}}
});
CodeMirror.defineMIME("text/x-ocaml","ocaml");