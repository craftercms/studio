<script>
CStudioAuthoring.OverlayRequiredResources.loadRequiredResources();
CStudioAuthoring.OverlayRequiredResources.loadContextNavCss();

CStudioAuthoring.Events.contextNavLoaded.subscribe(function() {
	CStudioAuthoring.ContextualNav.hookNavOverlayFromAuthoring();
	CStudioAuthoring.InContextEdit.autoInitializeEditRegions();
});		
</script>