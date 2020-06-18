<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <!-- to keep the right formatting -->
    <xsl:output method="xml" indent="yes" />
    <xsl:strip-space elements="*"/>

    <!-- copy all elements -->
    <xsl:template match="node() | @*">
        <!-- insert line breaks before comments -->
        <xsl:if test="self::comment()">
            <xsl:text>&#10;</xsl:text>
        </xsl:if>
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
        <!-- insert line breaks after comments -->
        <xsl:if test="self::comment()">
            <xsl:text>&#10;</xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="site-config">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
            <xsl:if test="not(translation)">
                <xsl:text>&#10;</xsl:text>
                <xsl:text>&#10;</xsl:text>
                <xsl:text>&#x9;</xsl:text>
                <xsl:comment>
                    <xsl:text> Site Translation </xsl:text>
                </xsl:comment>
                <xsl:text>&#10;</xsl:text>
                <xsl:text>&#x9;</xsl:text>
                <xsl:comment>
                    <xsl:text>&#10;&#x9;&#x9;&lt;translation&gt;&#10;&#x9;&#x9;&#x9;&lt;localeCodes&gt;&#10;&#x9;&#x9;&#x9;&#x9;&lt;localeCode&gt;en_us&lt;/localeCode&gt;&#10;&#x9;&#x9;&#x9;&lt;/localeCodes&gt;&#10;&#x9;&#x9;&#x9;&lt;defaultLocaleCode&gt;en_us&lt;/defaultLocaleCode&gt;&#10;&#x9;&#x9;&lt;/translation&gt;&#10;&#x9;&#x9;</xsl:text>
                </xsl:comment>
                <xsl:text>&#10;</xsl:text>
                <xsl:text>&#10;</xsl:text>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>