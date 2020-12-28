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

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import ru.limedev.mtacse.core.UserSettings;

import static ru.limedev.mtacse.core.UserSettings.appPreferences;
import static ru.limedev.mtacse.core.Utilities.getFromR;

public class EnterDialogFragment extends DialogFragment {

    private SharedPreferences mtacseSettings;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() != null && getContext() != null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getFromR(getContext(), R.string.crypto_agreement));
            builder.setMessage(getFromR(getContext(), R.string.crypto_agreement_text));
            mtacseSettings = getActivity().getSharedPreferences(appPreferences, Context.MODE_PRIVATE);

            builder.setPositiveButton(getFromR(getContext(), R.string.yes), (dialog, id) -> {
                dialog.cancel();
                SharedPreferences.Editor editor = mtacseSettings.edit();
                editor.putBoolean(UserSettings.agreementAcceptedName, true);
                editor.apply();
            });

            builder.setNegativeButton(getFromR(getContext(), R.string.no), (dialog, id) -> {
                if (getActivity() != null) {
                    getActivity().moveTaskToBack(true);
                    getActivity().finish();
                }
            });

            builder.setCancelable(true);
            return builder.create();
        }
        return super.onCreateDialog(savedInstanceState);
    }
}
