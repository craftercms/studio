$(document).ready(function(){module("testProcessingInstructions");
test("testProcessingInstructions",function(){expect(1);
var b="data(<?target content?>) instance of xs:string";
var d='<span class="cm-variable cm-def">data</span>(<span class="cm-comment cm-meta">&lt;?target content?&gt;</span>) <span class="cm-keyword">instance</span> <span class="cm-keyword">of</span> <span class="cm-atom">xs:string</span>';
$("#sandbox").html('<textarea id="editor">'+b+"</textarea>");
var c=CodeMirror.fromTextArea($("#editor")[0]);
var a=$(".CodeMirror-lines div div pre")[0].innerHTML;
equal(a,d);
$("#editor").html("")
})
});