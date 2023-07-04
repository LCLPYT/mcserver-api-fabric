package work.lclpnet.serverimpl.kibu.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.MathHelper;
import work.lclpnet.serverapi.msg.MCMessage;

import java.util.List;

public class KibuMCMessageImpl {

    private final KibuServerTranslation translations;

    protected KibuMCMessageImpl(KibuServerTranslation translations) {
        this.translations = translations;
    }

    public MutableText convert(MCMessage msg, ServerPlayerEntity receiver) {
        MutableText root = Text.empty();

        append(root, msg, null, receiver);

        return root;
    }

    private void append(MutableText parent, MCMessage msg, MCMessage.MessageStyle parentStyle, ServerPlayerEntity receiver) {
        MCMessage.MessageStyle style = switch (msg.getColorMode()) {
            case INHERIT -> parentStyle != null ? parentStyle : msg.getStyle();
            case LOCAL -> msg.getStyle();
        };

        if (!msg.isTextNode()) {
            for (MCMessage child : msg.getChildren()) {
                append(parent, child, style, receiver);
            }

            return;
        }

        final String content;

        if (msg instanceof MCMessage.MCTranslationMessage translationMsg) {
            List<MCMessage> substituteList = translationMsg.getSubstitutes();
            String[] substitutes = new String[substituteList.size()];

            for (int i = 0; i < substituteList.size(); i++) {
                MCMessage subMsg = substituteList.get(i);
                substitutes[i] = subMsg.getText();  // TODO replace with convert(subMsg) and replace accordingly
            }

            content = translations.translate(receiver, translationMsg.getText(), (Object[]) substitutes);
        } else {
            content = msg.getText();
        }

        Style mcStyle = convert(style);

        MutableText text = Text.literal(content).setStyle(mcStyle);

        parent.append(text);
    }

    private Style convert(MCMessage.MessageStyle messageStyle) {
        Style style = Style.EMPTY;

        if (messageStyle.isReset()) {
            return style;
        }

        MCMessage.MessageColor color = messageStyle.getColor();
        if (color != null) {
            int rgb = 0x000000;
            rgb |= MathHelper.clamp(color.red, 0, 255) << 16;
            rgb |= MathHelper.clamp(color.green, 0, 255) << 8;
            rgb |= MathHelper.clamp(color.blue, 0, 255);

            style = style.withColor(TextColor.fromRgb(rgb));
        }

        if (messageStyle.isBold()) style = style.withBold(true);
        if (messageStyle.isItalic()) style = style.withItalic(true);
        if (messageStyle.isObfuscated()) style = style.withObfuscated(true);
        if (messageStyle.isStrikethrough()) style = style.withStrikethrough(true);
        if (messageStyle.isUnderline()) style = style.withUnderline(true);

        return style;
    }
}
