<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ /*
  ~  * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
  ~  *
  ~  * This program is free software: you can redistribute it and/or modify
  ~  * it under the terms of the GNU General Public License version 3 as published by
  ~  * the Free Software Foundation.
  ~  *
  ~  * This program is distributed in the hope that it will be useful,
  ~  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~  * GNU General Public License for more details.
  ~  *
  ~  * You should have received a copy of the GNU General Public License
  ~  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ~  */
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <!-- to keep the right formatting -->
    <xsl:output method="xml" indent="yes"/>
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

    <!-- Update statement for static-assets -->
    <xsl:template match="/site-policy">
        <xsl:copy>
            <xsl:element name="statement">
                <xsl:element name="target-path-pattern">
                    <xsl:text>/static-assets/.*</xsl:text>
                </xsl:element>
                <xsl:element name="permitted">
                    <xsl:element name="path">
                        <xsl:element name="source-regex">
                            <xsl:text>[\(\)]</xsl:text>
                        </xsl:element>
                        <xsl:element name="target-regex">
                            <xsl:text>-</xsl:text>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
            <xsl:for-each select="statement">
                <!-- Keep other statements unless it is matching the same files -->
                <xsl:if test="not(target-path-pattern='/static-assets/.*') or not(permitted/path/source-regex='[\(\)]')">
                    <xsl:copy-of select="."/>
                </xsl:if>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
