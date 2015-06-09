<#import "/templates/system/common/cstudio-support.ftl" as studio />
<div <@studio.componentAttr path=model.storeUrl ice=true iceGroup="content" /> >${model.content_html!''}</div>
