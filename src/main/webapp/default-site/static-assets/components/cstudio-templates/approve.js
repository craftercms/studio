/**
 * File: delete.js
 * Component ID: templateholder-delete
 * @author: Roy Art
 * @date: 10.01.2011
 **/
(function(){

    CStudioAuthoring.register("TemplateHolder.Approve", {
        ITEM_ROW: [
            '<tr>',
                '<td class="text-center"><input type="checkbox" class="item-checkbox" data-item-id="{uri}" checked/></td>',
                '<td class="name"><div class="in">{internalName} {uri}</div></div.></td>',
                '<td class="text-right schedule">{scheduleDate}</td>',
            '</tr>'
        ].join("")
    });

    CStudioAuthoring.Env.ModuleMap.map("template-approve", CStudioAuthoring.TemplateHolder.Approve);

})();
