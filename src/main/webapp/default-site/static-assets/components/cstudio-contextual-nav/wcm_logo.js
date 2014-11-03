var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;

/**
 * Branded Logo Plugin
 */
CStudioAuthoring.ContextualNav.WcmLogo = CStudioAuthoring.ContextualNav.WcmLogo || {

	initialized: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		YAHOO.util.Event.onAvailable("acn-wcm-logo", function() {
		 	YDom.get("acn-wcm-logo-image").src = CStudioAuthoringContext.baseUri + '/proxy/alfresco/cstudio/services/content/content-at-path?path=/cstudio/config/app-logo.png';
			
 			YDom.get("acn-wcm-logo-link").href = CStudioAuthoringContext.authoringAppBaseUri + CStudioAuthoringContext.homeUri;
		}, this);	
	}
}

CStudioAuthoring.Module.moduleLoaded("wcm_logo", CStudioAuthoring.ContextualNav.WcmLogo);
