CStudioAdminConsole.Tool.CommentModeration = CStudioAdminConsole.Tool.CommentModeration ||  function(config, el)  {
	return this;
}

YAHOO.extend(CStudioAdminConsole.Tool.CommentModeration, CStudioAdminConsole.Tool, {
	renderWorkarea: function() {
		var workareaEl = document.getElementById("cstudio-admin-console-workarea");
		
		workareaEl.innerHTML = "Not Yet Implemented"; 

		var actions = [ 
		];
		
		CStudioAuthoring.ContextualNav.AdminConsoleNav.initActions(actions);

		CStudioAdminConsole.CommandBar.render([]); 
	}
});

CStudioAuthoring.Module.moduleLoaded("cstudio-console-tools-comment-moderation",CStudioAdminConsole.Tool.CommentModeration);