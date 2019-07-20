package elamien.abdullah.socialnote.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.github.irshulx.models.EditorTextStyle
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.databinding.ActivityAddNoteBinding


class AddNoteActivity : AppCompatActivity() {


    private lateinit var mBinding: ActivityAddNoteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_note)
        mBinding.editorToolbar.handlers = this
        mBinding.editor.render()
    }

    fun onBoldClick(view: View) {
        mBinding.editor.updateTextStyle(EditorTextStyle.BOLD)
    }

    fun onItalicClick(view: View) {
        mBinding.editor.updateTextStyle(EditorTextStyle.ITALIC)
    }

    fun onHeading1Click(view: View) {
        mBinding.editor.updateTextStyle(EditorTextStyle.H1)
    }

    fun onHeading2Click(view: View) {
        mBinding.editor.updateTextStyle(EditorTextStyle.H2)
    }

    fun onHeading3Click(view: View) {
        mBinding.editor.updateTextStyle(EditorTextStyle.H3)
    }


    fun onIndentClick(view: View) {
        mBinding.editor.updateTextStyle(EditorTextStyle.INDENT)
    }

    fun onOutdentClick(view: View) {
        mBinding.editor.updateTextStyle(EditorTextStyle.OUTDENT)
    }

    fun onBulletsClick(view: View) {
        mBinding.editor.insertList(false)
    }

    fun onNumbersClick(view: View) {
        mBinding.editor.insertList(true)
    }

    fun onAddDividerClick(view: View) {
        mBinding.editor.insertDivider()
    }

    fun onBlockQuoteClick(view: View) {
        mBinding.editor.updateTextStyle(EditorTextStyle.BLOCKQUOTE)
    }

    fun onHighlightClick(view: View) {
        mBinding.editor.updateTextColor("#8e0000")
    }

    fun onInsertImageClick(view: View) {
    }

    fun onInsertLickClick(view: View) {
        mBinding.editor.insertLink()
    }
}
