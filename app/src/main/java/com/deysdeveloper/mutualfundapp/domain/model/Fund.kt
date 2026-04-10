package com.deysdeveloper.mutualfundapp.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a single search result from GET /mf/search?q={query}
 */
data class Fund(
    @SerializedName("schemeCode")
    val schemeCode: Int,

    @SerializedName("schemeName")
    val schemeName: String
)
