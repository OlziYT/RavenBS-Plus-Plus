package xyz.ravenbs.mixin.client;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import xyz.ravenbs.utility.CommandManager;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public abstract class MixinChatInputSuggestor {

    @Shadow private TextFieldWidget textField;
    @Shadow private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow private boolean windowActive;
    
    @Shadow protected abstract void show(boolean narrative);

    @Inject(method = "refresh", at = @At("HEAD"), cancellable = true)
    private void onRefresh(CallbackInfo ci) {
        String input = this.textField.getText();
        if (input.startsWith(".")) {
            // Cancel vanilla suggestions (server)
            ci.cancel();
            
            // Get suggestions context from CommandManager
            CommandManager.SuggestionContext context = CommandManager.getSuggestions(input);
            
            // Build suggestions starting at the calculated offset
            SuggestionsBuilder builder = new SuggestionsBuilder(input, context.offset);
            
            for (CommandManager.Suggestion s : context.suggestions) {
                if (s.tooltip != null) {
                    builder.suggest(s.text, net.minecraft.text.Text.of(s.tooltip));
                } else {
                    builder.suggest(s.text);
                }
            }
            
            this.pendingSuggestions = CompletableFuture.completedFuture(builder.build());
            this.show(true);
        }
    }
}
