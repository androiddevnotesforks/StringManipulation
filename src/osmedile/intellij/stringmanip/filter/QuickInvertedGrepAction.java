package osmedile.intellij.stringmanip.filter;

import com.intellij.openapi.editor.Editor;
import osmedile.intellij.stringmanip.config.PluginPersistentStateComponent;

public class QuickInvertedGrepAction extends GrepAction {
	@Override
	protected GrepSettings getSettings(Editor editor, String initialValue) {
		if (!GrepAction.INITIAL_VALUE.equals(initialValue)) {
			GrepSettings grepSettings = new GrepSettings();
			grepSettings.setInverted(true);
			grepSettings.setPattern(initialValue);
			grepSettings.setCaseSensitive(true);
			grepSettings.quick = true;
			storeGrepSettings(grepSettings);
			return grepSettings;
		} else {
			return super.getSettings(editor, initialValue);
		}
	}

	@Override
	protected void postProcess(Editor editor, GrepSettings grepSettings) {
		if (grepSettings.quick) {
			editor.getSelectionModel().removeSelection();
		}
	}

	@Override
	protected GrepSettings getSettings(String initialValue) {
		return PluginPersistentStateComponent.getInstance().guessSettings(initialValue, true);
	}
}