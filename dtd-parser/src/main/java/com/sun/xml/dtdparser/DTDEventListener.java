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

import java.util.EventListener;

/**
 * All DTD parsing events are signaled through delegate methods of this
 * interface. Client applications should either
 * <ol>
 * <li>implement {@code DTDEventListener} and override
 * methods that pertain to DTD features they need to process, <strong>or</strong></li>
 * <li>extend {@link DTDHandlerBase} and override only those methods 
 * that pertain to DTD features they need to process.</li>
 * </ol>
 * <p>
 * The following table associates several common parsing events with the methods
 * that the DTD parser calls:</p>
 * <table>
 * <caption class="caption" style="font-family: sans-serif;"><span>Common DTD parsing events</span></caption>
 * <tr class="table-header"><th>Event Description</th><th>Delegate Method</th></tr>
 * <tr class="even-row-color"><td>Processing Instructions</td><td>{@link #processingInstruction}</td></tr>
 * <tr class="odd-row-color"><td>Notation Declarations</td><td>{@link #notationDecl}</td></tr>
 * <tr class="even-row-color"><td>Unparsed Entity Declarations</td><td>{@link #unparsedEntityDecl}</td></tr>
 * <tr class="odd-row-color"><td>Internal General Entity Declarations</td><td>{@link #internalGeneralEntityDecl}</td></tr>
 * <tr class="even-row-color"><td>External General Entity Declarations</td><td>{@link #internalGeneralEntityDecl}</td></tr>
 * <tr class="odd-row-color"><td>Internal Parameter Entity Declarations</td><td>{@link #internalParameterEntityDecl}</td></tr>
 * <tr class="even-row-color"><td>External Parameter Entity Declarations</td><td>{@link #externalParameterEntityDecl}</td></tr>
 * <tr class="odd-row-color"><td>Start of DTD</td><td>{@link #startDTD}</td></tr>
 * <tr class="even-row-color"><td>End of DTD</td><td>{@link #endDTD}</td></tr>
 * </table>
 * <p>
 * The DTD parser calls the method indicated in the second column for each event in the first column.
 * </p>
 * @since 1.0.7, SAX 1.0
 * @author Mavaddat Javid
 */
public interface DTDEventListener extends EventListener {

    /**
     * Sets the mutable locator object that points to the current source line.
     * @param loc the locator object
    */
    void setDocumentLocator(Locator loc);

    /**
     * This method executes upon notification of a processing instruction as per <a href="https://www.w3.org/TR/xml-stylesheet/xml-stylesheet.xml#the-xml-stylesheet-processing-instruction">W3 Rec <cite>Associating Style Sheets with XML documents</cite></a>.
     * 
     * <p>Client applications may override this method in a subclass to take
     * specific actions for each processing instruction, such as setting status
     * variables or invoking other methods.</p>
     *
     * @param target The target of the processing instruction
     * @param data   The processing instruction data, or null if
     *               none is supplied.
     * @throws SAXException Any SAX exception, possibly
     *                      wrapping another exception.
     * @see org.xml.sax.HandlerBase#processingInstruction
     */
    void processingInstruction(String target, String data)
            throws SAXException;

    /**
     * Receive notification of a notation declaration event per <a href="https://www.w3.org/TR/REC-xml/#sec-notation-decl">W3 Rec <cite>Extensible Markup Language (XML) 1.0</cite></a>.
     *
     * <p>It is up to a client application to record the notation for later
     * reference, if necessary;
     * notations may appear as attribute values and in unparsed entity
     * declarations, and are sometime used with processing instruction
     * target names.</p>
     *
     * <p>At least one of {@code publicId} and {@code systemId} shall
     * be non-null. If a system identifier is present, and it is a URL, the SAX
     * parser shall resolve it fully before passing it to the client
     * application through this event.</p>
     *
     * <p>There is no guarantee that the notation declaration will be
     * reported before any unparsed entities that use it.</p>
     *
     * @param name The notation name.
     * @param publicId The notation's public identifier, or null if
     *        none was given.
     * @param systemId The notation's system identifier, or null if
     *        none was given.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #unparsedEntityDecl
     * @see org.xml.sax.Attributes
     */
    void notationDecl(String name, String publicId, String systemId)
            throws SAXException;

