package osmedile.intellij.stringmanip.config;

import com.intellij.openapi.diagnostic.Logger;
import osmedile.intellij.stringmanip.Donate;
import osmedile.intellij.stringmanip.RepeatService;
import osmedile.intellij.stringmanip.utils.ActionUtils;

import javax.swing.*;
import java.util.Objects;

public class SettingsForm {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(SettingsForm.class);

	private JPanel root;
	private JPanel settings;
	private JPanel customActions;
	private JCheckBox doNotAddSelection;
	private JPanel donatePanel;
	private JPanel charSwitchEncoding;
	private JCheckBox repeatLastActionWithoutDialog;
	private JCheckBox normalizeCaseSwitching;
	private JTextField extraRepeatActionsCount;
	private CaseSwitchingSettingsForm caseSwitchingSettingsForm;
	private CustomActionSettingsForm customActionSettingsForm;
	private CharacterSwitchingSettingsForm characterSwitchingSettingsForm;

	public SettingsForm() {
		donatePanel.add(Donate.newDonateButton());
	}

	public JPanel getRoot() {
		return root;
	}

	public void dispose() {
		if (customActionSettingsForm != null) {
			customActionSettingsForm.dispose();
		}
	}

	private void createUIComponents() {
		caseSwitchingSettingsForm = new CaseSwitchingSettingsForm();
		settings = caseSwitchingSettingsForm.getRoot();
		customActionSettingsForm = new CustomActionSettingsForm();
		customActions = customActionSettingsForm.getRoot();
		characterSwitchingSettingsForm = new CharacterSwitchingSettingsForm();
		charSwitchEncoding = characterSwitchingSettingsForm.getRoot();
	}

	public boolean _isModified(PluginPersistentStateComponent data) {
		if (isModified(data)) return true;
		if (customActionSettingsForm.isModified()) return true;
		if (caseSwitchingSettingsForm.isModified(data.getCaseSwitchingSettings())) return true;
		if (characterSwitchingSettingsForm.isModified(data.getCharacterSwitchingSettings())) return true;
		return false;
	}

	public void _getData(PluginPersistentStateComponent data) {
		getData(data);
		customActionSettingsForm.getData();
		caseSwitchingSettingsForm.getData(data.getCaseSwitchingSettings());
		characterSwitchingSettingsForm.getData(data.getCharacterSwitchingSettings());
	}

	public void _setData(PluginPersistentStateComponent data) {
		setData(data);
		customActionSettingsForm.setData();
		caseSwitchingSettingsForm.setData(data.getCaseSwitchingSettings());
		characterSwitchingSettingsForm.setData(data.getCharacterSwitchingSettings());
	}

	public void setData(PluginPersistentStateComponent data) {
		doNotAddSelection.setSelected(data.isDoNotAddSelection());
		repeatLastActionWithoutDialog.setSelected(data.isRepeatLastActionWithoutDialog());
		normalizeCaseSwitching.setSelected(data.isNormalizeCaseSwitching());
		extraRepeatActionsCount.setText(String.valueOf(RepeatService.getInstance().getState().getActionCount()));
	}

	public void getData(PluginPersistentStateComponent data) {
		data.setDoNotAddSelection(doNotAddSelection.isSelected());
		data.setRepeatLastActionWithoutDialog(repeatLastActionWithoutDialog.isSelected());
		data.setNormalizeCaseSwitching(normalizeCaseSwitching.isSelected());
		RepeatService.getInstance().getState().setActionCount(ActionUtils.safeParse(extraRepeatActionsCount.getText(), 1));
	}

	public boolean isModified(PluginPersistentStateComponent data) {
		if (doNotAddSelection.isSelected() != data.isDoNotAddSelection()) return true;
		if (repeatLastActionWithoutDialog.isSelected() != data.isRepeatLastActionWithoutDialog()) return true;
		if (normalizeCaseSwitching.isSelected() != data.isNormalizeCaseSwitching()) return true;
		if (!Objects.equals(extraRepeatActionsCount.getText(), String.valueOf(RepeatService.getInstance().getState().getActionCount()))) return true;
		return false;
	}
}

