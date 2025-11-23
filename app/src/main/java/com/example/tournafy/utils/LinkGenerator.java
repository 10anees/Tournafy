package com.example.tournafy.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Utility class for generating unique visibility links for matches, tournaments, and series.
 * 
 * Provides multiple strategies for link generation:
 * - Short codes (like YouTube)
 * - Readable format (match-name-based)
 * - Hash-based (secure)
 */
public class LinkGenerator {
    
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_CODE_LENGTH = 8;
    
    /**
     * Generates a random short code for visibility link.
     * Example: "aB3xK9mQ"
     * 
     * @param length The length of the code (default: 8)
     * @return A random alphanumeric code
     */
    public static String generateShortCode(int length) {
        Random random = new Random();
        StringBuilder code = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            code.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        
        return code.toString();
    }
    
    /**
     * Generates a short code with default length (8 characters).
     * 
     * @return A random alphanumeric code
     */
    public static String generateShortCode() {
        return generateShortCode(DEFAULT_CODE_LENGTH);
    }
    
    /**
     * Generates a readable visibility link based on entity name and ID.
     * Example: "barcelona-vs-madrid-a3b5c7"
     * 
     * @param name The name of the entity (match, tournament, etc.)
     * @param entityId The unique ID of the entity
     * @return A readable URL-friendly link
     */
    public static String generateReadableLink(String name, String entityId) {
        if (name == null || name.isEmpty()) {
            return generateShortCode();
        }
        
        // Convert name to URL-friendly format
        String urlFriendlyName = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special chars
                .replaceAll("\\s+", "-")          // Replace spaces with hyphens
                .replaceAll("-+", "-")            // Remove multiple hyphens
                .trim();
        
        // Limit name length to avoid overly long URLs
        if (urlFriendlyName.length() > 40) {
            urlFriendlyName = urlFriendlyName.substring(0, 40);
        }
        
        // Add short ID suffix
        String idSuffix = entityId != null && entityId.length() >= 6 
                ? entityId.substring(0, 6) 
                : generateShortCode(6);
        
        return urlFriendlyName + "-" + idSuffix;
    }
    
    /**
     * Generates a hash-based visibility link for enhanced security.
     * Uses MD5 hash of entity ID and timestamp.
     * 
     * @param entityId The unique ID of the entity
     * @return A hash-based code
     */
    public static String generateHashLink(String entityId) {
        try {
            String input = entityId + System.currentTimeMillis();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            
            // Convert to base62 for shorter, URL-friendly string
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < 6 && i < digest.length; i++) {
                String hex = Integer.toHexString(0xff & digest[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to random code if hashing fails
            return generateShortCode();
        }
    }
    
    /**
     * Generates a short, memorable match code (6 characters).
     * Format: ABC123 (3 letters + 3 numbers) - easy to type and share
     * 
     * @param entityName The name of the entity (unused, kept for compatibility)
     * @param entityId The unique ID used as seed for generation
     * @return A short match code (e.g., "MHA-E2B")
     */
    public static String generateLink(String entityName, String entityId) {
        // Generate a short, memorable code
        return generateShortMatchCode(entityId);
    }
    
    /**
     * Generates a short match code format: XXX-YYY (3 chars - 3 chars)
     * Uses entity ID as seed for consistent generation.
     * 
     * @param entityId The entity ID to use as seed
     * @return Short match code like "MHA-E2B"
     */
    private static String generateShortMatchCode(String entityId) {
        if (entityId == null || entityId.length() < 6) {
            return generateShortCode(6);
        }
        
        // Use first 6 chars of entity ID and convert to alphanumeric
        String code = entityId.substring(0, 6).toUpperCase()
                .replaceAll("[^A-Z0-9]", "")
                .replaceAll("O", "0")  // Replace O with 0 for clarity
                .replaceAll("I", "1")  // Replace I with 1 for clarity
                .replaceAll("L", "1"); // Replace L with 1 for clarity
        
        // Ensure we have 6 characters
        while (code.length() < 6) {
            code += CHARS.charAt(new Random().nextInt(CHARS.length()));
        }
        
        // Format as XXX-YYY for readability
        return code.substring(0, 3) + "-" + code.substring(3, 6);
    }
    
    /**
     * Validates if a link string is valid (contains only allowed characters).
     * 
     * @param link The link to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidLink(String link) {
        if (link == null || link.isEmpty()) {
            return false;
        }
        
        // Check if contains only alphanumeric and hyphens
        return link.matches("^[a-zA-Z0-9-]+$");
    }
    
    /**
     * Generates a full shareable URL for a match.
     * Uses HTTPS for clickability in all apps, with deep link handling in app.
     * 
     * @param visibilityLink The visibility link code
     * @return Full HTTPS URL (e.g., "https://tournafy.app/match/abc123")
     */
    public static String generateMatchUrl(String visibilityLink) {
        return "https://tournafy.app/match/" + visibilityLink;
    }
    
    /**
     * Generates a custom app scheme URL for direct app opening.
     * 
     * @param visibilityLink The visibility link code
     * @return App-specific deep link (e.g., "tournafy://match/abc123")
     */
    public static String generateMatchAppUrl(String visibilityLink) {
        return "tournafy://match/" + visibilityLink;
    }
    
    /**
     * Generates a full shareable URL for a tournament.
     * 
     * @param visibilityLink The visibility link code
     * @return Full URL (e.g., "https://tournafy.app/tournament/abc123")
     */
    public static String generateTournamentUrl(String visibilityLink) {
        return "https://tournafy.app/tournament/" + visibilityLink;
    }
    
    /**
     * Generates a full shareable URL for a series.
     * 
     * @param visibilityLink The visibility link code
     * @return Full URL (e.g., "https://tournafy.app/series/abc123")
     */
    public static String generateSeriesUrl(String visibilityLink) {
        return "https://tournafy.app/series/" + visibilityLink;
    }
}
