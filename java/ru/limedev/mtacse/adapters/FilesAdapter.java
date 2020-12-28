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

package ru.limedev.mtacse.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.List;

import ru.limedev.mtacse.R;

import static ru.limedev.mtacse.core.Utilities.getFileCutPath;
import static ru.limedev.mtacse.core.Utilities.getFileExtension;
import static ru.limedev.mtacse.core.Utilities.getFileTypeFromExtension;
import static ru.limedev.mtacse.core.Utilities.getFolderSizeLabel;

public class FilesAdapter extends ArrayAdapter<File> {

    private final Context context;

    public FilesAdapter(Context context, List<File> filesList) {
        super(context, R.layout.files_list_item, filesList);
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        File currentFile = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.files_list_item, parent,
                    false);
        }

        if (currentFile != null) {
            TextView fileName = convertView.findViewById(R.id.fileNameItem);
            TextView fileType = convertView.findViewById(R.id.fileTypeItem);
            TextView filePath = convertView.findViewById(R.id.filePathItem);
            TextView fileSize = convertView.findViewById(R.id.fileSizeItem);
            fileName.setText(currentFile.getName());
            String fileExtension = getFileExtension(currentFile);
            int fileFormat = getFileTypeFromExtension(fileExtension);
            switch (fileFormat) {
                case 0:
                    fileType.setText(R.string.image);
                    break;
                case 1:
                    fileType.setText(R.string.sound);
                    break;
                case 2:
                    fileType.setText(R.string.code);
                    break;
                default:
                    fileType.setText(R.string.unknown_format);
                    break;
            }
            filePath.setText(getFileCutPath(currentFile));
            fileSize.setText(getFolderSizeLabel(context, currentFile));
        }

        return convertView;
    }
}
