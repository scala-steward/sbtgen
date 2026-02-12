package izumi.sbt.plugins

import com.github.sbt.git.GitPlugin
import com.github.sbt.git.SbtGit.GitKeys.useConsoleForROGit
import sbt.{AutoPlugin, Def, PluginTrigger, Plugins, file}

/**
  * Make sbt-git compatible with worktrees
  *
  * @see https://github.com/sbt/sbt-git/pull/230
  */
object IzumiGitWorktreePlugin extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = super.requires && GitPlugin

  override def buildSettings: Seq[Def.Setting[_]] = {
    Seq(
      useConsoleForROGit ~= (previousValue => if (file(".git").exists()) true else previousValue)
    )
  }
}
