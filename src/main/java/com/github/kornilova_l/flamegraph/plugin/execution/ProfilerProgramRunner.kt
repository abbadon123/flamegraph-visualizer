package com.github.kornilova_l.flamegraph.plugin.execution

import com.github.kornilova_l.flamegraph.configuration.Configuration
import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.github.kornilova_l.flamegraph.plugin.configuration.ConfigStorage
import com.github.kornilova_l.flamegraph.plugin.configuration.PluginConfigManager
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.impl.DefaultJavaProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment

/**
 * ProgramRunner which runs Profiler Executor
 */
class ProfilerProgramRunner : DefaultJavaProgramRunner() {
    private var configuration: Configuration? = null
    private var projectName: String? = null

    @Throws(ExecutionException::class)
    override fun execute(environment: ExecutionEnvironment) {
        projectName = environment.project.name
        configuration = environment.project.getComponent(ConfigStorage::class.java).state as Configuration
        super.execute(environment)
    }

    @Throws(ExecutionException::class)
    override fun patch(javaParameters: JavaParameters,
                       settings: RunnerSettings?,
                       runProfile: RunProfile,
                       beforeExecution: Boolean) {
        val config = configuration ?: return
        val name = projectName ?: return
        val configFile = PluginFileManager.getConfigurationFile(name)
        PluginConfigManager.exportConfig(configFile, config)
        val pathToAgent = PluginFileManager.getPathToJar("javaagent.jar") ?: return
        /* add javaagent */
        javaParameters.vmParametersList.add(
                "-javaagent:" +
                        pathToAgent +
                        "=" +
                        PluginFileManager.createLogFile(projectName!!, runProfile.name).absolutePath +
                        "&" +
                        configFile.absolutePath
        )
        /* add Proxy class to classpath. It is for classes that do not have system classloader in chain */
        javaParameters.classPath.add(PluginFileManager.getPathToJar("proxy.jar"))
    }

    override fun getRunnerId(): String {
        return RUNNER_ID
    }

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return executorId == ProfilerExecutor.EXECUTOR_ID
    }

    companion object {
        private const val RUNNER_ID = "ProfileRunnerID"
    }
}
