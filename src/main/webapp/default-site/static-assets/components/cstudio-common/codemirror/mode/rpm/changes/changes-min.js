CodeMirror.defineMode("changes",function(a,e){var d=/^-+$/;
var c=/^(Mon|Tue|Wed|Thu|Fri|Sat|Sun) (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)  ?\d{1,2} \d{2}:\d{2}(:\d{2})? [A-Z]{3,4} \d{4} - /;
var b=/^[\w+.-]+@[\w.-]+/;
return{token:function(f){if(f.sol()){if(f.match(d)){return"tag"
}if(f.match(c)){return"tag"
}}if(f.match(b)){return"string"
}f.next();
return null
}}
});
CodeMirror.defineMIME("text/x-rpm-changes","changes");