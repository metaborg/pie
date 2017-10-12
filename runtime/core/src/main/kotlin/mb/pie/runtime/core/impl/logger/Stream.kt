package mb.pie.runtime.core.impl.logger

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.*
import java.io.OutputStream
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicInteger

open class StreamBuildLogger(infoStream: OutputStream = System.out, traceStream: OutputStream? = System.out, private val descLimit: Int = 200) : BuildLogger {
  private val infoWriter: PrintWriter = PrintWriter(infoStream, true)
  private val traceWriter: PrintWriter? = if(traceStream == null) null else PrintWriter(traceStream, true)
  private var indentation = AtomicInteger(0)
  private val indent get() = " ".repeat(indentation.get())


  override fun requireStart(app: UBuildApp) {
    traceWriter?.println("$indent? ${app.toShortString(descLimit)}")
    indentation.incrementAndGet()
  }

  override fun requireEnd(app: UBuildApp, info: UBuildInfo) {
    indentation.decrementAndGet()
    traceWriter?.println("$indent✔ ${app.toShortString(descLimit)}")
  }


  override fun checkConsistentStart(app: UBuildApp) {

  }

  override fun checkConsistentEnd(app: UBuildApp, result: UBuildRes?) {

  }


  override fun checkCachedStart(app: UBuildApp) {

  }

  override fun checkCachedEnd(app: UBuildApp, result: UBuildRes?) {

  }


  override fun checkStoredStart(app: UBuildApp) {

  }

  override fun checkStoredEnd(app: UBuildApp, result: UBuildRes?) {

  }


  override fun checkGenStart(app: UBuildApp, gen: Gen) {

  }

  override fun checkGenEnd(app: UBuildApp, gen: Gen, reason: InconsistentGenPath?) {
    if(reason != null) {
      traceWriter?.println("$indent␦ ${gen.path} (inconsistent: ${gen.stamp} vs ${reason.newStamp})")
    } else {
      traceWriter?.println("$indent␦ ${gen.path} (consistent: ${gen.stamp})")
    }
  }


  override fun checkPathReqStart(app: UBuildApp, req: PathReq) {

  }

  override fun checkPathReqEnd(app: UBuildApp, req: PathReq, reason: InconsistentPathReq?) {
    if(reason != null) {
      traceWriter?.println("$indent␦ ${req.path} (inconsistent: ${req.stamp} vs ${reason.newStamp})")
    } else {
      traceWriter?.println("$indent␦ ${req.path} (consistent: ${req.stamp})")
    }
  }


  override fun checkBuildReqStart(app: UBuildApp, req: UBuildReq) {

  }

  override fun checkBuildReqEnd(app: UBuildApp, req: UBuildReq, reason: BuildReason?) {
    when(reason) {
      is InconsistentBuildReq ->
        traceWriter?.println("$indent␦ ${req.app.toShortString(descLimit)} (inconsistent: ${req.stamp} vs ${reason.newStamp})")
      is InconsistentBuildReqTransientOutput ->
        traceWriter?.println("$indent␦ ${req.app.toShortString(descLimit)} (inconsistent transient output: ${reason.inconsistentResult.toShortString(descLimit)})")
      null ->
        traceWriter?.println("$indent␦ ${req.app.toShortString(descLimit)} (consistent: ${req.stamp})")
    }
  }


  override fun rebuildStart(app: UBuildApp, reason: BuildReason) {
    infoWriter.println("$indent> ${app.toShortString(descLimit)} (reason: $reason)")
  }

  override fun rebuildEnd(app: UBuildApp, reason: BuildReason, result: UBuildRes) {
    infoWriter.println("$indent< ${result.toShortString(descLimit)}")
  }
}