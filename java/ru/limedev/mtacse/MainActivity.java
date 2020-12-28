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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import ru.limedev.mtacse.adapters.SectionsPagerAdapter;
import ru.limedev.mtacse.core.UserSettings;
import ru.limedev.mtacse.core.pojo.CustomViewPager;

import static ru.limedev.mtacse.MainFragment.isImageShowing;
import static ru.limedev.mtacse.MainFragment.isLoadingShowing;
import static ru.limedev.mtacse.core.Constants.*;
import static ru.limedev.mtacse.core.UserSettings.*;
import static ru.limedev.mtacse.core.Utilities.deleteDirectory;
import static ru.limedev.mtacse.core.Utilities.getFromR;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences mtacseSettings;
    private Menu menu;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, initializationStatus -> {});
        mtacseSettings = getSharedPreferences(appPreferences, Context.MODE_PRIVATE);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        CustomViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new CustomViewPager.OnPageChangeListener() {

            public void onPageScrollStateChanged(int state) {}

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                if (currentPosition < position) {
                    toggleCodeItems(true);
                } else if (currentPosition > position) {
                    toggleCodeItems(false);
                }
                currentPosition = position;
            }
        });

        boolean acceptedAgreement = mtacseSettings.getBoolean(UserSettings.agreementAcceptedName, DEFAULT_AGREEMENT_ACCEPTED);
        if (!acceptedAgreement) {
            EnterDialogFragment enterDialogFragment = new EnterDialogFragment();
            enterDialogFragment.setCancelable(false);
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            enterDialogFragment.show(transaction, "");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserSettings.saveFile = mtacseSettings.getString(saveFileName, DEFAULT_FILE_SAFE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_action_menu, menu);
        this.menu = menu;
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!isLoadingShowing && !isImageShowing) {
            switch (item.getItemId()) {
                case R.id.menuSettings:
                    openToolsWindow();
                    return true;
                case R.id.clearCache:
                    clearCache();
                    return true;
                case R.id.menuFind:
                    openFind();
                    return true;
                case R.id.aboutApp:
                    openAboutWindow();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } else {
            return false;
        }
    }

    public void toggleCodeItems(boolean show) {
        if (menu != null) {
            menu.findItem(R.id.moreVertical).getSubMenu().setGroupVisible(R.id.menuCodeGroup, show);
        }
    }

    public void openToolsWindow() {
        Intent intent = new Intent(MainActivity.this, ToolsActivity.class);
        startActivity(intent);
    }

    public void openAboutWindow() {
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(intent);
    }

    public void openFind() {
        RelativeLayout relativeLayout = findViewById(R.id.findGroup);
        if (relativeLayout.getVisibility() != View.VISIBLE) {
            relativeLayout.setVisibility(View.VISIBLE);
        }
    }

    public void closeFind(View view) {
        RelativeLayout relativeLayout = findViewById(R.id.findGroup);
        relativeLayout.setVisibility(View.INVISIBLE);
    }

    public void findNext(View view) {
        EditText findTextEdit = findViewById(R.id.editTextFind);
        String findText = findTextEdit.getText().toString();
        CodeFragment.getInstance().findText(this, findText);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void clearCache() {
        File docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File unpackDir = new File(docsDir, MTA_CSE);
        if (unpackDir.exists()) {
            deleteDirectory(unpackDir);
            Toast.makeText(this, getFromR(this, R.string.cache_cleared), Toast.LENGTH_SHORT).show();
        }
    }
}