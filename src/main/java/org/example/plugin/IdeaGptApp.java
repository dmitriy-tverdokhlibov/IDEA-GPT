package org.example.plugin;

import okhttp3.*;
import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The IdeaGptApp class is responsible for managing the core functionalities
 * of the Idea Generation Platform powered by GPT.
 * This class serves as the main entry point for the application.
 *
 * @author dmitriy-tverdokhlibov
 */

public class IdeaGptApp {


    private static final String API_URL = "https://api.openai.com/v1/completions";
    private static String API_KEY;

    static {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
            API_KEY = properties.getProperty("OPENAI_API_KEY");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load API key", e);
        }
    }

    private final OkHttpClient httpClient;

    public IdeaGptApp() {
        httpClient = new OkHttpClient();
    }

    /**
     * Sends a prompt to the OpenAI API and returns the response.
     *
     * @param prompt The user input to be sent to GPT
     * @return The response from GPT as a String
     * @throws IOException if an error occurs during the API call
     */
    public String getGptResponse(String prompt) throws IOException {
        RequestBody body = new FormBody.Builder()
                .add("model", "gpt-4")
                .add("prompt", prompt)
                .add("max_tokens", "100")
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            if (response.body() == null) throw new IOException("Response body is null");
            return response.body().string();
        }
    }

    /**
     * Initializes the user interface for interacting with GPT.
     */
    public void initializeUI() {
        JFrame frame = new JFrame("IdeaGpt - Powered by GPT");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);

        JTextArea promptTextArea = new JTextArea(5, 30);
        JTextArea responseTextArea = new JTextArea(10, 30);
        responseTextArea.setEditable(false);

        JButton submitButton = new JButton("Send to GPT");

        submitButton.addActionListener(e -> {
            String prompt = promptTextArea.getText();
            if (!prompt.isEmpty()) {
                try {
                    String response = getGptResponse(prompt);
                    responseTextArea.setText(response);
                } catch (IOException ex) {
                    responseTextArea.setText("Error: " + ex.getMessage());
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Enter your prompt:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(promptTextArea), BorderLayout.CENTER);
        panel.add(submitButton, BorderLayout.SOUTH);

        frame.getContentPane().add(panel, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(responseTextArea), BorderLayout.CENTER);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            IdeaGptApp app = new IdeaGptApp();
            app.initializeUI();
        });
    }
}
