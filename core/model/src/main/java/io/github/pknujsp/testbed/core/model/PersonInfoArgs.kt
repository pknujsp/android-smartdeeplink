import io.github.pknujsp.deeplink.DeepArgs

data class PersonInfoArgs(
  val name: String,
  val age: Int,
  val height: Float,
  val isMale: Boolean,
) : DeepArgs()
