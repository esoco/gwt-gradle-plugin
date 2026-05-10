/**
 * This file is part of gwt-gradle-plugin.
 * <p>
 * gwt-gradle-plugin is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * <p>
 * gwt-gradle-plugin is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with gwt-gradle-plugin. If not, see <http://www.gnu.org/licenses/>.
 */
package de.esoco.gwt.gradle.command;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import de.esoco.gwt.gradle.GwtLibPlugin;
import de.esoco.gwt.gradle.extension.JavaOption;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

public abstract class AbstractCommand {

	private final List<String> javaArgs = Lists.newArrayList();

	private final List<String> args = Lists.newArrayList();

	private final List<String> classPaths = Lists.newArrayList();

	private final Project project;

	private final String mainClass;

	private final ExecOperations execOperations;

	private JavaOption javaOptions;

	public AbstractCommand(Project project, ExecOperations execOperations, String mainClass) {

		this.project = project;
		this.execOperations = execOperations;
		this.mainClass = mainClass;

		javaArgs.add("-Dfile.encoding=" + Charset.defaultCharset().name());
	}

	public AbstractCommand addArg(String argName) {

		if (!Strings.isNullOrEmpty(argName)) {
			this.args.add(argName);
		}

		return this;
	}

	public AbstractCommand addArg(String argName, File value) {

		if (value != null) {
			this.args.add(argName);
			this.args.add(value.getAbsolutePath());
		}

		return this;
	}

	public AbstractCommand addArg(String argName, Object value) {

		if (value != null) {
			this.args.add(argName);
			this.args.add(value.toString());
		}

		return this;
	}

	public AbstractCommand addArg(String argName, String value) {

		if (!Strings.isNullOrEmpty(value)) {
			this.args.add(argName);
			this.args.add(value);
		}

		return this;
	}

	public void addArgIf(Boolean condition, String argName) {

		if (Boolean.TRUE.equals(condition)) {
			addArg(argName);
		}
	}

	public void addArgIf(Boolean condition, String ifTrue, String ifFalse) {

		if (condition != null) {
			this.args.add(condition ? ifTrue : ifFalse);
		}
	}

	public AbstractCommand addClassPath(String classPath) {

		this.classPaths.add(classPath);
		return this;
	}

	public AbstractCommand addClassPath(Iterable<File> files) {

		for (File file : files) {
			if (file != null && file.exists()) {
				addClassPath(file.getAbsolutePath());
			}
		}

		return this;
	}

	public AbstractCommand addJavaArgs(String javaArg) {

		this.javaArgs.add(javaArg);
		return this;
	}

	public void configureJavaArgs(JavaOption javaOptions) {

		this.javaOptions = javaOptions;

		if (!Strings.isNullOrEmpty(javaOptions.getMaxPermSize())) {
			addJavaArgs("-XX:MaxPermSize=" + javaOptions.getMaxPermSize());
		}

		if (javaOptions.isDebugJava()) {

			String sb =
				"-agentlib:jdwp=server=y,transport=dt_socket,address=" + javaOptions.getDebugPort() + ",suspend=" + (
					javaOptions.isDebugSuspend() ?
					"y" :
					"n");
			addJavaArgs(sb);
		}

		for (String javaArg : javaOptions.getJavaArgs()) {
			addJavaArgs(javaArg);
		}
	}

	public void execute() {

		ExecResult execResult = execOperations.javaexec(new Action<JavaExecSpec>() {

			@Override
			public void execute(JavaExecSpec spec) {

				FileCollection classpath = project.files(classPaths);

				spec.getMainClass().set(mainClass);
				spec.setMinHeapSize(javaOptions.getMinHeapSize());
				spec.setMaxHeapSize(javaOptions.getMaxHeapSize());

				if (javaOptions.isEnvClasspath()) {
					spec.environment("CLASSPATH", classpath.getAsPath());
				} else {
					spec.setClasspath(classpath);
				}

				spec.jvmArgs(javaArgs);
				spec.args(args);

				if (javaOptions.getExecutable() != null) {
					spec.setExecutable(javaOptions.getExecutable());
				}
			}
		});
		execResult.assertNormalExitValue().rethrowFailure();
	}

	protected Collection<File> getDependencySourceDirs(Project project) {

		ConfigurationContainer configs = project.getConfigurations();
		Configuration compileConf =
			configs.getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);
		DependencySet depSet = compileConf.getAllDependencies();

		List<File> result = Lists.newArrayList();

		for (Dependency dep : depSet) {
			if (dep instanceof ProjectDependency) {
				Project projectDependency =
					project.project(((ProjectDependency) dep).getPath());

				if (projectDependency
					.getPlugins()
					.hasPlugin(GwtLibPlugin.class)) {
					JavaPluginExtension javaExtension = projectDependency
						.getExtensions()
						.getByType(JavaPluginExtension.class);
					SourceSet mainSourceSet = javaExtension
						.getSourceSets()
						.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

					result.addAll(mainSourceSet.getAllSource().getSrcDirs());
				}
			}
		}

		return result;
	}
}
