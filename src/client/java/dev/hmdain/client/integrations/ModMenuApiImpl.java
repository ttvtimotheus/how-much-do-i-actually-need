package dev.hmdain.client.integrations;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.hmdain.client.config.ModConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("How Much Do I Actually Need Config"));

            ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            general.addEntry(entryBuilder.startBooleanToggle(Component.literal("HUD Enabled"), ModConfig.get().hudEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> ModConfig.get().hudEnabled = newValue)
                    .build());

            general.addEntry(entryBuilder.startIntField(Component.literal("Max HUD Lines"), ModConfig.get().maxHudLines)
                    .setDefaultValue(10)
                    .setSaveConsumer(newValue -> ModConfig.get().maxHudLines = newValue)
                    .build());

            builder.setSavingRunnable(ModConfig::save);

            return builder.build();
        };
    }
}
