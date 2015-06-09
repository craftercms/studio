
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


        <div id="about" class="section primary-section">
            <div class="container">
                <div <@studio.iceAttr iceGroup="body"/> class="about-text">
                    ${model.body_html}
                </div>
            </div>
        </div>

		<@renderComponent componentPath="/site/components/footers/global-footer.xml" />

		<#include "/templates/web/common-page-scripts.ftl" />
        <@studio.toolSupport/> 
    </body>
</html>