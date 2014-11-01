CodeMirror.defineMode("spec",function(b,g){var e=/^(i386|i586|i686|x86_64|ppc64|ppc|ia64|s390x|s390|sparc64|sparcv9|sparc|noarch|alphaev6|alpha|hppa|mipsel)/;
var c=/^(Name|Version|Release|License|Summary|Url|Group|Source|BuildArch|BuildRequires|BuildRoot|AutoReqProv|Provides|Requires(\(\w+\))?|Obsoletes|Conflicts|Recommends|Source\d*|Patch\d*|ExclusiveArch|NoSource|Supplements):/;
var f=/^%(debug_package|package|description|prep|build|install|files|clean|changelog|preun|postun|pre|post|triggerin|triggerun|pretrans|posttrans|verifyscript|check|triggerpostun|triggerprein|trigger)/;
var h=/^%(ifnarch|ifarch|if)/;
var d=/^%(else|endif)/;
var a=/^(\!|\?|\<\=|\<|\>\=|\>|\=\=|\&\&|\|\|)/;
return{startState:function(){return{controlFlow:false,macroParameters:false,section:false}
},token:function(k,j){var i=k.peek();
if(i=="#"){k.skipToEnd();
return"comment"
}if(k.sol()){if(k.match(c)){return"preamble"
}if(k.match(f)){return"section"
}}if(k.match(/^\$\w+/)){return"def"
}if(k.match(/^\$\{\w+\}/)){return"def"
}if(k.match(d)){return"keyword"
}if(k.match(h)){j.controlFlow=true;
return"keyword"
}if(j.controlFlow){if(k.match(a)){return"operator"
}if(k.match(/^(\d+)/)){return"number"
}if(k.eol()){j.controlFlow=false
}}if(k.match(e)){return"number"
}if(k.match(/^%[\w]+/)){if(k.match(/^\(/)){j.macroParameters=true
}return"macro"
}if(j.macroParameters){if(k.match(/^\d+/)){return"number"
}if(k.match(/^\)/)){j.macroParameters=false;
return"macro"
}}if(k.match(/^%\{\??[\w \-]+\}/)){return"macro"
}k.next();
return null
}}
});
CodeMirror.defineMIME("text/x-rpm-spec","spec");