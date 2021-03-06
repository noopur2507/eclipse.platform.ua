/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.context;

/*
 * A reference to a context XML file, or more precisely, a set of files, one
 * for each locale.
 */
public class ContextFile {

	private String bundleId;
	private String file;

	/*
	 * Creates a new context file reference.
	 */
	public ContextFile(String bundleId, String file) {
		this.bundleId = bundleId;
		this.file = file;
	}

	/*
	 * Return the id of the bundle containing the file, e.g. "org.eclipse.help"
	 */
	public String getBundleId() {
		return bundleId;
	}

	/*
	 * Returns the bundle-relative path to the file, e.g. "/path/contexts.xml"
	 */
	public String getFile() {
		return file;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ContextFile) {
			if (obj == this) {
				return true;
			}
			return bundleId.equals(((ContextFile)obj).bundleId) && file.equals(((ContextFile)obj).file);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return bundleId.hashCode() + file.hashCode();
	}
}
