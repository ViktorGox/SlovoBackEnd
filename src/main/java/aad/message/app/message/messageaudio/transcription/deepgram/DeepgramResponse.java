package aad.message.app.message.messageaudio.transcription.deepgram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeepgramResponse {
    public Metadata metadata;
    public Results results;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        @JsonProperty("request_id")
        public String requestId;
        public String sha256;
        public String created;
        public double duration;
        public int channels;
        public List<String> models;
        @JsonProperty("model_info")
        public ModelInfo modelInfo;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelInfo {
        @JsonProperty("65c2022f-3683-4be7-a405-7a4f425434dc")
        public ModelDetails modelDetails;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelDetails {
        public String name;
        public String version;
        public String arch;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Results {
        public List<Channel> channels;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Channel {
        public List<Alternative> alternatives;
        @JsonProperty("detected_language")
        public String detectedLanguage;
        @JsonProperty("language_confidence")
        public double languageConfidence;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Alternative {
        public String transcript;
        public double confidence;
        public List<Word> words;
        public Paragraphs paragraphs;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Word {
        public String word;
        public double start;
        public double end;
        public double confidence;
        @JsonProperty("punctuated_word")
        public String punctuatedWord;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Paragraphs {
        public String transcript;
        public List<Paragraph> paragraphs;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Paragraph {
        public List<Sentence> sentences;
        @JsonProperty("num_words")
        public int numWords;
        public double start;
        public double end;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sentence {
        public String text;
        public double start;
        public double end;
    }


}