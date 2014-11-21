controller = {
        event: {
            onCopy:function (site, path) {
                logger.log("running copy operation event on " + site + ":" + path);
            },
            onDelete:function (path, path) {
                logger.log("running delete operation event on " + site + ":" + path);
            },
            onDuplicate:function (path, path) {
                logger.log("running duplicate operation event on " + site + ":" + path);
            },
            onNew:function (path, path) {
                logger.log("running new operation event on " + site + ":" + path);
            },
            onRename:function (path, path) {
                logger.log("running rename operation event on " + site + ":" + path);
            },
            onRevert:function(path, path) {
                logger.log("running revert operation event on " + site + ":" + path);
            },
            onUpdate:function (path, path) {
                logger.log("running update operation event on " + site + ":" + path);
            }

        },
        execute:function () {
            if (contentLifecycleOperation == "COPY") {
                controller.event.onCopy(site, path);
            } else if (contentLifecycleOperation == "DELETE") {
                controller.event.onDelete(site, path);
            } else if (contentLifecycleOperation == "DUPLICATE") {
                controller.event.onDuplicate(site, path);
            } else if (contentLifecycleOperation == "NEW") {
                controller.event.onNew(site, path);
            } else if (contentLifecycleOperation == "RENAME") {
                controller.event.onRename(site, path);
            } else if (contentLifecycleOperation == "REVERT") {
                controller.event.onRevert(site, path);
            } else if (contentLifecycleOperation == "UPDATE") {
                controller.event.onUpdate(site, path);
            } else {
            	logger.log("Unkown operation: " + contentLifecycleOperation + " for " + site + ":" + path);
            }
        }
  };



