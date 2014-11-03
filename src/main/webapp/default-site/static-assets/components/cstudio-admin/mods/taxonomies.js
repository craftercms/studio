CStudioAdminConsole.Tool.ContentTypes = CStudioAdminConsole.Tool.ContentTypes ||  function(config, el)  {
	return this;
}

YAHOO.extend(CStudioAdminConsole.Tool.ContentTypes, CStudioAdminConsole.Tool, {
	renderWorkarea: function() {
		var a = "<div id='content-type-canvas'>" +
		""+
		"</div>"+
		
		"<div id='content-type-tools'>" +
		"" +
		"</div>";
	}
});

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-content-types",CStudioAdminConsole.Tool.ContentTypes);