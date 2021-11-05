<#import "/templates/system/common/crafter.ftl" as crafter />

<!--
	Editorial by HTML5 UP
	html5up.net | @ajlkn
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
-->
<!doctype html>
<html lang="en">
<head>
  <#include "/templates/web/fragments/head.ftl">
  <@crafter.head/>
</head>
<body class="is-preload">
<@crafter.body_top/>

<!-- Wrapper -->
<div id="wrapper">

  <!-- Main -->
  <div id="main">
    <div class="inner">

      <!-- Header -->
      <@crafter.renderComponentCollection $field="header_o"/>
      <!-- /Header -->

      <!-- Banner -->
      <section id="banner">
        <div class="content">
          <@crafter.header $field="hero_title_html">
            ${contentModel.hero_title_html}
          </@crafter.header>
          <@crafter.div $field="hero_text_html">
            ${contentModel.hero_text_html}
          </@crafter.div>
        </div>
        <span class="image object">
          <@crafter.img $field="hero_image_s" src=(contentModel.hero_image_s!"") alt=""/>
        </span>
      </section>
      <!-- /Banner -->

      <!-- Section: Features -->
      <section>
        <header class="major">
          <@crafter.h2 $field="features_title_t">
            ${contentModel.features_title_t}
          </@crafter.h2>
        </header>
        <@crafter.renderComponentCollection
          $field="features_o"
          $containerAttributes={ "class": "features" }
          $itemAttributes={ "class": "feature-container" }
        />
      </section>
      <!-- /Section: Features -->

      <!-- Section: Articles -->
      <section>
        <header class="major">
          <h2>Featured Articles</h2>
        </header>
        <div class="posts">
          <#list articles as article>
            <@crafter.article $model=article>
              <a href="${article.url}" class="image">
                <#--
                Note for docs:
                Works: src=article.image???then(article.image, "/static-assets/images/placeholder.png")
                Error: src="${article.image???then(article.image, "/static-assets/images/placeholder.png")}" 🤷
                however...
                Works: href="${article.url}"
                -->
                <@crafter.img
                  $model=article
                  $field="image_s"
                  src=article.image???then(article.image, "/static-assets/images/placeholder.png")
                  alt=""
                />
              </a>
              <h3>
                <@crafter.a $model=article $field="subject_t" href="${article.url}">
                  ${article.title}
                </@crafter.a>
              </h3>
              <@crafter.p $model=article $field="summary_t">
                ${article.summary}
              </@crafter.p>
              <ul class="actions">
                <li>
                  <a href="${article.url}" class="button">More</a>
                </li>
              </ul>
            </@crafter.article>
          </#list>
        </div>
      </section>
      <!-- /Section: Articles -->

    </div>
  </div>
  <!-- /Main -->

  <!-- Left Rail -->
  <@crafter.renderComponentCollection $field="left_rail_o" />
  <!-- /Left Rail -->

</div>
<!-- /Wrapper -->

<#include "/templates/web/fragments/scripts.ftl">
<@crafter.body_bottom/>

</body>
</html>
