<#import "/templates/web/navigation2/navigation.ftl" as nav/>

<div id="sidebar">
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

    <!-- Section -->
    <#if sidebarArticles?? && sidebarArticles?size &gt; 0>
    <section>
      <header class="major">
        <h2>${contentModel.articles_section_header}</h2>
      </header>
      <div class="mini-posts">
      <#list sidebarArticles as article>
        <#if article.image??>
          <#assign articleImage = article.image/>
        <#else>
          <#assign articleImage = "/static-assets/images/placeholder.png"/>
        </#if>
        <article>
          <a href="${article.url}" class="image"><img src="${articleImage}" alt="" /></a>
          <h4><a href="${article.url}">${article.title}</a></h4>
        </article>
      </#list>
      </div>
    </section>
    </#if>

    <!-- Section -->
    <section>
      <header class="major">
        <h2>Get in touch</h2>
      </header>
      <p>Sed varius enim lorem ullamcorper dolore aliquam aenean ornare velit lacus, ac varius enim lorem ullamcorper dolore. Proin sed aliquam facilisis ante interdum. Sed nulla amet lorem feugiat tempus aliquam.</p>
      <ul class="contact">
        <li class="fa-envelope-o"><a href="#">information@untitled.tld</a></li>
        <li class="fa-phone">(000) 000-0000</li>
        <li class="fa-home">1234 Somewhere Road #8254<br />
          Nashville, TN 00000-0000</li>
      </ul>
    </section>

    <!-- Footer -->
    <footer id="footer">
      <p class="copyright">&copy; Untitled. All rights reserved. Demo Images: <a href="https://unsplash.com">Unsplash</a>. Design: <a href="https://html5up.net">HTML5 UP</a>.</p>
    </footer>

  </div>
</div>
