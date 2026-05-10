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
package de.esoco.gwt.gradle.task;

import com.google.common.collect.ImmutableMap;
import de.esoco.gwt.gradle.extension.GwtExtension;
import de.esoco.gwt.gradle.util.ResourceUtils;
import org.gradle.api.Project;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class GwtSetUpTask extends AbstractTask {

	public static final String NAME = "gwtSetUp";

	@Input
	private List<String> modules;

	@Inject
	public GwtSetUpTask(ExecOperations execOperations) {
		super(execOperations);
		setDescription("Set up the GWT project from a skeleton");
	}

	public static boolean isEnable(final Project project,
		final GwtExtension extension) {
		String mainModule = null;
		if (extension.getModule() != null && extension.getModule().size() > 0) {
			mainModule = extension.getModule().get(0);
		}
		if (mainModule != null) {
			String moduleFilePath =
				"src/main/java/" + mainModule.replaceAll("\\.",
					"/") + ".gwt.xml";
			File moduleFile = new File(project.getProjectDir(),
				moduleFilePath);
			return !moduleFile.exists();
		}
		return false;
	}

	public void configure(final GwtExtension extension) {
		ConventionMapping mapping =
			((IConventionAware) this).getConventionMapping();

		mapping.map("modules", new Callable<List<String>>() {
			@Override
			public List<String> call() {
				return extension.getModule();
			}
		});
	}

	@TaskAction
	public void exec() throws Exception {

		GwtExtension extension =
			getProject().getExtensions().getByType(GwtExtension.class);
		File projectDir = getProject().getProjectDir();
		File srcMainJava =
			ResourceUtils.ensureDir(new File(projectDir, "src/main/java"));
		File srcMainWebapp =
			ResourceUtils.ensureDir(new File(projectDir, "src/main/webapp"));

		for (String module : extension.getModule()) {
			String moduleName = module.substring(module.lastIndexOf('.') + 1);
			String packageName = module.substring(0, module.lastIndexOf('.'));
			String packagePath = packageName.replaceAll("\\.", "/");

			File moduleDir =
				ResourceUtils.ensureDir(new File(srcMainJava, packagePath));
			File clientDir =
				ResourceUtils.ensureDir(new File(moduleDir, "client"));

			Map<String, String> model =
				new ImmutableMap.Builder<String, String>()
					.put("__APP_NAME__", moduleName)
					.put("__PKG_NAME__", packageName)
					.build();

			ResourceUtils.copy("/skeleton/gwt.xml.txt",
				new File(moduleDir, moduleName + ".gwt.xml"), model);
			ResourceUtils.copy("/skeleton/entryPoint.java.txt",
				new File(clientDir, moduleName + ".java"), model);
			ResourceUtils.copy("/skeleton/entryPoint.ui.xml.txt",
				new File(clientDir, moduleName + ".ui.xml"), model);
			ResourceUtils.copy("/skeleton/index.html.txt",
				new File(srcMainWebapp, "index.html"), model);
		}
	}

	public List<String> getModules() {
		return modules;
	}
}
