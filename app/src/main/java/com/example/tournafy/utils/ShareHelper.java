package com.example.tournafy.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.tournafy.domain.models.base.Match;
import com.example.tournafy.domain.models.match.cricket.CricketMatch;
import com.example.tournafy.domain.models.match.football.FootballMatch;

/**
 * Helper class for sharing matches, tournaments, and series via intents.
 * Provides methods to share via system share sheet, copy to clipboard, etc.
 */
public class ShareHelper {
    
    /**
     * Shares a match using the system share sheet (explicit intent).
     * Creates a custom app link that will open directly in the app.
     * 
     * @param context The context to start the intent from
     * @param match The match to share
     */
    public static void shareMatch(Context context, Match match) {
        if (match == null) {
            Toast.makeText(context, "No match to share", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String matchCode = match.getVisibilityLink();
        if (matchCode == null || matchCode.isEmpty()) {
            Toast.makeText(context, "Generating match code... Please try again in a moment", Toast.LENGTH_LONG).show();
            android.util.Log.w("ShareHelper", "Match code not available for match: " + match.getName() + " (ID: " + match.getEntityId() + ")");
            return;
        }
        
        // Share just the match code in uppercase for easy copying
        String shareText = matchCode.toUpperCase();
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Match Code: " + matchCode.toUpperCase());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        
        context.startActivity(Intent.createChooser(shareIntent, "Share match code"));
    }
    
    /**
     * Copies the match code to clipboard.
     * 
     * @param context The context
     * @param match The match whose code to copy
     */
    public static void copyMatchCodeToClipboard(Context context, Match match) {
        if (match == null || match.getVisibilityLink() == null) {
            Toast.makeText(context, "Match code not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String matchCode = match.getVisibilityLink().toUpperCase();
        
        ClipboardManager clipboard = (ClipboardManager) 
            context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Match Code", matchCode);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(context, "Match code " + matchCode + " copied!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Builds the share text for a match with emoji and formatting.
     * 
     * @param match The match to build text for
     * @param matchUrl The full URL of the match
     * @return Formatted share text
     */
    private static String buildMatchShareText(Match match, String matchCode) {
        StringBuilder builder = new StringBuilder();
        
        // Add sport-specific emoji
        if (match instanceof CricketMatch) {
            builder.append("🏏 ");
        } else if (match instanceof FootballMatch) {
            builder.append("⚽ ");
        } else {
            builder.append("🏆 ");
        }
        
        // Match name
        builder.append("Watch ").append(match.getName()).append(" LIVE!\n\n");
        
        // Match details
        if (match.getVenue() != null && !match.getVenue().isEmpty()) {
            builder.append("📍 Venue: ").append(match.getVenue()).append("\n");
        }
        
        // Status
        String status = match.getMatchStatus();
        if ("LIVE".equals(status)) {
            builder.append("🔴 Status: LIVE NOW\n");
        } else if ("SCHEDULED".equals(status)) {
            builder.append("⏰ Status: Upcoming\n");
        } else if ("COMPLETED".equals(status)) {
            builder.append("✅ Status: Completed\n");
        }
        
        builder.append("\n");
        
        // Match Code - prominently displayed
        builder.append("� Match Code: ").append(matchCode.toUpperCase()).append("\n\n");
        
        // Instructions
        builder.append("📱 To watch:\n");
        builder.append("1. Open Tournafy app\n");
        builder.append("2. Search for this match code\n");
        builder.append("3. Enjoy live updates!\n\n");
        
        builder.append("Don't have the app? Download Tournafy now!");
        
        return builder.toString();
    }
    
    /**
     * Generates and returns the match share text without sharing.
     * Useful for preview purposes.
     * 
     * @param match The match
     * @return The formatted share text
     */
    public static String getMatchShareText(Match match) {
        if (match == null || match.getVisibilityLink() == null) {
            return "";
        }
        
        String matchCode = match.getVisibilityLink();
        return buildMatchShareText(match, matchCode);
    }
    
    /**
     * Gets the match code for display purposes.
     * 
     * @param match The match
     * @return The match code, or null if not available
     */
    public static String getMatchCode(Match match) {
        if (match == null || match.getVisibilityLink() == null) {
            return null;
        }
        return match.getVisibilityLink().toUpperCase();
    }
    
    /**
     * Checks if a match has a shareable link.
     * 
     * @param match The match to check
     * @return true if the match has a valid visibility link
     */
    public static boolean isShareable(Match match) {
        return match != null 
            && match.getVisibilityLink() != null 
            && !match.getVisibilityLink().isEmpty();
    }
}
