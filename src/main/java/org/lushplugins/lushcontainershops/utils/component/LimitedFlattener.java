package org.lushplugins.lushcontainershops.utils.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.flattener.FlattenerListener;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

public class LimitedFlattener implements FlattenerListener {
    private final TextComponent.Builder component;
    private TextComponent.Builder currentComponent;
    private int remainingLimit;
    private boolean cancel = false;

    public LimitedFlattener(int charLimit) {
        this.component = Component.text();
        this.remainingLimit = charLimit;
    }

    public Component getResult() {
        return this.component.build();
    }

    @Override
    public void pushStyle(@NotNull Style style) {
        if (shouldCancel()) {
            return;
        }

        this.currentComponent = Component.text()
            .style(style);
    }

    @Override
    public void component(@NotNull String text) {
        if (shouldCancel()) {
            return;
        }

        String shortenedText;
        if (text.length() > this.remainingLimit) {
            shortenedText = text.substring(0, this.remainingLimit - 1);
        } else {
            shortenedText = text;
        }

        this.remainingLimit -= text.length();
        this.currentComponent.content(shortenedText);
        this.component.append(this.currentComponent);
        this.currentComponent = null;
    }

    @Override
    public boolean shouldContinue() {
        return !this.cancel;
    }

    public boolean shouldCancel() {
        if (remainingLimit <= 0 && !this.cancel) {
            this.cancel = true;
            this.component.append(Component.text("..."));
        }

        return this.cancel;
    }
}
