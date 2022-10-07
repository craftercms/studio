<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

    <!-- Remove child-content datasource -->
    <xsl:template match="/config/tools/tool/datasources/datasource[name/text() = 'child-content']">
        <!-- Empty to remove the whole element -->
    </xsl:template>

    <!-- Remove dropTargets datasource -->
    <xsl:template match="/config/tools/tool/datasources/datasource[name/text() = 'dropTargets']">
        <!-- Empty to remove the whole element -->
    </xsl:template>

    <!-- Rename site-component => simpleTaxonomy datasource -->
    <xsl:template match="/config/tools/tool/datasources/datasource[name/text() = 'site-component']">
        <xsl:element name="datasource">
            <xsl:element name="name">
                <xsl:text>simpleTaxonomy</xsl:text>
            </xsl:element>
            <xsl:element name="icon">
                <xsl:element name="class">
                    <xsl:text>fa-tags</xsl:text>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <!-- Add components datasource -->
    <xsl:template match="/config/tools/tool/datasources">
        <xsl:copy>
            <xsl:if test="not(datasource/name/text() = 'components')">
                <xsl:element name="datasource">
                    <xsl:element name="name">
                        <xsl:text>components</xsl:text>
                    </xsl:element>
                    <xsl:element name="icon">
                        <xsl:element name="class">
                            <xsl:text>fa-puzzle-piece</xsl:text>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>
                <xsl:text>&#10;</xsl:text>
            </xsl:if>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>