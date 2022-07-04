package com.adafruit.pyleap

import android.content.Context
import com.adafruit.pyleap.model.ProjectsRepository
import com.adafruit.pyleap.model.ProjectsRepositoryImpl
import io.openroad.ble.filetransfer.FileTransferConnectionManager
import io.openroad.ble.filetransfer.FileTransferConnectionManagerImpl
import io.openroad.ble.scanner.BlePeripheralScanner
import io.openroad.ble.state.BleStateDataSource
import io.openroad.ble.state.BleStateRepository
import io.openroad.ble.state.BleStateRepositoryImpl
import kotlinx.coroutines.MainScope

/**
 * Created by Antonio García (antonio@openroad.es)
 */

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val projectsRepository: ProjectsRepository
    val bleStateRepository: BleStateRepository
    val filetransferConnectionManager: FileTransferConnectionManager
}

/**
 * Implementation for the Dependency Injection container at the application level.
 *
 * Variables are initialized lazily and the same instance is shared across the whole app.
 */
class AppContainerImpl(private val applicationContext: Context) : AppContainer {
    override val projectsRepository: ProjectsRepository by lazy {
        ProjectsRepositoryImpl(context = applicationContext)
    }

    override val bleStateRepository: BleStateRepository by lazy {
        val bleStateDataSource = BleStateDataSource(context = applicationContext)
        BleStateRepositoryImpl(bleStateDataSource, MainScope())
    }

    override val filetransferConnectionManager: FileTransferConnectionManager by lazy {
        FileTransferConnectionManagerImpl(context = applicationContext)
    }
}