    /**
     * Receive notification of an unparsed entity declaration event per <a href="https://www.w3.org/TR/REC-xml/#dt-unparsed">W3 Rec <cite>Extensible Markup Language (XML) 1.0</cite></a>.
     *
     * <p>Note that the notation name corresponds to a notation
     * reported by the {@link #notationDecl notationDecl} event.
     * It is up to the application to record the entity for later
     * reference, if necessary;
     * unparsed entities may appear as attribute values.
     * </p>
     *
     * <p>If the system identifier is a URL, the parser must resolve it
     * fully before passing it to the application.</p>
     *
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @param name The unparsed entity's name.
     * @param publicId The entity's public identifier, or null if none
     *        was given.
     * @param systemId The entity's system identifier.
     * @param notationName The name of the associated notation.
     * @see #notationDecl
     * @see org.xml.sax.Attributes
     */
    void unparsedEntityDecl(String name, String publicId,
                            String systemId, String notationName)
            throws SAXException;

    /**
     * This method executes upon notification of a internal general entity declaration event per <a href="https://www.w3.org/TR/REC-xml/#sec-internal-ent">W3 Rec <cite>Extensible Markup Language (XML) 1.0</cite></a>.
     *
     * @param name  The internal general entity name.
     * @param value The value of the entity, which may include unexpanded
     *              entity references.  Character references will have been
     *              expanded.
     * @throws SAXException for errors
     * @see #externalGeneralEntityDecl(String, String, String)
     */
    void internalGeneralEntityDecl(String name, String value)
            throws SAXException;

    /**
     * This method executes upon notification of an external parsed general
     * entity declaration event per <a href="https://www.w3.org/TR/REC-xml/#sec-external-ent">W3 Rec <cite>Extensible Markup Language (XML) 1.0</cite></a>.
     *
     * <p>If a system identifier is present, and it is a relative URL, the
     * parser will have resolved it fully before passing it through this
     * method to a listener.</p>
     *
     * @param name     The entity name.
     * @param publicId The entity's public identifier, or null if
     *                 none was given.
     * @param systemId The entity's system identifier.
     * @throws SAXException for errors
     * @see #unparsedEntityDecl(String, String, String, String)
     */
    void externalGeneralEntityDecl(String name, String publicId,
                                   String systemId)
            throws SAXException;

    /**
     * This method executes upon notification of a internal parameter entity
     * declaration event per <a href="https://www.w3.org/TR/REC-xml/#sec-internal-ent">W3 Rec <cite>Extensible Markup Language (XML) 1.0</cite></a>.
     *
     * @param name  The internal parameter entity name.
     * @param value The value of the entity, which may include unexpanded
     *              entity references.  Character references will have been
     *              expanded.
     * @throws SAXException for errors
     * @see #externalParameterEntityDecl(String, String, String)
     */
    void internalParameterEntityDecl(String name, String value)
            throws SAXException;

    /**
     * This method executes upon notification of an external parameter entity
     * declaration event per <a href="https://www.w3.org/TR/REC-xml/#sec-external-ent">W3 Rec <cite>Extensible Markup Language (XML) 1.0</cite></a>.
     *
     * <p>If a system identifier is present, and it is a relative URL, the
     * parser will have resolved it fully before passing it through this
     * method to a listener.</p>
     *
     * @param name     The parameter entity name.
     * @param publicId The entity's public identifier, or null if
     *                 none was given.
     * @param systemId The entity's system identifier.
     * @throws SAXException for errors
     * @see #unparsedEntityDecl(String, String, String, String)
     */
    void externalParameterEntityDecl(String name, String publicId,
                                     String systemId)
            throws SAXException;

    /**
     * This method executes upon notification of the beginning of the DTD per <a href="https://www.w3.org/TR/REC-xml/#sec-starttags">W3 Rec <cite>Extensible Markup Language (XML) 1.0</cite></a>.
     *
     * @param in Current input entity.
     * @throws SAXException for errors
     * @see #endDTD()
     */
    void startDTD(InputEntity in)
            throws SAXException;

