package org.example.tvoard

import android.util.Log
import org.example.tvoard.InputTables.FirstConsonants
import org.example.tvoard.InputTables.LastConsonants
import org.example.tvoard.InputTables.NormalKeyMap
import org.example.tvoard.InputTables.ShiftedKeyMap
import org.example.tvoard.InputTables.Vowels
import org.example.tvoard.common.ACTION_APPEND
import org.example.tvoard.common.ACTION_ERROR
import org.example.tvoard.common.ACTION_NONE
import org.example.tvoard.common.ACTION_UPDATE_COMPLETESTR
import org.example.tvoard.common.ACTION_UPDATE_COMPOSITIONSTR
import org.example.tvoard.common.ACTION_USE_INPUT_AS_RESULT
import org.example.tvoard.common.HANGUL_END
import org.example.tvoard.common.HANGUL_JAMO_END
import org.example.tvoard.common.HANGUL_JAMO_START
import org.example.tvoard.common.HANGUL_MO_START
import org.example.tvoard.common.HANGUL_START
import org.example.tvoard.common.KEYSTATE_ALT_MASK
import org.example.tvoard.common.KEYSTATE_CTRL_MASK
import org.example.tvoard.common.KEYSTATE_FN
import org.example.tvoard.common.KEYSTATE_SHIFT_MASK
import org.example.tvoard.common.NUM_OF_FIRST
import org.example.tvoard.common.NUM_OF_LAST
import org.example.tvoard.common.NUM_OF_LAST_INDEX
import org.example.tvoard.common.NUM_OF_MIDDLE

class KoreanAutomata {

    private var state = 0
    private var compositionString = ""
    private var completeString = ""
    private var isKoreanMode = false

    init {
        state = 0
        compositionString = ""
        completeString = ""
        isKoreanMode = false
    }

    companion object {
        private const val TAG = "KoreanAutomata"
    }

    fun getState(): Int {
        return state
    }

    fun getCompositionString(): String {
        return compositionString
    }

    fun getCompleteString(): String {
        return completeString
    }

    fun toggleMode() {
        isKoreanMode = !isKoreanMode
    }

    fun isKoreanMode(): Boolean {
        return isKoreanMode
    }

    private fun isHangul(word: Char): Boolean {
        if (word.code in HANGUL_START..HANGUL_END) return true
        return word.code in HANGUL_JAMO_START..HANGUL_JAMO_END
    }

    private fun isJamo(word: Char): Boolean {
        return word.code in HANGUL_JAMO_START..HANGUL_JAMO_END
    }

    private fun isConsonant(word: Char): Boolean {
        return word.code in HANGUL_JAMO_START until HANGUL_MO_START
    }

    private fun isVowel(word: Char): Boolean {
        return word.code in HANGUL_MO_START..HANGUL_JAMO_END
    }

    private fun getFirstConsonantIndex(word: Char): Int {
        var fcIndex = -1
        if (isHangul(word)) {
            if (isConsonant(word)) {
                fcIndex = 0
                while (fcIndex < NUM_OF_FIRST) {
                    if (word == FirstConsonants.Word[fcIndex]) break
                    fcIndex++
                }
                if (fcIndex >= NUM_OF_FIRST) fcIndex = -1
            } else if (isVowel(word)) {
                fcIndex = -1
            } else {
                val offset = word.code - HANGUL_START
                fcIndex = offset / (NUM_OF_MIDDLE * NUM_OF_LAST_INDEX)
            }
        }
        return fcIndex
    }

    fun getFirstConsonant(word: Char): Char {
        val fcCode: Char
        val fcIndex = getFirstConsonantIndex(word)
        fcCode = if (fcIndex < 0) 0.toChar() else FirstConsonants.Word[fcIndex]
        return fcCode
    }

    private fun getVowelIndex(word: Char): Int {
        var vIndex = -1
        if (isHangul(word)) {
            vIndex = if (isVowel(word)) {// vowel only character..
                convertVowelToIndex(word)
            } else {
                val offset = word.code - HANGUL_START
                offset % (NUM_OF_MIDDLE * NUM_OF_LAST_INDEX) / NUM_OF_LAST_INDEX
            }
        }
        return vIndex
    }

    private fun getVowel(word: Char): Char {
        val vCode: Char
        val vIndex = getVowelIndex(word)
        vCode = if (vIndex < 0) 0.toChar() else Vowels.Word[vIndex]
        return vCode
    }

