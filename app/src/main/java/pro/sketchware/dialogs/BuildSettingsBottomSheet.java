package pro.sketchware.dialogs;

import static mod.hey.studios.build.BuildSettings.SETTING_ANDROID_JAR_PATH;
import static mod.hey.studios.build.BuildSettings.SETTING_CLASSPATH;
import static mod.hey.studios.build.BuildSettings.SETTING_CUSTOM_KEY_ALIAS;
import static mod.hey.studios.build.BuildSettings.SETTING_CUSTOM_KEY_PASSWORD;
import static mod.hey.studios.build.BuildSettings.SETTING_CUSTOM_KEYSTORE_PASSWORD;
import static mod.hey.studios.build.BuildSettings.SETTING_CUSTOM_KEYSTORE_PATH;
import static mod.hey.studios.build.BuildSettings.SETTING_DEXER;
import static mod.hey.studios.build.BuildSettings.SETTING_DEXER_D8;
import static mod.hey.studios.build.BuildSettings.SETTING_DEXER_DX;
import static mod.hey.studios.build.BuildSettings.SETTING_ENABLE_LOGCAT;
import static mod.hey.studios.build.BuildSettings.SETTING_FORCE_SINGLE_FILE_COMPILE;
import static mod.hey.studios.build.BuildSettings.SETTING_JAVA_VERSION;
import static mod.hey.studios.build.BuildSettings.SETTING_JAVA_VERSION_10;
import static mod.hey.studios.build.BuildSettings.SETTING_JAVA_VERSION_11;
import static mod.hey.studios.build.BuildSettings.SETTING_JAVA_VERSION_1_7;
import static mod.hey.studios.build.BuildSettings.SETTING_JAVA_VERSION_1_8;
import static mod.hey.studios.build.BuildSettings.SETTING_JAVA_VERSION_1_9;
import static mod.hey.studios.build.BuildSettings.SETTING_NO_HTTP_LEGACY;
import static mod.hey.studios.build.BuildSettings.SETTING_NO_WARNINGS;
import static mod.hey.studios.build.BuildSettings.SETTING_SIGNING_ALGORITHM;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import dev.pranav.filepicker.FilePickerCallback;
import dev.pranav.filepicker.FilePickerDialogFragment;
import dev.pranav.filepicker.FilePickerOptions;
import dev.pranav.filepicker.SelectionMode;
import mod.hey.studios.build.BuildSettings;
import pro.sketchware.databinding.ProjectConfigLayoutBinding;
import pro.sketchware.utility.SketchwareUtil;

public class BuildSettingsBottomSheet extends BottomSheetDialogFragment {
    public static final String TAG = BuildSettingsBottomSheet.class.getSimpleName();

    private static final int VIEW_ANDROIR_JAR_PATH = 0;
    private static final int VIEW_CLASS_PATH = 1;
    private static final int VIEW_DEXER = 2;
    private static final int VIEW_JAVA_VERSION = 3;
    private static final int VIEW_NO_WARNINGS = 4;
    private static final int VIEW_NO_HTTP_LEGACY = 5;
    private static final int VIEW_ENABLE_LOGCAT = 6;
    private static final int VIEW_FORCE_SINGLE_FILE_COMPILE = 7;
    private static final int VIEW_KESTORE_PATH = 8;
    private static final int VIEW_KESTORE_PASSWORD = 9;
    private static final int VIEW_KEY_ALIAS = 10;
    private static final int VIEW_KEY_PASSWORD = 11;
    private static final int VIEW_SIGNING_ALGORITHM = 12;
    private final View[] views = new View[13];
    private ProjectConfigLayoutBinding binding;
    private BuildSettings projectSettings;

    public static BuildSettingsBottomSheet newInstance(String sc_id) {
        BuildSettingsBottomSheet sheet = new BuildSettingsBottomSheet();
        Bundle arguments = new Bundle();
        arguments.putString("sc_id", sc_id);
        sheet.setArguments(arguments);
        return sheet;
    }

    public static String[] getAvailableJavaVersions() {
        return new String[]{SETTING_JAVA_VERSION_1_7, SETTING_JAVA_VERSION_1_8, SETTING_JAVA_VERSION_1_9, SETTING_JAVA_VERSION_10, SETTING_JAVA_VERSION_11};
    }

