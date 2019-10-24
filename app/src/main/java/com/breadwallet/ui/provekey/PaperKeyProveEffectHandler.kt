/**
 * BreadWallet
 *
 * Created by Pablo Budelli on <pablo.budelli@breadwallet.com> 10/10/19.
 * Copyright (c) 2019 breadwallet LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.breadwallet.ui.provekey

import com.breadwallet.tools.manager.BRSharedPrefs
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PaperKeyProveEffectHandler(
    private val output: Consumer<PaperKeyProveEvent>,
    private val shakeFirstWord: () -> Unit,
    private val shakeSecondWord: () -> Unit,
    private val showBrdSignal: () -> Unit
) : Connection<PaperKeyProveEffect>, CoroutineScope {

    override val coroutineContext = SupervisorJob() + Dispatchers.Default

    override fun accept(value: PaperKeyProveEffect) {
        when (value) {
            PaperKeyProveEffect.StoreWroteDownPhrase -> {
                BRSharedPrefs.putPhraseWroteDown(check = true)
                output.accept(PaperKeyProveEvent.OnWroteDownKeySaved)
                launch(Dispatchers.Main) { showBrdSignal() }
            }
            is PaperKeyProveEffect.ShakeWords -> shakeWords(value)
        }
    }

    override fun dispose() {
        coroutineContext.cancel()
    }

    private fun shakeWords(value: PaperKeyProveEffect.ShakeWords) {
        if (value.first) {
            shakeFirstWord()
        }
        if (value.second) {
            shakeSecondWord()
        }
    }
}