package mb.pie.runtime.core.impl

import mb.pie.runtime.core.PathStamp
import mb.vfs.path.PPath
import java.io.Serializable


data class Gen(val path: PPath, val stamp: PathStamp) : Serializable