<?xml version="1.0" encoding="UTF-8"?>
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

    <xsl:template match="files[not(file/path = '/engine/urlrewrite.xml')]">
        <files>
            <xsl:apply-templates select="node() | @*"/>
            <file>
                <path>/engine/urlrewrite.xml</path>
                <title>Engine URL Rewrite Configuration (XML Style)</title>
                <description>Engine URL Rewrite Configuration (XML Style)</description>
                <samplePath>/studio/administration/samples/sample-urlrewrite.xml</samplePath>
            </file>
        </files>
    </xsl:template>

</xsl:stylesheet>