CodeMirror.defineMode("css",function(b){var e=b.indentUnit,h;
var n=d(["all","aural","braille","handheld","print","projection","screen","tty","tv","embossed"]);
var j=d(["width","min-width","max-width","height","min-height","max-height","device-width","min-device-width","max-device-width","device-height","min-device-height","max-device-height","aspect-ratio","min-aspect-ratio","max-aspect-ratio","device-aspect-ratio","min-device-aspect-ratio","max-device-aspect-ratio","color","min-color","max-color","color-index","min-color-index","max-color-index","monochrome","min-monochrome","max-monochrome","resolution","min-resolution","max-resolution","scan","grid"]);
var i=d(["align-content","align-items","align-self","alignment-adjust","alignment-baseline","anchor-point","animation","animation-delay","animation-direction","animation-duration","animation-iteration-count","animation-name","animation-play-state","animation-timing-function","appearance","azimuth","backface-visibility","background","background-attachment","background-clip","background-color","background-image","background-origin","background-position","background-repeat","background-size","baseline-shift","binding","bleed","bookmark-label","bookmark-level","bookmark-state","bookmark-target","border","border-bottom","border-bottom-color","border-bottom-left-radius","border-bottom-right-radius","border-bottom-style","border-bottom-width","border-collapse","border-color","border-image","border-image-outset","border-image-repeat","border-image-slice","border-image-source","border-image-width","border-left","border-left-color","border-left-style","border-left-width","border-radius","border-right","border-right-color","border-right-style","border-right-width","border-spacing","border-style","border-top","border-top-color","border-top-left-radius","border-top-right-radius","border-top-style","border-top-width","border-width","bottom","box-decoration-break","box-shadow","box-sizing","break-after","break-before","break-inside","caption-side","clear","clip","color","color-profile","column-count","column-fill","column-gap","column-rule","column-rule-color","column-rule-style","column-rule-width","column-span","column-width","columns","content","counter-increment","counter-reset","crop","cue","cue-after","cue-before","cursor","direction","display","dominant-baseline","drop-initial-after-adjust","drop-initial-after-align","drop-initial-before-adjust","drop-initial-before-align","drop-initial-size","drop-initial-value","elevation","empty-cells","fit","fit-position","flex","flex-basis","flex-direction","flex-flow","flex-grow","flex-shrink","flex-wrap","float","float-offset","font","font-feature-settings","font-family","font-kerning","font-language-override","font-size","font-size-adjust","font-stretch","font-style","font-synthesis","font-variant","font-variant-alternates","font-variant-caps","font-variant-east-asian","font-variant-ligatures","font-variant-numeric","font-variant-position","font-weight","grid-cell","grid-column","grid-column-align","grid-column-sizing","grid-column-span","grid-columns","grid-flow","grid-row","grid-row-align","grid-row-sizing","grid-row-span","grid-rows","grid-template","hanging-punctuation","height","hyphens","icon","image-orientation","image-rendering","image-resolution","inline-box-align","justify-content","left","letter-spacing","line-break","line-height","line-stacking","line-stacking-ruby","line-stacking-shift","line-stacking-strategy","list-style","list-style-image","list-style-position","list-style-type","margin","margin-bottom","margin-left","margin-right","margin-top","marker-offset","marks","marquee-direction","marquee-loop","marquee-play-count","marquee-speed","marquee-style","max-height","max-width","min-height","min-width","move-to","nav-down","nav-index","nav-left","nav-right","nav-up","opacity","order","orphans","outline","outline-color","outline-offset","outline-style","outline-width","overflow","overflow-style","overflow-wrap","overflow-x","overflow-y","padding","padding-bottom","padding-left","padding-right","padding-top","page","page-break-after","page-break-before","page-break-inside","page-policy","pause","pause-after","pause-before","perspective","perspective-origin","pitch","pitch-range","play-during","position","presentation-level","punctuation-trim","quotes","rendering-intent","resize","rest","rest-after","rest-before","richness","right","rotation","rotation-point","ruby-align","ruby-overhang","ruby-position","ruby-span","size","speak","speak-as","speak-header","speak-numeral","speak-punctuation","speech-rate","stress","string-set","tab-size","table-layout","target","target-name","target-new","target-position","text-align","text-align-last","text-decoration","text-decoration-color","text-decoration-line","text-decoration-skip","text-decoration-style","text-emphasis","text-emphasis-color","text-emphasis-position","text-emphasis-style","text-height","text-indent","text-justify","text-outline","text-shadow","text-space-collapse","text-transform","text-underline-position","text-wrap","top","transform","transform-origin","transform-style","transition","transition-delay","transition-duration","transition-property","transition-timing-function","unicode-bidi","vertical-align","visibility","voice-balance","voice-duration","voice-family","voice-pitch","voice-range","voice-rate","voice-stress","voice-volume","volume","white-space","widows","width","word-break","word-spacing","word-wrap","z-index"]);
var k=d(["black","silver","gray","white","maroon","red","purple","fuchsia","green","lime","olive","yellow","navy","blue","teal","aqua"]);
var g=d(["above","absolute","activeborder","activecaption","afar","after-white-space","ahead","alias","all","all-scroll","alternate","always","amharic","amharic-abegede","antialiased","appworkspace","arabic-indic","armenian","asterisks","auto","avoid","background","backwards","baseline","below","bidi-override","binary","bengali","blink","block","block-axis","bold","bolder","border","border-box","both","bottom","break-all","break-word","button","button-bevel","buttonface","buttonhighlight","buttonshadow","buttontext","cambodian","capitalize","caps-lock-indicator","caption","captiontext","caret","cell","center","checkbox","circle","cjk-earthly-branch","cjk-heavenly-stem","cjk-ideographic","clear","clip","close-quote","col-resize","collapse","compact","condensed","contain","content","content-box","context-menu","continuous","copy","cover","crop","cross","crosshair","currentcolor","cursive","dashed","decimal","decimal-leading-zero","default","default-button","destination-atop","destination-in","destination-out","destination-over","devanagari","disc","discard","document","dot-dash","dot-dot-dash","dotted","double","down","e-resize","ease","ease-in","ease-in-out","ease-out","element","ellipsis","embed","end","ethiopic","ethiopic-abegede","ethiopic-abegede-am-et","ethiopic-abegede-gez","ethiopic-abegede-ti-er","ethiopic-abegede-ti-et","ethiopic-halehame-aa-er","ethiopic-halehame-aa-et","ethiopic-halehame-am-et","ethiopic-halehame-gez","ethiopic-halehame-om-et","ethiopic-halehame-sid-et","ethiopic-halehame-so-et","ethiopic-halehame-ti-er","ethiopic-halehame-ti-et","ethiopic-halehame-tig","ew-resize","expanded","extra-condensed","extra-expanded","fantasy","fast","fill","fixed","flat","footnotes","forwards","from","geometricPrecision","georgian","graytext","groove","gujarati","gurmukhi","hand","hangul","hangul-consonant","hebrew","help","hidden","hide","higher","highlight","highlighttext","hiragana","hiragana-iroha","horizontal","hsl","hsla","icon","ignore","inactiveborder","inactivecaption","inactivecaptiontext","infinite","infobackground","infotext","inherit","initial","inline","inline-axis","inline-block","inline-table","inset","inside","intrinsic","invert","italic","justify","kannada","katakana","katakana-iroha","khmer","landscape","lao","large","larger","left","level","lighter","line-through","linear","lines","list-item","listbox","listitem","local","logical","loud","lower","lower-alpha","lower-armenian","lower-greek","lower-hexadecimal","lower-latin","lower-norwegian","lower-roman","lowercase","ltr","malayalam","match","media-controls-background","media-current-time-display","media-fullscreen-button","media-mute-button","media-play-button","media-return-to-realtime-button","media-rewind-button","media-seek-back-button","media-seek-forward-button","media-slider","media-sliderthumb","media-time-remaining-display","media-volume-slider","media-volume-slider-container","media-volume-sliderthumb","medium","menu","menulist","menulist-button","menulist-text","menulist-textfield","menutext","message-box","middle","min-intrinsic","mix","mongolian","monospace","move","multiple","myanmar","n-resize","narrower","navy","ne-resize","nesw-resize","no-close-quote","no-drop","no-open-quote","no-repeat","none","normal","not-allowed","nowrap","ns-resize","nw-resize","nwse-resize","oblique","octal","open-quote","optimizeLegibility","optimizeSpeed","oriya","oromo","outset","outside","overlay","overline","padding","padding-box","painted","paused","persian","plus-darker","plus-lighter","pointer","portrait","pre","pre-line","pre-wrap","preserve-3d","progress","push-button","radio","read-only","read-write","read-write-plaintext-only","relative","repeat","repeat-x","repeat-y","reset","reverse","rgb","rgba","ridge","right","round","row-resize","rtl","run-in","running","s-resize","sans-serif","scroll","scrollbar","se-resize","searchfield","searchfield-cancel-button","searchfield-decoration","searchfield-results-button","searchfield-results-decoration","semi-condensed","semi-expanded","separate","serif","show","sidama","single","skip-white-space","slide","slider-horizontal","slider-vertical","sliderthumb-horizontal","sliderthumb-vertical","slow","small","small-caps","small-caption","smaller","solid","somali","source-atop","source-in","source-out","source-over","space","square","square-button","start","static","status-bar","stretch","stroke","sub","subpixel-antialiased","super","sw-resize","table","table-caption","table-cell","table-column","table-column-group","table-footer-group","table-header-group","table-row","table-row-group","telugu","text","text-bottom","text-top","textarea","textfield","thai","thick","thin","threeddarkshadow","threedface","threedhighlight","threedlightshadow","threedshadow","tibetan","tigre","tigrinya-er","tigrinya-er-abegede","tigrinya-et","tigrinya-et-abegede","to","top","transparent","ultra-condensed","ultra-expanded","underline","up","upper-alpha","upper-armenian","upper-greek","upper-hexadecimal","upper-latin","upper-norwegian","upper-roman","uppercase","urdu","url","vertical","vertical-text","visible","visibleFill","visiblePainted","visibleStroke","visual","w-resize","wait","wave","white","wider","window","windowframe","windowtext","x-large","x-small","xor","xx-large","xx-small","yellow"]);
function d(q){var p={};
for(var o=0;
o<q.length;
++o){p[q[o]]=true
}return p
}function f(o,p){h=p;
return o
}function c(q,p){var o=q.next();
if(o=="@"){q.eatWhile(/[\w\\\-]/);
return f("def",q.current())
}else{if(o=="/"&&q.eat("*")){p.tokenize=m;
return m(q,p)
}else{if(o=="<"&&q.eat("!")){p.tokenize=a;
return a(q,p)
}else{if(o=="="){f(null,"compare")
}else{if((o=="~"||o=="|")&&q.eat("=")){return f(null,"compare")
}else{if(o=='"'||o=="'"){p.tokenize=l(o);
return p.tokenize(q,p)
}else{if(o=="#"){q.eatWhile(/[\w\\\-]/);
return f("atom","hash")
}else{if(o=="!"){q.match(/^\s*\w*/);
return f("keyword","important")
}else{if(/\d/.test(o)){q.eatWhile(/[\w.%]/);
return f("number","unit")
}else{if(o==="-"){if(/\d/.test(q.peek())){q.eatWhile(/[\w.%]/);
return f("number","unit")
}else{if(q.match(/^[^-]+-/)){return f("meta",h)
}}}else{if(/[,+>*\/]/.test(o)){return f(null,"select-op")
}else{if(o=="."&&q.match(/^\w+/)){return f("qualifier",h)
}else{if(o==":"){return f("operator",o)
}else{if(/[;{}\[\]\(\)]/.test(o)){return f(null,o)
}else{q.eatWhile(/[\w\\\-]/);
return f("property","variable")
}}}}}}}}}}}}}}}function m(r,q){var o=false,p;
while((p=r.next())!=null){if(o&&p=="/"){q.tokenize=c;
break
}o=(p=="*")
}return f("comment","comment")
}function a(r,q){var p=0,o;
while((o=r.next())!=null){if(p>=2&&o==">"){q.tokenize=c;
break
}p=(o=="-")?p+1:0
}return f("comment","comment")
}function l(o){return function(s,q){var r=false,p;
while((p=s.next())!=null){if(p==o&&!r){break
}r=!r&&p=="\\"
}if(!r){q.tokenize=c
}return f("string","string")
}
}return{startState:function(o){return{tokenize:c,baseIndent:o||0,stack:[]}
},token:function(r,q){if(r.eatSpace()){return null
}var p=q.tokenize(r,q);
var o=q.stack[q.stack.length-1];
if(p=="property"){if(o=="propertyValue"){if(g[r.current()]){p="string-2"
}else{if(k[r.current()]){p="keyword"
}else{p="variable-2"
}}}else{if(o=="rule"){if(!i[r.current()]){p+=" error"
}}else{if(!o||o=="@media{"){p="tag"
}else{if(o=="@media"){if(n[r.current()]){p="attribute"
}else{if(/^(only|not)$/i.test(r.current())){p="keyword"
}else{if(r.current().toLowerCase()=="and"){p="error"
}else{if(j[r.current()]){p="error"
}else{p="attribute error"
}}}}}else{if(o=="@mediaType"){if(n[r.current()]){p="attribute"
}else{if(r.current().toLowerCase()=="and"){p="operator"
}else{if(/^(only|not)$/i.test(r.current())){p="error"
}else{if(j[r.current()]){p="error"
}else{p="error"
}}}}}else{if(o=="@mediaType("){if(i[r.current()]){}else{if(n[r.current()]){p="error"
}else{if(r.current().toLowerCase()=="and"){p="operator"
}else{if(/^(only|not)$/i.test(r.current())){p="error"
}else{p+=" error"
}}}}}else{p="error"
}}}}}}}else{if(p=="atom"){if(!o||o=="@media{"){p="builtin"
}else{if(o=="propertyValue"){if(!/^#([0-9a-fA-f]{3}|[0-9a-fA-f]{6})$/.test(r.current())){p+=" error"
}}else{p="error"
}}}else{if(o=="@media"&&h=="{"){p="error"
}}}if(h=="{"){if(o=="@media"||o=="@mediaType"){q.stack.pop();
q.stack[q.stack.length-1]="@media{"
}else{q.stack.push("rule")
}}else{if(h=="}"){q.stack.pop();
if(o=="propertyValue"){q.stack.pop()
}}else{if(h=="@media"){q.stack.push("@media")
}else{if(o=="@media"&&/\b(keyword|attribute)\b/.test(p)){q.stack.push("@mediaType")
}else{if(o=="@mediaType"&&r.current()==","){q.stack.pop()
}else{if(o=="@mediaType"&&h=="("){q.stack.push("@mediaType(")
}else{if(o=="@mediaType("&&h==")"){q.stack.pop()
}else{if(o=="rule"&&h==":"){q.stack.push("propertyValue")
}else{if(o=="propertyValue"&&h==";"){q.stack.pop()
}}}}}}}}}return p
},indent:function(p,o){var q=p.stack.length;
if(/^\}/.test(o)){q-=p.stack[p.stack.length-1]=="propertyValue"?2:1
}return p.baseIndent+q*e
},electricChars:"}"}
});
CodeMirror.defineMIME("text/css","css");