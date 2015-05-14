<#include "/templates/system/common/cstudio-support.ftl" />
<#include "/templates/web/navigation/navigation.ftl">
<#include "/templates/web/library/social-support.ftl"/>



<!--get query vaues-->
<#assign productsPerPage = model.itemsPerPage?number />
<#assign pageNum = (RequestParameters["p"]!1)?number - 1 />
<#assign uri = requestContext.requestUri />
<#assign gender = uri?substring(1, uri?index_of("/", 2)) />
<#assign category = uri?substring(uri?index_of("/", 3)+1, uri?last_index_of("/")) />
 <#assign collection = uri?substring(uri?last_index_of("/")+1) />

<#assign sort = (Cookies["category-sort"]!"")?replace("-", " ")>
<#assign filterSize = (Cookies["category-filter-size"]!"*")>
<#assign filterColor = (Cookies["category-filter-color"]!"*")>
<#assign keyword = (RequestParameters["q"]!"") />

<#if gender == "womens" >
   <#assign gender = "female" />
<#elseif gender == "mens">
   <#assign gender = "men" />
<#else>
   <#assign gender = "" />
</#if>




<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Rosie Rivet - Crafter CMS Demo Site</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">

    <link href="/static-assets/css/main.css" rel="stylesheet">

    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

	<script>
	  var setCookie = function(name, value) {
	  	document.cookie = name + "=" + value + "; path=/;"; 
	  	document.location = document.location;
	  	return false;
	  }
	</script>
</head>
<body>
	<@facebookSupport />

<!-- build and execute query -->
    <#assign queryStatement = 'crafterSite:"rosie" ' />
    
    <#if keyword != "">
   	  <#assign queryStatement = queryStatement + 'AND description:*' + keyword + '* ' />
    </#if>
    <#assign queryStatement = queryStatement + 'AND content-type:"/component/jeans" ' />
	<#assign queryStatement = queryStatement + 'AND gender.item.key:"' + gender + '" ' />
	<#assign queryStatement = queryStatement + 'AND category:"' + category + '" ' />
	<#assign queryStatement = queryStatement + 'AND collection.item.key:"' + collection + '" ' />
	
	<#assign filteredQueryStatement = queryStatement />
	<#assign filteredQueryStatement = filteredQueryStatement + 'AND size.item.value:"' + filterSize + '" ' />
	<#assign filteredQueryStatement = filteredQueryStatement + 'AND color:"' + filterColor + '" ' />

	<#assign query = searchService.createQuery()>
	<#assign query = query.setQuery(queryStatement) />
	<#assign query = query.addParam("facet","on") />
    <#assign query = query.addParam("facet.field","size.item.value") />
    <#assign query = query.addParam("facet.field","color") />

	
	<#assign filteredQuery = searchService.createQuery()>		
	<#assign filteredQuery = filteredQuery.setQuery(filteredQueryStatement) />
	<#assign filteredQuery = filteredQuery.setStart(pageNum)>
	<#assign filteredQuery = filteredQuery.setRows(productsPerPage)>

	
	<#if sort?? && sort != "">
		<#assign filteredQuery = filteredQuery.addParam("sort","" + sort) />
	</#if>

	<#assign executedQuery = searchService.search(query) />	
	<#assign executedFilteredQuery = searchService.search(filteredQuery) />

	<#assign productsFound = executedFilteredQuery.response.numFound>	
	<#assign products = executedFilteredQuery.response.documents />
	<#assign sizes = executedQuery.facet_counts.facet_fields['size.item.value'] />
	<#assign colors = executedQuery.facet_counts.facet_fields['color'] />
    



<div id="main-container" class="categories-page">
            	
<#include "/templates/web/fragments/header.ftl"/>


<div class="container-fluid" id="content-body">
    
    <div class="row-fluid">
        <div class="span3 mb10" id="site-nav">
        
        	<div class="input-append" id="site-search">
	        	<input type="text" class="wauto" placeholder="search" />
	        	<a class="add-on">
		        	<i class="icon icon-search"></i>
	        	</a>
        	</div>
            
			<ul class="nav nav-list amaranth uppercase">
				<@renderNavigation "/site/website", 1 />	
			</ul>
            
        </div>
        <div class="span12" id="content">
            
