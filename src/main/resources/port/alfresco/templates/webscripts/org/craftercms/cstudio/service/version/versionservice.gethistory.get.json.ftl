{
	"versions":[
			<#list versions as version>
				{
					"versionNumber":"${version.getVersionNumber()}",
					"lastModifiedDate":"${version.lastModifiedDate}"
					"lastModifier":"${version.lastModifier}"					
				}
				<#if version_has_next>,</#if>   
			</#list> 
			]
}
