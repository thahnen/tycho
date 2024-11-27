/*******************************************************************************
 * Copyright (c) 2024 Tobias Hahnen and others.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Tobias Hahnen - initial API and implementation
 *******************************************************************************/
package org.eclipse.tycho.test.P2RedirectLoop;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.it.Verifier;
import org.eclipse.tycho.p2maven.transport.RedirectionLoopException;
import org.eclipse.tycho.test.AbstractTychoIntegrationTest;
import org.eclipse.tycho.test.util.HttpServer;
import org.eclipse.tycho.test.util.TargetDefinitionUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

public class P2RedirectLoopTest extends AbstractTychoIntegrationTest {
	private HttpServer server;
	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();
	private Verifier verifier;

	@Before
	public void startServer() throws Exception {
		server = HttpServer.startServer();
		server.addServer("loop", temporaryFolder.getRoot());
		verifier = getVerifier("P2RedirectLoop", false);
	}

	@After
	public void stopServer() throws Exception {
		if (server != null) {
			server.stop();
		}
	}

	@Test
	public void test_redirection_loop_warning() throws Exception {
		var redirectedUrl = server.addRedirect("loop", originalPath -> originalPath);
		configureRepositoryInTargetDefinition(redirectedUrl);
		Assert.assertThrows(RedirectionLoopException.class, () -> verifier.executeGoal("package"));
	}

	private void configureRepositoryInTargetDefinition(String url)
			throws IOException, ParserConfigurationException, SAXException {
		File platformFile = new File(verifier.getBasedir(), "targetplatform.target");
		TargetDefinitionUtil.setRepositoryURLs(platformFile, "loop", url);
	}
}
