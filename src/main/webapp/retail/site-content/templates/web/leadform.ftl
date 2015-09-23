<#import "/templates/web/library/hubspot/hubspot.ftl" as hubspot />

<div>
	<div id="form-${model.leadFormId}" style='float:${model.float}; padding: 20px; width:${model.width_i}px; min-height:400px;'> 
    <#if  RequestParameters['preview']??>
    	<div style="width:100%; height=100%; border=1px solid black !important; background: orange !important; min-height: 400px; padding-top: 100px; text-align: center;"><h1>Hubspot<br/><br/>Form</h1></div>
    <#else>    
		<@hubspot.renderForm model.leadFormId />
    </#if>
	</div>
</div>