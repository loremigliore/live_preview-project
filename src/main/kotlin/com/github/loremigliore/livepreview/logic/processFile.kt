package com.github.loremigliore.livepreview.logic

import com.github.loremigliore.livepreview.domain.ComposableData
import com.github.loremigliore.livepreview.domain.ComposableParameter
import com.github.loremigliore.livepreview.domain.LivePreviewState
import com.github.loremigliore.livepreview.utils.PreviewFileManager
import com.github.loremigliore.livepreview.utils.livePreviewStateHolder
import com.github.loremigliore.livepreview.utils.signatureListenerService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtFile

fun processFile(
    project: Project,
    file: VirtualFile,
) {
    project.livePreviewStateHolder.reset()
    if (file.extension != "kt") return

    val previousStateList = project.livePreviewStateHolder.state.value.composables

    val extracted =
        ApplicationManager.getApplication().runReadAction<List<Pair<ComposableData, List<ComposableParameter>>>> {
            val psiFile = PsiManager.getInstance(project).findFile(file) as? KtFile ?: return@runReadAction emptyList()
            val currentComposables = PreviewFileManager.getCurrentComposables(psiFile)

            currentComposables.mapNotNull { composable ->
                val name = composable.name ?: return@mapNotNull null
                val ktFile = composable.containingKtFile
                val packageName = ktFile.packageFqName.asString()

                val fqName =
                    if (packageName.isNotEmpty()) {
                        "$packageName.$name"
                    } else {
                        name
                    }

                val params =
                    composable.valueParameters.mapNotNull { param ->
                        val paramName = param.name ?: return@mapNotNull null
                        val type = param.typeReference?.text ?: "Any"
                        val defaultValueString =
                            if (param.hasDefaultValue()) {
                                param.defaultValue?.text ?: "/* $type */"
                            } else {
                                "/* $type */"
                            }
                        ComposableParameter(paramName, type, defaultValueString)
                    }

                ComposableData(fqName, name, params, "") to params
            }
        }

    if (extracted.isEmpty()) return

    ApplicationManager.getApplication().invokeLater {
        val finalDetails =
            extracted.mapIndexed { index, (newData, newParams) ->
                val previousData = previousStateList.getOrNull(index)
                var savedCode = PreviewFileManager.readPreviewCode(project, newData.fqName)

                if (previousData != null && savedCode == null) {
                    val oldCode = PreviewFileManager.readPreviewCode(project, previousData.fqName)
                    if (oldCode != null) {
                        savedCode = oldCode
                        PreviewFileManager.deletePreviewCode(project, previousData.fqName) // Assuming delete logic is added
                    }
                }

                if (savedCode == null) {
                    val args = newParams.joinToString(", ") { "${it.name} = ${it.defaultValue}" }
                    savedCode = "${newData.name}($args)"
                } else {
                    var updatedCode: String = savedCode

                    if (previousData != null) {
                        if (previousData.name != newData.name) {
                            updatedCode = updatedCode.replaceFirst("${previousData.name}(", "${newData.name}(")
                        }

                        val maxParams = minOf(previousData.parameters.size, newParams.size)
                        for (i in 0 until maxParams) {
                            val oldP = previousData.parameters[i]
                            val newP = newParams[i]

                            if (oldP.name != newP.name) {
                                val renameRegex = Regex("\\b${oldP.name}\\s*=")
                                // replaceFirst is safe here because Kotlin parameter names cannot contain $ or regex chars
                                updatedCode = updatedCode.replaceFirst(renameRegex, "${newP.name} =")
                            }

// b. Initializer (Default Value) Changed
                            val isComment = newP.defaultValue.startsWith("/*")
                            if (oldP.defaultValue != newP.defaultValue && !isComment) {
                                val valueRegex = Regex("(\\b${newP.name}\\s*=\\s*)([^,)]+)")

                                // Safely replace ONLY the first match to prevent doubling,
                                // without risking Regex group parsing crashes if defaultValue contains a '$'
                                val match = valueRegex.find(updatedCode)
                                if (match != null) {
                                    val before = updatedCode.substring(0, match.range.first)
                                    val after = updatedCode.substring(match.range.last + 1)
                                    updatedCode = "$before${match.groupValues[1]}${newP.defaultValue}$after"
                                }
                            }
                        }

                        if (newParams.size > previousData.parameters.size) {
                            for (i in previousData.parameters.size until newParams.size) {
                                val newP = newParams[i]
                                val newArg = "${newP.name} = ${newP.defaultValue}"
                                updatedCode =
                                    if (updatedCode.contains("()")) {
                                        updatedCode.replace("()", "($newArg)")
                                    } else {
                                        updatedCode.replace(")", ", $newArg)")
                                    }
                            }
                        }
                    }
                    savedCode = updatedCode
                }

                PreviewFileManager.writePreviewCode(project, newData.fqName, savedCode)
                file.refresh(true, true)

                newData.copy(code = savedCode)
            }

        project.livePreviewStateHolder.updateComposables(LivePreviewState(composables = finalDetails))
        project.signatureListenerService.startWatching(file)
    }
}
