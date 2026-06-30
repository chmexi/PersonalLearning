package com.example.personallearning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.personallearning.ui.screen.daohen.DaoHenScreen
import com.example.personallearning.ui.screen.express.ExpressExerciseScreen
import com.example.personallearning.ui.screen.express.ExpressHomeScreen
import com.example.personallearning.ui.screen.history.HistoryScreen
import com.example.personallearning.ui.screen.home.HomeScreen
import com.example.personallearning.ui.theme.PersonalLearningTheme
import com.example.personallearning.ui.viewmodel.DaoHenViewModel
import com.example.personallearning.ui.viewmodel.ExpressViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalLearningTheme {
                val navController = rememberNavController()
                val daoHenViewModel: DaoHenViewModel = viewModel()
                val expressViewModel: ExpressViewModel = viewModel()

                NavHost(navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onDaoHenClick = { navController.navigate("daohen") },
                            onExpressClick = { navController.navigate("express") }
                        )
                    }

                    // 道痕
                    composable("daohen") {
                        DaoHenScreen(
                            viewModel = daoHenViewModel,
                            onBack = { navController.popBackStack() },
                            onHistoryClick = { navController.navigate("history") }
                        )
                    }
                    composable("history") {
                        HistoryScreen(
                            viewModel = daoHenViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 表达训练
                    composable("express") {
                        ExpressHomeScreen(
                            viewModel = expressViewModel,
                            onBack = { navController.popBackStack() },
                            onExerciseClick = { id -> navController.navigate("express_exercise/$id") }
                        )
                    }
                    composable(
                        "express_exercise/{exerciseId}",
                        arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: return@composable
                        ExpressExerciseScreen(
                            viewModel = expressViewModel,
                            exerciseId = exerciseId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
