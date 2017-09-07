package util;

import beans.auth.HqUserDetailsBean;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;

public class FormplayerHttpRequest extends MultipleReadHttpRequest {
    private HqUserDetailsBean userDetails;
    private String domain;

    public FormplayerHttpRequest(HttpServletRequest request) {
        super(request);
        setDomain();
    }

    public void setUserDetails(HqUserDetailsBean userDetails) {
        this.userDetails = userDetails;
    }

    public HqUserDetailsBean getUserDetails() {
        return userDetails;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    private void setDomain() {
        JSONObject data;
        try {
            data = RequestUtils.getPostData(this);
        } catch(JSONException e) {
            setDomain(null);
            return;
        }
        if (data.has("domain")) {
            setDomain(data.getString("domain"));
        } else {
            setDomain(null);
        }
    }

    public void assertDomain() {
        if (domain == null) {
            throw new RuntimeException("No domain specified for the request: " + getRequestURI());
        }
    }

    public String getDomain() {
        return domain;
    }
}
