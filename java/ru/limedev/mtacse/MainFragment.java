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
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

import org.zeroturnaround.zip.ZipException;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import ru.limedev.mtacse.adapters.FilesAdapter;
import ru.limedev.mtacse.core.exceptions.IllegalFileException;
import ru.limedev.mtacse.core.http.HttpRequest;
import ru.limedev.mtacse.core.impl.LoadUIData;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static ru.limedev.mtacse.CodeFragment.openedFilePath;
import static ru.limedev.mtacse.core.Constants.*;
import static ru.limedev.mtacse.core.Utilities.*;

public class MainFragment extends Fragment implements View.OnClickListener {

    private static List<File> dirFiles;
    private static String unzippedFilesPath, docsPath;
    private static int downloadCount;

    public static boolean isLoadingShowing = false, isImageShowing = false;

    private ListView listView;
    private TextView loadingTextView;
    private LoadUIData loadUIData;

    public MainFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        dirFiles = new ArrayList<>();

        LinearLayout openFileButton = view.findViewById(R.id.openFileButton);
        LinearLayout compileButton = view.findViewById(R.id.compileButton);
        LinearLayout zipButton = view.findViewById(R.id.zipButton);
        RelativeLayout closeImage = view.findViewById(R.id.showImageBg);
        openFileButton.setOnClickListener(this);
        compileButton.setOnClickListener(this);
        zipButton.setOnClickListener(this);
        closeImage.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        listView = view.findViewById(R.id.filesList);
        loadingTextView = view.findViewById(R.id.loadingInfo);

        loadUIData = new LoadUIData(getActivity(), this, view.findViewById(R.id.mainProgressBarBg),
                view.findViewById(R.id.showImageBg));

        listView.setOnItemClickListener((parent, v, position, id) -> {
            if (!isLoadingShowing && !isImageShowing) {
                if (unzippedFilesPath != null && new File(unzippedFilesPath).exists()) {
                    String codeFilePath = listView.getItemAtPosition(position).toString();
                    File isFileToLoad = new File(codeFilePath);
                    if (isFileToLoad.exists() && isFileToLoad.isFile()) {
                        openedFilePath = codeFilePath;
                        String mtaExtension = getFileExtension(isFileToLoad);
                        if (getFileTypeFromExtension(mtaExtension) == 0) {
                            if (getView() != null) {
                                loadUIData.startShowImage(getView(), codeFilePath);
                            }
                        } else if (getFileTypeFromExtension(mtaExtension) == 1) {
                            if (getView() != null) {
                                loadUIData.playSound(getView(), codeFilePath);
                            }
                        } else if (getFileTypeFromExtension(mtaExtension) == 2) {
                            loadUIData.startLoadCode(codeFilePath, mtaExtension);
                        } else {
                            loadUIData.startLoadCode(codeFilePath, mtaExtension);
                        }
                    } else {
                        showToast(getTranslated(R.string.error_open), false);
                    }
                } else {
                    showToast(getTranslated(R.string.dir_path_not_found), false);
                }
            }
        });

        final AdView mAdView = view.findViewById(R.id.adView);
        final ScrollView scrollView = view.findViewById(R.id.filesImageBackgroundGroup);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                mAdView.setVisibility(View.GONE);
                RelativeLayout.LayoutParams paramsScroll = (RelativeLayout.LayoutParams) scrollView.getLayoutParams();
                paramsScroll.setMargins(paramsScroll.leftMargin, paramsScroll.topMargin, paramsScroll.rightMargin, 0);
                scrollView.setLayoutParams(paramsScroll);

