<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
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

    <!-- copy all elements -->
    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>

    <!-- insert line breaks before and after top level comments -->
    <xsl:template match="/comment()">
        <xsl:text>&#10;</xsl:text><xsl:copy-of select="."/><xsl:text>&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="config/tools/tool/controls[not(control/name='rte-tinymce4')]">
        <xsl:for-each select="control">
            <xsl:copy>
                <xsl:apply-templates select="node() | @*"/>
            </xsl:copy>
            <xsl:if test="name = 'rte'">
                <xsl:element name="control">
                    <xsl:element name="name"><xsl:text>rte-tinymce4</xsl:text></xsl:element>
                    <xsl:element name="icon">
                        <xsl:element name="class"><xsl:text>fa-code</xsl:text></xsl:element>
                    </xsl:element>
                </xsl:element>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>