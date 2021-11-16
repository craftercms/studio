<#import "/templates/system/common/crafter.ftl" as crafter />

<@crafter.div id="sidebar">
  <div class="inner">

    <!-- Search -->
    <section id="search" class="alt">
      <form method="post" action="#">
        <input type="text" name="query" id="query" placeholder="Search"/>
      </form>
    </section>

    <!-- Menu -->
    <nav id="menu">
      <header class="major">
        <h2>Menu</h2>
      </header>
      <@crafter.navigation url="/site/website" showNavElement=false />
    </nav>

    <!-- Widgets -->
    <#if contentModel.widgets_o.item?has_content>
      <#if articleCategories?? && articlePath??>
        <#assign additionalModel = {
        <#---->"articleCategories": articleCategories,
        <#---->"articlePath": articlePath
        } />
      <#else>
        <#assign additionalModel = {} />
      </#if>
      <@crafter.renderComponentCollection $field="widgets_o" renderComponentArguments=additionalModel/>
    </#if>
    <!-- /Widgets -->

    <!-- Footer -->
    <footer id="footer">
      <p class="copyright">
        &copy; Untitled. All rights reserved. Demo Images:
        <a href="https://unsplash.com">Unsplash</a>. Design: <a href="https://html5up.net">HTML5 UP</a>.
      </p>
    </footer>
    <!-- /Footer -->

  </div>
</@crafter.div>
