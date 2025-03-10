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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

/**
 * This is how the parser talks to its input entities, of all kinds.
 * The entities are in a stack.
 *
 * <P> For internal entities, the character arrays are referenced here,
 * and read from as needed (they're read-only).  External entities have
 * mutable buffers, that are read into as needed.
 *
 * <P> <em>Note:</em> This maps CRLF (and CR) to LF without regard for
 * whether it's in an external (parsed) entity or not.  The XML 1.0 spec
 * is inconsistent in explaining EOL handling; this is the sensible way.
 *
 * @author David Brownell
 * @author Janet Koenig
 * @version 1.4 00/08/05
 */
public class InputEntity {
    private int start, finish;
    private char[] buf;
    private int lineNumber = 1;
    private boolean returnedFirstHalf = false;
    private boolean maybeInCRLF = false;

    // name of entity (never main document or unnamed DTD PE)
    private String name;

    private InputEntity next;

    // for system and public IDs in diagnostics
    private InputSource input;

    // this is a buffer; some buffers can be replenished.
    private Reader reader;
    private boolean isClosed;

    private DTDEventListener errHandler;
    private Locale locale;

    private StringBuffer rememberedText;
    private int startRemember;

    // record if this is a PE, so endParsedEntity won't be called
    private boolean isPE;

    // InputStreamReader throws an internal per-read exception, so
    // we minimize reads.  We also add a byte to compensate for the
    // "ungetc" byte we keep, so that our downstream reads are as
    // nicely sized as we can make them.
    final private static int BUFSIZ = 8 * 1024 + 1;

    final private static char[] newline = {'\n'};
    /**
     * Gets the input entity based on the event listener and locale.
     * @param h the event listener
     * @param l the locale
     * @return the input entity
     */
    public static InputEntity getInputEntity(DTDEventListener h, Locale l) {
        InputEntity retval = new InputEntity();
        retval.errHandler = h;
        retval.locale = l;
        return retval;
    }
    /**
     * Obscures the constructor.
     */
    private InputEntity() {
    }

    //
    // predicate:  return true iff this is an internal entity reader,
    // and so may safely be "popped" as needed.  external entities have
    // syntax to uphold; internal parameter entities have at most validity
    // constraints to monitor.  also, only external entities get decent
    // location diagnostics.
    //
    /**
     * Returns true if this is an internal entity reader.
     * @return true if this is an internal entity reader
     */
    public boolean isInternal() {
        return reader == null;
    }

    //
    // predicate:  return true iff this is the toplevel document
    //
    /**
     * Returns true if this is the top level document.
     * @return true if this is the top level document
     */
    public boolean isDocument() {
        return next == null;
    }

    //
    // predicate:  return true iff this is a PE expansion (so that
    // LexicalEventListener.endParsedEntity won't be called)
    //
    /**
     * Returns true if this is a PE expansion, so that
     * {@code LexicalEventListener.endParsedEntity} won't be called
     * @return true if this is a PE expansion
     */
    public boolean isParameterEntity() {
        return isPE;
    }

    //
    // return name of current entity
    //
    /**
     * Gets the name of the current entity.
     * @return the name of the current entity
     */
    public String getName() {
        return name;
    }

    //
    // use this for an external parsed entity
    //
    /**
     * Initializes the input entity.
     * @param in the input source
     * @param name the name of the entity
     * @param stack the input entity stack
     * @param isPE true if this is a PE
     * @throws IOException if there is an I/O error
     * @throws SAXException if there is a SAX error
     */
    public void init(InputSource in, String name, InputEntity stack,
                     boolean isPE)
            throws IOException, SAXException {

        input = in;
        this.isPE = isPE;
        reader = in.getCharacterStream();

        if (reader == null) {
            InputStream bytes = in.getByteStream();

            if (bytes == null)
                if (Boolean.getBoolean("enableExternalEntityProcessing"))
                    reader = XmlReader.createReader(new URL(in.getSystemId()).openStream());
                else
                    fatal("P-082", new Object[] {in.getSystemId()});
            else if (in.getEncoding() != null)
                reader = XmlReader.createReader(in.getByteStream(), in.getEncoding());
            else
                reader = XmlReader.createReader(in.getByteStream());
        }
        next = stack;
        buf = new char[BUFSIZ];
        this.name = name;
        checkRecursion(stack);
    }

