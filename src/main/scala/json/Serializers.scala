package json

import droid.Patch.{MatrixControlledValue, MatrixControlledWaveform}
import droid.{DCO, Envelope, Patch, WaveformDistortion}
import play.api.libs.json._

object Serializers {
  implicit val matrixControlledValueWrites = new Writes[MatrixControlledValue] {
    override def writes(value: MatrixControlledValue) =
      value.fold(Json.toJson(_), Json.toJson(_))
  }

  implicit val waveformDistortionWrites = Json.writes[WaveformDistortion]

  implicit val matrixControlledWaveformSerializer = new Writes[MatrixControlledWaveform] {
    override def writes(value: MatrixControlledWaveform): JsValue =
      value.fold(Json.toJson(_), Json.toJson(_))
  }

  implicit val dcoSerializer = Json.writes[DCO]

  implicit val envelopeSerializer = Json.writes[Envelope]

  implicit val patchSerializer = Json.writes[Patch]
}
