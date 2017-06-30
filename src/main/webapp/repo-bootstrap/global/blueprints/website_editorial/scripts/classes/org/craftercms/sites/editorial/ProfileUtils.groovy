package org.craftercms.sites.editorial

class ProfileUtils {

  private ProfileUtils() {
  }

  static def getSegment(profile, siteItemService) {
    if (profile) {
        def taxonomyHelper = new TaxonomyHelper(siteItemService)
        def validSegments = taxonomyHelper.getValues("segments")
        def segment = profile.attributes.segment
        if (segment in validSegments) {
          return segment
        }
    }

    return null
  }

}