    private fun getLastConsonantIndex(word: Char): Int {
        var lcIndex = -1
        if (isHangul(word)) {
            if (isJamo(word)) {
                if (isConsonant(word)) {
                    lcIndex = 0
                    while (lcIndex < NUM_OF_LAST_INDEX) {
                        if (word == LastConsonants.Word[lcIndex]) break
                        lcIndex++
                    }
                    if (lcIndex >= NUM_OF_LAST_INDEX) lcIndex = -1
                } else lcIndex = -1
            } else {
                val offset = word.code - HANGUL_START
                lcIndex = offset % NUM_OF_LAST_INDEX
            }
        }
        return lcIndex
    }

    fun getLastConsonant(word: Char): Char {
        val lcIndex = getLastConsonantIndex(word)
        return if (lcIndex < 0) 0.toChar() else LastConsonants.Word[lcIndex]
    }

    // fcCode should be one of "First Consonants" otherwise return -1
    private fun convertFirstConsonantToIndex(fcWord: Char): Int {
        var fcIndex = 0
        while (fcIndex < NUM_OF_FIRST) {
            if (fcWord == FirstConsonants.Word[fcIndex]) {
                break
            }
            fcIndex++
        }
        if (fcIndex == NUM_OF_FIRST) {
            fcIndex = -1
        }
        return fcIndex
    }

    private fun convertVowelToIndex(vWord: Char): Int {
        if (vWord < Vowels.Word[0]) return -1
        val vIndex = vWord - Vowels.Word[0]
        return if (vIndex >= NUM_OF_MIDDLE) -1 else vIndex
    }

    // fcCode should be one of "Last Consonants", otherwise return -1
    private fun convertLastConsonantToIndex(lcWord: Char): Int {
        var lcIndex = 0
        while (lcIndex < NUM_OF_LAST_INDEX) {
            if (lcWord == LastConsonants.Word[lcIndex]) {
                break
            }
            lcIndex++
        }
        if (lcIndex == NUM_OF_LAST_INDEX) {
            lcIndex = -1
        }
        return lcIndex
    }

    fun combineVowelWithIndex(vIndex1: Int, vIndex2: Int): Int {
        var newIndex = -1
        val vCode1 = Vowels.Word[vIndex1]
        val vCode2 = Vowels.Word[vIndex2]
        val newWord = combineVowelWithWord(vCode1, vCode2)
        if (newWord != 0.toChar()) {
            newIndex = convertVowelToIndex(newWord)
        }
        return newIndex
    }

    private fun combineVowelWithWord(vWord1: Char, vWord2: Char): Char {
        var newWord = 0.toChar()
        if (vWord1.code == 0x3157) { // ㅗ
            when (vWord2.code) {
                0x314F -> newWord = 0x3158.toChar()
                0x3150 -> newWord = 0x3159.toChar()
                0x3163 -> newWord = 0x315A.toChar()
            }
        } else if (vWord1.code == 0x315C) { // ㅜ
            when (vWord2.code) {
                0x3153 -> newWord = 0x315D.toChar()
                0x3154 -> newWord = 0x315E.toChar()
                0x3163 -> newWord = 0x315F.toChar()
            }
        } else if (vWord1.code == 0x3161) { // ㅡ
            if (vWord2.code == 0x3163)
                newWord = 0x3162.toChar()
        }
        return newWord
    }

    private fun combineLastConsonantWithIndex(cIndex1: Int, cIndex2: Int): Int {
        var newIndex: Int
        var newWord = 0.toChar()
        if (LastConsonants.Code[cIndex1] == 0x3131 && LastConsonants.Code[cIndex2] == 0x3145) {
            newWord = 0x3133.toChar()
        }
        if (LastConsonants.Code[cIndex1] == 0x3142 && LastConsonants.Code[cIndex2] == 0x3145) {
            newWord = 0x3144.toChar()
        }

        // you may not use this file except in compliance with the License.
        if (LastConsonants.Code[cIndex1] == 0x3134) {
            when (LastConsonants.Code[cIndex2]) {
                0x3148 -> newWord = 0x3135.toChar()
                0x314E -> newWord = 0x3136.toChar()
            }
        }
        if (LastConsonants.Code[cIndex1] == 0x3139) {
            when (LastConsonants.Code[cIndex2]) {
                0x3131 -> newWord = 0x313A.toChar()
                0x3141 -> newWord = 0x313B.toChar()
                0x3142 -> newWord = 0x313C.toChar()
                0x3145 -> newWord = 0x313D.toChar()
                0x314C -> newWord = 0x313E.toChar()
                0x314D -> newWord = 0x313F.toChar()
                0x314E -> newWord = 0x3140.toChar()
            }
        }
        newIndex = if (newWord == 0.toChar()) -1 else convertLastConsonantToIndex(newWord)
        return newIndex
    }

