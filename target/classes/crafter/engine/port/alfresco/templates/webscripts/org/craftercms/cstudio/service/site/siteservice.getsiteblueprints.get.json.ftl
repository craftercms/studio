{
  blueprints: [
     <#list blueprints as blueprint>
        { id: "${blueprint.name}" }<#if blueprint_has_next>,</#if>
     </#list>
  ]
}
