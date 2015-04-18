CStudioForms.FormControllers.TestController = CStudioForms.FormControllers.TestController ||  function() {
};

YAHOO.extend(CStudioForms.FormControllers.TestController, CStudioForms.FormController, {
	isFieldRelevant: function(field) {
		//if(field.id == "tiles") {
		//	return false;
		//}
               if(field.id == "test") {
			return false;
		}
		
		return true;
	}
});


CStudioAuthoring.Module.moduleLoaded("/page/entry-controller", CStudioForms.FormControllers.TestController );