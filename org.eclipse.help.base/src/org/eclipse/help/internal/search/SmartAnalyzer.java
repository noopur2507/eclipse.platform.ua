/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sopot Cela - Bug 466829
 *******************************************************************************/
package org.eclipse.help.internal.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.AnalyzerWrapper;

/**
 * Smart Analyzer. Chooses underlying implementation based on the field which
 * text is analyzed.
 */
public final class SmartAnalyzer extends AnalyzerWrapper {
	Analyzer pluggedInAnalyzer;
	Analyzer exactAnalyzer;

	/**
	 * Constructor for SmartAnalyzer.
	 */
	public SmartAnalyzer(String locale, Analyzer pluggedInAnalyzer) {
		super(PER_FIELD_REUSE_STRATEGY);
		this.pluggedInAnalyzer = pluggedInAnalyzer;
		this.exactAnalyzer = new DefaultAnalyzer(locale);
	}

	@Override
	public final Analyzer getWrappedAnalyzer(String fieldName) {
		if (fieldName != null && fieldName.startsWith("exact_")) { //$NON-NLS-1$
			return exactAnalyzer;
		}
		return pluggedInAnalyzer;
	}
}
