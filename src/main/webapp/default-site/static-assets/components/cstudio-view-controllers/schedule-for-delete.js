/**
 * File: schedule-for-delete.js
 * Component ID: viewcontroller-schedulefordelete
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
        template = CStudioAuthoring.TemplateHolder.ScheduleForDelete;

    CStudioAuthoring.register("ViewController.ScheduleForDelete", function() {
        CStudioAuthoring.ViewController.ScheduleForDelete.superclass.constructor.apply(this, arguments);
    });

    Delete = CStudioAuthoring.ViewController.ScheduleForDelete;
    YAHOO.extend(Delete, CStudioAuthoring.ViewController.BaseDelete, {

        actions: [".schedule-for-delete", ".scheduling-policy"],
        events: ["hideRequest","showRequest"],

        initialise: function(usrCfg) {

            var _this = this,
                datepickerfield = this.getComponent("input.date-picker"),
                timepickerfield = this.getComponent("input.time-picker"),
                focus = function(){
                    // Check the schedule radio
                    _this.getComponent("input.at-requested-time").checked = true;
                    // Remove the grayed-style text
                    Dom.removeClass(_this.getComponent("input.date-picker"), "water-marked");
                    Dom.removeClass(_this.getComponent("input.time-picker"), "water-marked");
                };

            Event.addListener(datepickerfield, "focus", focus);
            Event.addListener(timepickerfield, "focus", focus);

            CStudioAuthoring.Utils.textFieldTimeHelper('timepicker', 'blur', 'timepicker');
            CStudioAuthoring.Utils.textFieldTimeIncrementHelper('timeIncrementButton', 'click', 'timeIncrementButton');
            CStudioAuthoring.Utils.textFieldTimeDecrementHelper('timeDecrementButton', 'click', 'timeDecrementButton');

            this.createCalendar();

            var oDelDialog = this.getComponent("div.cstudio-dialogue-body");
            if (oDelDialog && oDelDialog.style.height) {
                oDelDialog.style.height = "";
            }

            var toggleTimeSetting = function() {
                var oatReqTime = _this.getComponent("input.at-requested-time");
                if (oatReqTime && oatReqTime.checked) {
                    Dom.removeClass(_this.getComponent("input.date-picker"), "water-marked");
                    Dom.removeClass(_this.getComponent("input.time-picker"), "water-marked");
                } else {
                    Dom.addClass(_this.getComponent("input.date-picker"), "water-marked");
                    Dom.addClass(_this.getComponent("input.time-picker"), "water-marked");
                    _this.getComponent("input.date-picker").style.border = "";
                    _this.getComponent("input.date-picker").style.color = "";
                    _this.getComponent("input.time-picker").style.border = "";
                    _this.getComponent("input.time-picker").style.color = "";
                }
            };

            var oAsap = this.getComponent("input.asap");
            var oatReqTime = this.getComponent("input.at-requested-time");
            if (oAsap) {
                Event.addListener(oAsap, "click", toggleTimeSetting);
            }
            if (oatReqTime) {
                Event.addListener(oatReqTime, "click", toggleTimeSetting);
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
                html.push("No items selected");
            }
            var depCheckWrn = this.getComponent(".items-feedback");
            var oBodyDiv = this.getComponent("div.body");
            if (depCheckWrn && !depFlag) {
                depCheckWrn.style.display = "none";
            } else if (oBodyDiv) {
                //set height 20px lesser to accomadate space for dependency warning string.
                oBodyDiv.style.height = "178px";
            }
            this.getComponent("table.dependencies-table").innerHTML = html.join("");
            items.length && this.initCheckRules();
            this.updateSubmitButton();
        },

        _getScheduledDateData: function() {
            var date = new Date(this.getComponent("input.date-picker").value),
                timeval = this.getComponent("input.time-picker").value,
                isPM = timeval.indexOf("p.m") != -1,
                timesplit = timeval.substr(0,8).split(":");
            if (isPM && parseInt(timesplit[0]) < 12) {
                timesplit[0] = parseInt(timesplit[0]) + 12;
            } else if (!isPM && parseInt(timesplit[0]) == 12) {
                timesplit[0] = "00";
            }
            return [
                date.getFullYear(), "-",
                date.getMonth() + 1, "-",
                date.getDate(), "T",
                timesplit.join(":")
            ].join("");
        },
        _getItemsData: function() {
            var checks = this.getComponents("input[type=checkbox].item-check"),
                checkedItems = [];
            eachfn(checks, function(i, check){
                if (check.checked && !check.disabled) {
                    var parsed = JSON.parse(decodeURIComponent(
                            check.getAttribute("json")));
                    parsed.submittedForDeletion = true;
                    checkedItems.push(parsed);
                }
            });
            return checkedItems;
        },
        getData: function() {
            var asapChecked = this.getComponent("input.asap").checked;
            return JSON.stringify({
                now: asapChecked,
                scheduledDate: asapChecked ? "1900-01-01T00:00:00" : this._getScheduledDateData(),
                sendEmail: this.getComponent("input.email-notify").checked,
                items: this._getItemsData()
            });
        },

        afterSubmit: function(message){
            var agent = new TemplateAgent(template),
                body = agent.get("SUCCESS", {
                    msg: message
                });
            this.getComponent(".cstudio-view.delete-view").innerHTML = body;
            Event.addListener(this.getComponent(".action-complete-close"), "click", function(){
                this.end();
            }, null, this);
            if (this.getComponent(".action-complete-close1")) {
                CStudioAuthoring.Utils.setDefaultFocusOn(this.getComponent(".action-complete-close1"));
            }
        },

        scheduleForDeleteActionClicked: function(btn, evt) {
            if (this.getComponent("input.at-requested-time").checked) {
                var dateValue = this.getComponent("input.date-picker").value;
                var timeValue = this.getComponent("input.time-picker").value;
                if (dateValue == 'Date...' || timeValue == 'Time...' || timeValue == '') {
                    alert('Please provide a date and/or time');
                    return false;
                }
            }

            CStudioAuthoring.Utils.showLoadingImage("schedulefordelete");			
			this.disableActions();
            this.fire("submitStart");
            var data = this.getData(),
                _this = this;
            CStudioAuthoring.Service.request({
                method: "POST",
                data: data,
                resetFormState: true,
                url: CStudioAuthoringContext.baseUri + "/proxy/alfresco/cstudio/wcm/workflow/submit-to-delete?deletedep=true&site="+CStudioAuthoringContext.site,
                callback: {
                    success: function(oResponse) {
						CStudioAuthoring.Utils.hideLoadingImage("schedulefordelete");
                        _this.enableActions();
                        var oResp = JSON.parse(oResponse.responseText);
                        _this.afterSubmit(oResp.message);
                        _this.fire("submitEnd", oResp);
                        _this.fire("submitComplete", oResp);
                    },
                    failure: function(oResponse) {
                        CStudioAuthoring.Utils.hideLoadingImage("schedulefordelete");
						var oResp = JSON.parse(oResponse.responseText);
                        _this.enableActions();
                        _this.fire("submitEnd", oResp);
                    }
                }
            });
        },
        schedulingPolicyActionClicked: function(btn, evt) {
            if (this.schedulingPolicyDialogue) {
                this.schedulingPolicyDialogue.show();
            } else {
                var _this = this;
                CStudioAuthoring.Operations.viewSchedulingPolicy(function(d){
                    // TODO Clean Scheduling Policy View
                    _this.schedulingPolicyDialogue = d;
                    _this.fire("hideRequest");

                    // Manipulate and modify the scheduling policy
                    var dId = d.element.id,
                        container = Selector.query(".schedulePolicyWrapper", dId, true),
                        actionWrp = Selector.query(".schedulePolicySubmitButtons", dId, true),
                        content = Selector.query(".schedulePolicyContent", dId, true),
                        titleEl = Selector.query("h3", container, true),
                        h1Title = document.createElement("h1");

                    h1Title.innerHTML = "Scheduling Policy";
                    h1Title.className = "view-title";

                    container.replaceChild(h1Title, titleEl);
                    Dom.replaceClass(container, "schedulePolicyWrapper", "cstudio-view");

                    Dom.replaceClass(content, "schedulePolicyContent", "terms-policy-wrp view-block");

                    Dom.replaceClass(actionWrp, "schedulePolicySubmitButtons", "action-wrapper");
                    actionWrp.innerHTML = '<button class="dismiss-scheduling-policy" style="width:80px;">OK</button>';
                    Event.addListener(Selector.query(".dismiss-scheduling-policy", actionWrp, true), "click", function(){
                        d.hide();
                    });

                    d.hideEvent.subscribe(function(){
                        _this.fire("showRequest");
                    });
                    d.showEvent.subscribe(function(){
                        _this.fire("hideRequest");
                    });
                    _this.on("end", function(){
                        d.destroy();
                    });
                });
            }
        }
    });

    CStudioAuthoring.Env.ModuleMap.map("viewcontroller-schedulefordelete", Delete);

})();
