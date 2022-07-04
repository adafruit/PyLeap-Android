package io.openroad.ble.state

/**
 * Created by Antonio García (antonio@openroad.es)
 */
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

private const val kDefaultSubscribedTimeout =
    5000L     //  keep the upstream flow active after the disappearance of the last collector. That avoids restarting the upstream flow in certain situations such as configuration changes


interface BleStateRepository {
    val bleState: StateFlow<BleState>
}

class BleStateRepositoryImpl(
    bleStateDataSource: BleStateDataSource,
    externalScope: CoroutineScope
) : BleStateRepository {
    override val bleState: StateFlow<BleState> = bleStateDataSource.bleStateFlow.stateIn(
        externalScope, WhileSubscribed(
            kDefaultSubscribedTimeout
        ),
        BleState.Unknown
    )
}

class FakeBleStateRepository(state: BleState, externalScope: CoroutineScope = MainScope()) : BleStateRepository {
    override val bleState: StateFlow<BleState> = flow { emit(state) }.stateIn(
        externalScope, WhileSubscribed(
            kDefaultSubscribedTimeout
        ),
        BleState.Unknown
    )
}