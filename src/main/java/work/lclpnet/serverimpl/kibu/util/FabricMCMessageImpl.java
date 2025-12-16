package work.lclpnet.serverimpl.kibu.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.serverapi.msg.MCMessage;

import java.util.List;

public class FabricMCMessageImpl {

    private final Translations translations;

    protected FabricMCMessageImpl(Translations translations) {
        this.translations = translations;
    }

    public MutableComponent convert(MCMessage msg, ServerPlayer receiver) {
        MutableComponent root = Component.empty();

        append(root, msg, null, receiver);

        return root;
    }

    private void append(MutableComponent parent, MCMessage msg, MCMessage.MessageStyle parentStyle, ServerPlayer receiver) {
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

        final Style mcStyle = convert(style);
        final Component text;

        if (msg instanceof MCMessage.MCTranslationMessage translationMsg) {
            List<MCMessage> substituteList = translationMsg.getSubstitutes();
            Component[] substitutes = new Component[substituteList.size()];

            for (int i = 0; i < substituteList.size(); i++) {
                MCMessage subMsg = substituteList.get(i);
                substitutes[i] = convert(subMsg, receiver);
            }

            text = translations.translateText(receiver, translationMsg.getText(), (Object[]) substitutes)
                    .setStyle(mcStyle);
        } else {
            text = Component.literal(msg.getText()).setStyle(mcStyle);
        }

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
            rgb |= Mth.clamp(color.red, 0, 255) << 16;
            rgb |= Mth.clamp(color.green, 0, 255) << 8;
            rgb |= Mth.clamp(color.blue, 0, 255);

            style = style.withColor(TextColor.fromRgb(rgb));
        }

        if (messageStyle.isBold()) style = style.withBold(true);
        if (messageStyle.isItalic()) style = style.withItalic(true);
        if (messageStyle.isObfuscated()) style = style.withObfuscated(true);
        if (messageStyle.isStrikethrough()) style = style.withStrikethrough(true);
        if (messageStyle.isUnderline()) style = style.withUnderlined(true);

        return style;
    }
}
