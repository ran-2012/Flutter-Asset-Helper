import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame
import javax.swing.UIManager

class MyApplicationComponent : LafManagerListener, ApplicationActivationListener{
    override fun lookAndFeelChanged(p0: LafManager) {

    }

    override fun applicationActivated(ideFrame: IdeFrame) {
        super.applicationActivated(ideFrame)
    }
}