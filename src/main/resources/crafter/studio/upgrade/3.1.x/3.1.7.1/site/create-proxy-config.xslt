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

    <xsl:template match="environment-config">
        <xsl:text>&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:comment>
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
    ~ along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
</xsl:comment>
        <xsl:text>&#10;</xsl:text>
        <xsl:text>&#10;</xsl:text>
        <xsl:comment>
        This file configures the proxy servers for preview.

        Every request received by Engine will be matched against the patterns of each server
        and the first one that matches will be used as proxy.

        &lt;server&gt;
            &lt;id/&gt; (id of the server, can have any value)
            &lt;url/&gt; (url of the server)
            &lt;patterns&gt;
                &lt;pattern/&gt; (regex to match requests)
            &lt;/patterns&gt;
        &lt;/server&gt;
</xsl:comment>
        <xsl:text>&#10;</xsl:text>
        <xsl:element name="proxy-config">
            <xsl:element name="version"><xsl:text>1</xsl:text></xsl:element>
            <!-- Servers need to be added in the right order, that is why this template is not dynamic -->
            <xsl:element name="servers">

                <!-- Add graphql server using the existing value -->
                <xsl:element name="server">
                    <xsl:element name="id">
                        <xsl:text>graphql</xsl:text>
                    </xsl:element>
                    <xsl:element name="url">
                        <xsl:value-of select="graphql-server-url"/>
                    </xsl:element>
                    <xsl:element name="patterns">
                        <xsl:element name="pattern">
                            <xsl:text>/api/1/site/graphql.*</xsl:text>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>

                <!-- Add engine server using the existing value -->
                <xsl:element name="server">
                    <xsl:element name="id">
                        <xsl:text>engine</xsl:text>
                    </xsl:element>
                    <xsl:element name="url">
                        <xsl:value-of select="preview-engine-server-url"/>
                    </xsl:element>
                    <xsl:element name="patterns">
                        <xsl:element name="pattern">
                            <xsl:text>/api/1/.*</xsl:text>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>

                <!-- Add preview server using the existing value -->
                <xsl:element name="server">
                    <xsl:element name="id">
                        <xsl:text>preview</xsl:text>
                    </xsl:element>
                    <xsl:element name="url">
                        <xsl:value-of select="preview-server-url"/>
                    </xsl:element>
                    <xsl:element name="patterns">
                        <xsl:element name="pattern">
                            <xsl:text>.*</xsl:text>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>

            </xsl:element>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>