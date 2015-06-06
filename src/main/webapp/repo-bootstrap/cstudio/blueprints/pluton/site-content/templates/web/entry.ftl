
<#import "/templates/system/common/cstudio-support.ftl" as studio />

<!DOCTYPE html>
<!-- 
 * A Design by GraphBerry 1
 * Author: GraphBerry
 * Author URL: http://graphberry.com
 * License: http://graphberry.com/pages/license
-->
<html lang="en">
    
    <head>
    	<#include "/templates/web/common-page-head.ftl" />
    </head>
    
    <body>

		<@renderComponent componentPath="/site/components/headers/global-header.xml" />

		<!-- Start home section -->
        <div id="home" <@studio.iceAttr iceGroup="banners"/>>
            <!-- Start cSlider -->
            <div id="da-slider" class="da-slider">
                <div class="triangle"></div>
                <!-- mask elemet use for masking background image -->
                <div class="mask"></div>
                <!-- All slides centred in container element -->
                <div class="container"  >
                    <!-- Start first slide -->

                    <#list model.banners.item as banner>
                    <div class="da-slide" >
                        <h2 class="fittext2">${banner.headline!''}</h2>
                        <h4>${banner.subheadline!''}</h4>
                        <p>${banner.explaination!''}</p>
                        <a href="#" class="da-link button">Read more</a>
                        <div class="da-img">
                            <img src="${banner.image!'NOIMAGE'}" alt="image01" width="320">
                        </div>
                    </div>
                    </#list>

                    <!-- Start cSlide navigation arrows -->
                    <div class="da-arrows">
                        <span class="da-arrows-prev"></span>
                        <span class="da-arrows-next"></span>
                    </div>
                    <!-- End cSlide navigation arrows -->
                </div>
            </div>
        </div>
        <!-- End home section -->
        <!-- Service section start -->
        <div class="section primary-section" id="service">
            <div  <@studio.iceAttr iceGroup="banners"/>  class="container">
                <!-- Start title section -->
                <div class="title">
                    <h1>What We Do?</h1>
                    <!-- Section's title goes here -->
                    <p>Duis mollis placerat quam, eget laoreet tellus tempor eu. Quisque dapibus in purus in dignissim.</p>
                    <!--Simple description for section goes here. -->
                </div>
                <div class="row-fluid">
                    <div class="span4">
                        <div class="centered service">
                            <div class="circle-border zoom-in">
                                <img class="img-circle" src="/static-assets/images/Service1.png" alt="service 1">
                            </div>
                            <h3>Modern Design</h3>
                            <p>We Create Modern And Clean Theme For Your Business Company.</p>
                        </div>
                    </div>
                    <div class="span4">
                        <div class="centered service">
                            <div class="circle-border zoom-in">
                                <img class="img-circle" src="/static-assets/images/Service2.png" alt="service 2" />
                            </div>
                            <h3>Powerfull Theme</h3>
                            <p>We Create Modern And Powerful Theme With Lots Animation And Features</p>
                        </div>
                    </div>
                    <div class="span4">
                        <div class="centered service">
                            <div class="circle-border zoom-in">
                                <img class="img-circle" src="/static-assets/images/Service3.png" alt="service 3">
                            </div>
                            <h3>Clean Code</h3>
                            <p>We Create Modern And Powerful Html5 And CSS3 Code Easy For Read And Customize.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

		<div  <@studio.componentContainerAttr target="col1Modules" />>
          <#list model.col1Modules.item as module>
              <@renderComponent component=module />
          </#list>
        </div>


        
        <@renderComponent componentPath="/site/components/footers/global-footer.xml" />

		<#include "/templates/web/common-page-scripts.ftl" />
        <@studio.toolSupport/> 
    </body>
</html>