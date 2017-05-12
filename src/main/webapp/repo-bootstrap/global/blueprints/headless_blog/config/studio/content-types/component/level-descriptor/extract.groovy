import scripts.libs.ExtractMetadataApi

def extractMetadataParams =[:]
extractMetadataParams.site = site
extractMetadataParams.path = path
extractMetadataParams.user = user
extractMetadataParams.contentType = contentType
extractMetadataParams.contentXml = contentXml
extractMetadataParams.applicationContext = applicationContext

def extractor = new ExtractMetadataApi(extractMetadataParams)
extractor.execute()
