package cn.zb.study.demo.protocol.request;

import cn.zb.study.demo.protocol.Packet;
import cn.zb.study.demo.protocol.command.Command;
import lombok.Data;

/**
 * @Description: 消息请求数据包
 * @Author: zb
 * @Date: 2020-02-27
 */
@Data
public class MessageRequestPacket extends Packet {

    /**
     * 消息
     */
    private String message;

    @Override
    public Byte getCommand() {
        return Command.MESSAGE_REQUEST;
    }
}