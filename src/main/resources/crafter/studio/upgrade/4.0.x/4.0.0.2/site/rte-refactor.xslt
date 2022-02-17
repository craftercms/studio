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

    <!-- Update the legacy control -->
    <xsl:template match="//field[type='rte' and properties/property/name='allowResize']">
        <xsl:copy>
            <xsl:element name="type">
                <xsl:text>rte</xsl:text>
            </xsl:element>
            <xsl:copy-of select="id"/>
            <xsl:copy-of select="iceId"/>
            <xsl:copy-of select="title"/>
            <xsl:copy-of select="description"/>
            <xsl:copy-of select="defaultValue"/>
            <xsl:copy-of select="help"/>

            <!-- Copy existing properties excluding the ones not supported by the new control -->
            <xsl:variable name="properties"
                          select="properties/property[not(name='allowResize' or name='forcePTags' or
                                                          name='forceImageAlts')]"/>
            <xsl:element name="properties">
                <xsl:for-each select="$properties">
                    <xsl:element name="property">
                        <xsl:copy-of select="name"/>
                        <xsl:copy-of select="value"/>
                        <xsl:copy-of select="type"/>
                    </xsl:element>
                </xsl:for-each>

                <!-- Add a new property -->
                <xsl:element name="property">
                    <xsl:element name="name">
                        <xsl:text>autoGrow</xsl:text>
                    </xsl:element>
                    <xsl:element name="value">
                        <xsl:text>false</xsl:text>
                    </xsl:element>
                    <xsl:element name="type">
                        <xsl:text>boolean</xsl:text>
                    </xsl:element>
                </xsl:element>
            </xsl:element>

            <!-- Copy all existing constraints -->
            <xsl:variable name="constraints" select="constraints/constraint"/>
            <xsl:element name="constraints">
                <xsl:for-each select="$constraints">
                    <xsl:element name="constraint">
                        <xsl:copy-of select="name"/>
                        <xsl:copy-of select="value"/>
                        <xsl:copy-of select="type"/>
                    </xsl:element>
                </xsl:for-each>
            </xsl:element>
        </xsl:copy>
    </xsl:template>

    <!-- Rename the current control -->
    <xsl:template match="//field/type[text()='rte-tinymce5']">
        <xsl:element name="type">
            <xsl:text>rte</xsl:text>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>