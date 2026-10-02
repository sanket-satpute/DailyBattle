package com.sanket_satpute_20.dailybattle.presentation.startup

import com.sanket_satpute_20.dailybattle.presentation.state.ScreenState
import org.junit.Assert.assertEquals
import org.junit.Test

class AppStartupViewModelTest {
    @Test
    fun `initial startup state is success once the view model is constructed`() {
        val viewModel = AppStartupViewModel()

        assertEquals(ScreenState.Success(Unit), viewModel.state.value)
    }
}
