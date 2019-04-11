package jp.studyplus.android.app.jasper.demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import jp.studyplus.android.app.jasper.CircleGaugeView

class CircleGaugeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_circle_gauge)
        val sampleView= findViewById<CircleGaugeView>(R.id.circle_gauge_view)
    }
}
