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
package org.eclipse.capra.handler.mylyn;

import org.eclipse.capra.core.handlers.AbstractArtifactHandler;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.mylyn.tasks.core.ITask;

/**
 * A handler to allow tracing from and to tasks handled by Mylyn.
 */
public class MylynHandler extends AbstractArtifactHandler {

	@Override
	public boolean canHandleSelection(Object selection) {
		return selection instanceof ITask;
	}

	@Override
	public Object resolveArtifact(EObject artifact) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName(Object selection) {
		if (selection instanceof ITask) {
			ITask task = (ITask) selection;
			return task.getSummary();
		}
		return null;
	}

	@Override
	public String getURI(Object selection) {
		if (selection instanceof ITask) {
			ITask task = (ITask) selection;
			String url = task.getUrl();
			if (url == null || url.length() == 0) {
				String repositoryUrl = task.getRepositoryUrl();
				String taskId = task.getTaskId();
				String taskSummary = task.getSummary();
				return repositoryUrl + "/?taskId=" + taskId + "&summary=" + taskSummary;
			}
		}

		return "";
	}

}
