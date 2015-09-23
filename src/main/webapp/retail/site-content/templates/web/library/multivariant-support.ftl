<#if siteContext.overlayCallback??>
	<#assign isPreview = true />
<#else>
	<#assign isPreview = false />
</#if>

<#macro experiment>
	<#if isPreview == false>
		<#nested />
	</#if>
</#macro>
