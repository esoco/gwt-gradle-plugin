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

import de.esoco.gwt.gradle.command.CompileCommand;
import de.esoco.gwt.gradle.extension.CompilerOption;
import de.esoco.gwt.gradle.extension.GwtExtension;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

import java.io.File;

import java.util.List;
import java.util.concurrent.Callable;


public class GwtCheckTask extends AbstractTask {

	public static final String NAME = "gwtCheck";

	private List<String>   modules;
	private FileCollection src;
	private File           war;

	@Inject
	public GwtCheckTask(ExecOperations execOperations) {
		super(execOperations);

		setDescription("Check the GWT modules");

		dependsOn(JavaPlugin.COMPILE_JAVA_TASK_NAME,
		          JavaPlugin.PROCESS_RESOURCES_TASK_NAME);
	}

	public void configure(final Project project, final GwtExtension extention) {

		final CompilerOption options = extention.getCompile();

		options.init(getProject());

		JavaPluginExtension javaExtension =
		    project.getExtensions().getByType(JavaPluginExtension.class);
		SourceSet           mainSourceSet =
		    javaExtension.getSourceSets()
		                  .getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		final FileCollection sources        =
		    getProject().files(project.files(mainSourceSet.getOutput()
		                                     .getResourcesDir()))
		                .plus(project.files(mainSourceSet.getOutput()
		                                    .getClassesDirs()))
		                .plus(getProject().files(mainSourceSet.getAllSource()
		                                         .getSrcDirs()));

		ConventionMapping mapping =
		    ((IConventionAware) this).getConventionMapping();

		mapping.map("modules", new Callable<List<String>>() {

		                @Override
		                public List<String> call() {

		                    return extention.getModule();
		                }
		            });
		mapping.map("war", new Callable<File>() {

		                @Override
		                public File call() {

		                    return new File(getProject().getLayout().getBuildDirectory().getAsFile().get(), "out");
		                }
		            });
		mapping.map("src", new Callable<FileCollection>() {

		                @Override
		                public FileCollection call() {

		                    return sources;
		                }
		            });
	}

	@TaskAction
	public void exec() {

		GwtExtension   extension       =
		    getProject().getExtensions().getByType(GwtExtension.class);
		CompilerOption compilerOptions = extension.getCompile();

		CompileCommand command =
		    new CompileCommand(getProject(), getExecOperations(), compilerOptions, getSrc(), null,
		                       getModules());

		command.addArg("-validateOnly");
		command.execute();
	}

	@Input
	public List<String> getModules() {

		return modules;
	}

	@InputFiles
	public FileCollection getSrc() {

		return src;
	}

	@OutputDirectory
	public File getWar() {

		return war;
	}
}
