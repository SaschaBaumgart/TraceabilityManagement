/*******************************************************************************
 * Copyright (c) 2016 Chalmers | University of Gothenburg, rt-labs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 *   Contributors:
 *      Chalmers | University of Gothenburg and rt-labs - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.capra.handler.emf;

import org.eclipse.capra.core.handlers.AbstractArtifactHandler;
import org.eclipse.capra.core.util.UIStringUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * Handler to allow tracing to and from arbitrary model elements handled by EMF.
 */
public class EMFHandler extends AbstractArtifactHandler {

	public boolean canHandleSelection(Object selection) {
		return selection instanceof EObject;
	}

	@Override
	public EObject getEObjectForSelection(Object selection, EObject artifactModel) {
		return EObject.class.cast(selection);
	}

	@Override
	public String getName(Object selection) {
		EObject eObject = EObject.class.cast(selection);
		return UIStringUtil.createUIString(eObject);
	}

	@Override
	public String getURI(Object selection) {
		EObject eObject = EObject.class.cast(selection);
		return EcoreUtil.getURI(eObject).toString();
	}

	@Override
	public Object resolveArtifact(EObject artifact) {
		return artifact;
	}
}
