/**
 * File: history.js
 * Component ID: viewcontroller-history
 * @author: Roy Art
 * @date: 03.01.2011
 **/
(function () {

    var History,
        Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event,

        TemplateAgent = CStudioAuthoring.Component.TemplateAgent,
        template = CStudioAuthoring.TemplateHolder.History;

    CStudioAuthoring.register('ViewController.History', function () {
        CStudioAuthoring.ViewController.History.superclass.constructor.apply(this, arguments);
    });

    History = CStudioAuthoring.ViewController.History;
    YAHOO.extend(History, CStudioAuthoring.ViewController.Base, {

        events: ['wipeAndRevert', 'view', 'restore', 'compare', 'revert', 'wipeRecent'],

        actions: ['.close-button'],

        loadHistory: function (selection) {
            var _this = this,
                colspan = 5,
                loadFn;
            loadFn = function () {

                var tbody = _this.getComponent('table.item-listing tbody');
                tbody.innerHTML = '<tr><td colspan="5"><i>Loading, please wait&hellip;</i></td></tr>';

                CStudioAuthoring.Service.getVersionHistory(
                    CStudioAuthoringContext.site,
                    selection, {
                        success: function (history) {

                            var versions = history.versions;

                            var itemStateEl = _this.getComponent('span.show-for-item');
                            Dom.addClass(itemStateEl, CStudioAuthoring.Utils.getIconFWClasses(history.item));
                            itemStateEl.innerHTML = history.item.internalName;

                            if (versions.length == 0) {
                                tbody.innerHTML = '<tr><td colspan="5"><i>No versions found.</i></td></tr>';
                            } else {

                                tbody.innerHTML = '';

                                for (var i = 0; i < versions.length; i++) {

                                    var version = versions[i],
                                        rowEl = document.createElement("tr"),
                                        tdEl,
                                        col2El,
                                        col3El,
                                        col4El,
                                        col5El,
                                        col6El,
                                        revertActionEl;

                                    col2El = document.createElement('div');
                                    Dom.addClass(col2El, "c2");
                                    col2El.innerHTML = version.versionNumber;
                                    tdEl = document.createElement('td');
                                    tdEl.appendChild(col2El);
                                    rowEl.appendChild(tdEl);

                                    col3El = document.createElement('div');
                                    Dom.addClass(col3El, "c3");
                                    col3El.innerHTML = CStudioAuthoring.Utils.formatDateFromString(version.lastModifiedDate, "tooltipformat");
                                    tdEl = document.createElement('td');
                                    tdEl.appendChild(col3El);
                                    rowEl.appendChild(tdEl);

                                    col4El = document.createElement('div');
                                    Dom.addClass(col4El, "c4");
                                    col4El.innerHTML = version.lastModifier;
                                    tdEl = document.createElement('td');
                                    tdEl.appendChild(col4El);
                                    rowEl.appendChild(tdEl);

                                    col6El = document.createElement('div');
                                    Dom.addClass(col6El, "c6");
                                    col6El.innerHTML = (version.comment) ? version.comment : "&nbsp;";
                                    tdEl = document.createElement('td');
                                    tdEl.appendChild(col6El);
                                    rowEl.appendChild(tdEl);

                                    col5El = document.createElement('div');
                                    Dom.addClass(col5El, "c5");
                                    tdEl = document.createElement('td');
                                    tdEl.appendChild(col5El);
                                    rowEl.appendChild(tdEl);

                                    revertActionEl = document.createElement("a");
                                    revertActionEl.innerHTML = "Revert";
                                    revertActionEl.item = selection;
                                    revertActionEl.version = version.versionNumber;
                                    col5El.appendChild(revertActionEl);
                                    Event.addListener(revertActionEl, "click", function () {
                                        CStudioAuthoring.Service.revertContentItem(
                                            CStudioAuthoringContext.site,
                                            this.item,
                                            this.version, {
                                                success: function () {
                                                    window.location.reload(true);
                                                },
                                                failure: function () {
                                                    alert("revert failed");
                                                }
                                            });
                                    });

                                    tbody.appendChild(rowEl);

                                }
                            }
                            //set focus on submit/delete button
                            var oSubmitBtn = _this.getComponent(_this.actions[0]);

                            if (oSubmitBtn) {
                                CStudioAuthoring.Utils.setDefaultFocusOn(oSubmitBtn);
                            }
                        },
                        failure: function () {
                            tbody.innerHTML = '<tr><td>Unable to load version history. <a class="retry-dependency-load" href="javascript:">Try again</a></td></tr>';
                            Event.addListener(_this.getComponent("a.retry-dependency-load"), "click", loadFn);
                        }
                    });
            };
            loadFn();
        },

        wipeRecentEdits: function () {
            this.fire("wipeRecent");
        },

        revertToLive: function () {
            this.fire("revert");
        },
        wipeAndRevert: function () {
            this.fire("wipeAndRevert");
        },
        view: function () {
            this.fire("view");
        },
        restore: function () {
            this.fire("restore");
        },
        compare: function () {
            this.fire("compare");
        },
        closeButtonActionClicked: function () {
            this.end();
        }
    });

    CStudioAuthoring.Env.ModuleMap.map("viewcontroller-history", History);

})();
