/*
 * Copyright (c) 2015, Minecrell <https://github.com/Minecrell>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.cadixdev.gradle.gitpatcher


import org.cadixdev.gradle.gitpatcher.task.FindGitTask
import org.cadixdev.gradle.gitpatcher.task.UpdateSubmodulesTask
import org.cadixdev.gradle.gitpatcher.task.patch.ApplyPatchesTask
import org.cadixdev.gradle.gitpatcher.task.patch.MakePatchesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class GitPatcher implements Plugin<Project> {

    protected Project project
    protected PatchExtension patchExtension

    @Override
    void apply(Project project) {
        this.project = project
        project.with {
            this.patchExtension = extensions.create('patches', PatchExtension)
            patchExtension.root = projectDir

            task('findGit', type: FindGitTask)
            task('updateSubmodules', type: UpdateSubmodulesTask, dependsOn: 'findGit')
            task('applyPatches', type: ApplyPatchesTask, dependsOn: 'updateSubmodules')
            task('makePatches', type: MakePatchesTask, dependsOn: 'findGit')

            afterEvaluate {
                // Configure the settings from our extension
                tasks.findGit.submodule = patchExtension.submodule

                configure([tasks.applyPatches, tasks.makePatches]) {
                    repo = patchExtension.target
                    root = patchExtension.root
                    submodule = patchExtension.submodule
                    patchDir = patchExtension.patches
                }

                tasks.makePatches.with {
                    formatPatchArgs = patchExtension.formatPatchArgs
                }

                tasks.applyPatches.updateTask = tasks.updateSubmodules

                tasks.updateSubmodules.with {
                    repo = patchExtension.root
                    submodule = patchExtension.submodule
                }
            }
        }
    }

    Project getProject() {
        return project
    }

    PatchExtension getPatchExtension() {
        return patchExtension
    }

}
