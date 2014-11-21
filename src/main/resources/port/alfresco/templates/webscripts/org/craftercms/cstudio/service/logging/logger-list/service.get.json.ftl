{ loggers: [
<#list loggers?keys as loggerName>
   <#assign logger = loggers[loggerName] />
   
	{ "name": "${loggerName}", "level": "${logger.level}" },  
		
</#list>
] }
