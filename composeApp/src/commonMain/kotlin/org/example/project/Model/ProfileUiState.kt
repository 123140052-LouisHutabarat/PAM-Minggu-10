package org.example.project.Model

data class ProfileUiState(
    val name: String = "Louis Hutabarat",
    val bio: String = "Mahasiswa Teknik Informatika NIM 123140052. Tertarik pada Mobile Dev & IoT.",
    val email: String = "louis.123140052@student.itera.ac.id",
    val phone: String = "+62 813-9780-3190",
    val location: String = "Bandar Lampung, Indonesia",
    val isDarkMode: Boolean = false,
    val isEditing: Boolean = false
)