package dev.ran.plugins.flutterex

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.jetbrains.lang.dart.psi.DartFile

/**
 * Trigger auto popup when user types '/'
 */
class CustomTypedHandlerDelegate : TypedHandlerDelegate() {

    private val log = Logger.getInstance(this::class.java)

    override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
        if (file !is DartFile) return Result.CONTINUE
        // TODO: Should only work in a string literal
        if (c == '/') {
            AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
            // Do not return Result.DEFAULT or STOP,
            // otherwise the '/' will not be typed
        }
        return Result.CONTINUE
    }
}