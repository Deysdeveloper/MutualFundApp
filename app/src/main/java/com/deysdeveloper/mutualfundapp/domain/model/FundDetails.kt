package com.deysdeveloper.mutualfundapp.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Top-level response from GET /mf/{schemeCode}
 */
data class FundDetailsResponse(
    @SerializedName("meta")
    val meta: FundMeta,

    @SerializedName("data")
    val data: List<NavEntry>,

    @SerializedName("status")
    val status: String
)

/**
 * Metadata section of the fund details response.
 */
data class FundMeta(
    @SerializedName("fund_house")
    val fundHouse: String,

    @SerializedName("scheme_type")
    val schemeType: String,

    @SerializedName("scheme_category")
    val schemeCategory: String,

    @SerializedName("scheme_code")
    val schemeCode: Int,

    @SerializedName("scheme_name")
    val schemeName: String
)

/**
 * A single NAV (Net Asset Value) history entry.
 */
data class NavEntry(
    @SerializedName("date")
    val date: String,

    @SerializedName("nav")
    val nav: String
)
