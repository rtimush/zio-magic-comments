import sbt._
import sbt.internal._

object MatrixUtils {

  implicit class ProjectMatrixExt(project: ProjectMatrix) {
    def jvmPlatformDyn(
        scalaVersions: Seq[String],
        settings: String => Seq[Def.Setting[_]]
    ): ProjectMatrix = scalaVersions.foldLeft(project) { (acc, version) =>
      acc.jvmPlatform(Seq(version), settings(version))
    }
  }

}
