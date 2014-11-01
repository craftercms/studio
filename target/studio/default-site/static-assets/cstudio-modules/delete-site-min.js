/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * DeleteCrafterSite module.
 *
 * Displays a dialog with cofirmation to delete a site.
 * 
 * @namespace Alfresco.module
 * @class Alfresco.module.DeleteCrafterSite
 */
(function()
{
   /**
    * DeleteCrafterSite constructor.
    *
    * DeleteCrafterSite is considered a singleton so constructor should be treated as private,
    * please use Alfresco.module.getDeleteCrafterSiteInstance() instead.
    *
    * @param {string} htmlId The HTML id of the parent element
    * @return {Alfresco.module.DeleteCrafterSite} The new DeleteCrafterSite instance
    * @constructor
    * @private
    */
   Alfresco.module.DeleteCrafterSite = function(containerId)
   {
      var instance = Alfresco.util.ComponentManager.get(containerId);
      if (instance !== null)
      {
         throw new Error("An instance of Alfresco.module.DeleteCrafterSite already exists.");
      }

      Alfresco.module.DeleteCrafterSite.superclass.constructor.call(this, "Alfresco.module.DeleteCrafterSite", containerId, ["button", "container", "connection", "selector", "json"]);

      // Set prototype properties
      this.deletePromptActive = false;

      return this;
   };

   YAHOO.extend(Alfresco.module.DeleteCrafterSite, Alfresco.component.Base,
   {
      /**
       * Will become true when the template (that sets the i18n messages)
       * to this module has been loaded.
       */
      localized: false,

      /**
       * Makes sure the dialog isn't displayed twice at the same time
       */
      deletePromptActive: false,

      /**
       * Shows the dialog
       * in different ways depending on the config parameter.
       *
       * @method show
       * @param config {object} describes how the upload dialog should be displayed
       * The config object is in the form of:
       * {
       *    site:
       *    {
       *       shortName: {string},    // shortName of site to delete
       *       title: {string}      // Name of site to delete
       *    }
       * }
       */
      show: function DeleteCrafterSite_show(config)
      {
         var c = config;
         if (this.localized)
         {
             this._showDialog(c);
         }
         else
         {
            Alfresco.util.Ajax.request(
            {
               url: Alfresco.constants.URL_SERVICECONTEXT + "cstudio/modules/delete-site",
               dataObj:{ htmlid: this.id },
               successCallback:
               {
                  fn: function()
                  {
                     this.onMessagesLoaded(c);
                  },
                  scope: this
               },
               execScripts: true,
               failureMessage: "Could not load delete site messages"
            });
         }
      },

      /**
       * Called when the DeleteCrafterSite messages has been returned from the server.
       * Shows the dialog.
       *
       * @method onMessagesLoaded
       * @param config {object} The config for the dialog
       */
      onMessagesLoaded: function DeleteCrafterSite_onMessagesLoaded(config)
      {
         this.localized = true;
         this._showDialog(config);
      },

      /**
       * Shows the dialog.
       *
       * @method _showDialog
       * @param config {object} The config for the dialog
       * @private
       */
      _showDialog: function DeleteCrafterSite__showDialog(config)
      {
         if (!this.deletePromptActive)
         {
            this.deletePromptActive = true;
            var me = this,
               c = config;
            Alfresco.util.PopupManager.displayPrompt(
            {
               title: Alfresco.util.message("title.DeleteCrafterSite", this.name),
               text: Alfresco.util.message("label.DeleteCrafterSite", this.name,
               {
                  "0": Alfresco.util.encodeHTML(c.site.title)
               }),
               noEscape: true,
               buttons: [
               {
                  text: Alfresco.util.message("button.delete", this.name),
                  handler: function DeleteCrafterSite__sD_delete()
                  {                        
                     this.destroy();
                     me._onDeleteClick.call(me, c);
                  }
               },
               {
                  text: Alfresco.util.message("button.cancel", this.name),
                  handler: function DeleteCrafterSite__sD_cancel()
                  {
                     me.deletePromptActive = false;
                     this.destroy();
                  },
                  isDefault: true
               }]
            });
         }
      },
      
      /**
       * Handles delete click event.
       *
       * @method _onDeleteClick
       * @param config {object} The config for the dialog
       * @private
       */
      _onDeleteClick: function DeleteCrafterSite__onDeleteClick(config)
      {
         var me = this,
            c = config;
         Alfresco.util.PopupManager.displayPrompt(
         {
            title: Alfresco.util.message("title.DeleteCrafterSite", this.name),
            text: Alfresco.util.message("label.confirmDeleteCrafterSite", this.name),
            noEscape: true,            
            buttons: [
               {
                  text: Alfresco.util.message("button.yes", this.name),
                  handler: function DeleteCrafterSite__oDC_delete()
                  {
                     this.destroy();
                     me._onConfirmedDeleteClick.call(me, c);
                  }
               },
               {
                  text: Alfresco.util.message("button.no", this.name),
                  handler: function DeleteCrafterSite__oDC_cancel()
                  {
                     me.deletePromptActive = false;
                     this.destroy();
                  },
                  isDefault: true
               }]
         });
      },

      /**
       * Handles confirmed delete click event.
       *
       * @method _onConfirmedDeleteClick
       * @param config {object} The config for the dialog
       * @private
       */
      _onConfirmedDeleteClick: function DeleteCrafterSite__onConfirmedDeleteClick(config)
      {
         var me = this,
            c = config;
         var feedbackMessage = Alfresco.util.PopupManager.displayMessage(
         {
            text: "Deleting website", //Alfresco.util.message(deletingSite", this.name),
            spanClass: "wait",
            displayTime: 0
         });
         
         // user has confirmed, perform the actual delete
         Alfresco.util.Ajax.jsonPost(
         {
            url: Alfresco.constants.URL_SERVICECONTEXT + "cstudio/modules/delete-site",
            dataObj:
            {
               shortName: config.site.shortName
            },
            successCallback:
            {
               fn: function(response)
               {
                  me.deletePromptActive = false;
                  feedbackMessage.destroy();
                  if (response.json && response.json.success)
                  {
                     Alfresco.util.PopupManager.displayMessage(
                     {
                        text: "Website deleted" //Alfresco.util.message("message.siteDeleted", this.name)
                     });
                     
                     // Tell other components that the site has been deleted
                     YAHOO.Bubbling.fire("siteDeleted",
                     {
                        site: c.site
                     });
                  }
                  else
                  {
                     Alfresco.util.PopupManager.displayMessage(
                     {
                        text: Alfresco.util.message("message.deleteFailed", this.name)
                     });
                  }
               },
               scope: this
            },
            failureCallback:
            {
               fn: function(response)
               {
                  me.deletePromptActive = false;
                  feedbackMessage.destroy();
                  Alfresco.util.PopupManager.displayMessage(
                  {
                     text: Alfresco.util.message("message.deleteFailed", this.name)
                  });
               },
               scope: this
            }
         });
      }
   });
})();

Alfresco.module.getDeleteCrafterSiteInstance = function()
{
   var instanceId = "alfresco-DeleteCrafterSite-instance";
   return Alfresco.util.ComponentManager.get(instanceId) || new Alfresco.module.DeleteCrafterSite(instanceId);
};