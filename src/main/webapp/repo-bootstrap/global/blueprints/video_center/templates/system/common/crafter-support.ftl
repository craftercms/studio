<#macro renderComponents componentList>
  <#if componentList?? && componentList.item??>
    <#list componentList.item as module>
      <@renderComponent component=module />
    </#list>
  </#if>
</#macro>

<#macro renderRTEComponents model>
 
  <#assign componentCount = model['count(//rteComponents//item/id)'] />

  <#if componentCount == 1 > 
      <#assign curComponentPath = ""+model['//rteComponents//item/contentId'] />
    <div style='display:none' id='o_${model['//rteComponents//item/id']}'>
      <#-- @renderComponent component=model['//rteComponents//item'] /-->
      <@renderComponent componentPath=curComponentPath />
    </div>  

     <#assign item = siteItemService.getSiteItem(curComponentPath) />
     <@renderRTEComponents model=item />

  <#elseif (componentCount > 1) == true >
    <#assign components = model['//rteComponents//item'] />
    <#list components as c>
          <#if c.id??>
              <div style='display:none' id='o_${c.id}'>
          <#assign curComponentPath = "" + c.contentId />
                   <@renderComponent componentPath=curComponentPath />
              </div>
        
         <#assign item = siteItemService.getSiteItem(curComponentPath) />
         <@renderRTEComponents model=item />
          </#if>
    </#list>
  </#if>
</#macro>