import scala.io
import scala.math.pow
import java.util.Date
import java.util.Calendar
import scala.collection.mutable.MutableList
import java.util.concurrent.TimeUnit

import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat

val START_TIME = "2014-08-01 00:00:00"
val END_TIME = "2014-10-00 00:00:00"
val STEP = 5
val SLIP = 3

val date_format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:SS")
val start_date = date_format.parse(START_TIME)
val end_date = date_format.parse(END_TIME)
val start_cal = Calendar.getInstance()
val end_cal = Calendar.getInstance() 
start_cal.setTime(start_date)
end_cal.setTime(end_date)

def convertMinu(minu:Int) = {
      val conv_cal = Calendar.getInstance()
          conv_cal.setTime(start_cal.getTime)
              conv_cal.add(java.util.Calendar.MINUTE, minu)
                  date_format.format(conv_cal.getTime)
}

def buildWin(eve_time:String) : Array[(String, String)] = {
    val eve_date = date_format.parse(eve_time)
    val eve_cal = Calendar.getInstance()
    eve_cal.setTime(eve_date)
  
    val time_point = eve_date.getTime - start_date.getTime
    val time_point_minu = TimeUnit.MINUTES.convert(time_point, TimeUnit.MILLISECONDS) 
    //start_point = 0
    val end_point = end_date.getTime - start_date.getTime
    val end_point_minu = TimeUnit.MINUTES.convert(end_point, TimeUnit.MILLISECONDS)	

    if(time_point_minu - STEP <= 0){
        val windows_point = Array((0.toInt, STEP))
        val wins_format = windows_point.map(x => (convertMinu(x._1), convertMinu(x._2)))
        return wins_format
    }
    val first_start = ((time_point_minu - STEP) / SLIP + 1) * SLIP
    val last_start = (time_point_minu / SLIP) * SLIP
    val windows_point = Range(first_start.toInt, last_start.toInt + 1, SLIP).toArray.map(x => (x, x + STEP)).filter(x => x._2 <= end_point_minu)
    val wins_format = windows_point.map(x => (convertMinu(x._1) , convertMinu(x._2)))
    return wins_format
}
