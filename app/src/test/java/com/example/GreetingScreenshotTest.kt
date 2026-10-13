package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.repository.FamilyRepositoryImpl
import com.example.data.repository.MedicineRepositoryImpl
import com.example.data.repository.SessionRepositoryImpl
import com.example.domain.model.PasswordEntry
import com.example.domain.repository.PasswordRepository
import com.example.domain.usecase.FamilyUseCases
import com.example.domain.usecase.MedicineUseCases
import com.example.domain.usecase.PasswordUseCases
import com.example.domain.usecase.SessionUseCases
import com.example.ui.theme.AccessOsTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

class FakePasswordRepository : PasswordRepository {
    private val list = mutableListOf(
        PasswordEntry(id = 1, title = "Home Wi-Fi", username = "Sharma_5G", password = "SweetHome@2026")
    )
    override fun getAllPasswords(): Flow<List<PasswordEntry>> = flowOf(list)
    override suspend fun addPassword(password: PasswordEntry): Long = 1L
    override suspend fun updatePassword(password: PasswordEntry) {}
    override suspend fun deletePassword(id: Long) {}
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sessionRepo = SessionRepositoryImpl()
    val sessionUseCases = SessionUseCases(sessionRepo)
    val passwordRepo = FakePasswordRepository()
    val passwordUseCases = PasswordUseCases(passwordRepo, sessionRepo)
    val medicineRepo = MedicineRepositoryImpl()
    val medicineUseCases = MedicineUseCases(medicineRepo)
    val familyRepo = FamilyRepositoryImpl()
    val familyUseCases = FamilyUseCases(familyRepo)

    composeTestRule.setContent {
      AccessOsTheme {
        AccessOsApp(
            sessionUseCases = sessionUseCases,
            passwordUseCases = passwordUseCases,
            medicineUseCases = medicineUseCases,
            familyUseCases = familyUseCases
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
