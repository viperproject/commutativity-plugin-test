// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2011-2019 ETH Zurich.

package viper.silicon.tests

import java.nio.file.Path

import viper.silicon.{Silicon, SiliconFrontend, SymbExLogger}
import viper.silver.reporter.NoopReporter
import viper.silver.testing._
import viper.silver.verifier.Verifier

/**
 * This file is based on the one from the Silicon source code.
 */
class SiliconCommutativityTests extends SilSuite {
  private val commutativityTestDirectories = Seq("commutativity-tests")

  val testDirectories = commutativityTestDirectories 

  override def frontend(verifier: Verifier, files: Seq[Path]) = {
    require(files.length == 1, "tests should consist of exactly one file")

    SymbExLogger.reset()
    SymbExLogger.filePath = files.head
    SymbExLogger.initUnitTestEngine()
    val fe = new SiliconFrontend(NoopReporter)
    fe.init(verifier)
    fe.reset(files.head)
    fe
  }

  override def annotationShouldLeadToTestCancel(ann: LocatedAnnotation) = {
    ann match {
      case UnexpectedOutput(_, _, _, _, _, _) => true
      case MissingOutput(_, _, _, _, _, issue) => issue != 34
      case _ => false
    }
  }

  override def projectInfo: ProjectInfo = super.projectInfo.update("Commutativity")

  lazy val verifiers = List(createSiliconInstance())

  val commandLineArguments: Seq[String] = Seq("--plugin", "commutativity.CommutativityPlugin")

  private def createSiliconInstance() = {
    val args =
      commandLineArguments ++
      Silicon.optionsFromScalaTestConfigMap(prefixSpecificConfigMap.getOrElse("silicon", Map()))
    val reporter = NoopReporter
    val debugInfo = ("startedBy" -> "viper.silicon.tests.SiliconCommutativityTests") :: Nil
    val silicon = Silicon.fromPartialCommandLineArguments(args, reporter, debugInfo)

    silicon
  }
}
