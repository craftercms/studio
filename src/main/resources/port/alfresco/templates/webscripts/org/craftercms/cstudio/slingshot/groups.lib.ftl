<#macro groupSummaryJSON group>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "groupName" : "${group.properties["usr:authorityName"]}"
}
</#escape>
</#macro>
