package alexademo.ellison.test.alexademo.connect;

public class AvsTemplateItem extends AvsItem {
   private Directive.Payload mPayLoad;

    public AvsTemplateItem(String token, Directive.Payload payload) {
        super(token);
        mPayLoad = payload;
    }

    public Directive.Payload getPayLoad(){
        return mPayLoad;
    }

    public void setPayLoad(Directive.Payload payload){
        this.mPayLoad = payload;
    }

    public boolean isWeatherType(){
        return mPayLoad.getType().equals(Directive.TYPE_WATHERTEMPLATE);
    }

    public boolean isBodyType(){
        return mPayLoad.getType().contains(Directive.TYPE_BODYTEMPLATE);
    }

}
