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
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>

    <!-- insert line breaks before and after top level comments -->
    <xsl:template match="/comment()">
        <xsl:text>&#10;</xsl:text><xsl:copy-of select="."/><xsl:text>&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="site-config/form-engine">
        <xsl:copy>
            <xsl:apply-templates/>
            <xsl:if test="not(ignore-postfix-fields)">
                <xsl:text>&#10;</xsl:text>
                <xsl:comment>
                    <xsl:text> List of field names that should not have a postfix </xsl:text>
                </xsl:comment>
                <xsl:text>&#10;</xsl:text>
                <xsl:element name="ignore-postfix-fields">
                    <xsl:element name="field">
                        <xsl:text>file-name</xsl:text>
                    </xsl:element>
                    <xsl:element name="field">
                        <xsl:text>internal-name</xsl:text>
                    </xsl:element>
                    <xsl:element name="field">
                        <xsl:text>placeInNav</xsl:text>
                    </xsl:element>
                    <xsl:element name="field">
                        <xsl:text>scripts</xsl:text>
                    </xsl:element>
                    <xsl:element name="field">
                        <xsl:text>mime-type</xsl:text>
                    </xsl:element>
                    <xsl:element name="field">
                        <xsl:text>force-https</xsl:text>
                    </xsl:element>
                    <xsl:element name="field">
                        <xsl:text>navLabel</xsl:text>
                    </xsl:element>
                    <xsl:element name="field">
                        <xsl:text>expired</xsl:text>
                    </xsl:element>
                    <xsl:element name="field">
                        <xsl:text>key</xsl:text>
                    </xsl:element>
                    <xsl:element name="field">
                        <xsl:text>value</xsl:text>
                    </xsl:element>
                    <xsl:element name="field">
                        <xsl:text>items</xsl:text>
                    </xsl:element>
                    <xsl:element name="field">
                        <xsl:text>redirect-url</xsl:text>
                    </xsl:element>
                    <xsl:element name="field">
                        <xsl:text>authorizedRoles</xsl:text>
                    </xsl:element>
                    <xsl:element name="field">
                        <xsl:text>role</xsl:text>
                    </xsl:element>
                    <xsl:element name="field">
                        <xsl:text>disabled</xsl:text>
                    </xsl:element>
                </xsl:element>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="site-config">
        <xsl:copy>
            <xsl:apply-templates/>
            <xsl:if test="not(form-engine)">
                <xsl:element name="form-engine">
                    <xsl:text>&#10;</xsl:text>
                    <xsl:comment>
                        <xsl:text> Indicates if postfixes should be required for all fields </xsl:text>
                    </xsl:comment>
                    <xsl:text>&#10;</xsl:text>
                    <xsl:element name="field-name-postfix">
                        <xsl:text>false</xsl:text>
                    </xsl:element>
                    <xsl:text>&#10;</xsl:text>
                    <xsl:comment>
                        <xsl:text> List of field names that should not have a postfix </xsl:text>
                    </xsl:comment>
                    <xsl:text>&#10;</xsl:text>
                    <xsl:element name="ignore-postfix-fields">
                        <xsl:element name="field">
                            <xsl:text>file-name</xsl:text>
                        </xsl:element>
                        <xsl:element name="field">
                            <xsl:text>internal-name</xsl:text>
                        </xsl:element>
                        <xsl:element name="field">
                            <xsl:text>placeInNav</xsl:text>
                        </xsl:element>
                        <xsl:element name="field">
                            <xsl:text>scripts</xsl:text>
                        </xsl:element>
                        <xsl:element name="field">
                            <xsl:text>mime-type</xsl:text>
                        </xsl:element>
                        <xsl:element name="field">
                            <xsl:text>force-https</xsl:text>
                        </xsl:element>
                        <xsl:element name="field">
                            <xsl:text>navLabel</xsl:text>
                        </xsl:element>
                        <xsl:element name="field">
                            <xsl:text>expired</xsl:text>
                        </xsl:element>
                        <xsl:element name="field">
                            <xsl:text>key</xsl:text>
                        </xsl:element>
                        <xsl:element name="field">
                            <xsl:text>value</xsl:text>
                        </xsl:element>
                        <xsl:element name="field">
                            <xsl:text>items</xsl:text>
                        </xsl:element>
                        <xsl:element name="field">
                            <xsl:text>redirect-url</xsl:text>
                        </xsl:element>
                        <xsl:element name="field">
                            <xsl:text>authorizedRoles</xsl:text>
                        </xsl:element>
                        <xsl:element name="field">
                            <xsl:text>role</xsl:text>
                        </xsl:element>
                        <xsl:element name="field">
                            <xsl:text>disabled</xsl:text>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>