import scripts.libs.salesforce.SalesForce;

def salesforce = new SalesForce();

return salesforce.getCampaignInfluencedOpportunities(applicationContext);