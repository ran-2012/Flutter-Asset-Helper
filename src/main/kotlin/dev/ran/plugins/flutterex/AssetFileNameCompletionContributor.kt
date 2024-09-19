package dev.ran.plugins.flutterex

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.diagnostic.LogLevel
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import com.jetbrains.lang.dart.DartTokenTypes
import com.shenyong.flutter.psi.AssetUtility
import javax.swing.Icon

class AssetFileNameCompletionContributor : CompletionContributor() {

    val log: Logger = Logger.getInstance(this::class.java)

    init {
        log.setLevel(LogLevel.DEBUG)
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        log.debug("fillCompletionVariants")
        if (parameters.position.context.toString() == DartTokenTypes.STRING_LITERAL_EXPRESSION.toString()) {

            AssetFileEntryManager.instance.getCompletionEntries(
                parameters.originalFile.virtualFile,
                result.prefixMatcher.prefix
            ).forEachIndexed { index, it ->
                var element = LookupElementBuilder.create(it.path)
                element = element.withTypeText(getFileType(it.file))
                // Only show the first 10 icons since we may need to load files
                if (index < 10) {
                    element = element.withIcon(getFileIcon(it.file))
                }
                val cr = CompletionResult.wrap(
                    element,
                    result.prefixMatcher,
                    CompletionSorter.defaultSorter(parameters, result.prefixMatcher)
                )
                if (cr != null) {
                    // Using result.addElement(element) will not trigger the sorting.
                    // Since it will be canceled in the coroutine for unknown reason.
                    result.passResult(cr)
                }
            }
        }
    }

    private fun getFileType(file: VirtualFile): String {
        return if (file.isFile) "File"
        else if (AssetUtility.isImage(file)) "Image"
        else "Directory"
    }

    private fun getFileIcon(file: VirtualFile): Icon {
        return if (file.isDirectory) AllIcons.Nodes.Folder
        else if (AssetUtility.isSvg(file)) {
            AssetUtility.loadSvg(file, false)
        } else if (AssetUtility.isImage(file)) AssetUtility.loadImage(file)
        else AllIcons.FileTypes.Any_type
    }
}