    //
    // use this for an internal parsed entity; buffer is readonly
    //
    /**
     * Initializes the input entity.
     * @param b the character array
     * @param name the name of the entity
     * @param stack the input entity stack
     * @param isPE true if this is a PE
     * @throws SAXException if there is a SAX error
     */
    public void init(char[] b, String name, InputEntity stack, boolean isPE)
            throws SAXException {

        next = stack;
        buf = Arrays.copyOf(b, b.length);
        finish = b.length;
        this.name = name;
        this.isPE = isPE;
        checkRecursion(stack);
    }

    /**
     * Checks for recursion based on the input entity stack.
     * @param stack the input entity stack
     * @throws SAXException if there is a SAX error
     */
    private void checkRecursion(InputEntity stack)
            throws SAXException {

        if (stack == null)
            return;
        for (stack = stack.next; stack != null; stack = stack.next) {
            if (stack.name != null && stack.name.equals(name))
                fatal("P-069", new Object[]{name});
        }
    }

    /**
     * Removes and returns the last character from the buffer.
     * @return the last character from the buffer
     * @throws IOException for errors
     */
    public InputEntity pop() throws IOException {

        // caller has ensured there's nothing left to read
        close();
        return next;
    }

    /**
     * returns true iff there's no more data to consume ...
     * @throws IOException for errors
     * @throws SAXException for errors
     * @return true iff there's no more data to consume
     */
    public boolean isEOF() throws IOException, SAXException {

        // called to ensure WF-ness of included entities and to pop
        // input entities appropriately ... EOF is not always legal.
        if (start >= finish) {
            fillbuf();
            return start >= finish;
        } else
            return false;
    }

    /**
     * Returns the name of the encoding in use, else null; the name
     * returned is in as standard a form as we can get.
     * @return the name of the encoding in use
     */
    public String getEncoding() {

        if (reader == null)
            return null;
        if (reader instanceof XmlReader)
            return ((XmlReader) reader).getEncoding();

        // XXX prefer a java2std() call to normalize names...

        if (reader instanceof InputStreamReader)
            return ((InputStreamReader) reader).getEncoding();
        return null;
    }


    /**
     * returns the next name char, or NUL ... faster than getc(),
     * and the common "name or nmtoken must be next" case won't
     * need ungetc().
     * @return the next name char, or NUL
     * @throws IOException for errors
     * @throws SAXException for errors
     */
    public char getNameChar() throws IOException, SAXException {

        if (finish <= start)
            fillbuf();
        if (finish > start) {
            char c = buf[start++];
            if (XmlChars.isNameChar(c))
                return c;
            start--;
        }
        return 0;
    }

