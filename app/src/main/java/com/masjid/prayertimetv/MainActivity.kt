package com.masjid.prayertimetv

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Prayer(val name:String,val hour:Int,val minute:Int,val pm:Boolean)
private val Defaults=listOf(
 Prayer("Fajr",5,12,false),Prayer("Dhuhr",12,28,true),Prayer("Asr",4,3,true),
 Prayer("Maghrib",6,9,true),Prayer("Isha",7,45,true))
private val Bg=Color(0xFF061A36); private val Panel=Color(0xFF102A4D)
private val Blue=Color(0xFF1688FF); private val White=Color.White; private val Dim=Color(0xFFB9C9DE)

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{PrayerTimeApp()}}
}

@Composable
private fun PrayerTimeApp(){
 val ctx=androidx.compose.ui.platform.LocalContext.current
 val prefs=remember{ctx.getSharedPreferences("prayer_settings",Context.MODE_PRIVATE)}
 fun load()=Defaults.mapIndexed{i,p->p.copy(hour=prefs.getInt("h$i",p.hour),minute=prefs.getInt("m$i",p.minute),pm=prefs.getBoolean("p$i",p.pm))}
 var prayers by remember{mutableStateOf(load())}
 var page by remember{mutableIntStateOf(0)}
 var saved by remember{mutableStateOf(false)}
 val prayerFocus = remember { List(20) { FocusRequester() } }
 LaunchedEffect(page) {
  if (page == 0) {
   kotlinx.coroutines.yield()
   prayerFocus[0].requestFocus()
  }
 }
 BackHandler(enabled=page!=0){page=0}
 Row(Modifier.fillMaxSize().background(Bg)){
  Sidebar(page){page=it; if(it==0){ prayerFocus[0].requestFocus() }}
  Box(Modifier.weight(1f).fillMaxHeight().padding(28.dp)){when(page){
   0->PrayerEditor(prayers,prayerFocus,{i,p->prayers=prayers.toMutableList().also{it[i]=p};saved=false},{
    prayers.forEachIndexed{i,p->prefs.edit().putInt("h$i",p.hour).putInt("m$i",p.minute).putBoolean("p$i",p.pm).apply()};saved=true},{
    prayers=Defaults;saved=false},{
    prayers=load();saved=false},saved)
   1->SimplePage("Date & Time","Set the TV date and clock used by the mosque display.")
   2->SimplePage("Display","Display settings can be added here. Use OK to select an option.")
   3->SimplePage("Audio","Audio settings can be added here. Use OK to select an option.")
   4->SimplePage("Background","Background images are disabled for this clean settings interface.")
   5->SimplePage("Announcement","Announcement text settings can be added here.")
   else->SimplePage("About","Masjid Prayer Time TV\nVersion 1.0")
  }}
 }
}

@Composable
private fun Sidebar(page:Int,onPage:(Int)->Unit){
 val items=listOf("⚙  Prayer Times","▦  Date & Time","▣  Display","◖  Audio","▧  Background","▤  Announcement","ⓘ  About")
 Column(Modifier.width(225.dp).fillMaxHeight().background(Color(0xFF071B35)).padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
  Spacer(Modifier.height(8.dp))
  items.forEachIndexed{i,label->
   FocusButton(label,Modifier.fillMaxWidth().height(52.dp),primary=page==i,onClick={onPage(i)})
  }
 }
}

