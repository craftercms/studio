/**
 * File: schedule-for-delete.js
 * Component ID: viewcontroller-delete
 * @author: Roy Art
 * @date: 05.01.2011
 **/
(function(){

    var Delete,
        Event = YAHOO.util.Event,
        Dom = YAHOO.util.Dom,
        JSON = YAHOO.lang.JSON,
        SUtils = CStudioAuthoring.StringUtils,

        eachfn = CStudioAuthoring.Utils.each,

        TemplateAgent = CStudioAuthoring.TemplateHolder.TemplateAgent,
        template = CStudioAuthoring.TemplateHolder.Delete;

    CStudioAuthoring.register("ViewController.Delete", function() {
        CStudioAuthoring.ViewController.Delete.superclass.constructor.apply(this, arguments);
    });

    Delete = CStudioAuthoring.ViewController.Delete;
    YAHOO.extend(Delete, CStudioAuthoring.ViewController.BaseDelete, {

        actions: [".do-delete", ".overlay-close"],

        initialise: function(usrCfg) {

            this.createSchedulingOverlay();
//            this.initItemIndividualScheduling();

            Dom.setStyle(this.cfg.getProperty("context"), "overflow", "visible");

        },

        // initItemIndividualScheduling: function() {
        //     var itemsbody = this.getComponent("table.item-listing tbody");
        //     Event.addListener(itemsbody, "click", function(evt){
        //         var e = evt.target;
        //         if (Dom.hasClass(e, "when")) {
        //             var overlay = this.overlay,
        //                 schedule = Dom.get(e.getAttribute("checkid")).getAttribute("scheduleddate"),
        //                 whichRadio;
        //             if (schedule != "-") {
        //                 whichRadio = "input[type=radio].scheduled-delete";
        //                 this.setTimeConfiguration(schedule);
        //             } else {
        //                 whichRadio = "input[type=radio].now";
        //             }
        //             this.getComponent(whichRadio).checked = true;
        //             var oatReqTime = this.getComponent("input.scheduled-delete");
        //             if (oatReqTime && oatReqTime.checked) {
        //                 Dom.removeClass(this.getComponent("input.date-picker"), "water-marked");
        //                 Dom.removeClass(this.getComponent("input.time-picker"), "water-marked");
        //             } else {
        //                 Dom.addClass(this.getComponent("input.date-picker"), "water-marked");
        //                 Dom.addClass(this.getComponent("input.time-picker"), "water-marked");
        //                 this.getComponent("input.date-picker").style.border = "";
        //                 this.getComponent("input.date-picker").style.color = "";
        //                 this.getComponent("input.time-picker").style.border = "";
        //                 this.getComponent("input.time-picker").style.color = "";
        //             }
        //             overlay.cfg.setProperty("context", [e, "tr", "br"], null, [0,-10]);
        //             overlay.show();
        //         }
        //     }, null, this);
        // },

        createSchedulingOverlay: function(){
            // var overlayel = this.getComponent(".schedule-overlay"),
            // var overlay;
            // overlayel.style.display = "";
            // overlay = this.overlay = new YAHOO.widget.Overlay(overlayel, {
            //     visible: false,
            //     width: "270px"
            // });
            // overlay.render();
            // overlay.showEvent.subscribe(function(){
            //     overlay.hideMacGeckoScrollbars();
            // });

            // Initialise the timepicker
            // var _this = this,
            //     datepickerfield = this.getComponent("input.date-picker"),
            //     timepickerfield = this.getComponent("input.time-picker"),
            //     focusFn = function(){
            //         // Check the schedule radio
            //         _this.getComponent("input.scheduled-delete").checked = true;
            //         // Remove the grayed-style text
            //         Dom.removeClass(_this.getComponent("input.date-picker"), "water-marked");
            //         Dom.removeClass(_this.getComponent("input.time-picker"), "water-marked");
            //     };

            // Event.addListener(datepickerfield, "focus", focusFn);
            // Event.addListener(timepickerfield, "focus", focusFn);

            // // TODO use more usable timepicker
            // CStudioAuthoring.Utils.textFieldTimeHelper('timepicker', 'blur', 'timepicker');
            // CStudioAuthoring.Utils.textFieldTimeIncrementHelper('timeIncrementButton', 'click', 'timeIncrementButton');
            // CStudioAuthoring.Utils.textFieldTimeDecrementHelper('timeDecrementButton', 'click', 'timeDecrementButton');

            // var toggleTimeSetting = function() {
            //     var oatReqTime = _this.getComponent("input.scheduled-delete");
            //     if (oatReqTime && oatReqTime.checked) {
            //         Dom.removeClass(_this.getComponent("input.date-picker"), "water-marked");
            //         Dom.removeClass(_this.getComponent("input.time-picker"), "water-marked");
            //     } else {
            //         Dom.addClass(_this.getComponent("input.date-picker"), "water-marked");
            //         Dom.addClass(_this.getComponent("input.time-picker"), "water-marked");
            //         _this.getComponent("input.date-picker").style.border = "";
            //         _this.getComponent("input.date-picker").style.color = "";
            //         _this.getComponent("input.time-picker").style.border = "";
            //         _this.getComponent("input.time-picker").style.color = "";
            //     }
            // };

            // var oAsap = this.getComponent("input.now");
            // var oatReqTime = this.getComponent("input.scheduled-delete");
            // if (oAsap) {
            //     Event.addListener(oAsap, "click", toggleTimeSetting);
            // }
            // if (oatReqTime) {
            //     Event.addListener(oatReqTime, "click", toggleTimeSetting);
            // }
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
                    var displayURL = "..." + SUtils.truncate(item.browserUri, 37);
                    //Delete should not assume the item is scheduled.
                    html.push(agent.get(!depth ? "ROOT_ROW" : "SUB_ROW", {
                        url: item.browserUri,
                        displayURL: displayURL,
                        displayName: displayName,
                        internalName: item.internalName,
                        classNames: getClasses(item),
                        data: encodeURIComponent(JSON.stringify(item)),
                        id: item.browserUri,
                        parent: parentUri,
                        scheduledDate: "-",
                        scheduledDateText: "Now",
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
            if (depCheckWrn && !depFlag) {
                depCheckWrn.style.display = "none";
            }
            this.getComponent("table.item-listing tbody").innerHTML = html.join("");
            this.updateSubmitButton();
            if (items.length) {
                this.initCheckRules();
                this.createCalendar();
            }
        },

        getData: function() {
            return JSON.stringify({
                items: this._getItemsData()
            });
        },
        _getItemsData: function() {
            var checks = this.getComponents("input[type=checkbox].item-check"),
                checkedItems = [];
            eachfn(checks, function(i, check){
                if (check.checked && !check.disabled) {
                    var parsed = JSON.parse(decodeURIComponent(
                            check.getAttribute("json")));
                        //scheduledDate = check.getAttribute("scheduleddate");
                    //parsed.scheduledDate = scheduledDate == "-" ? "" : scheduledDate;
                    checkedItems.push(parsed.uri);
                }
            });
            return checkedItems;
        },

        updateItemSchedule: function(element){
            // var nowChecked = this.getComponent("input[type=radio].now").checked,
            //     check = Dom.get(element.getAttribute("checkid")),
            //     scheduledDate = "-";
            // if (nowChecked ) {
            //     element.innerHTML = "Now";
            // } else {
            //     var time = this.getComponent("input.time-picker").value,
            //         dateStr = this.getComponent("input.date-picker").value + " " + time,
            //         date;
            //     var dateValue = this.getComponent("input.date-picker").value;
            //     element.innerHTML = CStudioAuthoring.Utils.getScheduledDateTimeUI(dateValue, time);
            //     scheduledDate = this.getScheduledDateTimeForJson(dateValue, time);
            // }
            // check.setAttribute( "scheduleddate", scheduledDate );
        },

        afterSubmit: function(message){
            var agent = new TemplateAgent(template),
                body = agent.get("SUCCESS", {
                    msg: message
                });
            this.getComponent(".cstudio-view.admin-delete-view").innerHTML = body;
            Event.addListener(this.getComponent(".action-complete-close"), "click", function(){
                this.end();
            }, null, this);
            if (this.getComponent(".action-complete-close1")) {
                CStudioAuthoring.Utils.setDefaultFocusOn(this.getComponent(".action-complete-close1"));
            }
        },

        doDeleteActionClicked: function(){
			this.showProcessingOverlay(true);
            //this.disableActions();
            this.fire("submitStart");
            var data = this.getData(),
                _this = this;
            CStudioAuthoring.Service.request({
                method: "POST",
                data: data,
                resetFormState: true,
                url: CStudioAuthoring.Service.createServiceUri(
                    CStudioAuthoring.Service.deleteContentUrl + 
                        "?deletedep=true&site=" +CStudioAuthoringContext.site+
                        "&user="+CStudioAuthoringContext.user),
                callback: {
                    success: function(oResponse) {
                        _this.showProcessingOverlay(false);
                        _this.enableActions();
                        var oResp = JSON.parse(oResponse.responseText);
                        _this.afterSubmit(oResp.message);
                        _this.fire("submitEnd", oResp);
                        _this.fire("submitComplete", oResp);
                    },
                    failure: function(oResponse) {
                        _this.showProcessingOverlay(false);
						var oResp = JSON.parse(oResponse.responseText);
                        _this.fire("submitEnd", oResp);
                        _this.enableActions();
                    }
                }
            });
        },
        overlayCloseActionClicked: function() {
            //var fields = [this.getComponent("input.time-picker"), this.getComponent("input.date-picker")];
            //var timeValue = fields[0].value;
            //var dateValue = fields[1].value;
            // if (((dateValue == 'Date...') || (timeValue == 'Time...') || (timeValue == '')) && this.getComponent('input.scheduled-delete').checked) {
            //     alert('Please provide a date and/or time');
            //     return false;
            // }
            var o = this.overlay;
            o.hide();
//            this.updateItemSchedule(o.cfg.getProperty("context")[0]);

            // fields[0].value = fields[0].getAttribute("default");
            // fields[1].value = fields[1].getAttribute("default");
        },
        setAllNowActionClicked: function(){
            eachfn(this.getComponents("a.when"), function(i, e){
                e.innerHTML = "Now";
            });
            eachfn(this.getComponents("input.item-check"), function(i, e){
                e.setAttribute("scheduleddate","-");
            });
        },
        getScheduledDateTimeForJson: function (dateValue, timeValue) {
            // var dateValueArray = dateValue.split("/");
            // var timeValueArray = timeValue.split(" ");
            // var timeSplit = timeValueArray[0].split(":");

            // //converting am/pm to 12 hour time.
            // var hr = parseInt(timeSplit[0], 10);
            // if(timeValueArray[1] == "p.m." || timeValueArray[1] == "PM"){
            //     if(hr != 12) hr = hr + 12;
            // }else {
            //     if(hr == 12) hr = hr + 12;
            // }
            // timeSplit[0] = hr;

            // var schedDate = new Date(dateValueArray[2], dateValueArray[0] - 1, dateValueArray[1], timeSplit[0], timeSplit[1], timeSplit[2], "");

            // var schedDateMonth = schedDate.getMonth() + 1;
            // var scheduledDate = schedDate.getFullYear() + '-' + schedDateMonth + '-'
            //                     + schedDate.getDate() + 'T' + schedDate.getHours() + ':'
            //                     + schedDate.getMinutes() + ':' + schedDate.getSeconds();

            // return scheduledDate;
        },
        setTimeConfiguration: function (scheduledDate) {
            if (scheduledDate) {
                var datePick = this.getComponent("input.date-picker");
                var timePick = this.getComponent("input.time-picker");
                datePick.value = "Date...";
                timePick.value = "Time...";
                if (scheduledDate == "now") {

                } else {
                    var shedSplit = scheduledDate.split("T");
                    if(shedSplit.length == 2) {
                        var dateArray = shedSplit[0].split("-");
                        if (dateArray.length == 3) {
                            datePick.value = (isNaN(dateArray[1])?dateArray[1]:parseInt(dateArray[1], 10)) + "/" +
                                             (isNaN(dateArray[2])?dateArray[2]:parseInt(dateArray[2], 10)) + "/" + dateArray[0];
                        }
                        var timeArray = shedSplit[1].split(":");
                        if (timeArray.length == 3) {
                            var hours = (isNaN(timeArray[0])?timeArray[0]:parseInt(timeArray[0], 10));
                            var mins  = (isNaN(timeArray[1])?timeArray[1]:parseInt(timeArray[1], 10));
                            var secs  = (isNaN(timeArray[2])?timeArray[2]:parseInt(timeArray[2], 10));
                            var hr = hours;
                            if (hours == 0) {
                                hours = 12;
                            } else if (hours > 12) {
                                hours = hours - 12;
                            }
                            timeString = (hours<10?("0"+hours):hours) + ":" +
                                         (mins<10?("0"+mins):mins) + ":" +
                                         (secs<10?("0"+secs):secs);
                            if (hr >= 12) {
                                timePick.value = timeString + " p.m.";
                            } else {
                                timePick.value = timeString + " a.m.";
                            }
                        }
                    }
                }
            }
        },

        showProcessingOverlay: function (show) {
            var elem = this.getComponent(".processing-overlay");
            if ( elem ) {
                elem.style.display = show ? '' : 'none';
            }
        }

    });

    CStudioAuthoring.Env.ModuleMap.map("viewcontroller-delete", Delete);

})();
