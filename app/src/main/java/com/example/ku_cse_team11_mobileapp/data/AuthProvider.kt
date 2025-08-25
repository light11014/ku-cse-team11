package com.example.ku_cse_team11_mobileapp.data

import android.content.Context
import com.example.ku_cse_team11_mobileapp.model.FakeAuthApi

object AuthProvider {
    fun provide(context: Context): AuthApi =
        FakeAuthApi()
}