    private fun combineLastConsonantWithWord(lcWord1: Char, lcWord2: Char): Char {
        var newWord: Char = 0.toChar()
        if (lcWord1.code == 0x3131 && lcWord2.code == 0x3145) { // ㄱ
            newWord = 'ㄳ'
        } else if (lcWord1.code == 0x3142 && lcWord2.code == 0x3145) {
            newWord = 'ㅄ'
        } else if (lcWord1.code == 0x3134) { // ㄴ
            if (lcWord2.code == 0x3148) { // ㅈ
                newWord = 'ㄵ'
            } else if (lcWord2.code == 0x314E) { // ㅎ
                newWord = 'ㄶ'
            }
        } else if (lcWord1.code == 0x3139) { // ㄹ
            when (lcWord2.code) {
                0x3131 -> newWord = 0x313A.toChar()
                0x3141 -> newWord = 0x313B.toChar()
                0x3142 -> newWord = 0x313C.toChar()
                0x3145 -> newWord = 0x313D.toChar()
                0x314C -> newWord = 0x313E.toChar()
                0x314D -> newWord = 0x313F.toChar()
                0x314E -> newWord = 0x3140.toChar()
            }
        }
        return newWord
    }

    private fun composeWordWithIndexes(fcIndex: Int, vIndex: Int, lcIndex: Int): Char {
        var word = 0.toChar()
        if (fcIndex in 0 until NUM_OF_FIRST) {
            if (vIndex in 0 until NUM_OF_MIDDLE) {
                if (lcIndex in 0 until NUM_OF_LAST) {
                    val offset =
                        fcIndex * NUM_OF_MIDDLE * NUM_OF_LAST_INDEX + vIndex * NUM_OF_LAST_INDEX + lcIndex
                    word = (offset + HANGUL_START).toChar()
                }
            }
        }
        return word
    }

    private fun getAlphabetIndex(code: Char): Int {
        if (code in 'a'..'z') return (code - 'a')
        return if (code in 'A'..'Z') (code - 'A') else -1
    }

    // Input is ended by external causes
    fun finishAutomataWithoutInput(): Int {
        Log.v(TAG, "finishAutomataWithoutInput: 0.")
        val ret = ACTION_NONE
        if (isKoreanMode) {
            completeString = ""
            compositionString = ""
            state = 0
        }
        return ret
    }

