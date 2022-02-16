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

    <xsl:template match="/dependency-resolver/item-types">
        <xsl:copy>
            <xsl:for-each select="item-type">
                <xsl:element name="item-type">
                    <xsl:copy-of select="name"/>
                    <xsl:element name="includes">
                        <xsl:for-each select="includes/path-pattern">
                            <xsl:copy-of select="."/>
                        </xsl:for-each>
                    </xsl:element>
                    <xsl:choose>
                        <xsl:when test="name = 'rendering-template' and not(excludes/path-pattern)">
                            <xsl:element name="excludes">
                                <xsl:element name="path-pattern">
                                    <xsl:text>/templates/system/plugins/.+</xsl:text>
                                </xsl:element>
                            </xsl:element>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:copy-of select="excludes"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:element name="dependency-types">
                        <xsl:for-each select="dependency-types/dependency-type">
                            <xsl:element name="dependency-type">
                                <xsl:copy-of select="name"/>
                                <xsl:element name="includes">
                                    <xsl:for-each select="includes/pattern">
                                        <xsl:element name="pattern">
                                            <xsl:copy-of select="find-regex"/>
                                            <xsl:if test="transforms">
                                                <xsl:element name="transforms">
                                                    <xsl:for-each select="transforms/transform">
                                                        <xsl:element name="transform">
                                                            <xsl:copy-of select="split"/>
                                                            <xsl:copy-of select="match"/>
                                                            <xsl:copy-of select="replace"/>
                                                        </xsl:element>
                                                    </xsl:for-each>
                                                </xsl:element>
                                            </xsl:if>
                                        </xsl:element>
                                    </xsl:for-each>
                                </xsl:element>
                            </xsl:element>
                        </xsl:for-each>

                        <xsl:if test="(name = 'page' or name = 'component' or name = 'item') and not(dependency-types/dependency-type/name = 'form-definition')">
                            <xsl:element name="dependency-type">
                                <xsl:element name="name">
                                    <xsl:text>form-definition</xsl:text>
                                </xsl:element>
                                <xsl:element name="includes">
                                    <xsl:element name="pattern">
                                        <xsl:element name="find-regex">
                                            <xsl:text>&lt;component.+&gt;[\s\S]*?&lt;\/component&gt;</xsl:text>
                                        </xsl:element>
                                        <xsl:element name="transforms">
                                            <xsl:element name="transform">
                                                <xsl:element name="match">
                                                    <xsl:text>[\s\S]*?&lt;content-type&gt;([^&lt;]+)&lt;\/content-type&gt;[\s\S]*</xsl:text>
                                                </xsl:element>
                                                <xsl:element name="replace">
                                                    <xsl:text>/config/studio/content-types$1/form-definition.xml</xsl:text>
                                                </xsl:element>
                                            </xsl:element>
                                        </xsl:element>
                                    </xsl:element>
                                </xsl:element>
                            </xsl:element>
                        </xsl:if>
                    </xsl:element>
                </xsl:element>
            </xsl:for-each>

            <xsl:if test="not(item-type/name = 'content-type')">
                <xsl:element name="item-type">
                    <xsl:element name="name">
                        <xsl:text>content-type</xsl:text>
                    </xsl:element>
                    <xsl:element name="includes">
                        <xsl:element name="path-pattern">
                            <xsl:text>/config/studio/content-types/.*?/form-definition\.xml</xsl:text>
                        </xsl:element>
                    </xsl:element>
                    <xsl:element name="dependency-types">
                        <xsl:element name="dependency-type">
                            <xsl:element name="name">
                                <xsl:text>form-definition</xsl:text>
                            </xsl:element>
                            <xsl:element name="includes">
                                <xsl:element name="pattern">
                                    <xsl:element name="find-regex">
                                        <xsl:text>&lt;value&gt;\/(?:component|page).+?&lt;\/value&gt;</xsl:text>
                                    </xsl:element>
                                    <xsl:element name="transforms">
                                        <xsl:element name="transform">
                                            <xsl:element name="split">
                                                <xsl:text>true</xsl:text>
                                            </xsl:element>
                                            <xsl:element name="match">
                                                <xsl:text>&lt;value&gt;(\/(?:component|page).+?)&lt;\/value&gt;</xsl:text>
                                            </xsl:element>
                                            <xsl:element name="replace">
                                                <xsl:text>/config/studio/content-types$1/form-definition.xml</xsl:text>
                                            </xsl:element>
                                        </xsl:element>
                                    </xsl:element>
                                </xsl:element>
                            </xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>