package cn.zb.study.demo.protocol;

import static cn.zb.study.demo.protocol.command.Command.CREATE_GROUP_REQUEST;
import static cn.zb.study.demo.protocol.command.Command.CREATE_GROUP_RESPONSE;
import static cn.zb.study.demo.protocol.command.Command.GROUP_MESSAGE_REQUEST;
import static cn.zb.study.demo.protocol.command.Command.GROUP_MESSAGE_RESPONSE;
import static cn.zb.study.demo.protocol.command.Command.HEARTBEAT_REQUEST;
import static cn.zb.study.demo.protocol.command.Command.HEARTBEAT_RESPONSE;
import static cn.zb.study.demo.protocol.command.Command.JOIN_GROUP_REQUEST;
import static cn.zb.study.demo.protocol.command.Command.JOIN_GROUP_RESPONSE;
import static cn.zb.study.demo.protocol.command.Command.LIST_GROUP_MEMBERS_REQUEST;
import static cn.zb.study.demo.protocol.command.Command.LIST_GROUP_MEMBERS_RESPONSE;
import static cn.zb.study.demo.protocol.command.Command.LOGIN_REQUEST;
import static cn.zb.study.demo.protocol.command.Command.LOGIN_RESPONSE;
import static cn.zb.study.demo.protocol.command.Command.LOGOUT_REQUEST;
import static cn.zb.study.demo.protocol.command.Command.LOGOUT_RESPONSE;
import static cn.zb.study.demo.protocol.command.Command.MESSAGE_REQUEST;
import static cn.zb.study.demo.protocol.command.Command.MESSAGE_RESPONSE;
import static cn.zb.study.demo.protocol.command.Command.QUIT_GROUP_REQUEST;
import static cn.zb.study.demo.protocol.command.Command.QUIT_GROUP_RESPONSE;
import cn.zb.study.demo.protocol.request.CreateGroupRequestPacket;
import cn.zb.study.demo.protocol.request.GroupMessageRequestPacket;
import cn.zb.study.demo.protocol.request.HeartBeatRequestPacket;
import cn.zb.study.demo.protocol.request.JoinGroupRequestPacket;
import cn.zb.study.demo.protocol.request.ListGroupMembersRequestPacket;
import cn.zb.study.demo.protocol.request.LoginRequestPacket;
import cn.zb.study.demo.protocol.request.LogoutRequestPacket;
import cn.zb.study.demo.protocol.request.MessageRequestPacket;
import cn.zb.study.demo.protocol.request.QuitGroupRequestPacket;
import cn.zb.study.demo.protocol.response.CreateGroupResponsePacket;
import cn.zb.study.demo.protocol.response.GroupMessageResponsePacket;
import cn.zb.study.demo.protocol.response.HeartBeatResponsePacket;
import cn.zb.study.demo.protocol.response.JoinGroupResponsePacket;
import cn.zb.study.demo.protocol.response.ListGroupMembersResponsePacket;
import cn.zb.study.demo.protocol.response.LoginResponsePacket;
import cn.zb.study.demo.protocol.response.LogoutResponsePacket;
import cn.zb.study.demo.protocol.response.MessageResponsePacket;
import cn.zb.study.demo.protocol.response.QuitGroupResponsePacket;
import cn.zb.study.demo.serialize.Serializer;
import cn.zb.study.demo.serialize.impl.JSONSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Description: 通信包编解码
 *
 * <pre>
 * **********************************************************************
 *                                Protocol（协议）
 * +-------+----------+------------+----------+---------+---------------+
 * |       |          |            |          |         |               |
 * |   4   |     1    |     1      |    1     |    4    |       N       |
 * +--------------------------------------------------------------------+
 * |       |          |            |          |         |               |
 * | magic |  version | serializer | command  |  length |      body     |
 * |       |          |            |          |         |               |
 * +-------+----------+------------+----------+---------+---------------+
 * 消息头11个字节定长
 * magic 4 // 魔数,magic = (int) 0x12345678
 * version 1 // 版本号,通常情况下时预留字段,用于协议升级的时候用到.
 * serializer 1 // 序列化算法,如何把Java对象转换二进制数据已经二进制数据如何转换回Java对象
 * command 1 // 指令
 * length 4 // 数据部分的长度,int类型
 * body N //数据
 * </pre>
 *
 * @Author: zb
 * @Date: 2020-02-27
 */
