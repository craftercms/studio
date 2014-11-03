/**
 * File: schedule-for-delete.js
 * Component ID: templateholder-schedule-for-delete
 * @author: Roy Art
 * @date: 10.01.2011
 **/
(function(){

    CStudioAuthoring.register("TemplateHolder.ScheduleForDelete", {
        ROOT_ROW: [
            '<tr>',
                '<td>',
                    '<div class="item">',
                        '<input class="item-check" id="{id}" type="checkbox" json="{data}" {checked} /> ',
                        '<label for="{id}" class="{class}" title="{internalName}">{displayName}</label>',
                    '</div>',
                '</td>',
                '<td>',
                    '<div class="item-desc" title="{url}">',
                        '{displayURL}',
                    '</div>',
                '</td>',
            '</tör>'
        ].join(""),
        SUB_ROW: [
            '<tr>',
                '<td>',
                    '<div class="item sub-item">',
                        '<input class="item-check" id="{id}" type="checkbox" json="{data}" parentid="{parent}" {checked} /> ',
                        '<label for="{id}" class="{class}" title="{internalName}">{displayName}</label>',
                    '</div>',
                '</td>',
                '<td>',
                    '<div class="item-desc" title="{url}">',
                        '{displayURL}',
                    '</div>',
                '</td>',
            '</tr>'
        ].join(""),
        SUCCESS: [
            '<h1 class="view-title">Submittal Complete</h1>',
            '<div class="msg-area" style="height:348px;margin-top:15px;color:#000;">{msg}</div>',
            '<div class="action-wrapper">',
                '<button style="width:80px;" class="action-complete-close1" onClick="CStudioAuthoring.Operations.pageReload(\'deleteSchedule\');">OK</button>',
            '</div>'
        ].join("")
    });

    CStudioAuthoring.Env.ModuleMap.map("template-schedulefordelete", CStudioAuthoring.TemplateHolder.ScheduleForDelete);

})();
