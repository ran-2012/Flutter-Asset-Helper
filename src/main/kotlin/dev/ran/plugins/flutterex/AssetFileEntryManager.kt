package dev.ran.plugins.flutterex

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import com.jetbrains.lang.dart.psi.DartFile
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import java.nio.file.Path

class AssetFileEntryManager {
    companion object {
        val log: Logger = Logger.getInstance(AssetFileEntryManager::class.java)

        @JvmStatic
        val instance by lazy {
            AssetFileEntryManager()
        }

        @JvmStatic
        val ASSET_DIRECTORY_NAME = "assets"
    }

    private fun getCurrentDartProjectRoot(currentFile: VirtualFile): VirtualFile? {
        assert(currentFile.isFile)
        assert(currentFile.name.endsWith(".dart"))
        var parent = currentFile.parent
        while (parent != null) {
            if (parent.findChild("pubspec.yaml") != null) {
                return parent
            }
            parent = parent.parent
        }
        return null
    }

    private fun createEntry(path: String, file: VirtualFile): AssetFileCompletionEntry {
        return AssetFileCompletionEntry(path, file)
    }

    fun getAssetFile(currentFile: VirtualFile, path: String): VirtualFile? {
        val projectRoot = getCurrentDartProjectRoot(currentFile) ?: return null
        return projectRoot.findFileByRelativePath(path)
    }

    fun getCompletionEntries(currentFile: VirtualFile, prefix: String): List<AssetFileCompletionEntry> {
        val projectRoot = getCurrentDartProjectRoot(currentFile) ?: return emptyList()
        val projectRootUri = projectRoot.toNioPath().toUri()
        val assetRoot = projectRoot.findChild(ASSET_DIRECTORY_NAME) ?: return emptyList()
        val asserRootUri = assetRoot.toNioPath().toUri()

        val res = ArrayList<AssetFileCompletionEntry>()

        val dirs = prefix.split('/')

        if (dirs.isEmpty()) return listOf(createEntry(ASSET_DIRECTORY_NAME, assetRoot))

        if (prefix.endsWith("/")) {
            // List all files in the directory
            val prefixDir = projectRoot.findFileByRelativePath(prefix) ?: return emptyList()
            prefixDir.children.forEach {
                val childrenUri = it.toNioPath().toUri()
                res.add(createEntry("${projectRootUri.relativize(childrenUri)}", it))
            }
        } else {
            if (dirs.size == 1) {
                // "assets"
                return listOf(createEntry(ASSET_DIRECTORY_NAME, assetRoot))
            }
            // List all files in parent directory
            val prefixDir = projectRoot.findFileByRelativePath(dirs.subList(0, dirs.size - 1).joinToString("/"))
                ?: return emptyList()
            prefixDir.children.forEach {
                val childrenUri = it.toNioPath().toUri()
                res.add(createEntry("${projectRootUri.relativize(childrenUri)}", it))
            }
        }

        return res.map {
            if (it.path.endsWith("/")) {
                it.path = it.path.substring(0, it.path.length - 1)
            }
            it
        }

        // TODO: We should cache the result or cache whole directory, but currently, just make it work firstly
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    val context = newSingleThreadContext("AssetFileEntryManager")

    private val assetDirectory = HashMap<String, FileEntry>()

    private fun initAssetDirectory(vf: VirtualFile) {
    }

    private fun resolveAssetRootDirectory(root: VirtualFile, parent: FileEntry) {
        val rootUri = root.toNioPath().toUri()
        VfsUtilCore.iterateChildrenRecursively(
            root, null
        ) { file ->
            val fileUri = file.toNioPath().toUri()

            val relative = rootUri.relativize(fileUri)

            val dirs = relative.toString().split('/')

            var temp = parent;
            for (i in dirs.indices) {
                val dir = dirs[i]
                if (i == dirs.size - 1) {
                    temp.child[dir] = FileEntry(dir, FileType.FILE)
                } else {
                    if (temp.child[dir] == null) {
                        temp.child[dir] = FileEntry(dir, FileType.DIRECTORY)
                    }
                    temp = temp.child[dir]!!
                }
            }

            true
        }

    }
}

data class AssetFileCompletionEntry(var path: String, val file: VirtualFile)

internal class AssetRootEntry(val path: Path, fileEntry: FileEntry)

internal enum class FileType {
    FILE,
    DIRECTORY,
}

internal class FileEntry(
    val name: String = "",
    val type: FileType = FileType.FILE,
    val child: HashMap<String, FileEntry> = HashMap<String, FileEntry>()
)