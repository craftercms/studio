$(document).ready(function(){module("testQuoteEscape");
test("testQuoteEscapeDouble",function(){expect(1);
var b='let $rootfolder := "c:\\builds\\winnt\\HEAD\\qa\\scripts\\"let $keysfolder := concat($rootfolder, "keys\\")return$keysfolder';
var d='<span class="cm-keyword">let</span> <span class="cm-variable">$rootfolder</span> <span class="cm-keyword">:=</span> <span class="cm-string">"c:\\builds\\winnt\\HEAD\\qa\\scripts\\"</span><span class="cm-keyword">let</span> <span class="cm-variable">$keysfolder</span> <span class="cm-keyword">:=</span> <span class="cm-variable cm-def">concat</span>(<span class="cm-variable">$rootfolder</span>, <span class="cm-string">"keys\\"</span>)<span class="cm-word">return$keysfolder</span>';
$("#sandbox").html('<textarea id="editor">'+b+"</textarea>");
var c=CodeMirror.fromTextArea($("#editor")[0]);
var a=$(".CodeMirror-lines div div pre")[0].innerHTML;
equal(a,d);
$("#editor").html("")
})
});