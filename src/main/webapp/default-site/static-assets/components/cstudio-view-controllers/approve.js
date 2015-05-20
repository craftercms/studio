/**
 * File: approve.js
 * Component ID: viewcontroller-approve
 * @author: Roy Art
 * @date: 12.09.2014
 **/
(function(CStudioAuthoring){

    var Base = CStudioAuthoring.ViewController.Base,
        Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event,
        agent = new CStudioAuthoring.TemplateHolder.TemplateAgent(CStudioAuthoring.TemplateHolder.Approve),
        each = CStudioAuthoring.Utils.each;

    Base.extend('Approve', {

        events: ['submitStart','submitComplete','submitEnd'],
        actions: ['.close-button', '.submit-button', '.select-all-check'],
        startup: ['itemsClickedDelegation'],

        itemsClickedDelegation: itemsClickedDelegation,

        loadItems: loadItems,

        loadPublishingChannels: loadPublishingChannels,

        renderItems: renderItems,

        populatePublishingOptions: populatePublishingOptions,

        submitButtonActionClicked: submit,

        selectAllCheckActionClicked: selectAllItems,

        closeButtonActionClicked: closeButtonClicked

    });

    function closeButtonClicked() {
        this.end();
    }

    function itemsClickedDelegation() {
        var me = this;
        Event.delegate(this.cfg.getProperty('context'), "click", function(e, elem) {

            var allCheck = me.getComponent('.select-all-check');

            if (!elem.checked) {
                allCheck.checked = false;
            } else {

                var allItemsChecked = true;
                var itemChecks = me.getComponents('input[data-item-id]');

                each(itemChecks, function (i, check) {
                    if (!check.checked) {
                        allItemsChecked = false;
                        return false;
                    }
                });

                allCheck.checked = allItemsChecked;

            }

        }, 'input.item-checkbox');
    }

    function selectAllItems(checkbox) {

        var items = this.getComponents('input[data-item-id][type="checkbox"]');
        var bool = checkbox.checked;

        each(items, function (i, check) {
            check.checked = bool;
        });
    }

    function submit() {

        var data = {
            schedule: this.getComponent('[name="schedulingMode"]:checked').value,
            submissionComment: this.getComponent('.submission-comment').value,
            publishOptionComment: this.getComponent('.publish-option-comment').value,
            publishChannel: this.getComponent('.publish-option').value,
            items: []
        };

        var checked = this.getComponents('tbody input[type="checkbox"]:checked');
        each(checked, function (i, check) {
            data.items.push(check.getAttribute('data-item-id'));
        });

        if (data.schedule === 'custom') {
            data.scheduleDate = this.getComponent('[name="scheduleDate"]').value;
            data.scheduleTime = this.getComponent('[name="scheduleTime"]').value;
            data.scheduledDate = getScheduledDateTimeForJson(data.scheduleDate, data.scheduleTime);
        }



        //this.showProcessingOverlay(true);
        this.disableActions();
        this.fire("submitStart");
        //var data = this.getData(),
        var _this = this;
        CStudioAuthoring.Service.request({
            method: "POST",
            data: JSON.stringify(data),
            resetFormState: true,
            url: CStudioAuthoringContext.baseUri + "/api/1/services/api/1/workflow/go-live.json?site="+CStudioAuthoringContext.site+"&user="+CStudioAuthoringContext.user,
            callback: {
                success: function(oResponse) {
                    _this.enableActions();
                    var oResp = JSON.parse(oResponse.responseText);
                    _this.fire("submitComplete", oResp);
                    _this.fire("submitEnd", oResp);
                },
                failure: function(oResponse) {
                    var oResp = JSON.parse(oResponse.responseText);
                    _this.fire("submitEnd", oResp);
                    _this.enableActions();
                }
            }
        });

    }

    function loadItems(data) {
        var me = this;
        CStudioAuthoring.Service.request({
            method: "POST",
            data: CStudioAuthoring.Utils.createContentItemsJson(data),
            resetFormState: true,
            url: CStudioAuthoringContext.baseUri + '/api/1/services/api/1/dependency/get-dependencies.json?site='+ CStudioAuthoringContext.site,
            callback: {
                success: function(oResponse) {
                    var respJson = oResponse.responseText;
                    try {
                        var dependencies = eval("(" + respJson + ")");
                        var submissionCommentElem = me.getComponent('.submission-comment');
                        submissionCommentElem.value = dependencies.submissionComment + ' ' + submissionCommentElem.value;
                        me.renderItems(dependencies.items);

                    } catch(err) {
                        var error = err;
                    }/*
                     var responseData = {
                     submissionComment: 'Blah',
                     items: [
                     { internalName: 'Home', uri: '/site/website/index.xml', scheduleDateString: 'Now' },
                     { internalName: 'Home', uri: '/site/website/index.xml', scheduleDateString: '2015-02-02 5:50pm' }
                     ]
                     };

                     var submissionCommentElem = me.getComponent('.submission-comment');
                     submissionCommentElem.value = responseData.submissionComment + ' ' + submissionCommentElem.value;

                     me.renderItems(responseData.items);*/
                },
                failure: function(oResponse) {

                }
            }
        });
    }

    function loadPublishingChannels() {
        var me = this;
        CStudioAuthoring.Service.request({
            method: "GET",
            resetFormState: true,
            url: CStudioAuthoringContext.baseUri + '/api/1/services/api/1/deployment/get-available-publishing-channels.json?site='+ CStudioAuthoringContext.site,
            callback: {
                success: function(oResponse) {
                    var respJson = oResponse.responseText;
                    var allChannels = eval("(" + respJson + ")");
                    var channels = allChannels.availablePublishChannels;
                    /*
                     var respJson = oResponse.responseText;
                     try {
                     var dependencies = eval("(" + respJson + ")");
                     var submissionCommentElem = me.getComponent('.submission-comment');
                     submissionCommentElem.value = dependencies.submissionComment + ' ' + submissionCommentElem.value;
                     me.renderItems(dependencies.items);

                     } catch(err) {
                     var error = err;
                     }/*
                     var responseData = {
                     submissionComment: 'Blah',
                     items: [
                     { internalName: 'Home', uri: '/site/website/index.xml', scheduleDateString: 'Now' },
                     { internalName: 'Home', uri: '/site/website/index.xml', scheduleDateString: '2015-02-02 5:50pm' }
                     ]
                     };

                     var submissionCommentElem = me.getComponent('.submission-comment');
                     submissionCommentElem.value = responseData.submissionComment + ' ' + submissionCommentElem.value;

                     me.renderItems(responseData.items);*/
                    populatePublishingOptions.call(me, channels);
                },
                failure: function(oResponse) {

                }
            }
        });
    }

    function renderItems(items) {

        var html = [];

        each(items, function (index, item) {
            html.push(agent.get('ITEM_ROW', item));
        });

        this.getComponent('tbody').innerHTML = html.join('');

    }

    function populatePublishingOptions(items) {
        var select = this.getComponent('.publish-option');
        for (var i = 0, option; i < items.length; ++i) {
            option = new Option(items[i].name, items[i].name);
            select.options[i] = option;
        }
    }

    function getScheduledDateTimeForJson(dateValue, timeValue) {
        var schedDate = new Date(dateValue + " " + timeValue);
        var schedDateMonth = schedDate.getMonth() + 1;
        var scheduledDate = schedDate.getFullYear() + '-' + schedDateMonth + '-'
            + schedDate.getDate() + 'T' + schedDate.getHours() + ':'
            + schedDate.getMinutes() + ':' + schedDate.getSeconds();

        return scheduledDate;
    }

}) (CStudioAuthoring);