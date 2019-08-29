<#if articles?? && articles?size &gt; 0>
<section>
	<header class="major">
		<h2>${contentModel.title_t}</h2>
	</header>
	<div class="mini-posts">
		<#list articles as article>
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
