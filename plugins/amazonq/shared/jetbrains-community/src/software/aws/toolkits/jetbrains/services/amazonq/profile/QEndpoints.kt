// Copyright 2025 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.amazonq.profile
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.util.registry.Registry
import software.aws.toolkits.core.utils.getLogger
import software.aws.toolkits.core.utils.warn

data class QRegionEndpoint(val region: String, val endpoint: String)

object QDefaultServiceConfig {
    const val REGION = "us-east-1"
    const val ENDPOINT = "https://codewhisperer.us-east-1.amazonaws.com/"

    val ENDPOINT_MAP = mapOf(
        "us-east-1" to "https://q.us-east-1.amazonaws.com/",
        "eu-central-1" to "https://q.eu-central-1.amazonaws.com/"
    )
}

object QEndpoints {
    private val LOG = getLogger<QEndpoints>()

    private fun parseEndpoints(): Map<String, String> {
        val rawJson = Registry.get("amazon.q.endpoints.json").asString().takeIf { it.isNotBlank() } ?: return QDefaultServiceConfig.ENDPOINT_MAP
        return try {
            val regionList: List<QRegionEndpoint> = jacksonObjectMapper().readValue(rawJson)
            regionList.associate { it.region to it.endpoint }
        } catch (e: Exception) {
            LOG.warn(e) { "Failed to parse amazon.q.endpoints.json: $rawJson" }
            QDefaultServiceConfig.ENDPOINT_MAP
        }
    }

    fun listRegionEndpoints(): List<QRegionEndpoint> = parseEndpoints().map { (region, endpoint) -> QRegionEndpoint(region, endpoint) }

    fun getQEndpointWithRegion(regionId: String): String {
        val all = parseEndpoints()
        return all[regionId]
            ?: error("No available endpoint for region=$regionId (check amazon.q.endpoints.json or default fallback)")
    }
}
