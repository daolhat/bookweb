package com.programing.bookweb.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class CodeGenerator {

    private static final Random random = new Random();
    private static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";


    public static String generateOrderCode(String userName, LocalDateTime orderDateTime, int productCount) {
        StringBuilder randomPrefix = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            randomPrefix.append(ALPHA.charAt(random.nextInt(ALPHA.length())));
        }
        String userPrefix;
        if (userName == null || userName.length() < 2) {
            userPrefix = "XX";
        } else {
            userPrefix = userName.trim().substring(0, 2).toUpperCase();
        }
        String dateString = orderDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String countString = String.format("%02d", Math.min(productCount, 99));
        int randomNum = random.nextInt(99);
        return randomPrefix.toString() + userPrefix + dateString + countString + String.format("%02d", randomNum);
    }


//    /**
//     * Generates a category code with format AB123
//     * AB: first 2 characters of category name
//     * 123: random 3-digit number
//     * Total length: 5 characters
//     */
//    public static String generateCategoryCode(String categoryName) {
//        if (categoryName == null || categoryName.isEmpty()) {
//            return "XX" + String.format("%03d", random.nextInt(1000));
//        }
//
//        // Get first 2 characters of category name, convert to uppercase
//        String prefix = categoryName.trim().length() >= 2 ?
//                categoryName.trim().substring(0, 2).toUpperCase() :
//                categoryName.trim().toUpperCase() + "X";
//
//        int randomNum = random.nextInt(1000);
//        return prefix + String.format("%03d", randomNum);
//    }

    /**
     * Generates a category code with format AB123
     * AB: first 2 characters of category name
     * 123: random 3-digit number
     * Total length: 5 characters
     */
    public static String generateCategoryCode(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return "XX" + String.format("%03d", random.nextInt(1000));
        }

        String[] words = categoryName.trim().split("\\s+");
        String firstWord = words[0].toLowerCase();
        String prefix;

        // Check if the first word is "Sách" or "Dạy"
        if (firstWord.equals("sách") || firstWord.equals("dạy")) {
            // Use the next word if available, otherwise fallback to first word
            String targetWord = words.length > 1 ? words[1] : words[0];
            prefix = targetWord.length() >= 2 ?
                    targetWord.substring(0, 2).toUpperCase() :
                    targetWord.toUpperCase() + "X";
        } else {
            // Use the first word
            prefix = firstWord.length() >= 2 ?
                    firstWord.substring(0, 2).toUpperCase() :
                    firstWord.toUpperCase() + "X";
        }

        int randomNum = random.nextInt(1000);
        return prefix + String.format("%03d", randomNum);
    }


    /**
     * Generates a product code with format AB123000000B
     * AB123: category code
     * 000000: random 6-digit number
     * B: random letter
     * Total length: 12 characters
     */
    public static String generateProductCode(String categoryCode) {
        // If category code is invalid, generate a placeholder
        if (categoryCode == null || categoryCode.length() != 5) {
            categoryCode = "XX" + String.format("%03d", random.nextInt(1000));
        }

        // Generate random 6-digit number
        int randomNum = random.nextInt(1000000);

        // Generate random letter
        char randomChar = ALPHA.charAt(random.nextInt(ALPHA.length()));

        return categoryCode + String.format("%06d", randomNum) + randomChar;
    }

    /**
     * Generates a user code with format 0000AB000000
     * 00: registration month
     * 00: last 2 digits of registration year
     * AB: first 2 characters of user's name
     * 000000: random 6-digit number
     * Total length: 12 characters
     */
    public static String generateUserCode(String name, LocalDateTime registrationDate) {
        // Extract month and year
        String month = String.format("%02d", registrationDate.getMonthValue());
        String year = String.format("%02d", registrationDate.getYear() % 100);

        // Get first 2 characters of name
        String namePrefix;
        if (name == null || name.isEmpty()) {
            namePrefix = "XX";
        } else {
            namePrefix = name.trim().length() >= 2 ?
                    name.trim().substring(0, 2).toUpperCase() :
                    name.trim().toUpperCase() + "X";
        }

        // Generate random 6-digit number
        int randomNum = random.nextInt(1000000);

        return month + year + namePrefix + String.format("%06d", randomNum);
    }

}
