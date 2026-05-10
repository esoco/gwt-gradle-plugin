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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import de.esoco.gwt.gradle.command.CodeServerCommand;
import de.esoco.gwt.gradle.command.AbstractCommand;
import de.esoco.gwt.gradle.command.JettyServerCommand;
import de.esoco.gwt.gradle.extension.DevOption;
import de.esoco.gwt.gradle.extension.GwtExtension;
import de.esoco.gwt.gradle.util.ResourceUtils;

import org.gradle.api.Project;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.War;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


public class GwtDevTask extends AbstractTask {

	public static final String NAME = "gwtDev";

	private final List<String> modules   = Lists.newArrayList();
	private File               jettyConf;

	@Inject
	public GwtDevTask(ExecOperations execOperations) {
		super(execOperations);

		setDescription("Run DevMode");

		dependsOn(JavaPlugin.COMPILE_JAVA_TASK_NAME,
		          JavaPlugin.PROCESS_RESOURCES_TASK_NAME);
	}

	public void configureCodeServer(final Project project,
	                                final GwtExtension extention) {

		final DevOption options = extention.getDev();

		options.init(project);

		ConventionMapping convention =
		    ((IConventionAware) this).getConventionMapping();

		convention.map("modules", new Callable<List<String>>() {

		                   @Override
		                   public List<String> call() {

		                       return extention.getModule();
		                   }
		               });
	}

	@TaskAction
	public void exec() throws Exception {

		GwtExtension extension =
		    getProject().getExtensions().getByType(GwtExtension.class);
		DevOption    sdmOption = extension.getDev();

		createWarExploded(sdmOption);
		ResourceUtils.ensureDir(sdmOption.getWar());
		ResourceUtils.ensureDir(sdmOption.getWorkDir());
		jettyConf =
		    new File(getProject().getLayout().getBuildDirectory().getAsFile().get(),
		             GwtExtension.DIRECTORY +
		             "/conf/jetty-run-conf.xml");

		Map<String, String> model =
		    new ImmutableMap.Builder<String, String>().put("__WAR_FILE__",
		                                                   sdmOption.getWar()
		                                                   .getAbsolutePath())
		                                              .build();

		ResourceUtils.copy("/stub.jetty-conf.xml", jettyConf, model);

		Future<?> sdmTask = execSdm();

		if (!sdmTask.isDone()) {
			execJetty();
			sdmTask.cancel(true);
		}
	}

	@Input
	public List<String> getModules() {

		return modules;
	}

	private void createWarExploded(DevOption sdmOption) throws IOException {

		War                  warTask        =
		    (War) getProject().getTasks().getByName("war");
		JavaPluginExtension  javaExtension  =
		    getProject().getExtensions().getByType(JavaPluginExtension.class);

		File warDir = sdmOption.getWar();

		ResourceUtils.copyDirectory(warTask.getWebAppDirectory().getAsFile().get(), warDir);

		if (Boolean.TRUE.equals(sdmOption.getNoServer())) {
			File webInfDir =
			    ResourceUtils.ensureDir(new File(warDir, "WEB-INF"));

			ResourceUtils.deleteDirectory(webInfDir);
		} else {
			SourceSet mainSourceSet =
			    javaExtension.getSourceSets().getByName("main");
			File      classesDir    =
			    ResourceUtils.ensureDir(new File(warDir, "WEB-INF/classes"));

			for (File file : mainSourceSet.getResources().getSrcDirs()) {
				ResourceUtils.copyDirectory(file, classesDir);
			}

			for (File f : mainSourceSet.getOutput().getClassesDirs()) {
				ResourceUtils.copyDirectory(f, classesDir);
			}

			for (File file : mainSourceSet.getOutput().getFiles()) {
				if (file.exists() && file.isFile()) {
					ResourceUtils.copy(file,
					                   new File(classesDir, file.getName()));
				}
			}

			File libDir =
			    ResourceUtils.ensureDir(new File(warDir, "WEB-INF/lib"));

			for (File file : mainSourceSet.getRuntimeClasspath()) {
				if (file.exists() && file.isFile()) {
					ResourceUtils.copy(file, new File(libDir, file.getName()));
				}
			}
		}
	}

	private JettyServerCommand execJetty() {

		GwtExtension extension =
		    getProject().getExtensions().getByType(GwtExtension.class);

		JettyServerCommand command =
		    new JettyServerCommand(getProject(), getExecOperations(), extension.getJetty(),
		                           jettyConf);

		command.execute();
		return command;
	}

	private Future<?> execSdm() {

		GwtExtension extension =
		    getProject().getExtensions().getByType(GwtExtension.class);

		DevOption devOption = extension.getDev();

		if (!Strings.isNullOrEmpty(extension.getSourceLevel()) &&
		    Strings.isNullOrEmpty(devOption.getSourceLevel())) {
			devOption.setSourceLevel(extension.getSourceLevel());
		}

		AbstractCommand command =
		    new CodeServerCommand(getProject(), getExecOperations(), extension, getModules());

		FutureTask<?> sdmTask = new FutureTask<>(() -> command.execute(), null);

		new Thread(sdmTask).start();

		return sdmTask;
	}
}
