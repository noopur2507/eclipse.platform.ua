/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl.html;
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.intro.impl.util.*;
import org.osgi.framework.*;

/**
 * Convenience class for generating HTML elements.
 */
public final class HTMLUtil {
	/**
	 * Creates an HTML opening element of the form <elementName
	 * elementAttributes>
	 * 
	 * @param elementName
	 *            the name of the element to create
	 * @param elementAttributes
	 *            a map of attribute names and values to be inserted into the
	 *            element start tag
	 * @param insertLineBreak
	 *            true to insert a line break after the start tag is closed,
	 *            false otherwise
	 * @return
	 */
	public static StringBuffer createHTMLStartTag(String elementName,
			Map elementAttributes, boolean insertLineBreak) {
		StringBuffer element = new StringBuffer();
		if (elementName != null) {
			// open the start tag
			element.append(openHTMLStartTag(elementName));
			// add the attributes, if there are any
			if (elementAttributes != null && !(elementAttributes.isEmpty()))
				element.append(IIntroHTMLConstants.SPACE).append(
						createAttributeList(elementAttributes));
			// close the start tag
			element.append(closeHTMLTag(insertLineBreak));
		}
		return element;
	}
	/**
	 * Creates an HTML start tag of the form <elementName>
	 * 
	 * @param elementName
	 *            the name of the element to create
	 * @param insertLineBreak
	 *            true to insert a new line after the start tag
	 * @return
	 */
	public static StringBuffer createHTMLStartTag(String elementName,
			boolean insertLineBreak) {
		return createHTMLStartTag(elementName, null, insertLineBreak);
	}
	/**
	 * Creates an HTML start tag of the form <elementName>and inserts a line
	 * break after the start tag
	 * 
	 * @param elementName
	 *            the name of the element to create
	 * @return
	 */
	public static StringBuffer createHTMLStartTag(String elementName) {
		return createHTMLStartTag(elementName, null, true);
	}
	/**
	 * Creates an HTML closing element of the form </elementName>
	 * 
	 * @param elementName
	 *            the name of the closing element to create
	 * @param addNewLine
	 *            true to add a new line at the end
	 * @return
	 */
	public static StringBuffer createHTMLEndTag(String elementName,
			boolean addNewLine) {
		StringBuffer closingElement = new StringBuffer();
		if (elementName != null)
			closingElement.append(IIntroHTMLConstants.LT).append(
					IIntroHTMLConstants.FORWARD_SLASH).append(elementName)
					.append(closeHTMLTag(addNewLine));
		return closingElement;
	}
	/**
	 * Given a map of attribute names and values, this method will create a
	 * StringBuffer of the attributes in the form: attrName="attrValue". These
	 * attributes can appear in the start tag of an HTML element.
	 * 
	 * @param attributes
	 *            the attributes to be converted into a String list
	 * @return
	 */
	public static String createAttributeList(Map attributes) {
		if (attributes == null)
			return null;
		StringBuffer attributeList = new StringBuffer();
		Set attrNames = attributes.keySet();
		for (Iterator it = attrNames.iterator(); it.hasNext();) {
			Object name = it.next();
			Object value = attributes.get(name);
			if ((name instanceof String) && (value instanceof String)) {
				attributeList.append(createAttribute((String) name,
						(String) value));
				if (it.hasNext()) {
					attributeList.append(IIntroHTMLConstants.SPACE);
				}
			}
		}
		return attributeList.toString();
	}
	/**
	 * Creates an HTML attribute of the form attrName="attrValue"
	 * 
	 * @param attrName
	 *            the name of the attribute
	 * @param attrValue
	 *            the value of the attribute
	 * @return
	 */
	public static StringBuffer createAttribute(String attrName, String attrValue) {
		StringBuffer attribute = new StringBuffer();
		if (attrName != null && attrValue != null) {
			attribute.append(attrName).append(IIntroHTMLConstants.EQUALS)
					.append(IIntroHTMLConstants.QUOTE).append(attrValue)
					.append(IIntroHTMLConstants.QUOTE);
		}
		return attribute;
	}
	public static StringBuffer openHTMLStartTag(String elementName) {
		return new StringBuffer().append(IIntroHTMLConstants.LT).append(
				elementName);
	}
	public static StringBuffer closeHTMLTag() {
		return closeHTMLTag(true);
	}
	public static StringBuffer closeHTMLTag(boolean newLine) {
		StringBuffer closing = new StringBuffer()
				.append(IIntroHTMLConstants.GT);
		if (newLine)
			closing.append(IIntroHTMLConstants.NEW_LINE);
		return closing;
	}
	/**
	 * Get the absolute path of the given bundle, in the form
	 * file:/path_to_plugin
	 * 
	 * @param bundle
	 * @return
	 */
	public static String getResolvedBundleLocation(Bundle bundle) {
		try {
			URL bundleLocation = bundle.getEntry(""); //$NON-NLS-1$
			if (bundleLocation == null)
				return null;
			bundleLocation = Platform.asLocalURL(bundleLocation);
			return bundleLocation.toExternalForm();
		} catch (IllegalStateException e) {
			Log.error("Failed to access bundle: " //$NON-NLS-1$
					+ bundle.getSymbolicName(), e);
			return null;
		} catch (IOException e) {
			Log.error("Failed to resolve URL path for bundle: " //$NON-NLS-1$
					+ bundle.getSymbolicName(), e);
			return null;
		}
	}
}