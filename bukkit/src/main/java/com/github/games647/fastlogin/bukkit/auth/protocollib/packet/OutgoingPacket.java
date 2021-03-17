package com.github.games647.fastlogin.bukkit.auth.protocollib.packet;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import java.security.PublicKey;

import static com.comphenix.protocol.PacketType.Login.Server.DISCONNECT;
import static com.comphenix.protocol.PacketType.Login.Server.ENCRYPTION_BEGIN;

public class OutgoingPacket {

    public static class Disconnect {

        public static PacketContainer create(WrappedChatComponent reason) {
            PacketContainer kickPacket = new PacketContainer(DISCONNECT);
            kickPacket.getChatComponents().write(0, reason);
            return kickPacket;
        }
    }

    /**
     * Packet Information: https://wiki.vg/Protocol#Encryption_Request
     *
     * ServerID="" (String) key=public server key verifyToken=random 4 byte array
     */
    public static class EncryptionRequest {

        private final PacketContainer packet;

        private EncryptionRequest(PacketContainer packet) {
            this.packet = packet;
        }

        public static EncryptionRequest create() {
            return new EncryptionRequest(new PacketContainer(ENCRYPTION_BEGIN));
        }

        public EncryptionRequest verifyToken(byte[] verifyToken) {
            // Choose the last field, because in newer versions the public is now also a byte field
            int byteModifierSize = packet.getByteArrays().size();
            packet.getByteArrays().write(byteModifierSize - 1, verifyToken);
            return this;
        }

        /**
         * Specifies the server id, in vanilla version not specified.
         *
         * @param serverId server id representation
         * @return this builder
         */
        public EncryptionRequest serverId(String serverId) {
            packet.getStrings().write(0, serverId);
            return this;
        }

        public EncryptionRequest publicKey(PublicKey publicKey) {
            StructureModifier<PublicKey> keyModifier = packet.getSpecificModifier(PublicKey.class);
            if (keyModifier.getFields().isEmpty()) {
                // Since 1.16.4 this is now a encoded byte field
                packet.getByteArrays().write(0, publicKey.getEncoded());
            } else {
                keyModifier.write(0, publicKey);
            }

            return this;
        }

        public PacketContainer build() {
            return packet;
        }
    }
}