    /**
     * gets the next Java character -- might be part of an XML
     * text character represented by a surrogate pair, or be
     * the end of the entity.
     * @return the next Java character
     * @throws IOException for errors
     * @throws SAXException for errors
     */
    public char getc() throws IOException, SAXException {

        if (finish <= start)
            fillbuf();
        if (finish > start) {
            char c = buf[start++];

            // [2] Char ::= #x0009 | #x000A | #x000D
            //            | [#x0020-#xD7FF]
            //            | [#xE000-#xFFFD]
            // plus surrogate _pairs_ representing [#x10000-#x10ffff]
            if (returnedFirstHalf) {
                if (c >= 0xdc00 && c <= 0xdfff) {
                    returnedFirstHalf = false;
                    return c;
                } else
                    fatal("P-070", new Object[]{Integer.toHexString(c)});
            }
            if ((c >= 0x0020 && c <= 0xD7FF)
                    || c == 0x0009
                    // no surrogates!
                    || (c >= 0xE000 && c <= 0xFFFD))
                return c;

            //
            // CRLF and CR are both line ends; map both to LF, and
            // keep line count correct.
            //
            else if (c == '\r' && !isInternal()) {
                maybeInCRLF = true;
                c = getc();
                if (c != '\n')
                    ungetc();
                maybeInCRLF = false;

                lineNumber++;
                return '\n';

            } else if (c == '\n' || c == '\r') { // LF, or 2nd char in CRLF
                if (!isInternal() && !maybeInCRLF)
                    lineNumber++;
                return c;
            }

            // surrogates...
            if (c >= 0xd800 && c < 0xdc00) {
                returnedFirstHalf = true;
                return c;
            }

            fatal("P-071", new Object[]{Integer.toHexString(c)});
        }
        throw new EndOfInputException();
    }


    /**
     * lookahead one character
     * @param c character to lookahead
     * @return true if found
     * @throws IOException for errors
     * @throws SAXException for errors
     */
    public boolean peekc(char c) throws IOException, SAXException {

        if (finish <= start)
            fillbuf();
        if (finish > start) {
            if (buf[start] == c) {
                start++;
                return true;
            } else
                return false;
        }
        return false;
    }


    /**
     * two character pushback is guaranteed
     */
    public void ungetc() {

        if (start == 0)
            throw new InternalError("ungetc");
        start--;

        if (buf[start] == '\n' || buf[start] == '\r') {
            if (!isInternal())
                lineNumber--;
        } else if (returnedFirstHalf)
            returnedFirstHalf = false;
    }


    /**
     * optional grammatical whitespace (discarded)
     * @throws IOException for errors
     * @throws SAXException for errors
     * @return true if whitespace was found
     */
    public boolean maybeWhitespace()
            throws IOException, SAXException {

        char c;
        boolean isSpace = false;
        boolean sawCR = false;

        // [3] S ::= #20 | #09 | #0D | #0A
        for (; ;) {
            if (finish <= start)
                fillbuf();
            if (finish <= start)
                return isSpace;

            c = buf[start++];
            if (c == 0x20 || c == 0x09 || c == '\n' || c == '\r') {
                isSpace = true;

                //
                // CR, LF are line endings ... CLRF is one, not two!
                //
                if ((c == '\n' || c == '\r') && !isInternal()) {
                    if (!(c == '\n' && sawCR)) {
                        lineNumber++;
                        sawCR = false;
                    }
                    if (c == '\r')
                        sawCR = true;
                }
            } else {
                start--;
                return isSpace;
            }
        }
    }


