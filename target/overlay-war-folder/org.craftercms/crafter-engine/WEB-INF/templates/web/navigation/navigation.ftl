<#import "/templates/web/navigation/nav-functions.ftl" as navFunctions>
<#import "/templates/web/navigation/main/nav-macros.ftl" as mainNavMacros>
<#import "/templates/web/navigation/main/breadcrumb-macros.ftl" as mainBreadcrumbMacros>

<#macro renderNavigationLevel currLevel maxLevel navTree macroNs>
    <#assign navItemCount = navTree.childItems?size>
    <#assign navItems = navTree.childItems>
    <#list navItems as navItem>
        <#assign navigable = true>

        <#-- Ignore index.xml here since index.xml is used only for folders -->
        <#if navItem.storeName == "index.xml">
            <#assign navigable = false>
        </#if>

        <#if navItem.folder>
            <#assign indexItem = navItem.getChildItem("index.xml")!>
            <#if indexItem != "">
                <#if indexItem.disabled?? && indexItem.disabled?lower_case == "true">
                    <#assign navigable = false>
                </#if>
            <#else>
                <#assign navigable = false>
            </#if>
        </#if>

        <#if navigable>
            <#if model.storeUrl?starts_with(navItem.storeUrl)>
                <#assign active = true>
            <#else>
                <#assign active = false>
            </#if>

            <#assign navItemChildCount = navItem.childItems?size>
            <#if currLevel == 1>
                <#if (navItemChildCount >= 2) && (currLevel < maxLevel)>
                    <@macroNs.renderNavItemWithSubItems navItem, active>
                        <@renderNavigationLevel currLevel + 1, maxLevel, navItem, macroNs />
                    </@macroNs.renderNavItemWithSubItems>
                <#else>
                    <@macroNs.renderNavItem navItem, active />
                </#if>
            <#else>
                <#if (navItemChildCount >= 2) && (currLevel < maxLevel)>
                    <@macroNs.renderNavSubItemWithSubItems navItem, active>
                        <@renderNavigationLevel currLevel + 1, maxLevel, navItem, macroNs />
                    </@macroNs.renderNavSubItemWithSubItems>
                <#else>
                    <@macroNs.renderNavSubItem navItem, active />
                </#if>
            </#if>
        </#if>
    </#list>
</#macro>

<#macro renderNavigation navTreeUrl levels includeByNameRegex = "" excludeByNameRegex = "" nodeXPathAndExpectedValuePairs = [["*/placeInNav", "true"]] ns = mainNavMacros>
    <#assign navTree = siteItemService.getSiteTree(navTreeUrl, levels + 1, includeByNameRegex, excludeByNameRegex, nodeXPathAndExpectedValuePairs)>
    <@renderNavigationLevel 1, levels, navTree, ns />
</#macro>

<#macro renderBreadcrumb url macroNs = mainBreadcrumbMacros>
    <#assign breadcrumb = breadcrumbBuilder.buildBreadcrumb(url)>
    <#list breadcrumb as item>
        <#if item_has_next>
            <@macroNs.renderBreadcrumbItem item, false />
        <#else>
            <@macroNs.renderBreadcrumbItem item, true />
        </#if>
    </#list>
</#macro>