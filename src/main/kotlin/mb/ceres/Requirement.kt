package mb.ceres

import java.io.Serializable

sealed class Req : Serializable
data class FileReq(val path: CPath, val stamp: PathStamp) : Req()
data class BuildReq<out I : In, out O : Out>(val request: BuildRequest<I, O>, val stamp: OutputStamp) : Req()


data class Gen(val path: CPath, val stamp: PathStamp) : Serializable