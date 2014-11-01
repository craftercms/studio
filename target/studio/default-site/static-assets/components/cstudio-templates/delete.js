/**
 * File: delete.js
 * Component ID: templateholder-delete
 * @author: Roy Art
 * @date: 10.01.2011
 **/
(function(){

    CStudioAuthoring.register("TemplateHolder.Delete", {
        ROOT_ROW: [
            '<tr>',
                '<td>',
                    '<div class="item">',
                        '<input class="item-check" id="{id}" type="checkbox" json="{data}" {checked} scheduleddate="{scheduledDate}" /> ',
                        '<label for="{id}" class="{class}" title="{internalName}">{displayName}</label>',
                    '</div>',
                '</td>',
                '<td>',
                    '<div class="item-desc" title="{url}">',
                        '{displayURL}',
                    '</div>',
                '</td>',
                '<td>',
                    '<div class="item-sch">',
                        '<a class="when" href="javascript:" checkid="{id}">{scheduledDateText}</a>',
                    '</div>',
                '</td>',
            '</tr>'
        ].join(""),
        SUB_ROW: [
            '<tr>',
                '<td>',
                    '<div class="item sub-item">',
                        '<input class="item-check" id="{id}" type="checkbox" json="{data}" parentid="{parent}" scheduleddate="{scheduledDate}" {checked} /> ',
                        '<label for="{id}" class="{class}" title="{internalName}">{displayName}</label>',
                    '</div>',
                '</td>',
                '<td>',
                    '<div class="item-desc" title="{url}">',
                        '{displayURL}',
                    '</div>',
                '</td>',
                '<td>',
                    '<div class="item-sch">',
                        '<a class="when" href="javascript:" checkid="{id}">{scheduledDateText}</a>',
                    '</div>',
                '</td>',
            '</tr>'
        ].join(""),
        OVERLAY: [
            '<div class="schedule-overlay" style="padding:10px">',
                '<div class="bd">',
                    '<div>',
                        '<input type="radio" name="when-to-delete" class="now" /> Now',
                        '<a href="javascript:" class="overlay-close" style="float:right">Done</a>',
                    '</div>',
                    '<div>',
                        '<input type="radio" name="when-to-delete" class="scheduled" /> ',
                        '<input class="date-picker water-marked" value="Date..." default="Date..." />',
                        '<input class="time-picker water-marked" value="Time..." default="Time..." />',
                    '</div>',
                '</div>',
            '</div>'
        ].join(""),
        SUCCESS: [
            '<h1 class="view-title">Submittal Complete</h1>',
            '<div class="msg-area" style="height:100px;margin-top:15px;color:#000;">{msg}</div>',
            '<div class="action-wrapper acnSubmitButtons">',
                '<input type="button" value="OK" style="width:80px;" class="action-complete-close1" onClick="CStudioAuthoring.Operations.pageReload(\'deleteSchedule\');" />',
            '</div>'
        ].join("")
    });

    CStudioAuthoring.Env.ModuleMap.map("template-delete", CStudioAuthoring.TemplateHolder.Delete);

})();
