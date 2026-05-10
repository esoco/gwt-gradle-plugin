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
package de.esoco.gwt.gradle.command;

import de.esoco.gwt.gradle.GwtLibPlugin;
import de.esoco.gwt.gradle.extension.JettyOption;
import de.esoco.gwt.gradle.util.ResourceUtils;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.process.ExecOperations;

import java.io.File;


public class JettyServerCommand extends AbstractCommand {

	public JettyServerCommand(Project project, ExecOperations execOperations,
	                          JettyOption jettyOption,
	                          File jettyConf) {

		super(project, execOperations, "org.eclipse.jetty.runner.Runner");

		configure(project, jettyOption, jettyConf);
	}

	private void configure(Project project, JettyOption jettyOption,
	                       File jettyConf) {

		ConfigurationContainer configs = project.getConfigurations();

		Configuration runtimeConf    =
		    configs.getByName(WarPlugin.PROVIDED_RUNTIME_CONFIGURATION_NAME);
		Configuration jettyClassPath =
		    configs.getByName(GwtLibPlugin.CONF_JETTY);

		configureJavaArgs(jettyOption);

		addClassPath(jettyClassPath.getAsPath());
		addClassPath(runtimeConf.getAsPath());

		if (jettyOption.getLogRequestFile() != null) {
			ResourceUtils.ensureDir(jettyOption.getLogRequestFile()
			                        .getParentFile());
			addArg("--log", jettyOption.getLogRequestFile());
		}

		if (jettyOption.getLogFile() != null) {
			ResourceUtils.ensureDir(jettyOption.getLogFile().getParentFile());
			addArg("--out", jettyOption.getLogFile());
		}

		addArg("--host", jettyOption.getBindAddress());
		addArg("--port", jettyOption.getPort());
		addArg("--stop-port", jettyOption.getStopPort());
		addArg("--stop-key", jettyOption.getStopKey());

		addArg(jettyConf.getAbsolutePath());
	}
}
