<#import "/templates/system/common/cstudio-support.ftl" as studio />

<#assign imageSource = model.image!"" />
<#if imageSource = "">
	<#assign imageSource = "http://placehold.it/350x150" />
</#if>
<img <@studio.componentAttr path=model.storeUrl ice=false /> src="${imageSource}" alt="${model.alttext!""}" />