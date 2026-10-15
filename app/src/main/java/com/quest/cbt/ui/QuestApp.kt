package com.quest.cbt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quest.cbt.data.local.QuestDatabase
import com.quest.cbt.data.repository.AttemptRepository
import com.quest.cbt.data.repository.GoogleDriveCloudSyncRepository
import com.quest.cbt.data.repository.TestRepository
import com.quest.cbt.ui.navigation.QuestBottomBar
import com.quest.cbt.ui.navigation.Screen
import com.quest.cbt.ui.screens.cbt.CbtScreen
import com.quest.cbt.ui.screens.cbt.CbtViewModel
import com.quest.cbt.ui.screens.home.HomeScreen
import com.quest.cbt.ui.screens.instructions.InstructionsScreen
import com.quest.cbt.ui.screens.instructions.SyllabusScreen
import com.quest.cbt.ui.screens.progress.ProgressScreen
import com.quest.cbt.ui.screens.result.ResultAnalysisScreen
import com.quest.cbt.ui.screens.settings.SettingsScreen
import com.quest.cbt.ui.screens.upload.UploadScreen
import com.quest.cbt.ui.theme.DarkNavyBg

@Composable
fun QuestApp() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val database = remember { QuestDatabase.getInstance(context) }
    val testRepository = remember { TestRepository(database.testDao()) }
    val attemptRepository = remember { AttemptRepository(database.attemptDao()) }
    val cloudSyncRepository = remember {
        GoogleDriveCloudSyncRepository(context, testRepository, attemptRepository)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Upload.route,
        Screen.Progress.route
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavyBg),
        containerColor = DarkNavyBg,
        bottomBar = {
            if (showBottomBar) {
                QuestBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { targetRoute ->
                        if (targetRoute != currentRoute) {
                            navController.navigate(targetRoute) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        testRepository = testRepository,
                        attemptRepository = attemptRepository,
                        onStartTest = { testId ->
                            navController.navigate(Screen.Instructions.createRoute(testId))
                        },
                        onViewAnalysis = { attemptId ->
                            navController.navigate(Screen.Result.createRoute(attemptId))
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route)
                        },
                        onNavigateToUpload = {
                            navController.navigate(Screen.Upload.route)
                        }
                    )
                }

                composable(Screen.Upload.route) {
                    UploadScreen(
                        testRepository = testRepository,
                        onTestSaved = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Progress.route) {
                    ProgressScreen(
                        attemptRepository = attemptRepository,
                        onViewAttempt = { attemptId ->
                            navController.navigate(Screen.Result.createRoute(attemptId))
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        cloudSyncRepository = cloudSyncRepository,
                        testRepository = testRepository,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.Instructions.route,
                    arguments = listOf(navArgument("testId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val testId = backStackEntry.arguments?.getString("testId") ?: return@composable
                    InstructionsScreen(
                        testId = testId,
                        testRepository = testRepository,
                        onProceedToSyllabus = {
                            navController.navigate(Screen.Syllabus.createRoute(testId))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.Syllabus.route,
                    arguments = listOf(navArgument("testId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val testId = backStackEntry.arguments?.getString("testId") ?: return@composable
                    SyllabusScreen(
                        testId = testId,
                        testRepository = testRepository,
                        onStartTest = {
                            navController.navigate(Screen.CbtWindow.createRoute(testId))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.CbtWindow.route,
                    arguments = listOf(navArgument("testId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val testId = backStackEntry.arguments?.getString("testId") ?: return@composable
                    val cbtViewModel: CbtViewModel = viewModel(
                        key = "cbt_$testId",
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                return CbtViewModel(testId, testRepository, attemptRepository) as T
                            }
                        }
                    )
                    CbtScreen(
                        viewModel = cbtViewModel,
                        onTestSubmitted = { attemptId ->
                            navController.navigate(Screen.Result.createRoute(attemptId)) {
                                popUpTo(Screen.Home.route)
                            }
                        },
                        onExitTest = {
                            navController.popBackStack(Screen.Home.route, inclusive = false)
                        }
                    )
                }

                composable(
                    route = Screen.Result.route,
                    arguments = listOf(navArgument("attemptId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val attemptId = backStackEntry.arguments?.getString("attemptId") ?: return@composable
                    ResultAnalysisScreen(
                        attemptId = attemptId,
                        attemptRepository = attemptRepository,
                        testRepository = testRepository,
                        onNavigateHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
