<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License version 3 as published by
  ~ the Free Software Foundation.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <!-- Identity template: copy everything by default -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Match the specific <find-regex> under asset item-type/asset dependency-type and replace its content -->
    <xsl:template match="item-type[name='asset']/dependency-types/dependency-type[name='asset']/includes/pattern/find-regex[text()='/static-assets/([^&lt;&quot;''\)\?\]\#]+)']">
        <find-regex>/static-assets/([^&lt;"'\)\?\\\]\#]+)</find-regex>
    </xsl:template>
</xsl:stylesheet>
