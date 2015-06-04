/**
 * File: submit-for-delete.js
 * Component ID: templateholder-submit-for-delete
 * @author: Roy Art
 * @date: 10.01.2011
 **/
(function(){

    CStudioAuthoring.register("TemplateHolder.SubmitForDelete", {
        ROOT_ROW: [
            '<tr>',
                '<td>',
                    '<div class="item">',
                        '<input class="item-check" id="{id}" type="checkbox" json="{data}" uri="{uri}" {checked} /> ',
                        '<label for="{id}" class="{class}" title="{internalName}">{displayName}</label>',
                    '</div>',
                '</td>',
                '<td>',
                    '<div class="item-desc" title="{url}">',
                        '{displayURL}',
                    '</div>',
                '</td>',
            '</t�r>'
        ].join(""),
        SUB_ROW: [
            '<tr>',
                '<td>',
                    '<div class="item sub-item">',
                        '<input class="item-check" id="{id}" type="checkbox" json="{data}" parentid="{parent}" uri="{uri}" {checked} /> ',
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
                '<button style="width:80px;" class="action-complete-close1" onClick="CStudioAuthoring.Operations.pageReload(\'deleteSubmit\');">OK</button>',
            '</div>'
        ].join("")
    });

    CStudioAuthoring.Env.ModuleMap.map("template-submitfordelete", CStudioAuthoring.TemplateHolder.SubmitForDelete);

})();
