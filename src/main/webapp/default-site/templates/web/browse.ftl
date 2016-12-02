<#assign mode = RequestParameters["mode"] />
<#-- <#assign view = RequestParameters["view"] /> -->
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>Crafter Studio</title>
    <#include "/templates/web/common/page-fragments/head.ftl" />
    <#include "/templates/web/common/page-fragments/studio-context.ftl" />

    <link rel="stylesheet" href="/static-assets/css/bootstrap.css">
    <link rel="stylesheet" type="text/css" href="/studio/static-assets/styles/browse.css" />

    <link rel="stylesheet" href="/studio/static-assets/libs/jQuery-contextMenu-master/dist/jquery.contextMenu.min.css" type="text/css">

    <script src="/studio/static-assets/libs/jquery/dist/jquery.js"></script>
    <script src="/studio/static-assets/libs/handlebars/handlebars.js"></script>
    <script src="/studio/static-assets/libs/jstree/dist/jstree.min.js"></script>

  
    <script src="/studio/static-assets/libs/jQuery-contextMenu-master/dist/jquery.contextMenu.js" type="text/javascript"></script>
    <script src="/studio/static-assets/libs/jQuery-contextMenu-master/dist/jquery.ui.position.min.js" type="text/javascript"></script>


    <script type="text/javascript" src="/studio/static-assets/components/cstudio-browse/browse.js"></script>
    <link rel="stylesheet" type="text/css" href="/studio/static-assets/libs/jstree/dist/themes/default/style.min.css" />
    <link href="/studio/static-assets/themes/cstudioTheme/css/icons.css" type="text/css" rel="stylesheet">

    <#assign path="/studio/static-assets/components/cstudio-common/resources/" />
    <script src="${path}en/base.js?version=${UIBuildId!''}"></script>
    <script src="${path}kr/base.js?version=${UIBuildId!''}"></script>
    <script src="${path}es/base.js?version=${UIBuildId!''}"></script>

    <script>
        var CMgs = CStudioAuthoring.Messages,
            browseLangBundle = CMgs.getBundle("browse", CStudioAuthoringContext.lang);
    </script>

  </head>

  <body class="yui-skin-cstudioTheme skin-browse">
    <div class="cstudio-browse-container">

      <p class="current-folder">
        <span class="path"></span>
      </p>

      <div id="cstudio-wcm-search-filter-controls">
          <div id="data" class="demo"></div>
      </div>

      <div id="cstudio-wcm-search-result">

          <div class="cstudio-results-actions"></div>
          
          <div class="results"></div>

          <div id="cstudio-wcm-search-render-finish">
          
          </div>
      </div>

    </div>    

    <div id="cstudio-command-controls">
      <div id="submission-controls" class="cstudio-form-controls-button-container">
        <#if mode == "select">
        <input id="formSaveButton" type="button" class="cstudio-search-btn cstudio-button btn btn-primary" disabled value="Add Selection">
        </#if>
        <input id="formCancelButton" type="button" class="cstudio-search-btn cstudio-button btn btn-default" value="Cancel">

        
      </div>
    </div>

    <div class="cstudio-browse-image-popup-overlay">
      <div id="cstudio-browse-image-pop-up">
          <div>
              <input type="button" class="close btn btn-default" value="x">
          </div>
          <img src="">
      </div>
    </div>
  
    <#-- <#if view == "window" >
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
     </#if> -->

     <script id="hb-search-result" type="text/x-handlebars-template">
        <div class="cstudio-search-result clearfix">
            <div id="result-select-{{browserUri}}" class="cstudio-search-select-container">
              <#-- none, many, one -->

              {{#equal selectMode "many"}}
                <input type="checkbox" name="result-select">
              {{/equal}}

              {{#equal selectMode "one"}}
                <input type="radio" name="result-select">
              {{/equal}}

            </div>
            <div class="cstudio-result-body row" style="overflow: hidden;">
              <div class="cstudio-search-result-description">
                <span class="browse-icon {{status}}" id="result-status-static-assets-images-brand-bg-png"></span> 
                <span class="cstudio-search-component cstudio-search-component-title-nopreview">
                {{#if internalName}}
                  {{internalName}}
                {{else}}
                  {{name}}
                {{/if}}
                </span>

                {{#if showUrl}}
                <span class="cstudio-search-component cstudio-search-component-url">
                  <span class="component-title bold">{{labelUrl}}:</span>
                  <a href="{{browserUri}}" target="_blank">{{browserUri}}</a>
                </span>
                {{/if}}


                <span class="cstudio-search-component cstudio-search-component-type">
                  <span class="component-title bold">{{labelType}}:</span>
                  {{type}}
                </span>
                <span class="cstudio-search-component cstudio-search-component-button">
                  <a class="btn btn-default cstudio-search-btn add-close-btn results-btn" href="#" role="button">{{labelAddClose}}</a>
                </span>
              </div>
              <div class="cstudio-search-description-preview">
                {{#equal type "image"}}
                <img src="{{browserUri}}" alt="{{name}}" class="cstudio-search-banner-image"">
                <img src="http://localhost:8080/studio/static-assets/themes/cstudioTheme/images/magnify.jpg" class="magnify-icon" style="position: absolute; right: 0; bottom: 0;" data-source="{{browserUri}}">
                {{/equal}}
              </div>
            </div>
          </div>
    </script>

    <script id="hb-search-results-actions-buttons" type="text/x-handlebars-template">
      {{#if onlyClear}}
      <a class="cstudio-search-btn btn btn-default cstudio-search-select-all results-btn" href="#" role="button" style="margin-right: 10px; margin-bottom: 20px">{{labelSelectAll}}</a>
      {{/if}}
      <a class="cstudio-search-btn btn btn-default cstudio-search-clear-selection results-btn" href="#" role="button" style="margin-bottom: 20px;">{{labelClearAll}}</a>
    </script>
    
    <script type="text/javascript">
      Handlebars.registerHelper('equal', function(lvalue, rvalue, options) {
        if (arguments.length < 3)
            throw new Error("Handlebars Helper equal needs 2 parameters");
        if( lvalue!=rvalue ) {
            return options.inverse(this);
        } else {
            return options.fn(this);
        }
      });
    </script>
  
   </body>

</html>
