var site = args.site;
var type = args.type;

if(type && type != '')
	rolesService.refreshRoles(type);
else 
	rolesService.refreshRoles();
