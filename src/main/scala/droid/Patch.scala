package droid

import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.{ByteBuffer, ByteOrder}

import droid.Distortion.Distortion
import droid.Patch.{MatrixControlledValue, MatrixControlledWaveform}
import droid.TuningMode.TuningMode

case class DCO(octave: Int,
               amplitude: MatrixControlledValue,
               frequency: MatrixControlledValue,
               offset: MatrixControlledValue,
               pulseWidth: MatrixControlledValue,
               waveform: MatrixControlledWaveform,
               tuningMode: TuningMode)

case class Envelope(attack: Int,
                    decay: Int,
                    attackLevel: Int,
                    release: Int,
                    sustain: Int,
                    offset: MatrixControlledValue)

case class Patch(name: String,
                 tags: Seq[String],
                 author: String,
                 comment: String,
                 dco1: DCO,
                 dco2: DCO,
                 env1: Envelope,
                 env2: Envelope,
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

  val SawUp = Value(0)
  val SawDown = Value(1)
  val Square = Value(2)
  val Triangle = Value(3)
  val Noise = Value(4)
  val `Sample&Hold` = Value(5)
  val Digital = Value(6)
  val Silence = Value(7)
}


object Distortion extends Enumeration {
  type Distortion = Value

  val Clip = Value(0)
  val Mirror = Value(1)
  val ZeroSnap = Value(2)
  val Wrap = Value(3)
}


case class WaveformDistortion(waveform: Waveform.Waveform, distortion: Distortion.Distortion)


object TuningMode extends Enumeration {
  type TuningMode = Value

  val Fine = Value(0)
  val Linear = Value(1)
  val Standard = Value(2)
  val Wide = Value(3)
}


object Patch {

  import droid.MatrixController.MatrixController

  type MatrixControlledValue = Either[MatrixController, Int]

  type MatrixControlledWaveform = Either[MatrixController, WaveformDistortion]

  private val windowsCharset = Charset.forName("Windows-1252")

  def waveformValue(matrixController: Int, distortion: Distortion): MatrixControlledWaveform =
    if (matrixController <= 7)
      Right(WaveformDistortion(Waveform(matrixController), distortion))
    else
      Left(MatrixController(matrixController - 8))

  def controlledValue(matrixController: Int, value: Int): MatrixControlledValue =
    if (matrixController == 0)
      Right(value)
    else
      Left(MatrixController(matrixController - 1))

  private def upperCaseFirst(str: String): String =
    if (str.length > 0 && str.charAt(0).isLower)
      str.charAt(0).toUpper + str.substring(1)
    else
      str

  private def removeQuotes(str: String): String =
    if (str.startsWith("\"") && str.endsWith("\""))
      str.substring(1, str.length - 1)
    else
      str

  private def filterName(str: String): String =
    upperCaseFirst(removeQuotes(str))
      .map(ch => if (ch == '_') ' ' else ch)
      .replace("Cheasy", "Cheesy")
      .replace("Popbass", "Pop Bass")
      .replace("Snareroll", "Snare Roll")
      .replace("Drunked", "Drunken")
      .replace("Standart", "Standard")
      .replace("Winther", "Winter")

  private def getAscii(buffer: ByteBuffer, chars: Int): String = {
    val array = new Array[Byte](chars)
    buffer.get(array)
    new String(array.takeWhile(_ != 0), windowsCharset)
  }

  private def filterComment(comment: String): String =
    if (comment == "2000 chars max")
      ""
    else
      upperCaseFirst(comment.trim
        .filter(_ != '\r')
        .split('\n')
        .map(_.trim)
        .mkString("\n")
      )

  private val cleanClassName = Map(
    "TechBass" -> List("Tech", "Bass"),
    "SynthBass" -> List("Synth", "Bass"),
    "Synths" -> List("Synth"),
    "Seq" -> List("Sequence"),
    "Perc" -> List("Percussion"),
    "Prec" -> List("Percussion"),
    "Synthbass" -> List("Synth", "Bass"),
    "TechSynth" -> List("Tech", "Synth"),
    "BassSynth" -> List("Bass", "Synth"),
    "LegatoCycle" -> List("Legato", "Cycle"),
    "Drum" -> List("Percussion"),
    "Efffect" -> List("Effect"),
    "Effects" -> List("Effect"),
    "SFX" -> List("Effect"),
    "DanceBass" -> List("Dance", "Bass"),
    "SynthLead" -> List("Synth", "Lead"),
    "Techsynth" -> List("Tech", "Synth"),
    "Synthwave" -> List("Synth"),
    "C4" -> List("KeyC4"),
    "F5" -> List("KeyF5"),
    "Sound" -> Nil
  )

