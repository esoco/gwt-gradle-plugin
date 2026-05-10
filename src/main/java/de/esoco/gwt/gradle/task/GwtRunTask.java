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

import com.google.common.collect.ImmutableMap;

import de.esoco.gwt.gradle.command.JettyServerCommand;
import de.esoco.gwt.gradle.extension.GwtExtension;
import de.esoco.gwt.gradle.util.ResourceUtils;

import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.War;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

import java.io.File;

import java.util.Map;


public class GwtRunTask extends AbstractTask {

	public static final String NAME = "gwtRun";

	private File jettyConf;

	@Inject
	public GwtRunTask(ExecOperations execOperations) {
		super(execOperations);

		setDescription("Run jetty with the GW the GWT modules");

		dependsOn(WarPlugin.WAR_TASK_NAME);
	}

	@TaskAction
	public void exec() throws Exception {

		War warTask = (War) getProject().getTasks().getByName("war");

		jettyConf =
		    new File(getProject().getLayout().getBuildDirectory().getAsFile().get(), "gwt/conf/jetty-run-conf.xml");

		Map<String, String> model =
		    new ImmutableMap.Builder<String, String>().put("__WAR_FILE__",
		                                                   warTask
		                                                   .getArchiveFile()
		                                                   .get()
		                                                   .getAsFile()
		                                                   .getAbsolutePath())
		                                              .build();

		ResourceUtils.copy("/stub.jetty-conf.xml", jettyConf, model);

		GwtExtension extension =
		    getProject().getExtensions().getByType(GwtExtension.class);

		JettyServerCommand command =
		    new JettyServerCommand(getProject(), getExecOperations(), extension.getJetty(),
		                           jettyConf);

		command.execute();
	}
}
