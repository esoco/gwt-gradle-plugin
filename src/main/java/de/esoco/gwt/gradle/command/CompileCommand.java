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
import de.esoco.gwt.gradle.extension.CompilerOption;
import de.esoco.gwt.gradle.extension.GwtExtension;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;

import java.io.File;

import java.util.Collection;


public class CompileCommand extends AbstractCommand {

	public CompileCommand(Project project, CompilerOption compilerOptions,
	                      FileCollection sources, File war,
	                      Collection<String> modules) {

		super(project, "com.google.gwt.dev.Compiler");

		configure(project, compilerOptions, sources, war, modules);
	}

	private void configure(Project project, CompilerOption compilerOptions,
	                       FileCollection sources, File war,
	                       Collection<String> modules) {

		Configuration sdmConf     =
		    project.getConfigurations().getByName(GwtLibPlugin.CONF_GWT_SDK);
		Configuration compileConf =
		    project.getConfigurations()
		           .getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);

		configureJavaArgs(compilerOptions);
		addJavaArgs("-Dgwt.persistentunitcachedir=" + project.getBuildDir() +
		            "/" + GwtExtension.DIRECTORY + "/work/cache");

		for (File sourceDir : sources) {
			addClassPath(sourceDir.getAbsolutePath());
		}

		Collection<File> deps = getDependencySourceDirs(project);

        for (File sourceDir : deps) {
            addClassPath(sourceDir.getAbsolutePath());
        }

        if (compilerOptions.isEnvClasspath()) {
            for (String path : compileConf.getAsPath().split(";")) {
                addClassPath(path);
            }
            for (String path : sdmConf.getAsPath().split(";")) {
                addClassPath(path);
            }
        } else {
            addClassPath(compileConf.getAsPath());
            addClassPath(sdmConf.getAsPath());
        }

		addArg("-war", war);
		addArg("-extra", compilerOptions.getExtra());
		addArg("-workDir", compilerOptions.getWorkDir());
		addArg("-gen", compilerOptions.getGen());
		addArg("-deploy", compilerOptions.getDeploy());

		addArg("-logLevel", compilerOptions.getLogLevel());
		addArg("-localWorkers", compilerOptions.getLocalWorkers());
		addArgIf(compilerOptions.getStrict(), "-strict");
		addArgIf(compilerOptions.getFailOnError(), "-failOnError",
		         "-nofailOnError");
		addArg("-sourceLevel", compilerOptions.getSourceLevel());
		addArgIf(compilerOptions.getDraftCompile(), "-draftCompile",
		         "-nodraftCompile");
		addArg("-optimize", compilerOptions.getOptimize());
		addArg("-style", compilerOptions.getStyle());
		addArgIf(compilerOptions.getCompileReport(), "-compileReport",
		         "-nocompileReport");
		addArgIf(compilerOptions.getIncremental(), "-incremental");
		addArgIf(compilerOptions.getCheckAssertions(), "-checkAssertions",
		         "-nocheckAssertions");
		addArgIf(compilerOptions.getCheckCasts(), "-XcheckCasts",
		         "-XnocheckCasts");
		addArgIf(compilerOptions.getEnforceStrictResources(),
		         "-XenforceStrictResources", "-XnoenforceStrictResources");
		addArgIf(compilerOptions.getClassMetadata(), "-XclassMetadata",
		         "-XnoclassMetadata");

		addArgIf(compilerOptions.getOverlappingSourceWarnings(),
		         "-overlappingSourceWarnings", "-nooverlappingSourceWarnings");
		addArgIf(compilerOptions.getSaveSource(), "-saveSource",
		         "-nosaveSource");
		addArg("-XmethodNameDisplayMode",
		       compilerOptions.getMethodNameDisplayMode());
		addArg("-XjsInteropMode", compilerOptions.getJsInteropMode());

		if (compilerOptions.getGenerateJsInteropExports()) {
			addArg("-generateJsInteropExports");

			if (compilerOptions.getIncludeJsInteropExports() != null) {
				for (String arg : compilerOptions.getIncludeJsInteropExports()) {
					addArg("-includeJsInteropExports", arg);
				}
			}

			if (compilerOptions.getExcludeJsInteropExports() != null) {
				for (String arg : compilerOptions.getExcludeJsInteropExports()) {
					addArg("-excludeJsInteropExports", arg);
				}
			}
		}

		if (compilerOptions.getExtraArgs() != null) {
			for (String arg : compilerOptions.getExtraArgs()) {
				addArg(arg);
			}
		}

		for (String module : modules) {
			addArg(module);
		}
	}
}