    /**
     * This method executes upon notification of reaching the end of a DTD per <a href="https://www.w3.org/TR/REC-xml/#sec-starttags">W3 Rec <cite>Extensible Markup Language (XML) 1.0</cite></a>.
     * <p>The parser will invoke this method only once.
     *
     * @throws SAXException for errors
     * @see #startDTD(InputEntity)
     */
    void endDTD()
            throws SAXException;

    /**
     * This method executes upon notification that a comment has been read per <a href="https://www.w3.org/TR/REC-xml/#sec-comments">W3 Rec <cite>Extensible Markup Language (XML) 1.0</cite></a>.
     *
     * <P> Note that processing instructions are the mechanism designed
     * to hold information for consumption by applications, not comments.
     * XML systems may rely on applications being able to access information
     * found in processing instructions; this is not true of comments, which
     * are typically discarded.
     *
     * @param text the text within the comment delimiters.
     * @throws SAXException for errors
     */
    void comment(String text)
            throws SAXException;

    /**
     * This method executes upon notification of character data from the body of
     * an XML element per <a href="https://www.w3.org/TR/REC-xml/#sec-starttags">W3 Rec <cite>Extensible Markup Language (XML) 1.0</cite></a>.
     *
     * @apiNote The Parser will call this method to report each chunk of
     * character data.  SAX parsers may return all contiguous character
     * data in a single chunk, or they may split it into several
     * chunks; however, all of the characters in any single event
     * must come from the same external entity, so that the Locator
     * provides useful information.
     *
     * @implNote The client application must not attempt to read from the array
     * outside of the specified range.
     *
     * @implSpec Some parsers will report whitespace using the
     * {@link #ignorableWhitespace} method rather than this one (validating
     * parsers must do so).
     *
     * @param ch     The characters from the DTD.
     * @param start  The starting offset into the buffer.
     * @param length The number of characters to read from the array.
     * @throws SAXException for errors
     * @see #ignorableWhitespace(char[], int, int)
     */
    void characters(char[] ch, int start, int length)
            throws SAXException;


    /**
     * This method executes upon notification of ignorable whitespace in element
     * content.
     *
     * <p>Validating Parsers must use this method to report each chunk
     * of ignorable whitespace (see <a 
     * href="https://www.w3.org/TR/xml/#sec-white-space">the W3C XML
     *  1.0 recommendation, section 2.10</a>): non-validating parsers may also use
     * this method if they are capable of parsing and using content models.</p>
     *
     * <p>SAX parsers may return all contiguous whitespace in a single
     * chunk, or they may split it into several chunks; however, all of
     * the characters in any single event must come from the same
     * external entity, so that the Locator provides useful
     * information.</p>
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * @param ch     The characters from the DTD.
     * @param start  The start position in the array.
     * @param length The number of characters to read from the array.
     * @throws SAXException for errors
     * @see #characters(char[], int, int)
     */
    void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException;

    /**
     * This method executes upon notification that a {@code CDATA} section is
     * beginning. Data in a {@code CDATA} section is then reported through the
     * either {@link #characters} or {@link #ignorableWhitespace}.
     * #ignorableWhitespace}.
     *
     * @throws SAXException for errors
     * @see #endCDATA()
     */
    void startCDATA() throws SAXException;


    /**
     * This method executes upon notification that the {@code CDATA} section finished.
     *
     * @throws SAXException for errors
     * @see #startCDATA()
     */
    void endCDATA() throws SAXException;

    /**
     * This method executes upon notification of a fatal error.
     * <p>
     * The default behavior is to take no action.
     * </p>
     * <p>
     * Applications may override this method in a subclass to take specific
     * actions for each error, such as inserting the message in a log file
     * or printing it to the console.
     * </p>
     * <p>
     * For XML processing errors, a SAX driver must use this method to report
     * the conditions specified in the XML recommendation. For example, a
     * driver must include information about the XML version, encoding, and
     * standalone status in every report.
     * </p>
     * <p>
     * For different errors, the SAX driver will use this method in
     * different ways.  Some drivers will provide a full stack trace,
     * others will merely provide a message.  In some cases, the
     * information reported by this method will be sufficient, while in
     * other cases it will not; in those cases, the parser writer
     * will need to provide their own error messages.
     * </p>
     * <p>
     * The application must assume that the document is unusable after the
     * parser has invoked this method, and should continue (if at all) only
     * for the sake of collecting additional error messages: in fact, SAX
     * parsers are free to stop reporting any other events once this method
     * has been invoked.
     * </p>
     *
     * @param err The error information encapsulated in a SAX parse
     *            exception.
     * @throws SAXException Any SAX exception, possibly wrapping another
     *                      exception.
     * @see #warning(SAXParseException)
     * @see #error(SAXParseException)
     */
    void fatalError(SAXParseException err)
            throws SAXException;

