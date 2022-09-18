package com.example.explorecanvas

import android.content.Context
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import androidx.core.graphics.toRectF
import com.google.android.material.snackbar.Snackbar


const val ORIGINAL_WIDTH = 960
const val ORIGINAL_HEIGHT = 1080


class HighLightedImageView: androidx.appcompat.widget.AppCompatImageView{
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    private val textDataList: MutableList<TextData> = mutableListOf()
    private var onWordListener: (TextData) -> Unit = {}

    fun setTextDataList(textDataList: List<TextData>){
        this.textDataList.clear()
        this.textDataList.addAll(textDataList)
        invalidate()
    }

    fun setOnWordListener(listener: (TextData) -> Unit) {
        this.onWordListener = listener
    }

    private val paint = Paint().apply{
        color = Color.argb(200, 100, 100, 100)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val widthRatio = width.toFloat() / ORIGINAL_WIDTH
        val heightRatio = height.toFloat() / ORIGINAL_HEIGHT

        textDataList.forEach { textData ->
            if (textData.isActive){
                canvas?.drawRect(textData.deviceRect(widthRatio, heightRatio), paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            activateText(event.x, event.y)
        }

        if (event?.action == MotionEvent.ACTION_UP){
            decativateAll()
        }
        return true
    }

    private fun decativateAll() {
        setTextDataList(
            textDataList.map{
                it.copy(isActive = false)
            }
        )
    }

    private fun activateText(x: Float, y: Float) {
        val widthRatio = width.toFloat() / ORIGINAL_WIDTH
        val heightRatio = height.toFloat() / ORIGINAL_HEIGHT

        val originX = x / widthRatio
        val originY = y / heightRatio

        setTextDataList(
            textDataList.map{
                if (it.rect.contains(originX, originY)){
                    onWordListener.invoke(it)
                    it.copy(isActive = true)
                } else {
                    it
                }
            }
        )

    }


}


data class TextData(
    val text: String,
    val rect: RectF,
    val isActive: Boolean = false
) {
    fun deviceRect(xRatio: Float, yRatio: Float): RectF =  RectF(
        rect.left * xRatio,
        rect.top * yRatio,
        rect.right * xRatio,
        rect.bottom * yRatio
    )
}

val bbox = """Pillow,100,100,226,154
is,238,100,268,154
the,280,100,354,154
friendly,366,100,539,154
PIL,551,100,610,154
fork,100,164,193,218
by,205,164,263,218
Alex,275,164,373,218
Clark,385,164,505,218
and,517,164,607,218
Contributors.,100,228,401,282
PIL,413,228,472,282
is,484,228,514,282
the,526,228,600,282
Python,100,292,254,346
Imaging,266,292,455,346
Library,467,292,630,346
by,642,292,700,346
Fredrik,100,356,260,410
Lundh,272,356,406,410
and,418,356,508,410
Contributors.,100,420,401,474
""".trimIndent()

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val image = findViewById<HighLightedImageView>(R.id.image)
        // Contributors.,100,420,401,474
        val contributors = RectF(100f, 420f, 401f, 474f)

        val textDataList: List<TextData> = bbox.split("\n").map{ row ->
            val (text, left, top, right, bottom) = row.split(",")
            TextData(text, Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt()).toRectF())
        }

        image.setTextDataList(textDataList)
        image.setOnWordListener {
                Snackbar.make(image, it.text, Snackbar.LENGTH_SHORT).show()
        }

    }
}