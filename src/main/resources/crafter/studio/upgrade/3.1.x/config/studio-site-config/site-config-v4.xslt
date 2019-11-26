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
            <xsl:apply-templates select="node() | @*" />
            <xsl:if test="not(publishing)">
                <xsl:element name="publishing">
                    <xsl:element name="comments">
                        <xsl:text>&#10;</xsl:text>
                        <xsl:comment>
                            <xsl:text> Global setting would apply to all </xsl:text>
                        </xsl:comment>
                        <xsl:element name="required">
                            <xsl:text>false</xsl:text>
                        </xsl:element>
                        <xsl:text>&#10;</xsl:text>
                        <xsl:comment>
                            <xsl:text> Additional (also optional) specific overrides </xsl:text>
                        </xsl:comment>
                        <xsl:text>&#10;</xsl:text>
                        <xsl:comment>
                            <xsl:text> &lt;delete-required/&gt; </xsl:text>
                        </xsl:comment>
                        <xsl:text>&#10;</xsl:text>
                        <xsl:comment>
                            <xsl:text> &lt;bulk-publish-required/&gt; </xsl:text>
                        </xsl:comment>
                        <xsl:text>&#10;</xsl:text>
                        <xsl:comment>
                            <xsl:text> &lt;publish-by-commit-required/&gt; </xsl:text>
                        </xsl:comment>
                    </xsl:element>
                </xsl:element>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>