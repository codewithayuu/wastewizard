package com.example.wastewizard;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.ColorInt;

import com.google.android.material.color.MaterialColors;

public class CategoryColors {

    // Base category colors (kept as references, will be harmonized to theme)
    @ColorInt private static int baseForLabel(Context c, String label) {
        switch (label == null ? "" : label.toLowerCase()) {
            case "plastic":   return c.getResources().getColor(R.color.cat_plastic);
            case "glass":     return c.getResources().getColor(R.color.cat_glass);
            case "metal":     return c.getResources().getColor(R.color.cat_metal);
            case "paper":     return c.getResources().getColor(R.color.cat_paper);
            case "cardboard": return c.getResources().getColor(R.color.cat_cardboard);
            default:          return MaterialColors.getColor(c, com.google.android.material.R.attr.colorOutline, Color.GRAY);
        }
    }

    // Harmonize with theme primary to blend with dynamic palette; great for strokes/icons
    @ColorInt public static int accent(Context c, String label) {
        int base = baseForLabel(c, label);
        int primary = MaterialColors.getColor(c, com.google.android.material.R.attr.colorPrimary, Color.BLACK);
        return MaterialColors.harmonize(base, primary);
    }

    // Stronger variant if you want emphasis (e.g., chips/badges)
    @ColorInt public static int accentEmphasis(Context c, String label) {
        int color = accent(c, label);
        // Compose slightly over surface to bring it forward without breaking contrast
        int surface = MaterialColors.getColor(c, com.google.android.material.R.attr.colorSurface, Color.WHITE);
        return MaterialColors.layer(surface, color, 0.15f);
    }
}
