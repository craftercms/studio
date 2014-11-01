CodeMirror.defineMode("yaml",function(){var a=["true","false","on","off","yes","no"];
var b=new RegExp("\\b(("+a.join(")|(")+"))$","i");
return{token:function(f,e){var d=f.peek();
var c=e.escaped;
e.escaped=false;
if(d=="#"){f.skipToEnd();
return"comment"
}if(e.literal&&f.indentation()>e.keyCol){f.skipToEnd();
return"string"
}else{if(e.literal){e.literal=false
}}if(f.sol()){e.keyCol=0;
e.pair=false;
e.pairStart=false;
if(f.match(/---/)){return"def"
}if(f.match(/\.\.\./)){return"def"
}if(f.match(/\s*-\s+/)){return"meta"
}}if(!e.pair&&f.match(/^\s*([a-z0-9\._-])+(?=\s*:)/i)){e.pair=true;
e.keyCol=f.indentation();
return"atom"
}if(e.pair&&f.match(/^:\s*/)){e.pairStart=true;
return"meta"
}if(f.match(/^(\{|\}|\[|\])/)){if(d=="{"){e.inlinePairs++
}else{if(d=="}"){e.inlinePairs--
}else{if(d=="["){e.inlineList++
}else{e.inlineList--
}}}return"meta"
}if(e.inlineList>0&&!c&&d==","){f.next();
return"meta"
}if(e.inlinePairs>0&&!c&&d==","){e.keyCol=0;
e.pair=false;
e.pairStart=false;
f.next();
return"meta"
}if(e.pairStart){if(f.match(/^\s*(\||\>)\s*/)){e.literal=true;
return"meta"
}if(f.match(/^\s*(\&|\*)[a-z0-9\._-]+\b/i)){return"variable-2"
}if(e.inlinePairs==0&&f.match(/^\s*-?[0-9\.\,]+\s?$/)){return"number"
}if(e.inlinePairs>0&&f.match(/^\s*-?[0-9\.\,]+\s?(?=(,|}))/)){return"number"
}if(f.match(b)){return"keyword"
}}e.pairStart=false;
e.escaped=(d=="\\");
f.next();
return null
},startState:function(){return{pair:false,pairStart:false,keyCol:0,inlinePairs:0,inlineList:0,literal:false,escaped:false}
}}
});
CodeMirror.defineMIME("text/x-yaml","yaml");