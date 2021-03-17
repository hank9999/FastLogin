package com.github.games647.fastlogin.bukkit.auth.protocollib.packet;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedGameProfile;

import java.util.UUID;

import static com.comphenix.protocol.PacketType.Login.Server.ENCRYPTION_BEGIN;

public class IncomingPacket {

    public static class LoginStart {

        private final PacketContainer packet;

        private LoginStart(PacketContainer packet) {
            this.packet = packet;
        }

        public static PacketContainer create(String username) {
            PacketContainer packet = new PacketContainer(ENCRYPTION_BEGIN);

            // uuid is ignored by the packet definition
            WrappedGameProfile fakeProfile = new WrappedGameProfile(UUID.randomUUID(), username);
            packet.getGameProfiles().write(0, fakeProfile);

            return packet;
        }

        public static LoginStart from(PacketContainer packet) {
            return new LoginStart(packet);
        }

        public String getUsername() {
            // player.getName() won't work at this state
            return packet.getGameProfiles().read(0).getName();
        }
    }

    public static class EncryptionReply {

        private final PacketContainer packet;

        private EncryptionReply(PacketContainer packet) {
            this.packet = packet;
        }

        public static EncryptionReply from(PacketContainer packet) {
            return new EncryptionReply(packet);
        }

        public byte[] getSharedSecret() {
            return packet.getByteArrays().read(0);
        }

        public byte[] getEncryptedVerifyToken() {
            return packet.getByteArrays().read(1);
        }
    }
}
