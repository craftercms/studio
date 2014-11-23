{ "permissions": [
		<#list result as permission>
		  {"permission": "${permission}"}<#if permission_has_next>,</#if>
		</#list> 
	]
}