    fun doBackSpace(): Int {
        Log.v(TAG, "doBackSpace: 0.")

        var ret: Int
        var word: Char = if (compositionString !== "") compositionString[0] else 0.toChar()
        Log.v(TAG, "doBackSpace: 1. code=$word")
        if (state != 0 && word == 0.toChar()) {
            return ACTION_ERROR
        }

        Log.v(TAG, "doBackSpace: 2. mState=$state")
        when (state) {
            0 -> ret = ACTION_USE_INPUT_AS_RESULT
            1, 4 -> {
                compositionString = ""
                state = 0
                ret = ACTION_USE_INPUT_AS_RESULT
            }
            2 -> {
                run {
                    val fcIndex = getFirstConsonantIndex(word)
                    compositionString = FirstConsonants.Word[fcIndex] + ""
                    state = 1
                }
                return ACTION_UPDATE_COMPOSITIONSTR
            }
            3 -> {
                run {
                    val lcIndex = getLastConsonantIndex(word)
                    compositionString = (word.code - lcIndex).toChar() + ""
                    state = 2
                }
                return ACTION_UPDATE_COMPOSITIONSTR
            }
            5 -> {
                run {
                    val vIndex = getVowelIndex(word)
                    if (vIndex < 0) {
                        return ACTION_ERROR
                    }
                    val newIndex = Vowels.iMiddle[vIndex]
                    if (newIndex < 0) {
                        return ACTION_ERROR
                    }
                    compositionString = Vowels.Word[newIndex] + ""
                    state = 4
                }
                return ACTION_UPDATE_COMPOSITIONSTR
            }
            10 -> {
                run {
                    val lcIndex = getLastConsonantIndex(word)
                    if (lcIndex < 0) {
                        return ACTION_ERROR
                    }
                    val newIndex = LastConsonants.iLast[lcIndex]
                    if (newIndex < 0) {
                        return ACTION_ERROR
                    }
                    compositionString = LastConsonants.Word[newIndex] + ""
                    state = 1
                }
                return ACTION_UPDATE_COMPOSITIONSTR
            }
            11 -> {
                run {
                    val lcIndex = getLastConsonantIndex(word)
                    if (lcIndex < 0) {
                        return ACTION_ERROR
                    }
                    val newIndex = LastConsonants.iLast[lcIndex]
                    if (newIndex < 0) {
                        return ACTION_ERROR
                    }
                    compositionString = (word.code - lcIndex + newIndex).toChar() + ""
                    state = 3
                }
                return ACTION_UPDATE_COMPOSITIONSTR
            }
            20 -> {
                run {
                    val fcIndex = getFirstConsonantIndex(word)
                    val vIndex = getVowelIndex(word)
                    val newIndex = Vowels.iMiddle[vIndex]
                    if (newIndex < 0) {
                        return ACTION_ERROR
                    }
                    compositionString = composeWordWithIndexes(fcIndex, newIndex, 0) + ""
                    state = 2
                }
                return ACTION_UPDATE_COMPOSITIONSTR
            }
            21 -> {
                run {
                    val lcIndex = getLastConsonantIndex(word)
                    compositionString = (word.code - lcIndex).toChar() + ""
                    state = 20
                }
                return ACTION_UPDATE_COMPOSITIONSTR
            }
            22 -> {
                run {
                    val lcIndex = getLastConsonantIndex(word)
                    if (lcIndex < 0) {
                        return ACTION_ERROR
                    }
                    val newIndex = LastConsonants.iLast[lcIndex]
                    if (newIndex < 0) {
                        return ACTION_ERROR
                    }
                    compositionString = (word.code - lcIndex + newIndex).toChar() + ""
                    state = 21
                }
                return ACTION_UPDATE_COMPOSITIONSTR
            }
            else -> return ACTION_ERROR // error. should not be here in any circumstance.
        }
        return ret
    }

    fun doAutomata(word: Char, KeyState: Int): Int {
        Log.v(TAG, "doAutomata: 0. word=$word KeyState=$KeyState mState=$state")

        // 1. word가 [a, ... z, A, ... Z]에 속하지 않은 경우
        val alphaIndex = getAlphabetIndex(word)
        Log.v(TAG, "doAutomata: 1. alphaIndex=$alphaIndex")
        if (alphaIndex < 0) {
            var result = ACTION_NONE
            if (isKoreanMode) {
                // flush Korean characters first.
                completeString = compositionString
                compositionString = ""
                state = 0
                result = ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
            }
            // process the code as English
            if (KeyState and (KEYSTATE_ALT_MASK or KEYSTATE_CTRL_MASK or KEYSTATE_FN) == 0) {
                result = result or ACTION_USE_INPUT_AS_RESULT
            }
            return result
        }

        // 2. 한글 키보드 아닌 경우 => 입력한 대로 입력
        Log.v(TAG, "doAutomata: 2. mKoreanMode=$isKoreanMode")
        if (!isKoreanMode) {
            return ACTION_USE_INPUT_AS_RESULT
        }

        // 3. 한글 키보드인 경우 => Shift 여부에 따라 글자 변환하여 입력
        val hCode: Char =
            if (KeyState and KEYSTATE_SHIFT_MASK == 0) NormalKeyMap.Word[alphaIndex] else ShiftedKeyMap.Word[alphaIndex]
        Log.v(TAG, "doAutomata: 3. hCode=$hCode mState=$state")
        return when (state) {
            0 -> doState00(hCode)
            1 -> doState01(hCode)
            2 -> doState02(hCode)
            3 -> doState03(hCode)
            4 -> doState04(hCode)
            5 -> doState05(hCode)
            10 -> doState10(hCode)
            11 -> doState11(hCode)
            20 -> doState20(hCode)
            21 -> doState21(hCode)
            22 -> doState22(hCode)
            else -> ACTION_ERROR // error. should not be here in any circumstance.
        }
    }

