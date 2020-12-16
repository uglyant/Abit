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

package ch.dissem.apps.abit

import android.app.Dialog
import android.app.FragmentManager
import android.app.ProgressDialog.show
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import ch.dissem.apps.abit.dialog.AddIdentityDialogFragment
import ch.dissem.apps.abit.listener.ListSelectionListener
import ch.dissem.apps.abit.repository.AndroidLabelRepository.Companion.LABEL_ARCHIVE
import ch.dissem.apps.abit.service.Singleton
import ch.dissem.apps.abit.service.Singleton.currentLabel
import ch.dissem.apps.abit.synchronization.SyncAdapter
import ch.dissem.apps.abit.util.Drawables
import ch.dissem.apps.abit.util.Labels
import ch.dissem.apps.abit.util.NetworkUtils
import ch.dissem.apps.abit.util.Preferences
import ch.dissem.bitmessage.BitmessageContext
import ch.dissem.bitmessage.entity.BitmessageAddress
import ch.dissem.bitmessage.entity.Plaintext
import ch.dissem.bitmessage.entity.valueobject.Label
import com.github.amlcurran.showcaseview.ShowcaseView
import com.mikepenz.aboutlibraries.util.MovementCheck.Companion.instance
import com.mikepenz.iconics.IconicsColor
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.dsl.iconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.actionBar
import com.mikepenz.iconics.utils.color
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.iconics.iconicsIcon
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SwitchDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.util.addItemAtPosition
import com.mikepenz.materialdrawer.util.addItems
import com.mikepenz.materialdrawer.util.updateItem
import com.mikepenz.materialdrawer.util.updateStickyFooterItem
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView


import io.github.kobakei.materialfabspeeddial.FabSpeedDial
import org.ini4j.spi.OptionsParser.newInstance

import org.jetbrains.anko.ctx
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.email
import org.jetbrains.anko.uiThread
import java.io.Serializable
import java.lang.ref.WeakReference
import java.lang.reflect.Array.newInstance
import java.net.URLClassLoader.newInstance

/*import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder*/

/**
 * An activity representing a list of Messages. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [MessageDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 *
 *
 * The activity makes heavy use of fragments. The list of items is a
 * [MessageListFragment] and the item details
 * (if present) is a [MessageDetailFragment].
 *
 *
 * This activity also implements the required
 * [ListSelectionListener] interface
 * to listen for item selections.
 *
 */
class MainActivity : AppCompatActivity(), ListSelectionListener<Serializable> {

    private var active: Boolean = false

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    var hasDetailPane: Boolean = false
        private set