public class PacketCodec {

    /**
     * 魔数
     */
    public static final int MAGIC_NUMBER = 0x12345678;

    /**
     * 单例对象
     */
    public static final PacketCodec INSTANCE = new PacketCodec();

    /**
     * 通信包map
     */
    private final Map<Byte, Class<? extends Packet>> packetMap;

    /**
     * 序列化map
     */
    private final Map<Byte, Serializer> serializerMap;

    /**
     * 数据初始化
     */
    public PacketCodec() {
        packetMap = new HashMap<>();
        packetMap.put(LOGIN_REQUEST, LoginRequestPacket.class);
        packetMap.put(LOGIN_RESPONSE, LoginResponsePacket.class);
        packetMap.put(MESSAGE_REQUEST, MessageRequestPacket.class);
        packetMap.put(MESSAGE_RESPONSE, MessageResponsePacket.class);
        packetMap.put(LOGOUT_REQUEST, LogoutRequestPacket.class);
        packetMap.put(LOGOUT_RESPONSE, LogoutResponsePacket.class);
        packetMap.put(CREATE_GROUP_REQUEST, CreateGroupRequestPacket.class);
        packetMap.put(CREATE_GROUP_RESPONSE, CreateGroupResponsePacket.class);
        packetMap.put(JOIN_GROUP_REQUEST, JoinGroupRequestPacket.class);
        packetMap.put(JOIN_GROUP_RESPONSE, JoinGroupResponsePacket.class);
        packetMap.put(QUIT_GROUP_REQUEST, QuitGroupRequestPacket.class);
        packetMap.put(QUIT_GROUP_RESPONSE, QuitGroupResponsePacket.class);
        packetMap.put(LIST_GROUP_MEMBERS_REQUEST, ListGroupMembersRequestPacket.class);
        packetMap.put(LIST_GROUP_MEMBERS_RESPONSE, ListGroupMembersResponsePacket.class);
        packetMap.put(GROUP_MESSAGE_REQUEST, GroupMessageRequestPacket.class);
        packetMap.put(GROUP_MESSAGE_RESPONSE, GroupMessageResponsePacket.class);
        packetMap.put(HEARTBEAT_REQUEST, HeartBeatRequestPacket.class);
        packetMap.put(HEARTBEAT_RESPONSE, HeartBeatResponsePacket.class);

        serializerMap = new HashMap<>();
        Serializer serializer = new JSONSerializer();
        serializerMap.put(serializer.getSerializerAlgorithm(), serializer);
    }

    /**
     * 编码过程
     */
    public ByteBuf encode(ByteBuf byteBuf, Packet packet) {
        // 1. 序列化 Java 对象
        byte[] bytes = Serializer.DEFAULT.serialize(packet);

        // 2. 编码过程
        byteBuf.writeInt(MAGIC_NUMBER);
        byteBuf.writeByte(packet.getVersion());
        byteBuf.writeByte(Serializer.DEFAULT.getSerializerAlgorithm());
        byteBuf.writeByte(packet.getCommand());
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);

        return byteBuf;
    }

    /**
     * 解码过程
     */
    public Packet decode(ByteBuf byteBuf) {
        // 跳过魔数
        byteBuf.skipBytes(4);
        // 跳过版本号
        byteBuf.skipBytes(1);
        // 序列化算法标识
        byte serializeAlgorithm = byteBuf.readByte();
        // 指令
        byte command = byteBuf.readByte();
        // 数据包长度
        int length = byteBuf.readInt();
        // 数据
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        Class<? extends Packet> packet = getPacket(command);
        Serializer serializer = getSerializer(serializeAlgorithm);
        if (Objects.nonNull(packet) && Objects.nonNull(serializer)) {
            return serializer.deserialize(packet, bytes);
        }
        return null;
    }

    /**
     * 获取序列化
     */
    private Serializer getSerializer(byte serializeAlgorithm) {
        return serializerMap.get(serializeAlgorithm);
    }

    /**
     * 获取通信包
     */
    private Class<? extends Packet> getPacket(byte command) {
        return packetMap.get(command);
    }
}
