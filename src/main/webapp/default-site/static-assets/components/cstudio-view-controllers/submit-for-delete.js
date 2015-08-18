/**
 * File: submit-for-delete.js
 * Component ID: viewcontroller-submitfordelete
 * @author: Roy Art
 * @date: 05.01.2011
 **/
(function(){

    var Delete,
        Event = YAHOO.util.Event,
        Dom = YAHOO.util.Dom,
        JSON = YAHOO.lang.JSON,
        Selector = YAHOO.util.Selector,
        SUtils = CStudioAuthoring.StringUtils,

        eachfn = CStudioAuthoring.Utils.each,

        TemplateAgent = CStudioAuthoring.TemplateHolder.TemplateAgent,
        template = CStudioAuthoring.TemplateHolder.SubmitForDelete;

    CStudioAuthoring.register("ViewController.SubmitForDelete", function() {
        CStudioAuthoring.ViewController.SubmitForDelete.superclass.constructor.apply(this, arguments);
    });

    Delete = CStudioAuthoring.ViewController.SubmitForDelete;
    YAHOO.extend(Delete, CStudioAuthoring.ViewController.BaseDelete, {

        actions: [".submit-for-delete"],//, ".scheduling-policy"],
        events: ["hideRequest","showRequest"],

        initialise: function(usrCfg) {

            var _this = this;
            var oDelDialog = this.getComponent("div.cstudio-dialogue-body");
            if (oDelDialog && oDelDialog.style.height) {
                oDelDialog.style.height = "";
            }

        },

        renderItems: function(items) {
            var html = [];
            var depFlag = false;
            if (items.length) {
                var getClasses = CStudioAuthoring.Utils.getIconFWClasses,
                    agent = new TemplateAgent(template),
                    depth = 0,
                    iterator, parentUri = "";
                iterator = function(i, item){
                    var displayName = SUtils.truncate(item.internalName, 20);
                    if (item.newFile) {
                        displayName += "*";
                    }
                    var displayURL = "..." + SUtils.truncate(item.browserUri, 47);
                    html.push(agent.get(!depth ? "ROOT_ROW" : "SUB_ROW", {
                        url: item.browserUri,
                        displayURL: displayURL,
                        displayName: displayName,
                        internalName: item.internalName,
                        classNames: getClasses(item),
                        data: encodeURIComponent(JSON.stringify(item)),
                        id: item.browserUri,
                        parent: parentUri,
                        uri: item.uri,
                        checked: "checked"
                    }));
                    if (item.children && item.children.length) {
                        depFlag = true;
                        depth++;
                        parentUri = item.browserUri;
                        eachfn(item.children, iterator);
                        parentUri = "";
                        depth--;
                    }
                }
                eachfn(items, iterator);
            } else {
                html.push(CMgs.format(formsLangBundle, "deleteDialogNoItemsSelected"));
            }
            var depCheckWrn = this.getComponent(".items-feedback");
            var oBodyDiv = this.getComponent("div.body");
            if (depCheckWrn && !depFlag) {
                depCheckWrn.style.display = "none";
            } else if (oBodyDiv) {
                //set height 20px lesser to accomadate space for dependency warning string.
                oBodyDiv.style.height = "178px";
            }
            this.getComponent("table.item-listing tbody").innerHTML = html.join("");
            items.length && this.initCheckRules();
            this.updateSubmitButton();
        },

        _getItemsData: function() {
            var checks = this.getComponents("input[type=checkbox].item-check"),
                checkedItems = [];
            eachfn(checks, function(i, check){
                if (check.checked && !check.disabled) {
                    checkedItems.push(check.getAttribute("uri"));
                }
            });
            return checkedItems;
        },
        getData: function() {
            var asapChecked = this.getComponent("input.asap").checked;
            return JSON.stringify({
                now: asapChecked,
                sendEmail: this.getComponent("input.email-notify").checked,
                items: this._getItemsData()
            });
        },

        afterSubmit: function(message){
            var agent = new TemplateAgent(template),
                body = agent.get("SUCCESS", {
                    msg: message
                });
            this.getComponent(".studio-view.delete-view").innerHTML = body;
            Event.addListener(this.getComponent(".action-complete-close"), "click", function(){
                this.end();
            }, null, this);
            if (this.getComponent(".action-complete-close1")) {
                CStudioAuthoring.Utils.setDefaultFocusOn(this.getComponent(".action-complete-close1"));
            }
        },

        submitForDeleteActionClicked: function(btn, evt) {

            CStudioAuthoring.Utils.showLoadingImage("submitfordelete");
			this.disableActions();
            this.fire("submitStart");
            var data = this.getData(),
                _this = this;

            CStudioAuthoring.Service.request({
                method: "POST",
                data: data,
                resetFormState: true,
                url: CStudioAuthoringContext.baseUri + "/api/1/services/api/1/workflow/submit-to-delete.json?deletedep=true&site="+CStudioAuthoringContext.site,
                callback: {
                    success: function(oResponse) {
						CStudioAuthoring.Utils.hideLoadingImage("submitfordelete");
                        _this.enableActions();
                        var oResp = JSON.parse(oResponse.responseText);
                        _this.afterSubmit(oResp.message);
                        _this.fire("submitEnd", oResp);
                        _this.fire("submitComplete", oResp);
                    },
                    failure: function(oResponse) {
                        CStudioAuthoring.Utils.hideLoadingImage("submitfordelete");
						var oResp = JSON.parse(oResponse.responseText);
                        _this.enableActions();
                        _this.fire("submitEnd", oResp);
                    }
                }
            });
        }

    });

    CStudioAuthoring.Env.ModuleMap.map("viewcontroller-submitfordelete", Delete);

})();
