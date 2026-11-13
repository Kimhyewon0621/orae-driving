package com.orae.core.model

import kotlinx.serialization.Serializable

/** 운전자가 내비에 저장해 둔 즐겨찾기 = "익숙한 목적지" (NaviHelper getBookmarkInfo) */
@Serializable
data class FamiliarDestination(
    val id: String,
    val name: String,
)

/** 시동부터 주차까지 한 번의 운행 */
@Serializable
data class Trip(
    val id: String,
    /** 운행 날짜 (예: 2026-03-05). 기준선 기간과 최근 기간을 나누는 데 씀 */
    val date: String,
    /** 시연용 설명 (예: "3월 평소 운전") */
    val label: String,
    val destinationId: String? = null,
    val samples: List<DriveSample>,
    val naviEvents: List<NaviEvent> = emptyList(),
)

/** scenarios 폴더의 JSON 파일 한 개의 전체 구조 */
@Serializable
data class ScenarioFile(
    val version: Int,
    val driverName: String,
    val familiarDestinations: List<FamiliarDestination>,
    val trips: List<Trip>,
)
