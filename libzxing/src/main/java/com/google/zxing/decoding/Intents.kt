/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.zxing.decoding

import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType

/**
 * This class provides the constants to use when sending an Intent to Barcode Scanner.
 * These strings are effectively API and cannot be changed.
 */
class Intents private constructor() {
    object Scan {
        /**
         * Send this intent to open the Barcodes app in scanning mode, find a barcode, and return
         * the results.
         */
        const val ACTION: String = "com.google.zxing.client.android.SCAN"

        /**
         * By default, sending Scan.ACTION will decode all barcodes that we understand. However it
         * may be useful to limit scanning to certain formats. Use Intent.putExtra(MODE, value) with
         * one of the values below ([.PRODUCT_MODE], [.ONE_D_MODE], [.QR_CODE_MODE]).
         * Optional.
         * 
         * 
         * Setting this is effectively shorthnad for setting explicit formats with [.SCAN_FORMATS].
         * It is overridden by that setting.
         */
        const val MODE: String = "SCAN_MODE"

        /**
         * Comma-separated list of formats to scan for. The values must match the names of
         * [BarcodeFormat]s, such as [BarcodeFormat.EAN_13].
         * Example: "EAN_13,EAN_8,QR_CODE"
         * 
         * 
         * This overrides [.MODE].
         */
        const val SCAN_FORMATS: String = "SCAN_FORMATS"

        /**
         * @see DecodeHintType.CHARACTER_SET
         */
        const val CHARACTER_SET: String = "CHARACTER_SET"

        /**
         * Decode only UPC and EAN barcodes. This is the right choice for shopping apps which get
         * prices, reviews, etc. for products.
         */
        const val PRODUCT_MODE: String = "PRODUCT_MODE"

        /**
         * Decode only 1D barcodes (currently UPC, EAN, Code 39, and Code 128).
         */
        const val ONE_D_MODE: String = "ONE_D_MODE"

        /**
         * Decode only QR codes.
         */
        const val QR_CODE_MODE: String = "QR_CODE_MODE"

        /**
         * Decode only Data Matrix codes.
         */
        const val DATA_MATRIX_MODE: String = "DATA_MATRIX_MODE"

        /**
         * If a barcode is found, Barcodes returns RESULT_OK to onActivityResult() of the app which
         * requested the scan via startSubActivity(). The barcodes contents can be retrieved with
         * intent.getStringExtra(RESULT). If the user presses Back, the result code will be
         * RESULT_CANCELED.
         */
        const val RESULT: String = "SCAN_RESULT"

        /**
         * Call intent.getStringExtra(RESULT_FORMAT) to determine which barcode format was found.
         * See Contents.Format for possible values.
         */
        const val RESULT_FORMAT: String = "SCAN_RESULT_FORMAT"

        /**
         * Setting this to false will not save scanned codes in the history.
         */
        const val SAVE_HISTORY: String = "SAVE_HISTORY"
    }

    object Encode {
        /**
         * Send this intent to encode a piece of data as a QR code and display it full screen, so
         * that another person can scan the barcode from your screen.
         */
        const val ACTION: String = "com.google.zxing.client.android.ENCODE"

        /**
         * The data to encode. Use Intent.putExtra(DATA, data) where data is either a String or a
         * Bundle, depending on the type and format specified. Non-QR Code formats should
         * just use a String here. For QR Code, see Contents for details.
         */
        const val DATA: String = "ENCODE_DATA"

        /**
         * The type of data being supplied if the format is QR Code. Use
         * Intent.putExtra(TYPE, type) with one of Contents.Type.
         */
        const val TYPE: String = "ENCODE_TYPE"

        /**
         * The barcode format to be displayed. If this isn't specified or is blank,
         * it defaults to QR Code. Use Intent.putExtra(FORMAT, format), where
         * format is one of Contents.Format.
         */
        const val FORMAT: String = "ENCODE_FORMAT"
    }

    object SearchBookContents {
        /**
         * Use Google Book Search to search the contents of the book provided.
         */
        const val ACTION: String = "com.google.zxing.client.android.SEARCH_BOOK_CONTENTS"

        /**
         * The book to search, identified by ISBN number.
         */
        const val ISBN: String = "ISBN"

        /**
         * An optional field which is the text to search for.
         */
        const val QUERY: String = "QUERY"
    }

    object WifiConnect {
        /**
         * Internal intent used to trigger connection to a wi-fi network.
         */
        const val ACTION: String = "com.google.zxing.client.android.WIFI_CONNECT"

        /**
         * The network to connect to, all the configuration provided here.
         */
        const val SSID: String = "SSID"

        /**
         * The network to connect to, all the configuration provided here.
         */
        const val TYPE: String = "TYPE"

        /**
         * The network to connect to, all the configuration provided here.
         */
        const val PASSWORD: String = "PASSWORD"
    }


    object Share {
        /**
         * Give the user a choice of items to encode as a barcode, then render it as a QR Code and
         * display onscreen for a friend to scan with their phone.
         */
        const val ACTION: String = "com.google.zxing.client.android.SHARE"
    }
}
