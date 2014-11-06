/**
 * Logout Plugin
 */ 
CStudioAuthoring.ContextualNav.WcmLogoutMod = CStudioAuthoring.ContextualNav.WcmLogoutMod || {

	initialized: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		var CMgs = CStudioAuthoring.Messages;
        var contextNavLangBundle = CMgs.getBundle("contextnav", CStudioAuthoringContext.lang);

		var el = YDom.get("acn-logout");
		el.innerHTML = '<a id="acn-logout-link" href="'+CStudioAuthoringContext.authoringAppBaseUri + "/logout"+'">'+CMgs.format(contextNavLangBundle, "logout")+'</a>';
	}
}

CStudioAuthoring.Module.moduleLoaded("logout", CStudioAuthoring.ContextualNav.WcmLogoutMod);
