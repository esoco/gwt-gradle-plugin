/**
 * This file is part of gwt-gradle-plugin.
 * <p>
 * gwt-gradle-plugin is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * gwt-gradle-plugin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with gwt-gradle-plugin. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package de.esoco.gwt.gradle;

import de.esoco.gwt.gradle.extension.GwtExtension;
import de.esoco.gwt.gradle.task.GwtCheckTask;
import de.esoco.gwt.gradle.task.GwtCodeServerTask;
import de.esoco.gwt.gradle.task.GwtCompileTask;
import de.esoco.gwt.gradle.task.GwtDevTask;
import de.esoco.gwt.gradle.task.GwtRunTask;
import de.esoco.gwt.gradle.task.GwtStopTask;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.War;

public class GwtPlugin implements Plugin<Project> {

	private static final Logger LOGGER = Logging.getLogger(GwtPlugin.class);

	@Override
	public void apply(final Project project) {
		LOGGER.debug("apply gwt plugin");
		project.getPlugins().apply(GwtLibPlugin.class);
		project.getPlugins().apply(WarPlugin.class);

		// createSetUpTask(project);
		createCheckTask(project);
		createCompileTask(project);
		createCodeServerTask(project);
		createDevTask(project);
		createRunTask(project);
		createStopTask(project);
	}

	// private void createSetUpTask(final Project project) {
	// final GwtExtension extension = project.getExtensions().getByType(GwtExtension.class);
	// final Task setUpTask = project.getTasks().create(GwtSetUpTask.NAME, GwtSetUpTask.class);
	// final Task javaTask = project.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME);
	// javaTask.dependsOn(GwtSetUpTask.NAME);
	// project.getTasks().withType(GwtSetUpTask.class, new Action<GwtSetUpTask>() {
	// @Override
	// public void execute(final GwtSetUpTask task) {
	// task.configure(extension);
	// }
	// });
	// project.afterEvaluate(new Action<Project>() {
	// @Override
	// public void execute(final Project project) {
	// setUpTask.setEnabled(GwtSetUpTask.isEnable(project, extension));
	// }
	// });
	// }

	private void createStopTask(Project project) {
		project.getTasks().register(GwtStopTask.NAME, GwtStopTask.class);
	}

	private void createCheckTask(final Project project) {
		project.getTasks().register(GwtCheckTask.NAME, GwtCheckTask.class);
		final GwtExtension extension = project.getExtensions().getByType(GwtExtension.class);
		final Task checkTask = project.getTasks().getByName(JavaBasePlugin.CHECK_TASK_NAME);
		checkTask.dependsOn(GwtCheckTask.NAME);
		project.getTasks().withType(GwtCheckTask.class, new Action<GwtCheckTask>() {
			@Override
			public void execute(final GwtCheckTask task) {
				task.configure(project, extension);
			}
		});
	}

	private void createCompileTask(final Project project) {
		project.getTasks().register(GwtCompileTask.NAME, GwtCompileTask.class);
		final GwtExtension extension = project.getExtensions().getByType(GwtExtension.class);
		final War warTask = project.getTasks().withType(War.class).getByName("war");
		warTask.dependsOn(GwtCompileTask.NAME);
		project.getTasks().withType(GwtCompileTask.class, new Action<GwtCompileTask>() {
			@Override
			public void execute(final GwtCompileTask task) {
				task.configure(project, extension);
				warTask.from(extension.getCompile().getWar());
			}
		});
	}

	private void createRunTask(final Project project) {
		project.getTasks().register(GwtRunTask.NAME, GwtRunTask.class);
	}

	private void createCodeServerTask(final Project project) {
		project.getTasks().register(GwtCodeServerTask.NAME, GwtCodeServerTask.class);
		final GwtExtension extension = project.getExtensions().getByType(GwtExtension.class);
		project.getTasks().withType(GwtCodeServerTask.class, new Action<GwtCodeServerTask>() {
			@Override
			public void execute(final GwtCodeServerTask task) {

				task.configureCodeServer(project, extension);
			}
		});
	}

	private void createDevTask(final Project project) {
		project.getTasks().register(GwtDevTask.NAME, GwtDevTask.class);
		final GwtExtension extension = project.getExtensions().getByType(GwtExtension.class);
		project.getTasks().withType(GwtDevTask.class, new Action<GwtDevTask>() {
			@Override
			public void execute(final GwtDevTask task) {
				task.configureCodeServer(project, extension);
			}
		});
	}
}