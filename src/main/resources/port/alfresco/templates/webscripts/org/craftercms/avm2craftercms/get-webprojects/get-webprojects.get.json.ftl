<#assign search="TYPE:\"{http://www.alfresco.org/model/wcmappmodel/1.0}webfolder\"">
<#assign username=person.properties.userName>

{ avmProjects: [
<#list companyhome.childrenByLuceneSearch[search]?sort_by('name') as wp>
	<#list wp.childAssocs["wca:webuser"] as user>
         <#if user.properties["wca:username"] = username>
        	<#assign storeId=wp.properties["wca:avmstore"]>
            
            <#assign sandbox=avm.userSandboxStore(storeId, username)>
            { name:"${wp.name}",
              description:"${wp.description!}",
              store:"${storeId}--${username}",
            },
    	</#if>
    </#list>
</#list>
]}