    /**
     * 조합: NULL
     */
    private fun doState00(word: Char): Int {
        Log.v(TAG, "-doState00: 0. word=$word")

        state = if (isConsonant(word)) 1 else 4
        completeString = ""
        compositionString = word + ""
        return ACTION_UPDATE_COMPOSITIONSTR or ACTION_APPEND
    }

    /**
     * 조합: single consonant only
     * 예시: ㄱ
     */
    private fun doState01(word: Char): Int {
        Log.v(TAG, "-doState01: 0. word=$word")

        Log.v(TAG, "-doState01: 1. compositionString=$compositionString")
        if (compositionString === "") {
            return ACTION_ERROR
        }

        Log.v(TAG, "-doState01: 2. isConsonant=${isConsonant(word)}")
        if (isConsonant(word)) {
            // cannot combine last consonants
            val newWord = combineLastConsonantWithWord(compositionString[0], word)
            Log.v(TAG, "-doState01: 3. newWord=$newWord")
            return if (newWord == 0.toChar()) {
                state = 1
                completeString = compositionString // flush
                compositionString = word + ""
                ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
            } else { // can combine last consonants
                state = 10
                completeString = ""
                compositionString = newWord + ""
                ACTION_UPDATE_COMPOSITIONSTR
            }
        }

        val fcIndex = convertFirstConsonantToIndex(compositionString[0])
        val vIndex = convertVowelToIndex(word)
        val newWord = composeWordWithIndexes(fcIndex, vIndex, 0)
        state = 2
        completeString = ""
        compositionString = newWord + ""
        return ACTION_UPDATE_COMPOSITIONSTR
    }

    /**
     * 조합: single consonant + single vowel
     * 예시: 가
     */
    private fun doState02(word: Char): Int {
        Log.v(TAG, "-doState02: 0. word=$word")

        Log.v(TAG, "-doState02: 1. compositionString=$compositionString")
        if (compositionString === "") {
            return ACTION_ERROR
        }

        Log.v(TAG, "-doState02: 2. isConsonant=${isConsonant(word)}")
        if (isConsonant(word)) {
            val lcIndex = getLastConsonantIndex(word)
            Log.v(TAG, "-doState02: 3. lcIndex=$lcIndex")
            return if (lcIndex != -1) {
                state = 3
                completeString = ""
                compositionString = (compositionString[0].code + lcIndex).toChar() + ""
                ACTION_UPDATE_COMPOSITIONSTR
            } else {
                state = 1
                completeString = compositionString
                compositionString = word + ""
                ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
            }
        } else { // vowel
            val vCode = getVowel(compositionString[0])
            val newWord = combineVowelWithWord(vCode, word)
            Log.v(TAG, "-doState02: 3. vCode=$vCode newWord=$newWord")
            return if (newWord != 0.toChar()) {
                val fcIndex = getFirstConsonantIndex(compositionString[0])
                val vIndex = convertVowelToIndex(newWord)
                state = 20
                completeString = ""
                compositionString = composeWordWithIndexes(fcIndex, vIndex, 0) + ""
                ACTION_UPDATE_COMPOSITIONSTR
            } else {
                state = 4
                completeString = compositionString
                compositionString = word + ""
                ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
            }
        }
    }

    /**
     * 조합: single consonant + single vowel + single consonant
     * 예시: 각
     */
    private fun doState03(word: Char): Int {
        Log.v(TAG, "-doState03: 0. word=$word")

        Log.v(TAG, "-doState03: 1. compositionString=${compositionString}")
        if (compositionString === "") {
            return ACTION_ERROR
        }

        Log.v(TAG, "-doState03: 2. isConsonant=${isConsonant(word)}")
        if (isConsonant(word)) {
            val lcIndex = getLastConsonantIndex(compositionString[0])
            Log.v(TAG, "-doState03: 3. lcIndex=$lcIndex")
            if (lcIndex < 0) {
                return ACTION_ERROR
            }

            val newWord = combineLastConsonantWithWord(LastConsonants.Word[lcIndex], word)
            Log.v(TAG, "-doState03: 4. newWord=$newWord")
            return if (newWord != 0.toChar()) { // Last Consonants can be combined
                completeString = ""
                compositionString =
                    (compositionString[0].code - lcIndex + getLastConsonantIndex(newWord)).toChar() + ""
                state = 11
                ACTION_UPDATE_COMPOSITIONSTR
            } else {
                completeString = compositionString
                compositionString = word + ""
                state = 1
                ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
            }
        } else { // vowel
            val lcIndex = getLastConsonantIndex(compositionString[0])
            Log.v(TAG, "-doState03: 3. lcIndex=$lcIndex")
            if (lcIndex < 0) {
                return ACTION_ERROR
            }

            completeString =
                (compositionString[0].code - lcIndex).toChar() + "" // remove last consonant and flush it.
            val fcIndex = getFirstConsonantIndex(LastConsonants.Word[lcIndex])
            Log.v(TAG, " -doState03: 4. fcIndex=$fcIndex")
            if (fcIndex < 0) {
                return ACTION_ERROR
            }

            val vIndex = getVowelIndex(word)
            Log.v(TAG, "-doState03: 5. vIndex=$vIndex")
            compositionString =
                composeWordWithIndexes(fcIndex, vIndex, 0) + "" // compose new composition string
            state = 2
            return ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
        }
    }

