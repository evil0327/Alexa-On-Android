package alexademo.ellison.test.alexademo.connect;

import java.util.ArrayList;
import java.util.List;

public class ApiResponse {
   private List<AvsItem> avsItems;
   private int responseCode;
   private String message;

    public List<AvsItem> getAvsItems() {
        return avsItems;
    }

    public void setAvsItems(List<AvsItem> avsItems) {
        this.avsItems = avsItems;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
