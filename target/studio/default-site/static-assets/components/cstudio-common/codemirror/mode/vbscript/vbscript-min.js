CodeMirror.defineMode("vbscript",function(){var a=/^(?:Call|Case|CDate|Clear|CInt|CLng|Const|CStr|Description|Dim|Do|Each|Else|ElseIf|End|Err|Error|Exit|False|For|Function|If|LCase|Loop|LTrim|Next|Nothing|Now|Number|On|Preserve|Quit|ReDim|Resume|RTrim|Select|Set|Sub|Then|To|Trim|True|UBound|UCase|Until|VbCr|VbCrLf|VbLf|VbTab)$/im;
return{token:function(c){if(c.eatSpace()){return null
}var b=c.next();
if(b=="'"){c.skipToEnd();
return"comment"
}if(b=='"'){c.skipTo('"');
return"string"
}if(/\w/.test(b)){c.eatWhile(/\w/);
if(a.test(c.current())){return"keyword"
}}return null
}}
});
CodeMirror.defineMIME("text/vbscript","vbscript");