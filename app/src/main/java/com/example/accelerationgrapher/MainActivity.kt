package com.example.accelerationgrapher

import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import com.jjoe64.*
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.Viewport
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private lateinit var txt_currentAccel:TextView
    private lateinit var txt_prevAccel:TextView
    private lateinit var txt_accel: TextView
    private lateinit var prog_shakeMeter:ProgressBar
    private lateinit var graph: GraphView

    private var accelerationCurrentValue: Double = 0.0
    private var accelerationPreviousValue: Double = 0.0

    private var series = LineGraphSeries<DataPoint>()
    private var pointsPlotted = 0
    private var graphIntervalCounter = 0
    private lateinit var viewport: Viewport

    private var sensorEventListener:SensorEventListener = object: SensorEventListener{
        override fun onSensorChanged(event: SensorEvent?) {
            if(event!=null){
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                accelerationCurrentValue = sqrt((x*x+y*y+z*z)).toDouble()
                var changeInAcceleration = abs(accelerationCurrentValue-accelerationPreviousValue)
                accelerationPreviousValue = accelerationCurrentValue
                txt_currentAccel.text = "Current = ${accelerationCurrentValue.toInt()}"
                txt_prevAccel.text = "Previous = ${accelerationPreviousValue.toInt()}"
                txt_accel.text = "Acceleration change = ${changeInAcceleration.toInt()}"

                prog_shakeMeter.progress = changeInAcceleration.toInt()
                if (changeInAcceleration>14){
                    txt_accel.setBackgroundColor(Color.RED)
                }else if (changeInAcceleration > 5){
                    txt_accel.setBackgroundColor(Color.parseColor("#fcad03"))
                }else if (changeInAcceleration > 2){
                    txt_accel.setBackgroundColor(Color.YELLOW)
                }else{
                    txt_accel.setBackgroundColor(resources.getColor(com.google.android.material.R.color.design_default_color_background))
                }
                pointsPlotted++
                if (pointsPlotted>1000){
                    pointsPlotted=1
                    series.resetData(arrayOf())
                }

                series.appendData(DataPoint(pointsPlotted.toDouble(),changeInAcceleration),true,pointsPlotted)
                viewport.setMaxX(pointsPlotted.toDouble())
                viewport.setMinX((pointsPlotted-200).toDouble())
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            TODO("Not yet implemented")
        }

    }

    private lateinit var mSensorManager: SensorManager
    private lateinit var mAcceleration: Sensor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txt_currentAccel=findViewById(R.id.txt_currentAccel)
        txt_prevAccel=findViewById(R.id.txt_prevAccel)
        txt_accel=findViewById(R.id.txt_accel)
        prog_shakeMeter=findViewById(R.id.prog_shakeMeter)
        graph=findViewById(R.id.graph)

        viewport=graph.viewport
        viewport.isScrollable=true
        viewport.isXAxisBoundsManual=true
        graph.addSeries(series)

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(sensorEventListener, mAcceleration, SensorManager.SENSOR_DELAY_NORMAL)
    }
    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(sensorEventListener)
    }

}