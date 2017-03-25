import scripts.api.ClipboardServices
import groovy.json.JsonSlurper

def result = [:]
def site = params.site

result.success = false

def requestBody = request.reader.text
def context = ClipboardServices.createContext(applicationContext, request)
def slurper = new JsonSlurper()
def tree = slurper.parseText(requestBody)

// convert inbound json structure to objects
rootItem = buildItemTree(tree)

// execute copy on the clipboard
ClipboardServices.copy(site, rootItem, context)

result.success = true

return result

/**
 * build the item tree from the JSON provided by the client
 * @param tree json structure
 * @return itemTree
 */
private Object buildItemTree(tree) {
	// parse the inbound request and compose an array of paths to put on the clipboard
	def rootItem = ClipboardServices.newClipboardItem(tree.item[0].uri, false)
	addChildItems(rootItem, tree.item[0])

	return rootItem
}

/**
 * Recursively add children to the tree
 * @param item current root item
 * @param treeNode current tree node
 */
private void addChildItems(item, treeNode) {

	treeNode.children.each { childItem ->
		def clipboardItem = ClipboardServices.newClipboardItem(childItem.uri, false)
		item.children.add(clipboardItem)

		addChildItems(clipboardItem, childItem)
	}
}