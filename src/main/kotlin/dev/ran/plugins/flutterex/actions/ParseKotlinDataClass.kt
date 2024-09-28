package dev.ran.plugins.flutterex.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.diagnostic.LogLevel
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.KtPsiSourceFile
import org.jetbrains.kotlin.psi.KtFile

class ParseKotlinDataClass : AnAction() {

  private val log = Logger.getInstance(this::class.java)

  init {
    log.setLevel(LogLevel.DEBUG)
  }

  override fun actionPerformed(action: AnActionEvent) {
    log.warn("action")
    val project = action.project
    val editor: Editor = action.dataContext.getData(PlatformDataKeys.EDITOR) ?: return
    val psiFile: PsiFile = action.dataContext.getData(PlatformDataKeys.PSI_FILE) ?: return

    if(psiFile !is KtFile){
      return
    }

    val ktFile = psiFile as KtFile


    log.warn("KtFile: ${psiFile.name}")

    log.warn("PsiFile: ${psiFile.name}")

    val position = editor.caretModel.currentCaret.logicalPosition
    //val element = psiFile.
  }
}