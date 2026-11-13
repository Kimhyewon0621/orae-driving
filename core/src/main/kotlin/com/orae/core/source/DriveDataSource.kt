package com.orae.core.source

import com.orae.core.model.DriveSample
import com.orae.core.model.NaviEvent
import kotlinx.coroutines.flow.Flow

/**
 * 주행 데이터가 흘러나오는 곳.
 *
 * 구현은 두 가지다.
 *  - ScenarioDataSource : JSON 시나리오를 재생 (시연·테스트용, 이 모듈에 있음)
 *  - PleosDataSource    : 실제 Vehicle SDK·NaviHelper를 구독 (app 모듈에서 구현, docs/SDK_INTEGRATION.md)
 *
 * 엔진과 화면은 이 인터페이스만 보므로, 어느 쪽을 꽂아도 똑같이 동작한다.
 */
interface DriveDataSource {
    fun samples(): Flow<DriveSample>
    fun naviEvents(): Flow<NaviEvent>
}
