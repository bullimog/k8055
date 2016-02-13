package connectors

object FakeK8055Board extends K8055Board{

  override def executeCommand(command:String): String = {
    "10;20;30;40;50;60"
  }
}
