package droid

import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.{ByteBuffer, ByteOrder}

import droid.Distortion.Distortion
import droid.Patch.{MatrixControlledValue, MatrixControlledWaveform}
import droid.TuningMode.TuningMode

case class DCO(octave: Int,
               amplitude: MatrixControlledValue,
               distortion: Distortion,
               frequency: MatrixControlledValue,
               offset: MatrixControlledValue,
               pulseWidth: MatrixControlledValue,
               waveform: MatrixControlledWaveform,
               tuningMode: TuningMode)

case class ENV(attack: Int,
               decay: Int,
               attackLevel: Int,
               release: Int,
               sustain: Int,
               offset: MatrixControlledValue)

case class Patch(name: String,
                 tags: List[String],
                 author: String,
                 comment: List[String],
                 dco1: DCO,
                 dco2: DCO,
                 env1: ENV,
                 env2: ENV,
                 dco2Env2Step: Int,
                 arpeggio: MatrixControlledValue,
                 filterFrequency1: MatrixControlledValue,
                 filterWidthFrequency2: MatrixControlledValue,
                 variousModes: Int,
                 mixingStructure: Int)


object MatrixController extends Enumeration {
  type MatrixController = Value

  val En1 = Value(0)
  val En2 = Value(1)
  val DC1 = Value(2)
  val DC2 = Value(3)
  val PB = Value(4)
  val PB2 = Value(5)
  val Mod = Value(6)
  val Vel = Value(7)
  val VlG = Value(8)
  val Gat = Value(9)
  val Aft = Value(10)
  val KF = Value(11)
  val KF2 = Value(12)
  val Hld = Value(13)
  val Exp = Value(14)
  val Bth = Value(15)
}


object Waveform extends Enumeration {
  type Waveform = Value

  val SUp = Value(0)
  val SDn = Value(1)
  val Squ = Value(2)
  val Tri = Value(3)
  val Nse = Value(4)
  val `S&H` = Value(5)
  val Dig = Value(6)
  val Sil = Value(7)
}


object TuningMode extends Enumeration {
  type TuningMode = Value

  val Fine = Value(0)
  val Linear = Value(1)
  val Standard = Value(2)
  val Wide = Value(3)
}


object Distortion extends Enumeration {
  type Distortion = Value

  val Clp = Value(0)
  val Mir = Value(1)
  val Zsn = Value(2)
  val Wrp = Value(3)
}


object Patch {

  import droid.MatrixController.MatrixController
  import droid.Waveform.Waveform

  type MatrixControlledValue = Either[MatrixController, Int]

  type MatrixControlledWaveform = Either[MatrixController, Waveform]

  def waveformValue(matrixController: Int): MatrixControlledWaveform =
    if (matrixController <= 7)
      Right(Waveform(matrixController))
    else
      Left(MatrixController(matrixController - 8))

  def controlledValue(matrixController: Int, value: Int): MatrixControlledValue =
    if (matrixController == 0)
      Right(value)
    else
      Left(MatrixController(matrixController - 1))

  private def upperCaseFirst(str: String): String =
    str.toList match {
      case head :: tail if head.isLower => (head.toUpper :: tail).mkString
      case _ => str
    }

  private def filterName(str: String): String =
    upperCaseFirst(str).map(ch => if (ch == '_') ' ' else ch)

  private def getAscii(buffer: ByteBuffer, chars: Int): String = {
    val array = new Array[Byte](chars)
    buffer.get(array)
    val v = array.takeWhile(_ != 0).map(b => b.toChar).mkString
    v
  }

  private def filterComment(comment: String): List[String] =
    if (comment == "2000 chars max") Nil else comment.filter(_ != '\r').split('\n').toList

  private def splitName(name: String): (String, List[String]) = {
    val optionalNameAndClass =
      if (name.endsWith(")")) {
        name.lastIndexOf("(") match {
          case -1 => None
          case parensIndex =>
            val (newName, classes) = splitName(name.substring(0, parensIndex).trim)
            Some(filterName(newName), upperCaseFirst(name.substring(parensIndex + 1, name.length - 1).trim) :: classes)
        }
      } else {
        None
      }

    optionalNameAndClass.getOrElse((name, Nil))
  }


