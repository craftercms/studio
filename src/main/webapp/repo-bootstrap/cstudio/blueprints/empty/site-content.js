var siteFolder = space.parent;
var siteName = siteFolder.name;

siteFolder.setPermission("Coordinator", "GROUP_site_" + siteName + "_SiteManager");
siteFolder.setPermission("SiteManager", "GROUP_site_" + siteName  + "_SiteManager");
                                                        
siteFolder.setPermission("Collaborator", "GROUP_site_" + siteName  + "_SiteCollaborator");
siteFolder.setPermission("SiteCollaborator", "GROUP_site_" + siteName  + "_SiteCollaborator");

siteFolder.setPermission("Contributor", "GROUP_site_" + siteName  + "_SiteContributor");
siteFolder.setPermission("SiteContributor", "GROUP_site_" + siteName  + "_SiteContributor");

siteFolder.setPermission("Consumer", "GROUP_site_" + siteName + "_SiteConsumer");
siteFolder.setPermission("SiteConsumer", "GROUP_site_" + siteName  + "_SiteConsumer");