    private lateinit var bmc: BitmessageContext
   /// private lateinit var accountHeader: AccountHeader

/*    private lateinit var drawer: Drawer
    private lateinit var nodeSwitch: SwitchDrawerItem*/
    private lateinit var headerView: AccountHeaderView
    private lateinit var slider: MaterialDrawerSliderView
    private lateinit var nodeSwitch: SwitchDrawerItem
    private lateinit var root: DrawerLayout
    val floatingActionButton: FabSpeedDial?
        get() = findViewById(R.id.fab)
    private lateinit var fab:FabSpeedDial
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = WeakReference(this)
        bmc = Singleton.getBitmessageContext(this)
        setContentView(R.layout.activity_main)
        fab = findViewById(R.id.fab)
        fab.hide()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val listFragment = MessageListFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.item_list, listFragment)
            .commit()

        if (findViewById<View>(R.id.message_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            hasDetailPane = true

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            listFragment.setActivateOnItemClick(true)
        }
        ///check(Preferences.isFullNodeActive(this@MainActivity));
        root = findViewById(R.id.root)
        slider = findViewById(R.id.slider)
      ///5
        nodeSwitch =  SwitchDrawerItem ().apply { nameRes = R.string.full_node;
            tag = 10003
            iconicsIcon = CommunityMaterial.Icon.cmd_cloud_outline;isChecked = Preferences.isFullNodeActive(this@MainActivity);

            onCheckedChangeListener.apply { onCheckedChangeListener = object : OnCheckedChangeListener {
                override fun onCheckedChanged(drawerItem: IDrawerItem<*>, buttonView: CompoundButton, isChecked: Boolean) {
                    if (isChecked) {
                        NetworkUtils.enableNode(this@MainActivity)
                    } else {
                        NetworkUtils.disableNode(this@MainActivity)
                    }
                }
            } }}
        createSliderDrawer(toolbar,savedInstanceState)

        // handle intents
        val intent = intent
        if (intent.hasExtra(EXTRA_SHOW_MESSAGE)) {
            onItemSelected(intent.getSerializableExtra(EXTRA_SHOW_MESSAGE))
        }
        if (intent.hasExtra(EXTRA_REPLY_TO_MESSAGE)) {
            val item = intent.getSerializableExtra(EXTRA_REPLY_TO_MESSAGE) as Plaintext
            ComposeMessageActivity.launchReplyTo(this, item)
        }

        if (Preferences.useTrustedNode(this)) {
            SyncAdapter.startSync(this)
        } else {
            SyncAdapter.stopSync(this)
        }
        ///drawer.isDrawerOpen
        if (root.isDrawerOpen(slider)) {
            val lps = RelativeLayout.LayoutParams(
                ViewGroup
                    .LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                val margin = ((resources.displayMetrics.density * 12) as Number).toInt()
                setMargins(margin, margin, margin, margin)
            }

            ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle(R.string.full_node)
                .setContentText(R.string.full_node_description)
                .setTarget {
                    val view = slider.stickyFooterView
                    val location = IntArray(2)
                    view?.getLocationInWindow(location)
                    val x = location[0] + 7 * (view?.width ?: 0) / 8
                    val y = location[1] + (view?.height ?: 0) / 2
                    Point(x, y)
                }
                .replaceEndButton(R.layout.showcase_button)
                .hideOnTouchOutside()
                .build()
                .setButtonPosition(lps)
        }
    }


    private fun addIdentityDialog() {
        val fragmentDialogue = AddIdentityDialogFragment.newInstance()
        fragmentDialogue.show(supportFragmentManager, "dialog")
    }
   /// private fun addIdentityDialog() = AddIdentityDialogFragment().show(fragmentManager, "dialog")

    private fun <F> changeList(listFragment: F) where F : Fragment, F : ListHolder<*> {
        if (active) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.item_list, listFragment)
            supportFragmentManager.findFragmentById(R.id.message_detail_container)?.let {
                transaction.remove(it)
            }
            transaction.addToBackStack(null).commit()

            if (hasDetailPane) {
                // In two-pane mode, list items should be given the
                // 'activated' state when touched.
                listFragment.setActivateOnItemClick(true)
            }
        }
    }

