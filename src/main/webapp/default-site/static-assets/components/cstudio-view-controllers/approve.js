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

        events: [],
        actions: ['.close-button', '.submit-button', '.select-all-check'],
        startup: ['itemsClickedDelegation'],

        itemsClickedDelegation: itemsClickedDelegation,

        loadItems: loadItems,

        renderItems: renderItems,

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
            publishOption: this.getComponent('.publish-option').value,
            items: []
        };

        var checked = this.getComponents('tbody input[type="checkbox"]:checked');
        each(checked, function (i, check) {
            data.items.push(check.getAttribute('data-item-id'));
        });

        if (data.schedule === 'custom') {
            data.scheduleDate = this.getComponent('[name="scheduleDate"]');
            data.scheduleTime = this.getComponent('[name="scheduleTime"]');
        }

        // TODO submit ajax request to service

    }

    function loadItems() {
        var me = this;
        // TODO async request to get items from server? ...Or from active content?
        setTimeout(function () {
            // Success callback.
            var responseData = {
                submissionComment: 'Blah',
                items: [
                    { internalName: 'Home', uri: '/site/website/index.xml', scheduleDateString: 'Now' },
                    { internalName: 'Home', uri: '/site/website/index.xml', scheduleDateString: '2015-02-02 5:50pm' }
                ]
            };

            var submissionCommentElem = me.getComponent('.submission-comment');
            submissionCommentElem.value = responseData.submissionComment + ' ' + submissionCommentElem.value;

            me.renderItems(responseData.items);
        }, 500);
    }

    function renderItems(items) {

        var html = [];

        each(items, function (index, item) {
            html.push(agent.get('ITEM_ROW', item));
        });

        this.getComponent('tbody').innerHTML = html.join('');

    }

}) (CStudioAuthoring);