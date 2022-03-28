package handler.header;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import util.ByteUtil;
import util.CodecUtil;
import util.ProtocolConfig;

/**
 * @author 13090
 * @version 1.0
 * @description: header for shake hand
 * @date 2022/3/15 14:34
 */

public class ShakeHandHeader extends PlainHeader {

    public static int magicNum = 114514;

    public static int length = 4;

    public static ByteBuf getShakeHandHeader(Channel ch) {
        ByteBuf byteBuf = ch.alloc().buffer(length, length);
        byteBuf.writeInt(magicNum | (1 << LabelPosition.IS_SHAKE_HAND_HEAD.value()));
        return byteBuf;
    }

    public static boolean isShakeHandHeader(ByteBuf byteBuf) {
        int magicNum = byteBuf.getInt(0);
        return (magicNum & (1 << LabelPosition.IS_SHAKE_HAND_HEAD.value())) != 0;
    }

    public static boolean validShakeHandHeader(ByteBuf byteBuf) {
        int magicNum = byteBuf.getInt(0);
        return (magicNum & 0x1ffff) == ShakeHandHeader.magicNum;
    }

    public static ByteBuf getShakeHandHeader(Channel ch, ProtocolConfig protocolConfig) {
        ByteBuf byteBuf = ch.alloc().buffer(length, length);
        int label = magicNum | (1 << LabelPosition.IS_SHAKE_HAND_HEAD.value());
        label |= protocolConfig.getEnableDoubleCompression() ? (1 << LabelPosition.ENABLE_DOUBLE_COMPRESSION.value()) : 0;
        label |= ((protocolConfig.getDoubleCompressionAccuracy() & 0x0f) << LabelPosition.DOUBLE_COMPRESSION_ACCURACY.value());
        label |= ((protocolConfig.getVariableHeadByteLength() - 1) & 0x03) << LabelPosition.VARIABLE_BYTE_LENGTH.value();
        label |= (ProtocolConfig.convertCharSetToByte(protocolConfig.getCharset()) & 0x07) << LabelPosition.CHARSET.value();
        label |= (protocolConfig.getEnableBaseLineCompression() ? 1 : 0) << LabelPosition.ENABLE_BASELINE_COMPRESSION.value();
        byteBuf.writeInt(label);
        return byteBuf;
    }

    public static ByteBuf getShakeHandHeaderWithBaseLineBody(Channel ch, ProtocolConfig protocolConfig, Object baseLine) {
        protocolConfig.setEnableBaseLineCompression(false);
        CodecUtil codecUtil = new CodecUtil(protocolConfig);
        try {
            byte[] baseLineBytes = codecUtil.encode3(baseLine);
            protocolConfig.setEnableBaseLineCompression(true);
            int baseLineLength = baseLineBytes.length;
            ByteBuf byteBuf = ch.alloc().buffer(length + baseLineLength + 2, length + baseLineLength + 2);
            byteBuf.writeInt(getShakeHandHeader(ch, protocolConfig).getInt(0) | (1 << LabelPosition.IS_PLAIN_BODY_HEAD.value()));
            int classIdentity = CodecUtil.getIdentityByClass(baseLine.getClass()) & ByteUtil.getMask(PlainBodyHeader.PlainBodyLabelPosition.CLASS_IDENTITY.value());
            byte[] classIdentityBytes = new byte[2];
            classIdentityBytes[0] = (byte) ((classIdentity >> 8));
            classIdentityBytes[1] = (byte) (classIdentity & 0xff);
            byteBuf.writeBytes(classIdentityBytes);
            byteBuf.writeBytes(baseLineBytes);
            return byteBuf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
