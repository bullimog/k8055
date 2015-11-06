import connector.K8055Board
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test.Helpers._
import play.api.test._


@RunWith(classOf[JUnitRunner])
class K8055Spec extends Specification {

  val k8055Board = new K8055Board {
    var lastCommand:String = ""
    var fakeBoardResponse = ""

    override def executeCommand(command:String): String = {
      lastCommand = command
      fakeBoardResponse
    }
  }

  "K8055Board" should {
    "execute the right commands, when analogue methods are called" in {
      k8055Board.setAnalogueOut(1, 255)
      k8055Board.getAnalogueOut(1) must equalTo(255)
      k8055Board.lastCommand must equalTo("k8055 -d:0 -a1:255 -a2:0")


      k8055Board.setAnaloguePercentageOut(1, 50)
      k8055Board.getAnaloguePercentageOut(1) must equalTo(50)
      k8055Board.getAnAnalogueOut(1, 100) must equalTo(127)
      k8055Board.lastCommand must equalTo("k8055 -d:0 -a1:127 -a2:0")


      k8055Board.fakeBoardResponse = "8055;24;25;26;27;28"  //just some numbers to match against
      k8055Board.getAnalogueIn(1) must equalTo(25)
      k8055Board.getAnalogueIn(2) must equalTo(26)
      k8055Board.lastCommand must equalTo("k8055")

      k8055Board.readStatus().get must beEqualTo(Array(8055,24,25,26,27,28))
    }
  }



}