@Composable
private fun PrayerEditor(prayers:List<Prayer>,focusers:List<FocusRequester>,change:(Int,Prayer)->Unit,save:()->Unit,reset:()->Unit,cancel:()->Unit,saved:Boolean){
 Column(Modifier.fillMaxSize()){
  Text("♜  Set Prayer Times",fontSize=36.sp,color=White,fontWeight=FontWeight.Bold)
  Text("Change the time for each of the 5 prayers",fontSize=18.sp,color=Dim,modifier=Modifier.padding(start=55.dp,top=2.dp,bottom=15.dp))
  Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(7.dp)){
   prayers.forEachIndexed{i,p->
    val card=when(i){0->Color(0xFF18588A);1->Color(0xFF245C86);2->Color(0xFF806137);3->Color(0xFF914638);else->Color(0xFF352B85)}
    Row(Modifier.fillMaxWidth().weight(1f).background(card,RoundedCornerShape(15.dp)).padding(horizontal=13.dp),verticalAlignment=Alignment.CenterVertically){
     Text(when(i){0->"☀";1->"☼";2->"◕";3->"☀";else->"☾"},fontSize=30.sp,color=White,modifier=Modifier.width(58.dp))
     Text(p.name,Modifier.weight(1f),color=White,fontSize=23.sp,fontWeight=FontWeight.Bold)
     Stepper(String.format("%02d",p.hour),{change(i,p.copy(hour=if(p.hour==12)1 else p.hour+1))},{change(i,p.copy(hour=if(p.hour==1)12 else p.hour-1))},
      focusers[i*4],focusers[i*4+1],if(i<4)focusers[(i+1)*4] else null,focusers[i*4+2],focusers[i*4+3],null,null)
     Text(":",color=White,fontSize=26.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(horizontal=8.dp))
     Stepper(String.format("%02d",p.minute),{change(i,p.copy(minute=(p.minute+1)%60))},{change(i,p.copy(minute=(p.minute+59)%60))},
      focusers[i*4+2],focusers[i*4+3],focusers[i*4],if(i<4)focusers[(i+1)*4+2] else null,focusers[i*4+4-4])
     Spacer(Modifier.width(10.dp))
     FocusButton(if(p.pm)"PM" else "AM",Modifier.width(68.dp).height(52.dp),onClick={change(i,p.copy(pm=!p.pm))})
    }
   }
  }
  Row(Modifier.fillMaxWidth().padding(top=14.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){
   FocusButton("↺  Reset to Default",Modifier.weight(1f).height(55.dp),onClick={reset()})
   FocusButton(if(saved)"✓  Saved" else "✓  Save Changes",Modifier.weight(1.25f).height(55.dp),primary=true,onClick={save()})
   FocusButton("✕  Cancel",Modifier.weight(1f).height(55.dp),onClick={cancel()})
  }
  Text(if(saved)"Changes saved successfully" else "↑ ↓ change  •  ← → move between controls  •  OK select",color=if(saved)Color(0xFF7DFFB2) else Dim,fontSize=15.sp,modifier=Modifier.align(Alignment.CenterHorizontally).padding(top=8.dp))
 }
}

@Composable
private fun Stepper(
 value:String,
 up:()->Unit,
 down:()->Unit,
 upRequester:FocusRequester,
 downRequester:FocusRequester,
 nextUpRequester:FocusRequester?,
 rightUpRequester:FocusRequester?,
 rightDownRequester:FocusRequester?,
 leftUpRequester:FocusRequester?,
 leftDownRequester:FocusRequester?
){
 Column(
  Modifier.width(82.dp).height(80.dp).background(Color(0xDD0A2345),RoundedCornerShape(10.dp)),
  horizontalAlignment=Alignment.CenterHorizontally
 ){
  ArrowButton("▲",up,upRequester,downRequester,rightUpRequester,leftUpRequester)
  Text(value,color=White,fontSize=23.sp,fontWeight=FontWeight.Bold,modifier=Modifier.height(28.dp),textAlign=TextAlign.Center)
  ArrowButton("▼",down,downRequester,nextUpRequester,rightDownRequester,leftDownRequester)
 }
}
@Composable
private fun ArrowButton(
 label:String,
 onClick:()->Unit,
 requester:FocusRequester,
 downTarget:FocusRequester?,
 rightTarget:FocusRequester?,
 leftTarget:FocusRequester?
){
 val focusMod=Modifier
  .focusRequester(requester)
  .focusProperties{
   if(downTarget!=null) down=downTarget
   if(rightTarget!=null) right=rightTarget
   if(leftTarget!=null) left=leftTarget
  }
 FocusButton(label,focusMod.fillMaxWidth().height(26.dp),onClick=onClick,font=17.sp)
}
@Composable private fun FocusButton(label:String,modifier:Modifier,primary:Boolean=false,onClick:()->Unit,font: androidx.compose.ui.unit.TextUnit=18.sp){
 var focused by remember{mutableStateOf(false)}
 Surface(onClick=onClick,modifier=modifier.onFocusChanged{focused=it.isFocused}.focusable(),shape=RoundedCornerShape(11.dp),
  color=if(primary)Blue else if(focused)Color(0xFF315E8C) else Color(0xCC132E50),
  border=if(focused)BorderStroke(2.dp,White) else BorderStroke(1.dp,Color(0x334D78A5))){
  Box(contentAlignment=Alignment.Center){Text(label,color=White,fontSize=font,fontWeight=FontWeight.Bold)}
 }
}

@Composable private fun SimplePage(title:String,description:String){
 Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally){
  Text(title,color=White,fontSize=38.sp,fontWeight=FontWeight.Bold)
  Spacer(Modifier.height(15.dp));Text(description,color=Dim,fontSize=20.sp,textAlign=TextAlign.Center)
  Spacer(Modifier.height(24.dp));Text("Press BACK to return to Prayer Times",color=Dim,fontSize=16.sp)
 }
}
