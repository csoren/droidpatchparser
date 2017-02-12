package json

import droid.{DCO, Envelope, Patch, WaveformDistortion}
import droid.Patch.{MatrixControlledValue, MatrixControlledWaveform}
import play.api.libs.json._

object Serializers {
  implicit val matrixControlledValueWrites = new Writes[MatrixControlledValue] {
    override def writes(value: MatrixControlledValue): JsValue =
      value match {
        case Left(matrixController) => JsString(matrixController.toString)
        case Right(v) => JsNumber(v)
      }
  }

  implicit val waveformDistortionWrites = new Writes[WaveformDistortion] {
    override def writes(value: WaveformDistortion): JsValue =
      Json.obj(
        "waveform" -> value.waveform.toString,
        "distortion" -> value.distortion.toString
      )
  }

  implicit val matrixControlledWaveformSerializer = new Writes[MatrixControlledWaveform] {
    override def writes(value: MatrixControlledWaveform): JsValue =
      value match {
        case Left(matrixController) => JsString(matrixController.toString)
        case Right(waveform) => Json.toJson(waveform)
      }
  }

  implicit val dcoSerializer = new Writes[DCO] {
    override def writes(value: DCO): JsValue =
      Json.obj(
        "amplitude" -> value.amplitude,
        "frequency" -> value.frequency,
        "octave" -> value.octave,
        "offset" -> value.offset,
        "pulseWidth" -> value.pulseWidth,
        "tuningMode" -> value.tuningMode.toString,
        "waveform" -> value.waveform
      )
  }

  implicit val envelopeSerializer = new Writes[Envelope] {
    override def writes(value: Envelope): JsValue =
      Json.obj(
        "attack" -> value.attack,
        "attackLevel" -> value.attackLevel,
        "decay" -> value.decay,
        "offset" -> value.offset,
        "release" -> value.release,
        "sustain" -> value.sustain
      )
  }

  implicit val patchSerializer = new Writes[Patch] {
    override def writes(value: Patch): JsValue =
      Json.obj(
        "name" -> value.name,
        "author" -> value.author,
        "comment" -> value.comment.mkString("\n"),
        "tags" -> value.tags,
        "mixingStructure" -> value.mixingStructure,
        "variousModes" -> value.variousModes,
        "filterFrequency1" -> value.filterFrequency1,
        "filterWidthFrequency2" -> value.filterWidthFrequency2,
        "arpeggio" -> value.arpeggio,
        "dco1" -> value.dco1,
        "dco2" -> value.dco2,
        "dco2Env2Step" -> value.dco2Env2Step,
        "envelope1" -> value.env1,
        "envelope2" -> value.env2
      )
  }
}