    public static void handleJavaVersionChange(String choice) {
        if (!choice.equals(SETTING_JAVA_VERSION_1_7)) {
            SketchwareUtil.toast("Don't forget to enable D8 to be able to compile Java 8+ code");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        projectSettings = new BuildSettings(arguments.getString("sc_id"));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ProjectConfigLayoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTabs();
        initializeViews();
        bindValues();
        bindClicks();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Normal Build Settings"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Keystore / Signing"));
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTabVisibility(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        TabLayout.Tab first = binding.tabLayout.getTabAt(0);
        if (first != null) first.select();
        setTabVisibility(0);
    }

    private void setTabVisibility(int index) {
        binding.normalSettingsContainer.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        binding.keystoreSettingsContainer.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
    }

    private void bindValues() {
        binding.tilAndroidJar.getEditText().setText(projectSettings.getValue(SETTING_ANDROID_JAR_PATH, ""));
        binding.tilClasspath.getEditText().setText(projectSettings.getValue(SETTING_CLASSPATH, ""));
        binding.etKeystorePath.setText(projectSettings.getValue(SETTING_CUSTOM_KEYSTORE_PATH, ""));
        binding.etKeystorePassword.setText(projectSettings.getValue(SETTING_CUSTOM_KEYSTORE_PASSWORD, ""));
        binding.etKeyAlias.setText(projectSettings.getValue(SETTING_CUSTOM_KEY_ALIAS, ""));
        binding.etKeyPassword.setText(projectSettings.getValue(SETTING_CUSTOM_KEY_PASSWORD, ""));
        binding.etSigningAlgorithm.setText(projectSettings.getValue(SETTING_SIGNING_ALGORITHM, "SHA256withRSA"));

        setRadioGroupOptions(binding.rgDexer, new String[]{SETTING_DEXER_DX, SETTING_DEXER_D8}, SETTING_DEXER, SETTING_DEXER_DX);
        setRadioGroupOptions(binding.rgJavaVersion, getAvailableJavaVersions(), SETTING_JAVA_VERSION, SETTING_JAVA_VERSION_1_7);

        setCheckboxValue(binding.cbNoWarnings, SETTING_NO_WARNINGS, true);
        setCheckboxValue(binding.cbNoHttpLegacy, SETTING_NO_HTTP_LEGACY, false);
        setCheckboxValue(binding.cbEnableLogcat, SETTING_ENABLE_LOGCAT, true);
        setCheckboxValue(binding.cbForceSingleFileCompile, SETTING_FORCE_SINGLE_FILE_COMPILE, false);
    }

    private void bindClicks() {
        binding.noWarnings.setOnClickListener(v -> binding.cbNoWarnings.performClick());
        binding.noHttpLegacy.setOnClickListener(v -> binding.cbNoHttpLegacy.performClick());
        binding.enableLogcat.setOnClickListener(v -> binding.cbEnableLogcat.performClick());
        binding.forceSingleFileCompile.setOnClickListener(v -> binding.cbForceSingleFileCompile.performClick());

        binding.btnSelectKeystore.setOnClickListener(v -> showKeystorePicker());
        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnSave.setOnClickListener(v -> {
            projectSettings.setValue(SETTING_CUSTOM_KEYSTORE_PATH, safeText(binding.etKeystorePath));
            projectSettings.setValue(SETTING_CUSTOM_KEYSTORE_PASSWORD, safeText(binding.etKeystorePassword));
            projectSettings.setValue(SETTING_CUSTOM_KEY_ALIAS, safeText(binding.etKeyAlias));
            projectSettings.setValue(SETTING_CUSTOM_KEY_PASSWORD, safeText(binding.etKeyPassword));
            projectSettings.setValue(SETTING_SIGNING_ALGORITHM, safeText(binding.etSigningAlgorithm));
            projectSettings.setValues(views);
            dismiss();
        });
    }

