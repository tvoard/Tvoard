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
/*
 * halbae87: this project is created from Soft Keyboard Sample source
 */
package org.example.tvoard.keyboard

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.PopupWindow
import android.widget.TextView
import org.example.tvoard.R

class MyKeyboardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleRes: Int = 0,
    defStyleAttr: Int = 0
) : KeyboardView(context, attrs) {

    private var mKeyboard: MyKeyboard? = null

    private var mLabelTextSize = 0
    private var mKeyTextSize = 0

    private var mTextColor = 0
    private var mBackgroundColor = 0
    private var mPrimaryColor = 0

    private var mPreviewText: TextView? = null
    private val mPreviewPopup: PopupWindow
    private var mPreviewTextSizeLarge = 0
    private var mPreviewHeight = 0

    private val mCoordinates = IntArray(2)
    private val mPopupKeyboard: PopupWindow
    private var mPopupParent: View
    private val mMiniKeyboardCache: MutableMap<Keyboard.Key, View?>
    private var mKeys = ArrayList<Keyboard.Key>()
    private var mMiniKeyboardSelectedKeyIndex = -1

    var mOnKeyboardActionListener: OnKeyboardActionListener? = null
    private var mVerticalCorrection = 0

    private val mPaint: Paint
    private var mPopupLayout = 0
    private var mPopupMaxMoveDistance = 0f
    private var mTopSmallNumberSize = 0f
    private var mTopSmallNumberMarginWidth = 0f
    private var mTopSmallNumberMarginHeight = 0f
    private val mSpaceMoveThreshold: Int

    private var mKeyBackground: Drawable? = null

    /** The accessibility manager for accessibility support  */
    private val mAccessibilityManager: AccessibilityManager

    private var mHandler: Handler? = null

    init {
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.MyKeyboardView, 0, defStyleRes)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val keyTextSize = 0
        val indexCnt = attributes.indexCount

        try {
            for (i in 0 until indexCnt) {
                val attr = attributes.getIndex(i)
                when (attr) {
                    R.styleable.MyKeyboardView_keyTextSize -> mKeyTextSize =
                        attributes.getDimensionPixelSize(attr, 18)
                }
            }
        } finally {
            attributes.recycle()
        }

        mPopupLayout = R.layout.keyboard_popup_keyboard
        mKeyBackground = resources.getDrawable(R.drawable.keyboard_key_selector, context.theme)
        mVerticalCorrection = resources.getDimension(R.dimen.vertical_correction).toInt()
        mLabelTextSize = resources.getDimension(R.dimen.label_text_size).toInt()
        mPreviewHeight = resources.getDimension(R.dimen.key_height).toInt()
        mSpaceMoveThreshold = resources.getDimension(R.dimen.medium_margin).toInt()
        mTextColor = resources.getColor(R.color.black, context.theme)
        mBackgroundColor = resources.getColor(R.color.blacktheme_color_background, context.theme)
        mPrimaryColor = resources.getColor(R.color.app_color_primary, context.theme)

        mPreviewPopup = PopupWindow(context)
        mPreviewText =
            inflater.inflate(resources.getLayout(R.layout.keyboard_key_preview), null) as TextView
        mPreviewTextSizeLarge = context.resources.getDimension(R.dimen.preview_text_size).toInt()
        mPreviewPopup.contentView = mPreviewText
        mPreviewPopup.setBackgroundDrawable(null)

        mPreviewPopup.isTouchable = false
        mPopupKeyboard = PopupWindow(context)
        mPopupKeyboard.setBackgroundDrawable(null)
        mPopupParent = this
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.textSize = keyTextSize.toFloat()
        mPaint.textAlign = Paint.Align.CENTER
        mPaint.alpha = 255
        mMiniKeyboardCache = HashMap()
        mAccessibilityManager =
            (context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager)
        mPopupMaxMoveDistance = resources.getDimension(R.dimen.popup_max_move_distance)
        mTopSmallNumberSize = resources.getDimension(R.dimen.small_text_size)
        mTopSmallNumberMarginWidth = resources.getDimension(R.dimen.top_small_number_margin_width)
        mTopSmallNumberMarginHeight = resources.getDimension(R.dimen.top_small_number_margin_height)
    }

    override fun onLongPress(key: Keyboard.Key?): Boolean {
        return super.onLongPress(key)
    }
}