    /**
     * 조합: single vowel
     * 예시: ㅏ
     */
    private fun doState04(word: Char): Int {
        Log.v(TAG, "-doState04: 0. word=$word")

        Log.v(TAG, "-doState04: 1. compositionString=$compositionString")
        if (compositionString === "") {
            return ACTION_ERROR
        }

        Log.v(TAG, "-doState04: 2. isConsonant=${isConsonant(word)}")
        if (isConsonant(word)) {
            completeString = compositionString
            compositionString = word + ""
            state = 1
            return ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
        }

        val newWord = combineVowelWithWord(compositionString[0], word)
        Log.v(TAG, "-doState04: 3. newWord=$newWord")
        return if (newWord != 0.toChar()) {
            completeString = ""
            compositionString = newWord + ""
            state = 5
            ACTION_UPDATE_COMPOSITIONSTR
        } else {
            completeString = compositionString
            compositionString = word + ""
            state = 4
            ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
        }
    }

    /**
     * 조합: a combined vowel
     * 예시: ㅘ
     */
    private fun doState05(word: Char): Int {
        Log.v(TAG, "-doState05: 0. word=$word")

        Log.v(TAG, "-doState05: 1. compositionString=$compositionString")
        if (compositionString === "") {
            return ACTION_ERROR
        }

        Log.v(TAG, "-doState05: 2. isConsonant=${isConsonant(word)}")
        return if (isConsonant(word)) {
            completeString = compositionString
            compositionString = word + ""
            state = 1
            ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
        } else {
            completeString = compositionString
            compositionString = word + ""
            state = 4
            ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
        }
    }

    /**
     * 조합: a combined consonant
     * 예시: ㄳ
     */
    private fun doState10(word: Char): Int {
        Log.v(TAG, "-doState10: 0. word=$word")

        Log.v(TAG, "-doState10: 1. compositionString=$compositionString")
        if (compositionString === "") {
            return ACTION_ERROR
        }

        Log.v(TAG, "-doState10: 2. isConsonant=${isConsonant(word)}")
        return if (isConsonant(word)) {
            completeString = compositionString
            compositionString = word + ""
            state = 1
            ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
        } else {
            val lcIndex0 = getLastConsonantIndex(compositionString[0])
            val lcIndex1 = LastConsonants.iLast[lcIndex0]
            val fcIndex = LastConsonants.iFirst[lcIndex0]
            val vIndex = getVowelIndex(word)
            completeString = "${LastConsonants.Code[lcIndex1]}"
            compositionString = composeWordWithIndexes(fcIndex, vIndex, 0) + ""
            state = 2
            ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
        }
    }

    /**
     * 조합: single consonant + single vowel + a combined consonant
     * 예시: 갃
     */
    private fun doState11(word: Char): Int {
        Log.v(TAG, "-doState11: 0. word=$word")

        Log.v(TAG, "-doState11: 1. compositionString=$compositionString")
        if (compositionString === "") {
            return ACTION_ERROR
        }

        Log.v(TAG, "-doState11: 2. isConsonant=${isConsonant(word)}")
        return if (isConsonant(word)) {
            completeString = compositionString
            compositionString = word + ""
            state = 1
            ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
        } else {
            val lcIndex = getLastConsonantIndex(compositionString[0])
            val vIndex = getVowelIndex(compositionString[0])
            val fcIndex = getFirstConsonantIndex(compositionString[0])
            val lcIndexNew = LastConsonants.iLast[lcIndex]
            val vIndexNew = convertVowelToIndex(word)
            val fcIndexNew = LastConsonants.iFirst[lcIndex]
            completeString = composeWordWithIndexes(fcIndex, vIndex, lcIndexNew) + ""
            compositionString = composeWordWithIndexes(fcIndexNew, vIndexNew, 0) + ""
            state = 2
            ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
        }
    }

