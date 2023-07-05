package work.lclpnet.serverimpl.kibu.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import work.lclpnet.kibu.access.PlayerLanguage;
import work.lclpnet.kibu.inv.item.ItemStackUtil;
import work.lclpnet.kibu.inv.type.KibuInventory;
import work.lclpnet.kibu.inv.type.RestrictedInventory;
import work.lclpnet.kibu.translate.TranslationService;
import work.lclpnet.lclpnetwork.facade.MCStats;
import work.lclpnet.translations.Translator;

import java.text.SimpleDateFormat;
import java.util.*;

public class StatsDisplay {

    private final TranslationService translations;
    private final StatsManager statsManager;
    private final Logger logger;

    public StatsDisplay(TranslationService translations, StatsManager statsManager, Logger logger) {
        this.translations = translations;
        this.statsManager = statsManager;
        this.logger = logger;
    }

    public KibuInventory createStatsInv(Text title, MCStats.Entry mainEntry, List<MCStats.Entry> items, int page,
                                        ServerPlayerEntity viewer, StatsManager.StatsInventory parent) {
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

        ItemStack border = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
        border.setCustomName(Text.empty());

        for (int i = 0; i < 9; i++) {
            inv.setStack(i, border);
        }

        for (int i = slots - 9; i < slots; i++) {
            inv.setStack(i, border);
        }

        for (int i = 9; i < slots - 9; i = (i % 9 == 0 ? i + 8 : i + 1)) {
            inv.setStack(i, border);
        }

        inv.setStack(4, getItem(mainEntry, true, viewer, statsInv));

        int minIdx = itemsPerPage * page;
        int maxIdx = Math.min(minIdx + itemsPerPage, items.size());

        int currentContentRow = 1;
        int currentContentColumn = 0;

        for (int i = minIdx; i < maxIdx; i++) {
            MCStats.Entry entry = items.get(i);

            int rowFirst = currentContentRow * 9;
            int rowColumn = rowStartIndex + currentContentColumn * (1 + columnSpacing);
            inv.setStack(rowFirst + rowColumn, getItem(entry, false, viewer, statsInv));

            if (++currentContentColumn >= 4) {
                currentContentColumn = 0;
                currentContentRow += 1;
            }
        }

        if (pagesRequired > 1) {
            inv.setStack(slots - 5, getPageItem(viewer, page, pagesRequired));

            if (page > 0) {
                ItemStack prevPage = getPrevPageItem(viewer);
                inv.setStack(slots - 9, prevPage);
                statsInv.setPrevPageItem(prevPage);
            }
            if (page < pagesRequired - 1) {
                ItemStack nextPage = getNextPageItem(viewer);
                inv.setStack(slots - 1, nextPage);
                statsInv.setNextPageItem(nextPage);
            }
        }

        if (mainEntry.getType() == MCStats.EntryType.GROUP) {
            ItemStack back = getBackItem(viewer);
            inv.setStack(0, back);
            statsInv.setBackItem(back);
        }

        statsManager.markAsStats(inv, statsInv);

        return inv;
    }

    private ItemStack getBackItem(ServerPlayerEntity viewer) {
        ItemStack stack = new ItemStack(Items.ARROW);

        stack.setCustomName(translations.translateText(viewer, "stats.back").formatted(Formatting.BLUE));

        return stack;
    }

    private ItemStack getPageItem(ServerPlayerEntity viewer, int page, int pagesRequired) {
        page += 1;

        ItemStack stack = new ItemStack(Items.PAPER);

        if (pagesRequired <= 64) {
            stack.setCount(page);
        }

        String content = translations.translate(viewer, "stats.page.current", page, pagesRequired);

        stack.setCustomName(Text.literal(content).formatted(Formatting.AQUA));

        return stack;
    }

    private ItemStack getNextPageItem(ServerPlayerEntity viewer) {
        ItemStack stack = new ItemStack(Items.EMERALD_BLOCK);

        stack.setCustomName(translations.translateText(viewer, "stats.page.next").formatted(Formatting.GREEN));

        return stack;
    }

    private ItemStack getPrevPageItem(ServerPlayerEntity viewer) {
        ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);

        stack.setCustomName(translations.translateText(viewer, "stats.page.prev").formatted(Formatting.RED));

        return stack;
    }

    private ItemStack getItem(MCStats.Entry entry, boolean mainEntry, ServerPlayerEntity viewer, StatsManager.StatsInventory statsInv) {
        Item item = getIconItem(entry);
        ItemStack stack = new ItemStack(item);

        Formatting displayNameColor = getFormatting(entry);
        Text name = Text.literal(entry.getTitle()).formatted(displayNameColor, Formatting.BOLD)
                .styled(style -> style.withItalic(false));

        stack.setCustomName(name);

        stack.addHideFlag(ItemStack.TooltipSection.ENCHANTMENTS);
        stack.addHideFlag(ItemStack.TooltipSection.ADDITIONAL);
        stack.addHideFlag(ItemStack.TooltipSection.DYE);
        stack.addHideFlag(ItemStack.TooltipSection.UPGRADES);
        stack.addHideFlag(ItemStack.TooltipSection.MODIFIERS);
        stack.addHideFlag(ItemStack.TooltipSection.UNBREAKABLE);
        stack.addHideFlag(ItemStack.TooltipSection.CAN_DESTROY);
        stack.addHideFlag(ItemStack.TooltipSection.CAN_PLACE);

        List<Text> lore = new ArrayList<>();
        if (entry.getType() == MCStats.EntryType.GROUP) {
            if (!mainEntry) {
                lore.add(translations.translateText(viewer, "stats.entry.open_group").formatted(Formatting.YELLOW));
            }
        } else {
            Map<String, MCStats.Value> properties = entry.getProperties();

            if (properties == null) {
                lore.add(translations.translateText(viewer, "stats.entry.none").formatted(Formatting.YELLOW, Formatting.ITALIC));
            } else {
                for (var e : properties.entrySet()) {
                    String key = e.getKey();
                    MCStats.Value value = e.getValue();

                    String keyTranslation = translations.translate(viewer, String.format("stat.%s.%s", entry.getName().toLowerCase(Locale.ROOT), key));
                    String valString = getValueAsText(value, viewer);

                    lore.add(Text.literal(keyTranslation.concat(": ")).formatted(Formatting.GREEN)
                            .append(Text.literal(valString).formatted(Formatting.YELLOW)));
                }
            }
        }

        ItemStackUtil.setLore(stack, lore);

        if (!mainEntry && entry.getType() == MCStats.EntryType.GROUP) {
            statsInv.setItemStackGroup(stack, entry);
        }

        return stack;
    }

    @NotNull
    private static Formatting getFormatting(MCStats.Entry entry) {
        final Formatting displayNameColor;

        if (entry.getType() == MCStats.EntryType.GENERAL) {
            displayNameColor = Formatting.AQUA;
        } else if (entry.getType() == MCStats.EntryType.GROUP) {
            displayNameColor = Formatting.GREEN;
        } else {
            displayNameColor = Formatting.GOLD;
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
            identifier = new Identifier(minecraftId);
        } catch (InvalidIdentifierException e) {
            logger.error("Invalid identifier {}", minecraftId, e);
            return Items.BOOK;
        }

        Item item = Registries.ITEM.get(identifier);

        if (item == Items.AIR) {
            return Items.BOOK;
        }

        return item;
    }

    private String getValueAsText(MCStats.Value value, ServerPlayerEntity viewer) {
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
