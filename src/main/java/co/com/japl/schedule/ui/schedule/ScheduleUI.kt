package co.com.japl.schedule.ui.schedule

import android.content.res.Resources
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.com.japl.ui.theme.MaterialThemeComposeUI
import co.com.japl.schedule.R
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

private val SELECTED = "SELECTED"
private val EMPTY = Pair("EMPTY",0)
@Composable
fun ScheduleBoard(){
    val context = LocalContext.current.applicationContext
    val resource = context.resources
    val list = arrayListOf(
        EMPTY,
        )
    list.addAll(getDaysOfWeek(resource))
    list.addAll(getScheduled(resource))
    Card (modifier = Modifier.padding(10.dp)){
        Column {
            Text(
                text = resource.getString(R.string.title_header),
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                contentPadding = PaddingValues(5.dp),
                content = {
                    items(count = list.size) { index ->
                        val value = list[index]
                        val day = value.second
                        if (day == 0) {
                            BoxEmpty()
                        } else if (index > 7 && value.first != SELECTED) {
                            HoursSchedule(hour = value.first)
                        } else if (index > 7 && value.first == SELECTED) {
                            BoxSelected(day = getDayByIndex(index),hour = value.second)
                        } else {
                            TitleSchedule(name = value.first, day = day)
                        }
                    }
                })
        }
    }
}

private fun getDayByIndex(index:Int):Int{
    return when( index  % 8) {
        1 -> DayOfWeek.SUNDAY.value
        2 -> DayOfWeek.MONDAY.value
        3 -> DayOfWeek.TUESDAY.value
        4 -> DayOfWeek.WEDNESDAY.value
        5 -> DayOfWeek.THURSDAY.value
        6 -> DayOfWeek.FRIDAY.value
        7 -> DayOfWeek.SATURDAY.value
        else -> DayOfWeek.SUNDAY.value
    }
}

private fun getScheduled(resource:Resources):List<Pair<String,Int>>{
    val hours = resource.getStringArray(R.array.hours)
    val monday = resource.getStringArray(R.array.schedule_monday).toList()
    val tuesday = resource.getStringArray(R.array.schedule_tuesday).toList()
    val wednesday = resource.getStringArray(R.array.schedule_wednesday).toList()
    val thursday = resource.getStringArray(R.array.schedule_thursday).toList()
    val friday = resource.getStringArray(R.array.schedule_friday).toList()
    val saturday = resource.getStringArray(R.array.schedule_saturday).toList()
    return hours.map {hourStr->
        val hour = if(hourStr.contains("pm")){
            (hourStr.replace("pm","").toInt()+12).toString()
        }else{
            hourStr.replace("am","")
        }
        arrayListOf(
            Pair(hourStr,-1),
            EMPTY,
            getScheduledPerDay(hour,monday),
            getScheduledPerDay(hour,tuesday),
            getScheduledPerDay(hour,wednesday),
            getScheduledPerDay(hour,thursday),
            getScheduledPerDay(hour,friday),
            getScheduledPerDay(hour,saturday)
        )
    }.flatten()
}

private fun getScheduledPerDay(hour:String,monday:List<String>):Pair<String,Int>{
    return monday.filter { it == hour }.let{
        if(it.isNotEmpty()) {
            Pair(SELECTED,hour.toInt())
        }else {
            EMPTY
        }
    }
}


private fun getDaysOfWeek(resource:Resources):List<Pair<String,Int>>{
    val listDays = getDaysOfWeek()
    return listOf(
        Pair(resource.getString(R.string.day_week_short_sunday),listDays[0]),
        Pair(resource.getString(R.string.day_week_short_monday),listDays[1]),
        Pair(resource.getString(R.string.day_week_short_tuesday),listDays[2]),
        Pair(resource.getString(R.string.day_week_short_wednesday),listDays[3]),
        Pair(resource.getString(R.string.day_week_short_thursday),listDays[4]),
        Pair(resource.getString(R.string.day_week_short_friday),listDays[5]),
        Pair(resource.getString(R.string.day_week_short_saturday),listDays[6]),
    )
}

private fun getDaysOfWeek():List<Int>{
    val currentDate = getCurrentDate()
    val startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    val endOfWeek = currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))

    val daysOfTheWeek = mutableListOf<Int>()

    var currentDay = startOfWeek
    while (!currentDay.isAfter(endOfWeek)) {
        daysOfTheWeek.add(currentDay.dayOfMonth)
        currentDay = currentDay.plusDays(1)
    }

    return daysOfTheWeek
}

@Composable
private fun HoursSchedule(hour:String){
    val time = getCurrentHour()
    val hourNum = getHourNumber(hour)
    val color = if(time == hourNum){
        MaterialTheme.colorScheme.onPrimaryContainer
    }else{
        MaterialTheme.colorScheme.secondary
    }

    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight()
    ) {
        Text(text = hour, modifier = Modifier.align(alignment = Alignment.CenterHorizontally),color = color)
    }
}

private fun getHourNumber(hour:String):Int{
    return if(hour.contains("am")){
        hour.replace("am","").toInt()
    }else{
        hour.replace("pm","").toInt() + 12
    }

}

@Composable
private fun TitleSchedule(name:String,day:Int){
    val date = getCurrentDate()
    val color = if(date.dayOfMonth == day){
        MaterialTheme.colorScheme.onPrimaryContainer
    }else{
        MaterialTheme.colorScheme.primaryContainer
    }


    Card(modifier = Modifier.padding(2.dp),
        colors = CardDefaults.cardColors(containerColor = color)) {
        Column(modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = name, modifier = Modifier.align(alignment = Alignment.CenterHorizontally))
            Text(text = "$day", modifier = Modifier.align(alignment = Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun BoxEmpty(){
    Box(
        modifier = Modifier.height(30.dp)
    ){}
}

@Composable
private fun BoxSelected(day :Int,hour:Int){
    val currentDay = getCurrentDate().dayOfWeek.value
    val time = getCurrentHour()
    val color = if(time == hour && currentDay==day){
        MaterialTheme.colorScheme.onPrimaryContainer
    }else{
        MaterialTheme.colorScheme.secondary
    }
    Box(
        modifier = Modifier
            .padding(start = 2.dp)
            .background(color)
            .height(30.dp)
    ){
        
    }
}

private fun getCurrentDate():LocalDateTime{
    return LocalDateTime.now()
}
private fun getCurrentHour():Int{
    return getCurrentDate().hour
}
@Preview(showSystemUi = true)
@Composable
fun preview(){
    MaterialThemeComposeUI {
        ScheduleBoard()
    }
}