    /**
     * normal content; whitespace in markup may be handled
     * specially if the parser uses the content model.
     *
     * <P> content terminates with markup delimiter characters,
     * namely ampersand (&amp;amp;) and left angle bracket (&amp;lt;).
     *
     * <P> the document handler's characters() method is called
     * on all the content found
     * @param docHandler the document handler
     * @throws IOException for errors
     * @throws SAXException for errors
     * @return true if any content was found
     * @see #maybeWhitespace
     */
    @SuppressWarnings("fallthrough")
    public boolean parsedContent(DTDEventListener docHandler
                                 /*ElementValidator validator*/)
            throws IOException, SAXException {

        // [14] CharData ::= [^<&]* - ([^<&]* ']]>' [^<&]*)

        int first;        // first char to return
        int last;        // last char to return
        boolean sawContent;    // sent any chars?
        char c;

        // deliver right out of the buffer, until delimiter, EOF,
        // or error, refilling as we go
        for (first = last = start, sawContent = false; ; last++) {

            // buffer empty?
            if (last >= finish) {
                if (last > first) {
//            validator.text ();
                    docHandler.characters(buf, first, last - first);
                    sawContent = true;
                    start = last;
                }
                if (isEOF())    // calls fillbuf
                    return sawContent;
                first = start;
                last = first - 1;    // incremented in loop
                continue;
            }

            c = buf[last];

            //
            // pass most chars through ASAP; this inlines the code of
            // [2] !XmlChars.isChar(c) leaving only characters needing
            // special treatment ... line ends, surrogates, and:
            //    0x0026 == '&'
            //    0x003C == '<'
            //    0x005D == ']'
            // Comparisons ordered for speed on 'typical' text
            //
            if ((c > 0x005D && c <= 0xD7FF)    // a-z and more
                    || (c < 0x0026 && c >= 0x0020)    // space & punct
                    || (c > 0x003C && c < 0x005D)    // A-Z & punct
                    || (c > 0x0026 && c < 0x003C)    // 0-9 & punct
                    || c == 0x0009
                    || (c >= 0xE000 && c <= 0xFFFD)
            )
                continue;

            // terminate on markup delimiters
            if (c == '<' || c == '&')
                break;

            // count lines
            if (c == '\n') {
                if (!isInternal())
                    lineNumber++;
                continue;
            }

            // External entities get CR, CRLF --> LF mapping
            // Internal ones got it already, and we can't repeat
            // else we break char ref handling!!
            if (c == '\r') {
                if (isInternal())
                    continue;

                docHandler.characters(buf, first, last - first);
                docHandler.characters(newline, 0, 1);
                sawContent = true;
                lineNumber++;
                if (finish > (last + 1)) {
                    if (buf[last + 1] == '\n')
                        last++;
                } else {    // CR at end of buffer
// XXX case not yet handled:  CRLF here will look like two lines
                }
                first = start = last + 1;
                continue;
            }

            // ']]>' is a WF error -- must fail if we see it
            if (c == ']') {
                switch (finish - last) {
                // for suspicious end-of-buffer cases, get more data
                // into the buffer to rule out this sequence.
                case 2:
                    if (buf[last + 1] != ']')
                        continue;
                    // FALLTHROUGH

                case 1:
                    if (reader == null || isClosed)
                        continue;
                    if (last == first)
                        throw new InternalError("fillbuf");
                    last--;
                    if (last > first) {
//            validator.text ();
                        docHandler.characters(buf, first, last - first);
                        sawContent = true;
                        start = last;
                    }
                    fillbuf();
                    first = last = start;
                    continue;

                    // otherwise any "]]>" would be buffered, and we can
                    // see right away if that's what we have
                default:
                    if (buf[last + 1] == ']' && buf[last + 2] == '>')
                        fatal("P-072", null);
                    continue;
                }
            }

            // correctly paired surrogates are OK
            if (c >= 0xd800 && c <= 0xdfff) {
                if ((last + 1) >= finish) {
                    if (last > first) {
//            validator.text ();
                        docHandler.characters(buf, first, last - first);
                        sawContent = true;
                        start = last + 1;
                    }
                    if (isEOF()) {    // calls fillbuf
                        fatal("P-081",
                                new Object[]{Integer.toHexString(c)});
                    }
                    first = start;
                    last = first;
                    continue;
                }
                if (checkSurrogatePair(last))
                    last++;
                else {
                    last--;
                    // also terminate on surrogate pair oddities
                    break;
                }
                continue;
            }

            fatal("P-071", new Object[]{Integer.toHexString(c)});
        }
        if (last == first)
            return sawContent;
//    validator.text ();
        docHandler.characters(buf, first, last - first);
        start = last;
        return true;
    }


