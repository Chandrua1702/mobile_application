package com.example.home

import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import javax.net.ssl.SSLSocketFactory

class MqttManager {
    private var client: MqttClient? = null

    fun connect(brokerUrl: String, clientId: String, user: String, pass: String, onStateChange: (Boolean) -> Unit) {
        try {
            val url = if (brokerUrl.startsWith("ssl://")) brokerUrl else "ssl://$brokerUrl"
            client = MqttClient(url, clientId, MemoryPersistence())

            val options = MqttConnectOptions().apply {
                userName = user
                password = pass.toCharArray()
                isAutomaticReconnect = true
                isCleanSession = false
                connectionTimeout = 20
                keepAliveInterval = 60
                socketFactory = SSLSocketFactory.getDefault()
            }

            client?.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    onStateChange(true)
                }
                override fun connectionLost(cause: Throwable?) {
                    onStateChange(false)
                }
                override fun messageArrived(topic: String?, message: MqttMessage?) {}
                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            client?.connect(options)
        } catch (e: Exception) {
            onStateChange(false)
        }
    }

    fun publish(topic: String, message: String) {
        if (client?.isConnected == true) {
            val msg = MqttMessage(message.toByteArray()).apply { qos = 1 }
            client?.publish(topic, msg)
        }
    }

    fun disconnect() {
        client?.disconnect()
    }
}