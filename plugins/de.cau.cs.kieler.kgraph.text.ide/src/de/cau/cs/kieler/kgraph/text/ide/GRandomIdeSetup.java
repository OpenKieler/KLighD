/*
 * generated by Xtext 2.12.0
 */
package de.cau.cs.kieler.kgraph.text.ide;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.cau.cs.kieler.kgraph.text.GRandomRuntimeModule;
import de.cau.cs.kieler.kgraph.text.GRandomStandaloneSetup;
import org.eclipse.xtext.util.Modules2;

/**
 * Initialization support for running Xtext languages as language servers.
 */
public class GRandomIdeSetup extends GRandomStandaloneSetup {

	@Override
	public Injector createInjector() {
		return Guice.createInjector(Modules2.mixin(new GRandomRuntimeModule(), new GRandomIdeModule()));
	}
	
}
