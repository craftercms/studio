<#assign uname = "rdanner617@gmail.com" />
<#assign password = "silverDiner20191tJGj7P23qQj10zOk5jAlZUUc" />

<#assign query = "select Id, lastModifiedDate, campaignId, contactId from CampaignMember " />
<#assign campaignMembers = SalesforceService.executeQuery(query, uname, password) />
  
  <#if campaignMembers??>
  [
	<#list campaignMembers as campaignMember>
	  <#assign query = "select Id, name from Campaign where id = '" + campaignMember.campaignId + "'" /> 
	  <#assign campaign = SalesforceService.executeQuery(query, uname, password)[0] />
	  
	  <#assign query = "select Id, opportunityId, role from OpportunityContactRole where contactId = '" + campaignMember.contactId + "'" /> 
	  <#assign oppertunityContactRoles = SalesforceService.executeQuery(query, uname, password)![] />
	
	  { id: "${campaign.id}", name: "${campaign.name}", contactCount: ${oppertunityContactRoles?size},

 	  oppertunities: [
	  <#if oppertunityContactRoles?size == 0>

  	   {	id: "no-oppertunity", 
	        name: "no oppertunity",
	        modifyDate: "XYX",
			isClosed: false,
			stageName: "none",
			probability: 0.0,
		
	  	  <#assign query = "select Id, firstName, lastName from Contact where id = '" + campaignMember.contactId + "'" /> 
	  	  <#assign contact = SalesforceService.executeQuery(query, uname, password)[0] />
			contact: { id: "${contact.id}", 
			           firstName: "${contact.firstName}", 
					   lastName: "${contact.lastName}", 
					   role: "Unknown" }
		}
	  <#else>
		  <#list oppertunityContactRoles as oppertunityContactRole>  
	   
			  <#assign query = "select Id, name, lastModifiedDate, isClosed, stageName, probability from Opportunity where id = '" + oppertunityContactRole.opportunityId + "'" /> 
			  <#assign oppertunity = SalesforceService.executeQuery(query, uname, password)[0] />
		  		  
		  	   {	id: "${oppertunity.id}", 
			        name: "${oppertunity.name}",
			        modifyDate: "${oppertunity.lastModifiedDate.getTime()?string("MMM/dd/yyyy")}",
					isClosed: ${oppertunity.isClosed?string},
					stageName: "${oppertunity.stageName}",
					probability: ${oppertunity.probability},
				
			  	  <#assign query = "select Id, firstName, lastName from Contact where id = '" + campaignMember.contactId + "'" /> 
			  	  <#assign contact = SalesforceService.executeQuery(query, uname, password)[0] />
					contact: { id: "${contact.id}", 
					           firstName: "${contact.firstName}", 
							   lastName: "${contact.lastName}", 
							   role: "${oppertunityContactRole.role!"unknown"}" }

			   }<#if oppertunityContactRole_has_next = true>,</#if>
		   </#list>
		   </#if>
	   ]
      }<#if campaignMember_has_next = true>,</#if>   
    </#list>
	
	]
  </#if>
  