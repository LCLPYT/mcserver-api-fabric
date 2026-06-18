package work.lclpnet.serverimpl.kibu.util;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.IdentifierException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import work.lclpnet.kibu.access.PlayerLanguage;
import work.lclpnet.kibu.inv.type.KibuInventory;
import work.lclpnet.kibu.inv.type.RestrictedInventory;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.lclpnetwork.facade.MCStats;
import work.lclpnet.translations.Translator;

import java.text.SimpleDateFormat;
import java.util.*;

public class StatsDisplay {

    private final Translations translations;
    private final StatsManager statsManager;
    private final Logger logger;

    public StatsDisplay(Translations translations, StatsManager statsManager, Logger logger) {
        this.translations = translations;
        this.statsManager = statsManager;
        this.logger = logger;
    }

    public KibuInventory createStatsInv(Component title, MCStats.Entry mainEntry, List<MCStats.Entry> items, int page,
                                        ServerPlayer viewer, StatsManager.StatsInventory parent) {
        int itemsPerRow = 4;
        int rowsPerPage = 4;
        int rowStartIndex = 1, columnSpacing = 1;

        int rowsRequired = (int) Math.ceil(items.size() / (float) itemsPerRow);
        int pagesRequired = (int) Math.ceil(rowsRequired / (float) rowsPerPage);

        int rows = Math.min(6, Math.max(4, rowsRequired + 2));
        int itemsPerPage = itemsPerRow * (rows - 2);
        int slots = rows * 9;

        final KibuInventory inv = new RestrictedInventory(rows, title);
        final StatsManager.StatsInventory statsInv = new StatsManager.StatsInventory(page, title, mainEntry, items);
        statsInv.setParent(parent);

        ItemStack border = new ItemStack(Items.STAINED_GLASS_PANE.pick(DyeColor.BLACK));
        border.set(DataComponents.CUSTOM_NAME, Component.empty());
        border.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, ReferenceSortedSets.emptySet()));

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
        }

        for (int i = slots - 9; i < slots; i++) {
            inv.setItem(i, border);
        }

        for (int i = 9; i < slots - 9; i = (i % 9 == 0 ? i + 8 : i + 1)) {
            inv.setItem(i, border);
        }

        inv.setItem(4, getItem(mainEntry, true, viewer, statsInv));

        int minIdx = itemsPerPage * page;
        int maxIdx = Math.min(minIdx + itemsPerPage, items.size());

        int currentContentRow = 1;
        int currentContentColumn = 0;

        for (int i = minIdx; i < maxIdx; i++) {
            MCStats.Entry entry = items.get(i);

            int rowFirst = currentContentRow * 9;
            int rowColumn = rowStartIndex + currentContentColumn * (1 + columnSpacing);
            inv.setItem(rowFirst + rowColumn, getItem(entry, false, viewer, statsInv));

            if (++currentContentColumn >= 4) {
                currentContentColumn = 0;
                currentContentRow += 1;
            }
        }

        if (pagesRequired > 1) {
            inv.setItem(slots - 5, getPageItem(viewer, page, pagesRequired));

            if (page > 0) {
                ItemStack prevPage = getPrevPageItem(viewer);
                inv.setItem(slots - 9, prevPage);
                statsInv.setPrevPageItem(prevPage);
            }
            if (page < pagesRequired - 1) {
                ItemStack nextPage = getNextPageItem(viewer);
                inv.setItem(slots - 1, nextPage);
                statsInv.setNextPageItem(nextPage);
            }
        }

        if (mainEntry.getType() == MCStats.EntryType.GROUP) {
            ItemStack back = getBackItem(viewer);
            inv.setItem(0, back);
            statsInv.setBackItem(back);
        }

        statsManager.markAsStats(inv, statsInv);

        return inv;
    }

    private ItemStack getBackItem(ServerPlayer viewer) {
        ItemStack stack = new ItemStack(Items.ARROW);

        stack.set(DataComponents.CUSTOM_NAME, translations.translateText(viewer, "stats.back").withStyle(ChatFormatting.BLUE));

        return stack;
    }

    private ItemStack getPageItem(ServerPlayer viewer, int page, int pagesRequired) {
        page += 1;

        ItemStack stack = new ItemStack(Items.PAPER);

        if (pagesRequired <= 64) {
            stack.setCount(page);
        }

        String content = translations.translate(viewer, "stats.page.current", page, pagesRequired);

        stack.set(DataComponents.CUSTOM_NAME, Component.literal(content).withStyle(ChatFormatting.AQUA));

        return stack;
    }

    private ItemStack getNextPageItem(ServerPlayer viewer) {
        ItemStack stack = new ItemStack(Items.EMERALD_BLOCK);

        stack.set(DataComponents.CUSTOM_NAME, translations.translateText(viewer, "stats.page.next").withStyle(ChatFormatting.GREEN));

        return stack;
    }

    private ItemStack getPrevPageItem(ServerPlayer viewer) {
        ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);

        stack.set(DataComponents.CUSTOM_NAME, translations.translateText(viewer, "stats.page.prev").withStyle(ChatFormatting.RED));

        return stack;
    }

    private ItemStack getItem(MCStats.Entry entry, boolean mainEntry, ServerPlayer viewer, StatsManager.StatsInventory statsInv) {
        Item item = getIconItem(entry);
        ItemStack stack = new ItemStack(item);

        ChatFormatting displayNameColor = getFormatting(entry);
        Component name = Component.literal(entry.getTitle()).withStyle(displayNameColor, ChatFormatting.BOLD)
                .withStyle(style -> style.withItalic(false));

        stack.set(DataComponents.CUSTOM_NAME, name);

        stack.set(DataComponents.TOOLTIP_DISPLAY, stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT)
                .withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true)
                .withHidden(DataComponents.DYED_COLOR, true)
                .withHidden(DataComponents.UNBREAKABLE, true));

        List<Component> lore = new ArrayList<>();
        if (entry.getType() == MCStats.EntryType.GROUP) {
            if (!mainEntry) {
                lore.add(translations.translateText(viewer, "stats.entry.open_group").withStyle(ChatFormatting.YELLOW));
            }
        } else {
            Map<String, MCStats.Value> properties = entry.getProperties();

            if (properties == null) {
                lore.add(translations.translateText(viewer, "stats.entry.none").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
            } else {
                for (var e : properties.entrySet()) {
                    String key = e.getKey();
                    MCStats.Value value = e.getValue();

                    String keyTranslation = translations.translate(viewer, String.format("stat.%s.%s", entry.getName().toLowerCase(Locale.ROOT), key));
                    String valString = getValueAsText(value, viewer);

                    lore.add(Component.literal(keyTranslation.concat(": ")).withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(valString).withStyle(ChatFormatting.YELLOW)));
                }
            }
        }

        stack.set(DataComponents.LORE, new ItemLore(Lists.transform(lore, t -> ComponentUtils.mergeStyles(t.copy(), Style.EMPTY.withItalic(false)))));

        if (!mainEntry && entry.getType() == MCStats.EntryType.GROUP) {
            statsInv.setItemStackGroup(stack, entry);
        }

        return stack;
    }

    @NotNull
    private static ChatFormatting getFormatting(MCStats.Entry entry) {
        final ChatFormatting displayNameColor;

        if (entry.getType() == MCStats.EntryType.GENERAL) {
            displayNameColor = ChatFormatting.AQUA;
        } else if (entry.getType() == MCStats.EntryType.GROUP) {
            displayNameColor = ChatFormatting.GREEN;
        } else {
            displayNameColor = ChatFormatting.GOLD;
        }
        return displayNameColor;
    }

    private Item getIconItem(MCStats.Entry entry) {
        final MCStats.Icon icon = entry.getIcon();
        final String minecraftId;

        if (icon == null || (minecraftId = icon.getMinecraft()) == null) {
            return Items.BOOK;
        }

        final Identifier identifier;

        try {
            identifier = Identifier.parse(minecraftId);
        } catch (IdentifierException e) {
            logger.error("Invalid identifier {}", minecraftId, e);
            return Items.BOOK;
        }

        Item item = BuiltInRegistries.ITEM.getValue(identifier);

        if (item == Items.AIR) {
            return Items.BOOK;
        }

        return item;
    }

    private String getValueAsText(MCStats.Value value, ServerPlayer viewer) {
        if (value.getType() != MCStats.ValueType.DATE) {
            return value.getValueAsFormattedString();
        }

        Date date = value.getAsDate();

        if (date == null) {
            return translations.translate(viewer, "stats.value.never");
        }

        Translator translator = translations.getTranslator();
        String language = PlayerLanguage.getLanguage(viewer);
        SimpleDateFormat format = translator.getDateFormat(language);

        return format.format(date);

    }
}
