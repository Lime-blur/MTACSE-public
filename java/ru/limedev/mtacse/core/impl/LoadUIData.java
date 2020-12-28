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

package ru.limedev.mtacse.core.impl;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import java.io.IOException;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import ru.limedev.mtacse.CodeFragment;
import ru.limedev.mtacse.MainFragment;
import ru.limedev.mtacse.R;
import ru.limedev.mtacse.core.exceptions.IllegalFileException;
import ru.limedev.mtacse.core.ifeces.LoadingUIData;

import static ru.limedev.mtacse.core.Constants.MTA_CSE;
import static ru.limedev.mtacse.core.Utilities.getFromR;

public class LoadUIData implements LoadingUIData {

    private final Activity activity;
    private final MainFragment fragment;
    private final RelativeLayout loadingLayout, showImageLayout;
    private MediaPlayer mediaPlayer;

    public LoadUIData(Activity activity, MainFragment fragment, RelativeLayout loadingLayout,
                      RelativeLayout showImageLayout) {
        this.activity = activity;
        this.fragment = fragment;
        this.loadingLayout = loadingLayout;
        this.showImageLayout = showImageLayout;
    }

    public RelativeLayout getLoadingLayout() {
        return loadingLayout;
    }

    public RelativeLayout getShowImageLayout() {
        return showImageLayout;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void showImage(View view, String path) {
        final ImageView showsImage = view.findViewById(R.id.viewShowsImage);
        showsImage.setImageBitmap(BitmapFactory.decodeFile(path));
    }

    public void playSound(View view, String path) {
        mediaPlayer = new MediaPlayer();
        if (fragment != null) {
            try {
                final ImageView showsImage = view.findViewById(R.id.viewShowsImage);
                showsImage.setImageResource(R.drawable.ic_audiotrack);
                fragment.closeShowsImageView(false);
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                Log.e(MTA_CSE, e.toString());
            }
        }
    }

    public Completable loadCodeProcess(String codeFilePath, String mtaExtension) {
        return Completable.fromAction(()-> CodeFragment.getInstance().loadText(codeFilePath, mtaExtension));
    }

    public Completable showImageProcess(View view, String codeFilePath) {
        return Completable.fromAction(()-> showImage(view, codeFilePath));
    }

    public void startShowImage(View view, String path) {
        if (fragment != null) {
            fragment.updateLoading(false);
            CompositeDisposable disposables = new CompositeDisposable();
            disposables.add(showImageProcess(view, path)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> Thread.sleep(500))
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        fragment.updateLoading(true);
                        fragment.closeShowsImageView(false);
                        dispose();
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        fragment.updateLoading(true);
                        if (activity != null) {
                            Toast.makeText(activity, getFromR(activity, R.string.error_open), Toast.LENGTH_SHORT).show();
                        }
                        Log.e(MTA_CSE, e.toString());
                    }
                })
            );
        }
    }

    public void startLoadCode(String path, String extension) {
        if (fragment != null) {
            fragment.updateLoading(false);
            CompositeDisposable disposables = new CompositeDisposable();
            disposables.add(loadCodeProcess(path, extension)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> Thread.sleep(500))
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        if (activity != null) {
                            ViewPager viewPager = activity.findViewById(R.id.viewPager);
                            viewPager.arrowScroll(View.FOCUS_RIGHT);
                            fragment.updateLoading(true);
                            Toast.makeText(activity, getFromR(activity, R.string.content_loaded), Toast.LENGTH_SHORT).show();
                        } else {
                            onError(new IllegalFileException());
                        }
                        dispose();
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        fragment.updateLoading(true);
                        if (activity != null) {
                            Toast.makeText(activity, getFromR(activity, R.string.error_open), Toast.LENGTH_SHORT).show();
                        }
                        Log.e(MTA_CSE, e.toString());
                    }
                })
            );
        }
    }
}
