/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 12/3/19.
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
package com.breadwallet.ui.importwallet

import com.breadwallet.crypto.Amount
import com.breadwallet.tools.util.BRConstants.FAQ_IMPORT_WALLET
import com.breadwallet.ui.navigation.NavEffectHolder
import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.util.CurrencyCode
import java.math.BigDecimal

object Import {
    data class M(
        val privateKey: String? = null,
        val keyPassword: String? = null,
        val keyRequiresPassword: Boolean = false,
        val isKeyValid: Boolean = false,
        val loadingState: LoadingState = LoadingState.IDLE,
        val currencyCode: CurrencyCode? = null
    ) {
        enum class LoadingState {
            IDLE, VALIDATING, ESTIMATING, SUBMITTING
        }

        val isLoading: Boolean =
            loadingState != LoadingState.IDLE

        fun reset(): M = copy(
            privateKey = null,
            keyPassword = null,
            keyRequiresPassword = false,
            isKeyValid = false,
            loadingState = LoadingState.IDLE
        )

        override fun toString(): String {
            return "M(privateKey='***', " +
                "keyPassword='***', " +
                "isKeyPasswordProtected=$keyRequiresPassword, " +
                "isKeyValid=$isKeyValid, " +
                "isLoading=$isLoading)"
        }

        companion object {
            fun createDefault(
                privateKey: String? = null,
                isPasswordProtected: Boolean = false
            ): M = M(
                privateKey = privateKey,
                keyRequiresPassword = isPasswordProtected,
                loadingState = if (privateKey != null) {
                    LoadingState.VALIDATING
                } else {
                    LoadingState.IDLE
                }
            )
        }
    }

    sealed class E {

        object OnFaqClicked : E()
        object OnScanClicked : E()
        object OnCloseClicked : E()

        object OnImportConfirm : E()
        object OnImportCancel : E()

        data class OnPasswordEntered(
            val password: String
        ) : E() {
            override fun toString() =
                "OnPasswordEntered()"
        }

        data class RetryImport(
            val privateKey: String,
            val password: String?
        ) : E() {
            override fun toString() =
                "RetryImport()"
        }

        data class OnKeyScanned(
            val privateKey: String,
            val isPasswordProtected: Boolean
        ) : E() {
            override fun toString() = "OnKeyScanned()"
        }

        sealed class Key : E() {
            object NoWallets : Key()
            object OnInvalid : Key()
            object OnPasswordInvalid : Key()
            data class OnValid(
                val isPasswordProtected: Boolean = false
            ) : Key()
        }

        sealed class Estimate : E() {
            data class Success(
                val balance: Amount,
                val feeAmount: Amount,
                val currencyCode: CurrencyCode
            ) : Estimate()

            data class FeeError(
                val balance: BigDecimal
            ) : Estimate()

            object NoBalance : Estimate()
            data class BalanceTooLow(
                val balance: BigDecimal
            ) : Estimate()
        }

        sealed class Transfer : E() {
            data class OnSuccess(
                val transferHash: String,
                val currencyCode: CurrencyCode
            ) : Transfer()

            object OnFailed : Transfer()
        }
    }

    sealed class F {

        object ShowKeyInvalid : F()
        object ShowPasswordInvalid : F()
        object ShowPasswordInput : F()
        object ShowBalanceTooLow : F()
        object ShowNoWalletsEnabled : F()
        object ShowNoBalance : F()
        object ShowImportFailed : F()
        object ShowImportSuccess : F()

        data class ShowConfirmImport(
            val receiveAmount: String,
            val feeAmount: String
        ) : F()

        data class ValidateKey(
            val privateKey: String,
            val password: String?
        ) : F() {
            override fun toString() = "ValidateKey(***)"
        }

        data class SubmitImport(
            val privateKey: String,
            val password: String?,
            val currencyCode: CurrencyCode
        ) : F() {
            override fun toString() = "SubmitImport()"
        }

        sealed class Nav : F(), NavEffectHolder {
            object GoBack : Nav() {
                override val navigationEffect =
                    NavigationEffect.GoBack
            }

            object GoToFaq : Nav() {
                override val navigationEffect =
                    NavigationEffect.GoToFaq(FAQ_IMPORT_WALLET)
            }

            object GoToScan : Nav() {
                override val navigationEffect =
                    NavigationEffect.GoToQrScan
            }
        }

        sealed class EstimateImport : F() {
            abstract val privateKey: String

            data class Key(
                override val privateKey: String
            ) : EstimateImport() {
                override fun toString() =
                    "EstimateImport.Key()"
            }

            data class KeyWithPassword(
                override val privateKey: String,
                val password: String
            ) : EstimateImport() {
                override fun toString() =
                    "EstimateImport.KeyWithPassword()"
            }
        }
    }
}