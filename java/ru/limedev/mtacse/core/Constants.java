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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Constants {
    public static final int PICKFILE_RESULT_CODE = 0;

    public static final String INTENT_TYPE = "*/*";
    public static final String MTA_LUA_LINK = "https://luac.mtasa.com/?compile=1&debug=0&obfuscate=3";
    public static final String ZIP = "zip";
    public static final String FX = "fx";
    public static final String MAP = "map";
    public static final String EDF = "edf";
    public static final String LUA = "lua";
    public static final String XML = "xml";
    public static final String PREFIX_PATH = "/storage/emulated/0/";

    public static final String SLASH = "/";
    public static final String VALID_FILENAME_REGEX = "[a-zA-Z0-9,.;:_'\\\\s-]*";
    public static final String FILE_SIZE_PATTERN = "#,##0.#";

    public static final String DEFAULT_FILE_SAFE = "code.lua";
    public static final boolean DEFAULT_AGREEMENT_ACCEPTED = false;

    public static final String CONTENT = "content";
    public static final String MTA_CSE = "MTACSE";

    public static final List<String> IMAGES_FORMATS = new ArrayList<>(Arrays.asList("bmp", "jpeg", "jpg", "tif", "png"));
    public static final List<String> SOUND_FORMATS = new ArrayList<>(Arrays.asList("mp3", "ogg", "wav"));
    public static final List<String> CODE_FORMATS = new ArrayList<>(Arrays.asList(FX, MAP, EDF, LUA, XML));
}

