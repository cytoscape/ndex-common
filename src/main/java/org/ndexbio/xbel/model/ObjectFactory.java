/**
 *   Copyright (c) 2013, 2015
 *  	The Regents of the University of California
 *  	The Cytoscape Consortium
 *
 *   Permission to use, copy, modify, and distribute this software for any
 *   purpose with or without fee is hereby granted, provided that the above
 *   copyright notice and this permission notice appear in all copies.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *   WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *   MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *   ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *   WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *   ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *   OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.12.03 at 11:33:45 AM PST 
//


package org.ndexbio.xbel.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.ndexbio.xbel.model package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Name_QNAME = new QName("http://belframework.org/schema/1.0/xbel", "name");
    private final static QName _Evidence_QNAME = new QName("http://belframework.org/schema/1.0/xbel", "evidence");
    private final static QName _Description_QNAME = new QName("http://belframework.org/schema/1.0/xbel", "description");
    private final static QName _Disclaimer_QNAME = new QName("http://belframework.org/schema/1.0/xbel", "disclaimer");
    private final static QName _ContactInfo_QNAME = new QName("http://belframework.org/schema/1.0/xbel", "contactInfo");
    private final static QName _ListValue_QNAME = new QName("http://belframework.org/schema/1.0/xbel", "listValue");
    private final static QName _Author_QNAME = new QName("http://belframework.org/schema/1.0/xbel", "author");
    private final static QName _PatternAnnotation_QNAME = new QName("http://belframework.org/schema/1.0/xbel", "patternAnnotation");
    private final static QName _Version_QNAME = new QName("http://belframework.org/schema/1.0/xbel", "version");
    private final static QName _License_QNAME = new QName("http://belframework.org/schema/1.0/xbel", "license");
    private final static QName _Comment_QNAME = new QName("http://belframework.org/schema/1.0/xbel", "comment");
    private final static QName _Copyright_QNAME = new QName("http://belframework.org/schema/1.0/xbel", "copyright");
    private final static QName _Usage_QNAME = new QName("http://belframework.org/schema/1.0/xbel", "usage");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.ndexbio.xbel.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Citation }
     * 
     */
    public Citation createCitation() {
        return new Citation();
    }

    /**
     * Create an instance of {@link AnnotationDefinitionGroup }
     * 
     */
    public AnnotationDefinitionGroup createAnnotationDefinitionGroup() {
        return new AnnotationDefinitionGroup();
    }

    /**
     * Create an instance of {@link InternalAnnotationDefinition }
     * 
     */
    public InternalAnnotationDefinition createInternalAnnotationDefinition() {
        return new InternalAnnotationDefinition();
    }

    /**
     * Create an instance of {@link ListAnnotation }
     * 
     */
    public ListAnnotation createListAnnotation() {
        return new ListAnnotation();
    }

    /**
     * Create an instance of {@link ExternalAnnotationDefinition }
     * 
     */
    public ExternalAnnotationDefinition createExternalAnnotationDefinition() {
        return new ExternalAnnotationDefinition();
    }

    /**
     * Create an instance of {@link Subject }
     * 
     */
    public Subject createSubject() {
        return new Subject();
    }

    /**
     * Create an instance of {@link Term }
     * 
     */
    public Term createTerm() {
        return new Term();
    }

    /**
     * Create an instance of {@link Parameter }
     * 
     */
    public Parameter createParameter() {
        return new Parameter();
    }

    /**
     * Create an instance of {@link Object }
     * 
     */
    public Object createObject() {
        return new Object();
    }

    /**
     * Create an instance of {@link Statement }
     * 
     */
    public Statement createStatement() {
        return new Statement();
    }

    /**
     * Create an instance of {@link AnnotationGroup }
     * 
     */
    public AnnotationGroup createAnnotationGroup() {
        return new AnnotationGroup();
    }

    /**
     * Create an instance of {@link Annotation }
     * 
     */
    public Annotation createAnnotation() {
        return new Annotation();
    }

    /**
     * Create an instance of {@link Citation.AuthorGroup }
     * 
     */
    public Citation.AuthorGroup createCitationAuthorGroup() {
        return new Citation.AuthorGroup();
    }

    /**
     * Create an instance of {@link LicenseGroup }
     * 
     */
    public LicenseGroup createLicenseGroup() {
        return new LicenseGroup();
    }

    /**
     * Create an instance of {@link org.ndexbio.xbel.model.AuthorGroup }
     * 
     */
    public org.ndexbio.xbel.model.AuthorGroup createAuthorGroup() {
        return new org.ndexbio.xbel.model.AuthorGroup();
    }

    /**
     * Create an instance of {@link Namespace }
     * 
     */
    public Namespace createNamespace() {
        return new Namespace();
    }

    /**
     * Create an instance of {@link Document }
     * 
     */
    public Document createDocument() {
        return new Document();
    }

    /**
     * Create an instance of {@link Header }
     * 
     */
    public Header createHeader() {
        return new Header();
    }

    /**
     * Create an instance of {@link NamespaceGroup }
     * 
     */
    public NamespaceGroup createNamespaceGroup() {
        return new NamespaceGroup();
    }

    /**
     * Create an instance of {@link StatementGroup }
     * 
     */
    public StatementGroup createStatementGroup() {
        return new StatementGroup();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://belframework.org/schema/1.0/xbel", name = "name")
    public JAXBElement<String> createName(String value) {
        return new JAXBElement<>(_Name_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://belframework.org/schema/1.0/xbel", name = "evidence")
    public JAXBElement<String> createEvidence(String value) {
        return new JAXBElement<>(_Evidence_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://belframework.org/schema/1.0/xbel", name = "description")
    public JAXBElement<String> createDescription(String value) {
        return new JAXBElement<>(_Description_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://belframework.org/schema/1.0/xbel", name = "disclaimer")
    public JAXBElement<String> createDisclaimer(String value) {
        return new JAXBElement<>(_Disclaimer_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://belframework.org/schema/1.0/xbel", name = "contactInfo")
    public JAXBElement<String> createContactInfo(String value) {
        return new JAXBElement<>(_ContactInfo_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://belframework.org/schema/1.0/xbel", name = "listValue")
    public JAXBElement<String> createListValue(String value) {
        return new JAXBElement<>(_ListValue_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://belframework.org/schema/1.0/xbel", name = "author")
    public JAXBElement<String> createAuthor(String value) {
        return new JAXBElement<>(_Author_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://belframework.org/schema/1.0/xbel", name = "patternAnnotation")
    public JAXBElement<String> createPatternAnnotation(String value) {
        return new JAXBElement<>(_PatternAnnotation_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://belframework.org/schema/1.0/xbel", name = "version")
    public JAXBElement<String> createVersion(String value) {
        return new JAXBElement<>(_Version_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://belframework.org/schema/1.0/xbel", name = "license")
    public JAXBElement<String> createLicense(String value) {
        return new JAXBElement<>(_License_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://belframework.org/schema/1.0/xbel", name = "comment")
    public JAXBElement<String> createComment(String value) {
        return new JAXBElement<>(_Comment_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://belframework.org/schema/1.0/xbel", name = "copyright")
    public JAXBElement<String> createCopyright(String value) {
        return new JAXBElement<>(_Copyright_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://belframework.org/schema/1.0/xbel", name = "usage")
    public JAXBElement<String> createUsage(String value) {
        return new JAXBElement<>(_Usage_QNAME, String.class, null, value);
    }

}
