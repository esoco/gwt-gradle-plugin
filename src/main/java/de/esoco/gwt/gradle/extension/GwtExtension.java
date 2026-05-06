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
package de.esoco.gwt.gradle.extension;

import java.util.Arrays;
import java.util.List;

import org.gradle.api.Action;

import com.google.common.collect.Lists;

public class GwtExtension {
	
	public static final String NAME = "gwt";
	public static final String DIRECTORY = "gwt";

	private String gwtVersion = "2.8.2";
	private boolean gwtServletLib = false;
	private boolean gwtElementalLib = false;
	private boolean gwtPluginEclipse = true;
	private String jettyVersion = "9.2.14.v20151106";
	/**
	 * Specifies Java source level.
	 */
	private String sourceLevel;

	/**
	 * GWT Module to compile.
	 */
	private final List<String> module = Lists.newArrayList();

	private CompilerOption compile = new CompilerOption();
	private DevOption dev = new DevOption();
	private JettyOption jetty = new JettyOption();

	public String getGwtVersion() {
		return gwtVersion;
	}

	public void setGwtVersion(String gwtVersion) {
		this.gwtVersion = gwtVersion;
	}

	public String getJettyVersion() {
		return jettyVersion;
	}

	public void setJettyVersion(String jettyVersion) {
		this.jettyVersion = jettyVersion;
	}

	public boolean isGwtServletLib() {
		return gwtServletLib;
	}

	public void setGwtServletLib(boolean gwtServletLib) {
		this.gwtServletLib = gwtServletLib;
	}

	public boolean isGwtElementalLib() {
		return gwtElementalLib;
	}

	public void setGwtElementalLib(boolean gwtElementalLib) {
		this.gwtElementalLib = gwtElementalLib;
	}

	public boolean isGwtPluginEclipse() {
		return gwtPluginEclipse;
	}

	public void setGwtPluginEclipse(boolean gwtPluginEclipse) {
		this.gwtPluginEclipse = gwtPluginEclipse;
	}

	public DevOption getDev() {
		return dev;
	}

	public void setDev(DevOption dev) {
		this.dev = dev;
	}

	public GwtExtension dev(Action<DevOption> action) {
		action.execute(dev);
		return this;
	}

	public CompilerOption getCompile() {
		return compile;
	}

	public void setCompile(CompilerOption compile) {
		this.compile = compile;
	}

	public GwtExtension compile(Action<CompilerOption> action) {
		action.execute(compile);
		return this;
	}

	public JettyOption getJetty() {
		return jetty;
	}

	public void setJetty(JettyOption jetty) {
		this.jetty = jetty;
	}

	public GwtExtension jetty(Action<JettyOption> action) {
		action.execute(jetty);
		return this;
	}

	public String getSourceLevel() {
		return sourceLevel;
	}

	public void setSourceLevel(String sourceLevel) {
		this.sourceLevel = sourceLevel;
	}

	public List<String> getModule() {
		return module;
	}

	public void module(String... modules) {
		this.module.addAll(Arrays.asList(modules));
	}
}
