/**
 * Clipboard sevices are scoped to the specific user / session
 * (The only stateful services)
 */
Class ClipboardServices{

	/** 
	 * copy a set of items to the clipboard
	 * @param site - the project ID
	 * @param items - items to copy
	 */
	def copy(site, items) {

	}

	/** 
	 * cut a set of items to the clipboard
	 * @param site - the project ID
	 * @param items - items to copy
	 */
	def cut(site, items) {

	}

	/** 
	 * paste a set of items from the clipboard
	 * @param site - the project ID
	 * @param items - items to copy
	 * @param newLocation - where the items should be pasted
	 */
	def paste(site, items, newLocation) {

	}

	/** 
	 * get the clippings for a given user and a given site
	 * @param site - the project ID
	 */
	def getClippings(site) {

	}
}