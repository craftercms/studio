CodeMirror.defineMode("jinja2",function(b,a){var c=["block","endblock","for","endfor","in","true","false","loop","none","self","super","if","as","not","and","else","import","with","without","context"];
c=new RegExp("^(("+c.join(")|(")+"))\\b");
function e(h,g){var f=h.next();
if(f=="{"){if(f=h.eat(/\{|%|#/)){h.eat("-");
g.tokenize=d(f);
return"tag"
}}}function d(f){if(f=="{"){f="}"
}return function(i,h){var g=i.next();
if((g==f||(g=="-"&&i.eat(f)))&&i.eat("}")){h.tokenize=e;
return"tag"
}if(i.match(c)){return"keyword"
}return f=="#"?"comment":"string"
}
}return{startState:function(){return{tokenize:e}
},token:function(g,f){return f.tokenize(g,f)
}}
});