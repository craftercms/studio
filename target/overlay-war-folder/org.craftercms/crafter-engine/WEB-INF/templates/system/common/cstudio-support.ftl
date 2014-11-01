
<#macro cstudioOverlaySupport>
    <#if siteContext.overlayCallback??>
        ${siteContext.overlayCallback.render()}
    </#if>
</#macro>

<#macro ice id="" component="" componentPath="">
    <#if siteContext.overlayCallback??>
        <#if id != "">
			<#if componentPath != "">
	            <div id='${componentPath}:${id}' class='cstudio-ice'><#nested></div>
			<#else>
	            <div id='${id}' class='cstudio-ice'><#nested></div>
			</#if>			
        <#elseif id == "" && componentPath == "">
            <div id="cstudio-component-${component.key}" class='cstudio-component-ice'><#nested></div>
        <#elseif id == "" && component == "">
            <div id="cstudio-component-${componentPath}" class='cstudio-component-ice'><#nested></div>
        </#if>
    <#else>
        <#nested>
    </#if>
</#macro>


<#macro draggableComponent id="" component="" componentPath="">
    <#if siteContext.overlayCallback??>
        <#if id != "" && component == "" && componentPath == "">
            <@ice id=id>
	            <div id='${id}' class='cstudio-draggable-component'><#nested></div>
	        </@ice>
        <#elseif id == "" && componentPath == "">
            <@ice component=component>
            	<div id="cstudio-component-${component.key}" class='cstudio-draggable-component'><#nested></div>
            </@ice>
        <#elseif id == "" && component == "">
            <@ice componentPath=componentPath>
            	<div id="cstudio-component-${componentPath}" class='cstudio-draggable-component'><#nested></div>
            </@ice>
        </#if>
    <#else>
        <#nested>
    </#if>
</#macro>


<#macro componentZone id="">
	<div class="cstudio-component-zone" id="zone-${id}">
		<@ice id=id>
			<#nested>
		</@ice>
	</div>
</#macro>