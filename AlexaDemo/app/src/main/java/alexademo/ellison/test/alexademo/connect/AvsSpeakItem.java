package alexademo.ellison.test.alexademo.connect;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class AvsSpeakItem extends AvsItem {
    private String mCid;
    private byte[] mAudio;

    public AvsSpeakItem(String token, String cid, ByteArrayInputStream audio) throws IOException {
        this(token, cid, IOUtils.toByteArray(audio));
        audio.close();
    }

    public AvsSpeakItem(String token, String cid, byte[] audio){
        super(token);
        mCid = cid;
        mAudio = audio;
    }

    public String getCid() {
        return mCid;
    }

    public byte[] getAudio() {
        return mAudio;
    }
}
