[
<#list items as item>
	{
		  uri: "${item.path}", state: "${item.state}", isSystemProcessing: <#if item.isSystemProcessing()==true>true<#else>false</#if>
 	},
</#list>
]