    /**
     * CDATA -- character data, terminated by {@code "]]>"} and optionally
     * including unescaped markup delimiters (ampersand and left angle
     * bracket).  This should otherwise be exactly like character data,
     * modulo differences in error report details.
     *
     * <P> The document handler's characters() or ignorableWhitespace()
     * methods are invoked on all the character data found
     *
     * @param docHandler               gets callbacks for character data
     * @param ignorableWhitespace      if true, whitespace characters will
     *                                 be reported using docHandler.ignorableWhitespace(); implicitly,
     *                                 non-whitespace characters will cause validation errors
     * @param whitespaceInvalidMessage if true, ignorable whitespace
     *                                 causes a validity error report as well as a callback
     * @return true if CDATA was found, false if not
     * @throws IOException for errors
     * @throws SAXException for errors
     */
    public boolean unparsedContent(DTDEventListener docHandler,
                                   /*ElementValidator validator,*/
                                   boolean ignorableWhitespace,
                                   String whitespaceInvalidMessage)
            throws IOException, SAXException {

        // [18] CDSect ::= CDStart CData CDEnd
        // [19] CDStart ::= '<![CDATA['
        // [20] CData ::= (Char* - (Char* ']]>' Char*))
        // [21] CDEnd ::= ']]>'

        // caller peeked the leading '<' ...
        if (!peek("![CDATA[", null))
            return false;
        docHandler.startCDATA();

        // only a literal ']]>' stops this ...
        int last;

        for (; ;) {        // until ']]>' seen
            boolean done = false;
            char c;

            // don't report ignorable whitespace as "text" for
            // validation purposes.
            boolean white = ignorableWhitespace;

            for (last = start; last < finish; last++) {
                c = buf[last];

                //
                // Reject illegal characters.
                //
                if (!XmlChars.isChar(c)) {
                    white = false;
                    if (c >= 0xd800 && c <= 0xdfff) {
                        if (checkSurrogatePair(last)) {
                            last++;
                            continue;
                        } else {
                            last--;
                            break;
                        }
                    }
                    fatal("P-071", new Object[]
                    {Integer.toHexString(buf[last])});
                }
                if (c == '\n') {
                    if (!isInternal())
                        lineNumber++;
                    continue;
                }
                if (c == '\r') {
                    // As above, we can't repeat CR/CRLF --> LF mapping
                    if (isInternal())
                        continue;

                    if (white) {
                        if (whitespaceInvalidMessage != null && errHandler != null)
                            errHandler.error(new SAXParseException(DTDParser.messages.getMessage(locale,
                                    whitespaceInvalidMessage), null));
                        docHandler.ignorableWhitespace(buf, start,
                                last - start);
                        docHandler.ignorableWhitespace(newline, 0, 1);
                    } else {
//            validator.text ();
                        docHandler.characters(buf, start, last - start);
                        docHandler.characters(newline, 0, 1);
                    }
                    lineNumber++;
                    if (finish > (last + 1)) {
                        if (buf[last + 1] == '\n')
                            last++;
                    } else {    // CR at end of buffer
// XXX case not yet handled ... as above
                    }
                    start = last + 1;
                    continue;
                }
                if (c != ']') {
                    if (c != ' ' && c != '\t')
                        white = false;
                    continue;
                }
                if ((last + 2) < finish) {
                    if (buf[last + 1] == ']' && buf[last + 2] == '>') {
                        done = true;
                        break;
                    }
                    white = false;
                    continue;
                } else {
                    //last--;
                    break;
                }
            }
            if (white) {
                if (whitespaceInvalidMessage != null && errHandler != null)
                    errHandler.error(new SAXParseException(DTDParser.messages.getMessage(locale,
                            whitespaceInvalidMessage), null));
                docHandler.ignorableWhitespace(buf, start, last - start);
            } else {
//        validator.text ();
                docHandler.characters(buf, start, last - start);
            }
            if (done) {
                start = last + 3;
                break;
            }
            start = last;
            if (isEOF())
                fatal("P-073", null);
        }
        docHandler.endCDATA();
        return true;
    }

    // return false to backstep at end of buffer)
    private boolean checkSurrogatePair(int offset)
            throws SAXException {

        if ((offset + 1) >= finish)
            return false;

        char c1 = buf[offset++];
        char c2 = buf[offset];

        if ((c1 >= 0xd800 && c1 < 0xdc00) && (c2 >= 0xdc00 && c2 <= 0xdfff))
            return true;
        fatal("P-074", new Object[]{
            Integer.toHexString(c1 & 0x0ffff),
            Integer.toHexString(c2 & 0x0ffff)
        });
        return false;
    }


