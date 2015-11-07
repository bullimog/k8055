import connector.K8055Board
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test.Helpers._
import play.api.test._


@RunWith(classOf[JUnitRunner])
class K8055BoardSpec extends Specification {

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
      k8055Board.analogueOut1 must equalTo(25500)


      k8055Board.setAnaloguePercentageOut(1, 50)
      k8055Board.getAnaloguePercentageOut(1) must equalTo(50)
      k8055Board.getAnAnalogueOut(1, 100) must equalTo(127)
      k8055Board.lastCommand must equalTo("k8055 -d:0 -a1:127 -a2:0")
      k8055Board.analogueOut1 must equalTo(12750)


      k8055Board.fakeBoardResponse = "0;24;25;26;27;28"  //just some numbers to match against
      k8055Board.getAnalogueIn(1) must equalTo(25)
      k8055Board.getAnalogueIn(2) must equalTo(26)
      k8055Board.lastCommand must equalTo("k8055")

      k8055Board.readStatus().get must beEqualTo(Array(0,24,25,26,27,28))
    }
  }




  "K8055Board" should {
    "set the right values, when digital out methods are called" in {
      k8055Board.setDigitalOut(1, true)
      k8055Board.getDigitalOut(1) must equalTo(true)
      k8055Board.setDigitalOut(1, false)
      k8055Board.getDigitalOut(1) must equalTo(false)

      k8055Board.setDigitalChannel(0)
      k8055Board.setDigitalChannel(1)
      k8055Board.setDigitalChannel(2)
      k8055Board.setDigitalChannel(3)
      k8055Board.setDigitalChannel(4)
      k8055Board.setDigitalChannel(5)
      k8055Board.setDigitalChannel(6)
      k8055Board.setDigitalChannel(7)
      k8055Board.setDigitalChannel(8)
      k8055Board.setDigitalChannel(9)

      k8055Board.getDigitalOut(0) must equalTo(false)
      k8055Board.getDigitalOut(1) must equalTo(true)
      k8055Board.getDigitalOut(2) must equalTo(true)
      k8055Board.getDigitalOut(3) must equalTo(true)
      k8055Board.getDigitalOut(4) must equalTo(true)
      k8055Board.getDigitalOut(5) must equalTo(true)
      k8055Board.getDigitalOut(6) must equalTo(true)
      k8055Board.getDigitalOut(7) must equalTo(true)
      k8055Board.getDigitalOut(8) must equalTo(true)
      k8055Board.getDigitalOut(9) must equalTo(false)

      k8055Board.clearDigitalChannel(0)
      k8055Board.clearDigitalChannel(1)
      k8055Board.clearDigitalChannel(2)
      k8055Board.clearDigitalChannel(3)
      k8055Board.clearDigitalChannel(4)
      k8055Board.clearDigitalChannel(5)
      k8055Board.clearDigitalChannel(6)
      k8055Board.clearDigitalChannel(7)
      k8055Board.clearDigitalChannel(8)
      k8055Board.clearDigitalChannel(9)

      k8055Board.getDigitalOut(0) must equalTo(false)
      k8055Board.getDigitalOut(1) must equalTo(false)
      k8055Board.getDigitalOut(2) must equalTo(false)
      k8055Board.getDigitalOut(3) must equalTo(false)
      k8055Board.getDigitalOut(4) must equalTo(false)
      k8055Board.getDigitalOut(5) must equalTo(false)
      k8055Board.getDigitalOut(6) must equalTo(false)
      k8055Board.getDigitalOut(7) must equalTo(false)
      k8055Board.getDigitalOut(8) must equalTo(false)
      k8055Board.getDigitalOut(9) must equalTo(false)


      k8055Board.byteMask(1) must equalTo(1)
      k8055Board.byteMask(2) must equalTo(2)
      k8055Board.byteMask(3) must equalTo(4)
      k8055Board.byteMask(4) must equalTo(8)
      k8055Board.byteMask(5) must equalTo(16)
      k8055Board.byteMask(6) must equalTo(32)
      k8055Board.byteMask(7) must equalTo(64)
      k8055Board.byteMask(8) must equalTo(128)
    }
  }

  "K8055Board" should {
    "set the right values, when digital in methods are called" in {
      k8055Board.fakeBoardResponse = "0;0;0;0;0;0"
      k8055Board.getDigitalIn(0) must equalTo(false)
      k8055Board.getDigitalIn(1) must equalTo(false)
      k8055Board.getDigitalIn(2) must equalTo(false)
      k8055Board.getDigitalIn(3) must equalTo(false)
      k8055Board.getDigitalIn(4) must equalTo(false)
      k8055Board.getDigitalIn(5) must equalTo(false)
      k8055Board.getDigitalIn(6) must equalTo(false)
      k8055Board.getDigitalIn(7) must equalTo(false)
      k8055Board.getDigitalIn(8) must equalTo(false)
      k8055Board.getDigitalIn(9) must equalTo(false)
      k8055Board.fakeBoardResponse = "0;255;0;0;0;0"
      k8055Board.getDigitalIn(0) must equalTo(false)
      k8055Board.getDigitalIn(1) must equalTo(true)
      k8055Board.getDigitalIn(2) must equalTo(true)
      k8055Board.getDigitalIn(3) must equalTo(true)
      k8055Board.getDigitalIn(4) must equalTo(true)
      k8055Board.getDigitalIn(5) must equalTo(true)
      k8055Board.getDigitalIn(6) must equalTo(true)
      k8055Board.getDigitalIn(7) must equalTo(true)
      k8055Board.getDigitalIn(8) must equalTo(true)
      k8055Board.getDigitalIn(9) must equalTo(false)

      k8055Board.resetCount(1)
      k8055Board.lastCommand must equalTo("k8055 -reset1")

      k8055Board.fakeBoardResponse = "0;0;0;0;7;8"
      k8055Board.getCount(1) must equalTo(7)
      k8055Board.getCount(2) must equalTo(8)

      k8055Board.getDigitalInLatch(1) must equalTo(true)
      k8055Board.lastCommand must equalTo("k8055 -reset1")
      k8055Board.fakeBoardResponse = "0;0;0;0;0;0"
      k8055Board.getCount(1) must equalTo(0)
      k8055Board.getDigitalInLatch(1) must equalTo(false)



    }
  }

}
