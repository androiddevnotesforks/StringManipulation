package osmedile.intellij.stringmanip;

import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import osmedile.intellij.stringmanip.config.PluginPersistentStateComponent;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class RepeatAction extends MyEditorAction {

	public static Object model = null;
	protected int actionIndex = 0;

	public RepeatAction() {
		super(new RepeatActionHandler());
	}

	private static boolean withModel() {
		boolean repeatLastActionWithoutDialog = PluginPersistentStateComponent.getInstance().isRepeatLastActionWithoutDialog();
		AWTEvent trueCurrentEvent = IdeEventQueue.getInstance().getTrueCurrentEvent();
		if (trueCurrentEvent instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) trueCurrentEvent;
			if (mouseEvent.isControlDown() || mouseEvent.isAltDown()) {
				return !repeatLastActionWithoutDialog;
			}
		}
		if (trueCurrentEvent instanceof KeyEvent) {
			KeyEvent keyEvent = (KeyEvent) trueCurrentEvent;
			if (keyEvent.getKeyChar() == '1' && (keyEvent.isControlDown() || keyEvent.isAltDown())) {
				return !repeatLastActionWithoutDialog;
			}
			if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER && (keyEvent.isControlDown() || keyEvent.isAltDown())) {
				return !repeatLastActionWithoutDialog;
			}
		}

		return repeatLastActionWithoutDialog;
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		RepeatService repeatService = RepeatService.getInstance();
		UniversalActionModel anAction = repeatService.getState().get(actionIndex);
		if (anAction != null) {
			e.getPresentation().setEnabled(true);
			e.getPresentation().setText(StringManipulationBundle.message("repeat.text") + " - " + anAction.getTextWithMnemonic());
			String description = anAction.getDescription();
//			if (anAction.getModelData() != null) {
//				if (PluginPersistentStateComponent.getInstance().isRepeatLastActionWithoutDialog()) {
//					description += " - hold Ctrl or Alt to show dialog";
//				}else{
//					description += " - hold Ctrl or Alt to skip dialog";
//				}
//			}
			e.getPresentation().setDescription(description);
		} else {
			e.getPresentation().setText(StringManipulationBundle.message("repeat.last.action.text"));
			e.getPresentation().setDescription((String) null);
			e.getPresentation().setEnabled(false);
		}
	}

	public void setActionIndex(int actionIndex) {
		this.actionIndex = actionIndex;
		RepeatActionHandler newHandler = new RepeatActionHandler();
		newHandler.actionIndex = actionIndex;
		setupHandler(newHandler);
	}

	private static class RepeatActionHandler extends MyEditorWriteActionHandler {

		/**
		 * from 1
		 */
		protected int actionIndex = 1;

		public RepeatActionHandler() {
			super(null);
		}

		@NotNull
		@Override
		protected Pair beforeWriteAction(Editor editor, DataContext dataContext) {
			RepeatService service = RepeatService.getInstance();
			Pair<AnAction, Object> pair = service.getLastAction(actionIndex);
			AnAction anAction = pair.getFirst();
			if (anAction != null) {
				try {
					if (withModel()) {
						RepeatAction.model = pair.getSecond();
					}
					AnActionEvent anActionEvent = new AnActionEvent(null, dataContext, "osmedile.intellij.stringmanip.RepeatAction", new Presentation(), ActionManager.getInstance(), 0);
					anAction.actionPerformed(anActionEvent);
				} finally {
					RepeatAction.model = null;
				}
			}
			return stopExecution();
		}

		@Override
		protected void executeWriteAction(Editor editor, DataContext dataContext, @Nullable Object additionalParameter) {

		}
	}
}