    private void initializeViews() {
        binding.tilAndroidJar.getEditText().setTag(SETTING_ANDROID_JAR_PATH);
        binding.tilClasspath.getEditText().setTag(SETTING_CLASSPATH);
        binding.rgDexer.setTag(SETTING_DEXER);
        binding.rgJavaVersion.setTag(SETTING_JAVA_VERSION);
        binding.cbNoWarnings.setTag(SETTING_NO_WARNINGS);
        binding.cbNoHttpLegacy.setTag(SETTING_NO_HTTP_LEGACY);
        binding.cbEnableLogcat.setTag(SETTING_ENABLE_LOGCAT);
        binding.cbForceSingleFileCompile.setTag(SETTING_FORCE_SINGLE_FILE_COMPILE);
        binding.etKeystorePath.setTag(SETTING_CUSTOM_KEYSTORE_PATH);
        binding.etKeystorePassword.setTag(SETTING_CUSTOM_KEYSTORE_PASSWORD);
        binding.etKeyAlias.setTag(SETTING_CUSTOM_KEY_ALIAS);
        binding.etKeyPassword.setTag(SETTING_CUSTOM_KEY_PASSWORD);
        binding.etSigningAlgorithm.setTag(SETTING_SIGNING_ALGORITHM);

        views[VIEW_ANDROIR_JAR_PATH] = binding.tilAndroidJar.getEditText();
        views[VIEW_CLASS_PATH] = binding.tilClasspath.getEditText();
        views[VIEW_DEXER] = binding.rgDexer;
        views[VIEW_ENABLE_LOGCAT] = binding.cbEnableLogcat;
        views[VIEW_FORCE_SINGLE_FILE_COMPILE] = binding.cbForceSingleFileCompile;
        views[VIEW_JAVA_VERSION] = binding.rgJavaVersion;
        views[VIEW_NO_HTTP_LEGACY] = binding.cbNoHttpLegacy;
        views[VIEW_NO_WARNINGS] = binding.cbNoWarnings;
        views[VIEW_KESTORE_PATH] = binding.etKeystorePath;
        views[VIEW_KESTORE_PASSWORD] = binding.etKeystorePassword;
        views[VIEW_KEY_ALIAS] = binding.etKeyAlias;
        views[VIEW_KEY_PASSWORD] = binding.etKeyPassword;
        views[VIEW_SIGNING_ALGORITHM] = binding.etSigningAlgorithm;
    }

    private void setRadioGroupOptions(RadioGroup radioGroup, String[] options, String key, String defaultValue) {
        radioGroup.removeAllViews();
        String value = projectSettings.getValue(key, defaultValue);
        for (String option : options) {
            RadioButton radioButton = new RadioButton(radioGroup.getContext());
            radioButton.setText(option);
            radioButton.setId(View.generateViewId());
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(0, -2, 1f));
            if (value.equals(option)) {
                radioButton.setChecked(true);
            }
            radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isChecked) return;
                if (key.equals(SETTING_JAVA_VERSION)) {
                    handleJavaVersionChange(option);
                }
            });
            radioGroup.addView(radioButton);
        }
    }

    private void setCheckboxValue(CheckBox checkBox, String key, boolean defaultValue) {
        String value = projectSettings.getValue(key, defaultValue ? "true" : "false");
        checkBox.setChecked(value.equals("true"));

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && key.equals(SETTING_NO_HTTP_LEGACY)) {
                SketchwareUtil.toast("Note that this option may cause issues if RequestNetwork component is used");
            }
        });
    }

    private String safeText(com.google.android.material.textfield.TextInputEditText editText) {
        CharSequence text = editText.getText();
        return text == null ? "" : text.toString().trim();
    }

    private void showKeystorePicker() {
        FilePickerOptions options = new FilePickerOptions();
        options.setSelectionMode(SelectionMode.BOTH);
        options.setMultipleSelection(false);
        options.setTitle("Select keystore file");
        options.setExtensions(new String[]{"jks", "keystore", "p12", "pfx"});

        FilePickerCallback callback = new FilePickerCallback() {
            @Override
            public void onFilesSelected(@NotNull List<? extends File> files) {
                if (files.isEmpty()) return;
                binding.etKeystorePath.setText(files.get(0).getAbsolutePath());
            }
        };

        new FilePickerDialogFragment(options, callback).show(getParentFragmentManager(), "keystorePicker");
    }
}
