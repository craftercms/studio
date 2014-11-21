{ "roles": [
		<#list result as role>
		  "${role}"<#if role_has_next>,</#if>
		</#list> 
	]
}
