<#function pathToUrl path querystring>
   <#assign url = (((path?replace("/index.xml", ""))?replace("/site/website" , ""))?replace(".xml", ".html")) + querystring />
   <#return url>
</#function>

<#function isEngineModePreview>
   <#return siteContext.overlayCallback?? />
</#function>