<#macro groupSummaryJSON group>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "role" : "${group.properties["usr:aut"]}"
}
</#escape>
</#macro>
