package com.sensorsdata.analytics.android.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.invocation.DefaultGradle

class SensorsAnalyticsPlugin implements Plugin<Project> {
    void apply(Project project) {
        SensorsAnalyticsExtension extension = project.extensions.create("sensorsAnalytics", SensorsAnalyticsExtension)
        extension.excludeString = project.sensorsAnalytics.excludeString
        extension.disableAppClick = project.sensorsAnalytics.disableAppClick
        extension.disableCostTime =  project.sensorsAnalytics.disableCostTime
        project.afterEvaluate {
            if (extension.disableAppClick){
                println("已禁用点击事件统计")
            }

            if (extension.disableCostTime){
                println("已禁用方法耗时统计")
            }
            SensorsAnalyticsTransform sensorsAnalyticsTransform = new SensorsAnalyticsTransform(project,extension);
            AppExtension appExtension = project.extensions.findByType(AppExtension.class)
            appExtension.registerTransform(sensorsAnalyticsTransform)
        }
    }
}