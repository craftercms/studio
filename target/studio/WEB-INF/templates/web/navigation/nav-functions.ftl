<#function getNavItemName item>
    <#assign actualNavItem = item>
    <#if item.folder>
        <#assign actualNavItem = item.getChildItem("index.xml")>
    </#if>

    <#return actualNavItem.navLabel!(item.storeName?replace("-", " ")?replace(".xml", "")?cap_first)>
</#function>

<#function getNavItemUrl item>
    <#return urlTransformationService.transform('storeUrlToRenderUrl', item.storeUrl)>
</#function>