    /**
     * 조합: single consonant + a combined vowel
     * 예시: 과
     */
    private fun doState20(word: Char): Int {
        Log.v(TAG, "-doState20: 0. word=$word")

        Log.v(TAG, "-doState20: 1. compositionString=$compositionString")
        if (compositionString === "") {
            return ACTION_ERROR
        }

        Log.v(TAG, "-doState20: 2. isConsonant=${isConsonant(word)}")
        if (isConsonant(word)) {
            val lcIndex = convertLastConsonantToIndex(word)
            // cannot compose the code with composition string. flush it.
            return if (lcIndex < 0) {
                completeString = compositionString
                compositionString = word + ""
                state = 1
                ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
            } else { // compose..과
                var newWord = compositionString[0]
                completeString = ""
                compositionString = (newWord.code + lcIndex).toChar() + ""
                state = 21
                ACTION_UPDATE_COMPOSITIONSTR
            }
        } else {
            completeString = compositionString
            compositionString = word + ""
            state = 4
            return ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
        }
    }

    /**
     * 조합: single consonant + a combined vowel + single consonant
     * 예시: 곽
     */
    private fun doState21(word: Char): Int {
        Log.v(TAG, "-doState21: 0. word=$word")

        Log.v(TAG, "-doState21: 1. compositionString=$compositionString")
        if (compositionString === "") {
            return ACTION_ERROR
        }

        Log.v(TAG, "-doState21: 2. isConsonant=${isConsonant(word)}")
        if (isConsonant(word)) {
            val lcIndex = getLastConsonantIndex(compositionString[0])
            val lcIndexTemp = convertLastConsonantToIndex(word)
            if (lcIndexTemp < 0) {
                state = 1
                completeString = compositionString
                compositionString = word + ""
                return ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
            }

            val lcIndexNew = combineLastConsonantWithIndex(lcIndex, lcIndexTemp)
            Log.v(TAG, "-doState21: 3. lcIndexNew=$lcIndexNew")
            return if (lcIndexNew < 0) {
                state = 1
                completeString = compositionString
                compositionString = word + ""
                ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
            } else {
                var newWord = compositionString[0]
                completeString = ""
                compositionString = (newWord.code - lcIndex + lcIndexNew).toChar() + ""
                state = 22
                ACTION_UPDATE_COMPOSITIONSTR
            }
        } else {
            var newWord = compositionString[0]
            val lcIndex = getLastConsonantIndex(newWord)
            val fcIndex = convertFirstConsonantToIndex(LastConsonants.Word[lcIndex])
            val vIndex = convertVowelToIndex(word)
            completeString = (newWord.code - lcIndex).toChar() + ""
            compositionString = composeWordWithIndexes(fcIndex, vIndex, 0) + ""
            state = 2
            return ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
        }
    }

    /**
     * 조합: single consonant + a combined vowel + a combined consonant
     * 예시: 곿
     */
    private fun doState22(word: Char): Int {
        Log.v(TAG, "-doState22: 0. word=$word")

        Log.v(TAG, "-doState22: 1. compositionString=$compositionString")
        if (compositionString === "") {
            return ACTION_ERROR
        }

        Log.v(TAG, "-doState22: 2. isConsonant=${isConsonant(word)}")
        return if (isConsonant(word)) {
            completeString = compositionString
            compositionString = word + ""
            state = 1
            ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
        } else {
            var tempChar = compositionString[0]
            val lcIndex0 = getLastConsonantIndex(tempChar)
            val lcIndex1 = LastConsonants.iLast[lcIndex0]
            val fcIndex = LastConsonants.iFirst[lcIndex0]
            val vIndex = getVowelIndex(word)
            completeString = (tempChar.code - lcIndex0 + lcIndex1).toChar() + ""
            compositionString = composeWordWithIndexes(fcIndex, vIndex, 0) + ""
            state = 2
            ACTION_UPDATE_COMPLETESTR or ACTION_UPDATE_COMPOSITIONSTR
        }
    }

}