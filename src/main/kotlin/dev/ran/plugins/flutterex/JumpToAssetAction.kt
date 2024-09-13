package dev.ran.plugins.flutterex

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages

class JumpToAssetAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val asm = AssetFileEntryManager.instance
        val currentProject = e.project ?: return
        val editor = e.getData(PlatformDataKeys.EDITOR) ?: return
        val currentFile = e.getData(PlatformDataKeys.PSI_FILE)?.virtualFile ?: return

        val assetFilePath = editor.caretModel.currentCaret.toString()
        if (!assetFilePath.startsWith("assets")) return
        val assetFile = asm.getAssetFile(currentFile, assetFilePath)

        if (assetFile != null) {
            val project = e.project ?: return
            val fileEditorManager = FileEditorManager.getInstance(project)
            fileEditorManager.openFile(assetFile, true)
        }else{
            Messages.showMessageDialog(currentProject, "Asset file not found.", e.presentation.text, Messages.getInformationIcon());
        }
    }
}