    /**
     * This method executes upon notification of an error.
     * <p>
     * The default behavior is to take no action.
     * </p>
     * <p>
     * Applications may override this method in a subclass to take specific
     * actions for each error, such as inserting the message in a log file
     * or printing it to the console.
     * </p>
     * <p>
     * For XML processing errors, a SAX driver must use this method to report
     * the conditions specified in the XML recommendation. For example, a
     * driver must include information about the XML version, encoding, and
     * standalone status in every report.
     * </p>
     * <p>
     * For different errors, the SAX driver will use this method in
     * different ways.  Some drivers will provide a full stack trace,
     * others will merely provide a message.  In some cases, the
     * information reported by this method may be sufficient to
     * locate the error in the original XML document.  Note, however,
     * that reporting the beginning position of the related event is
     * not sufficient, since the original document is likely no longer
     * available.  In such cases, the SAX driver should instead report
     * the location of the related event in the XML parser's input
     * source or byte stream.
     * </p>
     * <p>
     * The application must assume that the document is invalid after the
     * first error is reported, and should stop processing and
     * ignore all future events once an error is reported.
     * </p>
     * <p>
     * Filters may use this method to report other, non-XML errors as
     * well.
     * </p>
     * @param err The error information encoded as an exception.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.SAXParseException
     */
    void error(SAXParseException err) throws SAXException;

    /**
     * This method executes upon notification of a warning.
     * <p>
     * The default behavior is to take no action.
     * </p>
     * <p>
     * Applications may override this method in a subclass to take specific
     * actions for each warning, such as inserting the message in a log file
     * or printing it to the console.
     * </p>
     * <p>
     * For XML processing errors, a SAX driver must use this method to report
     * the conditions specified in the XML recommendation. For example, a
     * driver must include information about the XML version, encoding, and
     * standalone status in every report.
     * </p>
     * <p>
     * For recoverable errors, the parser must continue to provide normal
     * parsing events after invoking this method: it should still be possible
     * for the application to process the document through to the end.
     * </p>
     * <p>
     * Filters may use this method to report other, non-XML warnings as well.
     * </p>
     * @param warn The warning information encoded as an exception.
     * @throws SAXException Any SAX exception, possibly wrapping another
     *           exception.
     * @see org.xml.sax.ErrorHandler#warning
     * @see org.xml.sax.SAXParseException
     */
    void warning(SAXParseException warn) throws SAXException;

    /**
     * Elements whose <a href="https://www.w3.org/TR/DOM-Level-3-CMLS/content-models.html#CM-Interfaces-CMModel-createCMElementDeclaration">{@code contentSpec}</a> is marked as {@code  EMPTY} allow only attributes. Their attributes may
     * characterize the element or reference other files. An empty element
     * doesn't contain any content or data.
     * <p>
     * An example of an empty element is {@code <attribute name="prodid" type="positiveInteger"/>} processing instruction.
     */
    short CONTENT_MODEL_EMPTY = 0;
    /**
     * Elements whose <a href="https://www.w3.org/TR/DOM-Level-3-CMLS/content-models.html#CM-Interfaces-CMModel-createCMElementDeclaration">{@code contentSpec}</a> is marked as {@code ANY} allows either an element or parsed character
     * data as content for the element. The element may contain any number of
     * child elements or character data.
     * <p>
     * An example of an any element is {@code <element name="foo" minOccurs="1" maxOccurs="1">* </element>} processing instruction.
     */
    short CONTENT_MODEL_ANY = 1;
    /**
     * Elements whose <a href="https://www.w3.org/TR/DOM-Level-3-CMLS/content-models.html#CM-Interfaces-CMModel-createCMElementDeclaration">{@code contentSpec}</a> is marked as {@code MIXED} allows either an element or parsed character
     * data as content for the element. The element may contain any number of
     * child elements or character data. The character data must be of type
     * #PCDATA.
     * <p>
     * An example of a mixed element is {@code <element name="foo" minOccurs="1" maxOccurs="1">#PCDATA </element>} processing instruction.
     */
    short CONTENT_MODEL_MIXED = 2;
    /**
     * Elements whose <a href="https://www.w3.org/TR/DOM-Level-3-CMLS/content-models.html#CM-Interfaces-CMModel-createCMElementDeclaration">{@code contentSpec}</a> is marked as {@code CHILDREN} allows only elements as content for the element.
     * The element may contain any number of child elements.
     * <p>
     * An example of a children element is {@code <element name="foo" minOccurs="1" maxOccurs="1"> </element>} processing instruction.
     */
    short CONTENT_MODEL_CHILDREN = 3;

