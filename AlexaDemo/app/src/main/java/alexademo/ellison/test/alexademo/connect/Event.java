package alexademo.ellison.test.alexademo.connect;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Event {
    Header header;
    Payload payload;
    List<Event> context;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }


    public static class Header{
        String namespace;
        String name;
        String messageId;
        String dialogRequestId;

        public String getNamespace() {
            return namespace;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getMessageId() {
            return messageId;
        }

        public String getDialogRequestId() {
            return dialogRequestId;
        }

        public void setDialogRequestId(String dialogRequestId) {
            this.dialogRequestId = dialogRequestId;
        }
    }

    public static class Payload{
        String token;
        String profile;
        String format;
        Boolean muted;
        Long volume;
        Long offsetInMilliseconds;

        public String getProfile() {
            return profile;
        }

        public String getFormat() {
            return format;
        }


    }

    public static class EventWrapper{
        Event event;
        List<Event> context = new ArrayList<>();

        public Event getEvent() {
            return event;
        }

        public List<Event> getContext() {
            return context;
        }

        public String toJson(){
            return new Gson().toJson(this)+"\n";
        }
    }

    public static class Builder{
        Event event = new Event();
        Payload payload = new Payload();
        Header header = new Header();
        List<Event> context = new ArrayList<>();

        public Builder(){
            event.setPayload(payload);
            event.setHeader(header);
        }

        public EventWrapper build(){
            EventWrapper wrapper = new EventWrapper();
            wrapper.event = event;

            if (context != null && !context.isEmpty() && !(context.size() == 1 && context.get(0) == null)) {
                wrapper.context = context;
            }

            return wrapper;
        }

        public String toJson(){
            return build().toJson();
        }

        public Builder setContext(List<Event> context) {
            if (context == null) {
                return this;
            }
            this.context = context;
            return this;
        }

        public Builder setHeaderNamespace(String namespace){
            header.namespace = namespace;
            return this;
        }

        public Builder setHeaderName(String name){
            header.name = name;
            return this;
        }

        public Builder setHeaderMessageId(String messageId){
            header.messageId = messageId;
            return this;
        }

        public Builder setHeaderDialogRequestId(String dialogRequestId){
            header.dialogRequestId = dialogRequestId;
            return this;
        }

        public Builder setPayloadProfile(String profile){
            payload.profile = profile;
            return this;
        }
        public Builder setPayloadFormat(String format){
            payload.format = format;
            return this;
        }

        public Builder setPayloadMuted(boolean muted){
            payload.muted = muted;
            return this;
        }

        public Builder setPayloadVolume(long volume){
            payload.volume = volume;
            return this;
        }
        public Builder setPayloadToken(String token){
            payload.token = token;
            return this;
        }
        public Builder setPlayloadOffsetInMilliseconds(long offsetInMilliseconds){
            payload.offsetInMilliseconds = offsetInMilliseconds;
            return this;
        }
    }

    public static String getSpeechRecognizerEvent(){
        Builder builder = new Builder();
        builder.setHeaderNamespace("SpeechRecognizer")
                .setHeaderName("Recognize")
                .setHeaderMessageId(UUID.randomUUID().toString())
                .setHeaderDialogRequestId("dialogRequest-321")
                .setPayloadFormat("AUDIO_L16_RATE_16000_CHANNELS_1")
                .setPayloadProfile("NEAR_FIELD");
        return builder.toJson();
    }


}
