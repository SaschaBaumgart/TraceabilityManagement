package org.amalthea4public.tracemanagement.generic.handlers.selection;

import java.util.List;

import org.amalthea4public.tracemanagement.generic.helpers.TraceCreationHelper;
import org.amalthea4public.tracemanagement.generic.views.SelectionView;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class ItemSelectionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<Object> selection = TraceCreationHelper.extractSelectedElements(event);

		try {
			SelectionView v = (SelectionView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(SelectionView.ID).getViewSite().getPart();
			
			v.droptoSelection(selection);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
