package ch.dissem.apps.abit.util

import android.content.Context
import androidx.annotation.ColorInt

import com.mikepenz.iconics.typeface.IIcon

import ch.dissem.apps.abit.R
import ch.dissem.bitmessage.entity.valueobject.Label
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial

/**
 * Helper class to help with translating the default labels, getting label colors and so on.
 */
object Labels {
    fun getText(label: Label, ctx: Context): String = getText(label.type, label.toString(), ctx)!!

    fun getText(type: Label.Type?, alternative: String?, ctx: Context) = when (type) {
        Label.Type.INBOX -> ctx.getString(R.string.inbox)
        Label.Type.DRAFT -> ctx.getString(R.string.draft)
        Label.Type.OUTBOX -> ctx.getString(R.string.outbox)
        Label.Type.SENT -> ctx.getString(R.string.sent)
        Label.Type.UNREAD -> ctx.getString(R.string.unread)
        Label.Type.TRASH -> ctx.getString(R.string.trash)
        Label.Type.BROADCAST -> ctx.getString(R.string.broadcasts)
        else -> alternative
    }

    fun getIcon(label: Label): IIcon = when (label.type) {
        Label.Type.INBOX -> GoogleMaterial.Icon.gmd_inbox
        Label.Type.DRAFT -> CommunityMaterial.Icon2.cmd_file
        Label.Type.OUTBOX -> CommunityMaterial.Icon2.cmd_inbox_arrow_up
        Label.Type.SENT -> CommunityMaterial.Icon3.cmd_send
        Label.Type.BROADCAST -> CommunityMaterial.Icon3.cmd_rss
        Label.Type.UNREAD -> GoogleMaterial.Icon.gmd_markunread_mailbox
        Label.Type.TRASH -> GoogleMaterial.Icon.gmd_delete
        else -> CommunityMaterial.Icon2.cmd_label
    }

    @ColorInt
    fun getColor(label: Label) = if (label.type == null) {
        label.color
    } else 0xffff00  ///0xFF000000.toInt()
}
