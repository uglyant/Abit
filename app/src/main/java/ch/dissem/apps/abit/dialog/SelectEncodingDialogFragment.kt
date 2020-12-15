/*
 * Copyright 2016 Christian Basler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.dissem.apps.abit.dialog

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import ch.dissem.apps.abit.ComposeMessageActivity.Companion.EXTRA_ENCODING
import ch.dissem.apps.abit.R
import ch.dissem.bitmessage.entity.Plaintext
import ch.dissem.bitmessage.entity.Plaintext.Encoding.EXTENDED
import ch.dissem.bitmessage.entity.Plaintext.Encoding.SIMPLE
///import kotlinx.android.synthetic.main.dialog_select_message_encoding.*
import org.slf4j.LoggerFactory

/**
 * @author Christian Basler
 */

class SelectEncodingDialogFragment : AppCompatDialogFragment() {
    private lateinit var encoding: Plaintext.Encoding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        encoding = (arguments?.getSerializable(EXTRA_ENCODING) as? Plaintext.Encoding) ?: SIMPLE
        dialog?.setTitle(R.string.select_encoding_title)
        return inflater.inflate(R.layout.dialog_select_message_encoding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (encoding) {
            SIMPLE -> view.findViewById<RadioGroup>(R.id.radioGroup).check(R.id.simple)
            EXTENDED -> view.findViewById<RadioGroup>(R.id.radioGroup).check(R.id.extended)
            else -> LOG.warn("Unexpected encoding: " + encoding)
        }
        view.findViewById<Button>(R.id.ok).setOnClickListener(View.OnClickListener {
            encoding = when (view.findViewById<RadioGroup>(R.id.radioGroup).checkedRadioButtonId) {
                R.id.extended -> EXTENDED
                R.id.simple -> SIMPLE
                else -> {
                    dismiss()
                    return@OnClickListener
                }
            }
            val result = Intent()
            result.putExtra(EXTRA_ENCODING, encoding)
            targetFragment?.onActivityResult(targetRequestCode, RESULT_OK, result)
            dismiss()
        })
        view.findViewById<Button>(R.id.dismiss).setOnClickListener { dismiss() }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SelectEncodingDialogFragment::class.java)
    }
}
