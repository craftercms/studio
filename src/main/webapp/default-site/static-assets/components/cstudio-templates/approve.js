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
                '<td class="text-center small"><input type="checkbox" class="item-checkbox" data-item-id="{uri}" checked/></td>',
                '<td class="name large"><div class="in">{internalName} {uri}</div></div></td>',
                '<td class="text-right schedule medium">{scheduledDate}</td>',
            '</tr>'
        ].join("")
    });

    CStudioAuthoring.Env.ModuleMap.map("template-approve", CStudioAuthoring.TemplateHolder.Approve);

})();
