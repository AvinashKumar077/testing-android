import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.google.samples.apps.nowinandroid.configureJacoco
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class AndroidJacocoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("jacoco")

            val jacocoExtension: AndroidComponentsExtension<*, *, *>
            val androidExtension: CommonExtension<*, *, *, *, *, *> = when {
                pluginManager.hasPlugin("com.android.application") -> {
                    jacocoExtension = the<ApplicationAndroidComponentsExtension>()
                    the<BaseAppModuleExtension>()
                }

                pluginManager.hasPlugin("com.android.library") -> {
                    jacocoExtension = the<LibraryAndroidComponentsExtension>()
                    the<LibraryExtension>()
                }

                else -> TODO("This plugin is dependent on either nowinandroid.android.application or nowinandroid.android.library. Apply one of those plugins first.")
            }

            androidExtension.buildTypes.configureEach {
                enableAndroidTestCoverage = true
                enableUnitTestCoverage = true
            }

            configureJacoco(jacocoExtension)
        }
    }
}
