/**
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
 
/**
 * Global Header
 * 
 * @namespace Alfresco
 * @class Alfresco.Header
*/
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Element = YAHOO.util.Element,
      Event = YAHOO.util.Event;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML;

   Alfresco.CStudioHeader = function(htmlId)
   {
      this.name = "Alfresco.CStudioHeader";
      this.id = htmlId;

      this.widgets = {};
      
      /* Register this component */
      Alfresco.util.ComponentManager.register(this);
      
      /* Load YUI Components */
      Alfresco.util.YUILoaderHelper.require([], this.onComponentsLoaded, this);

      // Give the search component a chance to tell the header that it has been loaded
      // (and thus no page reload is required for a new search)
      YAHOO.Bubbling.on("searchComponentExists", this.onSearchComponentExists, this);
      
      return this;
   };

   Alfresco.CStudioHeader.prototype =
   {
      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options:
      {
         /**
          * Current siteId.
          * 
          * @property siteId
          * @type string
          * @default ""
          */
         siteId: "",

         /**
          * Current search type.
          * 
          * @property searchType
          * @type string
          * @default ""
          */
         searchType: ""
         
      },
      
      /**
       * Set multiple initialization options at once.
       *
       * @method setOptions
       * @param obj {object} Object literal specifying a set of options
       * @return {Alfresco.CStudioHeader} returns 'this' for method chaining
       */
      setOptions: function CStudioHeader_setOptions(obj)
      {
         this.options = YAHOO.lang.merge(this.options, obj);
         return this;
      },
      
      /**
       * Set messages for this component.
       *
       * @method setMessages
       * @param obj {object} Object literal specifying a set of messages
       * @return {Alfresco.CStudioHeader} returns 'this' for method chaining
       */
      setMessages: function CStudioHeader_setMessages(obj)
      {
         Alfresco.util.addMessages(obj, this.name);
         return this;
      },
      
      /**
       * Fired by YUILoaderHelper when required component script files have
       * been loaded into the browser.
       *
       * @method onComponentsLoaded
       */
      onComponentsLoaded: function CStudioHeader_onComponentsLoaded()
      {
         Event.onContentReady(this.id, this.onReady, this, true);           
      },
      
      /**
       * Fired by YUI when parent element is available for scripting.
       * Initial History Manager event registration
       *
       * @method onReady
       */
      onReady: function CStudioHeader_onReady()
      {
         Event.addListener(this.id + "-searchtext", "focus", this.focusSearchText, null, this);
         Event.addListener(this.id + "-searchtext", "blur", this.blurSearchText, null, this);
         Event.addListener(this.id + "-search-sbutton", "click", this.doSearch, null, this);
         
         this.defaultSearchText();
         
         // register the "enter" event on the search text field
         var zinput = Dom.get(this.id + "-searchtext");
         var me = this;
         new YAHOO.util.KeyListener(zinput, 
         {
            keys: 13
         }, 
         {
            fn: me.doSearch,
            scope: this,
            correctScope: true
         }, "keydown").enable();
           
      },
      
      /**
       * Update image class when sarch box has focus.
       *
       * @method focusSearchText
       */
      focusSearchText: function CStudioHeader_focusSearchText()
      {
         if (Dom.hasClass(this.id + "-searchtext", "gray"))
         {
            Dom.get(this.id + "-searchtext").value = "";
            Dom.removeClass(this.id + "-searchtext", "gray");
         }
         else
         {
            Dom.get(this.id + "-searchtext").select();
         }
      },
      
      /**
       * Set default search text when box loses focus and is empty.
       *
       * @method blurSearchText
       */
      blurSearchText: function CStudioHeader_blurSearchText()
      {
         var searchVal = Dom.get(this.id + "-searchtext").value;
         if (searchVal.length == 0)
         {
            this.defaultSearchText();
         }
      }, 
      
      /**
       * Set default search text for search box.
       *
       * @method defaultSearchText
       */
      defaultSearchText: function CStudioHeader_defaultSearchText()
      {
         Dom.get(this.id + "-searchtext").value = this._getToggleLabel(this.options.searchType);
         Dom.addClass(this.id + "-searchtext", "gray");
      },

      
      /**
       * Called by the Search component to tell the header that
       * no page refresh is required for a new search
       *
       * @method onSearchComponentExists
       * @param layer {object} Unused
       * @param args {object} Unused
       */
      onSearchComponentExists: function CStudioHeader_onSearchComponentExists(layer, args)
      {
         this.searchExists = true;
      },
      
      /**
       * Will trigger a search, via a page refresh to ensure the Back button works correctly
       *
       * @method doSearch
       */
      doSearch: function CStudioHeader_doSearch()
      {
         var searchTerm = Dom.get(this.id + "-searchtext").value;
         if (searchTerm.length != 0)
         {
            var searchAll =  (this.options.searchType == "all");
            
            // redirect to the search page
            var url = Alfresco.constants.URL_CONTEXT + "page/";
            if (this.options.siteId.length != 0)
            {
               url += "site/" + this.options.siteId + "/";
            }
            url += "cstudio-search?searchTerm=" + encodeURIComponent(searchTerm);
           
            window.location = url;
         }
      },
      
      /**
       * Returns the toggle label based on the passed-in search type
       *
       * @method _getToggleLabel
       * @param type {string} Search type
       * @return {string} i18n message corresponding to search type
       * @private
       */
      _getToggleLabel: function CStudioHeader__getToggleLabel(type)
      {
         return this._msg("header.search.searchsite", this.options.siteTitle);
      },
      
      /**
       * Gets a custom message
       *
       * @method _msg
       * @param messageId {string} The messageId to retrieve
       * @return {string} The custom message
       * @private
       */
      _msg: function CStudioHeader__msg(messageId)
      {
         return Alfresco.util.message.call(this, messageId, this.name, Array.prototype.slice.call(arguments).slice(1));
      }      
   };
})();
