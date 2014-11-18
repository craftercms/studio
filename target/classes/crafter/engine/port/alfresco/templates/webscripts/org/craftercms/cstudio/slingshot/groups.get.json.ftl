<#import "groups.lib.ftl" as groupLib/>
{
"groups" : [
   <#list grouplist as group>
      <@groupLib.groupSummaryJSON group=group/>
      <#if group_has_next>,</#if>
   </#list>
]
}
