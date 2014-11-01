CStudioForms.Controls.RTE.InsertPredefinedTable = CStudioForms.Controls.RTE.InsertPredefinedTable || {

        createControl: function(name, cm) {

            var predefinedTables = cm.editor.contextControl.rteTables;
            predefinedTables = (predefinedTables === undefined) ? [] : (Array.isArray(predefinedTables)) ? predefinedTables : [predefinedTables.table];

            if (name == "predefinedTable" && predefinedTables && predefinedTables.length) {
                var c = cm.createMenuButton('predefinedTable', {
                    title: 'Insert predefined table',
                    image: CStudioAuthoringContext.authoringAppBaseUri + '/static-assets/themes/cstudioTheme/images/icons/predefined-table.png',
                    icons: false
                });

                c.onRenderMenu.add( function(c, m) {

                    var addMenuOption = function (el) {
                        this.add({
                            title: el.name,
                            onclick: function() {
                                tinyMCE.activeEditor.execCommand('mceInsertContent', false, el.prototype);
                            }
                        });
                    }
                    predefinedTables.forEach(addMenuOption, m);
                });

                // Return the new menu button instance
                return c;
            }
            return null;
        },        
        /**
         * Returns information about the plugin as a name/value array.
         * The current keys are longname, author, authorurl, infourl and version.
         *
         * @return {Object} Name/value array containing information about the plugin.
         */
        getInfo : function() {
            return {
                longname : 'Crafter Studio Insert Predefined Table',
                author : 'Crafter Software',
                authorurl : 'http://www.craftercms.org',
                infourl : 'http://www.craftercms.org',
                version : "1.0"
            };
        }
    };

tinymce.create('tinymce.plugins.CStudioManagedPredefinedTablePlugin', CStudioForms.Controls.RTE.InsertPredefinedTable);

// Register plugin with a short name
tinymce.PluginManager.add('insertpredefinedtable', tinymce.plugins.CStudioManagedPredefinedTablePlugin);

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-rte-insert-predefined-table", CStudioForms.Controls.RTE.InsertPredefinedTable);