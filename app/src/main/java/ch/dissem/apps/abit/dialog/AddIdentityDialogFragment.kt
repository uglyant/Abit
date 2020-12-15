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

import android.app.AlertDialog
import android.app.FragmentManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import ch.dissem.apps.abit.ImportIdentityActivity
import ch.dissem.apps.abit.MainActivity
import ch.dissem.apps.abit.R
import ch.dissem.apps.abit.service.Singleton
import ch.dissem.bitmessage.BitmessageContext
import ch.dissem.bitmessage.entity.payload.Pubkey
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.xmlpull.v1.XmlPullParserFactory.newInstance

/**
 * @author Christian Basler
 */

class AddIdentityDialogFragment : AppCompatDialogFragment() {
    private lateinit var bmc: BitmessageContext
    private var parent: ViewGroup? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bmc = Singleton.getBitmessageContext(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.setTitle(R.string.add_identity)
        parent = container
        return inflater.inflate(R.layout.dialog_add_identity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.ok).setOnClickListener(View.OnClickListener {
            val ctx = activity?.baseContext ?: throw IllegalStateException("No context available")

            when ( view.findViewById<RadioGroup>(R.id.radioGroup).checkedRadioButtonId) {
                R.id.create_identity -> {
                    Toast.makeText(ctx,
                            R.string.toast_long_running_operation,
                            Toast.LENGTH_SHORT).show()
                    doAsync {
                        val identity = bmc.createIdentity(false, Pubkey.Feature.DOES_ACK)
                        uiThread {
                            Toast.makeText(ctx,
                                    R.string.toast_identity_created,
                                    Toast.LENGTH_SHORT).show()
                            MainActivity.apply {
                                addIdentityEntry(identity)
                            }
                        }
                    }
                }
                R.id.import_identity ->{
                    val intent = Intent(ctx, ImportIdentityActivity::class.java)
                    startActivity(intent)
                }
                R.id.add_chan -> addChanDialog()
                R.id.add_deterministic_address -> fragmentManager?.let { it1 -> DeterministicIdentityDialogFragment().show(it1, "dialog") }
                else -> return@OnClickListener
            }
            dismiss()
        })
        view.findViewById<Button>(R.id.dismiss).setOnClickListener { dismiss() }
    }

    private fun addChanDialog() {
        val activity = activity ?: throw IllegalStateException("No activity available")
        val ctx = activity.baseContext ?: throw IllegalStateException("No context available")
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_input_passphrase, parent)
        AlertDialog.Builder(activity)
                .setTitle(R.string.add_chan)
                .setView(dialogView)
                .setPositiveButton(R.string.ok) { _, _ ->
                    val passphrase = dialogView.findViewById<TextView>(R.id.passphrase)
                    Toast.makeText(ctx, R.string.toast_long_running_operation,
                            Toast.LENGTH_SHORT).show()
                    val pass = passphrase.text.toString()
                    doAsync {
                        val chan = bmc.createChan(pass)
                        chan.alias = pass
                        bmc.addresses.save(chan)
                        uiThread {
                            Toast.makeText(ctx,
                                    R.string.toast_chan_created,
                                    Toast.LENGTH_SHORT).show()
                            MainActivity.apply { addIdentityEntry(chan) }
                        }
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    override fun getTheme() = R.style.FixedDialog

    companion object {
        fun newInstance(): AddIdentityDialogFragment {
            return AddIdentityDialogFragment()
        }
    }
}
