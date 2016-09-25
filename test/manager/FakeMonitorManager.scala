package manager

import model.DeviceState

import scala.collection.mutable


object FakeMonitorManager extends MonitorManager {

  def setAnalogueOut(deviceId:String, analogueState:Int):Boolean={true}
  def setAnalogueOut2(deviceId:String, analogueState:Int):Boolean={true}
  def setDigitalOut(deviceId:String, digitalState:Boolean)={}
  def getAnalogueOut(deviceId:String):Int = {1}
  def getAnalogueOut2(deviceId:String):Int = {2}
  def getDigitalOut(deviceId:String):Boolean = {true}
}
