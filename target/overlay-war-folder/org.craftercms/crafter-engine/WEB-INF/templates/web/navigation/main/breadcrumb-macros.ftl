<#macro renderBreadcrumbItem item active = false>
<#if active>
    <li class="active">${item.name}</li>
<#else>
    <li><a href="${urlTransformationService.transform('storeUrlToRenderUrl', item.url)}">${item.name}</a> <span class="divider">/</span></li>
</#if>
</#macro>