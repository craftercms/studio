def categoriesItem = siteItemService.getSiteItem(contentModel.categories.item.key.text)
templateModel.categories = categoriesItem.items.item
