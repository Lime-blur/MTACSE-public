/*
 * MIT License
 *
 * Copyright (c) 2020 Tim Meleshko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.limedev.mtacse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import ru.limedev.mtacse.core.editor.lang.LanguageHLSL;
import ru.limedev.mtacse.core.editor.lang.LanguageLua;
import ru.limedev.mtacse.core.editor.lang.LanguageNonProg;
import ru.limedev.mtacse.core.editor.lang.xml.LanguageMetaXML;
import ru.limedev.mtacse.core.editor.lang.xml.LanguageXML;
import ru.limedev.mtacse.core.editor.util.DocumentProvider;
import ru.limedev.mtacse.core.pojo.CustomSpinner;
import ru.limedev.mtacse.core.pojo.TextEditor;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static ru.limedev.mtacse.core.Constants.*;
import static ru.limedev.mtacse.core.UserSettings.saveFile;
import static ru.limedev.mtacse.core.Utilities.*;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class CodeFragment extends Fragment implements View.OnClickListener {

    private static CodeFragment instance = null;
    private TextEditor codeEditText;
    static String openedFilePath = null;

    public CodeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_code, container, false);

        ImageButton undoButton = view.findViewById(R.id.imageButtonUndo);
        ImageButton redoButton = view.findViewById(R.id.imageButtonRedo);
        ImageButton saveButton = view.findViewById(R.id.imageButtonSave);
        ImageButton closeButton = view.findViewById(R.id.imageButtonClose);

        undoButton.setOnClickListener(this);
        redoButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);

        codeEditText = view.findViewById(R.id.editCodeView);
        if (getContext() != null) {
            CustomSpinner syntaxSpinner = view.findViewById(R.id.chooseSyntaxSpinner);
            ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(getContext(), R.array.syntax_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            syntaxSpinner.setAdapter(adapter);
            syntaxSpinner.setPromptId(R.string.choose_syntax);
            syntaxSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View itemSelected, int position, long selectedId) {
                    switch (position) {
                        case 0:
                            loadLocalSyntax(LUA);
                            break;
                        case 1:
                            loadLocalSyntax(XML);
                            break;
                        case 2:
                            loadLocalSyntax(EDF);
                            break;
                        case 3:
                            loadLocalSyntax(FX);
                            break;
                        case 4:
                            loadLocalSyntax(MAP);
                            break;
                        case 5:
                            loadLocalSyntax("");
                            break;
                    }
                }
                public void onNothingSelected(AdapterView<?> parent) { /* Ignored */ }
            });
        }

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
    }

    public static CodeFragment getInstance() {
        return instance;
    }

    private void saveTextFromEditText() {
        if (getActivity() != null && getContext() != null) {
            if (checkAppPermission(getContext())) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE}, 0);
            } else {
                DocumentProvider editDocumentProvider = codeEditText.getText();
                if (editDocumentProvider != null) {
                    File originalFile;
                    String data = editDocumentProvider.toString();
                    if (openedFilePath != null) {
                        originalFile = new File(openedFilePath);
                    } else {
                        originalFile = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOCUMENTS), saveFile);
                    }
                    if (isExternalStorageWritable()) {
                        try {
                            FileOutputStream fos = new FileOutputStream(originalFile);
                            fos.write(data.getBytes(StandardCharsets.UTF_8));
                            fos.close();
                            showToast(getTranslated(R.string.file_saved) + originalFile.getPath(), true);
                        } catch (IOException ex) {
                            showToast(ex.getMessage(), true);
                        }
                    } else {
                        showToast(getTranslated(R.string.unwritable_storage), false);
                    }
                }
            }
        }
    }

    private void cleanArea() {
        codeEditText.setText("");
        openedFilePath = null;
        codeEditText.setLanguage(LanguageNonProg.getInstance());
        showToast(getTranslated(R.string.text_field_cleaned), false);
    }

    private void chooseLanguage(String language) {
        switch (language) {
            case LUA:
                codeEditText.setLanguage(LanguageLua.getInstance());
                break;
            case XML:
                codeEditText.setLanguage(LanguageMetaXML.getInstance());
                break;
            case FX:
                codeEditText.setLanguage(LanguageHLSL.getInstance());
                break;
            case MAP:
            case EDF:
                codeEditText.setLanguage(LanguageXML.getInstance());
                break;
            default:
                codeEditText.setLanguage(LanguageNonProg.getInstance());
                break;
        }
    }

    public void loadText(String path, String mtaExtension) {
        byte[] fileContent = readTextFile(path);
        String text = new String(fileContent, StandardCharsets.UTF_8);
        chooseLanguage(mtaExtension);
        codeEditText.setText(text);
    }

    void findText(Context context, String text) {
        boolean found = codeEditText.findText(text, false, false);
        if (!found) {
            Toast.makeText(context, getFromR(context, R.string.nothing_found), Toast.LENGTH_SHORT).show();
        }
    }

    void cleanAreaWithHint() {
        if (getContext() != null) {
            codeEditText.setText(getFromR(getContext(), R.string.file_code_shows_there));
            openedFilePath = null;
            codeEditText.setLanguage(LanguageNonProg.getInstance());
        }
    }

    private void loadLocalSyntax(String type) {
        if (getActivity() != null && getContext() != null) {
            chooseLanguage(type);
            codeEditText.setText(codeEditText.getText());
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButtonUndo:
                codeEditText.undo();
                break;
            case R.id.imageButtonRedo:
                codeEditText.redo();
                break;
            case R.id.imageButtonSave:
                saveTextFromEditText();
                break;
            case R.id.imageButtonClose:
                cleanArea();
                break;
        }
    }

    private String getTranslated(int resource) {
        if (getContext() != null) {
            return getFromR(getContext(), resource);
        }
        return "";
    }

    private void showToast(String message, boolean longMessage) {
        if (getContext() != null) {
            if (longMessage) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
