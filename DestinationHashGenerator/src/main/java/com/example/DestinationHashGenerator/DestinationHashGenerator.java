package com.example.DestinationHashGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class DestinationHashGenerator {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage:- java -jar DestinationHashGenerator.jar <PRN Number> <path to json file>");
            return;
        }

        String prnNumber = args[0].trim().toLowerCase();
        String jsonFilePath = args[1];

        String destinationValue = null;
        try {
            destinationValue = getDestinationValueFromJson(jsonFilePath);
        } catch (IOException e) {
            System.out.println("Error reading JSON file: " + e.getMessage());
            return;
        }

        if (destinationValue == null) {
            System.out.println("Key 'destination' not found in JSON file.");
            return;
        }

        String randomString = generateRandomString(8);

        String concatenatedString = prnNumber + destinationValue + randomString;
        String md5Hash = generateMD5Hash(concatenatedString);

        System.out.println(md5Hash + ";" + randomString);
    }

    private static String getDestinationValueFromJson(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(new File(filePath));
        return traverseJson(rootNode);
    }

    private static String traverseJson(JsonNode node) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (entry.getKey().equals("destination")) {
                    return entry.getValue().asText();
                }
                String result = traverseJson(entry.getValue());
                if (result != null) return result;
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                String result = traverseJson(element);
                if (result != null) return result;
            }
        }
        return null;
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    private static String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found");
        }
    }
}
