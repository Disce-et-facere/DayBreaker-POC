package com.example.daybreaker_drapes_app

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

class DeviceViewModel(application: Application) : AndroidViewModel(application) {
    private val mqttClient: MqttClient =
        MqttClient("ssl://10.0.2.2:8883", MqttClient.generateClientId(), MemoryPersistence())

    private val _devices = MutableLiveData<List<String>>() // For UI display (only aliases or fallback MACs)
    val devices: LiveData<List<String>> = _devices

    // Lists for managing devices by index
    val macAddresses = mutableListOf<String>()
    val aliases = mutableListOf<String?>() // Nullable to handle cases where alias is not set

    // Selected MAC Address (global to access in other fragments)
    private val _selectedMacAddress = MutableLiveData<String>()
    val selectedMacAddress: LiveData<String> get() = _selectedMacAddress

    // Set the selected MAC address by index
    fun selectMacAddress(index: Int) {
        _selectedMacAddress.value = macAddresses.getOrNull(index)
    }

    // Add or update a device in the lists
    fun addOrUpdateDevice(macAddress: String, alias: String?) {
        val index = macAddresses.indexOf(macAddress)
        if (index != -1) {
            // Update alias if the MAC already exists
            aliases[index] = alias
        } else {
            // Add new device
            macAddresses.add(macAddress)
            aliases.add(alias)
        }
        updateDeviceListDisplay()
    }

    // Check if an alias is unique (or already associated with the current MAC)
    fun isAliasUnique(alias: String, currentMac: String): Boolean {
        val index = aliases.indexOf(alias)
        return index == -1 || macAddresses[index] == currentMac
    }

    // Refresh the UI display of devices
    private fun updateDeviceListDisplay() {
        val displayList = aliases.mapIndexed { index, alias -> alias ?: macAddresses[index] }
        _devices.postValue(displayList)
    }

    // Retrieve alias by index
    fun getAlias(index: Int): String? = aliases.getOrNull(index) ?: macAddresses.getOrNull(index)

    // Retrieve MAC by index
    fun getMac(index: Int): String? = macAddresses.getOrNull(index)

    init {
        connectToMqttBroker()
        _devices.value = emptyList()
    }

    private fun connectToMqttBroker() {
        try {
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                socketFactory = createSocketFactory()
            }

            mqttClient.connect(options)
            Log.d("MQTT", "Connected to MQTT broker")

            // Publish "hello" message on app start
            publishToTopic("msgHandler/android/ping", "hello")

            // Subscribe to the primary device topic to fetch MAC addresses
            subscribeToTopic("android/devices") { _, message ->
                val macAddress = String(message.payload)
                Log.d("MQTT", "Received MAC address: $macAddress")

                // If it's a new MAC address, add it with MAC fallback as alias initially
                if (macAddresses.indexOf(macAddress) == -1) {
                    addOrUpdateDevice(macAddress, null)
                    subscribeToAliasTopic(macAddress)
                }
            }
        } catch (e: MqttException) {
            Log.e("MQTT", "Failed to connect to MQTT broker", e)
        }
    }

    private fun subscribeToAliasTopic(macAddress: String) {
        Log.d("AliasSub", "Subscribing to alias topic for MAC: $macAddress")

        subscribeToTopic("android/alias/$macAddress") { _, message ->
            val alias = String(message.payload)
            Log.d("MQTT", "Received alias for $macAddress: $alias")
            addOrUpdateDevice(macAddress, alias)
        }
    }

    // Helper function to publish messages
    fun publishToTopic(topic: String, message: String) {
        try {
            mqttClient.publish(topic, MqttMessage(message.toByteArray()))
            Log.d("MQTT", "Published message \"$message\" to topic $topic")
        } catch (e: MqttException) {
            Log.e("MQTT", "Failed to publish to topic $topic", e)
        }
    }

    // Helper function to subscribe to a topic with a message handler
    private fun subscribeToTopic(topic: String, handler: (String, MqttMessage) -> Unit = { _, _ -> }) {
        try {
            mqttClient.subscribe(topic, handler)
            Log.d("MQTT", "Subscribed to topic $topic")
        } catch (e: MqttException) {
            Log.e("MQTT", "Failed to subscribe to topic $topic", e)
        }
    }

    private fun createSocketFactory(): SSLSocketFactory {
        return try {
            val caInput: InputStream = getApplication<Application>().resources.openRawResource(R.raw.ca)
            val ca = CertificateFactory.getInstance("X.509").generateCertificate(caInput)
            caInput.close()
            Log.d("SSL", "CA certificate loaded successfully.")

            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setCertificateEntry("ca", ca)

            val clientInput: InputStream = getApplication<Application>().resources.openRawResource(R.raw.client)
            val clientKeyStore = KeyStore.getInstance("PKCS12")
            clientKeyStore.load(clientInput, "mqtt-broker".toCharArray())
            clientInput.close()

            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(clientKeyStore, "mqtt-broker".toCharArray())

            val trustManager = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagerFactory.keyManagers, arrayOf(trustManager), SecureRandom())
            Log.d("SSL", "SSL socket factory created successfully.")
            sslContext.socketFactory
        } catch (e: Exception) {
            Log.e("SSL", "Failed to create SSL socket factory", e)
            throw e
        }
    }
}
