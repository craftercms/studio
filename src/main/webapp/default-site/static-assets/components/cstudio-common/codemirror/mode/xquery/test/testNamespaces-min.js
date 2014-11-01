$(document).ready(function(){module("test namespaces");
test("test namespaced variable",function(){expect(1);
var b='declare namespace e = "http://example.com/ANamespace";declare variable $e:exampleComThisVarIsNotRecognized as element(*) external;';
var d='<span class="cm-keyword">declare</span> <span class="cm-keyword">namespace</span> <span class="cm-word">e</span> <span class="cm-keyword">=</span> <span class="cm-string">"http://example.com/ANamespace"</span><span class="cm-word">;declare</span> <span class="cm-keyword">variable</span> <span class="cm-variable">$e:exampleComThisVarIsNotRecognized</span> <span class="cm-keyword">as</span> <span class="cm-keyword">element</span>(<span class="cm-keyword">*</span>) <span class="cm-word">external;</span>';
$("#sandbox").html('<textarea id="editor">'+b+"</textarea>");
var c=CodeMirror.fromTextArea($("#editor")[0]);
var a=$(".CodeMirror-lines div div pre")[0].innerHTML;
equal(a,d);
$("#editor").html("")
});
test("test EQName variable",function(){expect(1);
var b='declare variable $"http://www.example.com/ns/my":var := 12;<out>{$"http://www.example.com/ns/my":var}</out>';
var d='<span class="cm-keyword">declare</span> <span class="cm-keyword">variable</span> <span class="cm-variable">$"http://www.example.com/ns/my":var</span> <span class="cm-keyword">:=</span> <span class="cm-atom">12</span><span class="cm-word">;</span><span class="cm-tag">&lt;out&gt;</span>{<span class="cm-variable">$"http://www.example.com/ns/my":var</span>}<span class="cm-tag">&lt;/out&gt;</span>';
$("#sandbox").html('<textarea id="editor">'+b+"</textarea>");
var c=CodeMirror.fromTextArea($("#editor")[0]);
var a=$(".CodeMirror-lines div div pre")[0].innerHTML;
equal(a,d);
$("#editor").html("")
});
test("test EQName function",function(){expect(1);
var b='declare function "http://www.example.com/ns/my":fn ($a as xs:integer) as xs:integer {   $a + 2};<out>{"http://www.example.com/ns/my":fn(12)}</out>';
var d='<span class="cm-keyword">declare</span> <span class="cm-keyword">function</span> <span class="cm-variable cm-def">"http://www.example.com/ns/my":fn</span> (<span class="cm-variable">$a</span> <span class="cm-keyword">as</span> <span class="cm-atom">xs:integer</span>) <span class="cm-keyword">as</span> <span class="cm-atom">xs:integer</span> {   <span class="cm-variable">$a</span> <span class="cm-keyword">+</span> <span class="cm-atom">2</span>}<span class="cm-word">;</span><span class="cm-tag">&lt;out&gt;</span>{<span class="cm-variable cm-def">"http://www.example.com/ns/my":fn</span>(<span class="cm-atom">12</span>)}<span class="cm-tag">&lt;/out&gt;</span>';
$("#sandbox").html('<textarea id="editor">'+b+"</textarea>");
var c=CodeMirror.fromTextArea($("#editor")[0]);
var a=$(".CodeMirror-lines div div pre")[0].innerHTML;
equal(a,d);
$("#editor").html("")
});
test("test EQName function with single quotes",function(){expect(1);
var b="declare function 'http://www.example.com/ns/my':fn ($a as xs:integer) as xs:integer {   $a + 2};<out>{'http://www.example.com/ns/my':fn(12)}</out>";
var d='<span class="cm-keyword">declare</span> <span class="cm-keyword">function</span> <span class="cm-variable cm-def">\'http://www.example.com/ns/my\':fn</span> (<span class="cm-variable">$a</span> <span class="cm-keyword">as</span> <span class="cm-atom">xs:integer</span>) <span class="cm-keyword">as</span> <span class="cm-atom">xs:integer</span> {   <span class="cm-variable">$a</span> <span class="cm-keyword">+</span> <span class="cm-atom">2</span>}<span class="cm-word">;</span><span class="cm-tag">&lt;out&gt;</span>{<span class="cm-variable cm-def">\'http://www.example.com/ns/my\':fn</span>(<span class="cm-atom">12</span>)}<span class="cm-tag">&lt;/out&gt;</span>';
$("#sandbox").html('<textarea id="editor">'+b+"</textarea>");
var c=CodeMirror.fromTextArea($("#editor")[0]);
var a=$(".CodeMirror-lines div div pre")[0].innerHTML;
equal(a,d);
$("#editor").html("")
})
});