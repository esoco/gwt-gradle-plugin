/**
 * This file is part of gwt-gradle-plugin.
 *
 * gwt-gradle-plugin is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * gwt-gradle-plugin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with gwt-gradle-plugin. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package de.esoco.gwt.gradle.task;

import de.esoco.gwt.gradle.extension.GwtExtension;
import de.esoco.gwt.gradle.extension.JettyOption;

import java.io.OutputStream;
import java.net.Socket;

import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

public class GwtStopTask extends AbstractTask {

	public static final String NAME = "gwtStop";

	@Inject
	public GwtStopTask(ExecOperations execOperations) {
		super(execOperations);
		setDescription("Stop jetty");
	}

	@TaskAction
	public void exec() throws Exception {
		GwtExtension extension = getProject().getExtensions().getByType(GwtExtension.class);
		JettyOption jettyOption = extension.getJetty();

		getLogger().info("Stopping jetty");
		Socket soket = new Socket("localhost", jettyOption.getStopPort());
		soket.setSoLinger(false, 0);
		OutputStream out = soket.getOutputStream();
		out.write((jettyOption.getStopKey() + "\r\nstop\r\n").getBytes());
		out.flush();
		soket.close();
	}

}
