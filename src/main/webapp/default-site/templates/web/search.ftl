<#assign mode = RequestParameters["mode"] />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>


   <title>Crafter Studio</title>


    <#include "/templates/web/common/page-fragments/studio-context.ftl" />
    <#include "/templates/web/common/page-fragments/head.ftl" />

     <script type="text/javascript" src="/studio/static-assets/components/cstudio-common/resources/en/base.js"></script>
    <script type="text/javascript" src="/studio/static-assets/components/cstudio-common/resources/kr/base.js"></script>
   
    <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/search.js"></script>
    <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/results/default.js"></script>
    <script type="text/javascript" src="/studio/static-assets/yui/calendar/calendar-min.js"></script> 
    <link rel="stylesheet" type="text/css" href="/studio/static-assets/yui/assets/skins/sam/calendar.css" />
    <link rel="stylesheet" type="text/css" href="/studio/static-assets/themes/cstudioTheme/css/search.css" />

  <!-- filter templates -->
   <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/common.js"></script>
   <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/default.js"></script>
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/javascript.js"></script>  
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/css.js"></script>   
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/image.js"></script>   
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/xhtml.js"></script>   
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/flash.js"></script>   
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/filters/content-type.js"></script>  

  <!-- result templates -->
   <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/results/default.js"></script>
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/results/image.js"></script>   
       <script type="text/javascript" src="/studio/static-assets/components/cstudio-search/results/flash.js"></script>   
   <link href="/studio/static-assets/themes/cstudioTheme/css/icons.css" type="text/css" rel="stylesheet">
   <link href="/studio/static-assets/yui/container/assets/container.css" type="text/css" rel="stylesheet">


    <#if mode == "act">
      <#include "/templates/web/common/page-fragments/context-nav.ftl" />
    </#if>


    <script>
      CMgs = CStudioAuthoring.Messages;
      langBundle = CMgs.getBundle("search", CStudioAuthoringContext.lang);
    </script>

</head>

<body class="yui-skin-cstudioTheme">

   <div class="sticky-wrapper">
<div id="global_x002e_cstudio-search">
    <div id="global_x002e_cstudio-search_x0023_default">

  <div id="cstudio-wcm-search-wrapper"> 
    <div id="cstudio-wcm-search-main">        
      
      <div id="cstudio-wcm-search-search-title" class="cstudio-wcm-searchResult-header" style="font-size: 24px; font-weight:bold"></div>
      <div id="cstudio-wcm-search-filter-controls"></div>   
      <div style="clear:both;"></div>
      <br />
      <span><script>CMgs.display(langBundle, "keywords")</script></span>
      <br />
      <input type="text" name="keywords" id="cstudio-wcm-search-keyword-textbox"  value="${RequestParameters["s"]!''}"/>

      <input type="hidden" id="cstudio-wcm-search-presearch"  value="true" />
            
      <input type="button" id="cstudio-wcm-search-button" value="Search">   
      <div id="cstudio-wcm-search-result-header">
        <div id="cstudio-wcm-search-result-header-container">       
          <span class="cstudio-wcm-search-result-header"><script>CMgs.display(langBundle, "searchResults")</script></span>
          <span id="cstudio-wcm-search-message-span"></span>      
          <span id="cstudio-wcm-search-result-header-count"></span>
          <a id="cstudio-wcm-search-description-toggle-link" href="javascript:void(0)" onClick="CStudioSearch.toggleResultDetail(CStudioSearch.DETAIL_TOGGLE);"></a>
          
          <div class="filters">
            <div class="cstudio-wcm-search-result-header-pagination"> 
              <script>CMgs.display(langBundle, "show")</script>:<input type="text" 
                    class="cstudio-wcm-search-result-header-pagination-textbox" 
                    maxlength="3" 
                    value="20"
                    id="cstudio-wcm-search-item-per-page-textbox"
                    name="total"/>
            </div>
            <div class="cstudio-wcm-search-result-header-sort">
              <script>CMgs.display(langBundle, "sort")</script>:<select id="cstudio-wcm-search-sort-dropdown" name="sortBy">
              <!-- items added via ajax -->
              </select>
            </div>
          </div>
        </div>
      </div>      
      <div id="cstudio-wcm-search-result">
         <div id="cstudio-wcm-search-result-in-progress" class="cstudio-wcm-search-result-in-progress-img"></div>
        &nbsp;  
      </div>

      <div class="cstudio-wcm-search-pagination">
        <div id="cstudio-wcm-search-pagination-controls"></div>
      </div>
    

    </div>
  </div>  
    </div>

</div>  

    <#if mode == "select" >
      <div id="cstudio-command-controls"></div>
    </#if>

       </div>


</body>
</html>