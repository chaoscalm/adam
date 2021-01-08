/*
 * Copyright (C) 2021 Anton Malinskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.malinskiy.adam.request.reverse

import com.malinskiy.adam.Const
import com.malinskiy.adam.request.ComplexRequest
import com.malinskiy.adam.request.NonSpecifiedTarget
import com.malinskiy.adam.request.forwarding.LocalPortSpec
import com.malinskiy.adam.request.forwarding.RemotePortSpec
import com.malinskiy.adam.transport.AndroidReadChannel
import com.malinskiy.adam.transport.AndroidWriteChannel
import java.nio.ByteBuffer

/**
 * Doesn't work with SerialTarget, have to use the serial as a parameter for the execute method
 */
class ListReversePortForwardsRequest : ComplexRequest<List<ReversePortForwardingRule>>(target = NonSpecifiedTarget) {
    override suspend fun readElement(readChannel: AndroidReadChannel, writeChannel: AndroidWriteChannel): List<ReversePortForwardingRule> {
        val sizeBuffer: ByteBuffer = ByteBuffer.allocate(4)
        readChannel.readFully(sizeBuffer)
        val size = String(sizeBuffer.array(), Const.DEFAULT_TRANSPORT_ENCODING).toInt(radix = 16)

        val payloadBuffer = ByteBuffer.allocate(size)
        readChannel.readFully(payloadBuffer)
        val payload = String(payloadBuffer.array(), Const.DEFAULT_TRANSPORT_ENCODING)
        return payload.lines().mapNotNull { line ->
            if (line.isNotEmpty()) {
                val split = line.split(" ")
                ReversePortForwardingRule(
                    serial = split[0],
                    localSpec = RemotePortSpec.parse(split[1]),
                    remoteSpec = LocalPortSpec.parse(split[2])
                )
            } else {
                null
            }
        }
    }

    override fun serialize() = createBaseRequest("reverse:list-forward")
}