<!-- body content -->
<div class="pull-left">
	            <strong class="uppercase">Sort By:</strong>
	            <select onchange="setCookie('category-sort', this.value);">
	            	<option  <#if sort=="arrivalDate_dt desc">selected</#if> value="arrivalDate_dt-desc">New Arrivals</option>
	            	<!--option  <#if sort=="collection.item.value asc">selected</#if> value="collection.item.value-asc">Collection</option-->
	            	<!--option>Best Sellers</option-->
	            	<option  <#if sort=="price_d desc">selected</#if>  value="price_d-desc">Price: High to Low</option>
	            	<option  <#if sort=="price_d asc">selected</#if> value="price_d-asc">Price: Low to High</option>
	            </select>
            </div>

            <div class="pull-right clearfix">
	            <strong class="uppercase">Filter By:</strong>

	            <select style="width: 90px"  onchange="setCookie('category-filter-size', this.value);">
	                <option <#if filterSize=='*'>selected</#if> value="*">Size</option>
	            	<#list sizes?keys as sizeOption>
		            	<option <#if filterSize==sizeOption>selected</#if> value="${sizeOption}">${sizeOption} (${sizes[sizeOption]})</option>
		            </#list>
	            </select>
	            
	            <select style="width: 90px"  onchange="setCookie('category-filter-color', this.value);">
	                <option <#if filterColor=='*'>selected</#if> value="*">Color</option>
	            	<#list colors?keys as colorOption>
		            	<option <#if filterColor==colorOption>selected</#if> value="${colorOption}">${colorOption} (${colors[colorOption]})</option>
		            </#list>
	            </select>           
	            
	            <select style="width: 90px"  onchange="setCookie('category-filter-fitdetail', this.value);">
	                <option selected value="*">Fit Detail</option>
	            </select>  
	            
            </div>
            
            <div class="categories clearfix">

            		<#list products as product>
            			<#assign productId = product.localId?substring(product.localId?last_index_of("/")+1)?replace('.xml','')>
            			<@ice componentPath=product.localId />
            				   
 		            	<div id="socialised1" class="cat">
 		            		<img src="${product.frontImage}" />
		            		
		            		
		            			

		            		<div class="title" style='width:170px;'><a href="/womens/jeans/details?p=${productId}">${product.productTitle}</a></div>
		            		<div class="price">${product.price_d?string.currency}</div>
							<div class="rating">
		            			<!--span class="star checked"></span>
		            			<span class="star checked"></span>
		            			<span class="star checked"></span>
		            			<span class="star checked"></span>
		            			<span class="star"></span-->
		            			
		            			<@facebookLike contentUrl='http://www.rosiesrivets.com/womens/jeans/details?p=${productId}' width="75" faces="false" layout="button_count"/>
		            		</div>

		            	</div>
					</#list>
            
            </div>
            
            <div class="pagination">
            	<ul>
            		<#assign pages = (productsFound / productsPerPage)?round />
            		<#if pages == 0><#assign pages = 1 /></#if>
            		
            		<#list 1..pages as count>
            			<li <#if count=(pageNum+1) >class="active"</#if>><a href="${uri}?p=${count}">${count}</a></li>
            		</#list>
            	</ul>
            </div>




        </div>
    </div>

<div id="sumer11"></div>

    <hr>
    <#include "/templates/web/fragments/footer.ftl"/>
</div>
<!-- /container -->

</div>



