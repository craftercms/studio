var siteFolder = space;
var siteName = siteFolder.name;

siteFolder.setPermission("Coordinator", "GROUP_site_" + siteName + "_SiteManager");
siteFolder.setPermission("SiteManager", "GROUP_site_" + siteName  + "_SiteManager");