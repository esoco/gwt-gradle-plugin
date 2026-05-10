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

import com.google.common.base.Strings;
import de.esoco.gwt.gradle.command.AbstractCommand;
import de.esoco.gwt.gradle.command.CodeServerCommand;
import de.esoco.gwt.gradle.extension.DevOption;
import de.esoco.gwt.gradle.extension.GwtExtension;
import de.esoco.gwt.gradle.util.ResourceUtils;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;

@DisableCachingByDefault(because = "Produces no artifacts")
public class GwtCodeServerTask extends AbstractTask {

    public static final String NAME = "gwtCodeServer";

    @Inject
    public GwtCodeServerTask(ExecOperations execOperations) {
        super(execOperations);

        setDescription("Run CodeServer in SuperDevMode");

        dependsOn(JavaPlugin.COMPILE_JAVA_TASK_NAME,
            JavaPlugin.PROCESS_RESOURCES_TASK_NAME);
    }

    public void configureCodeServer(final Project project,
        final GwtExtension extention) {

        final DevOption options = extention.getDev();

        options.init(project);
    }

    @TaskAction
    public void exec() {

        GwtExtension extension =
            getProject().getExtensions().getByType(GwtExtension.class);

        if (!Strings.isNullOrEmpty(extension.getSourceLevel()) &&
            Strings.isNullOrEmpty(extension.getDev().getSourceLevel())) {
            extension.getDev().setSourceLevel(extension.getSourceLevel());
        }

        ResourceUtils.ensureDir(extension.getDev().getLauncherDir());

        AbstractCommand command =
            new CodeServerCommand(getProject(), getExecOperations(), extension,
                extension.getModule());

        command.execute();
    }
}