<script>
    function extendForRating ( director, CrafterSocial ) {
	    var S = CrafterSocial,
			C = S.Constants,
	        $ = S.$,
	        Model = S.Backbone.Model;
			
		function POST (url, attributes, data) {
			    var thisUrl = CrafterSocial.url(url, this.toJSON());
				
		        this.save(data, {
		            url: "/crafter-social/api/2/ugc/create.json",
		            type: 'POST'
		        });
		};

		function UPDATE (url, attributes, data) {
			    var thisUrl = CrafterSocial.url(url, this.toJSON());
				
		        this.save(data, {
		            url: "/crafter-social/api/2/ugc/update.json",
		            type: 'POST'
		        });
		};
				
		function createRating() {
			return { 
				"profile": {
					"displayName":"You",
					"roles":["SOCIAL_ADVISORY","SOCIAL_ADMIN","SOCIAL_MODERATOR"],
					"id":"",
					"userName":"admin",
					"password":"",
					"active":false,
					"created":Date.now(),
					"modified":Date.now(),
					"tenantName":"",
					"email":""
				},
				"dateAdded":Date.now(),
				"createdDate":Date.now(),
				"lastModifiedDate":  Date.now(),	
				"tenant": ((this.attributes) ? this.attributes.tenant : ""), 
				"targetId": ((this.attributes) ? this.attributes.target : ""),   
				"id":null,              
				"profileId":"",
				"targetDescription":"",
				"targetUrl":"",
				"timesModerated":0,
				"attributes": {
			    	'oneCount' :   0,
			    	'twoCount' :   0,
			    	'threeCount' : 0,
			    	'fourCount' :  0,
			    	'fiveCount' :  0,
			    	'totalCount' : 0 
				}
			};
		};

		var Rater = CrafterSocial.Backbone.Model.extend({
			idAttribute: 'id',
			RATING: {
				ONE: 1,
			    TWO: 2,
			    THREE: 3,
				FOUR: 4,
				FIVE: 5
			},
    		defaults: createRating(),    	

	        url: function () {
				var url;
			
	            if (this.isNew()) {
	                url = S.url('ugc.create');
	            } else {
	                url = S.url('ugc.target', this.attributes);
	            }
			
				return url;
	        },

	        getRating: function () {
				var url = S.url('ugc.target', this.attributes);
			
				var results = this.fetch( { url: url,  async:false, data: this.attributes } ).responseJSON;
				
				return results.list[results.list.length-1];
	        },

	        setRating: function(countType) {
				var isNew = false;
				var rating = this.getRating();
			
				if(!rating || !rating.attributes) {
					rating = createRating.call(this);
					isNew = true;
				}
				
				switch(countType) {
					case this.RATING.ONE:
						rating.attributes.oneCount++;
						break;
					case this.RATING.TWO:
						rating.attributes.twoCount++;
						break;
					case this.RATING.THREE:
						rating.attributes.threeCount++;
						break;
					case this.RATING.FOUR:
						rating.attributes.fourCount++;
						break;
					case this.RATING.FIVE:
						rating.attributes.fiveCount++;
						break;
				};
					
				rating.attributes.totalCount = 
					  rating.attributes.oneCount
					+ rating.attributes.twoCount
					+ rating.attributes.threeCount
					+ rating.attributes.fourCount
					+ rating.attributes.fiveCount;

				if(isNew == true) {
					POST.call(this, 'ugc.rating', {}, rating);
				}
				else {
					UPDATE.call(this, 'ugc.rating', {}, rating);	
				}
			}
		});

	  	CrafterSocial.define('model.Rater', Rater);



// =====================================================

		(function (S) {
		    'use strict';

		    var Class,
		        Superclass = S.view.Base,
		        C = S.Constants,
		        $ = S.$;

		    var STAR_EMPTY_CLASS = 'cs-icon-star-empty',
		        STAR_CLASS = 'cs-icon-star',
		        RATED_CLASS = 'rated';

		    Class = Superclass.extend({
		        icon: 'star',
		        title: 'Rate',
				rater: null,

 			    tagName: 'li',
		        events: {
		            'mouseover i' : 'highlight',
		            'mouseleave i' : 'dehighlight',
		            'click i' : 'select'
		        },
				
				render: function(config) {
		            var el = document.getElementById(config.target);
					this.rater = new CrafterSocial.model.Rater(config);
					el.innerHTML = Class.DEFAULTS.hardCodedTemplates.main;
					
					var rating = this.rater.getRating();
					
					var avg = (rating.attributes.totalCount == 0 ) ? 0 : 
					    ( (rating.attributes.oneCount   * 1)
						+ (rating.attributes.twoCount   * 2)
						+ (rating.attributes.threeCount * 3)
						+ (rating.attributes.fourCount  * 4)
						+ (rating.attributes.fiveCount  * 5)) / (rating.attributes.totalCount);
					
					var starEls = $('#'+config.target).find('i'); 
					
					for(var idx in starEls) {
						var el = $(starEls[idx]);
						el.removeClass(STAR_CLASS).addClass(STAR_EMPTY_CLASS);
						
						if(parseInt(el.context.attributes.score.nodeValue) <= avg) {
		                    el.removeClass(STAR_EMPTY_CLASS).addClass(STAR_CLASS);
						}
					}  
				},
				
		        initialize: function(config) {
					
		            var newConfig = $.extend({}, Class.DEFAULTS, config);
		            Superclass.prototype.initialize.call(this, newConfig);
		            this.$('.tool-tip').find('i').tooltip();					
		        },

		        highlight: function ( e ) {
		            var $me = $(e.target);
		            if (!$me.hasClass('rated')) {

		                $me.parent().find('i:not(.rated)')
		                    .removeClass(STAR_CLASS)
		                    .addClass(STAR_EMPTY_CLASS);
		                $me.prevAll().add($me)
		                    .removeClass(STAR_EMPTY_CLASS)
		                    .addClass(STAR_CLASS);
		            }
		        },
				
		        dehighlight: function ( e ) {
		            var $me = $(e.target);
		            if (!$me.hasClass('rated')) {
		                $me.parent().find('i:not(.rated)').removeClass(STAR_CLASS).addClass(STAR_EMPTY_CLASS);
		            }
		        },
				
		        select: function ( e ) {
		            var $me = $(e.target),
		                $iElements = $me.parent().find('i'),
		                previouslyRatedFirst = ($me.hasClass('rated') && $me.get(0) === $iElements.get(0) && $iElements.filter('.rated').length === 1),
		                classes = '%@ %@'.fmt(STAR_CLASS, RATED_CLASS);

		            $iElements
		                .removeClass(classes)
		                .addClass(STAR_EMPTY_CLASS);

		            if (!previouslyRatedFirst) {
		                $me.prevAll().add($me)
		                    .removeClass(STAR_EMPTY_CLASS)
		                    .addClass(classes);
		            }
					
					var score = $me.context.attributes.score.nodeValue;
					this.rater.setRating(parseInt(score));
		        }
		    });

		    Class.DEFAULTS = {
		        classes: ['crafter-social-bar-form2', 'crafter-social-bar-rate2'],
		        templates: { main: null  },
				hardCodedTemplates: {
		            main: ['<div class="crafter-social-view crafter-social-bar-form2 crafter-social-bar-rate2 crafter-social-bar-widget2" style="display: block;">',
					       '<div class="clearfix">',
		                   '<div class="form-group star-rating">',
		                   '<i class="crafter-social-icon cs-icon-star-empty" title="Poor" score="1"></i>',
		                   '<i class="crafter-social-icon cs-icon-star-empty" title="Alright" score="2"></i>',
		                   '<i class="crafter-social-icon cs-icon-star-empty" title="Quite Good" score="3"></i>',
		                   '<i class="crafter-social-icon cs-icon-star-empty" title="Real Good" score="4"></i>',
		                   '<i class="crafter-social-icon cs-icon-star-empty" title="Fantastic" score="5"></i>',
		                '</div>',
		                '</div>',
						'</div>'].join("")
		        }
		    };

		    S.define('view.Rater', Class);

		}) (crafter.social);


        var rateWidget = new crafter.social.view.Rater({el: $('#sumer11')});
		rateWidget.render({tenant:"craftercms", target:"sumer11"});
		

	}
