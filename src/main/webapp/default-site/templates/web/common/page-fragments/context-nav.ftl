<script>
    (function (CStudioAuthoring) {

        CStudioAuthoring.OverlayRequiredResources.loadRequiredResources();
        CStudioAuthoring.OverlayRequiredResources.loadContextNavCss();
        CStudioAuthoring.Events.contextNavLoaded.subscribe(function() {
            CStudioAuthoring.ContextualNav.hookNavOverlayFromAuthoring();
            CStudioAuthoring.InContextEdit.autoInitializeEditRegions();
        });

    }) (CStudioAuthoring);
</script>