package com.sensorsdata.analytics.android.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.invocation.DefaultGradle

class SensorsAnalyticsPlugin implements Plugin<Project> {
    void apply(Project project) {
        SensorsAnalyticsExtension extension = project.extensions.create("sensorsAnalytics", SensorsAnalyticsExtension)
        if (!extension.disableAppClick) {
            SensorsAnalyticsTransform sensorsAnalyticsTransform = new SensorsAnalyticsTransform(project,extension);
            AppExtension appExtension = project.extensions.findByType(AppExtension.class)
            appExtension.registerTransform(sensorsAnalyticsTransform)
        } else {
            println("------------您已关闭了插件--------------")
        }
    }
}