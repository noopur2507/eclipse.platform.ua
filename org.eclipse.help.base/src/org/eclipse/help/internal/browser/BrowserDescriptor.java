/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.browser;
import org.eclipse.help.browser.*;

public class BrowserDescriptor {
	private String browserID;
	private String browserLabel;
	private IBrowserFactory factory;
	/**
	 * @param id
	 *            ID of a browser as specified in plugin.xml
	 * @param label
	 *            name of the browser
	 * @param factory
	 *            the factory that creates instances of this browser
	 */
	public BrowserDescriptor(String id, String label, IBrowserFactory factory) {
		this.browserID = id;
		this.browserLabel = label;
		this.factory = factory;
	}
	public String getID() {
		return browserID;
	}
	public String getLabel() {
		return browserLabel;
	}
	public IBrowserFactory getFactory() {
		return factory;
	}
	public boolean isExternal() {
		return !BrowserManager.BROWSER_ID_EMBEDDED.equals(getID());
	}
}