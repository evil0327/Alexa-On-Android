package alexademo.ellison.test.alexademo.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import java.util.List;

import alexademo.ellison.test.alexademo.R;
import alexademo.ellison.test.alexademo.connect.AvsTemplateItem;
import alexademo.ellison.test.alexademo.connect.Directive;

public class WeatherTemplateView extends RelativeLayout {
    private ImageView mCloseView;
    private TextView mTitleView, mSubTitleView, mCurrentTemperatureView;
    private ImageView mCurrentWeatherIconView;
    private LinearLayout mForcastLinear;

    public WeatherTemplateView(Context context) {
        super(context);
        init();
    }

    public WeatherTemplateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WeatherTemplateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WeatherTemplateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.weather_template_layout, this);
        mCloseView = findViewById(R.id.close);
        mTitleView = findViewById(R.id.title);
        mSubTitleView = findViewById(R.id.subtitle);
        mCurrentTemperatureView = findViewById(R.id.current_temperature);
        mCurrentWeatherIconView = findViewById(R.id.current_weather_icon);
        mForcastLinear = findViewById(R.id.wather_forecast_linear);
        mCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibility(GONE);
            }
        });
    }

    public void setData(AvsTemplateItem item) {
        mTitleView.setText(item.getPayLoad().getTitle().getMainTitle());
        mSubTitleView.setText(item.getPayLoad().getTitle().getSubTitle());
        mCurrentTemperatureView.setText(item.getPayLoad().getCurrentWeather());
        Picasso.get().load(item.getPayLoad().getCurrentWeatherIcon().getSources().get(0).getDarkBackgroundUrl()).into(mCurrentWeatherIconView);

        setForacst(item.getPayLoad().getWeatherForecast());
    }

    private void setForacst(List<Directive.Payload.WeatherForecast> foracasts) {
        mForcastLinear.removeAllViews();
        //only show 5 days forcast
        for (int i=0;i<5;i++) {
            Directive.Payload.WeatherForecast forecast = foracasts.get(i);
            View v = LayoutInflater.from(getContext()).inflate(R.layout.wather_forecast_layout, null);
            TextView date = v.findViewById(R.id.forcast_title);
            TextView high = v.findViewById(R.id.high_temparature);
            TextView low = v.findViewById(R.id.low_temparature);
            ImageView iv =  v.findViewById(R.id.forcast_weather_icon);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
            lp.weight = 0.2f;

            date.setText(forecast.getDate());
            high.setText(forecast.getHighTemperature());
            low.setText(forecast.getLowTemperature());
            Picasso.get().load(forecast.getImage().getSources().get(0).getDarkBackgroundUrl()).into(iv);

            mForcastLinear.addView(v, lp);
        }
    }

}
