
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
    
    <body >

		<@renderComponent componentPath="/site/components/headers/global-header.xml" />

        <div class="section primary-section" id="service">
            <div class="container">
                <!-- Start title section -->
                <div class="title">
                    <h1>What We Do?</h1>
                    <!-- Section's title goes here -->
                    <p>Duis mollis placerat quam, eget laoreet tellus tempor eu. Quisque dapibus in purus in dignissim.</p>
                    <!--Simple description for section goes here. -->
                </div>
                <div class="row-fluid">
                    <div class="span4" <@studio.componentContainerAttr target="zone2" />>
						<#if model.zone2?? && model.zone2.item??>
                          <#list model.zone2.item as module>
                              <@renderComponent component=module />
                          </#list>
                        </#if>
					</div>
                    <div class="span4" <@studio.componentContainerAttr target="zone3"/>>
                       
						<#if model.zone3?? && model.zone3.item??>
                          <#list model.zone3.item as module>
                              <@renderComponent component=module />
                          </#list>
                        </#if>
                        
                    </div>
                    <div class="span4" <@studio.componentContainerAttr target="zone4"/>>
						<#if model.zone4?? && model.zone4.item??>
                          <#list model.zone4.item as module>
                              <@renderComponent component=module />
                          </#list>
                        </#if>
					</div>
                </div>
            </div>
            
            <br/><br/><br/><br/><br/>
        </div>
        
        

		<@renderComponent componentPath="/site/components/footers/global-footer.xml" />
   
		<#include "/templates/web/common-page-scripts.ftl" />
        <@studio.toolSupport/> 
    </body>
</html>