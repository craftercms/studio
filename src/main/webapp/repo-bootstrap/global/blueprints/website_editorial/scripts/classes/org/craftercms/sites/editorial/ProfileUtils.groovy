package org.craftercms.sites.editorial

class ProfileUtils {

  private ProfileUtils() {
  }

  static def getSegment(profile) {
    if (profile) {
        def segment = profile.attributes.segment
        if (segment != "unknown") {
          return segment
        }
    }

    return null
  }

}
