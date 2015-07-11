/**
 * File: cancel-workflow.js
 * Component ID: template-cancel-workflow
 * @author: Roy Art
 * @date: 21.03.2013
 */

(function(CSA){

    CSA.register("TemplateHolder.CancelWorkflow", {
        ROOT: [
            '<div class="cstudio-view cancel-workflow-view">',

                '<h3 class="view-title">Warning: Workflow Cancellation</h3>',

                '<div class="view-caption">',
                    '<span>',
                        'Edit will cancel all items that are in the scheduled deployment batch. ',
                        'Please review the list of files below and chose "Continue" to cancel ',
                        'workflow and edit or "Cancel" to remain in your dashboard.',
                    '</span>',
                '</div>',

                '<div class="view-block view-square-wrp">',
                    '<div class="head">',
                        '<table class="dependencies-table-head">',
                            '<tr>',
                                '<td><div class="column col-1">Page</div></td>',
                                '<td><div class="column col-2">Path</div></td>',
                            '</tr>',
                        '</table>',
                    '</div>',
                    '<div class="body">',
                        '<div class="dependencies-listing">',
                            '<table class="dependencies-table">',

                            '</table>',
                        '</div>',
                    '</div>',
                '</div>',
                '<div class="acn-submit-buttons action-wrapper">',
                    '<button class="continue btn btn-primary">Continue</button>',
                    '<button class="cancel btn btn-default">Cancel</button>',
                '</div>',
            '</div>'
        ].join(''),
        FILE_ROW: [
            '<tr>',
                '<td><div class="column col-1">{name}</div></td>',
            '<td><div class="column col-2" title="{browserUri}">{browserUri}</div></td>',
            '</tr>'
        ].join(''),
        LOADING_ROW: [
            '<tr class="loading-row">',
                '<td colspan="2">',
                    '<div>Loading affected paths listing, please wait&hellip;</div>',
                '</td>',
            '</tr>'
        ].join(''),
        ERROR_ROW: [
            '<tr class="loading-row">',
                '<td colspan="2">',
                    '<div>An error occurred attempting to get the affected files (<a href="javascript:" class="load-retry">retry</a>)</div>',
                '</td>',
            '</tr>'
        ].join('')
    });

    CSA.Env.ModuleMap.map(
        "template-cancel-workflow", CSA.TemplateHolder.CancelWorkflow);

}) (CStudioAuthoring);
