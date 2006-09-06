/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.servlet;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.IHelpBaseConstants;
import org.eclipse.help.internal.protocols.HelpURLStreamHandler;
import org.eclipse.help.internal.util.URLCoder;
import org.eclipse.help.internal.webapp.data.ServletResources;
import org.eclipse.help.internal.webapp.data.UrlUtil;

/**
 * Performs transfer of data from eclipse to a jsp/servlet
 */
public class EclipseConnector {
	
	private static final String PROTOCOL_HTTP = "http"; //$NON-NLS-1$
	private static final String PATH_TOPIC = "/help/ntopic/"; //$NON-NLS-1$

	private static final String errorPageBegin = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n" //$NON-NLS-1$
			+ "<html><head>\n" //$NON-NLS-1$
			+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" //$NON-NLS-1$
			+ "</head>\n" //$NON-NLS-1$
			+ "<body><p>\n"; //$NON-NLS-1$
	private static final String errorPageEnd = "</p></body></html>"; //$NON-NLS-1$
	private static final IFilter filters[] = new IFilter[]{
			new HighlightFilter(), new FramesetFilter(), new InjectionFilter()};

	/**
	 * Constructor.
	 */
	public EclipseConnector(ServletContext context) {
		//this.context = context;
	}

	public void transfer(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		try {

			String url = getURL(req, false);
			if (url == null)
				return;
			if (url.toLowerCase(Locale.ENGLISH).startsWith("file:/") //$NON-NLS-1$
					|| url.toLowerCase(Locale.ENGLISH).startsWith("jar:file:/")) { //$NON-NLS-1$
				int i = url.indexOf('?');
				if (i != -1)
					url = url.substring(0, i);
				// ensure the file is only accessed from a local installation
				if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_INFOCENTER
						|| !UrlUtil.isLocalRequest(req)) {
					return;
				}
			} else {
				// enable activities matching url
				// HelpBasePlugin.getActivitySupport().enableActivities(url);

				url = "help:" + url; //$NON-NLS-1$
			}

			URLConnection con = openConnection(url, req, resp);
			resp.setContentType(con.getContentType());

			long maxAge = 0;
			try {
				// getExpiration() throws NullPointerException when URL is
				// jar:file:...
				long expiration = con.getExpiration();
				maxAge = (expiration - System.currentTimeMillis()) / 1000;
				if (maxAge < 0)
					maxAge = 0;
			} catch (Exception e) {
			}
			resp.setHeader("Cache-Control", "max-age=" + maxAge); //$NON-NLS-1$ //$NON-NLS-2$

			InputStream is = null;
			try {
				is = con.getInputStream();
			} catch (IOException ioe) {
				// if we're not in an infocenter, check if there's remote content
				if (BaseHelpSystem.getMode() != BaseHelpSystem.MODE_INFOCENTER) {
					is = openRemoteInputStream(req, resp);
				}
				if (is == null) {
					// couldn't find any content, remote or local
					resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
					if (url.toLowerCase(Locale.ENGLISH).endsWith("htm") //$NON-NLS-1$
							|| url.toLowerCase(Locale.ENGLISH).endsWith("html")) { //$NON-NLS-1$
						String error = errorPageBegin
								+ ServletResources.getString("noTopic", req) //$NON-NLS-1$
								+ errorPageEnd;
						is = new ByteArrayInputStream(error.getBytes("UTF8")); //$NON-NLS-1$
					} else {
						return;
					}
				}
			}
			catch (Exception e) {
				// if it's a wrapped exception, unwrap it
				Throwable t = e;
				if (t instanceof UndeclaredThrowableException && t.getCause() != null) {
					t = t.getCause();
				}

				StringBuffer message = new StringBuffer();
				message.append(errorPageBegin);
				message.append("<p>"); //$NON-NLS-1$
				message.append(ServletResources.getString(
						"contentProducerException", //$NON-NLS-1$
						req));
				message.append("</p>"); //$NON-NLS-1$
				message.append("<pre>"); //$NON-NLS-1$
				Writer writer = new StringWriter();
				t.printStackTrace(new PrintWriter(writer));
				message.append(writer.toString());
				message.append("</pre>"); //$NON-NLS-1$
				message.append(errorPageEnd);
				
				is = new ByteArrayInputStream(message.toString().getBytes("UTF8")); //$NON-NLS-1$
			}

			OutputStream out = resp.getOutputStream();
			for (int i = 0; i < filters.length; i++) {
				out = filters[i].filter(req, out);
			}

			transferContent(is, out);
			out.flush();
			is.close();

		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	/**
	 * Write the body to the response
	 */
	private void transferContent(InputStream inputStream, OutputStream out)
			throws IOException {
		try {
			// Prepare the input stream for reading
			BufferedInputStream dataStream = new BufferedInputStream(
					inputStream);

			// Create a fixed sized buffer for reading.
			// We could create one with the size of availabe data...
			byte[] buffer = new byte[4096];
			int len = 0;
			while (true) {
				len = dataStream.read(buffer); // Read file into the byte array
				if (len == -1)
					break;
				out.write(buffer, 0, len);
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	/**
	 * Gets content from the named url (this could be and eclipse defined url)
	 */
	private URLConnection openConnection(String url,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		//System.out.println("help content for: " + url);

		URLConnection con = null;
		if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_INFOCENTER) {
			// it is an infocentre, add client locale to url
			String locale = UrlUtil.getLocale(request, response);
			if (url.indexOf('?') >= 0) {
				url = url + "&lang=" + locale; //$NON-NLS-1$
			} else {
				url = url + "?lang=" + locale; //$NON-NLS-1$
			}
		}
		// URL helpURL = new URL(url);
		URL helpURL;
		if (url.startsWith("help:")) { //$NON-NLS-1$
			helpURL = new URL("help", //$NON-NLS-1$
					null, -1, url.substring("help:".length()), //$NON-NLS-1$
					HelpURLStreamHandler.getDefault());
		} else {
			if (url.startsWith("jar:")) { //$NON-NLS-1$
				// fix for bug 83929
				int excl = url.indexOf("!/"); //$NON-NLS-1$
				String jar = url.substring(0, excl);
				String path = url.length() > excl + 2 ? url.substring(excl + 2)
						: ""; //$NON-NLS-1$
				url = jar.replaceAll("!", "%21") + "!/" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ path.replaceAll("!", "%21"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			helpURL = new URL(url);
		}
		String protocol = helpURL.getProtocol();
		if (!("help".equals(protocol) //$NON-NLS-1$
				|| "file".equals(protocol) //$NON-NLS-1$
		|| "jar".equals(protocol))) { //$NON-NLS-1$
			throw new IOException();
		}
		con = helpURL.openConnection();

		con.setAllowUserInteraction(false);
		con.setDoInput(true);
		con.connect();
		return con;
	}

	/*
	 * Opens a connection to the document on the remote help server, if one
	 * was specified. If the document doesn't exist on the remote server,
	 * returns null;
	 */
	private InputStream openRemoteInputStream(HttpServletRequest req, HttpServletResponse resp) {		
		Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();
		String host = prefs.getString(IHelpBaseConstants.P_KEY_REMOTE_HELP_SERVER_HOST);
		if (host != null && host.length() > 0) {
			int port = prefs.getInt(IHelpBaseConstants.P_KEY_REMOTE_HELP_SERVER_PORT);
			try {
				URL url = new URL(PROTOCOL_HTTP, host, port == 0 ? -1 : port, PATH_TOPIC + getURL(req, true));
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				if (connection.getResponseCode() != HttpServletResponse.SC_NOT_FOUND) {
					return connection.getInputStream();
				}
			}
			catch (IOException e) {
				String msg = "I/O error while trying to contact the remote help server"; //$NON-NLS-1$
				HelpBasePlugin.logError(msg, e);
			}
		}
		return null;
	}
	
	/**
	 * Extracts the url from a request
	 */
	private String getURL(HttpServletRequest req, boolean encode) {
		String query = ""; //$NON-NLS-1$
		boolean firstParam = true;
		for (Enumeration params = req.getParameterNames(); params
				.hasMoreElements();) {
			String param = (String) params.nextElement();
			String[] values = req.getParameterValues(param);
			if (values == null)
				continue;
			for (int i = 0; i < values.length; i++) {
				String value = encode ? URLCoder.encode(values[i]) : values[i];
				if (firstParam) {
					query += "?" + param + "=" + value; //$NON-NLS-1$ //$NON-NLS-2$
					firstParam = false;
				} else
					query += "&" + param + "=" + value; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		// the request contains the eclipse url help: or search:
		String url = req.getPathInfo() + query;
		if (url.startsWith("/")) //$NON-NLS-1$
			url = url.substring(1);
		return url;
	}

}
