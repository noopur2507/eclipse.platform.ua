/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.cheatsheet.parser;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheet;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheetParser;
import org.junit.Assert;
import org.junit.Test;

/*
 * Tests the cheat sheets parser on tolerable cheat sheets. This means they're not strictly correct,
 * but the parser will tolerate them.
 */
public class TolerateTest {

	private void parseCheatsheet(String file) {
		Path path = new Path("data/cheatsheet/valid/tolerate/" + file);
		URL url = FileLocator.find(UserAssistanceTestPlugin.getDefault().getBundle(), path, null);
		CheatSheetParser parser = new CheatSheetParser();
		CheatSheet sheet = (CheatSheet)parser.parse(url, UserAssistanceTestPlugin.getPluginId(), CheatSheetParser.SIMPLE_ONLY);
		Assert.assertEquals("Warning not generated: " + url, IStatus.WARNING, parser.getStatus().getSeverity());
		Assert.assertNotNull("Tried parsing a tolerable cheat sheet but parser returned null: " + url, sheet);
	}

	@Test
	public void testItemExtraAttr() {
		parseCheatsheet("ItemElement_ExtraAttr.xml");
	}

	@Test
	public void testIntroExtraElement() {
		parseCheatsheet("IntroElement_ExtraElement.xml");
	}

	@Test
	public void testIntroExtraAttr() {
		parseCheatsheet("IntroElement_ExtraAttr.xml");
	}

	@Test
	public void testDescExtraElement() {
		parseCheatsheet("DescriptionElement_ExtraElements.xml");
	}

	@Test
	public void testConditionalExtraElement() {
		parseCheatsheet("ConditionalSubItem_ExtraElement.xml");
	}

	@Test
	public void testConditionalExtraAttr() {
		parseCheatsheet("ConditionalSubItem_ExtraAttr.xml");
	}

	@Test
	public void testElementExtraElement() {
		parseCheatsheet("CheatSheetElement_ExtraElement.xml");
	}

	@Test
	public void testElementExtraAttr() {
		parseCheatsheet("CheatSheetElement_ExtraAttr.xml");
	}

	@Test
	public void testExtraElement() {
		parseCheatsheet("ActionElement_ExtraElement.xml");
	}

	@Test
	public void testExtraAttr() {
		parseCheatsheet("ActionElement_ExtraAttr.xml");
	}

}
