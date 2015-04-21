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

    if (jQuery && jQuery.isPlainObject)

    function closeButtonClicked() {
        this.end();
    }

    function submit() {

    }

    function renderItems(items) {
        var html = [],
            tpl = [
                '<tr>',
                '<td><input type="checkbox" class="select-all-check"/></td>',
                '<td>_NAME_</td>',
                '<td>_SCHEDULE_</td>',
                '</tr>'
            ].join();
        $.each(items, function (i, item) {
            html.push(tpl
                .replace('_NAME_', item.internalName)
                .replace('_SCHEDULE_', item.browserUri));
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
            } else {
                me.$('.date-picker-control').show();
                me.$('.date-picker').select();
            }
        });

        me.$('.date-picker').datetimepicker();

    }

}) (CStudioAuthoring);