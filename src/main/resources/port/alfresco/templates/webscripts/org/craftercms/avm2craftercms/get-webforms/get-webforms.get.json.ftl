<#assign query="TYPE:\"{http://www.alfresco.org/model/wcmappmodel/1.0}webfolder\"">
<#assign username=person.properties.userName>

{ avmProjectForms: [
<#list companyhome.childrenByLuceneSearch[query]?sort_by('name') as wp>
	<#list wp.childAssocs["wca:webuser"] as user>
         <#if user.properties["wca:username"] = username>
        	<#assign storeId=wp.properties["wca:avmstore"]>
			<#assign sandboxId=wp.properties["wca:avmstore"]+"--"+username>
			<#if sandboxId == args.projectId>
				<#list wp.childAssocs["wca:webform"] as form>
	            	{ name: "${form.properties['wca:formname']}",
	            	  title: "${form.properties.title}",
	            	  description: "${form.properties.description}"
	            	},
	           </#list>
	    	</#if>
    	</#if>
    </#list>
</#list>
]}