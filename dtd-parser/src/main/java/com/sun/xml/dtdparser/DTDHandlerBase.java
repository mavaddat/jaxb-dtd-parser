/*
 * Copyright (c) 1998, 2022, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.xml.dtdparser;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This is a minimal implementation of the {@link DTDEventListener}
 * interface, meaning that none of the delegate methods do anything. This
 * class is provided as a convenience for inheritance in client applications.
 * To consume DTD events, client applications may either extend this class or
 * implement the {@link DTDEventListener} interface
 * directly.
 * <p>
 * Exemplary concrete child classes using this pattern may be found below:
 * <ul>
 * <li><a href="https://github.com/openjdk/jdk/blob/master/src/java.xml/share/classes/org/xml/sax/helpers/DefaultHandler.java#L70">
 * {@code org.xml.sax.helpers.DefaultHandler}</a> in <cite>OpenJDK</cite>'s
 * <cite>JDK Project</cite></li>
 * <li><a href="https://github.com/android-async-http/android-async-http/blob/master/sample/src/main/java/com/loopj/android/http/sample/SaxSample.java#L105">
 * {@code com.loopj.android.http.sample.SaxSample}</a> in <cite>James
 * Smith</cite>'s <cite>Android Asynchronous Http Client</cite></li>
 * <li><a href="https://github.com/stanfordnlp/CoreNLP/blob/main/src/edu/stanford/nlp/process/TransformXML.java#L41">
 * {@code edu.stanford.nlp.process.TransformXML}</a> in
 * <cite>StanfordNLP</cite>'s <cite>Python NLP Library
 * for Many Human Languages</cite></li>
 * <li><a href="https://github.com/liquibase/liquibase/blob/master/liquibase-core/src/main/java/liquibase/parser/core/xml/XMLChangeLogSAXHandler.java#L21">
 * {@code liquibase.parser.core.xml.XMLChangeLogSAXHandler}</a> in
 * <cite>Nathan Voxland</cite>'s <cite>Liquibase library</cite></li>
 * <li><a href="https://github.com/thinkgem/jeesite4/blob/v5.3/common/src/main/java/com/jeesite/common/utils/excel/ExcelReader.java#L32">
 * {@code com.jeesite.common.utils.excel.ExcelReader}</a> in
 * <cite>thinkgem</cite>'s <cite>jeesite</cite></li>
 * <li><a href="https://github.com/w3c/epubcheck/blob/main/src/main/java/com/adobe/epubcheck/xml/handlers/DelegateDefaultHandler.java#L20">
 * {@code com.adobe.epubcheck.xml.handlers.DelegateDefaultHandler}</a> in
 * <cite>World Wide Web Consortium (W3C)</cite>'s <cite>EPUBCheck</cite></li>
 * <li><a href="https://github.com/adempiere/adempiere/blob/develop/base/src/org/adempiere/pipo/PackInHandler.java#L75">
 * {@code org.adempiere.pipo.PackInHandler}</a> in <cite>ADempiere</cite>'s
 * <cite>ADempiere Project</cite></li>
 * <li><a href="https://github.com/eclipse-ee4j/glassfish/blob/master/appserver/web/web-core/src/main/java/org/apache/tomcat/util/digester/Digester.java#L63">
 * {@code org.apache.tomcat.util.digester.Digester}</a> in
 * <cite>Eclipse EE4J</cite>'s <cite>GlassFish</cite></li>
 * <li><a href="https://github.com/eclipse-ee4j/jaxb-ri/blob/master/jaxb-ri/xjc/src/main/java/com/sun/tools/xjc/reader/dtd/TDTDReader.java#L64">
 * {@code com.sun.tools.xjc.reader.dtd.TDTDReader}</a> in <cite>Eclipse
 * EE4J</cite>'s <cite>Implementation of JAXB</cite></li>
 * </ul>
 * Note: This list is not exhaustive nor do the examples demonstrate all
 * possible use cases. Inclusion in the list is not intended as an endorsement.
 */
public class DTDHandlerBase implements DTDEventListener {

    /**
     * Constructs a DTDHandlerBase.
     */
    public DTDHandlerBase() {}

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
    }

    @Override
    public void setDocumentLocator(Locator loc) {
    }

    @Override
    public void fatalError(SAXParseException err) throws SAXException {
        throw err;
    }

    @Override
    public void error(SAXParseException err) throws SAXException {
        throw err;
    }

    @Override
    public void warning(SAXParseException err) throws SAXException {
    }

    @Override
    public void notationDecl(String name, String publicId, String systemId) throws SAXException {
    }

    @Override
    public void unparsedEntityDecl(String name, String publicId,
                                   String systemId, String notationName) throws SAXException {
    }

    @Override
    public void endDTD() throws SAXException {
    }

    @Override
    public void externalGeneralEntityDecl(String n, String p, String s) throws SAXException {
    }

    @Override
    public void internalGeneralEntityDecl(String n, String v) throws SAXException {
    }

    @Override
    public void externalParameterEntityDecl(String n, String p, String s) throws SAXException {
    }

    @Override
    public void internalParameterEntityDecl(String n, String v) throws SAXException {
    }

    @Override
    public void startDTD(InputEntity in) throws SAXException {
    }

    @Override
    public void comment(String n) throws SAXException {
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

    @Override
    public void startCDATA() throws SAXException {
    }

    @Override
    public void endCDATA() throws SAXException {
    }


    @Override
    public void startContentModel(String elementName, short contentModelType) throws SAXException {
    }

    @Override
    public void endContentModel(String elementName, short contentModelType) throws SAXException {
    }

    @Override
    public void attributeDecl(String elementName, String attributeName, String attributeType,
                              String[] enumeration, short attributeUse, String defaultValue) throws SAXException {
    }

    @Override
    public void childElement(String elementName, short occurrence) throws SAXException {
    }

    @Override
    public void mixedElement(String elementName) throws SAXException {
    }

    @Override
    public void startModelGroup() throws SAXException {
    }

    @Override
    public void endModelGroup(short occurrence) throws SAXException {
    }

    @Override
    public void connector(short connectorType) throws SAXException {
    }
}

