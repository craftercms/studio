<#macro countToRange bottom=1  top=5 >
  <#list bottom..top as count>
      ${count}<#if count != top>,</#if>
  </#list>
</#macro>