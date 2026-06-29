package com.example.personallearning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.personallearning.ui.screen.daohen.DaoHenScreen
import com.example.personallearning.ui.screen.home.HomeScreen
import com.example.personallearning.ui.theme.PersonalLearningTheme
import com.example.personallearning.ui.viewmodel.DaoHenViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalLearningTheme {
                val navController = rememberNavController()
                val daoHenViewModel: DaoHenViewModel = viewModel()
                NavHost(navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onDaoHenClick = { navController.navigate("daohen") }
                        )
                    }
                    composable("daohen") {
                        DaoHenScreen(
                            viewModel = daoHenViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
