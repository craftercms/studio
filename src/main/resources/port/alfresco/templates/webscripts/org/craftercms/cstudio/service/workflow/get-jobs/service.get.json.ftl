[
<#list jobs as job>
	{
		"id": "${job.id!''}",
		"createDate": "${job.createDate?date}",
		"modifiedDate": "${job.modifiedDate?date}",
		"currentStatus": "${job.currentStatus!''}",
		"processName": "${job.processName!''}",
		"site": "${job.site!''}",
		"properties": [
			<#list job.properties?keys as prop>
				{
					"name": "${prop}",
					"value": "${job.properties[prop]}"
				},
			</#list>
		],
		"items": [
			<#list job.items as item>
				{
					"id": "${item.id!''}",
					"jobId": "${item.jobId!''}",
					"path": "${item.path!''}"
				},
			</#list>
		]
 	},
</#list>
]
