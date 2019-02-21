package alexademo.ellison.test.alexademo.connect;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Response;

public class ApiParser {
    private static final String TAG = ApiParser.class.getName();
    private static final Pattern PATTERN = Pattern.compile("<(.*?)>");

    public static List<AvsItem> parse(InputStream stream, String boundary) throws IOException {
        long start = System.currentTimeMillis();

        List<Directive> directives = new ArrayList<>();
        HashMap<String, ByteArrayInputStream> audio = new HashMap<>();

        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(stream);
        } catch (IOException exp) {
            exp.printStackTrace();
            Log.e(TAG, "Error copying bytes[]");
        }

        String responseString = new String(bytes);
        MultipartStream mpStream = new MultipartStream(new ByteArrayInputStream(bytes), boundary.getBytes(), 100000);
        Log.i(TAG, "Response Body: \n" + responseString);
        if (mpStream.skipPreamble()) {
            Log.i(TAG, "Found initial boundary: true");

            //we have to use the count hack here because otherwise readBoundary() throws an exception
            int count = 0;
            while (count < 1 || mpStream.readBoundary()) {
                String headers;
                try {
                    headers = mpStream.readHeaders();
                } catch (MultipartStream.MalformedStreamException exp) {
                    break;
                }
                ByteArrayOutputStream data = new ByteArrayOutputStream();
                mpStream.readBodyData(data);
                if (!isJson(headers)) {
                    // get the audio data
                    //convert our multipart into byte data
                    String contentId = getCID(headers);
                    if(contentId != null) {
                        Matcher matcher = PATTERN.matcher(contentId);
                        if (matcher.find()) {
                            String currentId = "cid:" + matcher.group(1);
                            audio.put(currentId, new ByteArrayInputStream(data.toByteArray()));
                        }
                    }
                } else {
                    // get the json directive
                    String directive = data.toString(Charset.defaultCharset().displayName());
                    log(directive);
                    if(!TextUtils.isEmpty(directive) && directive.toLowerCase().contains("PausePrompt")){
                        continue;
                    }
                    directives.add(getDirective(directive));
                }
                count++;
            }

        } else {
            Log.i(TAG, "Response Body: \n" + new String(bytes));
            try {
                directives.add(getDirective(responseString));
            }catch (JsonParseException e) {
                e.printStackTrace();
            }
        }

        List<AvsItem> result = new ArrayList<AvsItem>();
        for (Directive directive: directives) {
            Log.i(TAG, "Parsing directive type: "+directive.getHeader().getNamespace()+":"+directive.getHeader().getName());
            AvsItem item = null;
            if(directive.isTypeSpeak()){
                String cid = directive.getPayload().getUrl();
                ByteArrayInputStream sound = audio.get(cid);
                item = new AvsSpeakItem(directive.getPayload().getToken(), cid, sound);
            }else if(directive.isTypeTemplate()){
                item = new AvsTemplateItem(directive.getPayload().getToken(), directive.getPayload());
            }else{
                Log.e(TAG, "Unknown type found");
            }

            if(item != null){
                result.add(item);
            }
        }

        Log.i(TAG, "Parsing response took: " + (System.currentTimeMillis() - start) +" size is " + result.size());
        return result;
    }


    private static Directive getDirective(String directive) {
        Gson gson = new Gson();
        Directive.DirectiveWrapper wrapper = gson.fromJson(directive, Directive.DirectiveWrapper.class);
        if (wrapper.getDirective() == null) {
            return gson.fromJson(directive, Directive.class);
        }
        return wrapper.getDirective();
    }

    private static String getCID(String headers) throws IOException {
        final String contentString = "Content-ID:";
        BufferedReader reader = new BufferedReader(new StringReader(headers));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (line.startsWith(contentString)) {
                return line.substring(contentString.length()).trim();
            }
        }
        return null;
    }

    private static boolean isJson(String headers) {
        if (headers.contains("application/json")) {
            return true;
        }
        return false;
    }

    //for printing long log
    private static void log(String message){
        int maxLogSize = 2000;
        for(int i = 0; i <= message.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i+1) * maxLogSize;
            end = end > message.length() ? message.length() : end;
            Log.i(TAG, message.substring(start, end));
        }
    }
}
