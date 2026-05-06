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

import de.esoco.gwt.gradle.extension.GwtExtension;

import org.gradle.api.DefaultTask;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

public abstract class AbstractTask extends DefaultTask {

	private final ExecOperations execOperations;

	@Inject
	public AbstractTask(ExecOperations execOperations) {
		this.execOperations = execOperations;
		setGroup(GwtExtension.NAME);
	}

	protected ExecOperations getExecOperations() {
		return execOperations;
	}
}