    /**
     * This method executes upon notification that parsing of content model is beginning.
     *
     * @param elementName      name of the element whose content model is going to be defined.
     * @param contentModelType {@link #CONTENT_MODEL_EMPTY}
     *                         this element has {@code EMPTY} content model. This notification
     *                         will be immediately followed by the corresponding endContentModel.
     *                         {@link #CONTENT_MODEL_ANY}
     *                         this element has {@code ANY} content model. This notification
     *                         will be immediately followed by the corresponding endContentModel.
     *                         {@link #CONTENT_MODEL_MIXED}
     *                         this element has mixed content model. #PCDATA will not be reported.
     *                         each child element will be reported by mixedElement method.
     *                         {@link #CONTENT_MODEL_CHILDREN}
     *                         this element has child content model. The actual content model will
     *                         be reported by childElement, startModelGroup, endModelGroup, and
     *                         connector methods. Possible call sequences are:
     *                         <p>
     *                         START := MODEL_GROUP
     *                         MODEL_GROUP := startModelGroup TOKEN (connector TOKEN)* endModelGroup
     *                         TOKEN := childElement
     *                         | MODEL_GROUP
     * @throws SAXException for errors
     */
    void startContentModel(String elementName, short contentModelType) throws SAXException;

    /**
     * This method executes upon notification that parsing of content model is finished.
     * @param elementName      name of the element whose content model is going to be defined.
     * @param contentModelType {@link #CONTENT_MODEL_EMPTY}
     *                          this element has {@code EMPTY} content model. This notification
     *                          will be immediately followed by the corresponding endContentModel.
     *                          {@link #CONTENT_MODEL_ANY}
     *                          this element has {@code ANY} content model. This notification
     *                          will be immediately followed by the corresponding endContentModel.
     *                          {@link #CONTENT_MODEL_MIXED}
     *                          this element has mixed content model. #PCDATA will not be reported.
     *                          each child element will be reported by mixedElement method.
     *                          {@link #CONTENT_MODEL_CHILDREN}
     *                          this element has child content model. The actual content model will
     *                          be reported by childElement, startModelGroup, endModelGroup, and
     *                          connector methods. Possible call sequences are:
     *                          START := MODEL_GROUP
     *                          MODEL_GROUP := startModelGroup TOKEN (connector TOKEN)* endModelGroup
     *                          TOKEN := childElement
     *                          | MODEL_GROUP
     * @throws SAXException for errors
     */
    void endContentModel(String elementName, short contentModelType) throws SAXException;

    /**
     * An unpredicated attribute declared in an {@code ATTLIST} declaration which sets
     * the default value of the attribute.
     */
    short USE_NORMAL = 0;
    /**
     * An attribute declared in an {@code ATTLIST} declaration which is marked optional using the {@code #IMPLIED} keyword predicate.
     */
    short USE_IMPLIED = 1;
    /**
     * An attribute declared in an {@code ATTLIST} declaration which is marked fixed using the {@code #FIXED} keyword predicate.
     */
    short USE_FIXED = 2;
    /**
     * An attribute declared in an {@code ATTLIST} declaration which is marked required using the {@code #REQUIRED} keyword predicate.
     */
    short USE_REQUIRED = 3;

