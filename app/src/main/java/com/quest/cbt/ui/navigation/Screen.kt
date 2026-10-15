package com.quest.cbt.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Upload : Screen("upload")
    data object Progress : Screen("progress")
    data object Settings : Screen("settings")

    data object Instructions : Screen("instructions/{testId}") {
        fun createRoute(testId: String) = "instructions/$testId"
    }

    data object Syllabus : Screen("syllabus/{testId}") {
        fun createRoute(testId: String) = "syllabus/$testId"
    }

    data object CbtWindow : Screen("cbt/{testId}") {
        fun createRoute(testId: String) = "cbt/$testId"
    }

    data object Result : Screen("result/{attemptId}") {
        fun createRoute(attemptId: String) = "result/$attemptId"
    }
}
