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

    <xsl:template match="site-config">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
            <xsl:if test="not(site-urls)">
                <xsl:comment>Site URLs. Default to http://localhost:8080 if blank</xsl:comment>
                <xsl:text>&#10;</xsl:text>
                <xsl:element name="site-urls">
                    <xsl:element name="authoring-url">
                            <xsl:apply-templates
                                    select="document('/config/studio/environment/environment-config,xml')/environment-config/authoring-server-url"/>
                    </xsl:element>
                    <xsl:text>&#10;</xsl:text>
                    <xsl:element name="staging-url">
                        <xsl:apply-templates
                                select="document('/config/studio/environment/environment-config,xml')/environment-config/staging-server-url"/>
                    </xsl:element>
                    <xsl:text>&#10;</xsl:text>
                    <xsl:element name="live-url">
                        <xsl:apply-templates
                                select="document('/config/studio/environment/environment-config,xml')/environment-config/live-server-url"/>
                    </xsl:element>
                    <xsl:text>&#10;</xsl:text>
                </xsl:element>
                <xsl:text>&#10;</xsl:text>
            </xsl:if>
            <xsl:if test="not(admin-email-address)">
                <xsl:element name="admin-email-address">
                    <xsl:apply-templates
                            select="document('/config/studio/environment/environment-config,xml')/environment-config/admin-email-address"/>
                </xsl:element>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>