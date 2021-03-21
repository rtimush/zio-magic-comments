resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.bintrayIvyRepo("rallyhealth", "sbt-plugins")

addSbtPlugin("ch.epfl.scala"       % "sbt-scalafix"       % "0.9.26")
addSbtPlugin("com.rallyhealth.sbt" % "sbt-git-versioning" % "1.4.0")
addSbtPlugin("org.xerial.sbt"      % "sbt-sonatype"       % "3.9.7")
addSbtPlugin("com.github.sbt"      % "sbt-pgp"            % "2.1.2")
