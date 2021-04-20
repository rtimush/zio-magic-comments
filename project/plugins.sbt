resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.bintrayIvyRepo("rallyhealth", "sbt-plugins")

addSbtPlugin("com.eed3si9n"        % "sbt-projectmatrix"  % "0.7.0")
addSbtPlugin("ch.epfl.scala"       % "sbt-scalafix"       % "0.9.27")
addSbtPlugin("org.scalameta"       % "sbt-scalafmt"       % "2.4.2")
addSbtPlugin("com.rallyhealth.sbt" % "sbt-git-versioning" % "1.5.0")
addSbtPlugin("org.xerial.sbt"      % "sbt-sonatype"       % "3.9.7")
addSbtPlugin("com.github.sbt"      % "sbt-pgp"            % "2.1.2")
