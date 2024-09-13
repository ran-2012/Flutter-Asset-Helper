package dev.ran.plugins.flutterex.listeners

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.isFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import dev.ran.plugins.flutterex.AssetFileNameCompletionContributor

class AssetFileListener : BulkFileListener {
    val log = Logger.getInstance(this::class.java)
    override fun after(events: MutableList<out VFileEvent>) {
        // TODO: Update asset file cache
    }
}