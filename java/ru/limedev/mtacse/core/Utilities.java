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

package ru.limedev.mtacse.core;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ru.limedev.mtacse.R;
import ru.limedev.mtacse.core.exceptions.IllegalFileException;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static ru.limedev.mtacse.core.Constants.*;

public class Utilities {

    public static String getFromR(Context context, int stringRes) {
        return context.getResources().getString(stringRes);
    }

    public static String getFromR(Activity activity, int stringRes) {
        return activity.getResources().getString(stringRes);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            View view = activity.getCurrentFocus();
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static int getFileTypeFromExtension(String extension) {
        if (IMAGES_FORMATS.contains(extension)) {
            return 0;
        } else if (SOUND_FORMATS.contains(extension)) {
            return 1;
        } else if (CODE_FORMATS.contains(extension)) {
            return 2;
        } else {
            return -1;
        }
    }

    public static void enableDisableView(View view, boolean enabled) {
        if (view.getId() != R.id.mainProgressBarBg && view.getId() != R.id.showImageBg) {
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int idx = 0; idx < group.getChildCount(); idx++) {
                    enableDisableView(group.getChildAt(idx), enabled);
                }
            }
        }
    }

    public static String getFolderSizeLabel(Context context, File file) {
        long size = getFolderSize(file);
        return readableFileSize(context, size);
    }

    public static String getFileCutPath(File file) {
        String path = file.getAbsolutePath();
        return path.replace(PREFIX_PATH, "");
    }

    public static long getFolderSize(File file) {
        long size = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    size += getFolderSize(child);
                }
            }
        } else {
            size = file.length();
        }
        return size;
    }

    public static String readableFileSize(Context context, long size) {
        final String[] units = context.getResources().getStringArray(R.array.sizes_array);
        if (size <= 0) return 0 + units[0];
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat(FILE_SIZE_PATTERN).format(size / Math.pow(1024, digitGroups))
                + units[digitGroups];
    }

    public static String getFileExtension(File f) {
        int i = f.getName().lastIndexOf('.');
        if (i > 0) {
            return f.getName().substring(i + 1).toLowerCase();
        } else {
            return "";
        }
    }

    public static File changeFileExtension(File f, String newExtension) {
        int i = f.getName().lastIndexOf('.');
        String name = f.getName().substring(0, i);
        return new File(f.getParent(), name + "." + newExtension);
    }

    public static byte[] readTextFile(String path) {
        byte[] read = null;
        try {
            InputStream stream = new FileInputStream(path);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            read = buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return read;
    }

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), CONTENT)) {
            try (Cursor cursor = context.getContentResolver().query(
                    uri, null, null, null, null)
            ) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf(SLASH);
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    public static void deleteDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] filesList = fileOrDirectory.listFiles();
            if (filesList != null) {
                for (File child : filesList) {
                    deleteDirectory(child);
                }
            }
        }
        fileOrDirectory.delete();
    }

    public static boolean customUnzip(Context context, Uri uri, File unpackDir, String zipName) {
        try (InputStream inputStream = Objects.requireNonNull(context.getContentResolver().openInputStream(uri))) {
            String fileName;
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
            ZipEntry zipEntry;
            File dir = new File(unpackDir, zipName);
            String path = unpackDir + SLASH + zipName + SLASH;
            if (dir.exists() && dir.isDirectory()) {
                Toast.makeText(context, getFromR(context, R.string.open_exists_dir) + path, Toast.LENGTH_LONG).show();
                return true;
            }
            if (!dir.mkdirs()) {
                throw new IllegalFileException(getFromR(context, R.string.error_init_directory), MTA_CSE);
            }
            byte[] buffer = new byte[16384];
            int count;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                fileName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    File file = new File(path, fileName);
                    if (!file.mkdirs()) {
                        throw new IllegalFileException(getFromR(context, R.string.error_init_directory), MTA_CSE);
                    }
                    continue;
                }
                FileOutputStream fileOutputStream = new FileOutputStream(path + fileName);
                while ((count = zipInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, count);
                }
                fileOutputStream.close();
                zipInputStream.closeEntry();
            }
            zipInputStream.close();
            Toast.makeText(context, getFromR(context, R.string.unpacked_successfully) + path, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            throw new IllegalFileException(getFromR(context, R.string.error_unpack), MTA_CSE);
        }
        return true;
    }

    public static boolean copyFileToDocuments(Context context, Uri uri, File unpackDir, String zipName) {
        try (InputStream inputStream = Objects.requireNonNull(context.getContentResolver().openInputStream(uri))) {
            String path = unpackDir + SLASH + zipName + SLASH;
            File dir = new File(unpackDir, zipName);
            if (dir.exists() && dir.isDirectory()) {
                Toast.makeText(context, getFromR(context, R.string.open_exists_dir) + path, Toast.LENGTH_LONG).show();
                return true;
            }
            if (!dir.mkdirs()) {
                throw new IllegalFileException(getFromR(context, R.string.error_init_directory), MTA_CSE);
            }
            byte[] buffer = new byte[16384];
            int count;
            FileOutputStream fileOutputStream = new FileOutputStream(dir + SLASH + zipName);
            while ((count = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, count);
            }
            fileOutputStream.close();
            Toast.makeText(context, getFromR(context, R.string.unpacked_successfully) + path, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            throw new IllegalFileException(getFromR(context, R.string.error_open), MTA_CSE);
        }
        return true;
    }

    private static boolean checkSpaceAndNull(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        return !word.contains(" ");
    }

    public static boolean isValidFilename(String filename) {
        if (checkSpaceAndNull(filename)) {
            return filename.matches(VALID_FILENAME_REGEX);
        } else {
            return false;
        }
    }

    public static boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager =
                ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static boolean isExternalStorageWritable() {
        return android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState());
    }

    public static boolean checkAppPermission(Context context) {
        int check = ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE);
        return (check == PackageManager.PERMISSION_DENIED);
    }
}
