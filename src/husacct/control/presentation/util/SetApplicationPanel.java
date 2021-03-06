package husacct.control.presentation.util;

import husacct.ServiceProvider;
import husacct.common.dto.ApplicationDTO;
import husacct.common.locale.ILocaleService;
import husacct.common.services.IServiceListener;
import husacct.control.IControlService;
import husacct.control.task.MainController;
import husacct.control.task.configuration.ConfigurationManager;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class SetApplicationPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	private JLabel pathLabel, applicationNameLabel, languageSelectLabel, versionLabel;
	private JList<String> pathList;
	public 	JTextField applicationNameText, versionText;
	private JComboBox<String> languageSelect;
	private JButton addButton, removeButton;
	private String[] languages;
	private DefaultListModel<String> pathListModel = new DefaultListModel<String>();
	private JDialog dialogOwner;
	private GridBagConstraints constraint = new GridBagConstraints();
	private MainController mainController;
	private IControlService controlService = ServiceProvider.getInstance().getControlService();
	private ILocaleService localeService = ServiceProvider.getInstance().getLocaleService();
	private String selectedFile =ConfigurationManager.getProperty("LastUsedAddedProjectPathPath");

	public SetApplicationPanel(JDialog dialogOwner, MainController mainController){
		this.setDialogOwner(dialogOwner);
		this.mainController = mainController;
		addComponents();
		setListeners();
		setDefaultValues();
	}

	public void addComponents(){
		this.setLayout(new GridBagLayout());
		this.languages = ServiceProvider.getInstance().getAnalyseService().getAvailableLanguages();

		applicationNameLabel = new JLabel(localeService.getTranslatedString("ApplicationNameLabel"));
		languageSelectLabel = new JLabel(localeService.getTranslatedString("LanguageSelectLabel"));
		versionLabel = new JLabel(localeService.getTranslatedString("VersionLabel"));
		pathLabel = new JLabel(localeService.getTranslatedString("PathLabel"));
		addButton = new JButton(localeService.getTranslatedString("AddButton"));
		removeButton = new JButton(localeService.getTranslatedString("RemoveButton"));

		applicationNameText = new JTextField("myApplication", 20);
		languageSelect = new JComboBox<String>(languages);
		versionText = new JTextField(10);

		pathList = new JList<String>(pathListModel);
		pathList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		pathList.setLayoutOrientation(JList.VERTICAL);
		pathList.setVisibleRowCount(-1);
		JScrollPane listScroller = new JScrollPane(pathList);
		listScroller.setAlignmentX(Component.LEFT_ALIGNMENT);

		removeButton.setEnabled(false);

		add(applicationNameLabel, getConstraint(0, 0, 1, 1));
		add(applicationNameText, getConstraint(1, 0, 2, 1));
		add(languageSelectLabel, getConstraint(0, 1, 1, 1));
		add(languageSelect, getConstraint(1, 1, 2, 1));
		add(versionLabel, getConstraint(0, 2, 1, 1));
		add(versionText, getConstraint(1, 2, 2, 1));
		add(pathLabel, getConstraint(0, 3, 1, 1));
		add(listScroller, getConstraint(0, 4, 2, 3, 200, 150));

		add(addButton, getConstraint(2, 4, 1, 1));
		add(removeButton, getConstraint(2, 5, 1, 1));
	}

	private void setListeners(){
		pathList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(pathList.getSelectedIndex() >= 0){
					removeButton.setEnabled(true);
				} else {
					removeButton.setEnabled(false);
				}
			}
		});

		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showAddFileDialog();
			}
		});

		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pathListModel.remove(pathList.getSelectedIndex());
			}
		});

		localeService.addServiceListener(new IServiceListener() {
			@Override
			public void update() {
				applicationNameLabel.setText(localeService.getTranslatedString("ApplicationNameLabel"));
				languageSelectLabel.setText(localeService.getTranslatedString("LanguageSelectLabel"));
				versionLabel.setText(localeService.getTranslatedString("VersionLabel"));
				pathLabel.setText(localeService.getTranslatedString("PathLabel"));
				addButton.setText(localeService.getTranslatedString("AddButton"));
				removeButton.setText(localeService.getTranslatedString("RemoveButton"));
			}
		});
	}

	private void showAddFileDialog() {
		FileDialog fileChooser = new FileDialog(JFileChooser.DIRECTORIES_ONLY, localeService.getTranslatedString("AddButton"));
		String pathToSelectedFileDir;
		if((selectedFile != null) && (!selectedFile.equals(""))){
			if(selectedFile.contains("\\")) {
			pathToSelectedFileDir = selectedFile.substring(0, selectedFile.lastIndexOf('\\') + 1);
			fileChooser.setCurrentDirectory(new File(pathToSelectedFileDir));
			}
		}
		int returnVal = fileChooser.showDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile().getAbsolutePath();
			pathListModel.add(pathListModel.size(), selectedFile);
			ConfigurationManager.setProperty("LastUsedAddProjectPath", selectedFile);
		}
	}

	private void setDefaultValues(){
		ApplicationDTO applicationData = ServiceProvider.getInstance().getDefineService().getApplicationDetails();
		
		applicationNameText.setText(applicationData.name);
		versionText.setText(applicationData.version);
		
		boolean applicationHasProject = applicationData.projects.size() > 0;
		ArrayList<String> items = new ArrayList<String>();
		if(applicationHasProject){
			for(int i=0; i<languages.length; i++){
				if(applicationData.projects.get(0).programmingLanguage.equals(languages[i])){
					languageSelect.setSelectedIndex(i);
				}
			}
			
			items = applicationData.projects.get(0).paths;
			for (int i=0; i<items.size(); i++) {
				pathListModel.add(i, items.get(i));
			}
		}
	}

	public ApplicationDTO getApplicationData(){
		String name = applicationNameText.getText();
		String language = languages[languageSelect.getSelectedIndex()];
		String version = versionText.getText();
		ArrayList<String> paths = new ArrayList<String>(Arrays.asList(Arrays.copyOf(pathListModel.toArray(), pathListModel.toArray().length, String[].class)));
		
		ApplicationDTO applicationData = mainController.getApplicationController().createApplicationData(name, language, version, paths);
		return applicationData;
	}

	private GridBagConstraints getConstraint(int gridx, int gridy, int gridwidth, int gridheight, int ipadx, int ipady){
		constraint.fill = GridBagConstraints.BOTH;
		constraint.insets = new Insets(3, 3, 3, 3);
		constraint.ipadx = ipadx;
		constraint.ipady = ipady;
		constraint.gridx = gridx;
		constraint.gridy = gridy;
		constraint.gridwidth = gridwidth;
		constraint.gridheight = gridheight;
		return constraint;		
	}

	private GridBagConstraints getConstraint(int gridx, int gridy, int gridwidth, int gridheight){
		return getConstraint(gridx, gridy, gridwidth, gridheight, 0, 0);
	}

	public boolean dataValidated() {
		String applicationName = applicationNameText.getText();

		boolean showError = false;
		String errorMessage = "";

		if(applicationName == null || applicationName.length() < 1){
			errorMessage = localeService.getTranslatedString("FieldEmptyError");
			showError = true;
		}else if (!Regex.matchRegex(Regex.nameWithSpacesRegex, applicationNameText.getText())) {
			errorMessage = localeService.getTranslatedString("MustBeAlphaNumericError");
			showError = true;
		}else if(pathListModel.size() < 1){
			errorMessage = localeService.getTranslatedString("NoPathsAdded");
			showError = true;
		}

		if(showError){
			controlService.showErrorMessage(errorMessage);
			return false;
		}
		return true;
	}

	public JDialog getDialogOwner() {
		return dialogOwner;
	}

	public void setDialogOwner(JDialog dialogOwner) {
		this.dialogOwner = dialogOwner;
	}
}