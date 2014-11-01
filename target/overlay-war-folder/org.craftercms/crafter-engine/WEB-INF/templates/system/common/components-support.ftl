<#assign componentCount = model['count(//rteComponents//item/id)'] />

<#if componentCount == 1 >
	<div style='display:none' id='o_${model['//rteComponents//item/id']}'>
		<@renderComponent component=model['//rteComponents//item'] />
	</div>	
<#elseif (componentCount > 1) == true >
	<#assign components = model['//rteComponents//item'] />
	<#list components as c>
        <#if c.id??>
            <div style='display:none' id='o_${c.id}'>
                <@renderComponent component = c />
            </div>
        </#if>
	</#list>
</#if>