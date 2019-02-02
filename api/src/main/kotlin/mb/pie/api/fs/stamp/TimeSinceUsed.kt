package mb.pie.api.fs.stamp
import java.time.Duration
import java.time.Instant
import mb.pie.api.fs.FileSystemResource

class TimeSinceUsedResourceStamper(dt: Duration) : FileSystemStamper {
  var prevStamp : Instant = Instant.now()
  val dt = dt;
  override fun stamp(resource: FileSystemResource): FileSystemStamp {
    if( Duration.between(prevStamp,Instant.now()) > dt ) {
      prevStamp = Instant.now()

    }
    return ValueResourceStamp(prevStamp,this)
  }

  override fun toString(): String {
    return "FreshnessStamper"
  }
}

