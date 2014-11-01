/**
 * File: history.js
 * Component ID: viewcontroller-history
 * @author: Roy Art
 * @date: 03.01.2011
 **/
(function(){

    var History,
        Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event,

        TemplateAgent = CStudioAuthoring.Component.TemplateAgent,
        template = CStudioAuthoring.TemplateHolder.History;

    CStudioAuthoring.register("ViewController.History", function() {
        CStudioAuthoring.ViewController.History.superclass.constructor.apply(this, arguments);
    });

    History = CStudioAuthoring.ViewController.History;
    YAHOO.extend(History, CStudioAuthoring.ViewController.Base, {
        events: ["wipeAndRevert","view","restore","compare","revert","wipeRecent"],
        actions: [".close-button"],
        //startup: [""],


        loadHistory: function(selection) {
            var _this = this,
                loadFn;
            loadFn = function() {
                _this.getComponent("div.history-listing").innerHTML =
                         '<table class="history-tbl history-listing"><tr><td><i>Loading, please wait&hellip;</i></td></tr></table>';
                CStudioAuthoring.Service.getVersionHistory(
                    CStudioAuthoringContext.site,
                    selection, {
                        success: function(history) {

                            var versions = history.versions;

                            var itemStateEl = _this.getComponent("span.show-for-item");

                            Dom.addClass(itemStateEl, CStudioAuthoring.Utils.getIconFWClasses( history.item));

                            itemStateEl.innerHTML = history.item.internalName;

                            var diffButtonEl = _this.getComponent("input.compare-checked");
                            diffButtonEl.parentNode.removeChild(diffButtonEl);

                            if(versions.length == 0) {
                                _this.getComponent("div.history-listing").innerHTML =
                                    '<table class="history-tbl history-listing"><tr><td>No Versions.</td></tr></table>';
                            } else {

                                _this.getComponent("a.wipe-out-and-revert").innerHTML = "";
                                _this.getComponent("div.history-listing").innerHTML =  '<table class="history-tbl history-listing"></table>';

                                for(var i=0; i<versions.length; i++) {

                                    var version = versions[i];

                                    var rowEl = document.createElement("tr");
                                    var col1El = document.createElement("td");
                                    Dom.addClass(col1El, "c1");
                                    col1El.innerHTML = "&nbsp;";
                                    rowEl.appendChild(col1El);

                                    var col2El = document.createElement("td");
                                    Dom.addClass(col2El, "c2");
                                    col2El.innerHTML = version.versionNumber;
                                    rowEl.appendChild(col2El);

                                    var col3El = document.createElement("td");
                                    Dom.addClass(col3El, "c3");
                                    col3El.innerHTML = CStudioAuthoring.Utils.formatDateFromString(version.lastModifiedDate, "tooltipformat");
                                    rowEl.appendChild(col3El);

                                    var col4El = document.createElement("td");
                                    Dom.addClass(col4El, "c4");
                                    col4El.innerHTML = version.lastModifier;
                                    rowEl.appendChild(col4El);

                                    var col6El = document.createElement("td");
                                    Dom.addClass(col6El, "c6");
                                    col6El.innerHTML = (version.comment)
                                        ? version.comment
                                        : "&nbsp;";
                                    rowEl.appendChild(col6El);

                                    var col5El = document.createElement("td");
                                    Dom.addClass(col5El, "c5");
                                    rowEl.appendChild(col5El);

                                    //var viewActionEl = document.createElement("a");
                                    //viewActionEl.innerHTML = "View";
                                    //viewActionEl.onclick = function() { alert("view"); };
                                    //col5El.appendChild(viewActionEl);

                                    //var dividerEl = document.createElement("span");
                                    //dividerEl.innerHTML = "&nbsp;|&nbsp";
                                    //col5El.appendChild(dividerEl);

                                    var revertActionEl = document.createElement("a");
                                    revertActionEl.innerHTML = "Revert";

                                    var revertFn = function() {
                                        CStudioAuthoring.Service.revertContentItem(
                                            CStudioAuthoringContext.site,
                                            this.item,
                                            this.version, {
                                                success: function() {
                                                    window.location.reload(true);
                                                },
                                                failure: function() {
                                                    alert("revert failed");
                                                }
                                            });
                                    };

                                    revertActionEl.item = versions[0].contentItem;
                                    revertActionEl.version = version.versionNumber;

                                    Event.addListener(revertActionEl, "click", revertFn);
                                    col5El.appendChild(revertActionEl);

                                    _this.getComponent("table.history-listing").appendChild(rowEl);

                                }
                            }
                            //set focus on submit/delete button
                            var oSubmitBtn = _this.getComponent(_this.actions[0]);

                            if (oSubmitBtn) {
                                CStudioAuthoring.Utils.setDefaultFocusOn(oSubmitBtn);
                            }
                        },
                        failure: function(){
                            _this.getComponent("div.history-listing").innerHTML =
                                '<table class="history-tbl history-listing"><tr><td>Unable to load version history. <a class="retry-dependency-load" href="javascript:">Try again</a></td></tr></table>';
                            Event.addListener(_this.getComponent("a.retry-dependency-load"), "click", loadFn);
                        }
                    });
            }
            loadFn();
        },

        wipeRecentEdits: function() {

            this.fire("wipeRecent");
        },
        
        revertToLive: function() {

            this.fire("revert");
        },
        wipeAndRevert: function() {

            this.fire("wipeAndRevert");
        },
        view: function() {

            this.fire("view");
        },
        restore: function() {

            this.fire("restore");
        },
        compare: function() {

            this.fire("compare");
        },
        closeButtonActionClicked: function() {
            this.end();
        }
    });

    CStudioAuthoring.Env.ModuleMap.map("viewcontroller-history", History);

})();
