package alexademo.ellison.test.alexademo.connect;

import android.text.TextUtils;

import java.util.List;

public class Directive {
    private Header header;
    private Payload payload;

    private static final String TYPE_SPEAK = "Speak";
    private static final String TYPE_PLAY = "Play";
    private static final String TYPE_RENDERTEMPLATE = "RenderTemplate";
    public static final String TYPE_WATHERTEMPLATE = "WeatherTemplate";
    public static final String TYPE_BODYTEMPLATE = "BodyTemplate";
    private static final String TYPE_EXCEPTION = "Exception";

    private static final String PLAY_BEHAVIOR_REPLACE_ALL = "REPLACE_ALL";
    private static final String PLAY_BEHAVIOR_ENQUEUE = "ENQUEUE";
    private static final String PLAY_BEHAVIOR_REPLACE_ENQUEUED = "REPLACE_ENQUEUED";

    //DIRECTIVE TYPES
    public boolean isTypeTemplate(){
        return TextUtils.equals(header.getName(), TYPE_RENDERTEMPLATE);
    }

    public boolean isTypeSpeak(){
        return TextUtils.equals(header.getName(), TYPE_SPEAK);
    }

    public boolean isTypePlay(){
        return TextUtils.equals(header.getName(), TYPE_PLAY);
    }

    public boolean isTypeException(){
        return TextUtils.equals(header.getName(), TYPE_EXCEPTION);
    }

    //PLAY BEHAVIORS
    public boolean isPlayBehaviorReplaceAll(){
        return TextUtils.equals(payload.getPlayBehavior(), PLAY_BEHAVIOR_REPLACE_ALL);
    }
    public boolean isPlayBehaviorEnqueue(){
        return TextUtils.equals(payload.getPlayBehavior(), PLAY_BEHAVIOR_ENQUEUE);
    }
    public boolean isPlayBehaviorReplaceEnqueued(){
        return TextUtils.equals(payload.getPlayBehavior(), PLAY_BEHAVIOR_REPLACE_ENQUEUED);
    }

    public Header getHeader() {
        return header;
    }

    public Payload getPayload() {
        return payload;
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

        public String getMessageId() {
            return messageId;
        }

        public String getDialogRequestId() {
            return dialogRequestId;
        }
    }

    public static class Payload{
        String url;
        String format;
        String token;
        String type;
        String scheduledTime;
        String playBehavior;
        AudioItem audioItem;
        long volume;
        boolean mute;
        long timeoutInMilliseconds;
        String description;
        String code;
        //for template response
        String textField;
        Title title;
        Image image;
        //for weather template
        String currentWeather;
        CurrentWeatherIcon currentWeatherIcon;
        List<WeatherForecast> weatherForecast;

        public String getUrl() {
            return url;
        }

        public String getFormat() {
            return format;
        }

        public String getToken() {
            if(token == null){
                //sometimes we need to return the stream tokens, not the top level tokens
                if(audioItem != null && audioItem.getStream() != null){
                    return audioItem.getStream().getToken();
                }
            }
            return token;
        }

        public String getType() {
            return type;
        }

        public String getScheduledTime() {
            return scheduledTime;
        }

        public String getPlayBehavior() {
            return playBehavior;
        }

        public AudioItem getAudioItem() {
            return audioItem;
        }

        public long getVolume() {
            return volume;
        }

        public boolean isMute(){
            return mute;
        }

        public long getTimeoutInMilliseconds(){ return timeoutInMilliseconds; }

        public String getDescription() {
            return description;
        }

        public String getCode() {
            return code;
        }

        public String getTextField() {
            return textField;
        }

        public Title getTitle() {
            return title;
        }

        public Image getImage() {
            return image;
        }

        public String getCurrentWeather() {
            return currentWeather;
        }

        public CurrentWeatherIcon getCurrentWeatherIcon() {
            return currentWeatherIcon;
        }

        public List<WeatherForecast> getWeatherForecast() {
            return weatherForecast;
        }

        public static class CurrentWeatherIcon{
            private List<Sources> sources;
            private String contentDescription;

            public List<Sources> getSources() {
                return sources;
            }

            public String getContentDescription() {
                return contentDescription;
            }

            public class Sources{
                private String darkBackgroundUrl;
                private int widthPixels, heightPixels;
                private String size;

                public String getDarkBackgroundUrl() {
                    return darkBackgroundUrl;
                }

                public int getWidthPixels() {
                    return widthPixels;
                }

                public int getHeightPixels() {
                    return heightPixels;
                }

                public String getSize() {
                    return size;
                }
            }
        }

        public static class WeatherForecast{
            String highTemperature, lowTemperature;
            String date;
            Image image;

            public String getHighTemperature() {
                return highTemperature;
            }

            public String getLowTemperature() {
                return lowTemperature;
            }

            public String getDate() {
                return date;
            }

            public Image getImage() {
                return image;
            }

            public class Image{
                private List<Sources> sources;
                private String contentDescription;

                public List<Sources> getSources() {
                    return sources;
                }

                public String getContentDescription() {
                    return contentDescription;
                }

                public class Sources{
                    private String darkBackgroundUrl;
                    private int widthPixels, heightPixels;
                    private String size;

                    public String getDarkBackgroundUrl() {
                        return darkBackgroundUrl;
                    }

                    public int getWidthPixels() {
                        return widthPixels;
                    }

                    public int getHeightPixels() {
                        return heightPixels;
                    }

                    public String getSize() {
                        return size;
                    }
                }

            }

        }

        public static class Title{
            String mainTitle;
            String subTitle;

            public String getMainTitle() {
                return mainTitle;
            }

            public String getSubTitle() { return subTitle;}
        }

        public static class Image{
           List<source> sources;

            public List<source> getSources() {
                return sources;
            }

            public static class source{
               String size;
               String url;

               public String getSize() {
                   return size;
               }

               public String getUrl() {
                   return url;
               }
           }
        }
    }

    public static class AudioItem{
        String audioItemId;
        Stream stream;

        public String getAudioItemId() {
            return audioItemId;
        }

        public Stream getStream() {
            return stream;
        }
    }
    public static class Stream{
        String url;
        String streamFormat;
        long offsetInMilliseconds;
        String expiryTime;
        String token;
        String expectedPreviousToken;

        public String getUrl() {
            return url;
        }

        public String getStreamFormat() {
            return streamFormat;
        }

        public long getOffsetInMilliseconds() {
            return offsetInMilliseconds;
        }

        public String getExpiryTime() {
            return expiryTime;
        }

        public String getToken() {
            return token;
        }

        public String getExpectedPreviousToken() {
            return expectedPreviousToken;
        }
    }

    public static class DirectiveWrapper{
        Directive directive;
        public Directive getDirective(){
            return directive;
        }
    }
}