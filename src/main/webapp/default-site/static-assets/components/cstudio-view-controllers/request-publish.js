/**
 * File: request-publish.js
 * Component ID: viewcontroller-requestpublish
 * @author: Roy Art
 * @date: 2015.04.15
 **/
(function(CStudioAuthoring){

    var Base = CStudioAuthoring.ViewController.Base,
        $ = jQuery;

    Base.extend('RequestPublish', {

        actions: ['.close-button', '.submit-button'],

        startup: ['initDatePicker'],

        renderItems: renderItems,

        submitButtonActionClicked: submit,

        closeButtonActionClicked: closeButtonClicked,

        initDatePicker: initDatePicker

    });


    function closeButtonClicked() {
        this.end();
    }

    function submit() {
        var data = {
            sendEmail: this.getComponent('[name="notifyApproval"]').checked,
            schedule: this.getComponent('[name="schedulingMode"]:checked').value,
            submissionComment: this.getComponent('.submission-comment').value,
            items: []
        };

        var checked = this.getComponents('tbody input[type="checkbox"]:checked');
        $.each(checked, function (i, check) {
            data.items.push(check.getAttribute('data-item-id'));
        });

        if (data.schedule === 'custom') {
            data.scheduledDate =  getScheduledDateTimeForJson(this.getComponent('[name="scheduleDate"]').value);
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
            url: CStudioAuthoringContext.baseUri + "/api/1/services/api/1/workflow/submit-to-go-live.json?site="+CStudioAuthoringContext.site+"&user="+CStudioAuthoringContext.user,
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

        window.location.reload();
    }

    function renderItems(items) {
        var html = [],
            tpl = [
                '<tr>',
                '<td class="small"><input type="checkbox" class="select-all-check" data-item-id="_URI_" checked/></td>',
                '<td class="large">_INTERNALNAME_ _URI_</td>',
                '<td class="medium">_SCHEDULE_</td>',
                '</tr>'
            ].join();
        $.each(items, function (i, item) {
            html.push(tpl
                .replace('_URI_', item.uri)
                .replace('_INTERNALNAME_', item.internalName)
                .replace('_SCHEDULE_', item.scheduledDate ? item.scheduledDate : "")
                .replace('_URI_', item.uri));
        });
        this.$('.item-listing tbody').html(html.join(''));
    }

    function initDatePicker() {

        var me = this;

        me.$('[name="schedulingMode"]').change(function () {
            var $elem = $(this);
            if ($elem.val() === 'now') {
                me.$('.date-picker-control').hide();
                me.$('.date-picker').val('');
                me.$('#approveSubmit').prop('disabled', false);
                me.$('#approveSubmitVal').hide;
            } else {
                me.$('.date-picker-control').show();
                me.$('.date-picker').select();
                me.$('#approveSubmit').prop('disabled', true);
                me.$('#approveSubmitVal').show();
            }
        });

        me.$('.date-picker').datetimepicker({
            format: 'm/d/Y h:i a',
            dateFormat: "m/d/Y",
            formatTime:	'h:i a',
            minDate: '0',
            minTime: 0,
            step: 15
        });

        me.$('.date-picker').change(function () {
            var $elem = $(this);
            if ($elem.val() !=null && $elem.val() != "") {
                me.$('#approveSubmit').prop('disabled', false);
                me.$('#approveSubmitVal').hide();
            }else{
                me.$('#approveSubmit').prop('disabled', true);
                me.$('#approveSubmitVal').show();
            }
        });

    }

    function getScheduledDateTimeForJson(dateTimeValue) {
        var schedDate = new Date(dateTimeValue);
        var schedDateMonth = schedDate.getMonth() + 1;
        var scheduledDate = schedDate.getFullYear() + '-' + schedDateMonth + '-'
            + schedDate.getDate() + 'T' + schedDate.getHours() + ':'
            + schedDate.getMinutes() + ':' + schedDate.getSeconds();

        return scheduledDate;
    }

}) (CStudioAuthoring);