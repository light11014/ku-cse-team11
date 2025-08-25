package com.example.ku_cse_team11_mobileapp.data

import android.content.Context
import com.example.ku_cse_team11_mobileapp.domain.CommunityRepository
import com.example.ku_cse_team11_mobileapp.model.community.FakeCommunityApi

object CommunityRepositoryProvider {
    fun provide(context: Context): CommunityRepository {
        return FakeCommunityRepository(FakeCommunityApi(networkDelayMs = 300))
    }
}