/*    private fun createDrawer(toolbar: Toolbar) {
        val profiles = ArrayList<IProfile<*>>()
        profiles.add(
            ProfileSettingDrawerItem()
                .withName(getString(R.string.add_identity))
                .withDescription(getString(R.string.add_identity_summary))
                .withIcon(
                    IconicsDrawable(this, GoogleMaterial.Icon.gmd_add)
                        .actionBar()
                        .paddingDp(5)
                        .colorRes(R.color.icons)
                )
                .withIdentifier(ADD_IDENTITY.toLong())
        )
        profiles.add(
            ProfileSettingDrawerItem()
                .withName(getString(R.string.manage_identity))
                .withIcon(GoogleMaterial.Icon.gmd_settings)
                .withIdentifier(MANAGE_IDENTITY.toLong())
        )
        // Create the AccountHeader
        accountHeader = AccountHeaderBuilder()
            .withActivity(this)
            .withHeaderBackground(R.drawable.header)
            .withProfiles(profiles)
            .withOnAccountHeaderProfileImageListener(ProfileImageListener(this))
            .withOnAccountHeaderListener(
                ProfileSelectionListener(
                    this@MainActivity,
                    supportFragmentManager
                )
            )
            .build()
        if (profiles.size > 2) { // There's always the add and manage identity items
            accountHeader.setActiveProfile(profiles[0], true)
        }

        val drawerItems = ArrayList<IDrawerItem<*, *>>()
        drawerItems.add(
            PrimaryDrawerItem()
                .withIdentifier(LABEL_ARCHIVE.id as Long)
                .withName(R.string.archive)
                .withTag(LABEL_ARCHIVE)
                .withIcon(CommunityMaterial.Icon.cmd_archive)
        )
        drawerItems.add(DividerDrawerItem())
        drawerItems.add(
            PrimaryDrawerItem()
                .withName(R.string.contacts_and_subscriptions)
                .withIcon(GoogleMaterial.Icon.gmd_contacts)
        )
        drawerItems.add(
            PrimaryDrawerItem()
                .withName(R.string.settings)
                .withIcon(GoogleMaterial.Icon.gmd_settings)
        )

        nodeSwitch = SwitchDrawerItem()
            .withIdentifier(ID_NODE_SWITCH)
            .withName(R.string.full_node)
            .withIcon(CommunityMaterial.Icon.cmd_cloud_outline)
            .withChecked(Preferences.isFullNodeActive(this))
            .withOnCheckedChangeListener { _, _, isChecked ->
                if (isChecked) {
                    NetworkUtils.enableNode(this@MainActivity)
                } else {
                    NetworkUtils.disableNode(this@MainActivity)
                }
            }

        drawer = DrawerBuilder()
            .withActivity(this)
            .withToolbar(toolbar)
            .withAccountHeader(accountHeader)
            .withDrawerItems(drawerItems)
            .addStickyDrawerItems(nodeSwitch)
            .withOnDrawerItemClickListener(DrawerItemClickListener())
            .withShowDrawerOnFirstLaunch(true)
            .build()

        loadDrawerItemsAsynchronously()
    }*/

    private fun createSliderDrawer(toolbar: Toolbar, savedInstanceState: Bundle?) {


        val profile = ProfileDrawerItem().apply { nameText = getString(R.string.add_identity); descriptionText = getString(R.string.add_identity_summary);
            iconDrawable =  IconicsDrawable(this@MainActivity, GoogleMaterial.Icon.gmd_add).apply { actionBar(); paddingDp = 5 ;colorRes = R.color.icons};
            identifier = ADD_IDENTITY.toLong() }
        val profile2 = ProfileDrawerItem().apply { nameText = getString(R.string.manage_identity);iconicsIcon = GoogleMaterial.Icon.gmd_settings; identifier = MANAGE_IDENTITY.toLong() }
///4
        headerView = AccountHeaderView(this).apply {
            attachToSliderView(slider)
            headerBackground = ImageHolder(R.drawable.header)
            addProfiles(
                profile,
                profile2)

            onAccountHeaderProfileImageListener = { view, profile, current ->
                if (current) {
                    //  Show QR code in modal dialog
                    val dialog = Dialog(ctx)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

                    val imageView = ImageView(ctx)
                    imageView.setImageBitmap(Drawables.qrCode(Singleton.getIdentity(ctx)))
                    imageView.setOnClickListener { dialog.dismiss() }
                    dialog.addContentView(
                        imageView,
                        RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                    val window = dialog.window
                    if (window != null) {
                        val display = window.windowManager.defaultDisplay
                        val size = Point()
                        display.getSize(size)
                        val dim = if (size.x < size.y) size.x else size.y

                        val lp = WindowManager.LayoutParams()
                        lp.copyFrom(window.attributes)
                        lp.width = dim
                        lp.height = dim

                        window.attributes = lp
                    }
                    dialog.show()

                }
                false
            }

            onAccountHeaderListener = { view, profile, current ->
                //sample usage of the onProfileChanged listener
                //if the clicked item has the identifier 1 add a new profile ;)
                when (profile.identifier.toInt()) {
                    ADD_IDENTITY -> addIdentityDialog()
                    MANAGE_IDENTITY -> {
                        val identity = Singleton.getIdentity(ctx)
                        if (identity == null) {
                            Toast.makeText(ctx, R.string.no_identity_warning, Toast.LENGTH_LONG).show()
                        } else {
                            val show = Intent(ctx, AddressDetailActivity::class.java)
                            show.putExtra(AddressDetailFragment.ARG_ITEM, identity)
                            ctx.startActivity(show)
                        }
                    }
                    else -> if (profile is ProfileDrawerItem) {
                        val tag = profile.tag
                        if (tag is BitmessageAddress) {
                            Singleton.setIdentity(tag)
                        }
                    }
                }
                // false if it should close the drawer
                false
            }
            withSavedInstance(savedInstanceState)
        }


        slider.apply {
            addItems(
                PrimaryDrawerItem().apply { nameRes = R.string.archive; tag = LABEL_ARCHIVE;iconicsIcon = CommunityMaterial.Icon.cmd_archive; isSelectable = false; identifier = LABEL_ARCHIVE.id as Long },////3
                DividerDrawerItem(),
                PrimaryDrawerItem().apply { nameRes = R.string.contacts_and_subscriptions; tag = 10001; iconicsIcon = GoogleMaterial.Icon.gmd_contacts; isSelectable = false;  },////222
                PrimaryDrawerItem().apply { nameRes = R.string.settings; tag = 10002; iconicsIcon = GoogleMaterial.Icon.gmd_settings; isSelectable = false; },///1111
                nodeSwitch)

                    onDrawerItemClickListener = { _, item, _ ->
                    //check if the drawerItem is set.
                    //there are different reasons for the drawerItem to be null
                    //--> click on the header
                    //--> click on the footer
                    //those items don't contain a drawerItem

                    val itemList = supportFragmentManager.findFragmentById(R.id.item_list)
                    val tag = item.tag
                    if (tag is Label) {
                        currentLabel.value = tag
                        if (itemList !is MessageListFragment) {
                            changeList(MessageListFragment())
                        }

                    } else if (item is Nameable) {
                        when (item.tag) {
                            /*R.string.contacts_and_subscriptions*/ 10001 -> {
                                if (itemList is AddressListFragment) {
                                    itemList.updateList()
                                } else {
                                    changeList(AddressListFragment())
                                }

                            }
                            /*StringHolder(R.string.settings)*/10002 -> {
                                supportFragmentManager
                                    .beginTransaction()
                                    .replace(R.id.item_list, SettingsFragment())
                                    .addToBackStack(null)
                                    .commit()

                            }
                            /*StringHolder( R.string.full_node)*/10003 ->  true
                            else -> false
                        }
                    }
                    false
                }
            setSavedInstance(savedInstanceState)
        }
        loadDrawerItemsAsynchronously()
    }

    private fun loadDrawerItemsAsynchronously() {
        doAsync {
            val identities = bmc.addresses.getIdentities()
            if (identities.isEmpty()) {
                // Create an initial identity
                Singleton.getIdentity(this@MainActivity)
            }

            uiThread {
                for (identity in identities) {
                    addIdentityEntry(identity)
                }
            }
        }

        doAsync {
            val labels = bmc.labels.getLabels()

            uiThread {
                if (intent.hasExtra(EXTRA_SHOW_LABEL)) {
                    currentLabel.value = intent.getSerializableExtra(EXTRA_SHOW_LABEL) as Label
                } else if (currentLabel.value == null) {
                    currentLabel.value = labels[0]
                }
                for (label in labels) {
                    addLabelEntry(label)
                }
                currentLabel.value?.let {
                    slider.setSelection(it.id as Long)
                }
                updateUnread()
            }
        }
    }

    override fun onBackPressed() {
        val listFragment = supportFragmentManager.findFragmentById(R.id.item_list)
        if (listFragment !is ListHolder<*> || !listFragment.showPreviousList()) {
            super.onBackPressed()
        }
    }

   /* private inner class DrawerItemClickListener : Drawer.OnDrawerItemClickListener {
        override fun onItemClick(view: View?, position: Int, item: IDrawerItem<*, *>): Boolean {
            val itemList = supportFragmentManager.findFragmentById(R.id.item_list)
            val tag = item.tag
            if (tag is Label) {
                currentLabel.value = tag
                if (itemList !is MessageListFragment) {
                    changeList(MessageListFragment())
                }
                return false
            } else if (item is Nameable) {
                when (item.name.textRes) {
                    R.string.contacts_and_subscriptions -> {
                        if (itemList is AddressListFragment) {
                            itemList.updateList()
                        } else {
                            changeList(AddressListFragment())
                        }
                        return false
                    }
                    R.string.settings -> {
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.item_list, SettingsFragment())
                            .addToBackStack(null)
                            .commit()
                        return false
                    }
                    R.string.full_node -> return true
                    else -> return false
                }
            }
            return false
        }
    }*/

    override fun onResume() {
        updateUnread()
        if (Preferences.isFullNodeActive(this) && Preferences.isConnectionAllowed(this@MainActivity)) {
            NetworkUtils.enableNode(this, false)
        }
        Singleton.getMessageListener(this).resetNotification()
        currentLabel.addObserver(this) { label ->
            if (label != null && label.id is Long) {
                slider.setSelection(label.id as Long)
            }
        }
        active = true
        super.onResume()
    }

    override fun onPause() {
        currentLabel.removeObserver(this)
        super.onPause()
        active = false
    }

    fun addIdentityEntry(identity: BitmessageAddress) {
        val newProfile = ProfileDrawerItem().apply {
            iconDrawable = Identicon(identity)
            nameText = identity.toString()
            isNameShown = true
            description = StringHolder(identity.address)
            tag = identity
        }
/*            .withIcon(Identicon(identity))
            .withName(identity.toString())
            .withNameShown(true)
            .withEmail(identity.address)
            .withTag(identity)*/
        if (headerView.profiles != null) {
            // we know that there are 2 setting elements.
            // Set the new profile above them ;)
           /* accountHeader.addProfile(
                newProfile, accountHeader.profiles.size - 2
            )*/
            headerView.addProfile(
                newProfile, headerView.profiles!!.size - 2
            )
        } else {
            headerView.addProfiles(newProfile)
        }
    }

    private fun addLabelEntry(label: Label) {
       /* PrimaryDrawerItem().apply {  name = StringHolder(label.toString());  iconicsIcon = GoogleMaterial.Icon.gmd_settings;
            iconColor  = Labels.getColor(label); isSelectable = false; }*/
        val item = PrimaryDrawerItem().apply {
            name = StringHolder(label.toString());
                iconDrawable = IconicsDrawable(this@MainActivity, Labels.getIcon(label)).apply {
                    color = IconicsColor.colorInt(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
                tag =label
                icon
                identifier = label.id as Long}
            }
/*        val item = PrimaryDrawerItem()
            .withIdentifier(label.id as Long)
            .withName(label.toString())
            .withTag(label)
            .withIcon(Labels.getIcon(label))
            .withIconColor(Labels.getColor(label))*/
        /*drawer.addItemAtPosition(item, drawer.drawerItems.size - 3)*/
        slider.addItemAtPosition(slider.itemAdapter.adapterItemCount - 3,item )
    }

    fun updateIdentityEntry(identity: BitmessageAddress) {
        for (profile in headerView.profiles!!) {
            if (profile is ProfileDrawerItem) {
                if (identity == profile.tag) {
                    profile
                        .withName(identity.toString())
                        .withTag(identity)
                    return
                }
            }
        }
    }

    fun removeIdentityEntry(identity: BitmessageAddress) {
        for (profile in headerView.profiles!!) {
            if (profile is ProfileDrawerItem) {
                if (identity == profile.tag) {
                    headerView.removeProfile(profile)
                    return
                }
            }
        }
    }

    fun updateUnread() {
       /// drawer.drawerItems
        for (item in slider.itemAdapter.adapterItems) {
            if (item.tag is Label) {
                val label = item.tag as Label
                if (label !== LABEL_ARCHIVE) {
                    val unread = bmc.messages.countUnread(label)
                    if (unread > 0) {
                        (item as PrimaryDrawerItem).badge = StringHolder("");
                        ///(item as PrimaryDrawerItem).withBadge(unread.toString())
                    } else {
                        (item as PrimaryDrawerItem).badge = StringHolder("");
                        ///(item as PrimaryDrawerItem).withBadge(null as String?)
                    }
                    slider.updateItem(item)
                }
            }
        }
    }

    /**
     * Callback method from [ListSelectionListener]
     * indicating that the item with the given ID was selected.
     */
    override fun onItemSelected(item: Serializable) {
        if (hasDetailPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            val fragment = when (item) {
                is Plaintext -> {
                    if (item.labels.any { it.type == Label.Type.DRAFT }) {
                        ComposeMessageFragment().apply {
                            arguments = Bundle().apply {
                                putSerializable(ComposeMessageActivity.EXTRA_DRAFT, item)
                            }
                        }
                    } else {
                        MessageDetailFragment().apply {
                            arguments = Bundle().apply {
                                putSerializable(MessageDetailFragment.ARG_ITEM, item)
                            }
                        }
                    }
                }
                is BitmessageAddress -> {
                    AddressDetailFragment().apply {
                        arguments = Bundle().apply {
                            putSerializable(AddressDetailFragment.ARG_ITEM, item)
                        }
                    }
                }
                else -> throw IllegalArgumentException("Plaintext or BitmessageAddress expected, but was ${item::class.simpleName}")
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.message_detail_container, fragment)
                .commit()
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            val detailIntent = when (item) {
                is Plaintext -> {
                    if (item.labels.any { it.type == Label.Type.DRAFT }) {
                        Intent(this, ComposeMessageActivity::class.java).apply {
                            putExtra(ComposeMessageActivity.EXTRA_DRAFT, item)
                        }
                    } else {
                        Intent(this, MessageDetailActivity::class.java).apply {
                            putExtra(MessageDetailFragment.ARG_ITEM, item)
                        }
                    }
                }
                is BitmessageAddress -> Intent(this, AddressDetailActivity::class.java).apply {
                    putExtra(AddressDetailFragment.ARG_ITEM, item)
                }
                else -> throw IllegalArgumentException("Plaintext or BitmessageAddress expected, but was ${item::class.simpleName}")
            }
            startActivity(detailIntent)
        }
    }

    fun setDetailView(fragment: Fragment) {
        if (hasDetailPane) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.message_detail_container, fragment)
                .commit()
        }
    }

    fun updateTitle(title: CharSequence) {
        supportActionBar?.title = title
    }

    companion object {
        const val EXTRA_SHOW_MESSAGE = "ch.dissem.abit.ShowMessage"
        const val EXTRA_SHOW_LABEL = "ch.dissem.abit.ShowLabel"
        const val EXTRA_REPLY_TO_MESSAGE = "ch.dissem.abit.ReplyToMessage"
        const val ACTION_SHOW_INBOX = "ch.dissem.abit.ShowInbox"

        const val ADD_IDENTITY = 1
        const val MANAGE_IDENTITY = 2

        private const val ID_NODE_SWITCH: Long = 1

        private var instance: WeakReference<MainActivity>? = null

        fun updateNodeSwitch() {
            apply {
                runOnUiThread {
                    nodeSwitch.withChecked(Preferences.isFullNodeActive(this))
                    ////slider.updateStickyFooterItem(nodeSwitch)
                }
            }
        }

        /**
         * Runs the given code in the main activity context, if it currently exists. Otherwise,
         * it's ignored.
         */
        fun apply(run: MainActivity.() -> Unit) {
            instance?.get()?.let { run.invoke(it) }
        }
    }
}
