resolvers += Resolver.sonatypeRepo("releases")

addSbtPlugin("com.eed3si9n"        % "sbt-projectmatrix"  % "0.9.0")
addSbtPlugin("ch.epfl.scala"       % "sbt-scalafix"       % "0.9.27")
addSbtPlugin("org.scalameta"       % "sbt-scalafmt"       % "2.4.6")
addSbtPlugin("com.rallyhealth.sbt" % "sbt-git-versioning" % "1.6.0")
addSbtPlugin("org.xerial.sbt"      % "sbt-sonatype"       % "3.11.3")
addSbtPlugin("com.github.sbt"      % "sbt-pgp"            % "2.1.2")
