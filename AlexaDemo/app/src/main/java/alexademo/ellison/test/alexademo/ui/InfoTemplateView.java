package alexademo.ellison.test.alexademo.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import alexademo.ellison.test.alexademo.R;
import alexademo.ellison.test.alexademo.connect.AvsTemplateItem;

public class InfoTemplateView extends RelativeLayout{
    private TextView mTitleView;
    private TextView mTextFieldView;
    private ImageView mImageView;
    private ImageView mCloseView;

    public InfoTemplateView(Context context) {
        super(context);
        init();
    }

    public InfoTemplateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InfoTemplateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public InfoTemplateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.info_template_layout, this);
        mTitleView = findViewById(R.id.title);
        mTextFieldView = findViewById(R.id.textfield);
        mImageView = findViewById(R.id.image);
        mCloseView = findViewById(R.id.close);
        mCloseView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibility(GONE);
            }
        });

    }

    public void setData(AvsTemplateItem item){
        mTitleView.setText(item.getPayLoad().getTitle().getMainTitle());
        mTextFieldView.setText(item.getPayLoad().getTextField());
        mImageView.setVisibility(GONE);
        if(item.getPayLoad().getImage()!=null && item.getPayLoad().getImage().getSources()!=null){
            Picasso.get().load(item.getPayLoad().getImage().getSources().get(0).getUrl()).into(mImageView);
            mImageView.setVisibility(VISIBLE);
        }
    }

}