    /**
     * whitespace in markup (flagged to app, discardable)
     *
     * <P> the document handler's ignorableWhitespace() method
     * is called on all the whitespace found
     * @param handler gets callbacks for ignorable whitespace
     * @return true if ignorable whitespace was found, false if not
     * @throws IOException for errors
     * @throws SAXException for errors
     */
    @SuppressWarnings("fallthrough")
    public boolean ignorableWhitespace(DTDEventListener handler)
            throws IOException, SAXException {

        char c;
        boolean isSpace = false;
        int first;

        // [3] S ::= #20 | #09 | #0D | #0A
        for (first = start; ;) {
            if (finish <= start) {
                if (isSpace)
                    handler.ignorableWhitespace(buf, first, start - first);
                fillbuf();
                first = start;
            }
            if (finish <= start)
                return isSpace;

            c = buf[start++];
            switch (c) {
            case '\n':
                if (!isInternal())
                    lineNumber++;
// XXX handles Macintosh line endings wrong
                // fallthrough
            case 0x09:
            case 0x20:
                isSpace = true;
                continue;

            case '\r':
                isSpace = true;
                if (!isInternal())
                    lineNumber++;
                handler.ignorableWhitespace(buf, first,
                        (start - 1) - first);
                handler.ignorableWhitespace(newline, 0, 1);
                if (start < finish && buf[start] == '\n')
                    ++start;
                first = start;
                continue;

            default:
                ungetc();
                if (isSpace)
                    handler.ignorableWhitespace(buf, first, start - first);
                return isSpace;
            }
        }
    }

    /**
     * returns false iff 'next' string isn't as provided,
     * else skips that text and returns true.
     *
     * <P> NOTE:  two alternative string representations are
     * both passed in, since one is faster.
     * @param next string to match
     * @param chars string to match
     * @return false iff 'next' string isn't as provided
     * @throws IOException for errors
     * @throws SAXException for errors
     */
    public boolean peek(String next, char[] chars)
            throws IOException, SAXException {

        int len;
        int i;

        if (chars != null)
            len = chars.length;
        else
            len = next.length();

        // buffer should hold the whole thing ... give it a
        // chance for the end-of-buffer case and cope with EOF
        // by letting fillbuf compact and fill
        if (finish <= start || (finish - start) < len)
            fillbuf();

        // can't peek past EOF
        if (finish <= start)
            return false;

        // compare the string; consume iff it matches
        if (chars != null) {
            for (i = 0; i < len && (start + i) < finish; i++) {
                if (buf[start + i] != chars[i])
                    return false;
            }
        } else {
            for (i = 0; i < len && (start + i) < finish; i++) {
                if (buf[start + i] != next.charAt(i))
                    return false;
            }
        }

        // if the first fillbuf didn't get enough data, give
        // fillbuf another chance to read
        if (i < len) {
            if (reader == null || isClosed)
                return false;

            //
            // This diagnostic "knows" that the only way big strings would
            // fail to be peeked is where it's a symbol ... e.g. for an
            // </EndTag> construct.  That knowledge could also be applied
            // to get rid of the symbol length constraint, since having
            // the wrong symbol is a fatal error anyway ...
            //
            if (len > buf.length) {
                fatal("P-077", new Object[]{buf.length});
            }

            fillbuf();
            return peek(next, chars);
        }

        start += len;
        return true;
    }


    //
    // Support for reporting the internal DTD subset, so <!DOCTYPE...>
    // declarations can be recreated.  This is collected as a single
    // string; such subsets are normally small, and many applications
    // don't even care about this.
    //
    /**
     * Starts the process of remembering the text that is read.
     * @throws InternalError if the previous text was not remembered
     */
    public void startRemembering() {

        if (startRemember != 0)
            throw new InternalError();
        startRemember = start;
    }

