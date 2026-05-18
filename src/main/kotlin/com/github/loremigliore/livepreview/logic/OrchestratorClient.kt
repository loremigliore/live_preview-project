package com.github.loremigliore.livepreview.logic

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.roots.ProjectFileIndex
import java.io.DataOutputStream
import java.io.File
import java.net.Socket

object OrchestratorClient {
    fun sendHotReloadPayload(
        project: Project,
        sourceFilePath: String,
    ) {
        val virtualFile =
            com.intellij.openapi.vfs.LocalFileSystem
                .getInstance()
                .findFileByPath(sourceFilePath) ?: return
        val module = ProjectFileIndex.getInstance(project).getModuleForFile(virtualFile) ?: return

        val compilerExtension = CompilerModuleExtension.getInstance(module)
        val outputDir = compilerExtension?.compilerOutputPath?.path ?: return

        val fileNameWithoutExt = virtualFile.nameWithoutExtension
        val compiledClasses =
            File(outputDir)
                .walkTopDown()
                .filter {
                    it.isFile && it.extension == "class" && it.name.startsWith(fileNameWithoutExt)
                }.toList()

        if (compiledClasses.isNotEmpty()) {
            transmitToTarget(compiledClasses)
        }
    }

    private const val ORCHESTRATOR_PORT = 49787

    private fun transmitToTarget(classFiles: List<File>) {
        try {
            Socket("localhost", ORCHESTRATOR_PORT).use { socket ->
                DataOutputStream(socket.getOutputStream()).use { dos ->
                    // Protocol Specification (Standard JVM Class Reload Payload)
                    // 1. Send total number of classes
                    dos.writeInt(classFiles.size)

                    for (classFile in classFiles) {
                        val bytecode = classFile.readBytes()
                        // Reconstruct FQDN from relative path (assuming standard package structure)
                        val className = classFile.nameWithoutExtension

                        // 2. Send class name string length, then string
                        dos.writeUTF(className)

                        // 3. Send bytecode array length, then array
                        dos.writeInt(bytecode.size)
                        dos.write(bytecode)
                    }
                    dos.flush()
                }
            }
        } catch (e: Exception) {
            // Target process is likely terminated or port is unreachable
        }
    }
}
