package osmedile.intellij.stringmanip;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.Pair;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import osmedile.intellij.stringmanip.styles.custom.CustomActionModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@State(name = "StringManipulationRepeatServiceState", storages = {@Storage("stringManipulationRepeat.xml")})
public class RepeatService implements PersistentStateComponent<RepeatService.State> {
	private static final Logger LOG = Logger.getInstance(RepeatService.class);

	public static class State {
		private Integer version = 1;
		private Integer actionCount = 2;
		private List<UniversalActionModel> list = new ArrayList<UniversalActionModel>();

		public void add(UniversalActionModel lastModel) {
			int existingIndex = -1;
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getActionClassName().equals(lastModel.getActionClassName())) {
					existingIndex = i;
					break;
				}
			}
			if (existingIndex != -1) {
				list.remove(existingIndex);
			}
			list.add(0, lastModel);
			while (list.size() > actionCount) {
				list.remove(list.size() - 1);
			}
		}

		public UniversalActionModel get(int i) {
			i--;
			if (i >= 0 && i < list.size()) {
				return list.get(i);
			}
			return null;
		}

		public Integer getActionCount() {
			return actionCount;
		}

		public void setActionCount(Integer actionCount) {
			this.actionCount = actionCount;
		}
	}

	private State myState = new State();

	@NotNull
	@Override
	public State getState() {
		return myState;
	}

	@Override
	public void loadState(State state) {
		myState = state;
	}


	private Map<Class, AnAction> classToActionCache;

	public static RepeatService getInstance() {
		return ApplicationManager.getApplication().getService(RepeatService.class);
	}

	public RepeatService() {
	}

	@NotNull
	public Pair<AnAction, Object> getLastAction(int actionIndex) {
		Class<?> aClass;
		Object modelAsObject;
		try {
			UniversalActionModel universalActionModel = myState.get(actionIndex);

			modelAsObject = universalActionModel.getModelAsObject();
			aClass = Class.forName(universalActionModel.getActionClassName());

			if (modelAsObject instanceof CustomActionModel customModel) {
				String id = customModel.getId();
				AnAction action = ActionManagerEx.getInstanceEx().getAction(id);
				return Pair.pair(action, modelAsObject);
			}

			AnAction anAction = getActionMap().get(aClass);
			return Pair.pair(anAction, modelAsObject);
		} catch (ClassNotFoundException | IOException | JDOMException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setAction(Class aClass, Object customActionModel) {
		if (aClass != null) {
			RepeatService repeatService = getInstance();
			AnAction anAction = repeatService.getActionMap().get(aClass);

			Presentation templatePresentation = anAction.getTemplatePresentation();
			String description = templatePresentation.getDescription();
			String textWithMnemonic = templatePresentation.getTextWithMnemonic();

			UniversalActionModel lastModel = new UniversalActionModel(aClass.getCanonicalName(), customActionModel, description, textWithMnemonic);

			repeatService.myState.add(lastModel);
		}
	}


	public static void setAction(Class aClass) {
		setAction(aClass, null);
	}

	@NotNull
	protected Map<Class, AnAction> getActionMap() {
		if (this.classToActionCache == null) {
			ActionManagerEx instanceEx = ActionManagerEx.getInstanceEx();
			PluginId pluginId = PluginId.getId("String Manipulation");

			HashMap<Class, AnAction> classToActionMap = new HashMap<Class, AnAction>();
			for (String string_manipulation : instanceEx.getPluginActions(pluginId)) {
				AnAction action = instanceEx.getAction(string_manipulation);
				classToActionMap.put(action.getClass(), action);
			}
			this.classToActionCache = classToActionMap;
		}
		return this.classToActionCache;
	}

}
