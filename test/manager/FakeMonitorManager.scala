package manager

import model.DeviceState

import scala.collection.mutable


object FakeMonitorManager extends MonitorManager {

  def setAnalogueOut(deviceId:String, analogueState:Int)={}
  def setDigitalOut(deviceId:String, digitalState:Boolean)={}
  def getAnalogueOut(deviceId:String):Int = {1}
  def getDigitalOut(deviceId:String):Boolean = {true}
}
