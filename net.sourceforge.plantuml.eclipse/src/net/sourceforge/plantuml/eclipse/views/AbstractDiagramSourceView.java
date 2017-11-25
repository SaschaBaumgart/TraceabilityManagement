package net.sourceforge.plantuml.eclipse.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import net.sourceforge.plantuml.eclipse.Activator;
import net.sourceforge.plantuml.eclipse.utils.DiagramTextProvider;
import net.sourceforge.plantuml.eclipse.utils.DiagramTextProvider2;
import net.sourceforge.plantuml.eclipse.utils.PlantumlConstants;

public abstract class AbstractDiagramSourceView extends ViewPart {

	private String pinnedToId = null;
	private IWorkbenchPart pinnedTo = null;
	private String initialDiagramSource = null;

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		memento.putString("pinnedTo", (pinnedTo != null && pinnedTo instanceof IEditorPart
				? getEditorInputId(((IEditorPart) pinnedTo).getEditorInput()) : null));
		memento.putString("initialDiagramSource", getDiagramText());
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			pinnedToId = memento.getString("pinnedTo");
			initialDiagramSource = memento.getString("initialDiagramSource");
		}
	}

	public boolean isLinkedToActiveEditor() {
		return true;
	}

	public boolean isLinkingActive() {
		return isLinkedToActiveEditor() && toggleAction == null || toggleAction.isChecked();
	}

	@Override
	public void createPartControl(Composite parent) {
		if (isLinkedToActiveEditor()) {
			registerListeners();
			// without this it deadlocked during startup
			parent.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (pinnedTo != null || initialDiagramSource == null) {
						updateDiagramText(true, pinnedTo, null);
					} else if (initialDiagramSource != null) {
						updateDiagramText(initialDiagramSource, null);
					}
				}
			});
		}
		makeActions();
		contributeToActionBars();
	}

	protected void contributeToActionBars() {
	}

	private IAction toggleAction, pinToAction, spawnAction;

	protected void makeActions() {
		pinToAction = new Action() {
			@Override
			public boolean isEnabled() {
				return isLinkedToActiveEditor();
			}

			public void run() {
				pinnedTo = (isChecked() ? currentPart : null);
				if (pinnedTo != null && pinnedTo instanceof IEditorPart) {
					setToolTipText("Pinned to " + getEditorInputId(((IEditorPart) pinnedTo).getEditorInput()));
				} else {
					updateDiagramText(true, null, null);
					setToolTipText(PlantumlConstants.PIN_TO_BUTTON);
				}
			}
		};
		pinToAction.setToolTipText(PlantumlConstants.PIN_TO_BUTTON);
		pinToAction.setImageDescriptor(ImageDescriptor.createFromFile(PlantumlConstants.class, "/icons/pin.png"));
		pinToAction.setChecked(pinnedTo != null || pinnedToId != null);

		spawnAction = new Action() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				String id = AbstractDiagramSourceView.this.getViewSite().getId();
				try {
					page.showView(id, id + "-" + System.currentTimeMillis(), IWorkbenchPage.VIEW_ACTIVATE);
				} catch (PartInitException e) {
				}
			}
		};
		spawnAction.setToolTipText(PlantumlConstants.SPAWN_BUTTON);
		spawnAction.setImageDescriptor(ImageDescriptor.createFromFile(PlantumlConstants.class, "/icons/spawn.png"));

		// action to start or stop the generation of the actual diagram
		toggleAction = new Action() {
			public void run() {
				if (isChecked()) {
					updateDiagramText(true, null, null);
				}
			}
		};
		toggleAction.setToolTipText(PlantumlConstants.TOGGLE_GENERATION_BUTTON);
		toggleAction.setImageDescriptor(ImageDescriptor.createFromFile(PlantumlConstants.class, "/icons/link.gif"));
		toggleAction.setChecked(true);
	}

	protected void addActions(IContributionManager manager, IAction... actions) {
		if (!manager.isEmpty()) {
			manager.add(new Separator());
		}
		for (IAction action : actions) {
			manager.add(action);
		}
	}

	protected void addViewActions(IContributionManager toolBarManager) {
		addActions(toolBarManager, spawnAction, pinToAction, toggleAction);
	}

	protected String getEditorInputId(IEditorInput editorInput) {
		if (editorInput instanceof IStorageEditorInput) {
			IPath path = null;
			try {
				path = ((IStorageEditorInput) editorInput).getStorage().getFullPath();
			} catch (CoreException e) {
			}
			if (path != null) {
				return path.toString();
			}
		}
		if (editorInput instanceof IPathEditorInput) {
			return ((IPathEditorInput) editorInput).getPath().toString();
		}
		if (editorInput instanceof IURIEditorInput) {
			return ((IURIEditorInput) editorInput).getURI().toString();
		}
		return editorInput.getName();
	}

	protected boolean acceptEditor(IEditorPart editor) {
		if (pinnedTo == null && pinnedToId == null) {
			return true;
		}
		if (pinnedTo != null && pinnedTo == editor) {
			return true;
		}
		if (pinnedToId != null && acceptEditorInput(pinnedToId, editor.getEditorInput())) {
			pinnedTo = editor;
			pinnedToId = null;
			return true;
		}
		return false;
	}

	protected boolean acceptEditorInput(String inputId, IEditorInput editorInput) {
		return pinnedToId.equals(getEditorInputId(editorInput));
	}

	protected void registerListeners() {
		getSite().getPage().addPartListener(partListener);
		getSite().getPage().addPostSelectionListener(diagramTextChangedListener);
	}

	@Override
	public void dispose() {
		if (currentPart != null) {
			currentPart.removePropertyListener(diagramTextChangedListener);
		}
		getSite().getPage().removePartListener(partListener);
		getSite().getPage().removePostSelectionListener(diagramTextChangedListener);
	}

	protected abstract void updateDiagramText(String text, Map<String, Object> markerAttributes);

	public abstract String getDiagramText();

	private IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
			updateDiagramText(part);
		}

		protected void updateDiagramText(IWorkbenchPart part) {
			if (isLinkingActive()) {
				AbstractDiagramSourceView.this.updateDiagramText(false, part, null);
			}
		}

		public void partOpened(IWorkbenchPart part) {
		}

		public void partDeactivated(IWorkbenchPart part) {
		}

		public void partClosed(IWorkbenchPart part) {
		}

		public void partBroughtToTop(IWorkbenchPart part) {
		}
	};

	private class DiagramTextChangedListener implements IPropertyListener, ISelectionListener {

		public void propertyChanged(Object source, int propId) {
			if (currentPart instanceof IEditorPart && currentPart == source && propId == IEditorPart.PROP_DIRTY) {
				if (((IEditorPart) currentPart).isDirty()) {
					diagramChanged(currentPart, null);
				}
			}
		}

		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			diagramChanged(part, selection);
		}

		protected void diagramChanged(IWorkbenchPart workbenchPart, ISelection selection) {
			if (isLinkingActive()) {
				updateDiagramText(true, workbenchPart, selection);
			}
		}
	}

	private DiagramTextChangedListener diagramTextChangedListener = new DiagramTextChangedListener();
	private IWorkbenchPart currentPart;

	protected void updateDiagramText(boolean force, IWorkbenchPart part, ISelection selection) {
		if (selection == null) {
			ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
			if (selectionProvider != null) {
				selection = selectionProvider.getSelection();
			}
		}
		if (updateDiagramText(part, selection)) {
			return;
		}

		updateDiagramText((String) null, (Map<String, Object>) null);
	}

	private boolean updateDiagramText(IWorkbenchPart workbenchPart, ISelection selection) {
		if (workbenchPart != null) {
			DiagramTextProvider[] diagramTextProviders = Activator.getDefault().getDiagramTextProviders();
			Map<String, Object> markerAttributes = new HashMap<String, Object>();
			for (int i = 0; i < diagramTextProviders.length; i++) {
				DiagramTextProvider diagramTextProvider = diagramTextProviders[i];
				if (diagramTextProvider.supportsWorkbenchPart(workbenchPart)
						&& (selection == null || diagramTextProvider.supportsSelection(selection))) {
					String diagramText = null;
					if (diagramTextProvider instanceof DiagramTextProvider2) {
						markerAttributes.clear();
						diagramText = ((DiagramTextProvider2) diagramTextProvider).getDiagramText(workbenchPart,
								selection, markerAttributes);
					} else {
						diagramText = diagramTextProvider.getDiagramText(workbenchPart, selection);
					}
					if (diagramText != null) {
						updateDiagramText(diagramText, markerAttributes);
						return true;
					}
				}
			}
		}
		return false;
	}
}
