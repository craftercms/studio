<#macro registerCampaignStateForContact contactID, campaignID, pageId, state >
  <#assign uname = "X" />
  <#assign password = "X" />
  <#assign results = SalesforceService.updateContactWithCampaign(contactID, campaignID, state, uname, password) />
</#macro>