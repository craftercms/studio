<#import "/templates/system/common/cstudio-support.ftl" as studio />
<#import "/templates/web/navigation2/navigation.ftl" as nav/>

<div id="left-rail" <@studio.componentAttr path=contentModel.storeUrl ice=true />>
  <div class="inner">

    <!-- Search -->
    <section id="search" class="alt">
      <form method="post" action="#">
        <input type="text" name="query" id="query" placeholder="Search" />
      </form>
    </section>

    <!-- Menu -->
    <nav id="menu">
      <header class="major">
        <h2>Menu</h2>
      </header>
      <ul>
		<@nav.renderNavigation "/site/website" 1 true/>
      </ul>
    </nav>

    <!-- Widgets -->
    <#if articleCategories?? && articlePath??>
    	<#assign additionalModel = {"articleCategories": articleCategories, "articlePath": articlePath } />
    <#else>
    	<#assign additionalModel = {} />
    </#if>
    <#list contentModel.widgets.item as widget>
      <@renderComponent component = widget additionalModel = additionalModel />
    </#list>

    <!-- Footer -->
    <footer id="footer">
      <p class="copyright">&copy; Untitled. All rights reserved. Demo Images: <a href="https://unsplash.com">Unsplash</a>. Design: <a href="https://html5up.net">HTML5 UP</a>.</p>
    </footer>

  </div>
</div>
