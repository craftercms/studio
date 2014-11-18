<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" exclude-result-prefixes="xs" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema">
	<xsl:output indent="yes"/>
	<xsl:param name="contentType"/>
	<xsl:param name="internalName"/>
	
	
	<xsl:template match="/">
		<xsl:element name="{local-name()}">
			<xsl:call-template name="root"/>
		</xsl:element>
	</xsl:template>
	<xsl:template name="root">
		<xsl:copy>
			<xsl:element name="{local-name()}">
				<xsl:apply-templates>
					<xsl:with-param name="isRoot">true</xsl:with-param>
				</xsl:apply-templates>
			</xsl:element>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="*">
		<xsl:param name="isRoot"/>
		<xsl:element name="{local-name()}">
			<xsl:if test="$isRoot = 'true'">
				<content-type>/page/<xsl:value-of select="$contentType"/>
				</content-type>
				<internal-name>
					<xsl:value-of select="$internalName"/>
				</internal-name>
				<display-template>/templates/web/<xsl:value-of select="$contentType"/>.ftl</display-template>
			</xsl:if>
			<xsl:variable name="currentNodeName" select="local-name(.)"/>

			<xsl:if test="count(//self::*[local-name() = $currentNodeName]) &gt; 1 and preceding-sibling::*[local-name() != $currentNodeName]">

				
				<xsl:if test="//self::*[local-name() = $currentNodeName]">
					<xsl:for-each select="//self::*[local-name() = $currentNodeName]">
						<item>
							<xsl:element name="{local-name()}"><xsl:value-of select="."/></xsl:element >
						</item>
					</xsl:for-each>
				</xsl:if>
			</xsl:if>
			<xsl:if test="count(//self::*[local-name() = $currentNodeName]) = 1">
				<xsl:apply-templates select="@*|node()"/>
			</xsl:if>
		</xsl:element>
	</xsl:template>
	<xsl:template match="@*">
		<xsl:attribute name="{local-name()}">
			<xsl:value-of select="."/>
		</xsl:attribute>
	</xsl:template>
</xsl:stylesheet>