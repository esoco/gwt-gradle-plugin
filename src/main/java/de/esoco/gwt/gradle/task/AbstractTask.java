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

import de.esoco.gwt.gradle.extension.GwtExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Internal;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

@CacheableTask
public abstract class AbstractTask extends DefaultTask {

    private final ExecOperations execOperations;

    @Inject
    protected AbstractTask(ExecOperations execOperations) {
        this.execOperations = execOperations;
        setGroup(GwtExtension.NAME);
    }

    @Internal
    protected ExecOperations getExecOperations() {
        return execOperations;
    }
}