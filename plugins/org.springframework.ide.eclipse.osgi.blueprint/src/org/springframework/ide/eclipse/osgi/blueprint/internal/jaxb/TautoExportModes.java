//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.05 at 05:47:27 PM CET 
//

package org.springframework.ide.eclipse.osgi.blueprint.internal.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for TautoExportModes.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="TautoExportModes">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="disabled"/>
 *     &lt;enumeration value="interfaces"/>
 *     &lt;enumeration value="class-hierarchy"/>
 *     &lt;enumeration value="all-classes"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "TautoExportModes")
@XmlEnum
public enum TautoExportModes {

	@XmlEnumValue("disabled") DISABLED("disabled"), @XmlEnumValue("interfaces") INTERFACES(
			"interfaces"), @XmlEnumValue("class-hierarchy") CLASS_HIERARCHY(
					"class-hierarchy"), @XmlEnumValue("all-classes") ALL_CLASSES("all-classes");
	private final String value;

	TautoExportModes(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static TautoExportModes fromValue(String v) {
		for (TautoExportModes c : TautoExportModes.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}