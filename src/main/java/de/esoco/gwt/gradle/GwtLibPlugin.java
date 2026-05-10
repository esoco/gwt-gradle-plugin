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
package de.esoco.gwt.gradle;

import de.esoco.gwt.gradle.extension.GwtExtension;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.testing.Test;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.plugins.ide.eclipse.model.EclipseProject;


public
class GwtLibPlugin implements Plugin<Project> {
	static public final String CONF_GWT_SDK = "gwtSdk";
	static public final String CONF_JETTY   = "jettyConf";

	static private final String ECLIPSE_NATURE                    = "com.gwtplugins.gwt.eclipse.core.gwtNature";
	static private final String ECLIPSE_GWT_CONTAINER             = "com.gwtplugins.gwt.eclipse.core.GWT_CONTAINER";
	static private final String ECLIPSE_BUILDER_PROJECT_VALIDATOR = "com.gwtplugins.gwt.eclipse.core.gwtProjectValidator";
	static private final String ECLIPSE_BUILDER_WEBAPP_VALIDATOR  = "com.gwtplugins.gdt.eclipse.core.webAppProjectValidator";

	@Override
	public void apply(Project project) {
		project.getPlugins().apply(JavaPlugin.class);
		project.getPlugins().apply(MavenPublishPlugin.class);

		final GwtExtension extension = project.getExtensions()
		                                      .create(GwtExtension.NAME,
		                                              GwtExtension.class);

		ConfigurationContainer configurationContainer = project
		                                                .getConfigurations();

		configurationContainer.create(CONF_GWT_SDK).setVisible(false);
		configurationContainer.create(CONF_JETTY).setVisible(false);

		includeSourcesToJar(project);

		project.afterEvaluate(p -> initDependencies(project, extension));
	}

	private void includeSourcesForTest(Project project) {
		JavaPluginExtension javaExtension = project.getExtensions()
		                                             .getByType(JavaPluginExtension.class);

		SourceSet mainSourset = javaExtension.getSourceSets()
		                                      .getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		SourceSet testSourset = javaExtension.getSourceSets()
		                                      .getByName(SourceSet.TEST_SOURCE_SET_NAME);

		FileCollection testClasspath = project.files(mainSourset.getAllSource()
		                                             .getSrcDirs().toArray())
		                                      .plus(project.files(testSourset
		                                                          .getAllSource()
		                                                          .getSrcDirs()
		                                                          .toArray()))
		                                      .plus(testSourset
		                                            .getRuntimeClasspath());
		testSourset.setRuntimeClasspath(testClasspath);

		Test test = project.getTasks().withType(Test.class).getByName("test");
		test.getSystemProperties()
		    .put("gwt.persistentunitcachedir",
		         project.getLayout().getBuildDirectory().getAsFile().get() + GwtExtension.DIRECTORY + "/test");
	}

	private void includeSourcesToJar(Project project) {
		Jar                  jarTask        = project.getTasks()
		                                             .withType(Jar.class)
		                                             .getByName("jar");
		JavaPluginExtension javaExtension = project.getExtensions()
		                                             .getByType(JavaPluginExtension.class);
		SourceSet            mainSourset    = javaExtension.getSourceSets()
		                                                    .getByName("main");
		jarTask.from(mainSourset.getAllSource());
	}

	private void initDependencies(Project project, GwtExtension extension) {
		String gwtVersion     = extension.getGwtVersion();
		String jettyVersion   = extension.getJettyVersion();
		String sGwtUser       = "com.google.gwt:gwt-user:" + gwtVersion;
		String sGwtCodeserver = "com.google.gwt:gwt-codeserver:" +
		                        gwtVersion;

		DependencyHandler dependencies = project.getDependencies();
		dependencies.add(CONF_GWT_SDK, sGwtCodeserver);
		dependencies.add(CONF_GWT_SDK, sGwtUser);
		dependencies.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME,
		                 sGwtCodeserver);
		dependencies.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, sGwtUser);
		dependencies.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, sGwtUser);

		if (extension.isGwtElementalLib()) {
			dependencies.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME,
			                 "com.google.gwt:gwt-elemental:" + gwtVersion);
		}

		if (extension.isGwtServletLib()) {
			dependencies.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME,
			                 "com.google.gwt:gwt-servlet:" + gwtVersion);
		}

		if (project.getPlugins().hasPlugin(WarPlugin.class)) {
			dependencies.add(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME,
			                 "com.google.gwt:gwt-dev:" + gwtVersion);
		}
		
		dependencies.add(CONF_JETTY,
		                 "org.eclipse.jetty:jetty-runner:" + jettyVersion);

		includeSourcesForTest(project);
		initGwtEclipsePlugin(project);
	}

	private void initGwtEclipsePlugin(final Project project) {
		final GwtExtension gwtExtension = (GwtExtension) project.getExtensions()
		                                                        .getByName(GwtExtension.NAME);

		if (project.getPlugins().hasPlugin("eclipse") &&
		    gwtExtension.isGwtPluginEclipse()) {
			final EclipseModel eclipseModel = project.getExtensions()
			                                         .getByType(EclipseModel.class);

			final EclipseProject eclipseProject = eclipseModel.getProject();

			eclipseProject.natures(ECLIPSE_NATURE);
			eclipseProject.buildCommand(ECLIPSE_BUILDER_PROJECT_VALIDATOR);
			eclipseModel.getClasspath().getContainers()
			            .add(ECLIPSE_GWT_CONTAINER);

			project.getPlugins()
			       .withType(GwtPlugin.class, new Action<GwtPlugin>() {
			                     @Override
			                     public void execute(GwtPlugin warPlugin) {
			                         eclipseProject.buildCommand(ECLIPSE_BUILDER_WEBAPP_VALIDATOR);
			                     }
			                 });
		}
	}
}