    /**
     * Returns the text that was remembered since the last call
     * to {@link #startRemembering}.
     * @return the text that was remembered
     */
    public String rememberText() {

        String retval;

        // If the internal subset crossed a buffer boundary, we
        // created a temporary buffer.
        if (rememberedText != null) {
            rememberedText.append(buf, startRemember,
                    start - startRemember);
            retval = rememberedText.toString();
        } else
            retval = new String(buf, startRemember,
                    start - startRemember);

        startRemember = 0;
        rememberedText = null;
        return retval;
    }
    /**
     * Gets the top entity in the stack of entities.
     * @return the top entity in the stack of entities
     */
    private InputEntity getTopEntity() {

        InputEntity current = this;

        // don't report locations within internal entities!

        while (current != null && current.input == null)
            current = current.next;
        return current == null ? this : current;
    }

    /**
     * Returns the public ID of this input source, if known
     * @return the public ID of this input source
     */
    public String getPublicId() {

        InputEntity where = getTopEntity();
        if (where == this)
            return input.getPublicId();
        return where.getPublicId();
    }

    /**
     * Returns the system ID of this input source, if known
     * @return the system ID of this input source
     */
    public String getSystemId() {

        InputEntity where = getTopEntity();
        if (where == this)
            return input.getSystemId();
        return where.getSystemId();
    }

    /**
     * Returns the current line number in this input source
     * @return the current line number
     */
    public int getLineNumber() {

        InputEntity where = getTopEntity();
        if (where == this)
            return lineNumber;
        return where.getLineNumber();
    }

    /**
     * returns -1; maintaining column numbers hurts performance
     * @return -1
     */
    public int getColumnNumber() {

        return -1;        // not maintained (speed)
    }


    //
    // n.b. for non-EOF end-of-buffer cases, reader should return
    // at least a handful of bytes so various lookaheads behave.
    //
    // two character pushback exists except at first; characters
    // represented by surrogate pairs can't be pushed back (they'd
    // only be in character data anyway).
    //
    // DTD exception thrown on char conversion problems; line number
    // will be low, as a rule.
    //
    private void fillbuf() throws IOException, SAXException {

        // don't touched fixed buffers, that'll usually
        // change entity values (and isn't needed anyway)
        // likewise, ignore closed streams
        if (reader == null || isClosed)
            return;

        // if remembering DTD text, copy!
        if (startRemember != 0) {
            if (rememberedText == null)
                rememberedText = new StringBuffer(buf.length);
            rememberedText.append(buf, startRemember,
                    start - startRemember);
        }

        boolean extra = (finish > 0) && (start > 0);
        int len;

        if (extra)        // extra pushback
            start--;
        len = finish - start;

        System.arraycopy(buf, start, buf, 0, len);
        start = 0;
        finish = len;

        try {
            len = buf.length - len;
            len = reader.read(buf, finish, len);
        } catch (UnsupportedEncodingException err) {
            fatal("P-075", new Object[]{err.getMessage()});
        } catch (CharConversionException err) {
            fatal("P-076", new Object[]{err.getMessage()});
        }
        if (len >= 0)
            finish += len;
        else
            close();
        if (extra)        // extra pushback
            start++;

        if (startRemember != 0)
        // assert extra == true
            startRemember = 1;
    }
    /**
     * Closes the input source
     */
    public void close() {

        try {
            if (reader != null && !isClosed)
                reader.close();
            isClosed = true;
        } catch (IOException err) {
            /* NOTHING */
            
        }
    }


    private void fatal(String messageId, Object[] params)
            throws SAXException {

        SAXParseException err = new SAXParseException(DTDParser.messages.getMessage(locale, messageId, params), null);

        // not continuable ... e.g. WF errors
        close();
        if (errHandler != null) {
            errHandler.fatalError(err);
        }
        throw err;
    }
}
