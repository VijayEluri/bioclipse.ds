/* *****************************************************************************
 * Copyright (c) 2010 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/

package net.bioclipse.ds.signatures.prop.calc;

/**
 * 
 * @author ola
 *
 */
public class AtomSignatureHeight3PropertyCalculator extends 
									AtomSignaturePropertyCalculator{

	@Override
	protected int getHeight() {
		return 3;
	}

	public String getPropertyName() {
		return "Atom Signatures height 3";
	}

}
