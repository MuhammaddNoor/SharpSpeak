package com.example.sharpspeak;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 100;

    private String currentModePrompt = ""; 

    private final String PROMPT_GRAMMAR = "[System: You are an expert English teacher. The user is practicing B1/B2 level English. Your task is to FIRST check the user's message for grammar/vocabulary mistakes. If there is a mistake, point it out gently and correct it. THEN, reply to the conversation naturally.] User message: ";
    private final String PROMPT_INTERVIEW = "[System: You are a Senior Tech Recruiter interviewing the user for an entry-level Software Development role. Ask technical, algorithmic, and HR questions one by one. Keep your answers short and wait for the user's response. Evaluate their answers.] User message: ";

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private TextToSpeech textToSpeech;

    private boolean isVoiceEnabled = true; // متغير للتحكم بتشغيل وإيقاف الصوت
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private EditText userInput;
    private SwitchCompat voiceToggle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ربط عناصر الواجهة
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        userInput = findViewById(R.id.userInput);
        ImageButton micButton = findViewById(R.id.micButton);
        Button sendButton = findViewById(R.id.sendButton);
        voiceToggle = findViewById(R.id.voiceToggle);
        Button btnFreeTalk = findViewById(R.id.btnFreeTalk);
        Button btnGrammar = findViewById(R.id.btnGrammar);


        // إعداد RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        checkAudioPermission();
        initSpeechRecognizer();
        initTextToSpeech();

        voiceToggle.setOnCheckedChangeListener((buttonView, isChecked) -> isVoiceEnabled = isChecked);

        micButton.setOnClickListener(v -> {
            userInput.setText("");
            startListening();
        });

        sendButton.setOnClickListener(v -> {
            String text = userInput.getText().toString().trim();
            if (!text.isEmpty()) {
                appendChat(text, true);
                userInput.setText("");
                sendTextToGemini(text);
            }
        });

        btnFreeTalk.setOnClickListener(v -> {
            currentModePrompt = "";
            appendChat("--- Mode: Free Talk Activated ---", false);
        });

        btnGrammar.setOnClickListener(v -> {
            currentModePrompt = PROMPT_GRAMMAR;
            appendChat("--- Mode: B1/B2 Grammar Tutor Activated ---", false);
        });

        Button btnRandomWord = findViewById(R.id.btnRandomWord);

        btnRandomWord.setOnClickListener(v -> {
            appendChat("Teach me a random word ", true);

            // 1. قائمة مواضيع متنوعة لإجبار AI على التغيير
            String[] topics = {"technology", "travel", "emotions", "health", "business",
                    "nature", "food", "education", "sports", "art",
                    "daily routine", "science", "space", "history", "hobbies", "weather"};

            // 2. اختيار موضوع عشوائي من القائمة
            int randomIndex = new java.util.Random().nextInt(topics.length);
            String randomTopic = topics[randomIndex];

            // 3. دمج الموضوع العشوائي داخل الطلب الخفي
            String secretPrompt = "You are an English teacher. Give me exactly ONE random, useful English vocabulary word (B1/B2 level) strictly related to the topic of: [" + randomTopic + "]. " +
                    "Provide: \n1. The word.\n2. Its meaning in Arabic.\n3. One simple example sentence in English. " +
                    "Keep your response short and clear.";

            // 4. إرسال الطلب لـ Gemini
            GeminiHelper.askGemini(secretPrompt, new GeminiHelper.GeminiCallback() {
                @Override
                public void onSuccess(String responseText) {
                    appendChat(responseText, false);
                    speak(responseText);
                }

                @Override
                public void onError(Throwable throwable) {
                    appendChat("Error: " + throwable.getMessage(), false);
                }
            });
        });
    }


    private void appendChat(String message, boolean isUser) {
        runOnUiThread(() -> {
            messageList.add(new ChatMessage(message, isUser));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
        });
    }

    private void sendTextToGemini(String text) {
        String textToSend = currentModePrompt.isEmpty() ? text : currentModePrompt + text;

        GeminiHelper.askGemini(textToSend, new GeminiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String responseText) {
                appendChat(responseText, false);
                speak(responseText);
            }

            @Override
            public void onError(Throwable throwable) {
                appendChat("Error: " + throwable.getMessage(), false);
                Log.e("GeminiError", throwable.getMessage(), throwable);
            }
        });
    }

    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

            // 👇 الأسطر الجديدة: إطالة فترة الانتظار (مثلاً 5000 ملي ثانية = 5 ثوانٍ من الصمت المسموح)
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000L);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Toast.makeText(MainActivity.this, "Listening...", Toast.LENGTH_SHORT).show();
                }

                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() {}

                @Override
                public void onError(int error) {
                    Toast.makeText(MainActivity.this, "Voice Error: " + error, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String spokenText = matches.get(0);
                        appendChat(spokenText, true);
                        sendTextToGemini(spokenText);
                    }
                }

                @Override public void onPartialResults(Bundle partialResults) {}
                @Override public void onEvent(int eventType, Bundle params) {}
            });
        }
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);

                // البحث عن أفضل صوت أنثوي أمريكي متوفر
                if (textToSpeech.getVoices() != null) {
                    for (android.speech.tts.Voice voice : textToSpeech.getVoices()) {
                        String voiceName = voice.getName().toLowerCase();
                        // نبحث عن الأصوات الأمريكية التي تحتوي على دلالة أنثوية
                        if (voiceName.contains("en-us") &&
                                (voiceName.contains("female") || voiceName.contains("sfn") || voiceName.contains("f0"))) {
                            textToSpeech.setVoice(voice);
                            break; // بمجرد إيجاد الصوت الأنثوي نتوقف
                        }
                    }
                }
            }
        });
    }

    public void startListening() {
        if (speechRecognizer != null) {
            speechRecognizer.startListening(speechRecognizerIntent);
        }
    }

    private String cleanTextForSpeech(String text) {
        if (text == null) return "";
        return text
                .replace("**", "")
                .replace("*", "")
                .replace("#", "")
                .replace("`", "")
                .replaceAll("(?m)^\\s*-\\s*", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void speak(String text) {
        if (!isVoiceEnabled || textToSpeech == null) return;
        String cleanText = cleanTextForSpeech(text);
        textToSpeech.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
