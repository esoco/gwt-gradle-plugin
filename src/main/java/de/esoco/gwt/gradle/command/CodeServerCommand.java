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
import de.esoco.gwt.gradle.extension.DevOption;
import de.esoco.gwt.gradle.extension.GwtExtension;
import de.esoco.gwt.gradle.util.ResourceUtils;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.process.ExecOperations;

import java.io.File;

import java.util.Collection;


public class CodeServerCommand extends AbstractCommand {

	public CodeServerCommand(Project project, ExecOperations execOperations,
	                         GwtExtension extension,
	                         Collection<String> modules) {

		super(project, execOperations, "com.google.gwt.dev.codeserver.CodeServer");

		configure(project, extension, modules);
	}

	public void configure(Project project, GwtExtension extension,
	                      Collection<String> modules) {

		DevOption devOption = extension.getDev();

		if (extension.getGwtVersion().startsWith("2.6")) {
			addArg("-launcherDir", devOption.getLauncherDir());
		}

		ConfigurationContainer configs = project.getConfigurations();
		Configuration          sdmConf =
		    configs.getByName(GwtLibPlugin.CONF_GWT_SDK);

		SourceSet      mainSourceSet =
		    project.getExtensions().getByType(JavaPluginExtension.class)
		           .getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		FileCollection sources       =
		    project.files(mainSourceSet.getAllJava().getSrcDirs());

		configureJavaArgs(devOption);

		addClassPath(mainSourceSet.getOutput().getAsPath());
		addClassPath(mainSourceSet.getAllJava().getSrcDirs());
		addClassPath(mainSourceSet.getCompileClasspath().getAsPath());
		addClassPath(sdmConf.getAsPath());

		addSrc(sources);
		addSrc(getDependencySourceDirs(project));

		addArg("-bindAddress", devOption.getBindAddress());
		addArgIf(devOption.getFailOnError(), "-failOnError", "-nofailOnError");
		addArgIf(devOption.getPrecompile(), "-precompile", "-noprecompile");
		addArg("-port", devOption.getPort());
		addArgIf(devOption.getStrict(), "-strict");
		addArgIf(devOption.getEnforceStrictResources(),
		         "-XenforceStrictResources ", "-XnoenforceStrictResources");
		addArg("-workDir", ResourceUtils.ensureDir(devOption.getWorkDir()));
		addArgIf(devOption.getIncremental(), "-incremental", "-noincremental");
		addArg("-sourceLevel", devOption.getSourceLevel());

		if (!extension.getGwtVersion().startsWith("2.6")) {
			addArg("-logLevel", devOption.getLogLevel());
		}

		addArg("-XmethodNameDisplayMode", devOption.getMethodNameDisplayMode());
		addArg("-XjsInteropMode", devOption.getJsInteropMode());
		addArgIf(devOption.getGenerateJsInteropExports(),
		         "-generateJsInteropExports");

		if (devOption.getExtraArgs() != null) {
			for (String arg : devOption.getExtraArgs()) {
				if (arg != null && arg.length() > 0) {
					addArg(arg);
				}
			}
		}

		for (String module : modules) {
			addArg(module);
		}
	}

	private void addSrc(Iterable<File> sources) {

		for (File srcDir : sources) {
			if (srcDir.isDirectory()) {
				addArg("-src", srcDir);
			}
		}
	}
}
