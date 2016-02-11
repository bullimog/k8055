package connectors

object FakeK8055Board extends K8055Board{

  override def executeCommand(command:String): String = {"0;0;0;0;0"}
}