  def load(path: Path): Option[Patch] = {
    val channel = new RandomAccessFile(path.toString, "r").getChannel
    val map = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size)
    map.order(ByteOrder.LITTLE_ENDIAN)

    if (getAscii(map, 3) == "DRP" && map.getInt() == 0) {
      val (name, clazz) = splitName(getAscii(map, 256).trim)
      val author = getAscii(map, 256)
      val comment = filterComment(getAscii(map, 2048))

      val _midiChannel1 = map.getInt() + 1
      val _dco1AmplitudeValue, _dco1FrequencyValue, _dco1OffsetValue, _dco1PulsewidthValue = map.getInt()
      val _dco2AmplitudeValue, _dco2FrequencyValue, _dco2OffsetValue, _dco2PulsewidthValue = map.getInt()
      val _env1OffsetValue, _env2OffsetValue = map.getInt()
      val dco1Octave, dco2Octave = map.getInt()
      val env1Attack, env1Decay, env1AttackLevel, env1Release, env1Sustain = map.getInt()
      val env2Attack, env2Decay, env2AttackLevel, env2Release, env2Sustain = map.getInt()
      val dco2Env2StepAmount = map.getInt()
      val _arpeggioValue = map.getInt()
      val _filterFrequency1Value, _filterWidthFrequency2Value = map.getInt()
      val dco1Amplitude = controlledValue(map.getInt(), _dco1AmplitudeValue)
      val dco1Distortion = Distortion(map.getInt())
      val dco1Frequency = controlledValue(map.getInt(), _dco1FrequencyValue)
      val dco1Offset = controlledValue(map.getInt(), _dco1OffsetValue)
      val dco1PulseWidth = controlledValue(map.getInt(), _dco1PulsewidthValue)
      val dco1Waveform = waveformValue(map.getInt())
      val arpeggio = controlledValue(map.getInt(), _arpeggioValue)
      val filterFrequency1 = controlledValue(map.getInt(), _filterFrequency1Value)
      val dco2Amplitude = controlledValue(map.getInt(), _dco2AmplitudeValue)
      val dco2Distortion = Distortion(map.getInt())
      val dco2Frequency = controlledValue(map.getInt(), _dco2FrequencyValue)
      val dco2Offset = controlledValue(map.getInt(), _dco2OffsetValue)
      val dco2PulseWidth = controlledValue(map.getInt(), _dco2PulsewidthValue)
      val dco2Waveform = waveformValue(map.getInt())
      val env1Offset = controlledValue(map.getInt(), _env1OffsetValue)
      val env2Offset = controlledValue(map.getInt(), _env2OffsetValue)
      val _filterTypeRouting = map.getInt()
      val _midiChannel2 = map.getInt() + 1
      val filterFrequency2 = controlledValue(map.getInt(), _filterWidthFrequency2Value)
      val dco1TuningMode = TuningMode(map.getInt())
      val dco2TuningMode = TuningMode(map.getInt())
      val variousModes = map.getInt()
      val mixingStructure = map.getInt()

      val dco1 = DCO(dco1Octave, dco1Amplitude, dco1Distortion, dco1Frequency, dco1Offset, dco1PulseWidth, dco1Waveform, dco1TuningMode)
      val dco2 = DCO(dco2Octave, dco2Amplitude, dco2Distortion, dco2Frequency, dco2Offset, dco2PulseWidth, dco2Waveform, dco2TuningMode)
      val env1 = ENV(env1Attack, env1Decay, env1AttackLevel, env1Release, env1Sustain, env1Offset)
      val env2 = ENV(env2Attack, env2Decay, env2AttackLevel, env2Release, env2Sustain, env2Offset)

      Some(Patch(name, clazz, author, comment, dco1, dco2, env1, env2, dco2Env2StepAmount, arpeggio, filterFrequency1, filterFrequency2, variousModes, mixingStructure))
    } else {
      None
    }
  }
}
