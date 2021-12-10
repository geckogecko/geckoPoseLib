package at.steinbacher.geckoposelib.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import at.steinbacher.geckoposelib.R
import at.steinbacher.geckoposelib.data.Angle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

class GeckoLineChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val lineChart: LineChart

    init {
        LayoutInflater.from(context).inflate(R.layout.view_line_chart, this, true)

        lineChart = findViewById(R.id.line_chart)
    }

    fun setData(entries: List<Entry>, dataName: String) {
        val lineDataSet = LineDataSet(entries, dataName).apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            lineWidth = 3f
        }

        lineChart.data = LineData(lineDataSet)

        lineChart.xAxis.isEnabled = false
        lineChart.axisRight.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.description.isEnabled = false
    }

    fun setOnChartValueSelectedListener(listener: OnChartValueSelectedListener) {
        lineChart.setOnChartValueSelectedListener(listener)
    }

    fun addLimitLine(value: Float, text: String) {
        val limitLine = LimitLine(value, text).apply {
            lineWidth = 3f
            enableDashedLine(10f, 10f, 0f)
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            textSize = 10f
        }

        lineChart.axisLeft.addLimitLine(limitLine)
    }

    fun clearLimitLines() {
        lineChart.axisLeft.removeAllLimitLines()
    }
}