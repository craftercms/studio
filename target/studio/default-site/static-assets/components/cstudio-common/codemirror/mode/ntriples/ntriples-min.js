CodeMirror.defineMode("ntriples",function(){var b={PRE_SUBJECT:0,WRITING_SUB_URI:1,WRITING_BNODE_URI:2,PRE_PRED:3,WRITING_PRED_URI:4,PRE_OBJ:5,WRITING_OBJ_URI:6,WRITING_OBJ_BNODE:7,WRITING_OBJ_LITERAL:8,WRITING_LIT_LANG:9,WRITING_LIT_TYPE:10,POST_OBJ:11,ERROR:12};
function a(f,h){var e=f.location;
var g;
if(e==b.PRE_SUBJECT&&h=="<"){g=b.WRITING_SUB_URI
}else{if(e==b.PRE_SUBJECT&&h=="_"){g=b.WRITING_BNODE_URI
}else{if(e==b.PRE_PRED&&h=="<"){g=b.WRITING_PRED_URI
}else{if(e==b.PRE_OBJ&&h=="<"){g=b.WRITING_OBJ_URI
}else{if(e==b.PRE_OBJ&&h=="_"){g=b.WRITING_OBJ_BNODE
}else{if(e==b.PRE_OBJ&&h=='"'){g=b.WRITING_OBJ_LITERAL
}else{if(e==b.WRITING_SUB_URI&&h==">"){g=b.PRE_PRED
}else{if(e==b.WRITING_BNODE_URI&&h==" "){g=b.PRE_PRED
}else{if(e==b.WRITING_PRED_URI&&h==">"){g=b.PRE_OBJ
}else{if(e==b.WRITING_OBJ_URI&&h==">"){g=b.POST_OBJ
}else{if(e==b.WRITING_OBJ_BNODE&&h==" "){g=b.POST_OBJ
}else{if(e==b.WRITING_OBJ_LITERAL&&h=='"'){g=b.POST_OBJ
}else{if(e==b.WRITING_LIT_LANG&&h==" "){g=b.POST_OBJ
}else{if(e==b.WRITING_LIT_TYPE&&h==">"){g=b.POST_OBJ
}else{if(e==b.WRITING_OBJ_LITERAL&&h=="@"){g=b.WRITING_LIT_LANG
}else{if(e==b.WRITING_OBJ_LITERAL&&h=="^"){g=b.WRITING_LIT_TYPE
}else{if(h==" "&&(e==b.PRE_SUBJECT||e==b.PRE_PRED||e==b.PRE_OBJ||e==b.POST_OBJ)){g=e
}else{if(e==b.POST_OBJ&&h=="."){g=b.PRE_SUBJECT
}else{g=b.ERROR
}}}}}}}}}}}}}}}}}}f.location=g
}var d=function(e){return e!=" "
};
var c=function(e){return e!=">"
};
return{startState:function(){return{location:b.PRE_SUBJECT,uris:[],anchors:[],bnodes:[],langs:[],types:[]}
},token:function(j,i){var g=j.next();
if(g=="<"){a(i,g);
var l="";
j.eatWhile(function(m){if(m!="#"&&m!=">"){l+=m;
return true
}return false
});
i.uris.push(l);
if(j.match("#",false)){return"variable"
}j.next();
a(i,">");
return"variable"
}if(g=="#"){var k="";
j.eatWhile(function(m){if(m!=">"&&m!=" "){k+=m;
return true
}return false
});
i.anchors.push(k);
return"variable-2"
}if(g==">"){a(i,">");
return"variable"
}if(g=="_"){a(i,g);
var f="";
j.eatWhile(function(m){if(m!=" "){f+=m;
return true
}return false
});
i.bnodes.push(f);
j.next();
a(i," ");
return"builtin"
}if(g=='"'){a(i,g);
j.eatWhile(function(m){return m!='"'
});
j.next();
if(j.peek()!="@"&&j.peek()!="^"){a(i,'"')
}return"string"
}if(g=="@"){a(i,"@");
var e="";
j.eatWhile(function(m){if(m!=" "){e+=m;
return true
}return false
});
i.langs.push(e);
j.next();
a(i," ");
return"string-2"
}if(g=="^"){j.next();
a(i,"^");
var h="";
j.eatWhile(function(m){if(m!=">"){h+=m;
return true
}return false
});
i.types.push(h);
j.next();
a(i,">");
return"variable"
}if(g==" "){a(i,g)
}if(g=="."){a(i,g)
}}}
});
CodeMirror.defineMIME("text/n-triples","ntriples");