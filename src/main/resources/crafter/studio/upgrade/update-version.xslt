<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xslt="http://www.w3.org/1999/XSL/Transform">

    <xsl:output omit-xml-declaration="no"/>

    <!-- define parameter -->
    <xsl:param name="version" />

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

    <!-- update the version value -->
    <xsl:template match="version">
        <xslt:text>$version</xslt:text>
    </xsl:template>

</xsl:stylesheet>
