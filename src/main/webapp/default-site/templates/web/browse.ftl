<#assign mode = RequestParameters["mode"] />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <#include "/templates/web/common/page-fragments/head.ftl" />

   <title>Crafter Studio</title>
<script type="text/javascript" src="/studio/static-assets/components/cstudio-browse/browse.js"></script>
<script type="text/javascript" src="/studio/static-assets/yui/calendar/calendar-min.js"></script> 
<link rel="stylesheet" type="text/css" href="/studio/static-assets/yui/assets/skins/sam/calendar.css" />
<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/global.css" />
<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/search.css" />
<link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/forms-default.css" />
<link rel="stylesheet" type="text/css" href="/studio/static-assets/styles/forms-engine.css" />

<!-- Template Assets -->
   <script type="text/javascript" src="/studio/static-assets/yui/treeview/treeview-min.js"></script> 
   <script type="text/javascript" src="/studio/static-assets/yui/animation/animation-min.js"></script>
   <script type="text/javascript" src="/studio/static-assets/themes/cstudioTheme/js/global.js"></script>
   <script type="text/javascript" src="/studio/static-assets/components/cstudio-form/swfobject.js"></script>

  <!-- filter templates -->
   <script type="text/javascript" src="/studio/static-assets/components/cstudio-browse/filters/common.js"></script>
   <script type="text/javascript" src="/studio/static-assets/components/cstudio-browse/filters/default.js"></script>

  <!-- result templates -->
   <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/results/default.js"></script>
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/results/image.js"></script>   
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/results/flash.js"></script>   
   
   <link href="/studio/static-assets/themes/cstudioTheme/css/icons.css" type="text/css" rel="stylesheet">
   <link href="/studio/static-assets/yui/container/assets/container.css" type="text/css" rel="stylesheet">

    <#assign path="/studio/static-assets/components/cstudio-common/resources/" />
    <script src="${path}en/base.js"></script>
    <script src="${path}kr/base.js"></script>
    <script src="${path}es/base.js"></script>

    <#include "/templates/web/common/page-fragments/studio-context.ftl" />

    <#if mode == "act" >
      <#include "/templates/web/common/page-fragments/context-nav.ftl" />
    </#if>

    <script>
        var CMgs = CStudioAuthoring.Messages,
                siteDropdownLangBundle = CMgs.getBundle("siteDropdown", CStudioAuthoringContext.lang);
    </script>

</head>

<body class="yui-skin-cstudioTheme skin-browse">
   <div class="sticky-wrapper">
<div id="global_x002e_cstudio-browse">
    <div id="global_x002e_cstudio-browse_x0023_default">

  <script>
  YEvent.onAvailable("cstudio-command-controls", function() {
    CStudioAuthoring.Utils.addCss('/overlay-css.css?baseUrl=' +
                       CStudioAuthoringContext.baseUri);
                       
    var formControls = new CStudioAuthoring.CommandToolbar("cstudio-command-controls", true);
    
    formControls.addControl("formSaveButton", "Add Item", function() {

      var searchId = CStudioAuthoring.Utils.getQueryVariable(document.location.search, "searchId");
      var crossServerAccess = false;
      
        try {
          // unfortunately we cannot signal a form close across servers
          // our preview is in one server
          // our authoring is in another
          // in this case we just close the window, no way to pass back details which is ok in some cases
          if(window.opener.CStudioAuthoring) { }
        }
        catch(crossServerAccessErr) {
          crossServerAccess = true;
        }
  
      if(window.opener && !crossServerAccess) {
        
            if(window.opener.CStudioAuthoring) {
    
              var openerChildSearchMgr = window.opener.CStudioAuthoring.ChildSearchManager;
  
              if(openerChildSearchMgr) {
              
                var searchConfig = openerChildSearchMgr.searches[searchId];
                
                if(searchConfig) {
                  var callback = searchConfig.saveCallback;
  
                  if(callback) {
                    var selectedContentTOs = CStudioAuthoring.SelectedContent.getSelectedContent();
    
                openerChildSearchMgr.signalSearchClose(searchId, selectedContentTOs); 
                  }
                  else {
                //TODO PUT THIS BACK 
                    //alert("no success callback provided for seach: " + searchId);
                  }
                }
                else {
                  alert("unable to lookup child form callback for search:" + searchId);
                }
              }
              else {     
            alert("unable to lookup parent context for search:" + searchId);
              }             
            }
        
        window.close();
      }
      else {
        // no window opening context or cross server call
        // the only thing we can do is close the window
        window.close();
      }
    });
  
    formControls.addControl("formCancelButton", "Cancel", function() {
      window.close();
    });
  });

  </script>

  <div id="cstudio-wcm-search-wrapper" style="min-width: 1130px;">

    <div id="cstudio-wcm-search-main" class="cstudio-wcm-browse-main">
      <h1 id="cstudio-wcm-search-search-title" class="cstudio-wcm-searchResult-header"></h1>
      <div id="cstudio-wcm-search-filter-controls" style="overflow-x:scroll;width:230px; min-height:570px; background-color:white; float:left; padding: 10px 20px; border-radius: 5px; float: left; border: 1px #ccc solid; margin-bottom: 25px;"></div>
       
        <div id="cstudio-wcm-search-result" style="min-width: 715px; min-height:570px; width:67%; border-radius: 5px; float: left; border: 1px #ccc solid; margin-bottom: 50px;  margin-left: 10px; overflow:hidden;">
         <div id="cstudio-wcm-search-result-in-progress" class="cstudio-wcm-search-result-in-progress-img"></div>
        &nbsp;  
      </div>
      <div style="clear:both"></div>

    </div>
  </div>  
    </div>

</div>  

    <#if mode == "select" >
      <div id="cstudio-command-controls"></div>
    </#if>
   </div>

   <div id="studioBar" class="studio-view">
       <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
           <div class="container-fluid">
                   <a class="navbar-brand" href="/studio/site-dashboard">
                       <img src="/studio/static-assets/images/crafter_studio_360.png" alt="Crafter Studio">
                   </a>
               </div>
           </div>
       </nav>
   </div>

</html>
