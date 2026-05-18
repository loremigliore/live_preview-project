package com.github.loremigliore.livepreview.logic

import com.github.loremigliore.livepreview.LivePreviewBundle
import com.github.loremigliore.livepreview.domain.BuildTool
import com.github.loremigliore.livepreview.settings.LivePreviewSettings
import com.github.loremigliore.livepreview.utils.PreviewFileManager
import com.github.loremigliore.livepreview.utils.environmentStateHolder
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import java.awt.BorderLayout
import java.io.File
import javax.swing.JPanel

class RunPreview private constructor(
    private val project: Project,
    private val functionName: String,
    private val fqName: String,
) {
    private val tempRoot = File(project.basePath, "build/hot-reload-previews/$functionName")

    private fun execute() {
        val sourceCode = generateSourceCode() ?: return

        val commandLine =
            when (project.environmentStateHolder.state.value.buildTool) {
                BuildTool.AMPER -> prepareAmperExecution(sourceCode)
                BuildTool.GRADLE -> prepareGradleExecution(sourceCode)
                BuildTool.UNKNOWN -> throw IllegalStateException("Unrecognized build tool")
            }

        startProcess(commandLine)
    }

    private fun generateSourceCode(): String? {
        val settings = LivePreviewSettings.getInstance()
        val functionCode = PreviewFileManager.readPreviewCode(project, fqName) ?: return null

        val mainFunction =
            settings.mainFunction
                .replace("<COMPOSABLE>", functionCode)
                .replace("<COMPOSABLE_NAME>", functionName)
                .replace("<WIDTH>", settings.windowWidth.toString())
                .replace("<HEIGHT>", settings.windowHeight.toString())
                .replace("<ALWAYS_ON_TOP>", settings.alwaysOnTop.toString())
                .trimIndent()

        return """
            package livepreview.generated
            
            import androidx.compose.ui.window.application
            import androidx.compose.ui.window.Window
            import androidx.compose.ui.window.WindowState
            import androidx.compose.ui.unit.dp
            import $fqName
            
            $mainFunction
            """.trimIndent()
    }

    private fun prepareAmperExecution(sourceCode: String): GeneralCommandLine {
        tempRoot.deleteRecursively()
        tempRoot.mkdirs()

        File(tempRoot, "module.yaml").writeText(
            """
            product: app
            platforms: [jvm]
            dependencies:
              - ../../../
            """.trimIndent(),
        )

        val srcDir = File(tempRoot, "src").apply { mkdirs() }
        File(srcDir, "main.kt").writeText(sourceCode)

        val exePath = if (System.getProperty("os.name").lowercase().contains("win")) "amper.bat" else "./amper"

        return GeneralCommandLine()
            .withWorkDirectory(project.basePath)
            .withExePath(exePath)
            .withParameters("run", "--module", "build/hot-reload-previews/$functionName")
            .withParameters("--compose-hot-reload-mode")
            .withParameters("--jvm-args", "-Dcompose.reload.orchestration.port=49787")
    }

    private fun prepareGradleExecution(sourceCode: String): GeneralCommandLine {
        if (tempRoot.exists()) tempRoot.deleteRecursively()

        val generatedSrcDir = File(tempRoot, "src/main/kotlin").apply { mkdirs() }
        File(generatedSrcDir, "PreviewHost.kt").writeText(sourceCode)

        val profilerPath = project.environmentStateHolder.state.value.library
        val safeGeneratedSrcDir = generatedSrcDir.absolutePath.replace("\\", "/")
        val safeProfileLog = File(tempRoot, "profile.jfr").absolutePath.replace("\\", "/")

        val initScript = File(tempRoot, "preview-init.gradle")
        initScript.writeText(
            """
            allprojects {
                afterEvaluate { proj ->
                    if (proj.path == ':desktopApp' || proj.name == 'desktopApp') {
                        def target = proj.kotlin.targets.findByName("jvm") ?: proj.kotlin.targets.findByName("desktop")
                        if (target != null) {
                            def mainCompilation = target.compilations.findByName("main")
                            if (mainCompilation != null) {
                                mainCompilation.defaultSourceSet.kotlin.srcDirs += '$safeGeneratedSrcDir'
                                
                                proj.dependencies {
                                    def config = mainCompilation.defaultSourceSet.implementationConfigurationName
                                    add(config, "org.jetbrains.compose.ui:ui-tooling-preview-desktop:1.6.1")
                                }

                                proj.tasks.register('runHotReloadPreview', JavaExec) {
                                    group = 'compose desktop'
                                    mainClass = 'livepreview.generated.PreviewHostKt'
                                    dependsOn(mainCompilation.compileKotlinTaskName)
                                    classpath = mainCompilation.output.allOutputs + mainCompilation.runtimeDependencyFiles
                                    
                                    jvmArgs = [
                                                  "-Dcompose.reload.orchestration.port=49787",
                                                  "-Dcompose.hot.reload.enabled=true",
                                                  "-Dcompose.reload.log.level=DEBUG",
                                                  "-Dcompose.application.configure.hot.reload=true" 
                                    ]
                                    systemProperty "compose.reload.project.path", project.rootDir.absolutePath
                                    args = ["--compose-hot-reload-mode"]
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )

        val exePath = if (System.getProperty("os.name").lowercase().contains("win")) "gradlew.bat" else "./gradlew"

        return GeneralCommandLine()
            .withWorkDirectory(project.basePath)
            .withExePath(exePath)
            .withParameters("--init-script", initScript.absolutePath)
            .withParameters(":desktopApp:runHotReloadPreview")
            .also { cmd ->
                ProjectRootManager.getInstance(project).projectSdk?.homePath?.let {
                    cmd.withEnvironment("JAVA_HOME", it)
                }
            }
    }

    private fun startProcess(commandLine: GeneralCommandLine) {
        val handler = OSProcessHandler(commandLine)
        ProcessTerminatedListener.attach(handler)
        handler.addProcessListener(
            object : ProcessListener {
                override fun processTerminated(event: ProcessEvent) {
                    tempRoot.deleteRecursively()
                }
            },
        )

        ApplicationManager.getApplication().invokeLater {
            val title = "Live Preview: $functionName"
            val runManager = RunContentManager.getInstance(project)

            // Target the Debug Executor to enable JVM HotSwap
            val executor = DefaultDebugExecutor.getDebugExecutorInstance()

            runManager.allDescriptors.find { it.displayName == title }?.let { oldDescriptor ->
                val oldHandler = oldDescriptor.processHandler
                if (oldHandler != null && !oldHandler.isProcessTerminated) {
                    oldHandler.destroyProcess()
                }
                runManager.removeRunContent(executor, oldDescriptor)
            }

            val consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
            consoleView.attachToProcess(handler)

            val descriptor =
                RunContentDescriptor(
                    consoleView,
                    handler,
                    JPanel(BorderLayout()).apply { add(consoleView.component, BorderLayout.CENTER) },
                    title,
                )

            runManager.showRunContent(executor, descriptor)
            handler.startNotify()
        }
    }

    companion object {
        fun launchSafe(
            project: Project,
            functionName: String,
            fqName: String,
            psiFile: PsiFile? = null,
        ) {
            // 1. Syntax Error Check (if PSI context is provided)
            if (psiFile != null && PsiTreeUtil.hasErrorElements(psiFile)) {
                Messages.showErrorDialog(
                    project,
                    "${LivePreviewBundle.message("prevew.error.message")} -> ${psiFile.name}",
                    LivePreviewBundle.message("prevew.error.title"),
                )
                return
            }

            // 2. Memory-to-Disk Buffer Flush
            if (psiFile != null) {
                PsiDocumentManager.getInstance(project).getDocument(psiFile)?.let { doc ->
                    FileDocumentManager.getInstance().saveDocument(doc)
                }
            } else {
                FileDocumentManager.getInstance().saveAllDocuments()
            }

            // 3. Asynchronous Process Spawning
            ApplicationManager.getApplication().executeOnPooledThread {
                RunPreview(project, functionName, fqName).execute()
            }
        }
    }
}
