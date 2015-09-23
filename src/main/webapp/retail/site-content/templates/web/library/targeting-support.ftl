
<#if siteContext.overlayCallback??>
	<#assign isPreview = true />
<#else>
	<#assign isPreview = false />
</#if>

<#macro currentSeason locale>
	<#assign season = "" />
	
	<#if isPreview == false>
		<#assign calculatedSeason = dateService.dateToSeason(dateService.currentDate, locale)  />
		<#assign season = calculatedSeason />						
	<#else>
		<#if profile??>
		    <#assign season = profile.season!'none'>
		<#else>
		    <#assign season = 'none'>
		</#if>
	</#if>
</#macro>



