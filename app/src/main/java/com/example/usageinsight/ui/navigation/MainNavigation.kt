package com.example.usageinsight.ui.navigation

import androidx.compose.material3.NavigationBar as BottomNavigation
import androidx.compose.material3.NavigationBarItem as BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.usageinsight.ui.fitness.FitnessScreen
import com.example.usageinsight.ui.home.HomeScreen
import com.example.usageinsight.ui.settings.SettingsScreen
import com.example.usageinsight.ui.stats.StatsScreen

sealed class NavigationScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : NavigationScreen("home", "首页", Icons.Default.Home)
    data object Stats : NavigationScreen("stats", "统计", Icons.AutoMirrored.Filled.ShowChart)
    data object Settings : NavigationScreen("settings", "设置", Icons.Default.Settings)
    data object Fitness : NavigationScreen("fitness", "健康", Icons.AutoMirrored.Filled.DirectionsRun)

    companion object {
        val items = listOf(Home, Stats, Fitness, Settings)
    }
}

@Composable
fun MainNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavigationScreen.Home.route
    ) {
        composable(NavigationScreen.Home.route) {
            HomeScreen()
        }
        composable(NavigationScreen.Stats.route) {
            StatsScreen()
        }
        composable(NavigationScreen.Settings.route) {
            SettingsScreen()
        }
        composable(NavigationScreen.Fitness.route) {
            FitnessScreen()
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    BottomNavigation {
        NavigationScreen.items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
} 