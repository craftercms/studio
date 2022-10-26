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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0">

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

    <!-- Insert Taxonomy (after components) and Scripts (after templates) -->
    <xsl:template match="/site-config/repository/folders">
        <xsl:copy>
            <xsl:for-each select="folder">
                <xsl:if test="@path='/static-assets'">
                    <xsl:if test="not(exists(../folder[@path='/taxonomy']) or exists(../folder[@path='/components']))">
                        <xsl:call-template name="taxonomyFolder"/>
                    </xsl:if>
                </xsl:if>
                <xsl:copy-of select="."/>
                <xsl:if test="@path='/components'">
                    <xsl:if test="not(exists(../folder[@path='/taxonomy']))">
                        <xsl:call-template name="taxonomyFolder"/>
                    </xsl:if>
                </xsl:if>
            </xsl:for-each>
            <xsl:if test="not(exists(folder[@path='/taxonomy']) or exists(folder[@path='/components']) or exists(folder[@path='/static-assets']))">
                <xsl:call-template name="taxonomyFolder"/>
            </xsl:if>
            <xsl:if test="not(folder[@path='/scripts'])">
                <xsl:call-template name="scriptsFolder"/>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="taxonomyFolder">
        <xsl:element name="folder">
            <xsl:attribute name="name">Taxonomy</xsl:attribute>
            <xsl:attribute name="path">/taxonomy</xsl:attribute>
            <xsl:attribute name="read-direct-children">false</xsl:attribute>
            <xsl:attribute name="attach-root-prefix">true</xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template name="scriptsFolder">
        <xsl:element name="folder">
            <xsl:attribute name="name">Scripts</xsl:attribute>
            <xsl:attribute name="path">/scripts</xsl:attribute>
            <xsl:attribute name="read-direct-children">false</xsl:attribute>
            <xsl:attribute name="attach-root-prefix">false</xsl:attribute>
        </xsl:element>
    </xsl:template>


</xsl:stylesheet>