                RelativeLayout.LayoutParams paramsList = (RelativeLayout.LayoutParams) listView.getLayoutParams();
                paramsList.setMargins(paramsList.leftMargin, paramsList.topMargin, paramsList.rightMargin, 0);
                scrollView.setLayoutParams(paramsList);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (getContext() != null && uri != null) {
                    String zipName = getFileName(getContext(), uri);
                    File docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                    File unpackDir = new File(docsDir, MTA_CSE);
                    String extension = getFileExtension(new File(unpackDir, zipName));
                    try {
                        boolean isSuccessfully;
                        if (extension.equals(ZIP)) {
                            isSuccessfully = customUnzip(getContext(), uri, unpackDir, zipName);
                        } else {
                            isSuccessfully = copyFileToDocuments(getContext(), uri, unpackDir, zipName);
                        }
                        if (isSuccessfully) {
                            dirFiles.clear();
                            File zipDir = new File(unpackDir, zipName);
                            File zipDocsDir = new File(docsDir, zipName);
                            unzippedFilesPath = zipDir.getPath();
                            docsPath = zipDocsDir.getPath();
                            directoryToList(zipDir);
                            updateFilesList(dirFiles, true);
                            CodeFragment.getInstance().cleanAreaWithHint();
                        } else {
                            showToast(getTranslated(R.string.error_open), false);
                        }
                    } catch (IllegalFileException e) {
                        showToast(e.getMessage(), true);
                    }
                }
            }
        }
    }

    private void directoryToList(File dir) {
        File[] filesList = dir.listFiles();
        if (filesList != null) {
            for (File file : filesList) {
                if (file != null) {
                    if (file.isDirectory()) {
                        directoryToList(file);
                    } else {
                        dirFiles.add(file);
                    }
                }
            }
        }
    }

    public void updateFilesList(final List<File> filesList, boolean hideBg) {
        if (getView() != null) {
            ScrollView scrollViewToHide = getView().findViewById(R.id.filesImageBackgroundGroup);
            ListView listViewToShow = getView().findViewById(R.id.filesList);
            if (hideBg) {
                scrollViewToHide.setVisibility(View.GONE);
                listViewToShow.setVisibility(View.VISIBLE);
            } else {
                scrollViewToHide.setVisibility(View.VISIBLE);
                listViewToShow.setVisibility(View.GONE);
            }
        }
        ArrayAdapter<File> filesAdapter = new FilesAdapter(getContext(), filesList);
        listView.setAdapter(filesAdapter);
    }

    public void updateLoading(boolean hide) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            CoordinatorLayout coordinatorLayout = mainActivity.findViewById(R.id.mainAppLayout);
            if (coordinatorLayout != null) {
                if (hide) {
                    loadUIData.getLoadingLayout().setVisibility(View.GONE);
                    isLoadingShowing = false;
                    enableDisableView(coordinatorLayout, true);
                } else {
                    loadUIData.getLoadingLayout().setVisibility(View.VISIBLE);
                    isLoadingShowing = true;
                    enableDisableView(coordinatorLayout, false);
                }
                loadingTextView.setText(getTranslated(R.string.loading));
            }
        }
    }

    public void closeShowsImageView(boolean hide) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            CoordinatorLayout coordinatorLayout = mainActivity.findViewById(R.id.mainAppLayout);
            if (coordinatorLayout != null) {
                if (hide) {
                    if (loadUIData.getMediaPlayer() != null && loadUIData.getMediaPlayer().isPlaying()) {
                        loadUIData.getMediaPlayer().stop();
                        loadUIData.setMediaPlayer(null);
                    }
                    loadUIData.getShowImageLayout().setVisibility(View.GONE);
                    isImageShowing = false;
                    enableDisableView(coordinatorLayout, true);
                } else {
                    loadUIData.getShowImageLayout().setVisibility(View.VISIBLE);
                    isImageShowing = true;
                    enableDisableView(coordinatorLayout, false);
                }
            }
        }
    }

    private void deleteZipInDocs() {
        if (docsPath != null) {
            File zipInDocs = new File(docsPath);
            if (zipInDocs.exists() && zipInDocs.isFile()) {
                zipInDocs.delete();
            }
        }
    }

    private Completable zipProcess() {
        return Completable.fromAction(this::zipFiles);
    }

    private void zipFilesMain() {
        updateLoading(false);
        if (unzippedFilesPath != null && docsPath != null && new File(unzippedFilesPath).exists()) {
            CompositeDisposable disposables = new CompositeDisposable();
            disposables.add(zipProcess()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(() -> Thread.sleep(500))
                    .subscribeWith(new DisposableCompletableObserver() {
                        @Override public void onComplete() {
                            updateFilesList(dirFiles, false);
                            CodeFragment.getInstance().cleanAreaWithHint();
                            unzippedFilesPath = null;
                            docsPath = null;
                            updateLoading(true);
                            showToast(getTranslated(R.string.packed_successfully), true);
                            dispose();
                        }
                        @Override public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                            updateLoading(true);
                            showToast(getTranslated(R.string.error_pack), false);
                            Log.e(MTA_CSE, e.toString());
                        }
                    })
            );
        } else {
            showToast(getTranslated(R.string.dir_path_not_found), false);
        }
    }

    private void zipFiles() {
        try {
            File zipArchive = new File(unzippedFilesPath);
            deleteZipInDocs();
            File savedDocsFile = new File(docsPath);
            ZipUtil.pack(zipArchive, changeFileExtension(savedDocsFile, ZIP));
            deleteDirectory(zipArchive);
            dirFiles.clear();
        } catch (ZipException e) {
            deleteZipInDocs();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (!isLoadingShowing) {
            switch (v.getId()) {
                case R.id.openFileButton:
                    openFileOrArchive();
                    break;
                case R.id.compileButton:
                    if (getActivity() != null) {
                        if (isNetworkAvailable(getActivity())) {
                            if (unzippedFilesPath != null && new File(unzippedFilesPath).exists()) {
                                obfuscateProcess();
                                CodeFragment.getInstance().cleanAreaWithHint();
                                updateLoading(false);
                            } else {
                                showToast(getTranslated(R.string.dir_path_not_found), false);
                            }
                        } else {
                            showToast(getTranslated(R.string.no_internet_connection), false);
                        }
                    }
                    break;
                case R.id.zipButton:
                    zipFilesMain();
                    break;
                case R.id.showImageBg:
                    if (isImageShowing) {
                        closeShowsImageView(true);
                    }
                    break;
            }
        }
    }

    private void openFileOrArchive() {
        if (getActivity() != null && getContext() != null) {
            if (checkAppPermission(getContext())) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE}, 0);
            } else {
                if (isExternalStorageWritable()) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType(INTENT_TYPE);
                    startActivityForResult(intent, PICKFILE_RESULT_CODE);
                } else {
                    showToast(getTranslated(R.string.unwritable_storage), false);
                }
            }
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Optional<HttpRequest> checkHttpRequest(File file) {
        String extension = getFileExtension(file);
        if (extension.equals(LUA)) {
            try {
                HttpRequest httpRequest = HttpRequest.post(MTA_LUA_LINK).send(file);
                if (httpRequest.ok()) {
                    downloadCount++;
                    return Optional.of(httpRequest);
                }
            } catch (HttpRequest.HttpRequestException e) {
                Log.e(MTA_CSE, e.toString());
            }
        }
        return Optional.empty();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void obfuscateProcess() {
        final String TAIL_LOADING = SLASH + dirFiles.size();
        Observable<File> observable = Observable.fromIterable(dirFiles);
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(observable.subscribeOn(Schedulers.io()).doOnNext(file -> {
            Optional<HttpRequest> httpRequest = checkHttpRequest(file);
            httpRequest.ifPresent(request -> request.receive(file));
        }).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<File>() {
            @Override
            public void onNext(@io.reactivex.annotations.NonNull File file) {
                StringBuffer stringBuilder = new StringBuffer(getTranslated(R.string.loading));
                stringBuilder.append(downloadCount).append(TAIL_LOADING);
                loadingTextView.setText(stringBuilder);
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                Log.e(MTA_CSE, e.toString());
            }

            @Override
            public void onComplete() {
                updateLoading(true);
                showToast(getTranslated(R.string.download_successful) + downloadCount, false);
                downloadCount = 0;
                dispose();
            }
        }));
    }
}