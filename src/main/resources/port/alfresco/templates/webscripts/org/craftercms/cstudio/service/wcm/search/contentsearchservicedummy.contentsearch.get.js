
   var site = args.site;
   var sort = args.sort;
   var asc = args.asc;
   var num = args.num;
   var page = args.page;
   var show = args.show;
   var keyword = args.keyword;
   var product = args.product;
   var version = args.version;
   var contentType = args.contentType;
   var status = args.status;
   var publicOrPrivate = args.publicOrPrivate;
   var isHidePastEvent = args.isHidePastEvent;
   var region = args.region;
   var language = args.language;
   var imageLocation = args.imageLocation;
   var ret = {"name": {"first": "Tanveer", "last": "Hasan"}}

   var allRet = '{"resultCount": 22, '
			 +' "objectList": ['
				+'{"type": "document", "xpath": "avm://", "object": {"title": "This is a Document1", "documentType": "Document Type", "publicOrPrivate": "Public", "description": "When using JSON as the data exchange format, two common tasks are turning a native and in-memory representation into its JSON text representation and vice versa. Unfortunately, at the time of writing, JavaScript does not provide built-in functions to create JSON text from a given object or array.", "createDate": "03/22/2010", "language": "EN", "creator": "Admin", "likeCount": "2"}}, '
				+'{"type": "document", "xpath": "avm://", "object": {"title": "This is a Document2", "documentType": "Document Type", "publicOrPrivate": "Public", "description": "When using JSON as the data exchange format, two common tasks are turning a native and in-memory representation into its JSON text representation and vice versa. Unfortunately, at the time of writing, JavaScript does not provide built-in functions to create JSON text from a given object or array.", "createDate": "03/22/2010", "language": "EN", "creator": "Admin", "likeCount": "2"}}, '
				+'{"type": "news", "xpath": "avm://", "object": {"title": "New machine at Xen", "newsType": "Cover Story", "createDate": "03/01/2010"}}, '
				+'{"type": "download", "xpath": "avm://", "object": {"internalName": "CAG 1.0", "downloadType": "software", "description": "Access Gateway application for Linux", "status": "active", "publicOrPrivate": "Public", "createDate": "01/10/2010", "modifier": "Arvind Ramaswami"}}, '
				+'{"type": "document", "xpath": "avm://", "object": {"title": "This is a Document3", "documentType": "Document Type", "publicOrPrivate": "Public", "description": "When using JSON as the data exchange format, two common tasks are turning a native and in-memory representation into its JSON text representation and vice versa. Unfortunately, at the time of writing, JavaScript does not provide built-in functions to create JSON text from a given object or array.", "createDate": "03/22/2010", "language": "EN", "creator": "Admin", "likeCount": "2"}}, '
				+'{"type": "events", "xpath": "avm://", "object": {"title": "Synergy Berlin is a success", "eventType": "Conference", "eventCity": "Berlin", "fromDate": "05/02/2010", "toDate": "05/08/2010"}}, '
				+'{"type": "story", "xpath": "avm://", "object": {"title": "Success Story", "createDate": "04/02/2010", "region": "Russia", "language": "EN", "likeCount": "5"}}, '
				+'{"type": "story", "xpath": "avm://", "object": {"title": "Success Story1", "createDate": "04/02/2010", "region": "Brazil", "language": "EN", "likeCount": "5"}}, '
				+'{"type": "story", "xpath": "avm://", "object": {"title": "Success Story2", "createDate": "04/02/2010", "region": "USA", "language": "EN", "likeCount": "5"}}, '
				+'{"type": "story", "xpath": "avm://", "object": {"title": "Success Story3", "createDate": "04/02/2010", "region": "UK", "language": "EN", "likeCount": "5"}}, '
				+'{"type": "banner", "xpath": "avm://", "object": {"title": "Home page banner", "imageType": "banner", "description": "Home Page cstudio.com banner", "language": "EN", "publicOrPrivate": "Private", "createDate": "10/23/2009", "imageUrl": "assets/top1.jpg", "expireDate": "12/26/2009", "imageFileLocation": "assets/top2.jpg"}}, '
				+'{"type": "banner", "xpath": "avm://", "object": {"title": "Back page banner", "imageType": "banner", "description": "Home Page cstudio.com banner", "language": "EN", "publicOrPrivate": "Private", "createDate": "10/23/2009", "imageUrl": "assets/top1.jpg", "expireDate": "12/26/2009", "imageFileLocation": "assets/top2.jpg"}}, '
				+'{"type": "banner", "xpath": "avm://", "object": {"title": "Tummy tummy banner", "imageType": "banner", "description": "Home Page cstudio.com banner", "language": "EN", "publicOrPrivate": "Private", "createDate": "10/23/2009", "imageUrl": "assets/top1.jpg", "expireDate": "12/26/2009", "imageFileLocation": "assets/top2.jpg"}}, '
				+'{"type": "banner", "xpath": "avm://", "object": {"title": "Home page banner", "imageType": "banner", "description": "Home Page cstudio.com banner", "language": "EN", "publicOrPrivate": "Private", "createDate": "01/23/2009", "imageUrl": "assets/top1.jpg", "expireDate": "02/13/2010", "imageFileLocation": "assets/top2.jpg"}}, '
				+'{"type": "events", "xpath": "avm://", "object": {"title": "Synergy Sanfrancisco is a success", "eventType": "Conference", "eventCity": "Sanfrancisco", "fromDate": "05/02/2010", "toDate": "05/08/2010"}}, '
				+'{"type": "events", "xpath": "avm://", "object": {"title": "Summit Berlin is upcoming", "eventType": "Conference", "eventCity": "Berlin", "fromDate": "05/02/2010", "toDate": "05/08/2010"}}, '
				+'{"type": "news", "xpath": "avm://", "object": {"title": "Chittah is the machine", "newsType": "Staff News", "createDate": "03/05/2010"}}, '
				+'{"type": "news", "xpath": "avm://", "object": {"title": "Cow has four legs!", "newsType": "Staff News", "createDate": "03/06/2010"}}, '
				+'{"type": "news", "xpath": "avm://", "object": {"title": "New machine at Xen", "newsType": "Cover Story", "createDate": "03/10/2010"}}, '
				+'{"type": "download", "xpath": "avm://", "object": {"internalName": "Branch Rptr 1.0", "downloadType": "software", "description": "Access Gateway application for Linux", "status": "active", "publicOrPrivate": "Private", "createDate": "01/10/2010", "modifier": "Arvind Ramaswami"}}, '
				+'{"type": "download", "xpath": "avm://", "object": {"internalName": "Branch Rptr 2.0", "downloadType": "software", "description": "Access Gateway application for Linux", "status": "active", "publicOrPrivate": "Public", "createDate": "01/10/2010", "modifier": "Anurag Shrivastava"}}, '
				+'{"type": "download", "xpath": "avm://", "object": {"internalName": "CAG 2.0", "downloadType": "software", "description": "Access Gateway application for Linux", "status": "active", "publicOrPrivate": "Public", "createDate": "01/10/2010", "modifier": "Arvind Ramaswami"}}, '
			  +']}';

   var docRet = '{"resultCount": 3, '
			 +' "objectList": ['
				+'{"type": "document", "xpath": "avm://", "object": {"title": "This is a Document1", "documentType": "Document Type", "publicOrPrivate": "Public", "description": "When using JSON as the data exchange format, two common tasks are turning a native and in-memory representation into its JSON text representation and vice versa. Unfortunately, at the time of writing, JavaScript does not provide built-in functions to create JSON text from a given object or array.", "createDate": "03/22/2010", "language": "EN", "creator": "Admin", "likeCount": "2"}}, '
				+'{"type": "document", "xpath": "avm://", "object": {"title": "This is a Document2", "documentType": "Document Type", "publicOrPrivate": "Public", "description": "When using JSON as the data exchange format, two common tasks are turning a native and in-memory representation into its JSON text representation and vice versa. Unfortunately, at the time of writing, JavaScript does not provide built-in functions to create JSON text from a given object or array.", "createDate": "03/22/2010", "language": "EN", "creator": "Admin", "likeCount": "2"}}, '
				+'{"type": "document", "xpath": "avm://", "object": {"title": "This is a Document3", "documentType": "Document Type", "publicOrPrivate": "Public", "description": "When using JSON as the data exchange format, two common tasks are turning a native and in-memory representation into its JSON text representation and vice versa. Unfortunately, at the time of writing, JavaScript does not provide built-in functions to create JSON text from a given object or array.", "createDate": "03/22/2010", "language": "EN", "creator": "Admin", "likeCount": "2"}}, '
			  +']}';
			  
/* 
	if (page == undefined) 
	{
   		page = 1;
	}
	if (num == undefined) {
   		num = 0; // use the default number
	}

	var isAscending = true;
	if (asc  != undefined && asc == 'false') {
		isAscending = false;
	}
*/
	var ct = 1;
	if (contentType == undefined || contentType != 1) 
		model.result = allRet;
	else 
		model.result = docRet;
	