</script>

<!-- - - - - - - - -->
<!-- - - - SUI - - -->
<!-- - - - - - - - -->
<script>

    var crafterSocial_cfg = {

        // The SUI base URL
        'url.base'                      : '/static-assets/sui/',
        // The fixtures URL. May be relative.
       // 'url.service'                   : '/static-assets/sui/fixtures/api/2/',
	    'url.service'                   : '/crafter-social/api/2/',
        // The Templates URL. May be relative.
        'url.templates'                 : '/static-assets/sui/templates/',
        // 'url.security'                  : '...',
        // 'url.ugc.file'                  : '{attachmentId}.json',
        // 'url.ugc.{id}.get_attachments'  : '.json?tenant={tenant}',
        // 'url.ugc.{id}.add_attachment'   : '.json'
		'url.ugc.rating' : '/ugc/create.json', 
    };
   


    function crafterSocial_onAppReady ( director, CrafterSocial ) {

Xdirector=director;
        director.socialise({
            target: '#socialised1',
            tenant: 'craftercms'
        });

        director.socialise({
            target: '#socialised2',
            tenant: 'craftercms'
        });

        director.socialise({
            target: '#socialised3',
            tenant: 'craftercms'
        });

        // Initialise the "session user".
        director.setProfile({
            displayName: 'You',
            roles: [
                'SOCIAL_ADVISORY',
                'SOCIAL_ADMIN',
                'SOCIAL_MODERATOR'
            ]
        });

//extendForRating ( director, CrafterSocial );

    }

</script>


<link rel="stylesheet" href="/static-assets/sui/styles/main.css" />
<script type="text/javascript" src="/static-assets/sui/scripts/social.js"></script>
<style>
    .crafter-social-view.crafter-social-discussion-view.crafter-social-popover {
        bottom: auto;
    }
    .crafter-social-bar,
    .crafter-social-view.crafter-social-commentable-options {
        z-index: 11;
    }
    .modal-body .mod-content p.highlight:last-child:after {
        margin-bottom: 20px;
    }
    .crafter-social-view.crafter-social-bar-form,
    .crafter-social-view.crafter-social-bar-form.crafter-social-bar-feedback table.table,
    .crafter-social-view.crafter-social-bar-form.crafter-social-bar-widget table.table {
        color: #ebebeb !important;
    }
    .crafter-social-view.crafter-social-bar-form input[type="text"],
    .crafter-social-view.crafter-social-bar-form.crafter-social-bar-widget input[type="text"],
    .crafter-social-view.crafter-social-bar-form textarea,
    .crafter-social-view.crafter-social-bar-form.crafter-social-bar-widget textarea,
    .crafter-social-view.crafter-social-bar-form select,
    .crafter-social-view.crafter-social-bar-form.crafter-social-bar-widget select {
        color: #333;
    }
</style>
<!-- - - - - - - - -->
<!-- - - - SUI - - -->
<!-- - - - - - - - -->


<script src="/static-assets/js/jquery.min.js"></script>
<script src="/static-assets/js/bootstrap.min.js"></script>
<script src="/static-assets/js/main.js"></script>



</body>
</html>
<@cstudioOverlaySupport/>