    /**
     * For each entry in an {@code ATTLIST} declaration,
     * this event will be fired.
     * @param elementName name of the element
     * @param attributeName name of the attribute
     * @param attributeType attribute type. This is the same as
     *                      the value of the TYPE parameter in the
     *                      {@code ATTLIST} declaration, or "{@code #IMPLIED}" if the
     *                      {@code ATTLIST} declaration did not specify a type.
     * @param enumeration enumeration values. This is the same as
     *                      the value of the ENUMERATION parameter in the
     *                      {@code ATTLIST} declaration, or null if the {@code ATTLIST}
     *                      declaration did not specify an enumeration.
     * @param attributeUse attribute use. This is the same as
     *                      the value of the USE parameter in the
     *                      {@code ATTLIST} declaration, or {@code USE_NORMAL} if the
     *                      {@code ATTLIST} declaration did not specify a use.
     * @param defaultValue default value. This is the same as
     *                      the value of the DEFAULT parameter in the
     *                      {@code ATTLIST} declaration, or null if the {@code ATTLIST}
     *                      declaration did not specify a default.
     *
     * <p>
     * DTD allows the same attributes to be declared more than
     * once, and in that case the first one wins. I think
     * this method will be only fired for the first one,
     * but I need to check.
     * @throws SAXException for errors
     */
    void attributeDecl(String elementName, String attributeName, String attributeType,
                       String[] enumeration, short attributeUse, String defaultValue) throws SAXException;

    /**
     * This method executes upon notification of child element of child content model. This method is called for each child element.
     * @param elementName name of the child element
     * @param occurrence occurrence of the child element <ul>
     *                  <li>{@link #OCCURRENCE_ONCE}</li>
     *                  <li>{@link #OCCURRENCE_ZERO_OR_ONE}</li>
     *                  <li>{@link #OCCURRENCE_ZERO_OR_MORE}</li>
     *                  <li>{@link #OCCURRENCE_ONE_OR_MORE}</li></ul>
     * @throws SAXException for errors
     * @see #childElement(String, short)
     */
    void childElement(String elementName, short occurrence) throws SAXException;

    /**
     * This method executes upon notification of child element of mixed content model. This method is called for each child element.
     * @param elementName name of the child element
     *
     * @throws SAXException for errors
     * @see #startContentModel(String, short)
     */
    void mixedElement(String elementName) throws SAXException;

    /**
     * This method executes upon notification of start of model group.
     * @throws SAXException for errors
     */
    void startModelGroup() throws SAXException;

    /**
     * This method executes upon notification of end of model group.
     * @param occurrence occurrence of the model group <ul>
     *                  <li>{@link #OCCURRENCE_ONCE}</li>
     *                  <li>{@link #OCCURRENCE_ZERO_OR_ONE}</li>
     *                  <li>{@link #OCCURRENCE_ZERO_OR_MORE}</li>
     *                  <li>{@link #OCCURRENCE_ONE_OR_MORE}</li></ul>
     * @throws SAXException for errors
     */
    void endModelGroup(short occurrence) throws SAXException;

    /** 
     * The Connector Type {@code choice}.
     * See more info at <a href="http://www.w3.org/TR/xmlschema11-1/#element-choice">W3 XML Schema Element Choice</a>. */
    short CHOICE = 0;
    /** 
     * The Connector Type {@code sequence}.
     * See more info at <a href="https://www.w3.org/TR/xmlschema11-1/#element-sequence">W3 XML Schema Element Sequence</a>. */
    short SEQUENCE = 1;

    /**
     * Connectors in one model group should be guaranteed to be the same.
     * <p>
     * This means you should never see an event sequence like (a|b,c)
     * @param connectorType {@link #CHOICE} or {@link #SEQUENCE}
     * @throws SAXException for errors
     */
    void connector(short connectorType) throws SAXException;

    /** 
     * Zero or more occurrences corresponding to regex {@code *} quantifier. */
    short OCCURRENCE_ZERO_OR_MORE = 0;
    /** 
     * One or more occurrences corresponding to regex {@code +} quantifier.
    */
    short OCCURRENCE_ONE_OR_MORE = 1;
    /** 
     * Zero or one occurrences corresponding to regex {@code ?} quantifier
     * . */
    short OCCURRENCE_ZERO_OR_ONE = 2;
    /** 
     * Exactly one occurrence corresponding to regex {@code {1}} quantifier.
    */
    short OCCURRENCE_ONCE = 3;
}
