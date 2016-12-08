<#macro renderNavItem item active = false>
<li <#if active>class="selected"</#if>><a href="${navFunctions.getNavItemUrl(item)}">${navFunctions.getNavItemName(item)}</a></li>
</#macro>

<#macro renderNavItemWithSubItems item active = false>
<li <#if active>class="selected"<#else></#if>>
    <a  href="${navFunctions.getNavItemUrl(item)}">${navFunctions.getNavItemName(item)}</a>
    <#if active == true>
	<span class="arrow"></span>
      <ul class="subnav arial">
        <#nested>
      </ul>
    </#if>
</li>
</#macro>

<#macro renderNavSubItem item active = false>
<li <#if active>class="selected"</#if>><a href="${navFunctions.getNavItemUrl(item)}">${navFunctions.getNavItemName(item)}</a></li>
</#macro>

<#macro renderNavSubItemWithSubItems item active = false>
<li>
    <a href="${navFunctions.getNavItemUrl(item)}">${navFunctions.getNavItemName(item)}</a>
    <ul>
        <#nested>
    </ul>
</li>
</#macro>