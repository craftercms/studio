<#include "/templates/system/common/cstudio-support.ftl" />
<#include "/templates/web/navigation/navigation.ftl">

<!DOCTYPE html>
<html lang="en">
  <head>
	<title>Global Integrity</title>
    
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link href="/static-assets/css/bootstrap-responsive.css" rel="stylesheet">
	<link href="/static-assets/css/main.css" rel="stylesheet">

  </head>
  <body>
  	<div class="interior-page article-page wrapper">
		
  		<header>
  			<div class="top">
  				<div class="pad">
  					
					<nav>
						<ul class="main-nav clearfix">
                           <@renderNavigation "/site/website", 2 />
                        </ul>
					</nav>
                    
                    <#include "/templates/web/common/page-actions.ftl" />
				</div>
			</div>
  			<div class="bottom">
  				<div class="pad">
                    <h1 class="off-text logo" href="/">Global Integrity</h1>
                    <#include "/templates/web/common/search.ftl" />
  					
  					<div class="museo300 header-slogan">
						<@ice id='title' />
                        <div class="title-small mb5">ARTICLES</div>
  						<span class="title-larger lh1-2 weight-normal">
	  						${model.title}
  						</span>
					</div>
					
  				</div>
  			</div>
		</header>

        <div class="content arial">	
			<div class="row-fluid mt20 mb20">
				<div class="span8">
                  <@ice id='body' />
                  ${model.body_html!""}
				</div>
				<div class="span4">
					
					<div class="museo300 article-img-wrapper pull-right mt20">
						<a href="javascript:void(null)" class="block">
							<img src="${model.authorImage}" src="${model.author}" />
						</a>
                        <@ice id='author' />
						<div class="desc">${model.author}</div>
						<div class="desc">${model.publishDate_dt}</div>

					<@componentZone id="bottomPromos1">
						<#if model.bottomPromos1?? && model.bottomPromos1.item??>
		       				<#list model.bottomPromos1.item as module>
		        				    <@draggableComponent component=module  >
			        					<@renderComponent component=module />
			        				</@draggableComponent>
		       				</#list>
		       			</#if>
					</@componentZone>
					</div>
                    
				</div>
			</div>
		</div>
        
		
		<#include "/templates/web/common/footer.ftl" />

	</div>

    <script src="/static-assets/js/jquery-1.10.2.min.js"></script>
    <script src="/static-assets/js/bootstrap.min.js"></script>
    <script src="/static-assets/js/main.js"></script>
    <@cstudioOverlaySupport/>
  </body>
</html>