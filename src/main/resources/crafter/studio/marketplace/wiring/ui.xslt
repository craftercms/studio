<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0">

    <xsl:param name="parentId"/>
    <xsl:param name="pluginId"/>
    <xsl:param name="newXml"/>

    <xsl:variable name="newFragment" select="parse-xml-fragment($newXml)"/>

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

    <!-- If the parent doesn't have a any configuration add the new section -->
    <xsl:template match="(//widget[@id=$parentId] | ${parentXpath})[not(configuration)]">
        <xsl:copy>
            <xsl:element name="configuration">
                <xsl:element name="widgets">
                    <xsl:value-of select="$newXml"/>
                </xsl:element>
            </xsl:element>
        </xsl:copy>
    </xsl:template>

    <!-- If the parent doesn't have a any widgets add the new section -->
    <xsl:template match="(//widget[@id=$parentId] | ${parentXpath})/configuration[not(widgets)]">
        <xsl:copy>
            <!-- Copy all existing configuration -->
            <xsl:apply-templates select="node() | @*"/>

            <xsl:element name="widgets">
                <xsl:value-of select="$newXml"/>
            </xsl:element>
        </xsl:copy>
    </xsl:template>


    <!-- If the parent already has other widgets, add a new one -->
    <xsl:template match="(//widget[@id=$parentId] | ${parentXpath})/configuration/widgets[not(widget/plugin[@id=$pluginId])]">
        <xsl:copy>
            <!-- Copy all existing widgets -->
            <xsl:apply-templates select="node() | @*"/>

            <!-- Add the new widget -->
            <xsl:copy-of select="$newFragment"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>