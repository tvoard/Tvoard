/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.example.tvoard.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.inputmethodservice.Keyboard
import android.view.inputmethod.EditorInfo
import org.example.tvoard.R

@SuppressLint("InlinedApi")
class MyKeyboard : Keyboard {

    private var mEnterKey: Key? = null

    constructor(context: Context?, xmlLayoutResId: Int) : super(context, xmlLayoutResId)

    constructor(
        context: Context?,
        layoutTemplateResId: Int,
        characters: CharSequence?,
        columns: Int,
        horizontalPadding: Int
    ) : super(context, layoutTemplateResId, characters, columns, horizontalPadding)

    override fun createKeyFromXml(
        res: Resources, parent: Row, x: Int, y: Int,
        parser: XmlResourceParser
    ): Key {
        val key: Key = MyKey(res, parent, x, y, parser)
        if (key.codes[0] == 10) {
            mEnterKey = key
        }
        return key
    }

    /**
     * This looks at the ime options given by the current editor, to set the
     * appropriate label on the keyboard's enter key (if it has one).
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    fun setImeOptions(res: Resources, options: Int) {
        if (mEnterKey == null) {
            return
        }
        when (options and (EditorInfo.IME_MASK_ACTION or EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            EditorInfo.IME_ACTION_GO -> {
                mEnterKey!!.iconPreview = null
                mEnterKey!!.icon = null
                mEnterKey!!.label = res.getText(R.string.label_go_key)
            }

            EditorInfo.IME_ACTION_NEXT -> {
                mEnterKey!!.iconPreview = null
                mEnterKey!!.icon = null
                mEnterKey!!.label = res.getText(R.string.label_next_key)
            }

            EditorInfo.IME_ACTION_SEARCH -> {
                mEnterKey!!.icon = res.getDrawable(
                    R.drawable.sym_keyboard_search, null
                )
                mEnterKey!!.label = null
            }

            EditorInfo.IME_ACTION_SEND -> {
                mEnterKey!!.iconPreview = null
                mEnterKey!!.icon = null
                mEnterKey!!.label = res.getText(R.string.label_send_key)
            }

            else -> {
                mEnterKey!!.icon = res.getDrawable(
                    R.drawable.sym_keyboard_return, null
                )
                mEnterKey!!.label = null
            }
        }
    }

    internal class MyKey(
        res: Resources?,
        parent: Row?,
        x: Int,
        y: Int,
        parser: XmlResourceParser?
    ) : Key(res, parent, x, y, parser) {
        /**
         * Overriding this method so that we can reduce the target area for the key that
         * closes the keyboard.
         */
        override fun isInside(x: Int, y: Int): Boolean {
            return super.isInside(x, if (codes[0] == KEYCODE_CANCEL) y - 10 else y)
        }
    }
}