  private def filterClasses(classes: Seq[String]): Seq[String] =
    classes
      .filter(!_.toLowerCase.contains("comment"))
      .map(_.replace('_', '-'))
      .flatMap(_.split('-'))
      .map(upperCaseFirst)
      .flatMap(v => cleanClassName.getOrElse(v, List(v)))
  
  private def extractClasses(name: String): (String, Seq[String]) = {
    def splitName(name: String): (String, List[String]) = {
      val optionalNameAndClass =
        name.indexOf('(') match {
          case -1 => None
          case openIndex =>
            name.indexOf(')', openIndex) match {
              case -1 => None
              case closeIndex =>
                val tag = name.substring(openIndex + 1, closeIndex).trim
                val nameMinusTag = name.substring(0, openIndex).trim + name.substring(closeIndex + 1).trim
                val (newName, classes) = splitName(nameMinusTag)
                Some(filterName(newName), upperCaseFirst(tag) :: classes)
            }
        }

      optionalNameAndClass.getOrElse((name, Nil))
    }

    val (newName, classes) = splitName(name)
    (newName, filterClasses(classes))
  }


  def load(path: Path): Option[Patch] = {
    val channel = new RandomAccessFile(path.toString, "r").getChannel
    val map = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size)
    map.order(ByteOrder.LITTLE_ENDIAN)

    if (getAscii(map, 3) == "DRP" && map.getInt() == 0) {
      val (name, clazz) = extractClasses(getAscii(map, 256).trim)
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
      val _dco1Distortion = Distortion(map.getInt())
      val dco1Frequency = controlledValue(map.getInt(), _dco1FrequencyValue)
      val dco1Offset = controlledValue(map.getInt(), _dco1OffsetValue)
      val dco1PulseWidth = controlledValue(map.getInt(), _dco1PulsewidthValue)
      val dco1Waveform = waveformValue(map.getInt(), _dco1Distortion)
      val arpeggio = controlledValue(map.getInt(), _arpeggioValue)
      val filterFrequency1 = controlledValue(map.getInt(), _filterFrequency1Value)
      val dco2Amplitude = controlledValue(map.getInt(), _dco2AmplitudeValue)
      val _dco2Distortion = Distortion(map.getInt())
      val dco2Frequency = controlledValue(map.getInt(), _dco2FrequencyValue)
      val dco2Offset = controlledValue(map.getInt(), _dco2OffsetValue)
      val dco2PulseWidth = controlledValue(map.getInt(), _dco2PulsewidthValue)
      val dco2Waveform = waveformValue(map.getInt(), _dco2Distortion)
      val env1Offset = controlledValue(map.getInt(), _env1OffsetValue)
      val env2Offset = controlledValue(map.getInt(), _env2OffsetValue)
      val _filterTypeRouting = map.getInt()
      val _midiChannel2 = map.getInt() + 1
      val filterFrequency2 = controlledValue(map.getInt(), _filterWidthFrequency2Value)
      val dco1TuningMode = TuningMode(map.getInt())
      val dco2TuningMode = TuningMode(map.getInt())
      val variousModes = map.getInt()
      val mixingStructure = map.getInt()

      val dco1 = DCO(dco1Octave, dco1Amplitude, dco1Frequency, dco1Offset, dco1PulseWidth, dco1Waveform, dco1TuningMode)
      val dco2 = DCO(dco2Octave, dco2Amplitude, dco2Frequency, dco2Offset, dco2PulseWidth, dco2Waveform, dco2TuningMode)
      val env1 = Envelope(env1Attack, env1Decay, env1AttackLevel, env1Release, env1Sustain, env1Offset)
      val env2 = Envelope(env2Attack, env2Decay, env2AttackLevel, env2Release, env2Sustain, env2Offset)

      Some(Patch(name, clazz, author, comment, dco1, dco2, env1, env2, dco2Env2StepAmount, arpeggio, filterFrequency1, filterFrequency2, variousModes, mixingStructure))
    } else {
      None
    }
  }
}
