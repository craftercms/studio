 /*
  * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as published by
  * the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.craftercms.studio.model.rest.security;

 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;

 import javax.validation.constraints.NotBlank;
 import javax.validation.constraints.Size;

 import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.SITE_ID;

 /**
  * Holds the data for encryption
  *
  * @author joseross
  * @since 3.1.5
  */
 @JsonIgnoreProperties
 public class EncryptRequest {

     /**
      * The value to encrypt
      */
     @NotBlank
     private String text;
     @Size(max = 50)
     @EsapiValidatedParam(type = SITE_ID)
     private String siteId;

     public String getText() {
         return text;
     }

     public void setText(final String text) {
         this.text = text;
     }

     public String getSiteId() {
         return siteId;
     }

     public void setSiteId(String siteId) {
         this.siteId = siteId;
     }
 }
