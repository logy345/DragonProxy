/*
 * DragonProxy
 * Copyright (C) 2016-2019 Dragonet Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You can view the LICENSE file for more details.
 *
 * https://github.com/DragonetMC/DragonProxy
 */
package org.dragonet.proxy.network.translator.java;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerRespawnPacket;
import com.nukkitx.protocol.bedrock.packet.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.dragonet.proxy.network.session.ProxySession;
import org.dragonet.proxy.network.session.cache.object.CachedPlayer;
import org.dragonet.proxy.network.translator.PacketTranslator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class PCRespawnTranslator implements PacketTranslator<ServerRespawnPacket> {
    public static final PCRespawnTranslator INSTANCE = new PCRespawnTranslator();

    @Override
    public void translate(ProxySession session, ServerRespawnPacket packet) {
        CachedPlayer cachedPlayer = session.getCachedEntity();
        int dimension = 0;

        switch(packet.getDimension()) {
            case 0: dimension = 0; break;
            case -1: dimension = 1; break;
            case 1: dimension = 2; break;
            default:
                log.warn("Unknown dimension: " + packet.getDimension());
                break;
        }

        if (cachedPlayer.getDimension() == dimension) {
            return;
        }

        cachedPlayer.setDimension(dimension);

        ChangeDimensionPacket changeDimensionPacket = new ChangeDimensionPacket();
        changeDimensionPacket.setDimension(dimension);
        changeDimensionPacket.setRespawn(false);
        changeDimensionPacket.setPosition(cachedPlayer.getPosition());
        session.sendPacket(changeDimensionPacket);

        SetPlayerGameTypePacket playerGameTypePacket = new SetPlayerGameTypePacket();
        playerGameTypePacket.setGamemode(packet.getGamemode().ordinal());
        session.sendPacket(playerGameTypePacket);

        PlayStatusPacket playStatusPacket = new PlayStatusPacket();
        playStatusPacket.setStatus(PlayStatusPacket.Status.PLAYER_SPAWN);
        session.sendPacket(playStatusPacket);
    }
}
